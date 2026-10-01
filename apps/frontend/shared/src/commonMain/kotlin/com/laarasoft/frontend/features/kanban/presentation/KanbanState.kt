package com.laarasoft.frontend.features.kanban.presentation

import com.laarasoft.frontend.features.kanban.domain.models.Section

data class KanbanState(
    val boardId: String = "",
    val boardTitle: String = "",

    // Sections / columns
    val sections: List<Section> = emptyList(),
    val isLoadingSections: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Create Section dialog
    val showCreateSectionDialog: Boolean = false,
    val newSectionTitle: String = "",
    val isCreatingSection: Boolean = false,

    // Create Issue dialog
    val showCreateIssueDialog: Boolean = false,
    val createIssueSectionId: String? = null,
    val newIssueTitle: String = "",
    val newIssueDescription: String = "",
    val isCreatingIssue: Boolean = false,

    // Board switcher
    val showBoardSwitcher: Boolean = false,
)