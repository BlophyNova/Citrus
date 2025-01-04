package net.blophy.forum.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.blophy.forum.config.Settings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection.TRANSACTION_SERIALIZABLE

fun configureDatabases() {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "${Settings.jdbcHead}${Settings.dbAddr}/${Settings.dbName}"
        username = Settings.dbUsername
        password = Settings.dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    })

    val db = Database.connect(dataSource)
    TransactionManager.manager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE
    TransactionManager.defaultDatabase = db
}
