import SwiftUI
import Shared

extension Letter: Identifiable {}

struct WordScreenView: View {
    @StateObject var vm: iosAppViewModel

    var body: some View {
        VStack {
            // Match Android UI Header
            Text("Current Word Score: \(vm.currentScore)")
                .font(.system(size: 22, weight: .bold))
            Text("Total Score: \(vm.totalScore)")
                .font(.system(size: 18))
            Text("Words Built: \(vm.numWords)")
                .font(.system(size: 18))
            Text("Time Elapsed: \(vm.currentTime)")
                .font(.system(size: 22))

            Spacer().frame(height: 20)

            HStack(spacing: 40) {
                Button("Reshuffle") {
                    vm.shuffleLetters()
                }
                .buttonStyle(.borderedProminent)

                Button("Record Word") {
                    if vm.isValidWord() {
                        vm.validWordCreated()
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(vm.currentScore == 0)
            }

            Spacer().frame(height: 32)

            // Top Group (Target)
            LetterGroup(letters: $vm.targetLetters, groupName: "Top", onRemoveLetter: { removePos in
                vm.moveTo(group: .stock, pos: removePos)
            }) { arr in
                vm.rearrangeLetters(group: .centerbox, arr: arr)
            }

            Spacer().frame(height: 32)

            // Bottom Group (Stock)
            LetterGroup(letters: $vm.sourceLetters, groupName: "Bottom", onRemoveLetter: { removePos in
               vm.moveTo(group: .centerbox, pos: removePos)
            }) { arr in
                vm.rearrangeLetters(group: .stock, arr: arr)
            }
            Spacer()
            Group {
                Text("Double tap to move among groups")
                Text("Drag to move within group")
            }
            .font(.system(size: 13))
            .italic()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
}