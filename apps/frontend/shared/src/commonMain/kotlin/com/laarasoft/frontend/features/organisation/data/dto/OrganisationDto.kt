package com.laarasoft.frontend.features.organisation.data.dto

import com.laarasoft.frontend.features.organisation.domain.model.Organisation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganisationDto(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateOrgRequestDto(
    val title: String,
    val description: String
)

fun OrganisationDto.toDomain(): Organisation = Organisation(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Serializable
data class InviteMemberRequestDto(
    val username: String,
    @SerialName("organization_id")
    val organisationId: String
)

@Serializable
data class InviteMemberResponseDto(
    val id: String? = null,
    val user: String? = null,
    @SerialName("organization_id")
    val organisationId: String? = null,
    val role: String? = null,
    val accepted: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
)
