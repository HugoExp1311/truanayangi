package com.example.foodgacha.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToRoll: () -> Unit,
    onNavigateToTags: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val docId = DocumentsContract.getTreeDocumentId(it)
            val path = if (docId.startsWith("primary:")) {
                "${Environment.getExternalStorageDirectory().absolutePath}/${docId.substringAfter("primary:")}"
            } else {
                docId
            }
            viewModel.updateFolderPath(path)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Food Gacha") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.hasPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Storage Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This app scans direct child folders from your device storage. Please grant All Files Access to proceed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.requestStoragePermission(context) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mother Folder Setup",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Each direct child folder will be treated as one gacha item.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.motherFolderPath,
                        onValueChange = { viewModel.updateFolderPath(it) },
                        label = { Text("Mother Folder Path") },
                        placeholder = { Text("/storage/emulated/0/MyFoodCollection") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { folderPickerLauncher.launch(null) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Browse")
                        }

                        Button(
                            onClick = { viewModel.scanFolder() },
                            enabled = !uiState.isScanning && uiState.motherFolderPath.isNotBlank(),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            if (uiState.isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(18.dp).height(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan / Rescan")
                            }
                        }
                    }

                    uiState.scanMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = "Library Overview: ${uiState.totalItemsCount} item(s) loaded",
                style = MaterialTheme.typography.titleMedium
            )

            // Navigation Actions
            FilledTonalButton(
                onClick = onNavigateToRoll,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState.totalItemsCount > 0
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Roll Gacha (CS:GO Spin)", style = MaterialTheme.typography.titleMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToLibrary,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Library")
                }

                OutlinedButton(
                    onClick = onNavigateToTags,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Label, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tags & Export")
                }
            }
        }
    }
}
