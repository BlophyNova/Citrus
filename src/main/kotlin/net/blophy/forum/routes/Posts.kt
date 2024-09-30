package net.blophy.forum.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.postsRoutes() {
    route("/posts") {
        get {} // 获取帖子列表
        route("/{id}") {
            get {} // 获取特定帖子
            route("/comment") {
                put {} // 顶层评论
                put("/{id}") {} // 嵌套评论
                delete("/{id}") {} // 删除指定评论(顶层/嵌套可用)
            }
        }
    }
}
