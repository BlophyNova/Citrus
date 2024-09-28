package net.blophy.forum.plugins

import kotlinx.coroutines.Dispatchers
import net.blophy.forum.models.UserDetail
import net.blophy.forum.models.UserDetails
import net.blophy.forum.models.toUserDetail
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object UserService {

    val users = UserDetails

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(users)
        }
    }

    suspend fun read(id: Int): UserDetail? {
        return dbQuery {
            users.selectAll().where { this@UserService.users.id eq id }.singleOrNull()?.toUserDetail()
        }
    }

    suspend fun update(id: Int, user: UserDetail) {
        dbQuery {
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            users.deleteWhere { users.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

