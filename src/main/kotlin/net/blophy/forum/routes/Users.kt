package net.blophy.forum.routes

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.blophy.forum.services.UserService
import java.io.File

fun Route.userRoutes() {
    route("/users") {
        post("/register") {

        }
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
        route("/{id}") {
            post("/check_contribution") {
                val username =
                    UserService.getUserContactById(call.parameters["id"]?.toIntOrNull())?.get("github")
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest
                        )
                val repos = listOf(
                    "BlophyNova/Blophy",
                    "BlophyNova/Citrus",
                    "BlophyNova/ZestFlow",
                    "BlophyNova/BlophyNovaEdit",
                    "BlophyNova/blophynova.github.io"
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
            get("/latest_posts") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                return@get call.respond(HttpStatusCode.OK, UserService.getLatestPosts(id))
            }
            get("/avatar") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val png = File("citrus/users/$id/avatar.png")
                val jpg = File("citrus/users/$id/avatar.jpg")
                if (png.exists()) {
                    call.respondFile(png)
                } else if (jpg.exists()) {
                    call.respondFile(jpg)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            get("/background") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val file = File("citrus/users/$id/background.png")
                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
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