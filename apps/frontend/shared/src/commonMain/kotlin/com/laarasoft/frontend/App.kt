package com.laarasoft.frontend

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.laarasoft.frontend.config.di.initKoin
import com.laarasoft.frontend.config.navigation.AppNavigation
import com.laarasoft.frontend.core.theme.TrelloTheme
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    initKoin()
    KoinContext {
        TrelloTheme {
            AppNavigation()
        }
    }
}
