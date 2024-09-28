package net.blophy.forum.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object UserDetails : Table("userdetail") {
    val id = integer("id").uniqueIndex()
    val username = text("username")
    val introduce = text("introduce")
    val contact = json<Map<String, String>>("contact", Json { prettyPrint = true })

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserDetail(val id: Int, val username: String, val introduce: String, val contact: Map<String, String>)

fun ResultRow.toUserDetail() = UserDetail(
    id = this[UserDetails.id],
    username = this[UserDetails.username],
    introduce = this[UserDetails.introduce],
    contact = this[UserDetails.contact]
)
