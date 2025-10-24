package com.example.db.tables

import org.jetbrains.exposed.sql.Table

object DataPoints : Table("data_points") {
    val id = long("id").autoIncrement()
    val datasetId = long("dataset_id").references(Datasets.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val x = double("x")
    val y = double("y")
    override val primaryKey = PrimaryKey(id)
}