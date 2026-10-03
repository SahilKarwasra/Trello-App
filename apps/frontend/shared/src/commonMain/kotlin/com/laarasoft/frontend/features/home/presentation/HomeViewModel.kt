package com.laarasoft.frontend.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.config.network.onError
import com.laarasoft.frontend.config.network.onSuccess
import com.laarasoft.frontend.config.network.sendSnackbarOnError
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.home.domain.repository.BoardRepository
import com.laarasoft.frontend.features.organisation.domain.repository.OrganisationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val boardRepository: BoardRepository,
    private val organisationRepository: OrganisationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeState()
    )

    private val _events = Channel<HomeEvents>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.Init -> {
                if (_state.value.orgId != action.orgId || _state.value.boards.isEmpty()) {
                    _state.update {
                        it.copy(
                            orgId = action.orgId,
                            orgName = action.orgName
                        )
                    }
                    loadBoards(action.orgId)
                }
            }

            is HomeAction.OnRefresh -> {
                val currentOrgId = _state.value.orgId
                if (currentOrgId.isNotBlank()) {
                    _state.update { it.copy(isRefreshing = true) }
                    loadBoards(currentOrgId, isRefresh = true)
                }
            }

            is HomeAction.OnShowCreateBoardDialog -> {
                _state.update { it.copy(showCreateBoardDialog = true) }
            }

            is HomeAction.OnDismissCreateBoardDialog -> {
                _state.update {
                    it.copy(
                        showCreateBoardDialog = false,
                        newBoardTitle = ""
                    )
                }
            }

            is HomeAction.OnNewBoardTitleChange -> {
                _state.update { it.copy(newBoardTitle = action.title) }
            }

            is HomeAction.OnCreateBoard -> {
                createBoard()
            }

            is HomeAction.OnShowInviteDialog -> {
                _state.update { it.copy(showInviteDialog = true) }
            }

            is HomeAction.OnDismissInviteDialog -> {
                _state.update {
                    it.copy(
                        showInviteDialog = false,
                        inviteUsername = ""
                    )
                }
            }

            is HomeAction.OnInviteUsernameChange -> {
                _state.update { it.copy(inviteUsername = action.username) }
            }

            is HomeAction.OnInviteMember -> {
                inviteMember()
            }

            is HomeAction.OnChangeOrgClick -> {
                viewModelScope.launch {
                    _events.send(HomeEvents.NavigateToChangeOrg)
                }
            }

            is HomeAction.OnBoardClick -> {
                viewModelScope.launch {
                    _events.send(HomeEvents.NavigateToKanban(action.board.id, action.board.title))
                }
            }
        }
    }

    private fun loadBoards(orgId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.update { it.copy(isLoadingBoards = true, errorMessage = null) }
            }
            boardRepository.getBoards(orgId)
                .onSuccess { boards ->
                    _state.update {
                        it.copy(
                            boards = boards,
                            isLoadingBoards = false,
                            isRefreshing = false
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoadingBoards = false,
                            isRefreshing = false,
                            errorMessage = error.message
                        )
                    }
                }
                .sendSnackbarOnError()
        }
    }

    private fun createBoard() {
        val title = _state.value.newBoardTitle.trim()
        val orgId = _state.value.orgId

        if (title.isBlank()) {
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar("Board title cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreatingBoard = true) }
            boardRepository.createBoard(title = title, organizationId = orgId)
                .onSuccess { newBoard ->
                    _state.update { current ->
                        current.copy(
                            isCreatingBoard = false,
                            showCreateBoardDialog = false,
                            newBoardTitle = "",
                            boards = current.boards + newBoard
                        )
                    }
                    UiEventController.send(UiEvent.Snackbar("Board \"${newBoard.title}\" created!"))
                }
                .onError {
                    _state.update { it.copy(isCreatingBoard = false) }
                }
                .sendSnackbarOnError()
        }
    }

    private fun inviteMember() {
        val username = _state.value.inviteUsername.trim()
        val orgId = _state.value.orgId

        if (username.isBlank()) {
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar("Username cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isInvitingMember = true) }
            organisationRepository.inviteMember(orgId = orgId, username = username)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isInvitingMember = false,
                            showInviteDialog = false,
                            inviteUsername = ""
                        )
                    }
                    UiEventController.send(UiEvent.Snackbar("Invitation sent to @$username!"))
                }
                .onError {
                    _state.update { it.copy(isInvitingMember = false) }
                }
                .sendSnackbarOnError()
        }
    }
}