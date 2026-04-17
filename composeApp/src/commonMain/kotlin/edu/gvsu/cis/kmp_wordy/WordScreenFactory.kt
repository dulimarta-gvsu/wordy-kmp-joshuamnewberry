package edu.gvsu.cis.kmp_wordy

expect class WordScreenFactory() {
    fun createWordScreen(viewModel: AppViewModel): Any
}