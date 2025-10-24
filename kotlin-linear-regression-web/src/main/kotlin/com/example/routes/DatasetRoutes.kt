package com.example.routes

import com.example.models.DatasetCreateRequest
import com.example.repository.DatasetRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Endpoints CRUD para datasets.
 *
 * Base path: /api/datasets
 *
 * Contrato:
 * - GET /api/datasets
 *   -> 200 OK: List<DatasetSummary>
 *
 * - POST /api/datasets
 *   Request (application/json): DatasetCreateRequest { name: String, points: List<PointDTO> }
 *   -> 201 Created: DatasetDetail
 *   -> 400 Bad Request: validación fallida (p. ej., <2 puntos, var(x)=0, NaN/Inf)
 *
 * - GET /api/datasets/{id}
 *   -> 200 OK: DatasetDetail
 *   -> 404 Not Found: id inexistente
 *   -> 400 Bad Request: id inválido
 *
 * - DELETE /api/datasets/{id}
 *   -> 204 No Content: eliminado
 *   -> 404 Not Found: id inexistente
 *   -> 400 Bad Request: id inválido
 *
 * Requisitos:
 * - ContentNegotiation (kotlinx.serialization) instalado para (de)serializar JSON.
 *
 * Nota de diseño:
 * - Aquí se instancia un repositorio global simple. En entornos reales, considerar
 *   inyección de dependencias (DI) y pasar el repo desde Application.module().
 */
private val repo = DatasetRepository()

/**
 * Registra las rutas de datasets bajo /api/datasets.
 */
fun Route.datasetRoutes() {
    route("/api/datasets") {

        // GET /api/datasets — listado (resumen con count de puntos)
        get {
            val list = repo.listDatasets()
            call.respond(list) // 200 OK
        }

        // POST /api/datasets — creación (cabecera + puntos) y cálculo de regresión
        post {
            val req = call.receive<DatasetCreateRequest>() // deserializa JSON -> DTO
            try {
                val created = repo.createDataset(req)
                call.respond(HttpStatusCode.Created, created) // 201 Created
            } catch (e: IllegalArgumentException) {
                // Errores de validación de dominio (input inválido)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                // Error inesperado del servidor (evitar filtrar detalles internos)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error")))
            }
        }

        // GET /api/datasets/{id} — detalle con puntos y regresión
        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

            val detail = repo.getDataset(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "No encontrado"))

            call.respond(detail) // 200 OK
        }

        // DELETE /api/datasets/{id} — elimina dataset y sus puntos
        delete("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

            val deleted = repo.deleteDataset(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent) // 204 sin body
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No encontrado"))
            }
        }
    }
}