package net.blophy.forum.routes

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/users") {
        post("/login") {
            /*val param = call.receiveParameters()
            val user = param["email"]?.let { findUserByEmail(it) }
                ?: param["username"]?.let { findUserByUsername(it) }
                ?: return@post call.request(HttpStatusCode.NotFound)
            val password = param["password"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (user.group == UserGroup.Banned) return respond(HttpStatusCode.Forbidden)
            if (!checkPassword(user.id, password)) return respond(HttpStatusCode.Unauthorized)

            val token = generateToken(user.id, user.username)
            respond(hashMapOf("token" to token))
            // 将用户信息及token存储在redis中
            loginStatusCache.resource.use {
                it.set(user.id.toString(), token)
            }*/
        }
        post("/logout") {}
        post("/check_contribution") {
            val params = call.receive<Parameters>()
            val username = params["username"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val repos = listOf(
                "BlophyNova/Blophy",
                "BlophyNova/Citrus",
                "BlophyNova/ZestFlow",
                "BlophyNova/BlophyNovaEdit",
                "blophynova.github.io"
            )
            val httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
            }
            var contribution = 0
            for (repo in repos) {
                try {
                    val response =
                        httpClient.get("https://api.github.com/repos/$repo/commits?author=$username") {
                            headers {
                                append("Authorization", "token ${System.getenv("GITHUB_TOKEN")}")
                            }
                        }.toString()
                    if (response.isNotEmpty()) {
                        contribution += 1
                    }
                } catch (_: Exception) {
                    continue // 如果请求失败，继续检查下一个仓库
                }
            }
            call.respond(contribution)
        }
        authenticate("natayark") {
            post("/oauth_callback") {
                val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                if (principal != null) {
                    val accessToken = principal.accessToken
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "OAuth authentication failed")
                }
            }
        }
        authenticate("github") {
            route("/oauth_callback") {
                get {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    if (principal != null) {
                        val accessToken = principal.accessToken
                        val userInfo = HttpClient().get("https://api.github.com/user") {
                            headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
                        }.toString()
                        call.respond("Authenticated! User info: $userInfo")
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "OAuth authentication failed")
                    }
                }
            }
        }
    }
}