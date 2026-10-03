package com.laarasoft.frontend.config.network

object Endpoints {
    private var customHost: String? = null

    var host: String
        get() = customHost ?: PLATFORM_HOST
        set(value) {
            customHost = value
        }

    val BASE_URL: String get() = "http://$host:8080/api/v1"
    val SIGN_IN: String get() = "$BASE_URL/auth/sign-in"
    val SIGN_UP: String get() = "$BASE_URL/auth/sign-up"
    val ORGANISATIONS: String get() = "$BASE_URL/organisation"
    val BOARDS: String get() = "$BASE_URL/board"
    val INVITE: String get() = "$BASE_URL/invite"
    val SECTIONS: String get() = "$BASE_URL/section"
    val ISSUES: String get() = "$BASE_URL/issue"
    val MOVE_ISSUE: String get() = "$BASE_URL/move-issue"
    val MOVE_SECTION: String get() = "$BASE_URL/move-section"
    val WS_URL: String get() = "ws://$host:8080/api/v1/ws"
}
