package com.laarasoft.frontend.features.kanban.domain.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.kanban.data.dto.CreateSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.SectionDto

interface SectionApi {
    suspend fun getSections(boardId: String): Result<List<SectionDto>, DataError.Remote>
    suspend fun createSection(request: CreateSectionRequestDto): Result<SectionDto, DataError.Remote>
}
