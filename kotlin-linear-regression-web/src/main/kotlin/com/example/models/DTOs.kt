package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class PointDTO(val x: Double, val y: Double)

@Serializable
data class RegressionRequest(val points: List<PointDTO>)

@Serializable
data class RegressionResult(
    val n: Int,
    val slope: Double,
    val intercept: Double,
    val equation: String,
    val r2: Double?,
    val minX: Double,
    val maxX: Double,
    val linePoints: List<PointDTO>
)

@Serializable
data class DatasetCreateRequest(
    val name: String,
    val points: List<PointDTO>
)

@Serializable
data class DatasetSummary(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val count: Int
)

@Serializable
data class DatasetDetail(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val points: List<PointDTO>,
    val regression: RegressionResult
)