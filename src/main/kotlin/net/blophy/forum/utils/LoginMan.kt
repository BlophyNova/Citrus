package net.blophy.forum.utils

import net.blophy.forum.config.Settings
import net.blophy.forum.models.Cache


object LoginMan {
    suspend fun isLoggedIn(user: Int): Boolean = Cache.useWrapper { it.exists("login:${user}") }

    suspend fun login(user: Int) =
        Cache.useWrapper { it.set("login:${user}", "1", Settings.tokenExpireAt.toULong()) }

    suspend fun logout(user: Int) = Cache.useWrapper { it.del("login:${user}") }
}
