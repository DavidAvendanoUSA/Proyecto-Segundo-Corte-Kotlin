package com.example.plugins

import com.example.routes.datasetRoutes
import com.example.routes.regressionRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*   // staticResources está aquí
import io.ktor.server.response.*       // respondRedirect
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") { call.respondRedirect("/static/index.html") }
        staticResources("/static", "static")
        regressionRoutes()
        datasetRoutes()
    }
}