package com.laarasoft.frontend.features.board.data.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.map
import com.laarasoft.frontend.features.board.data.dto.CreateBoardRequestDto
import com.laarasoft.frontend.features.board.data.dto.toDomain
import com.laarasoft.frontend.features.board.domain.api.BoardApi
import com.laarasoft.frontend.features.board.domain.model.Board
import com.laarasoft.frontend.features.board.domain.repository.BoardRepository

class BoardRepositoryImpl(
    private val boardApi: BoardApi
) : BoardRepository {

    override suspend fun getBoards(organizationId: String): Result<List<Board>, DataError.Remote> {
        return boardApi.getBoards(organizationId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun createBoard(
        title: String,
        organizationId: String
    ): Result<Board, DataError.Remote> {
        return boardApi.createBoard(
            CreateBoardRequestDto(title = title, organisationId = organizationId)
        ).map { it.toDomain() }
    }
}
