package com.smaarig.nlinktagger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SELECT TAGS", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onNewTagClick, contentPadding = PaddingValues(0.dp)) {
                        Text("+ NEW TAG", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = tag.id in selectedTagIds
                        Box(
                            Modifier
                                .background(
                                    if (isSelected) Color(tag.colorHex.toColorInt()) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(1.dp, Color(tag.colorHex.toColorInt()), RoundedCornerShape(4.dp))
                                .clickable { 
                                    if (isSelected) selectedTagIds.remove(tag.id) else selectedTagIds.add(tag.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val backgroundColor = if (isSelected) Color(tag.colorHex.toColorInt()) else Color.Transparent
                            val isColorLight = isColorLight(backgroundColor)
                            Text(
                                tag.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    if (isColorLight) Color.Black else Color.White
                                } else {
                                    Color(tag.colorHex.toColorInt())
                                },
                                fontWeight = FontWeight.Bold
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
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            val isSelected = tag.id in selectedTagIds
                            Box(
                                Modifier
                                    .background(
                                        if (isSelected) Color(tag.colorHex.toColorInt()) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, Color(tag.colorHex.toColorInt()), RoundedCornerShape(4.dp))
                                    .clickable { 
                                        if (isSelected) selectedTagIds.remove(tag.id) else selectedTagIds.add(tag.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                val backgroundColor = if (isSelected) Color(tag.colorHex.toColorInt()) else Color.Transparent
                                val isColorLight = isColorLight(backgroundColor)
                                Text(
                                    tag.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        if (isColorLight) Color.Black else Color.White
                                    } else {
                                        Color(tag.colorHex.toColorInt())
                                    },
                                    fontWeight = FontWeight.Bold
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
