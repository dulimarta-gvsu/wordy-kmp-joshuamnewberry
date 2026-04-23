package edu.gvsu.cis.kmp_wordy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import platform.UIKit.UIViewController

@Composable
actual fun WordScreen(viewModel: AppViewModel) {
    Column {
        UIKitViewController(
            factory = {
                val factory = WordScreenFactory()
                factory.createWordScreen(viewModel) as UIViewController
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}