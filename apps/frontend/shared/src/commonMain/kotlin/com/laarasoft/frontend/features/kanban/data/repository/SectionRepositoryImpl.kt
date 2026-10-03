package com.laarasoft.frontend.features.kanban.data.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.map
import com.laarasoft.frontend.features.kanban.data.dto.CreateIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.CreateSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.MoveIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.MoveSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.toDomain
import com.laarasoft.frontend.features.kanban.domain.api.SectionApi
import com.laarasoft.frontend.features.kanban.domain.models.Issue
import com.laarasoft.frontend.features.kanban.domain.models.Section
import com.laarasoft.frontend.features.kanban.domain.repository.SectionRepository

class SectionRepositoryImpl(
    private val sectionApi: SectionApi
) : SectionRepository {

    override suspend fun getSections(boardId: String): Result<List<Section>, DataError.Remote> {
        return sectionApi.getSections(boardId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun createSection(
        title: String,
        boardId: String,
        position: Int
    ): Result<Section, DataError.Remote> {
        return sectionApi.createSection(
            CreateSectionRequestDto(title = title, boardId = boardId, position = position)
        ).map { it.toDomain() }
    }

    override suspend fun moveSection(
        sectionId: String,
        newPosition: Int
    ): Result<Section, DataError.Remote> {
        return sectionApi.moveSection(
            MoveSectionRequestDto(sectionId = sectionId, newPosition = newPosition)
        ).map { it.toDomain() }
    }

    override suspend fun deleteSection(sectionId: String): Result<Unit, DataError.Remote> {
        return sectionApi.deleteSection(sectionId)
    }

    override suspend fun createIssue(
        title: String,
        description: String,
        sectionId: String,
        position: Int
    ): Result<Issue, DataError.Remote> {
        return sectionApi.createIssue(
            CreateIssueRequestDto(
                title = title,
                description = description,
                sectionId = sectionId,
                position = position
            )
        ).map { it.toDomain() }
    }

    override suspend fun moveIssue(
        issueId: String,
        newSectionId: String,
        newPosition: Int
    ): Result<Issue, DataError.Remote> {
        return sectionApi.moveIssue(
            MoveIssueRequestDto(
                issueId = issueId,
                newSectionId = newSectionId,
                newPosition = newPosition
            )
        ).map { it.toDomain() }
    }

    override suspend fun deleteIssue(issueId: String): Result<Unit, DataError.Remote> {
        return sectionApi.deleteIssue(issueId)
    }
}
