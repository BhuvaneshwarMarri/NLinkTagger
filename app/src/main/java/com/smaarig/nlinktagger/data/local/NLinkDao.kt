package com.smaarig.nlinktagger.data.local

import androidx.room.*
import com.smaarig.nlinktagger.data.local.entities.LinkEntity
import com.smaarig.nlinktagger.data.local.entities.LinkTagCrossRef
import com.smaarig.nlinktagger.data.local.entities.LinkWithTags
import com.smaarig.nlinktagger.data.local.entities.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NLinkDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Transaction
    @Query("SELECT * FROM links")
    fun getAllLinksWithTags(): Flow<List<LinkWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: LinkEntity): Long

    @Delete
    suspend fun deleteLink(link: LinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkTagCrossRef(crossRef: LinkTagCrossRef)

    @Query("DELETE FROM LinkTagCrossRef WHERE linkId = :linkId")
    suspend fun deleteLinkTagCrossRefsForLink(linkId: Long)

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsList(): List<TagEntity>

    @Query("SELECT * FROM links")
    suspend fun getAllLinksList(): List<LinkEntity>

    @Query("SELECT * FROM LinkTagCrossRef")
    suspend fun getAllCrossRefs(): List<LinkTagCrossRef>

    @Transaction
    suspend fun insertLinkWithTags(link: LinkEntity, tagIds: List<Long>) {
        val linkId = insertLink(link)
        deleteLinkTagCrossRefsForLink(linkId)
        tagIds.forEach { tagId ->
            insertLinkTagCrossRef(LinkTagCrossRef(linkId, tagId))
        }
    }
}
