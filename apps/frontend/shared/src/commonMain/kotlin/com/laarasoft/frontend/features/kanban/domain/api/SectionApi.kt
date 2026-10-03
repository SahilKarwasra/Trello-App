package com.laarasoft.frontend.features.kanban.domain.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.kanban.data.dto.CreateIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.CreateSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.IssueDto
import com.laarasoft.frontend.features.kanban.data.dto.MoveIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.MoveSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.SectionDto

interface SectionApi {
    suspend fun getSections(boardId: String): Result<List<SectionDto>, DataError.Remote>
    suspend fun createSection(request: CreateSectionRequestDto): Result<SectionDto, DataError.Remote>
    suspend fun moveSection(request: MoveSectionRequestDto): Result<SectionDto, DataError.Remote>
    suspend fun deleteSection(sectionId: String): Result<Unit, DataError.Remote>
    suspend fun createIssue(request: CreateIssueRequestDto): Result<IssueDto, DataError.Remote>
    suspend fun moveIssue(request: MoveIssueRequestDto): Result<IssueDto, DataError.Remote>
    suspend fun deleteIssue(issueId: String): Result<Unit, DataError.Remote>
}
