package com.laarasoft.frontend.features.home.presentation

import com.laarasoft.frontend.features.home.domain.model.Board

data class HomeState(
    val orgId: String = "",
    val orgName: String = "",
    val boards: List<Board> = emptyList(),
    val isLoadingBoards: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Create Board dialog state
    val showCreateBoardDialog: Boolean = false,
    val newBoardTitle: String = "",
    val isCreatingBoard: Boolean = false,

    // Invite Member dialog state
    val showInviteDialog: Boolean = false,
    val inviteUsername: String = "",
    val isInvitingMember: Boolean = false
)