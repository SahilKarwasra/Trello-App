package com.laarasoft.frontend.features.organisation.presentation

import com.laarasoft.frontend.config.navigation.MainGraph
import com.laarasoft.frontend.features.organisation.domain.model.Organisation

sealed interface CreateOrSelectOrgEvents {
    data class NavigateToHome(val destination: MainGraph = MainGraph.HomeGraph) : CreateOrSelectOrgEvents
    data class OrgCreated(val organisation: Organisation) : CreateOrSelectOrgEvents
}