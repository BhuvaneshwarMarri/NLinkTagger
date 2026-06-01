package com.smaarig.nlinktagger.ui.mvi

import com.smaarig.nlinktagger.data.local.entities.LinkEntity
import com.smaarig.nlinktagger.data.local.entities.LinkWithTags
import com.smaarig.nlinktagger.data.local.entities.TagEntity
import com.smaarig.nlinktagger.ui.theme.AppTheme

enum class SortOrder {
    NEWEST, OLDEST, NAME_ASC, NAME_DESC
}

data class NLinkState(
    val tags: List<TagEntity> = emptyList(),
    val links: List<LinkWithTags> = emptyList(),
    val pendingUrl: String? = null,
    val searchQuery: String = "",
    val selectedFilterTagIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val appTheme: AppTheme = AppTheme.MINIMAL,
    val sortOrder: SortOrder = SortOrder.NEWEST
) {
    val filteredLinks: List<LinkWithTags> = links.filter { linkWithTags ->
        val matchesSearch = linkWithTags.link.name.contains(searchQuery, ignoreCase = true) ||
                linkWithTags.link.url.contains(searchQuery, ignoreCase = true)
        
        val matchesTags = selectedFilterTagIds.isEmpty() || 
                linkWithTags.tags.any { it.id in selectedFilterTagIds }
        
        matchesSearch && matchesTags
    }.let { filtered ->
        when (sortOrder) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.link.id }
            SortOrder.OLDEST -> filtered.sortedBy { it.link.id }
            SortOrder.NAME_ASC -> filtered.sortedBy { it.link.name.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.link.name.lowercase() }
        }
    }
}

sealed class NLinkIntent {
    data class AddTag(val name: String, val colorHex: String) : NLinkIntent()
    data class DeleteTag(val tag: TagEntity) : NLinkIntent()
    data class UpdateTag(val tag: TagEntity) : NLinkIntent()
    data class AddLink(val name: String, val url: String, val tagIds: List<Long>) : NLinkIntent()
    data class UpdateLink(val link: LinkEntity, val tagIds: List<Long>) : NLinkIntent()
    data class DeleteLink(val link: LinkEntity) : NLinkIntent()
    data class HandleIntent(val intent: android.content.Intent?) : NLinkIntent()
    data class UpdateSearchQuery(val query: String) : NLinkIntent()
    data class ToggleTagFilter(val tagId: Long) : NLinkIntent()
    object ClearTagFilters : NLinkIntent()
    object ClearPendingLink : NLinkIntent()
    object CreateBackup : NLinkIntent()
    data class RestoreBackup(val json: String) : NLinkIntent()
    data class ChangeTheme(val theme: AppTheme) : NLinkIntent()
    data class ChangeSortOrder(val sortOrder: SortOrder) : NLinkIntent()
}

sealed class NLinkEffect {
    data class ShowToast(val message: String) : NLinkEffect()
    data class SaveBackupToFile(val json: String) : NLinkEffect()
    object ClosePopup : NLinkEffect()
}
