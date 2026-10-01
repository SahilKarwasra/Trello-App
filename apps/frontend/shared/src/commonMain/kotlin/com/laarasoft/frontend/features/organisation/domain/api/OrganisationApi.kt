package com.laarasoft.frontend.features.organisation.domain.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.organisation.data.dto.CreateOrgRequestDto
import com.laarasoft.frontend.features.organisation.data.dto.OrganisationDto

interface OrganisationApi {
    suspend fun getOrganisations(): Result<List<OrganisationDto>, DataError.Remote>
    suspend fun createOrganisation(request: CreateOrgRequestDto): Result<OrganisationDto, DataError.Remote>
}
