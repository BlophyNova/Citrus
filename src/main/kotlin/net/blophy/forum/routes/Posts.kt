package net.blophy.forum.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.blophy.forum.models.Comment
import net.blophy.forum.serializables.SearchFilter
import net.blophy.forum.serializables.getFilteredPosts
import net.blophy.forum.services.PostsService
import java.io.File

fun Route.postsRoutes() {
    route("/posts") {
        get {  // 获取帖子列表
            PostsService.get()
        }
        get("/search") { // 搜索/条件过滤
            return@get call.respond(call.receive<SearchFilter>().getFilteredPosts())
        }
        route("/{id}") { // 获取特定帖子
            get {
                PostsService.get(
                    call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                )
            }
            get("/background") { // 获取帖子背景(如果有)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val file = File("citrus/posts/$id/background.png")
                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            route("/comment") { // 顶层评论
                put {
                    try {
                        PostsService.comment(
                            call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest),
                            call.receive<Comment>()
                        )
                    } catch (_: NoSuchElementException) {
                        return@put call.respond(HttpStatusCode.NotFound)
                    }
                }
                put("/{comment_id}") { // 嵌套评论
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
                }
                delete("/{id}") { // 删除指定评论(顶层/嵌套可用)
                    PostsService.deleteComment(
                        call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                            HttpStatusCode.BadRequest
                        ),
                        call.parameters["comment_id"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    )
                }
            }
        }
    }
}
