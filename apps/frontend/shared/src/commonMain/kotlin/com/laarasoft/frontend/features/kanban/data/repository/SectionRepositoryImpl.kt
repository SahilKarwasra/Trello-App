package com.laarasoft.frontend.features.kanban.data.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.map
import com.laarasoft.frontend.features.kanban.data.dto.CreateSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.toDomain
import com.laarasoft.frontend.features.kanban.domain.api.SectionApi
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
}
