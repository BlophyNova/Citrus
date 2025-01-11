package net.blophy.forum.models

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import net.blophy.forum.config.Settings

class KredsResourceWrapper(private val origin: KredsClient) {
    suspend fun set(key: String, value: String, expireSeconds: ULong? = null) {
        origin.set(key, value)
        if (expireSeconds != null)
            origin.expire(key, expireSeconds)
    }

    suspend fun exists(key: String): Boolean = origin.exists(key) == 1.toLong()
    suspend fun exists(vararg keys: String): Long = origin.exists(*keys)
    suspend fun get(key: String): String? = origin.get(key)
    suspend fun del(key: String) = origin.del(key)
}

object Cache {
    @Deprecated(
        message = "Use KredsResourceWrapper",
        replaceWith = ReplaceWith("Cache.useWrapper(body)")
    )
    suspend fun <R> use(body: suspend (KredsClient) -> R): R {
        return newClient(Endpoint.from(Settings.redis)).use { client ->
            body(client)
        }
    }

    suspend fun <R> useWrapper(body: suspend (KredsResourceWrapper) -> R): R {
        return newClient(Endpoint.from(Settings.redis)).use { client ->
            body(KredsResourceWrapper(client))
        }
    }
}