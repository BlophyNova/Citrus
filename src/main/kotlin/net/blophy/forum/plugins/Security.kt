package net.blophy.forum.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtAudience = System.getenv("CITRUS_JWT_AUDIENCE")
    val jwtDomain = System.getenv("CITRUS_JWT_ISSUER")
    val jwtSecret = System.getenv("CITRUS_JWT_SECRET")
    val httpClient = HttpClient(CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }
    }

    authentication {
        jwt {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
        val redirects = mutableMapOf<String, String>()
        oauth("natayark") {
            urlProvider = { "https://localhost:9000/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "natayark",
                    authorizeUrl = System.getenv("NATAYARK_AUTHORIZE_URL"),
                    accessTokenUrl = System.getenv("NATAYARK_ACCESS_TOKEN_URL"),
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("NATAYARK_CLIENT_ID"),
                    clientSecret = System.getenv("NATAYARK_CLIENT_SECRET"),
                    defaultScopes = listOf("https://forum.blophy.net"),
                    extraAuthParameters = listOf("access_type" to "offline"),
                    onStateCreated = { call, state ->
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            client = httpClient
        }
    }
}
