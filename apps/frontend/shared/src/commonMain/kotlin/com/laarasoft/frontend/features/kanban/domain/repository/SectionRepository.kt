package com.laarasoft.frontend.features.kanban.domain.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.kanban.domain.models.Issue
import com.laarasoft.frontend.features.kanban.domain.models.Section

interface SectionRepository {
    suspend fun getSections(boardId: String): Result<List<Section>, DataError.Remote>
    suspend fun createSection(title: String, boardId: String, position: Int): Result<Section, DataError.Remote>
    suspend fun moveSection(sectionId: String, newPosition: Int): Result<Section, DataError.Remote>
    suspend fun deleteSection(sectionId: String): Result<Unit, DataError.Remote>
    suspend fun createIssue(title: String, description: String, sectionId: String, position: Int): Result<Issue, DataError.Remote>
    suspend fun moveIssue(issueId: String, newSectionId: String, newPosition: Int): Result<Issue, DataError.Remote>
    suspend fun deleteIssue(issueId: String): Result<Unit, DataError.Remote>
}
