package com.laarasoft.frontend.features.kanban.data.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Endpoints
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.safeCall
import com.laarasoft.frontend.features.kanban.data.dto.CreateIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.CreateSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.DeleteIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.DeleteSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.IssueDto
import com.laarasoft.frontend.features.kanban.data.dto.MoveIssueRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.MoveSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.SectionDto
import com.laarasoft.frontend.features.kanban.domain.api.SectionApi
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SectionApiImpl(
    private val httpClient: HttpClient
) : SectionApi {

    override suspend fun getSections(boardId: String): Result<List<SectionDto>, DataError.Remote> {
        return safeCall<List<SectionDto>> {
            httpClient.get(Endpoints.SECTIONS) {
                parameter("board_id", boardId)
            }
        }
    }

    override suspend fun createSection(request: CreateSectionRequestDto): Result<SectionDto, DataError.Remote> {
        return safeCall<SectionDto> {
            httpClient.post(Endpoints.SECTIONS) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun moveSection(request: MoveSectionRequestDto): Result<SectionDto, DataError.Remote> {
        return safeCall<SectionDto> {
            httpClient.patch(Endpoints.MOVE_SECTION) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun deleteSection(sectionId: String): Result<Unit, DataError.Remote> {
        return safeCall<Unit> {
            httpClient.delete(Endpoints.SECTIONS) {
                contentType(ContentType.Application.Json)
                setBody(DeleteSectionRequestDto(sectionId))
            }
        }
    }

    override suspend fun createIssue(request: CreateIssueRequestDto): Result<IssueDto, DataError.Remote> {
        return safeCall<IssueDto> {
            httpClient.post(Endpoints.ISSUES) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun moveIssue(request: MoveIssueRequestDto): Result<IssueDto, DataError.Remote> {
        return safeCall<IssueDto> {
            httpClient.patch(Endpoints.MOVE_ISSUE) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun deleteIssue(issueId: String): Result<Unit, DataError.Remote> {
        return safeCall<Unit> {
            httpClient.delete(Endpoints.ISSUES) {
                contentType(ContentType.Application.Json)
                setBody(DeleteIssueRequestDto(issueId))
            }
        }
    }
}
