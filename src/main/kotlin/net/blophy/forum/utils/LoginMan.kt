package net.blophy.forum.utils

import net.blophy.forum.config.Settings
import net.blophy.forum.models.Cache


object LoginMan {
    suspend fun isLoggedIn(user: Int): Boolean = Cache.use { it.exists("login:${user}").toInt() == 1 }

    suspend fun login(user: Int) =
        Cache.use { it.set("login:${user}", "1"); it.expire("login:${user}", Settings.tokenExpireAt.toULong()) }

    suspend fun logout(user: Int) = Cache.use { it.del("login:${user}") }
}