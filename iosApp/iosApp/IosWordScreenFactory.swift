import SwiftUI
import composeApp

class IosWordScreenFactory: IosFactoryProvider {
    func createWordScreen(viewModel: AppViewModel) -> Any {
        let iosVM = iosAppViewModel(commonVm: viewModel)
        return UIHostingController(rootView: WordScreenView(vm: iosVM))
    }
}