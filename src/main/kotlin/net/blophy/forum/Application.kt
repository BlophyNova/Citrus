package net.blophy.forum

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.blophy.forum.config.Settings
import net.blophy.forum.plugins.configureDatabases
import net.blophy.forum.plugins.configureHTTP
import net.blophy.forum.plugins.configureRouting
import net.blophy.forum.plugins.configureSecurity

fun main() {
    embeddedServer(Netty, port = Settings.port, host = Settings.host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureDatabases()
    configureRouting()
}
