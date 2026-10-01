package com.laarasoft.frontend.features.kanban.domain.models

data class Issue(
    val id: String,
    val title: String,
    val description: String,
    val sectionId: String,
    val createdBy: String,
    val position: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
