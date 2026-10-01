package com.laarasoft.frontend.features.kanban.data.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Endpoints
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.safeCall
import com.laarasoft.frontend.features.kanban.data.dto.CreateSectionRequestDto
import com.laarasoft.frontend.features.kanban.data.dto.SectionDto
import com.laarasoft.frontend.features.kanban.domain.api.SectionApi
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
}
