package com.smaarig.nlinktagger.ui.screens.links

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.smaarig.nlinktagger.ui.mvi.NLinkIntent
import com.smaarig.nlinktagger.ui.mvi.NLinkState
import com.smaarig.nlinktagger.ui.mvi.NLinkViewModel
import com.smaarig.nlinktagger.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(state: NLinkState, viewModel: NLinkViewModel, onBack: () -> Unit) {
    val isFunky = state.appTheme == AppTheme.FUNKY

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "FILTER BY TAGS", 
                        style = if (isFunky) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.selectedFilterTagIds.isNotEmpty()) {
                        TextButton(onClick = { viewModel.onIntent(NLinkIntent.ClearTagFilters) }) {
                            Icon(Icons.Default.ClearAll, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("CLEAR ALL")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.tags.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("(NO TAGS AVAILABLE)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Text(
                    "SELECT ONE OR MORE TAGS", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.tags.forEach { tag ->
                        val isSelected = tag.id in state.selectedFilterTagIds
                        val tagColor = Color(tag.colorHex.toColorInt())
                        val isTagColorLight = com.smaarig.nlinktagger.ui.components.isColorLight(tagColor)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onIntent(NLinkIntent.ToggleTagFilter(tag.id)) },
                            label = { 
                                Text(
                                    tag.name.uppercase(), 
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tagColor,
                                selectedLabelColor = if (isTagColorLight) Color.Black else Color.White,
                                labelColor = tagColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = tagColor,
                                enabled = true,
                                selected = isSelected,
                                borderWidth = 2.dp,
                                selectedBorderWidth = 2.dp
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("APPLY FILTERS")
            }
        }
    }
}
