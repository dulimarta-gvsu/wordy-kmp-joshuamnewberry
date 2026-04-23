package edu.gvsu.cis.kmp_wordy

interface IosFactoryProvider {
    fun createWordScreen(viewModel: AppViewModel): Any
}

actual class WordScreenFactory actual constructor() {
    companion object {
        lateinit var provider: IosFactoryProvider
    }

    actual fun createWordScreen(viewModel: AppViewModel): Any {
        return provider.createWordScreen(viewModel)
    }
}

