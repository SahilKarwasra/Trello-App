package com.laarasoft.frontend.features.websocket.domain.models

sealed interface BoardEvent {
    val boardId: String

    data class CardCreated(
        override val boardId: String,
        val listId: String,
        val cardId: String,
        val title: String,
        val position: Double,
    ) : BoardEvent

    data class CardUpdated(
        override val boardId: String,
        val cardId: String,
        val title: String?,
        val description: String?,
    ) : BoardEvent

    data class CardMoved(
        override val boardId: String,
        val cardId: String,
        val fromListId: String,
        val toListId: String,
        val position: Double,
    ) : BoardEvent

    data class CardDeleted(
        override val boardId: String,
        val cardId: String,
    ) : BoardEvent

    data class ResyncRequired(override val boardId: String) : BoardEvent
}
