package com.example.routes

import com.example.models.RegressionRequest
import com.example.service.RegressionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Endpoints de regresión lineal.
 *
 * Ruta base: /api/regression
 *
 * Contrato:
 * - POST /api/regression
 *   - Request (application/json):
 *     {
 *       "points": [
 *         { "x": <Double>, "y": <Double> },
 *         ...
 *       ]
 *     }
 *   - Response (application/json):
 *     {
 *       "n": <Int>,
 *       "slope": <Double>,
 *       "intercept": <Double>,
 *       "equation": "y = mx + b",
 *       "r2": <Double|null>,
 *       "minX": <Double>,
 *       "maxX": <Double>,
 *       "linePoints": [ { "x": <Double>, "y": <Double> }, { ... } ]
 *     }
 *
 * Códigos de estado:
 * - 200 OK: cálculo exitoso.
 * - 400 Bad Request: validación fallida (p. ej., menos de 2 puntos, var(x)=0, NaN/Inf).
 * - 500 Internal Server Error: error inesperado en el servidor.
 *
 * Requisitos:
 * - El plugin ContentNegotiation (kotlinx.serialization) debe estar instalado
 *   para soportar call.receive<RegressionRequest>() y call.respond(...).
 *
 * Nota:
 * - Las IllegalArgumentException provienen de validaciones en RegressionService.
 * - Si se desea estandarizar errores, considerar mover el manejo a StatusPages.
 */
fun Route.regressionRoutes() {
    route("/api/regression") {
        post {
            // Deserializa el cuerpo JSON a RegressionRequest (requiere ContentNegotiation instalado).
            val req = call.receive<RegressionRequest>()
            try {
                // Ejecuta el cálculo (incluye validaciones de datos).
                val result = RegressionService.compute(req.points)

                // Respuesta 200 con el resultado serializado a JSON.
                call.respond(result)
            } catch (e: IllegalArgumentException) {
                // Errores de entrada del cliente (validaciones): 400 con mensaje.
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                // Falla no controlada: 500 con mensaje genérico (evitar filtrar detalles internos).
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error")))
            }
        }
    }
}