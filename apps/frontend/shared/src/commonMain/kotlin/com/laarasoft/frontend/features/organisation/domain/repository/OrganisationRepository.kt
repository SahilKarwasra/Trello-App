package com.laarasoft.frontend.features.organisation.domain.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.organisation.domain.model.Organisation

interface OrganisationRepository {
    suspend fun getOrganisations(): Result<List<Organisation>, DataError.Remote>
    suspend fun createOrganisation(title: String, description: String): Result<Organisation, DataError.Remote>
    suspend fun inviteMember(orgId: String, username: String): Result<Unit, DataError.Remote>
}
