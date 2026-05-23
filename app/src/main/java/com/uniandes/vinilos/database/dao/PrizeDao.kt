package com.uniandes.vinilos.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uniandes.vinilos.database.entities.PrizeEntity

@Dao
interface PrizeDao {
    @Query("SELECT * FROM prizes")
    suspend fun getAll(): List<PrizeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prizes: List<PrizeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prize: PrizeEntity)

    @Query("DELETE FROM prizes")
    suspend fun deleteAll()
}
