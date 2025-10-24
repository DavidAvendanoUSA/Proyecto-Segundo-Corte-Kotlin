package com.example

import com.example.db.DatabaseFactory
import com.example.plugins.configureCors
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*

/**
 * Punto de entrada y configuración base del servidor Ktor.
 *
 * - Arranca Netty en el puerto indicado por la variable de entorno PORT (fallback 8080).
 * - Registra en [Application.module] la tubería de plugins, base de datos y ruteo.
 */
fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080, // PORT externo o 8080 por defecto
        module = Application::module
    ).start(wait = true) // bloquea el hilo principal hasta la parada explícita
}

/**
 * Bootstrap de la aplicación:
 * - Observabilidad: logging de llamadas.
 * - Infra: (de)serialización JSON y CORS.
 * - Persistencia: inicialización de la capa de datos.
 * - Exposición: registro de rutas HTTP.
 */
fun Application.module() {

    // Logs de requests/responses (método, ruta, estado, latencia, etc.)
    install(CallLogging)

    // ContentNegotiation + kotlinx.serialization (JSON) y cualquier otro ajuste relacionado
    configureSerialization()

    // Políticas CORS (útil cuando el frontend vive en otro origen durante desarrollo)
    configureCors()

    // Inicializa la infraestructura de BD (pool, DSL/ORM, migraciones),
    // leyendo parámetros desde el Environment (application.conf/vars)
    DatabaseFactory.init(environment)

    // Define endpoints de la API y, si aplica, estáticos
    configureRouting()
}