package com.laarasoft.frontend.features.board.domain.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.board.data.dto.BoardDto
import com.laarasoft.frontend.features.board.data.dto.CreateBoardRequestDto

interface BoardApi {
    suspend fun getBoards(organizationId: String): Result<List<BoardDto>, DataError.Remote>
    suspend fun createBoard(request: CreateBoardRequestDto): Result<BoardDto, DataError.Remote>
}
