package net.blophy.forum.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Posts : Table("posts") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val posterId = integer("poster")
    val content = text("content")
    val comments = array<Comment>("comments").default(listOf())
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Post(val id: Int, val poster: Int, val content: String, val comments: List<Comment>)

@Serializable
data class Comment(val id: Int, val postId: Int?, val content: String, val authorId: Int)

fun ResultRow.toPost() = Post(this[Posts.id], this[Posts.posterId], this[Posts.content], this[Posts.comments])

fun List<ResultRow>.toPosts() = map { it.toPost() }