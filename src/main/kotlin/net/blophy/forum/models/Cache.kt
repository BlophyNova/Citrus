package net.blophy.forum.models

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import net.blophy.forum.config.Settings
import kotlin.use

object Cache {
    suspend fun <R> use(body: suspend (KredsClient) -> R): R {
        return newClient(Endpoint.from(Settings.redis)).use { client ->
            body(client)
        }
    }
}