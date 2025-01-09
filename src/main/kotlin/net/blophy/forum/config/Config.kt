@file:Suppress("SpellCheckingInspection")

package net.blophy.forum.config

import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Config(
    val host: String? = null,
    val port: Int? = null,
    val tokenExpireAt: Long? = null,
    val dbAddr: String? = null,
    val dbName: String? = null,
    val dbUsername: String? = null,
    val smtpHost: String? = null,
    val smtpPort: Int? = null,
    val smtpUsername: String? = null,
    val redisUrl: String? = null,
    val jdbcHead: String? = null,
)

object Settings {

    private var c = Config()

    init {
        val config = File("config.yml")
        try {
            c = Yaml.default.decodeFromString(Config.serializer(), if (config.exists()) config.readText() else "")
        } catch (_: EmptyYamlDocumentException) {
            println("Config file not found.")
        }
    }

    // Bind
    val host = System.getenv("HOST") ?: c.host ?: "localhost"
    val port = System.getenv("PORT")?.toIntOrNull() ?: c.port ?: 9000

    // Token
    val tokenExpireAt =
        System.getenv("TOKEN_EXPIRE_AT")?.toLongOrNull() ?: c.tokenExpireAt ?: 6.048e10.toLong() // 6.048*10⁸ms, 即7天

    val dbAddr = System.getenv("MAINDB_URL") ?: c.dbAddr ?: "localhost:5432"
    val dbName = System.getenv("DB_NAME") ?: c.dbName ?: "citrus"
    val dbUsername = System.getenv("MAINDB_USERNAME") ?: c.dbUsername ?: "surtic"
    val dbPassword = System.getenv("MAINDB_PASSWORD") ?: ""

    val redis = System.getenv("REDIS_URL") ?: c.redisUrl ?: "localhost:6379"

    val jdbcHead = System.getenv("JDBC_HEAD") ?: c.jdbcHead ?: "jdbc:postgresql://"

    // Email SMTP
    val smtpHost: String? = System.getenv("SMTP_HOST") ?: c.smtpHost
    val smtpPort = System.getenv("SMTP_PORT")?.toIntOrNull() ?: c.smtpPort ?: 465
    val smtpUsername = System.getenv("SMTP_USERNAME") ?: c.smtpUsername ?: "noreply@blophy.net"
    val smtpPassword = System.getenv("SMTP_PASSWORD") ?: ""

    // EmailServiceList
    val commonEmailService = setOf(
        // 腾讯
        "qq.com",
        "vip.qq.com",
        "foxmail.com",
        // 网易
        "163.com",
        "126.com",
        "yeah.net",
        "vip.163.com",
        "vip.126.com",
        "188.com",
        // 阿里
        "aliyun.com",
        // 新浪
        "sina.com",
        "sina.cn",
        "vip.sina.com",
        "vip.sina.cn",
        // 运营商
        "189.com", // 电信
        "139.com", // 移动
        // 国外
        "outlook.com",
        "gmail.com",
        "yandex.com",
        "yahoo.com",
        "myyahoo.com",
        // 友链(?
        "zyghit.cn",
        "milthm.cn",
        "morizero.com"
    )
    val trustedEmailService = setOf(
        "blophy.net",
    )
    val blockedEmailService = runBlocking {
        HttpClient().get("https://raw.githubusercontent.com/disposable/disposable-email-domains/master/domains_strict.txt")
            .bodyAsText().split("\\r?\\n|\\r").toSet()
    }
}
