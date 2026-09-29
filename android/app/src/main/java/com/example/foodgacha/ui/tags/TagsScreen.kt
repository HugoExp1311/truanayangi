package com.example.foodgacha.ui.tags

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagsViewModel = hiltViewModel()
) {
    val tags by viewModel.tags.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var newTagText by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importTagsFromUri(it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportMetadataToJson(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags & Data Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Import / Export Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup & Import",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("text/plain", "text/comma-separated-values", "text/csv")) },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isBusy
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Tags")
                        }

                        Button(
                            onClick = { exportLauncher.launch("food_gacha_metadata.json") },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isBusy
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON")
                        }
                    }

                    if (uiState.isBusy) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    uiState.message?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tag Management Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Manage Tags (${tags.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            label = { Text("Add Tag") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.addTag(newTagText)
                                newTagText = ""
                            },
                            enabled = newTagText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(tag.name) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.deleteTag(tag) },
                                        modifier = Modifier.width(18.dp).height(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Delete")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
