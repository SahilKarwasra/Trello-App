package com.laarasoft.frontend.features.kanban.domain.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.kanban.domain.models.Section

interface SectionRepository {
    suspend fun getSections(boardId: String): Result<List<Section>, DataError.Remote>
    suspend fun createSection(title: String, boardId: String, position: Int): Result<Section, DataError.Remote>
}
