package com.laarasoft.frontend.features.kanban.domain.models

data class Section(
    val id: String,
    val title: String,
    val boardId: String,
    val position: Int,
    val issues: List<Issue> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)
