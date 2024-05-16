package com.example.footballclubs.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Leagues::class, parentColumns = ["idLeague"], childColumns = ["leagueId"], onDelete = ForeignKey.CASCADE)])
data class Clubs(
    @PrimaryKey
    val id: String,
    val name: String,
    val shortName: String,
    val alternateNames: String,
    val formedYear: String,
    val leagueId: String,
    val stadium: String,
    val keywords: String,
    val stadiumThumb: String,
    val stadiumLocation: String,
    val stadiumCapacity: String,
    val website: String,
    val teamJersey: String,
    val teamLogo: String
)
