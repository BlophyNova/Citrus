package net.blophy.forum.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection.TRANSACTION_SERIALIZABLE

fun Application.configureDatabases() {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${System.getenv("dbaddr")}/${System.getenv("dbname")}"
        username = System.getenv("dbusername") ?: "root"
        password = System.getenv("dbpassword") ?: "Citrus!"
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    })

    val db = Database.connect(dataSource)
    TransactionManager.manager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE
    TransactionManager.defaultDatabase = db
}
