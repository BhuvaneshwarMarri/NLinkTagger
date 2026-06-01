package com.smaarig.nlinktagger.ui.mvi

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smaarig.nlinktagger.data.backup.BackupData
import com.smaarig.nlinktagger.data.local.entities.LinkEntity
import com.smaarig.nlinktagger.data.local.entities.TagEntity
import com.smaarig.nlinktagger.data.repository.NLinkRepository
import com.smaarig.nlinktagger.ui.theme.AppTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NLinkViewModel(private val repository: NLinkRepository) : ViewModel() {

    private val _state = MutableStateFlow(NLinkState())
    val state: StateFlow<NLinkState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<NLinkEffect>()
    val effects: SharedFlow<NLinkEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(repository.allTags, repository.allLinksWithTags) { tags, links ->
                _state.value.copy(tags = tags, links = links)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onIntent(intent: NLinkIntent) {
        when (intent) {
            is NLinkIntent.AddTag -> addTag(intent.name, intent.colorHex)
            is NLinkIntent.DeleteTag -> deleteTag(intent.tag)
            is NLinkIntent.UpdateTag -> updateTag(intent.tag)
            is NLinkIntent.AddLink -> addLink(intent.name, intent.url, intent.tagIds)
            is NLinkIntent.UpdateLink -> updateLink(intent.link, intent.tagIds)
            is NLinkIntent.DeleteLink -> deleteLink(intent.link)
            is NLinkIntent.HandleIntent -> handleIncomingIntent(intent.intent)
            is NLinkIntent.UpdateSearchQuery -> _state.value = _state.value.copy(searchQuery = intent.query)
            is NLinkIntent.ToggleTagFilter -> toggleTagFilter(intent.tagId)
            is NLinkIntent.ClearTagFilters -> _state.value = _state.value.copy(selectedFilterTagIds = emptySet())
            is NLinkIntent.ClearPendingLink -> _state.value = _state.value.copy(pendingUrl = null)
            is NLinkIntent.CreateBackup -> createBackup()
            is NLinkIntent.RestoreBackup -> restoreBackup(intent.json)
            is NLinkIntent.ChangeTheme -> _state.value = _state.value.copy(appTheme = intent.theme)
            is NLinkIntent.ChangeSortOrder -> _state.value = _state.value.copy(sortOrder = intent.sortOrder)
        }
    }

    private fun createBackup() {
        viewModelScope.launch {
            try {
                val data = repository.getBackupData()
                val json = Gson().toJson(data)
                _effects.emit(NLinkEffect.SaveBackupToFile(json))
            } catch (e: Exception) {
                _effects.emit(NLinkEffect.ShowToast("Backup failed: ${e.message}"))
            }
        }
    }

    private fun restoreBackup(json: String) {
        viewModelScope.launch {
            try {
                val data = Gson().fromJson(json, BackupData::class.java)
                repository.restoreBackupData(data)
                _effects.emit(NLinkEffect.ShowToast("Backup restored successfully"))
            } catch (e: Exception) {
                _effects.emit(NLinkEffect.ShowToast("Restore failed: ${e.message}"))
            }
        }
    }

    private fun addTag(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertTag(TagEntity(name = name, colorHex = colorHex))
        }
    }

    private fun deleteTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.deleteTag(tag)
        }
    }

    private fun updateTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.insertTag(tag)
        }
    }

    private fun addLink(name: String, url: String, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.insertLinkWithTags(LinkEntity(url = url, name = name), tagIds)
            _state.value = _state.value.copy(pendingUrl = null)
            _effects.emit(NLinkEffect.ClosePopup)
        }
    }

    private fun updateLink(link: LinkEntity, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.insertLinkWithTags(link, tagIds)
        }
    }

    private fun deleteLink(link: LinkEntity) {
        viewModelScope.launch {
            repository.deleteLink(link)
        }
    }

    private fun toggleTagFilter(tagId: Long) {
        val currentFilters = _state.value.selectedFilterTagIds
        val newFilters = if (tagId in currentFilters) {
            currentFilters - tagId
        } else {
            currentFilters + tagId
        }
        _state.value = _state.value.copy(selectedFilterTagIds = newFilters)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent?.let {
            if (it.action == Intent.ACTION_SEND && it.type == "text/plain") {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    _state.value = _state.value.copy(pendingUrl = text)
                }
            } else if (it.action == Intent.ACTION_VIEW) {
                val data = it.dataString
                if (data != null) {
                    _state.value = _state.value.copy(pendingUrl = data)
                }
            }
        }
    }

    class Factory(private val repository: NLinkRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NLinkViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NLinkViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
