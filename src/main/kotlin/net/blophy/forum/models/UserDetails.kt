package net.blophy.forum.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

enum class Tags(val id: Int) {
    NONE(0),
    DEVELOPER(1),
    CODE_CONTRIBUTOR(2),
    CHART_ASSESSOR(3),
    EXPERIENCED_MAPPER(4),
    LOVED_PLAYER(5);

    companion object {
        private val map = Tags.entries.toTypedArray().associateBy(Tags::id)
        fun fromId(typeId: Int) = map[typeId] ?: NONE
    }
}

object UserDetails : Table("userdetail") {
    val id = integer("id").uniqueIndex()
    val username = text("username")
    val introduce = text("introduce")
    val contact = json<Map<String, String>>("contact", Json { prettyPrint = true })
    val tags = array<Int>("tags")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserDetail(
    val id: Int,
    val username: String,
    val introduce: String,
    val contact: Map<String, String>,
    val tags: List<Tags>
)

fun ResultRow.toUserDetail() {
    val lst: List<Tags> = listOf()
    this[UserDetails.tags].forEach { t -> lst.plus(Tags.fromId(t)) }
    UserDetail(
        id = this[UserDetails.id],
        username = this[UserDetails.username],
        introduce = this[UserDetails.introduce],
        contact = this[UserDetails.contact],
        tags = lst
    )
}
