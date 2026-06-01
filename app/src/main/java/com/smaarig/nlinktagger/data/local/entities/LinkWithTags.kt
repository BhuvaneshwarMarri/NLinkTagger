package com.smaarig.nlinktagger.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LinkWithTags(
    @Embedded val link: LinkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LinkTagCrossRef::class,
            parentColumn = "linkId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
