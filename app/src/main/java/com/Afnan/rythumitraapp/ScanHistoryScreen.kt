package com.Afnan.rythumitraapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Afnan.rythumitraapp.data.ScanRepository
import com.Afnan.rythumitraapp.data.ScanResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanHistoryScreen(
    onBack: () -> Unit
) {

    val repository = remember {
        ScanRepository()
    }

    var scans by remember {
        mutableStateOf<List<ScanResult>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var loadAttempt by remember { mutableStateOf(0) }

    var selectedScan by remember {
        mutableStateOf<ScanResult?>(null)
    }

    LaunchedEffect(loadAttempt) {
        isLoading = true
        errorMessage = null

        repository.getScans(

            onSuccess = { result ->
                scans = result.sortedByDescending { it.createdAt }
                isLoading = false
            },

            onFailure = { exception ->
                errorMessage = exception.message
                isLoading = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
            .padding(16.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("← వెనక్కి")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "📋",
                modifier = Modifier.semantics { contentDescription = "AI పరీక్షల చరిత్ర" },
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "పంట పరీక్షల చరిత్ర",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = "మీరు ఇప్పటివరకు చేసిన పంట ఆరోగ్య పరీక్షలు",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(
                start = 4.dp,
                top = 4.dp,
                bottom = 16.dp
            )
        )

        when {

            isLoading -> {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "పరీక్షల వివరాలను తెస్తున్నాం...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            errorMessage != null -> {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️ వివరాలు లోడ్ చేయలేకపోయాము.\\n${errorMessage.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { loadAttempt++ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("మళ్లీ ప్రయత్నించండి")
                        }
                    }
                }
            }

            scans.isEmpty() -> {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "📋",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Text(
                            text = "ఇంకా పంట పరీక్షలు చేయలేదు",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "పంట ఆకు ఫోటో తీసి ఆరోగ్య పరీక్ష ప్రారంభించండి.",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = scans,
                        key = { it.id }
                    ) { scan ->

                        ScanHistoryCard(
                            scan = scan,

                            onClick = {
                                selectedScan = scan
                            },

                            onDelete = {

                                repository.deleteScan(

                                    scanId = scan.id,

                                    onSuccess = {

                                        scans = scans.filter {
                                            it.id != scan.id
                                        }
                                    },

                                    onFailure = { exception ->
                                        errorMessage = exception.message
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    selectedScan?.let { scan ->

        AlertDialog(

            onDismissRequest = {
                selectedScan = null
            },

            title = {

                Text(
                    text = "పంట పరీక్ష ఫలితం 🤖",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text = scan.result
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        selectedScan = null
                    }
                ) {
                    Text("మూసివేయండి")
                }
            }
        )
    }
}


@Composable
private fun ScanHistoryCard(
    scan: ScanResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    val health = getScanValue(
        scan.result,
        "💚 ఆరోగ్య స్థితి:"
    )

    val disease = getScanValue(
        scan.result,
        "🦠 సమస్య / వ్యాధి:"
    )

    val severity = getScanValue(
        scan.result,
        "⚠️ తీవ్రత:"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "🌱 ${scan.cropName.ifBlank { "పంట తెలియదు" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = formatScanDate(scan.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                TextButton(
                    onClick = onDelete
                ) {
                    Text("🗑️ తొలగించు")
                }
            }

            if (health.isNotBlank()) {

                Text(
                    text = "💚 ఆరోగ్య స్థితి: $health",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (disease.isNotBlank()) {

                Text(
                    text = "🦠 సమస్య / వ్యాధి: $disease",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (severity.isNotBlank()) {

                Text(
                    text = "⚠️ తీవ్రత: $severity",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (
                health.isBlank() &&
                disease.isBlank() &&
                severity.isBlank()
            ) {

                Text(
                    text = "పరీక్ష ఫలితం అందుబాటులో ఉంది.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Text(
                text = "పూర్తి ఫలితాన్ని చూడటానికి ఇక్కడ నొక్కండి →",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}


private fun formatScanDate(
    timestamp: Long
): String {

    if (timestamp <= 0L) {
        return "తేదీ తెలియదు"
    }

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}


private fun getScanValue(
    result: String,
    label: String
): String {

    val line = result.lines().firstOrNull {
        it.contains(label)
    } ?: return ""

    return line
        .substringAfter(label)
        .trim()
}


private fun getScanSummary(
    result: String
): String {

    val lines = result.lines()

    val diseaseLine = lines.firstOrNull {
        it.contains("🦠 సమస్య / వ్యాధి:")
    }

    val severityLine = lines.firstOrNull {
        it.contains("⚠️ తీవ్రత:")
    }

    return buildString {

        if (diseaseLine != null) {
            append(diseaseLine.trim())
        }

        if (severityLine != null) {

            if (isNotEmpty()) {
                append("\n")
            }

            append(severityLine.trim())
        }

        if (isEmpty()) {
            append("పరీక్ష ఫలితం అందుబాటులో ఉంది.")
        }
    }
}
