package com.example.footballclubs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LeaguesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagues(vararg leagues: Leagues)

    @Query("SELECT * FROM leagues")
    suspend fun getAllLeagues(): List<Leagues>
}
