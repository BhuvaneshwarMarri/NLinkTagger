package com.smaarig.nlinktagger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import com.smaarig.nlinktagger.data.local.entities.TagEntity
import com.smaarig.nlinktagger.ui.mvi.NLinkIntent
import com.smaarig.nlinktagger.ui.mvi.NLinkViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ManualAddLinkDialog(
    onDismiss: () -> Unit, 
    tags: List<TagEntity>, 
    onAdd: (String, String, List<Long>) -> Unit,
    onNewTagClick: () -> Unit,
    initialName: String = "",
    initialUrl: String = "",
    initialSelectedTagIds: List<Long> = emptyList(),
    title: String = "ADD NEW LINK"
) {
    var name by remember { mutableStateOf(initialName) }
    var url by remember { mutableStateOf(initialUrl) }
    val selectedTagIds = remember { mutableStateListOf<Long>().apply { addAll(initialSelectedTagIds) } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("LINK NAME", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(16.dp))
                
                // Selected Tags Display (with "wrong" button)
                if (selectedTagIds.isNotEmpty()) {
                    Text("SELECTED TAGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    FlowRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTagIds.forEach { tagId ->
                            val tag = tags.find { it.id == tagId } ?: return@forEach
                            val tagColor = Color(tag.colorHex.toColorInt())
                            InputChip(
                                selected = true,
                                onClick = { selectedTagIds.remove(tagId) },
                                label = { Text(tag.name.uppercase(), style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = tagColor,
                                    selectedLabelColor = if (isColorLight(tagColor)) Color.Black else Color.White
                                ),
                                border = null,
                                shape = RoundedCornerShape(4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SELECT TAGS", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onNewTagClick, contentPadding = PaddingValues(0.dp)) {
                        Text("+ NEW TAG", style = MaterialTheme.typography.labelSmall)
                    }
                }

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("PICK A TAG...", style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tags.filter { it.id !in selectedTagIds }.forEach { tag ->
                            val tagColor = Color(tag.colorHex.toColorInt())
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier
                                                .size(12.dp)
                                                .background(tagColor, CircleShape)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(tag.name.uppercase(), style = MaterialTheme.typography.labelMedium)
                                    }
                                },
                                onClick = {
                                    selectedTagIds.add(tag.id)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                        if (tags.all { it.id in selectedTagIds }) {
                            DropdownMenuItem(
                                text = { Text("NO MORE TAGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary) },
                                onClick = { expanded = false },
                                enabled = false
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                val isValidUrl = url.startsWith("http://") || url.startsWith("https://")
                if (url.isNotBlank() && !isValidUrl) {
                    Text("URL must start with http:// or https://", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = { if (name.isNotBlank() && isValidUrl) onAdd(name, url, selectedTagIds.toList()) },
                    enabled = name.isNotBlank() && isValidUrl,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE LINK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

fun isColorLight(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.5
}


@Composable
fun AddTagDialog(
    onDismiss: () -> Unit, 
    onAdd: (String, String) -> Unit,
    initialName: String = "",
    initialColor: String = "#FFFFFF",
    title: String = "CREATE NEW TAG"
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var customHex by remember { mutableStateOf("") }
    
    val colors = listOf("#FFFFFF", "#757575", "#FF0000", "#448AFF", "#69F0AE", "#FFD740", "#E040FB")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("TAG NAME", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("SELECT COLOR", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { colorHex ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .background(Color(colorHex.toColorInt()), CircleShape)
                                .border(
                                    if (selectedColor == colorHex) 2.dp else 1.dp,
                                    if (selectedColor == colorHex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clickable { 
                                    selectedColor = colorHex
                                    customHex = ""
                                }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { 
                        customHex = it
                        if (it.startsWith("#") && (it.length == 7 || it.length == 9)) {
                            try {
                                Color(it.toColorInt())
                                selectedColor = it
                            } catch (e: Exception) {}
                        } else if (!it.startsWith("#") && (it.length == 6 || it.length == 8)) {
                            try {
                                Color("#$it".toColorInt())
                                selectedColor = "#$it"
                            } catch (e: Exception) {}
                        }
                    },
                    label = { Text("CUSTOM HEX (e.g. #FFFFFF)", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { if (name.isNotBlank()) onAdd(name, selectedColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE TAG", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddLinkPopup(url: String, tags: List<TagEntity>, viewModel: NLinkViewModel, onNewTagClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val selectedTagIds = remember { mutableStateListOf<Long>() }

    Dialog(onDismissRequest = { viewModel.onIntent(NLinkIntent.ClearPendingLink) }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("TAG NEW LINK", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("URL: $url", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("LINK NAME", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(16.dp))
                
                // Selected Tags Display (with "wrong" button)
                if (selectedTagIds.isNotEmpty()) {
                    Text("SELECTED TAGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    FlowRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTagIds.forEach { tagId ->
                            val tag = tags.find { it.id == tagId } ?: return@forEach
                            val tagColor = Color(tag.colorHex.toColorInt())
                            InputChip(
                                selected = true,
                                onClick = { selectedTagIds.remove(tagId) },
                                label = { Text(tag.name.uppercase(), style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = tagColor,
                                    selectedLabelColor = if (isColorLight(tagColor)) Color.Black else Color.White
                                ),
                                border = null,
                                shape = RoundedCornerShape(4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SELECT TAGS", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onNewTagClick, contentPadding = PaddingValues(0.dp)) {
                        Text("+ NEW TAG", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                if (tags.isEmpty()) {
                    Text("(NO TAGS AVAILABLE - CREATE ONE FIRST)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("PICK A TAG...", style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            tags.filter { it.id !in selectedTagIds }.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag.name.uppercase(), style = MaterialTheme.typography.labelMedium) },
                                    onClick = {
                                        selectedTagIds.add(tag.id)
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                            if (tags.all { it.id in selectedTagIds }) {
                                DropdownMenuItem(
                                    text = { Text("NO MORE TAGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary) },
                                    onClick = { expanded = false },
                                    enabled = false
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                val isValidUrl = url.startsWith("http://") || url.startsWith("https://")
                if (!isValidUrl) {
                    Text("Invalid URL format", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = { if (name.isNotBlank() && isValidUrl) viewModel.onIntent(NLinkIntent.AddLink(name, url, selectedTagIds.toList())) },
                    enabled = name.isNotBlank() && isValidUrl,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE LINK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                TextButton(
                    onClick = { viewModel.onIntent(NLinkIntent.ClearPendingLink) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
