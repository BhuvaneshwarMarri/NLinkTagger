package com.smaarig.nlinktagger.data.repository

import com.smaarig.nlinktagger.data.local.NLinkDao
import com.smaarig.nlinktagger.data.local.entities.LinkEntity
import com.smaarig.nlinktagger.data.local.entities.LinkTagCrossRef
import com.smaarig.nlinktagger.data.local.entities.LinkWithTags
import com.smaarig.nlinktagger.data.local.entities.TagEntity
import com.smaarig.nlinktagger.data.backup.BackupData
import kotlinx.coroutines.flow.Flow

class NLinkRepository(private val nlinkDao: NLinkDao) {
    val allTags: Flow<List<TagEntity>> = nlinkDao.getAllTags()
    val allLinksWithTags: Flow<List<LinkWithTags>> = nlinkDao.getAllLinksWithTags()

    suspend fun insertTag(tag: TagEntity) = nlinkDao.insertTag(tag)
    suspend fun deleteTag(tag: TagEntity) = nlinkDao.deleteTag(tag)

    suspend fun insertLinkWithTags(link: LinkEntity, tagIds: List<Long>) = 
        nlinkDao.insertLinkWithTags(link, tagIds)

    suspend fun deleteLink(link: LinkEntity) = nlinkDao.deleteLink(link)

    suspend fun getBackupData(): BackupData {
        return BackupData(
            tags = nlinkDao.getAllTagsList(),
            links = nlinkDao.getAllLinksList(),
            crossRefs = nlinkDao.getAllCrossRefs()
        )
    }

    suspend fun restoreBackupData(backupData: BackupData) {
        val existingTags = nlinkDao.getAllTagsList()
        val tagIdMap = mutableMapOf<Long, Long>()

        // 1. Process Tags
        backupData.tags.forEach { tag ->
            val existing = existingTags.find { it.name.equals(tag.name, ignoreCase = true) }
            if (existing != null) {
                tagIdMap[tag.id] = existing.id
            } else {
                val newId = nlinkDao.insertTag(tag.copy(id = 0))
                tagIdMap[tag.id] = newId
            }
        }

        // 2. Process Links and CrossRefs
        val existingLinks = nlinkDao.getAllLinksList()
        backupData.links.forEach { link ->
            val existing = existingLinks.find { it.url == link.url && it.name == link.name }
            val linkId = if (existing != null) {
                existing.id
            } else {
                nlinkDao.insertLink(link.copy(id = 0))
            }

            // Find cross refs for this old link id
            backupData.crossRefs.filter { it.linkId == link.id }.forEach { crossRef ->
                val newTagId = tagIdMap[crossRef.tagId]
                if (newTagId != null) {
                    nlinkDao.insertLinkTagCrossRef(LinkTagCrossRef(linkId, newTagId))
                }
            }
        }
    }
}
