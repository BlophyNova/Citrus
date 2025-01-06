package net.blophy.forum.models

import kotlinx.serialization.Serializable
import net.blophy.forum.enums.Partitions
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

object Posts : IntIdTable("posts") {
    val posterId = integer("posterId")
    val content = text("content")
    val partitions = array<Int>("partitions")
    val comments = array<Int>("comments")
}

object Comments : IntIdTable("comments") {
    val postId = integer("post_id").references(Posts.id, onDelete = ReferenceOption.CASCADE)
    val parentCommentId = integer("parent_comment_id").default(0) // 0表示没有父评论
    val content = text("content")
    val authorId = integer("author_id")
}

@Serializable
data class Post(
    val id: Int,
    val poster: Int,
    val content: String,
    val comments: List<Comment>,
    val partitions: List<Partitions>
)

@Serializable
data class Comment(
    val id: Int,
    val postId: Int,
    val parentCommentId: Int,
    val content: String,
    val authorId: Int,
    val childCommentIds: List<Int> = emptyList()
)

fun ResultRow.toPost(): Post {
    val postId = this[Posts.id].value
    val comments = Comments.selectAll().where { Comments.postId eq postId }
        .map { it.toComment() }
    return Post(
        id = postId,
        poster = this[Posts.posterId],
        content = this[Posts.content],
        comments = comments,
        partitions = this[Posts.partitions].map { Partitions.fromId(it) }
    )
}

fun ResultRow.toComment(): Comment {
    val id = this[Comments.id].value
    val postId = this[Comments.postId]
    val parentId = this[Comments.parentCommentId]
    val content = this[Comments.content]
    val authorId = this[Comments.authorId]

    val childCommentIds = Comments.selectAll().where { Comments.parentCommentId eq id }
        .map { it[Comments.id].value }

    return Comment(
        id = id,
        postId = postId,
        parentCommentId = parentId,
        content = content,
        authorId = authorId,
        childCommentIds = childCommentIds
    )
}

fun List<ResultRow>.toPosts() = map { it.toPost() }
