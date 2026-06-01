package com.smaarig.nlinktagger.data.backup

import com.smaarig.nlinktagger.data.local.entities.LinkEntity
import com.smaarig.nlinktagger.data.local.entities.LinkTagCrossRef
import com.smaarig.nlinktagger.data.local.entities.TagEntity

data class BackupData(
    val tags: List<TagEntity>,
    val links: List<LinkEntity>,
    val crossRefs: List<LinkTagCrossRef>,
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
