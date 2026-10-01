package com.laarasoft.frontend.features.organisation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.config.navigation.UserScreenDestination
import com.laarasoft.frontend.config.network.onError
import com.laarasoft.frontend.config.network.onSuccess
import com.laarasoft.frontend.config.network.sendSnackbarOnError
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.organisation.domain.repository.OrganisationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateOrSelectOrgViewModel(
    private val organisationRepository: OrganisationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateOrSelectOrgState())
    val state = _state
        .onStart { loadOrganisations() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CreateOrSelectOrgState()
        )

    private val _events = Channel<CreateOrSelectOrgEvents>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateOrSelectOrgAction) {
        when (action) {
            is CreateOrSelectOrgAction.OnShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true) }
            }

            is CreateOrSelectOrgAction.OnDismissCreateDialog -> {
                _state.update {
                    it.copy(
                        showCreateDialog = false,
                        newOrgTitle = "",
                        newOrgDescription = ""
                    )
                }
            }

            is CreateOrSelectOrgAction.OnNewOrgTitleChange -> {
                _state.update { it.copy(newOrgTitle = action.title) }
            }

            is CreateOrSelectOrgAction.OnNewOrgDescriptionChange -> {
                _state.update { it.copy(newOrgDescription = action.description) }
            }

            is CreateOrSelectOrgAction.OnCreateOrg -> {
                createOrganisation()
            }

            is CreateOrSelectOrgAction.OnSelectOrg -> {
                viewModelScope.launch {
                    _events.send(
                        CreateOrSelectOrgEvents.NavigateToHome(
                            UserScreenDestination.HomeScreen(
                                orgId = action.organisation.id,
                                orgName = action.organisation.title
                            )
                        )
                    )
                }
            }
        }
    }

    private fun loadOrganisations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            organisationRepository.getOrganisations()
                .onSuccess { orgs ->
                    _state.update { it.copy(isLoading = false, organisations = orgs) }
                }
                .onError { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
                .sendSnackbarOnError()
        }
    }

    private fun createOrganisation() {
        val title = _state.value.newOrgTitle.trim()
        val description = _state.value.newOrgDescription.trim()

        if (title.isBlank()) {
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar("Organisation name cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true) }
            organisationRepository.createOrganisation(title, description)
                .onSuccess { org ->
                    _state.update { current ->
                        current.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            newOrgTitle = "",
                            newOrgDescription = "",
                            organisations = current.organisations + org
                        )
                    }
                    UiEventController.send(UiEvent.Snackbar("Organisation \"${org.title}\" created!"))
                    _events.send(CreateOrSelectOrgEvents.OrgCreated(org))
                }
                .onError { error ->
                    _state.update { it.copy(isCreating = false) }
                }
                .sendSnackbarOnError()
        }
    }
}