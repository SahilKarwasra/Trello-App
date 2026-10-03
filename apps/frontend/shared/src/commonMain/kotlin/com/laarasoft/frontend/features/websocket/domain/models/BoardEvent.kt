package com.laarasoft.frontend.features.websocket.domain.models

sealed interface BoardEvent {
    val boardId: String

    // Section events
    data class SectionCreated(
        override val boardId: String,
        val sectionId: String,
        val title: String,
        val position: Int,
    ) : BoardEvent

    data class SectionUpdated(
        override val boardId: String,
        val sectionId: String,
        val title: String,
        val position: Int,
    ) : BoardEvent

    data class SectionMoved(
        override val boardId: String,
        val sectionId: String,
        val title: String,
        val position: Int,
    ) : BoardEvent

    data class SectionDeleted(
        override val boardId: String,
        val sectionId: String,
    ) : BoardEvent

    // Issue events
    data class IssueCreated(
        override val boardId: String,
        val issueId: String,
        val title: String,
        val description: String,
        val sectionId: String,
        val createdBy: String,
        val position: Int,
    ) : BoardEvent

    data class IssueMoved(
        override val boardId: String,
        val issueId: String,
        val title: String,
        val description: String,
        val sectionId: String,
        val createdBy: String,
        val position: Int,
    ) : BoardEvent

    data class IssueDeleted(
        override val boardId: String,
        val issueId: String,
        val sectionId: String,
    ) : BoardEvent

    // Presence events
    data class UserJoined(
        override val boardId: String,
        val userId: String,
        val username: String,
        val onlineCount: Int,
    ) : BoardEvent

    data class UserLeft(
        override val boardId: String,
        val userId: String,
        val username: String,
        val onlineCount: Int,
    ) : BoardEvent

    data class RoomState(
        override val boardId: String,
        val onlineCount: Int,
        val activeUsers: List<UserPresence>,
    ) : BoardEvent

    data class UserPresence(
        val userId: String,
        val username: String,
    )

    data class ResyncRequired(override val boardId: String) : BoardEvent
}
