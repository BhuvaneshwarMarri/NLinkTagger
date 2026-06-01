package com.smaarig.nlinktagger

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smaarig.nlinktagger.data.backup.BackupWorker
import com.smaarig.nlinktagger.data.local.NLinkDatabase
import com.smaarig.nlinktagger.data.repository.NLinkRepository
import com.smaarig.nlinktagger.ui.components.AddLinkPopup
import com.smaarig.nlinktagger.ui.components.AddTagDialog
import com.smaarig.nlinktagger.ui.components.ManualAddLinkDialog
import com.smaarig.nlinktagger.ui.mvi.NLinkEffect
import com.smaarig.nlinktagger.ui.mvi.NLinkIntent
import com.smaarig.nlinktagger.ui.mvi.NLinkViewModel
import com.smaarig.nlinktagger.ui.screens.links.LinksScreen
import com.smaarig.nlinktagger.ui.screens.settings.SettingsScreen
import com.smaarig.nlinktagger.ui.screens.tags.TagsScreen
import com.smaarig.nlinktagger.ui.theme.AppTheme
import com.smaarig.nlinktagger.ui.theme.NLinkTaggerTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val database by lazy { NLinkDatabase.getDatabase(this) }
    private val repository by lazy { NLinkRepository(database.nlinkDao()) }
    private val viewModel: NLinkViewModel by viewModels { NLinkViewModel.Factory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.onIntent(NLinkIntent.HandleIntent(intent))

        scheduleBackup()

        setContent {
            val state by viewModel.state.collectAsState()
            NLinkTaggerTheme(appTheme = state.appTheme) {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is NLinkEffect.ShowToast -> {
                                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                            }
                            is NLinkEffect.SaveBackupToFile -> {
                                pendingBackupJson = effect.json
                                createBackupLauncher.launch("nlink_backup_${System.currentTimeMillis()}.json")
                            }
                            else -> {}
                        }
                    }
                }
                MainScreen(viewModel)
            }
        }
    }

    private fun scheduleBackup() {
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyBackup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    private var pendingBackupJson: String? = null
    private val createBackupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer ->
                writer.write(pendingBackupJson ?: "")
                Toast.makeText(this, "Backup saved!", Toast.LENGTH_SHORT).show()
            }
            pendingBackupJson = null
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        viewModel.onIntent(NLinkIntent.HandleIntent(intent))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NLinkViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddManualLinkDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val isFunky = state.appTheme == AppTheme.FUNKY

    if (showSettings) {
        SettingsScreen(state, viewModel, onBack = { showSettings = false })
        return
    }

    Scaffold(
        containerColor = if (isFunky) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "NLINK TAGGER", 
                        style = if (isFunky) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (isFunky) 2.sp else 1.sp,
                        color = if (isFunky) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground
                    ) 
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = "Settings",
                            tint = if (isFunky) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isFunky) 48.dp else 16.dp)
            ) {
                if (!isFunky) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isFunky) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (isFunky) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            height = if (isFunky) 8.dp else 2.dp
                        )
                    },
                    modifier = if (isFunky) Modifier.padding(horizontal = 4.dp).border(4.dp, Color.Black) else Modifier,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = if (isFunky && selectedTab == 0) Modifier.background(MaterialTheme.colorScheme.surface) else Modifier,
                        text = { 
                            Text(
                                "LINKS", 
                                style = if (isFunky) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
                                fontWeight = if (isFunky) FontWeight.Black else FontWeight.Normal
                            ) 
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = if (isFunky && selectedTab == 1) Modifier.background(MaterialTheme.colorScheme.surface) else Modifier,
                        text = { 
                            Text(
                                "TAGS", 
                                style = if (isFunky) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
                                fontWeight = if (isFunky) FontWeight.Black else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            Box(contentAlignment = Alignment.Center) {
                if (isFunky) {
                    // Offset shadow for neubrutalism
                    Box(
                        Modifier
                            .offset(6.dp, 6.dp)
                            .size(64.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                }
                FloatingActionButton(
                    onClick = { 
                        if (selectedTab == 0) showAddManualLinkDialog = true 
                        else showAddTagDialog = true 
                    },
                    containerColor = if (isFunky) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    contentColor = if (isFunky) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                    shape = if (isFunky) RoundedCornerShape(4.dp) else CircleShape,
                    modifier = Modifier
                        .padding(bottom = 0.dp) // Reset padding for the Box
                        .then(if (isFunky) Modifier.size(64.dp).border(4.dp, Color.Black, RoundedCornerShape(4.dp)) else Modifier)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add", 
                        modifier = if (isFunky) Modifier.size(32.dp) else Modifier.size(24.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .padding(bottom = 60.dp) // Ensure FAB doesn't cover content
                .fillMaxSize()
        ) {
            if (selectedTab == 0) {
                LinksScreen(state, viewModel, onNewTagClick = { showAddTagDialog = true })
            } else {
                TagsScreen(state.tags, viewModel)
            }

            state.pendingUrl?.let { url ->
                AddLinkPopup(
                    url = url, 
                    tags = state.tags, 
                    viewModel = viewModel,
                    onNewTagClick = { showAddTagDialog = true }
                )
            }

            if (showAddManualLinkDialog) {
                ManualAddLinkDialog(
                    onDismiss = { showAddManualLinkDialog = false },
                    tags = state.tags,
                    onAdd = { name, url, tagIds ->
                        viewModel.onIntent(NLinkIntent.AddLink(name, url, tagIds))
                        showAddManualLinkDialog = false
                    },
                    onNewTagClick = { showAddTagDialog = true }
                )
            }

            if (showAddTagDialog) {
                AddTagDialog(
                    onDismiss = { showAddTagDialog = false },
                    onAdd = { name, color ->
                        viewModel.onIntent(NLinkIntent.AddTag(name, color))
                        showAddTagDialog = false
                    }
                )
            }
        }
    }
}
