package com.laarasoft.frontend.features.kanban.presentation

sealed interface KanbanEvents {
    data object NavigateBack : KanbanEvents
    data object NavigateToSwitchBoard : KanbanEvents
}