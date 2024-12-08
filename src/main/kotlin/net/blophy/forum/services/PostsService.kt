package net.blophy.forum.services

import kotlinx.coroutines.Dispatchers
import net.blophy.forum.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object PostsService {

    val posts = Posts

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
            it[posts.id] = posts.select(posts.id).maxByOrNull { it[posts.id] }?.get(posts.id) ?: 0
            it[posts.content] = post.content
            it[posts.posterId] = post.poster
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        posts.deleteWhere { posts.id eq id }
    }

    suspend fun comment(postId: Int, content: Comment) = dbQuery {
        posts.update({ posts.id eq postId }) {
            it[posts.comments] =
                posts.selectAll().where { posts.id eq postId }.singleOrNull()?.toPost()?.comments?.plus(content)
                    ?: throw NoSuchElementException()
        }
    }

    suspend fun comment(postId: Int, commentId: Int, content: Comment) = dbQuery {}

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}