package com.laarasoft.frontend.features.home.presentation

sealed interface HomeEvents {
    data object NavigateToChangeOrg : HomeEvents
    data class NavigateToKanban(val boardId: String, val boardTitle: String) : HomeEvents
}