package com.laarasoft.frontend.features.board.data.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Endpoints
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.safeCall
import com.laarasoft.frontend.features.board.data.dto.BoardDto
import com.laarasoft.frontend.features.board.data.dto.CreateBoardRequestDto
import com.laarasoft.frontend.features.board.domain.api.BoardApi
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class BoardApiImpl(
    private val httpClient: HttpClient
) : BoardApi {

    override suspend fun getBoards(organizationId: String): Result<List<BoardDto>, DataError.Remote> {
        return safeCall<List<BoardDto>> {
            httpClient.get(Endpoints.BOARDS) {
                parameter("organization_id", organizationId)
            }
        }
    }

    override suspend fun createBoard(request: CreateBoardRequestDto): Result<BoardDto, DataError.Remote> {
        return safeCall<BoardDto> {
            httpClient.post(Endpoints.BOARDS) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}
