package com.example.footballclubs.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Leagues::class, Clubs::class],
    version = 2
)
abstract class LeagueDatabase: RoomDatabase() {
    abstract fun leaguesDao(): LeaguesDao
    abstract fun clubsDao(): ClubsDao
}
