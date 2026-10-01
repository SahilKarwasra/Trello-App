package com.laarasoft.frontend.features.kanban.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.config.network.onError
import com.laarasoft.frontend.config.network.onSuccess
import com.laarasoft.frontend.config.network.sendSnackbarOnError
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.kanban.domain.repository.SectionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KanbanViewModel(
    private val sectionRepository: SectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KanbanState())
    val state = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = KanbanState()
    )

    private val _events = Channel<KanbanEvents>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: KanbanAction) {
        when (action) {
            is KanbanAction.Init -> {
                if (_state.value.boardId != action.boardId || _state.value.sections.isEmpty()) {
                    _state.update {
                        it.copy(
                            boardId = action.boardId,
                            boardTitle = action.boardTitle
                        )
                    }
                    loadSections(action.boardId)
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
                viewModelScope.launch {
                    _events.send(KanbanEvents.NavigateBack)
                }
            }

            is KanbanAction.OnSwitchBoardClick -> {
                _state.update { it.copy(showBoardSwitcher = true) }
            }

            is KanbanAction.OnDismissBoardSwitcher -> {
                _state.update { it.copy(showBoardSwitcher = false) }
            }
        }
    }

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
        }
    }

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
        val newIssue = com.laarasoft.frontend.features.kanban.domain.models.Issue(
            id = "local-issue-${kotlin.random.Random.nextInt(10000, 99999)}",
            title = title,
            description = description,
            sectionId = sectionId,
            createdBy = "You",
            position = targetSection.issues.size + 1
        )

        val updatedSections = currentSections.map { sec ->
            if (sec.id == sectionId) {
                sec.copy(issues = sec.issues + newIssue)
            } else sec
        }

        _state.update {
            it.copy(
                sections = updatedSections,
                showCreateIssueDialog = false,
                newIssueTitle = "",
                newIssueDescription = "",
                createIssueSectionId = null
            )
        }
        viewModelScope.launch {
            UiEventController.send(UiEvent.Snackbar("Card \"$title\" added!"))
        }
    }

    private fun loadSections(boardId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.update { it.copy(isLoadingSections = true, errorMessage = null) }
            }
            sectionRepository.getSections(boardId)
                .onSuccess { sections ->
                    val sorted = sections.sortedBy { s -> s.position }
                    val withSampleIssues = seedDefaultIssuesIfEmpty(sorted)
                    _state.update {
                        it.copy(
                            sections = withSampleIssues,
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

    private fun seedDefaultIssuesIfEmpty(sections: List<com.laarasoft.frontend.features.kanban.domain.models.Section>): List<com.laarasoft.frontend.features.kanban.domain.models.Section> {
        val totalIssues = sections.sumOf { it.issues.size }
        if (totalIssues > 0 || sections.isEmpty()) return sections

        val sampleTemplates = listOf(
            listOf(
                "Design Neo-Brutalism system" to "Material theme tokens, high contrast borders & offset shadows",
                "Setup Android Network Policy" to "Allow cleartext communication for localhost / 10.0.2.2",
                "Setup Ktor Logging" to "Log all request and response bodies in Android logcat",
            ),
            listOf(
                "Drag & Drop Sections" to "Hold column header to drag and reorder sections horizontally",
                "Drag & Drop Cards" to "Drag cards across lists and drop at any position seamlessly",
            ),
            listOf(
                "Board switcher navigation" to "Quickly navigate between multiple project workspaces",
                "API Integration" to "Connect KMP shared repository to Go backend",
            )
        )

        return sections.mapIndexed { secIdx, section ->
            val templates = sampleTemplates.getOrNull(secIdx % sampleTemplates.size) ?: emptyList()
            val seededIssues = templates.mapIndexed { issIdx, (title, desc) ->
                com.laarasoft.frontend.features.kanban.domain.models.Issue(
                    id = "issue-${section.id}-$issIdx",
                    title = title,
                    description = desc,
                    sectionId = section.id,
                    createdBy = "Admin",
                    position = issIdx + 1
                )
            }
            section.copy(issues = seededIssues)
        }
    }

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
}