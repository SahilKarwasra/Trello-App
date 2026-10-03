package com.laarasoft.frontend.features.home.data.dto

import com.laarasoft.frontend.features.home.domain.model.Board
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardDto(
    val id: String,
    val title: String,
    @SerialName("organization_id")
    val organisationId: String,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateBoardRequestDto(
    val title: String,
    @SerialName("organization_id")
    val organisationId: String
)

fun BoardDto.toDomain(): Board = Board(
    id = id,
    title = title,
    organisationId = organisationId,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)
