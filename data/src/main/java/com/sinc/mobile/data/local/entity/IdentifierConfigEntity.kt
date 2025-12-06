package com.sinc.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identifier_configs")
data class IdentifierConfigEntity(
    @PrimaryKey val type: String,
    val label: String,
    val hint: String,
    val regex: String
)
