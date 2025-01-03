package net.blophy.forum.services

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import net.blophy.forum.enums.UserTags.NONE
import net.blophy.forum.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

enum class PostFilterDependsOn(val id: Int) {
    DATE(0),
    POPULAR(1);

    companion object {
        private val map = PostFilterDependsOn.entries.toTypedArray().associateBy(PostFilterDependsOn::id)
        fun fromId(typeId: Int) = map[typeId] ?: NONE
    }
}

@Serializable
data class PostFilter(
    val sortByDescending: Boolean = false,
    val userId: Int? = null,
    val dependsOn: PostFilterDependsOn = PostFilterDependsOn.POPULAR,
    val hasContent: String? = null
)

object PostsService {

    private val posts = Posts

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
            it[posts.id] = posts.select(posts.id).maxByOrNull { resultRow -> resultRow[posts.id] }?.get(posts.id) ?: 0
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

    suspend fun getFilteredPosts(filter: PostFilter) = dbQuery {
        val query = posts.selectAll()
            .applyFilters(filter)
            .applySorting(filter)
        query.toList().toPosts()
    }

    private fun Query.applyFilters(filter: PostFilter): Query = this
        .let { query ->
            filter.userId?.let { query.andWhere { posts.posterId eq it } } ?: query
        }
        .let { query ->
            filter.hasContent?.let { query.andWhere { posts.content like "%$it%" } } ?: query
        }

    private fun Query.applySorting(filter: PostFilter): Query = this
        .let { query ->
            filter.dependsOn.let {
                when (it) {
                    PostFilterDependsOn.DATE -> query.orderBy(posts.id to (if (filter.sortByDescending) SortOrder.DESC else SortOrder.ASC))
                    PostFilterDependsOn.POPULAR -> query.orderBy(posts.comments.count() to (if (filter.sortByDescending) SortOrder.DESC else SortOrder.ASC))
                }
            }
        }


    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}