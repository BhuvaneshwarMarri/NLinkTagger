package com.smaarig.nlinktagger.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["linkId", "tagId"],
    indices = [Index("tagId")],
    foreignKeys = [
        ForeignKey(
            entity = LinkEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LinkTagCrossRef(
    val linkId: Long,
    val tagId: Long
)
