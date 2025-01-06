package net.blophy.forum.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import net.blophy.forum.config.Settings

val httpClient = HttpClient(CIO) {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }
}

@Serializable
data class UserSession(val name: String, val userId: Int) {
    fun isValid(): Boolean {
        return true
    }
}

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = Settings.tokenExpireAt
        }
    }
    authentication {
        session<UserSession> {
            validate { session ->
                if (session.isValid()) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
        oauth("github") {
            client = HttpClient() // 可选择配置 HTTP client
            urlProvider = { "http://localhost:9000/users/github/oauth_callback" } // 回调地址（你需要根据你的实际地址修改）
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    clientId = System.getenv("GITHUB_CLIENT_ID"),
                    clientSecret = System.getenv("GITHUB_CLIENT_SECRET"),
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    requestMethod = HttpMethod.Post,
                    defaultScopes = listOf("read:user", "user:email")
                )
            }
        }
        oauth("natayark") {
            client = httpClient
            urlProvider = { "https://localhost:9000/users/natayark/oauth_callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "natayark",
                    authorizeUrl = System.getenv("NATAYARK_AUTHORIZE_URL"),
                    accessTokenUrl = System.getenv("NATAYARK_ACCESS_TOKEN_URL"),
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("NATAYARK_CLIENT_ID"),
                    clientSecret = System.getenv("NATAYARK_CLIENT_SECRET"),
                    authorizeUrlInterceptor = {
                        this.parameters.append("redirect_uri", "https://forum.blophy.net/natayark/oauth_callback")
                        this.parameters.append("response_type", "code")
                        this.parameters.append("client_id", "citrus")
                    }
                )
            }
        }
    }
}
