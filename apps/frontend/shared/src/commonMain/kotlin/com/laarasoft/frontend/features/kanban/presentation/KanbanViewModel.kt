package com.laarasoft.frontend.features.kanban.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.config.network.onError
import com.laarasoft.frontend.config.network.onSuccess
import com.laarasoft.frontend.config.network.sendSnackbarOnError
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.kanban.domain.models.Issue
import com.laarasoft.frontend.features.kanban.domain.models.Section
import com.laarasoft.frontend.features.kanban.domain.repository.SectionRepository
import com.laarasoft.frontend.features.websocket.domain.models.BoardEvent
import com.laarasoft.frontend.features.websocket.domain.repository.RealtimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KanbanViewModel(
    private val sectionRepository: SectionRepository,
    private val realtimeRepository: RealtimeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(KanbanState())
    val state = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = KanbanState()
    )

    private val _events = Channel<KanbanEvents>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var wsJob: Job? = null

    fun onAction(action: KanbanAction) {
        when (action) {
            is KanbanAction.Init -> {
                val isNewBoard = _state.value.boardId != action.boardId
                val hasNoSections = _state.value.sections.isEmpty()
                val isNotObserving = wsJob?.isActive != true

                if (isNewBoard || hasNoSections) {
                    _state.update {
                        it.copy(
                            boardId = action.boardId,
                            boardTitle = action.boardTitle,
                            sections = if (isNewBoard) emptyList() else it.sections,
                            onlineCount = 0,
                            activeUsers = emptyList()
                        )
                    }
                    loadSections(action.boardId)
                }

                if (isNewBoard || isNotObserving) {
                    observeBoardEvents(action.boardId)
                }
            }

            is KanbanAction.OnRefresh -> {
                val boardId = _state.value.boardId
                if (boardId.isNotBlank()) {
                    _state.update { it.copy(isRefreshing = true) }
                    loadSections(boardId, isRefresh = true)
                }
            }

            is KanbanAction.OnShowCreateSectionDialog -> {
                _state.update { it.copy(showCreateSectionDialog = true) }
            }

            is KanbanAction.OnDismissCreateSectionDialog -> {
                _state.update {
                    it.copy(showCreateSectionDialog = false, newSectionTitle = "")
                }
            }

            is KanbanAction.OnNewSectionTitleChange -> {
                _state.update { it.copy(newSectionTitle = action.title) }
            }

            is KanbanAction.OnCreateSection -> {
                createSection()
            }

            // Issue creation
            is KanbanAction.OnShowCreateIssueDialog -> {
                _state.update {
                    it.copy(
                        showCreateIssueDialog = true,
                        createIssueSectionId = action.sectionId,
                        newIssueTitle = "",
                        newIssueDescription = ""
                    )
                }
            }

            is KanbanAction.OnDismissCreateIssueDialog -> {
                _state.update {
                    it.copy(
                        showCreateIssueDialog = false,
                        createIssueSectionId = null,
                        newIssueTitle = "",
                        newIssueDescription = ""
                    )
                }
            }

            is KanbanAction.OnNewIssueTitleChange -> {
                _state.update { it.copy(newIssueTitle = action.title) }
            }

            is KanbanAction.OnNewIssueDescriptionChange -> {
                _state.update { it.copy(newIssueDescription = action.description) }
            }

            is KanbanAction.OnCreateIssue -> {
                createIssue()
            }

            // Drag and Drop
            is KanbanAction.ReorderSections -> {
                reorderSections(action.fromIndex, action.toIndex)
            }

            is KanbanAction.MoveIssue -> {
                moveIssue(
                    fromSectionId = action.fromSectionId,
                    toSectionId = action.toSectionId,
                    issueId = action.issueId,
                    targetIndex = action.targetIndex
                )
            }

            is KanbanAction.OnBackClick -> {
                leaveBoard()
                viewModelScope.launch {
                    _events.send(KanbanEvents.NavigateBack)
                }
            }

            is KanbanAction.OnDispose -> {
                leaveBoard()
            }

            is KanbanAction.OnSwitchBoardClick -> {
                _state.update { it.copy(showBoardSwitcher = true) }
            }

            is KanbanAction.OnDismissBoardSwitcher -> {
                _state.update { it.copy(showBoardSwitcher = false) }
            }

            is KanbanAction.OnTogglePresencePanel -> {
                _state.update { it.copy(showPresencePanel = !it.showPresencePanel) }
            }
        }
    }

    // ── WebSocket Event Handling ──────────────────────────────────────

    private fun observeBoardEvents(boardId: String) {
        wsJob?.cancel()
        wsJob = viewModelScope.launch {
            realtimeRepository.observeBoard(boardId).collect { event ->
                handleBoardEvent(event)
            }
        }
    }

    private fun handleBoardEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.SectionCreated -> {
                _state.update { current ->
                    val exists = current.sections.any { it.id == event.sectionId }
                    if (exists) current
                    else {
                        val newSection = Section(
                            id = event.sectionId,
                            title = event.title,
                            boardId = event.boardId,
                            position = event.position,
                            issues = emptyList()
                        )
                        current.copy(
                            sections = (current.sections + newSection).sortedBy { it.position }
                        )
                    }
                }
            }

            is BoardEvent.SectionUpdated -> {
                _state.update { current ->
                    current.copy(
                        sections = current.sections.map { sec ->
                            if (sec.id == event.sectionId) sec.copy(
                                title = event.title,
                                position = event.position
                            ) else sec
                        }.sortedBy { it.position }
                    )
                }
            }

            is BoardEvent.SectionMoved -> {
                // Full board refresh to get accurate positions for all sections
                loadSections(_state.value.boardId, isRefresh = true)
            }

            is BoardEvent.SectionDeleted -> {
                _state.update { current ->
                    current.copy(
                        sections = current.sections.filterNot { it.id == event.sectionId }
                    )
                }
            }

            is BoardEvent.IssueCreated -> {
                _state.update { current ->
                    current.copy(
                        sections = current.sections.map { sec ->
                            if (sec.id == event.sectionId) {
                                val exists = sec.issues.any { it.id == event.issueId }
                                if (exists) sec
                                else {
                                    val newIssue = Issue(
                                        id = event.issueId,
                                        title = event.title,
                                        description = event.description,
                                        sectionId = event.sectionId,
                                        createdBy = event.createdBy,
                                        position = event.position,
                                    )
                                    sec.copy(
                                        issues = (sec.issues + newIssue).sortedBy { it.position }
                                    )
                                }
                            } else sec
                        }
                    )
                }
            }

            is BoardEvent.IssueMoved -> {
                // Full board refresh to get accurate positions for all issues
                loadSections(_state.value.boardId, isRefresh = true)
            }

            is BoardEvent.IssueDeleted -> {
                _state.update { current ->
                    current.copy(
                        sections = current.sections.map { sec ->
                            sec.copy(
                                issues = sec.issues.filterNot { it.id == event.issueId }
                            )
                        }
                    )
                }
            }

            is BoardEvent.UserJoined -> {
                _state.update { current ->
                    val updated = current.activeUsers
                        .filterNot { it.userId == event.userId } +
                        BoardEvent.UserPresence(event.userId, event.username)
                    current.copy(
                        onlineCount = event.onlineCount,
                        activeUsers = updated,
                    )
                }
            }

            is BoardEvent.UserLeft -> {
                _state.update { current ->
                    current.copy(
                        onlineCount = event.onlineCount,
                        activeUsers = current.activeUsers.filterNot { it.userId == event.userId },
                    )
                }
            }

            is BoardEvent.RoomState -> {
                _state.update { current ->
                    current.copy(
                        onlineCount = event.onlineCount,
                        activeUsers = event.activeUsers,
                    )
                }
            }

            is BoardEvent.ResyncRequired -> {
                loadSections(_state.value.boardId, isRefresh = true)
            }
        }
    }

    // ── Optimistic Drag & Drop (with API sync) ──────────────────────

    private fun reorderSections(fromIndex: Int, toIndex: Int) {
        val currentSections = _state.value.sections.toMutableList()
        if (fromIndex in currentSections.indices && toIndex in 0..currentSections.size) {
            val moved = currentSections.removeAt(fromIndex)
            val safeIndex = toIndex.coerceIn(0, currentSections.size)
            currentSections.add(safeIndex, moved)
            val reindexed = currentSections.mapIndexed { index, sec ->
                sec.copy(position = index + 1)
            }
            _state.update { it.copy(sections = reindexed) }

            // Sync with backend
            val newPosition = safeIndex + 1
            viewModelScope.launch {
                sectionRepository.moveSection(moved.id, newPosition)
                    .onError {
                        // Revert on failure by reloading
                        loadSections(_state.value.boardId, isRefresh = true)
                    }
                    .sendSnackbarOnError()
            }
        }
    }

    private fun moveIssue(
        fromSectionId: String,
        toSectionId: String,
        issueId: String,
        targetIndex: Int
    ) {
        val currentSections = _state.value.sections
        val fromSection = currentSections.find { it.id == fromSectionId } ?: return
        val toSection = currentSections.find { it.id == toSectionId } ?: return
        val issueToMove = fromSection.issues.find { it.id == issueId } ?: return

        val updatedIssue = issueToMove.copy(sectionId = toSectionId)

        if (fromSectionId == toSectionId) {
            val updatedIssues = fromSection.issues.toMutableList()
            val currentIndex = updatedIssues.indexOfFirst { it.id == issueId }
            if (currentIndex != -1) {
                updatedIssues.removeAt(currentIndex)
                val safeIndex = targetIndex.coerceIn(0, updatedIssues.size)
                updatedIssues.add(safeIndex, updatedIssue)
                val reindexedIssues = updatedIssues.mapIndexed { idx, iss ->
                    iss.copy(position = idx + 1)
                }
                val newSections = currentSections.map { sec ->
                    if (sec.id == fromSectionId) sec.copy(issues = reindexedIssues) else sec
                }
                _state.update { it.copy(sections = newSections) }

                // Sync with backend
                val newPosition = targetIndex.coerceIn(0, updatedIssues.size - 1) + 1
                viewModelScope.launch {
                    sectionRepository.moveIssue(issueId, toSectionId, newPosition)
                        .onError {
                            loadSections(_state.value.boardId, isRefresh = true)
                        }
                        .sendSnackbarOnError()
                }
            }
        } else {
            val fromIssues = fromSection.issues
                .filterNot { it.id == issueId }
                .mapIndexed { idx, iss -> iss.copy(position = idx + 1) }

            val toIssues = toSection.issues.toMutableList()
            val safeIndex = targetIndex.coerceIn(0, toIssues.size)
            toIssues.add(safeIndex, updatedIssue)
            val reindexedToIssues = toIssues.mapIndexed { idx, iss ->
                iss.copy(position = idx + 1)
            }

            val newSections = currentSections.map { sec ->
                when (sec.id) {
                    fromSectionId -> sec.copy(issues = fromIssues)
                    toSectionId -> sec.copy(issues = reindexedToIssues)
                    else -> sec
                }
            }
            _state.update { it.copy(sections = newSections) }

            // Sync with backend
            val newPosition = safeIndex + 1
            viewModelScope.launch {
                sectionRepository.moveIssue(issueId, toSectionId, newPosition)
                    .onError {
                        loadSections(_state.value.boardId, isRefresh = true)
                    }
                    .sendSnackbarOnError()
            }
        }
    }

    // ── Create Issue (API call) ─────────────────────────────────────

    private fun createIssue() {
        val title = _state.value.newIssueTitle.trim()
        val description = _state.value.newIssueDescription.trim()
        val sectionId = _state.value.createIssueSectionId ?: return

        if (title.isBlank()) {
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar("Card title cannot be empty"))
            }
            return
        }

        val currentSections = _state.value.sections
        val targetSection = currentSections.find { it.id == sectionId } ?: return
        val position = targetSection.issues.size + 1

        viewModelScope.launch {
            _state.update { it.copy(isCreatingIssue = true) }
            sectionRepository.createIssue(
                title = title,
                description = description,
                sectionId = sectionId,
                position = position
            )
                .onSuccess { newIssue ->
                    _state.update { current ->
                        val updatedSections = current.sections.map { sec ->
                            if (sec.id == sectionId) {
                                val exists = sec.issues.any { it.id == newIssue.id }
                                if (exists) sec
                                else sec.copy(issues = (sec.issues + newIssue).sortedBy { it.position })
                            } else sec
                        }
                        current.copy(
                            sections = updatedSections,
                            showCreateIssueDialog = false,
                            newIssueTitle = "",
                            newIssueDescription = "",
                            createIssueSectionId = null,
                            isCreatingIssue = false
                        )
                    }
                    UiEventController.send(UiEvent.Snackbar("Card \"$title\" added!"))
                }
                .onError {
                    _state.update { it.copy(isCreatingIssue = false) }
                }
                .sendSnackbarOnError()
        }
    }

    // ── Load Sections (API call) ─────────────────────────────────────

    private fun loadSections(boardId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.update { it.copy(isLoadingSections = true, errorMessage = null) }
            }
            sectionRepository.getSections(boardId)
                .onSuccess { sections ->
                    val sorted = sections.sortedBy { s -> s.position }
                    _state.update {
                        it.copy(
                            sections = sorted,
                            isLoadingSections = false,
                            isRefreshing = false
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoadingSections = false,
                            isRefreshing = false,
                            errorMessage = error.message
                        )
                    }
                }
                .sendSnackbarOnError()
        }
    }

    // ── Create Section (API call) ────────────────────────────────────

    private fun createSection() {
        val title = _state.value.newSectionTitle.trim()
        val boardId = _state.value.boardId
        val nextPosition = _state.value.sections.size + 1

        if (title.isBlank()) {
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar("Section title cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreatingSection = true) }
            sectionRepository.createSection(title = title, boardId = boardId, position = nextPosition)
                .onSuccess { newSection ->
                    _state.update { current ->
                        current.copy(
                            isCreatingSection = false,
                            showCreateSectionDialog = false,
                            newSectionTitle = "",
                            sections = (current.sections + newSection).sortedBy { it.position }
                        )
                    }
                    UiEventController.send(UiEvent.Snackbar("Section \"${newSection.title}\" created!"))
                }
                .onError {
                    _state.update { it.copy(isCreatingSection = false) }
                }
                .sendSnackbarOnError()
        }
    }

    private fun leaveBoard() {
        wsJob?.cancel()
        wsJob = null
        realtimeRepository.disconnectAsync()
        _state.update {
            it.copy(
                boardId = "",
                onlineCount = 0,
                activeUsers = emptyList(),
                showPresencePanel = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        leaveBoard()
    }
}