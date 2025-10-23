package com.example.gerenciamentodefilmes.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gerenciamentodefilmes.model.entity.Diretor

@Dao
interface DiretorDao {
    @Insert
    suspend fun inserir(diretor: Diretor)
    @Query("SELECT * FROM diretor")
    suspend fun buscarTodos(): List<Diretor>
}