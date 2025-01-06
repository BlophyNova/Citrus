package net.blophy.forum.services

import kotlinx.coroutines.Dispatchers
import net.blophy.forum.models.*
import net.blophy.forum.serializables.PostFilter
import net.blophy.forum.serializables.PostFilterDependsOn
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.contains
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object PostsService {

    private val posts = Posts
    private val comments = Comments

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(posts)
        }
    }

    suspend fun get() = dbQuery {
        posts.selectAll().toList().toPosts()
    }

    suspend fun get(id: Int): Post? = dbQuery {
        posts.selectAll().where { Posts.id eq id }.singleOrNull()?.toPost()
    }

    suspend fun post(post: Post) = dbQuery {
        posts.insert {
            it[posts.content] = post.content
            it[posts.posterId] = post.poster
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        posts.deleteWhere { posts.id eq id }
    }

    suspend fun comment(post: Int, comment: Comment) = dbQuery {
        if (posts.selectAll().where { posts.id eq post }.singleOrNull() == null) {
            throw PostNotFoundException(post)
        }
        val id = comments.insertAndGetId {
            it[authorId] = comment.authorId
            it[postId] = post
            it[parentCommentId] = 0
            it[content] = comment.content
        }
        posts.upsert {
            it[comments] = it[comments].plus(id.value)
        }
    }

    suspend fun comment(post: Int, commentId: Int, comment: Comment) = dbQuery {
        if (posts.selectAll().where { posts.id eq post }.singleOrNull() == null) {
            throw PostNotFoundException(post)
        }
        if (comments.selectAll().where { comments.id eq commentId }.singleOrNull() == null) {
            throw CommentNotFoundException(post)
        }
        val id = comments.insertAndGetId {
            it[authorId] = comment.authorId
            it[postId] = post
            it[parentCommentId] = commentId
            it[content] = comment.content
        }
        posts.upsert {
            it[comments] = it[comments].plus(id.value)
        }
    }

    suspend fun deleteComment(post: Int, id: Int) = dbQuery {
        comments.deleteWhere { comments.id eq id }
        val postRecord = posts.selectAll().where { posts.id eq post }.singleOrNull()
        if (postRecord != null) {
            val updatedComments = postRecord[posts.comments].filter { it != id }
            posts.update({ posts.id eq post }) {
                it[posts.comments] = updatedComments
            }
        }
    }


    suspend fun getFilteredPosts(filter: PostFilter) = dbQuery {
        val query = posts.selectAll()
            .applyFilters(filter)
            .applySorting(filter)
        query.toList().toPosts()
    }

    private fun Query.applyFilters(filter: PostFilter): Query {
        return this.apply {
            filter.userId?.let { andWhere { posts.posterId eq it } }
            filter.hasContent?.let { andWhere { posts.content like "%$it%" } }
            filter.partitions?.let {
                andWhere { posts.partitions.contains({ it.map { i -> i.id } }) }
            }
        }
    }

    private fun Query.applySorting(filter: PostFilter): Query {
        val order = when (filter.dependsOn) {
            PostFilterDependsOn.DATE -> posts.id to if (filter.sortByDescending) SortOrder.DESC else SortOrder.ASC
            PostFilterDependsOn.POPULAR -> posts.comments.count() to if (filter.sortByDescending) SortOrder.DESC else SortOrder.ASC
        }
        return orderBy(order)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    class CommentNotFoundException(id: Int) : NoSuchElementException("Comment not found: $id")
    class PostNotFoundException(id: Int) : NoSuchElementException("Pos not found: $id")
}
