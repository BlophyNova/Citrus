package net.blophy.forum.routes

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/users") {
        post("/login") {}
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
            post("/oauth_callback") {} // oauth回调路由
        }
    }
}
