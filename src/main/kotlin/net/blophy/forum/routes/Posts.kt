package net.blophy.forum.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.blophy.forum.models.Comment
import net.blophy.forum.services.PostsService

fun Route.postsRoutes() {
    route("/posts") {
        get {
            PostsService.get()
        } // 获取帖子列表
        route("/{id}") {
            get {
                PostsService.get(
                    call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                )
            } // 获取特定帖子
            route("/comment") {
                put {
                    try {
                        PostsService.comment(
                            call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest),
                            call.receive<Comment>()
                        )
                    } catch (_: NoSuchElementException) {
                        return@put call.respond(HttpStatusCode.NotFound)
                    }
                } // 顶层评论
                put("/{comment_id}") {
                    try {
                        PostsService.comment(
                            call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest),
                            call.parameters["comment_id"]?.toIntOrNull()
                                ?: return@put call.respond(HttpStatusCode.BadRequest),
                            call.receive<Comment>()
                        )
                    } catch (_: NoSuchElementException) {
                        return@put call.respond(HttpStatusCode.NotFound)
                    }
                } // 嵌套评论
                delete("/{id}") {} // 删除指定评论(顶层/嵌套可用)
            }
        }
    }
}
