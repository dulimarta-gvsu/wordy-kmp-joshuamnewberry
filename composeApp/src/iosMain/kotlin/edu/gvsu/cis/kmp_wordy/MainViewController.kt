package edu.gvsu.cis.kmp_wordy

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

val LocalWordScreenFactory = staticCompositionLocalOf<WordScreenFactory> {
    error("WordScreenFactory not provided! Check MainViewController setup.")
}

fun MainViewController(factory:WordScreenFactory): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(LocalWordScreenFactory provides factory) {
        App()
    }
}