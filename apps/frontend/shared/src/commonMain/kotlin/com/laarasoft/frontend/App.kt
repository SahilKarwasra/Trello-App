package com.laarasoft.frontend

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.laarasoft.frontend.config.navigation.AppNavigation
import com.laarasoft.frontend.core.theme.TrelloTheme

@Composable
@Preview
fun App() {
    TrelloTheme(darkTheme = false) {
        AppNavigation()
    }
}
