package net.blophy.forum.services

import kotlinx.coroutines.Dispatchers
import net.blophy.forum.models.*
import net.blophy.forum.models.toUserDetail
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object UserService {

    private val users = UserDetails

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(users)
        }
    }

    // 创建新用户
    suspend fun create(user: UserRegistrationInfo) = dbQuery {
        users.insert{
            it[username] = user.name
            it[introduce] = user.introduce
        }
    }

    // 根据ID读取用户
    suspend fun read(id: Int) = dbQuery {
        users.selectAll().where { users.id eq id }
            .singleOrNull()?.toUserDetail()
    }

    // 更新用户信息
    suspend fun update(id: Int, user: UserDetail) = dbQuery {
        users.update({ users.id eq id }) {
            it[username] = user.username
            it[contact] = user.contact
            it[introduce] = user.introduce
            it[tags] = user.tags.map { t -> t.id }
        }
    }

    // 删除用户
    suspend fun delete(id: Int) = dbQuery {
        users.deleteWhere { users.id eq id }
    }

    suspend fun getUsernameById(id: Int?) = getUserDetailFieldById(id) { it.username }

    suspend fun getUserContactById(id: Int?) = getUserDetailFieldById(id) { it.contact }

    // 获取用户最新的帖子
    suspend fun getLatestPosts(id: Int) = dbQuery {
        PostsService.getFilteredPosts(
            PostFilter(
                userId = id, sortByDescending = true,
                dependsOn = PostFilterDependsOn.DATE,
            )
        )
    }

    private suspend fun <R> getUserDetailFieldById(id: Int?, fieldSelector: (UserDetail) -> R): R? {
        return id?.let {
            users.selectAll().where { users.id eq id }
                .singleOrNull()?.toUserDetail()?.let(fieldSelector)
        }
    }

    // 通用数据库查询方法
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
