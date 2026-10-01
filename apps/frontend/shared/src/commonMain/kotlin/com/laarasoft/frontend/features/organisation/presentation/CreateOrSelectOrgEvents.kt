package com.laarasoft.frontend.features.organisation.presentation

import com.laarasoft.frontend.config.navigation.UserScreenDestination
import com.laarasoft.frontend.features.organisation.domain.model.Organisation

sealed interface CreateOrSelectOrgEvents {
    data class NavigateToHome(val destination: UserScreenDestination.HomeScreen) : CreateOrSelectOrgEvents
    data class OrgCreated(val organisation: Organisation) : CreateOrSelectOrgEvents
}