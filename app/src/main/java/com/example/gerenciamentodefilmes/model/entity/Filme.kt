package com.example.gerenciamentodefilmes.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filme")
data class Filme(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var titulo: String,
    var diretor: String
)