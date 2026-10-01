package com.laarasoft.frontend.features.kanban.presentation

sealed interface KanbanAction {
    data class Init(val boardId: String, val boardTitle: String) : KanbanAction
    data object OnRefresh : KanbanAction

    // Section creation
    data object OnShowCreateSectionDialog : KanbanAction
    data object OnDismissCreateSectionDialog : KanbanAction
    data class OnNewSectionTitleChange(val title: String) : KanbanAction
    data object OnCreateSection : KanbanAction

    // Issue creation
    data class OnShowCreateIssueDialog(val sectionId: String) : KanbanAction
    data object OnDismissCreateIssueDialog : KanbanAction
    data class OnNewIssueTitleChange(val title: String) : KanbanAction
    data class OnNewIssueDescriptionChange(val description: String) : KanbanAction
    data object OnCreateIssue : KanbanAction

    // Drag and Drop
    data class ReorderSections(val fromIndex: Int, val toIndex: Int) : KanbanAction
    data class MoveIssue(
        val fromSectionId: String,
        val toSectionId: String,
        val issueId: String,
        val targetIndex: Int
    ) : KanbanAction

    // Navigation
    data object OnBackClick : KanbanAction
    data object OnSwitchBoardClick : KanbanAction
    data object OnDismissBoardSwitcher : KanbanAction
}