package com.laarasoft.frontend.features.organisation.presentation

import com.laarasoft.frontend.features.organisation.domain.model.Organisation

sealed interface CreateOrSelectOrgAction {
    data object OnShowCreateDialog : CreateOrSelectOrgAction
    data object OnDismissCreateDialog : CreateOrSelectOrgAction
    data class OnNewOrgTitleChange(val title: String) : CreateOrSelectOrgAction
    data class OnNewOrgDescriptionChange(val description: String) : CreateOrSelectOrgAction
    data object OnCreateOrg : CreateOrSelectOrgAction
    data class OnSelectOrg(val organisation: Organisation) : CreateOrSelectOrgAction
}