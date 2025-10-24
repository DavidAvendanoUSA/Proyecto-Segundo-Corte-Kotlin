package com.example.service

import com.example.models.PointDTO
import com.example.models.RegressionResult
import kotlin.math.pow

/**
 * Servicio de regresión lineal simple.
 *
 * - Implementa la solución cerrada (ecuaciones normales) para el modelo y = m*x + b.
 * - Complejidad O(n) sobre el número de puntos.
 * - Incluye validaciones básicas y cálculo opcional de R².
 * - Devuelve además dos puntos de la recta (en minX y maxX) para facilitar el plot.
 */
object RegressionService {

    /**
     * Validaciones de entrada:
     * - Al menos 2 puntos.
     * - x e y finitos (sin NaN/Infinity).
     * - No todos los x idénticos (evita var(x)=0).
     *
     * Lanza IllegalArgumentException si alguna condición falla.
     */
    fun validatePoints(points: List<PointDTO>) {
        require(points.size >= 2) { "Se requieren al menos 2 puntos para una regresión lineal." }

        // Verifica que cada coordenada sea un número finito.
        points.forEachIndexed { i, p ->
            require(p.x.isFinite() && p.y.isFinite()) {
                "Punto #${i + 1} inválido: x e y deben ser números finitos."
            }
        }

        // Asegura variación en X (si todos los x son iguales, la pendiente no está definida).
        val distinctX = points.map { it.x }.distinct()
        require(distinctX.size >= 2) { "Todos los valores de X son idénticos. No se puede calcular la pendiente." }
    }

    /**
     * Calcula la recta y = m*x + b y métricas derivadas.
     *
     * Fórmulas (ecuaciones normales):
     *  - m = (n*Σ(xy) - Σx*Σy) / (n*Σ(x^2) - (Σx)^2)
     *  - b = (Σy - m*Σx) / n
     *  - R² = 1 - SSres/SStot, donde:
     *      - SStot = Σ(y - ȳ)^2
     *      - SSres = Σ(y - (m*x + b))^2
     *
     * Retorna:
     *  - n, m (slope), b (intercept), ecuación formateada,
     *  - r2 (puede ser null cuando SStot=0),
     *  - minX, maxX y dos puntos de la recta para graficar.
     */
    fun compute(points: List<PointDTO>): RegressionResult {
        // 1) Precondiciones
        validatePoints(points)

        // 2) Sumatorias básicas
        val n = points.size
        val sumX = points.sumOf { it.x }
        val sumY = points.sumOf { it.y }
        val sumXY = points.sumOf { it.x * it.y }
        val sumX2 = points.sumOf { it.x * it.x }

        // 3) Componentes de la fórmula de la pendiente
        val numerator = n * sumXY - sumX * sumY
        val denominator = n * sumX2 - sumX * sumX
        require(denominator != 0.0) { "No se puede calcular la pendiente: denominador 0." }

        // 4) Parámetros de la recta
        val m = numerator / denominator
        val b = (sumY - m * sumX) / n

        // 5) Puntos extremos de la recta (útiles para dibujarla en el rango observado)
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val linePoints = listOf(
            PointDTO(minX, m * minX + b),
            PointDTO(maxX, m * maxX + b)
        )

        // 6) Métrica de bondad de ajuste (R²). Si no hay varianza en Y (SStot=0), lo marcamos como null.
        val meanY = sumY / n
        val ssTot = points.sumOf { (it.y - meanY).pow(2) }
        val ssRes = points.sumOf { (it.y - (m * it.x + b)).pow(2) }
        val r2 = if (ssTot == 0.0) null else 1 - (ssRes / ssTot)

        // 7) Ecuación formateada (más decimales para depuración/consistencia)
        val eq = "y = ${"%.6f".format(m)}x + ${"%.6f".format(b)}"

        // 8) Ensamblado del resultado
        return RegressionResult(
            n = n,
            slope = m,
            intercept = b,
            equation = eq,
            r2 = r2,
            minX = minX,
            maxX = maxX,
            linePoints = linePoints
        )
    }
}