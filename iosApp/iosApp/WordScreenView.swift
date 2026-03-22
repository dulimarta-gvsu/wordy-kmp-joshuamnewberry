import SwiftUI
import composeApp

extension Letter: Identifiable {}

struct ContentView: View {
    @StateObject var vm: iosAppViewModel

    init(viewModel: iosAppViewModel) {
        self._vm = StateObject(wrappedValue: iosAppViewModel(commonVm: AppViewModel()))
    }
    @State var letterSize: Int = 40

    var body: some View {
        VStack {
            Button("Restart") {
                vm.selectRandomLetters()
                print(vm.sourceLetters)
            }
            LetterGroup(letters: $vm.targetLetters, groupName: "Top", onRemoveLetter: { removePos in
                vm.moveTo(group: Origin.stock, pos: removePos)
            }) { arr in
                vm.rearrangeLetters(group: Origin.centerbox, arr: arr)

            }
            LetterGroup(letters: $vm.sourceLetters, groupName: "Bottom", onRemoveLetter: { removePos in
               vm.moveTo(group: Origin.centerbox, pos: removePos)
            } )  { arr in
                vm.rearrangeLetters(group: Origin.stock, arr: arr)
            }
            Spacer()
            Group {
                Text("Double tap to move among groups")
                Text("Drag to move within group")
            }
                .font(.system(size: 13))
                .italic(true)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
}

struct LetterGroup: View {
    @Binding var letters: [Letter?]
    var groupName: String
    var onRemoveLetter: (Int) -> Void
    var onRearrangeLetters: ([Letter]) -> Void

    @State var boxSize = CGSize.zero
    @State var startCellIndex: Int? = nil
    @State var blankCellIndex: Int? = nil
    @State var pointerIndex: Float? = nil
    @State var dragOffset = CGPoint.zero
    @State var draggedLetter: Letter? = nil
    @State var startPointerPosition = CGPoint.zero
    @GestureState var what = false
    var body: some View {
        ZStack {
            let letterSize = min(80, (UIScreen.main.bounds.width - 16) / CGFloat(letters.count))
            // The extra BigLetter position aligns with the center
            // of the HStack. So the dragoffset must be adjusted by
            // the relative position of the starting drag point w.r.t
            // to the HStack center
            if draggedLetter != nil {
                BigLetter(letter: draggedLetter, size: letterSize)
                    .offset(x: draggedLetter == nil ? 0 : dragOffset.x + startPointerPosition.x - boxSize.width / 2, y: dragOffset.y)
            }
            HStack(spacing: 2) {
                if letters.count > 0 {
                    ForEach(Array(self.letters.enumerated()), id: \.offset) { index, letter in
                        BigLetter(letter: letter, size: letterSize)
                            .gesture(TapGesture(count: 2) // Count:2 => double tap
                                .onEnded { s in
                                    onRemoveLetter(index)
                                }
                            )
                    }
                } else {
                    // Show a blank box if there are no letters
                    BigLetter(letter: nil, size: letterSize)
                }
            }
            .onGeometryChange(for: CGSize.self,
                              of: { $0.size as CGSize},
                              action: {
                boxSize = $0
            })
            .gesture(DragGesture()
                .onChanged { drag in
                    // Determine the letter box index from the current position
                    // of the pointer
                    let percentage = drag.location.x / boxSize.width
                    var index = percentage * CGFloat(letters.count)
                    startPointerPosition = drag.startLocation
                    // Make sure that index in inbound [0,N-1]
                    if index < 0 {
                        index = 0
                    } else if index > CGFloat(letters.count - 1) {
                        index = CGFloat(letters.count - 1)
                    }
                    if draggedLetter == nil { // Not currently dragging
                        blankCellIndex = Int(index)
                        draggedLetter = letters[blankCellIndex!]
                        // replace the spot with a nil
                        letters[blankCellIndex!] = nil
                    }
                    // remember the start index, in case we cancel dropping
                    if startCellIndex == nil {
                        startCellIndex = Int(index)
                    }
                    // If the pointer is no longer on the "blank" box
                    if blankCellIndex != Int(index) {
                        letters[blankCellIndex!] = letters[Int(index)]
                        letters[Int(index)] = nil
                        blankCellIndex = Int(index)
                    }
                    pointerIndex = Float(index)
                    // Compute the amount of total drag
                    dragOffset = CGPoint(x: drag.location.x - drag.startLocation.x,
                                         y: drag.location.y - drag.startLocation.y)
                }
                .onEnded { _ in
                    // print("Drag gesture ended")
                    letters[blankCellIndex!] = draggedLetter
                    draggedLetter = nil
                    pointerIndex = nil
                    startCellIndex = nil
                    blankCellIndex = nil
                    startPointerPosition = CGPoint.zero
                    dragOffset = CGPoint.zero
                    // Inform the viewmodel to readjust array
                    self.onRearrangeLetters(letters as! [Letter])
                }
            )
        }
    }
}

struct BigLetter: View {
    private let ch: String
    let size: CGFloat
    init(letter: Letter?, size: CGFloat = 44) {
        if let letter {
            self.ch = String(UnicodeScalar(letter.text)!)
        } else {
            self.ch = ""
        }
        self.size = size
    }
    var body: some View {
        Text(self.ch)
            .frame(width: self.size, height: self.size)
            .font(Font.system(size: 0.8 * self.size, weight: .bold))
            .background(ch == "" ? .clear : .mint)
            .cornerRadius(10)
            .overlay(RoundedRectangle(cornerRadius: 10)
                .stroke(.black, lineWidth: 2))
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
