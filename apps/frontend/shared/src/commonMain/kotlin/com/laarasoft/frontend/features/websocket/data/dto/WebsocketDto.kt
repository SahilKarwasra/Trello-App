package com.laarasoft.frontend.features.websocket.data.dto

import com.laarasoft.frontend.features.websocket.domain.models.BoardEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
internal data class WsEnvelope(
    val type: String,
    @SerialName("board_id") val boardId: String? = null,
    @SerialName("actor_id") val actorId: String? = null,
    val payload: JsonElement? = null,
)

// Section payloads (matches Go SectionResponse)
@Serializable
internal data class SectionPayloadDto(
    val id: String,
    val title: String,
    @SerialName("board_id") val boardId: String,
    val position: Int,
)

// Issue payloads (matches Go IssueResponse)
@Serializable
internal data class IssuePayloadDto(
    val id: String,
    val title: String,
    val description: String = "",
    @SerialName("section_id") val sectionId: String,
    @SerialName("created_by") val createdBy: String = "",
    val position: Int,
)

// Delete payloads
@Serializable
internal data class SectionDeletedPayloadDto(
    @SerialName("section_id") val sectionId: String,
    @SerialName("board_id") val boardId: String = "",
)

@Serializable
internal data class IssueDeletedPayloadDto(
    @SerialName("issue_id") val issueId: String,
    @SerialName("section_id") val sectionId: String = "",
    @SerialName("board_id") val boardId: String = "",
)

// Presence payloads
@Serializable
internal data class UserPresencePayloadDto(
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("online_count") val onlineCount: Int,
)

@Serializable
internal data class RoomStatePayloadDto(
    @SerialName("online_count") val onlineCount: Int,
    @SerialName("active_users") val activeUsers: List<ActiveUserDto> = emptyList(),
)

@Serializable
internal data class ActiveUserDto(
    @SerialName("user_id") val userId: String,
    val username: String,
)

internal class BoardEventMapper(private val json: Json) {
    fun map(env: WsEnvelope): BoardEvent? {
        val boardId = env.boardId ?: return null
        val p = env.payload ?: return null
        return when (env.type) {
            "SECTION_CREATED" -> json.decodeFromJsonElement<SectionPayloadDto>(p).let {
                BoardEvent.SectionCreated(boardId, it.id, it.title, it.position)
            }
            "SECTION_UPDATED" -> json.decodeFromJsonElement<SectionPayloadDto>(p).let {
                BoardEvent.SectionUpdated(boardId, it.id, it.title, it.position)
            }
            "SECTION_MOVED" -> json.decodeFromJsonElement<SectionPayloadDto>(p).let {
                BoardEvent.SectionMoved(boardId, it.id, it.title, it.position)
            }
            "SECTION_DELETED" -> json.decodeFromJsonElement<SectionDeletedPayloadDto>(p).let {
                BoardEvent.SectionDeleted(boardId, it.sectionId)
            }
            "ISSUE_CREATED" -> json.decodeFromJsonElement<IssuePayloadDto>(p).let {
                BoardEvent.IssueCreated(boardId, it.id, it.title, it.description, it.sectionId, it.createdBy, it.position)
            }
            "ISSUE_MOVED" -> json.decodeFromJsonElement<IssuePayloadDto>(p).let {
                BoardEvent.IssueMoved(boardId, it.id, it.title, it.description, it.sectionId, it.createdBy, it.position)
            }
            "ISSUE_DELETED" -> json.decodeFromJsonElement<IssueDeletedPayloadDto>(p).let {
                BoardEvent.IssueDeleted(boardId, it.issueId, it.sectionId)
            }
            "USER_JOINED" -> json.decodeFromJsonElement<UserPresencePayloadDto>(p).let {
                BoardEvent.UserJoined(boardId, it.userId, it.username, it.onlineCount)
            }
            "USER_LEFT" -> json.decodeFromJsonElement<UserPresencePayloadDto>(p).let {
                BoardEvent.UserLeft(boardId, it.userId, it.username, it.onlineCount)
            }
            "ROOM_STATE" -> json.decodeFromJsonElement<RoomStatePayloadDto>(p).let { dto ->
                BoardEvent.RoomState(
                    boardId = boardId,
                    onlineCount = dto.onlineCount,
                    activeUsers = dto.activeUsers.map {
                        BoardEvent.UserPresence(it.userId, it.username)
                    }
                )
            }
            else -> null
        }
    }
}