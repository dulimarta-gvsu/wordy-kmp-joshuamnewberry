import SwiftUI
import composeApp

struct ComposeViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(factory: WordScreenFactory())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@main
struct iOSApp: App {
    init() {
        // Provide the Kotlin interface with the Swift implementation
        WordScreenFactory.companion.provider = IosWordScreenFactory()
    }

    var body: some Scene {
        WindowGroup {
            ComposeViewController()
                .ignoresSafeArea(.all)
        }
    }
}