package com.laarasoft.frontend.features.organisation.domain.model

data class Organisation(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
