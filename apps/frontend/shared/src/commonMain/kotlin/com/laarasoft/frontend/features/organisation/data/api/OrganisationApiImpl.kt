package com.laarasoft.frontend.features.organisation.data.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Endpoints
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.safeCall
import com.laarasoft.frontend.features.organisation.data.dto.CreateOrgRequestDto
import com.laarasoft.frontend.features.organisation.data.dto.OrganisationDto
import com.laarasoft.frontend.features.organisation.domain.api.OrganisationApi
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OrganisationApiImpl(
    private val httpClient: HttpClient
) : OrganisationApi {

    override suspend fun getOrganisations(): Result<List<OrganisationDto>, DataError.Remote> {
        return safeCall<List<OrganisationDto>> {
            httpClient.get(Endpoints.ORGANISATIONS)
        }
    }

    override suspend fun createOrganisation(request: CreateOrgRequestDto): Result<OrganisationDto, DataError.Remote> {
        return safeCall<OrganisationDto> {
            httpClient.post(Endpoints.ORGANISATIONS) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}
