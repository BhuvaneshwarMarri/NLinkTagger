package com.smaarig.nlinktagger.ui.screens.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.smaarig.nlinktagger.data.local.entities.TagEntity
import com.smaarig.nlinktagger.ui.components.AddTagDialog
import com.smaarig.nlinktagger.ui.mvi.NLinkIntent
import com.smaarig.nlinktagger.ui.mvi.NLinkViewModel

@Composable
fun TagsScreen(tags: List<TagEntity>, viewModel: NLinkViewModel) {
    if (tags.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("(NO TAGS FOUND)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(tags) { tag ->
                var showEditDialog by remember { mutableStateOf(false) }

                if (showEditDialog) {
                    AddTagDialog(
                        onDismiss = { showEditDialog = false },
                        onAdd = { name, color ->
                            viewModel.onIntent(NLinkIntent.UpdateTag(tag.copy(name = name, colorHex = color)))
                            showEditDialog = false
                        },
                        initialName = tag.name,
                        initialColor = tag.colorHex,
                        title = "EDIT TAG"
                    )
                }

                TagItem(
                    tag = tag, 
                    onDelete = { viewModel.onIntent(NLinkIntent.DeleteTag(tag)) },
                    onEdit = { showEditDialog = true }
                )
            }
        }
    }
}

@Composable
fun TagItem(tag: TagEntity, onDelete: () -> Unit, onEdit: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(Color(tag.colorHex.toColorInt()), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Text(tag.name.uppercase(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}
