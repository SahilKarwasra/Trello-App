package com.laarasoft.frontend.features.home.presentation

import com.laarasoft.frontend.features.board.domain.model.Board

sealed interface HomeAction {
    data class Init(val orgId: String, val orgName: String) : HomeAction
    data object OnRefresh : HomeAction

    // Board Creation
    data object OnShowCreateBoardDialog : HomeAction
    data object OnDismissCreateBoardDialog : HomeAction
    data class OnNewBoardTitleChange(val title: String) : HomeAction
    data object OnCreateBoard : HomeAction

    // Member Invitation
    data object OnShowInviteDialog : HomeAction
    data object OnDismissInviteDialog : HomeAction
    data class OnInviteUsernameChange(val username: String) : HomeAction
    data object OnInviteMember : HomeAction

    // Navigation & Interaction
    data object OnChangeOrgClick : HomeAction
    data class OnBoardClick(val board: Board) : HomeAction
}