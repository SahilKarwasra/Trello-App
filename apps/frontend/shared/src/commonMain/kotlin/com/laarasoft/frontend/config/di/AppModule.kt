package com.laarasoft.frontend.config.di

import com.laarasoft.frontend.core.utils.TokenProvider
import com.laarasoft.frontend.config.datastore.DataStoreRepository
import com.laarasoft.frontend.features.auth.domain.api.AuthApi
import com.laarasoft.frontend.features.auth.data.api.AuthApiImpl
import com.laarasoft.frontend.features.auth.data.repository.AuthRepositoryImpl
import com.laarasoft.frontend.features.auth.domain.repository.AuthRepository
import com.laarasoft.frontend.features.auth.presentation.login.LoginViewModel
import com.laarasoft.frontend.features.auth.presentation.signup.SignupViewModel
import com.laarasoft.frontend.features.organisation.presentation.CreateOrSelectOrgViewModel
import com.laarasoft.frontend.features.home.presentation.HomeViewModel
import com.laarasoft.frontend.features.organisation.data.api.OrganisationApiImpl
import com.laarasoft.frontend.features.organisation.data.repository.OrganisationRepositoryImpl
import com.laarasoft.frontend.features.organisation.domain.api.OrganisationApi
import com.laarasoft.frontend.features.organisation.domain.repository.OrganisationRepository
import com.laarasoft.frontend.features.board.data.api.BoardApiImpl
import com.laarasoft.frontend.features.board.data.repository.BoardRepositoryImpl
import com.laarasoft.frontend.features.board.domain.api.BoardApi
import com.laarasoft.frontend.features.board.domain.repository.BoardRepository
import com.laarasoft.frontend.features.splash.presentation.SplashViewModel
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val sharedModules = module {
    includes(platformModule)

    singleOf(::DataStoreRepository)
    singleOf(::TokenProvider)

    single<HttpClient> {
        val tokenProvider: TokenProvider = get()
        val engine = getOrNull<HttpClientEngine>()
        val config: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(WebSockets)
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenProvider.getAccessToken()?.let { BearerTokens(it, "") }
                    }
                    sendWithoutRequest { true }
                }
            }
        }
        if (engine != null) HttpClient(engine, config) else HttpClient(config)
    }

    singleOf(::AuthApiImpl).bind<AuthApi>()
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()

    singleOf(::OrganisationApiImpl).bind<OrganisationApi>()
    singleOf(::OrganisationRepositoryImpl).bind<OrganisationRepository>()

    singleOf(::BoardApiImpl).bind<BoardApi>()
    singleOf(::BoardRepositoryImpl).bind<BoardRepository>()

    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::CreateOrSelectOrgViewModel)
    viewModelOf(::HomeViewModel)



}
