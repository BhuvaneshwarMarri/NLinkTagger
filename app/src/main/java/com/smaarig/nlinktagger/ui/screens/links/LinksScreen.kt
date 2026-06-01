package com.smaarig.nlinktagger.ui.screens.links

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.smaarig.nlinktagger.data.local.entities.LinkWithTags
import com.smaarig.nlinktagger.ui.components.ManualAddLinkDialog
import com.smaarig.nlinktagger.ui.mvi.NLinkIntent
import com.smaarig.nlinktagger.ui.mvi.NLinkState
import com.smaarig.nlinktagger.ui.mvi.NLinkViewModel
import com.smaarig.nlinktagger.ui.theme.AppTheme

@Composable
fun LinksScreen(state: NLinkState, viewModel: NLinkViewModel, onNewTagClick: () -> Unit) {
    val isFunky = state.appTheme == AppTheme.FUNKY

    Column(Modifier.fillMaxSize()) {
        // Search Bar & Sort
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (isFunky) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .offset(4.dp, 4.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp))
                    )
                }
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onIntent(NLinkIntent.UpdateSearchQuery(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isFunky) Modifier.border(4.dp, Color.Black, RoundedCornerShape(12.dp)) else Modifier),
                    placeholder = { 
                        Text(
                            "SEARCH LINKS...", 
                            style = if (isFunky) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelMedium,
                            fontWeight = if (isFunky) FontWeight.Black else FontWeight.Normal
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null, 
                            tint = if (isFunky) Color.Black else LocalContentColor.current,
                            modifier = if (isFunky) Modifier.size(28.dp) else Modifier.size(24.dp)
                        ) 
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onIntent(NLinkIntent.UpdateSearchQuery("")) }) {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = "Clear Search",
                                    tint = if (isFunky) Color.Black else LocalContentColor.current
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(if (isFunky) 12.dp else 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isFunky) Color.Transparent else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isFunky) Color.Transparent else MaterialTheme.colorScheme.outline,
                        unfocusedContainerColor = if (isFunky) MaterialTheme.colorScheme.surface else Color.Transparent,
                        focusedContainerColor = if (isFunky) MaterialTheme.colorScheme.surface else Color.Transparent,
                        focusedTextColor = if (isFunky) Color.Black else LocalContentColor.current,
                        unfocusedTextColor = if (isFunky) Color.Black else LocalContentColor.current
                    ),
                    singleLine = true
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            var showSortMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort, 
                        contentDescription = "Sort",
                        tint = if (isFunky) Color.Black else MaterialTheme.colorScheme.primary,
                        modifier = if (isFunky) Modifier.size(32.dp) else Modifier.size(24.dp)
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    com.smaarig.nlinktagger.ui.mvi.SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    order.name.replace("_", " "),
                                    fontWeight = if (isFunky) FontWeight.Black else FontWeight.Normal
                                ) 
                            },
                            onClick = {
                                viewModel.onIntent(NLinkIntent.ChangeSortOrder(order))
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Tag Filter Row
        if (state.tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.tags) { tag ->
                        val isSelected = tag.id in state.selectedFilterTagIds
                        val tagColor = Color(tag.colorHex.toColorInt())
                        val isTagColorLight = isColorLight(tagColor)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onIntent(NLinkIntent.ToggleTagFilter(tag.id)) },
                            label = { Text(tag.name.uppercase(), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tagColor,
                                selectedLabelColor = if (isTagColorLight) Color.Black else Color.White,
                                labelColor = tagColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = tagColor,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }
                if (state.selectedFilterTagIds.isNotEmpty()) {
                    IconButton(onClick = { 
                        viewModel.onIntent(NLinkIntent.ClearTagFilters)
                    }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear Filters")
                    }
                }
            }
        }

        if (state.filteredLinks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("(NO LINKS MATCHED)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(state.filteredLinks, key = { it.link.id }) { linkWithTags ->
                    AnimatedVisibility(
                        visible = true,
                        enter = if (state.appTheme == AppTheme.FUNKY) {
                            fadeIn() + slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        } else {
                            fadeIn()
                        }
                    ) {
                        var showEditDialog by remember { mutableStateOf(false) }
                        
                        if (showEditDialog) {
                            ManualAddLinkDialog(
                                onDismiss = { showEditDialog = false },
                                tags = state.tags,
                                onAdd = { name, url, tagIds ->
                                    viewModel.onIntent(NLinkIntent.UpdateLink(linkWithTags.link.copy(name = name, url = url), tagIds))
                                    showEditDialog = false
                                },
                                onNewTagClick = onNewTagClick,
                                initialName = linkWithTags.link.name,
                                initialUrl = linkWithTags.link.url,
                                initialSelectedTagIds = linkWithTags.tags.map { it.id },
                                title = "EDIT LINK"
                            )
                        }

                        LinkItem(linkWithTags, viewModel, state.appTheme, onEditClick = { showEditDialog = true })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkItem(linkWithTags: LinkWithTags, viewModel: NLinkViewModel, appTheme: AppTheme, onEditClick: () -> Unit) {
    val context = LocalContext.current
    val link = linkWithTags.link
    val tags = linkWithTags.tags

    val isFunky = appTheme == AppTheme.FUNKY

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isFunky) {
            Box(
                Modifier
                    .matchParentSize()
                    .offset(6.dp, 6.dp)
                    .background(Color.Black, RoundedCornerShape(16.dp))
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    if (isFunky) MaterialTheme.colorScheme.surface else Color.Transparent,
                    RoundedCornerShape(if (isFunky) 16.dp else 8.dp)
                )
                .border(
                    if (isFunky) 4.dp else 1.dp,
                    if (isFunky) Color.Black else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(if (isFunky) 16.dp else 8.dp)
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        link.name, 
                        style = if (isFunky) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isFunky) Color.Black else LocalContentColor.current
                    )
                    Text(
                        link.url, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = if (isFunky) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit", 
                        tint = if (isFunky) Color.Black else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { viewModel.onIntent(NLinkIntent.DeleteLink(link)) }) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete", 
                        tint = if (isFunky) Color.Black else MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        val tagColor = Color(tag.colorHex.toColorInt())
                        Box(
                            Modifier
                                .background(tagColor, RoundedCornerShape(4.dp))
                                .then(if (isFunky) Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp)) else Modifier)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                tag.name.uppercase(), 
                                style = TextStyle(
                                    fontSize = 12.sp, 
                                    color = if (isColorLight(tagColor)) Color.Black else Color.White, 
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val buttonModifier = Modifier.weight(1f).height(if (isFunky) 48.dp else 40.dp)
                
                OutlinedButton(
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                        context.startActivity(intent)
                    },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(if (isFunky) 4.dp else 4.dp),
                    border = BorderStroke(if (isFunky) 3.dp else 1.dp, if (isFunky) Color.Black else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFunky) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isFunky) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("OPEN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Link", link.url)
                        clipboard.setPrimaryClip(clip)
                    },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(if (isFunky) 4.dp else 4.dp),
                    border = BorderStroke(if (isFunky) 3.dp else 1.dp, if (isFunky) Color.Black else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFunky) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                        contentColor = if (isFunky) Color.Black else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("COPY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, link.url)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Link"))
                    },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(if (isFunky) 4.dp else 4.dp),
                    border = BorderStroke(if (isFunky) 3.dp else 1.dp, if (isFunky) Color.Black else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFunky) MaterialTheme.colorScheme.secondary else Color.Transparent,
                        contentColor = if (isFunky) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("SHARE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

fun isColorLight(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.5
}

