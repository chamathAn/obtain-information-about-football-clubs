package com.example.footballclubs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClubsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClubs(vararg clubs: Clubs)

    @Query("SELECT * FROM clubs WHERE leagueId = :leagueId")
    suspend fun getClubsByLeague(leagueId: String): List<Clubs>

    @Query("SELECT * FROM clubs WHERE name LIKE '%' || :searchQuery || '%' OR leagueId IN (SELECT idLeague FROM leagues WHERE strLeague LIKE '%' || :searchQuery || '%')")
    suspend fun searchClubsByClubNameOrLeague(searchQuery: String): List<Clubs>


}
