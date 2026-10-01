package com.laarasoft.frontend.features.organisation.data.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.map
import com.laarasoft.frontend.features.organisation.data.dto.CreateOrgRequestDto
import com.laarasoft.frontend.features.organisation.data.dto.toDomain
import com.laarasoft.frontend.features.organisation.domain.api.OrganisationApi
import com.laarasoft.frontend.features.organisation.domain.model.Organisation
import com.laarasoft.frontend.features.organisation.domain.repository.OrganisationRepository

class OrganisationRepositoryImpl(
    private val organisationApi: OrganisationApi
) : OrganisationRepository {

    override suspend fun getOrganisations(): Result<List<Organisation>, DataError.Remote> {
        return organisationApi.getOrganisations().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun createOrganisation(
        title: String,
        description: String
    ): Result<Organisation, DataError.Remote> {
        return organisationApi.createOrganisation(
            CreateOrgRequestDto(title = title, description = description)
        ).map { it.toDomain() }
    }
}
