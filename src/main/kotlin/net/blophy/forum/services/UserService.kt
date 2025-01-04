package net.blophy.forum.services

import kotlinx.coroutines.Dispatchers
import net.blophy.forum.models.UserDetail
import net.blophy.forum.models.UserDetails
import net.blophy.forum.models.UserRegistrationInfo
import net.blophy.forum.models.toUserDetail
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UserService {

    private val users = UserDetails

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(users)
        }
    }

    suspend fun create(user: UserRegistrationInfo) = dbQuery {
        users.insert {
            it[users.id] = users.select(users.id).maxByOrNull { resultRow -> resultRow[users.id] }?.get(users.id) ?: 0
            it[users.username] = user.name
            it[users.introduce] = user.introduce
        }
    }

    suspend fun read(id: Int) = dbQuery {
        users.selectAll().where { users.id eq id }.singleOrNull().toUserDetail()
    }

    suspend fun update(id: Int, user: UserDetail) = dbQuery {
        users.update({ users.id eq id }) {
            it[users.username] = user.username
            it[users.contact] = user.contact
            it[users.introduce] = user.introduce
            it[users.tags] = user.tags.map { t -> t.id }
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        users.deleteWhere { users.id eq id }
    }

    suspend fun getUsernameById(id: Int?) = dbQuery {
        return@dbQuery if (id != null) users.selectAll().where { users.id eq id }.singleOrNull()
            ?.toUserDetail()?.username else null
    }

    suspend fun getUserContactById(id: Int?) = dbQuery {
        return@dbQuery if (id != null) users.selectAll().where { users.id eq id }.singleOrNull()
            ?.toUserDetail()?.contact else null
    }

    suspend fun getLatestPosts(id: Int) = dbQuery {
        return@dbQuery PostsService.getFilteredPosts(
            PostFilter(
                userId = id, sortByDescending = true,
                dependsOn = PostFilterDependsOn.DATE,
            )
        )
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}