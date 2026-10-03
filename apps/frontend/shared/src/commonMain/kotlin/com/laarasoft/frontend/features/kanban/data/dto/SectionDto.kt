package com.laarasoft.frontend.features.kanban.data.dto

import com.laarasoft.frontend.features.kanban.domain.models.Issue
import com.laarasoft.frontend.features.kanban.domain.models.Section
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueDto(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("section_id")
    val sectionId: String,
    @SerialName("created_by")
    val createdBy: String,
    val position: Int,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class SectionDto(
    val id: String,
    val title: String,
    @SerialName("board_id")
    val boardId: String,
    val position: Int,
    val issues: List<IssueDto> = emptyList(),
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class GetSectionsRequestDto(
    @SerialName("board_id")
    val boardId: String
)

@Serializable
data class CreateSectionRequestDto(
    val title: String,
    @SerialName("board_id")
    val boardId: String,
    val position: Int
)

@Serializable
data class UpdateSectionRequestDto(
    @SerialName("section_id")
    val sectionId: String,
    val title: String
)

@Serializable
data class MoveSectionRequestDto(
    @SerialName("section_id")
    val sectionId: String,
    @SerialName("new_position")
    val newPosition: Int
)

@Serializable
data class DeleteSectionRequestDto(
    @SerialName("section_id")
    val sectionId: String
)

@Serializable
data class CreateIssueRequestDto(
    val title: String,
    val description: String = "",
    @SerialName("section_id")
    val sectionId: String,
    val position: Int
)

@Serializable
data class MoveIssueRequestDto(
    @SerialName("issue_id")
    val issueId: String,
    @SerialName("new_position")
    val newPosition: Int,
    @SerialName("new_section_id")
    val newSectionId: String
)

@Serializable
data class DeleteIssueRequestDto(
    @SerialName("issue_id")
    val issueId: String
)

fun IssueDto.toDomain(): Issue = Issue(
    id = id,
    title = title,
    description = description,
    sectionId = sectionId,
    createdBy = createdBy,
    position = position,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SectionDto.toDomain(): Section = Section(
    id = id,
    title = title,
    boardId = boardId,
    position = position,
    issues = issues.map { it.toDomain() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

