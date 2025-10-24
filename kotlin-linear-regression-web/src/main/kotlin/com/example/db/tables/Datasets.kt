package com.example.db.tables

import org.jetbrains.exposed.sql.Table

object Datasets : Table("datasets") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 200)
    val createdAt = long("created_at_ms") // epoch millis

    override val primaryKey = PrimaryKey(id)
}