package com.smaarig.nlinktagger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val name: String
)
