package com.laarasoft.frontend.features.splash.presentation

sealed interface SplashAction {
    data object Init: SplashAction
}