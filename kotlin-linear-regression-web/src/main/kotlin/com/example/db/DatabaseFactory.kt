package com.example.db

import com.example.db.tables.DataPoints
import com.example.db.tables.Datasets
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.Connection

/**
 * Bootstrap de la capa de persistencia.
 *
 * Responsabilidades:
 * - Leer configuración de BD desde el entorno de Ktor (application.conf / variables).
 * - Crear el pool de conexiones (HikariCP) y registrar la conexión en Exposed.
 * - Ejecutar migraciones de esquema con Flyway (classpath:db/migration).
 * - Ajustar el nivel de aislamiento por defecto para las transacciones Exposed.
 *
 * Claves de configuración (application.conf):
 * - app.db.url: JDBC URL (por defecto "jdbc:sqlite:./data/app.db").
 * - app.db.driver: clase del driver JDBC (por defecto "org.sqlite.JDBC").
 * - app.db.maxPoolSize: tamaño máximo del pool (por defecto 5).
 *
 * Notas:
 * - Para SQLite se asegura la creación del directorio destino del archivo *.db.
 * - isAutoCommit=false delega el control transaccional a Exposed.
 * - El aislamiento SERIALIZABLE es el más estricto; en SQLite se mapea al modo correspondiente.
 */
object DatabaseFactory {
    fun init(env: ApplicationEnvironment) {
        val config = env.config
        val url = config.propertyOrNull("app.db.url")?.getString()
            ?: "jdbc:sqlite:./data/app.db"
        val driver = config.propertyOrNull("app.db.driver")?.getString()
            ?: "org.sqlite.JDBC"
        val maxPool = config.propertyOrNull("app.db.maxPoolSize")?.getString()?.toIntOrNull() ?: 5

        // Si usamos SQLite por archivo, nos aseguramos de que exista el directorio destino.
        if (url.startsWith("jdbc:sqlite:")) {
            val path = url.removePrefix("jdbc:sqlite:")
            File(path).parentFile?.mkdirs()
        }

        // Configuración de HikariCP (pool de conexiones).
        // - isAutoCommit=false: Exposed manejará commit/rollback.
        // - transactionIsolation: nivel por defecto en las conexiones del pool.
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = driver
            maximumPoolSize = maxPool
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
            validate()
        }
        val dataSource = HikariDataSource(hikariConfig)

        // Registra el DataSource en Exposed para que las transactions usen este pool.
        Database.connect(dataSource)

        // Migraciones Flyway: aplica los scripts en resources/db/migration en orden.
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        // Nivel de aislamiento por defecto para TransactionManager (coherente con Hikari).
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
}