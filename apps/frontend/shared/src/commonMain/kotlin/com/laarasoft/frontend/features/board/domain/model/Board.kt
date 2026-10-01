package com.laarasoft.frontend.features.board.domain.model

data class Board(
    val id: String,
    val title: String,
    val organisationId: String,
    val createdBy: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
