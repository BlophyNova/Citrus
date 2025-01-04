package net.blophy.forum.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.blophy.forum.enums.UserTags
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object UserDetails : Table("userdetail") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val username = text("username")
    val introduce = text("introduce")
    val contact = json<MutableMap<String, String>>(
        "contact",
        Json { prettyPrint = true }
    ).default(
        mutableMapOf(
            "QQ" to "",
            "WeChat" to "",
            "Discord" to "",
            "Telegram" to "",
            "Twitter" to "",
            "GitHub" to "",
            "Steam" to "",
            "Epic" to "",
            "Twitch" to "",
            "YouTube" to "",
            "Bilibili" to "",
            "Other" to ""
        )
    )
    val tags = array<Int>("tags")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserDetail(
    val id: Int,
    val username: String,
    val introduce: String,
    val contact: MutableMap<String, String>,
    val tags: List<UserTags>
)

fun ResultRow?.toUserDetail() = this?.let {
    val tags = mutableListOf<UserTags>()
    this[UserDetails.tags].forEach { t -> tags.add(UserTags.fromId(t)) }

    UserDetail(
        id = this[UserDetails.id],
        username = this[UserDetails.username],
        introduce = this[UserDetails.introduce],
        contact = this[UserDetails.contact],
        tags = tags
    )
}

data class UserRegistrationInfo(
    val name: String,
    val introduce: String,
    val contact: Map<String, String>,
)
