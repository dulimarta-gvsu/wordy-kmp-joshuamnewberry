import SwiftUI
import composeApp

class IosWordScreenFactory: WordScreenFactory {
    func createWordScreen(viewModel: AppViewModel) -> Any {
        return UIHostingController(rootView: WordScreenView(viewModel: viewModel))
    }
}