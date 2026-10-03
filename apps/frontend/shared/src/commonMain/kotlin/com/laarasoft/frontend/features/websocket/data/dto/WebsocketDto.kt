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
    val payload: JsonElement? = null,
)

@Serializable
internal data class CardCreatedDto(
    @SerialName("list_id") val listId: String,
    @SerialName("card_id") val cardId: String,
    val title: String,
    val position: Double,
)

@Serializable
internal data class CardUpdatedDto(
    @SerialName("card_id") val cardId: String,
    val title: String? = null,
    val description: String? = null,
)

@Serializable
internal data class CardMovedDto(
    @SerialName("card_id") val cardId: String,
    @SerialName("from_list_id") val fromListId: String,
    @SerialName("to_list_id") val toListId: String,
    val position: Double,
)

@Serializable
internal data class CardDeletedDto(
    @SerialName("card_id") val cardId: String,
)

internal class BoardEventMapper(private val json: Json) {
    fun map(env: WsEnvelope): BoardEvent? {
        val boardId = env.boardId ?: return null
        val p = env.payload ?: return null
        return when (env.type) {
            "card.created" -> json.decodeFromJsonElement<CardCreatedDto>(p).let {
                BoardEvent.CardCreated(boardId, it.listId, it.cardId, it.title, it.position)
            }
            "card.updated" -> json.decodeFromJsonElement<CardUpdatedDto>(p).let {
                BoardEvent.CardUpdated(boardId, it.cardId, it.title, it.description)
            }
            "card.moved" -> json.decodeFromJsonElement<CardMovedDto>(p).let {
                BoardEvent.CardMoved(boardId, it.cardId, it.fromListId, it.toListId, it.position)
            }
            "card.deleted" -> json.decodeFromJsonElement<CardDeletedDto>(p).let {
                BoardEvent.CardDeleted(boardId, it.cardId)
            }
            else -> null
        }
    }
}

internal object WsOutbound {
    fun subscribe(json: Json, boardId: String) =
        json.encodeToString(WsEnvelope.serializer(), WsEnvelope(type = "subscribe", boardId = boardId))

    fun unsubscribe(json: Json, boardId: String) =
        json.encodeToString(WsEnvelope.serializer(), WsEnvelope(type = "unsubscribe", boardId = boardId))
}