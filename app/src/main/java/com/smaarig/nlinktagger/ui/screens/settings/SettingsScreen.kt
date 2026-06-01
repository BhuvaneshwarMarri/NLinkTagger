package com.smaarig.nlinktagger.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaarig.nlinktagger.ui.mvi.NLinkIntent
import com.smaarig.nlinktagger.ui.mvi.NLinkState
import com.smaarig.nlinktagger.ui.mvi.NLinkViewModel
import com.smaarig.nlinktagger.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: NLinkState, viewModel: NLinkViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val isFunky = state.appTheme == AppTheme.FUNKY

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    val json = reader.readText()
                    viewModel.onIntent(NLinkIntent.RestoreBackup(json))
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SETTINGS", 
                        style = if (isFunky) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = if (isFunky) Modifier.border(4.dp, Color.Black).padding(4.dp) else Modifier,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isFunky) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ColorLens, 
                        contentDescription = null, 
                        tint = if (isFunky) Color.Black else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "THEME", 
                        style = if (isFunky) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ThemeOption(
                        name = "MINIMAL",
                        selected = state.appTheme == AppTheme.MINIMAL,
                        isFunky = isFunky,
                        onClick = { viewModel.onIntent(NLinkIntent.ChangeTheme(AppTheme.MINIMAL)) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        name = "FUNKY",
                        selected = state.appTheme == AppTheme.FUNKY,
                        isFunky = isFunky,
                        onClick = { viewModel.onIntent(NLinkIntent.ChangeTheme(AppTheme.FUNKY)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!isFunky) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            } else {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).background(Color.Black))
                Spacer(Modifier.height(8.dp))
            }

            // Backup Section
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Backup, 
                        contentDescription = null, 
                        tint = if (isFunky) Color.Black else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "DATA MANAGEMENT", 
                        style = if (isFunky) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(16.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    if (isFunky) {
                        Box(Modifier.matchParentSize().offset(4.dp, 4.dp).background(Color.Black, RoundedCornerShape(4.dp)))
                    }
                    Button(
                        onClick = { viewModel.onIntent(NLinkIntent.CreateBackup) },
                        modifier = Modifier.fillMaxWidth().then(if (isFunky) Modifier.border(4.dp, Color.Black, RoundedCornerShape(4.dp)) else Modifier),
                        shape = RoundedCornerShape(if (isFunky) 4.dp else 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFunky) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("EXPORT BACKUP", fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(Modifier.height(if (isFunky) 16.dp else 8.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    if (isFunky) {
                        Box(Modifier.matchParentSize().offset(4.dp, 4.dp).background(Color.Black, RoundedCornerShape(4.dp)))
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                        modifier = Modifier.fillMaxWidth().then(if (isFunky) Modifier.border(4.dp, Color.Black, RoundedCornerShape(4.dp)) else Modifier),
                        shape = RoundedCornerShape(if (isFunky) 4.dp else 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isFunky) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                            contentColor = if (isFunky) Color.Black else MaterialTheme.colorScheme.primary
                        ),
                        border = if (isFunky) null else ButtonDefaults.outlinedButtonBorder()
                    ) {
                        Text("IMPORT BACKUP", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOption(name: String, selected: Boolean, isFunky: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        if (isFunky) {
            Box(Modifier.matchParentSize().offset(4.dp, 4.dp).background(Color.Black, RoundedCornerShape(8.dp)))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    if (selected) {
                        if (isFunky) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    RoundedCornerShape(8.dp)
                )
                .border(
                    if (isFunky) 4.dp else (if (selected) 0.dp else 1.dp),
                    if (isFunky) Color.Black else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = if (selected) {
                    if (isFunky) Color.White else MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
