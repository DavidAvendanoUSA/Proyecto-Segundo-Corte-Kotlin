package com.example.repository

import com.example.db.tables.DataPoints
import com.example.db.tables.Datasets
import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

/**
 * Repositorio de acceso a datos para Datasets y sus puntos.
 *
 * Notas:
 * - Usa Exposed DSL sobre SQLite.
 * - Cada operación se ejecuta dentro de `transaction {}` para garantizar atomicidad.
 * - Los métodos retornan DTOs listos para la capa de rutas.
 */
class DatasetRepository {

    /**
     * Crea un dataset y sus puntos asociados.
     *
     * Flujo:
     * 1) Calcula la regresión en memoria (antes de la escritura).
     * 2) Inserta el dataset (cabecera) y luego los puntos con `batchInsert`.
     * 3) Devuelve el detalle con la misma colección de puntos y el resultado de la regresión.
     *
     * Consideraciones:
     * - `createdAtMs` se toma del reloj de la app; podría leerse de la fila insertada si la BD lo genera.
     */
    fun createDataset(req: DatasetCreateRequest): DatasetDetail {
        // Cálculo de regresión previo (no depende del ID generado).
        val regression = com.example.service.RegressionService.compute(req.points)

        // Inserción atómica del dataset y sus puntos.
        val datasetId: Long = transaction {
            val now = Instant.now().toEpochMilli()

            // Inserta cabecera y obtiene el ID autogenerado.
            val newId = Datasets.insert {
                it[name] = req.name
                it[createdAt] = now
            } get Datasets.id

            // Inserción masiva de puntos para eficiencia.
            DataPoints.batchInsert(req.points) { p ->
                this[DataPoints.datasetId] = newId
                this[DataPoints.x] = p.x
                this[DataPoints.y] = p.y
            }

            newId
        }

        // Ensambla el DTO de salida.
        return DatasetDetail(
            id = datasetId,
            name = req.name,
            createdAtMs = Instant.now().toEpochMilli(),
            points = req.points,
            regression = regression
        )
    }

    /**
     * Lista datasets con conteo de puntos, ordenados por fecha de creación desc.
     *
     * Implementación:
     * - Primero construye un mapa datasetId -> count mediante agrupación.
     * - Luego recorre Datasets y compone los DTOs usando el mapa para `count`.
     * - Exposed COUNT retorna Long; aquí se convierte explícitamente a Int para el DTO.
     */
    fun listDatasets(): List<DatasetSummary> = transaction {
        // Conteo pre-agregado de puntos por dataset.
        val counts: Map<Long, Long> = DataPoints
            .slice(DataPoints.datasetId, DataPoints.id.count())
            .selectAll()
            .groupBy(DataPoints.datasetId)
            .associate { it[DataPoints.datasetId] to it[DataPoints.id.count()] }

        // Listado principal de datasets.
        Datasets
            .selectAll()
            .orderBy(Datasets.createdAt to SortOrder.DESC)
            .map {
                DatasetSummary(
                    id = it[Datasets.id],
                    name = it[Datasets.name],
                    createdAtMs = it[Datasets.createdAt],
                    count = (counts[it[Datasets.id]] ?: 0L).toInt() // COUNT(Long) -> Int
                )
            }
    }

    /**
     * Obtiene el detalle de un dataset por id (incluye puntos y regresión).
     *
     * - Retorna null si el dataset no existe.
     * - La regresión se recalcula sobre los puntos persistidos.
     */
    fun getDataset(id: Long): DatasetDetail? = transaction {
        // Cabecera del dataset (si no existe, null).
        val dsRow = Datasets.select { Datasets.id eq id }.singleOrNull() ?: return@transaction null

        // Puntos del dataset (ordenados por id para estabilidad).
        val pts = DataPoints
            .select { DataPoints.datasetId eq id }
            .orderBy(DataPoints.id to SortOrder.ASC)
            .map { PointDTO(it[DataPoints.x], it[DataPoints.y]) }

        // Regresión sobre los puntos recuperados.
        val regression = com.example.service.RegressionService.compute(pts)

        // DTO de detalle.
        DatasetDetail(
            id = dsRow[Datasets.id],
            name = dsRow[Datasets.name],
            createdAtMs = dsRow[Datasets.createdAt],
            points = pts,
            regression = regression
        )
    }

    /**
     * Elimina un dataset por id.
     *
     * Retorna:
     * - true si se afectó al menos una fila.
     * - false si no existía el id.
     */
    fun deleteDataset(id: Long): Boolean = transaction {
        val deleted = Datasets.deleteWhere { Datasets.id eq id }
        deleted > 0
    }
}