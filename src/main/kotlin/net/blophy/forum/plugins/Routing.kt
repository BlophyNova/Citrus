package net.blophy.forum.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.blophy.forum.routes.postsRoutes
import net.blophy.forum.routes.userRoutes

fun Application.configureRouting() {
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Citrus API"
            version = "latest"
            description = "Citrus the Blophy forum backend"
        }
        server {
            url = "http://localhost:9000"
            description = "Citrus"
        }
    }
    routing {
        userRoutes()
        postsRoutes()
    }
}
