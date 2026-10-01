package com.laarasoft.frontend.features.organisation.presentation

import com.laarasoft.frontend.features.organisation.domain.model.Organisation

data class CreateOrSelectOrgState(
    val organisations: List<Organisation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Create org dialog
    val showCreateDialog: Boolean = false,
    val newOrgTitle: String = "",
    val newOrgDescription: String = "",
    val isCreating: Boolean = false,
)