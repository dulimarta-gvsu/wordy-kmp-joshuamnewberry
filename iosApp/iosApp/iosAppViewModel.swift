import Foundation
import Shared

@MainActor
class iosAppViewModel: ObservableObject {
    private let commonVm: AppViewModel

    @Published var sourceLetters: Array<Letter?> = []
    @Published var targetLetters: Array<Letter?> = []

    // UI State
    @Published var currentScore: Int = 0
    @Published var totalScore: Int = 0
    @Published var numWords: Int = 0
    @Published var currentTime: Int64 = 0

    init(commonVm: AppViewModel) {
        self.commonVm = commonVm

        // Arrays
        self.commonVm.sourceLetters.subscribe(scope: commonVm.viewModelScope) {
            self.sourceLetters = $0 as? Array<Letter?> ?? []
        }
        self.commonVm.targetLetters.subscribe(scope: commonVm.viewModelScope) {
            self.targetLetters = $0 as? Array<Letter?> ?? []
        }

        // Game State
        self.commonVm.currentScore.subscribe(scope: commonVm.viewModelScope) {
            self.currentScore = $0?.intValue ?? 0
        }
        self.commonVm.totalScore.subscribe(scope: commonVm.viewModelScope) {
            self.totalScore = $0?.intValue ?? 0
        }
        self.commonVm.currentTime.subscribe(scope: commonVm.viewModelScope) {
            self.currentTime = $0?.int64Value ?? 0
        }
        self.commonVm.sessionList.subscribe(scope: commonVm.viewModelScope) {
            self.numWords = ($0 as? Array<GameSession>)?.count ?? 0
        }
    }

    // Handles the double-tap movement logic for iOS
    func moveTo(group: Origin, pos: Int) {
        var source = self.sourceLetters.compactMap { $0 }
        var target = self.targetLetters.compactMap { $0 }

        if group == .stock { // Move from CenterBox to Stock
            if pos < target.count {
                let letter = target.remove(at: pos)
                source.append(letter)
            }
        } else { // Move from Stock to CenterBox
            if pos < source.count {
                let letter = source.remove(at: pos)
                target.append(letter)
            }
        }

        self.commonVm.rearrangeLetters(group: .centerbox, arr: target)
        self.commonVm.rearrangeLetters(group: .stock, arr: source)
    }

    func rearrangeLetters(group: Origin, arr: [Letter]) {
        self.commonVm.rearrangeLetters(group: group, arr: arr)
    }

    func selectRandomLetters() { self.commonVm.selectRandomLetters() }
    func shuffleLetters() { self.commonVm.shuffleLetters() }
    func isValidWord() -> Bool { return self.commonVm.isValidWord() }
    func validWordCreated() { self.commonVm.validWordCreated() }
}