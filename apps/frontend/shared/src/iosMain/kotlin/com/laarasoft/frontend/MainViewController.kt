package com.laarasoft.frontend

import androidx.compose.ui.window.ComposeUIViewController
import com.laarasoft.frontend.config.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }