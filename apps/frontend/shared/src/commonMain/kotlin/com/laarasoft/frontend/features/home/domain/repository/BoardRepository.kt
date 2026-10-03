package com.laarasoft.frontend.features.home.domain.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.home.domain.model.Board

interface BoardRepository {
    suspend fun getBoards(organizationId: String): Result<List<Board>, DataError.Remote>
    suspend fun createBoard(title: String, organizationId: String): Result<Board, DataError.Remote>
}
