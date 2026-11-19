package com.Afnan.rythumitraapp
import com.Afnan.rythumitraapp.data.Crop

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn

import androidx.core.content.ContextCompat
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.Afnan.rythumitraapp.ui.theme.RythuMitraTheme

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.Afnan.rythumitraapp.data.CropRepository

import androidx.compose.material3.RadioButton

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// =========================================================
// MAIN ACTIVITY
// =========================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

// Initialize Firebase App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        val isDebuggable =
            (applicationInfo.flags and
                    android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        if (isDebuggable) {
            // Development builds
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            // Production builds
            firebaseAppCheck.installAppCheckProviderFactory(
                RecaptchaAppCheckProviderFactory.getInstance(
                    "6LddVRcuAAAAAI0vmMcUFmeuN2spfVBeIdRbpP45"
                )
            )
        }

// Anonymous Authentication
        FirebaseAuthManager.signInAnonymously()
        setContent {
            RythuMitraTheme {
                RythuMitraApp()
            }
        }
    }
}


// =========================================================
// MAIN APP
// =========================================================

@Composable
fun RythuMitraApp() {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var openScannerFromProfile by remember {
        mutableStateOf(false)
    }
    var currentWeatherData by remember {
        mutableStateOf<WeatherData?>(null)
    }
    var farmerCrops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }
    LaunchedEffect(Unit) {

        CropRepository().getCrops(
            onSuccess = { crops ->

                farmerCrops = crops

                android.util.Log.d(
                    "RythuMitraCrops",
                    "Loaded crops: ${crops.size}, names: ${crops.map { it.name }}, stages: ${crops.map { it.growthStage }}"
                )
            },
            onFailure = { error ->
                android.util.Log.e(
                    "RythuMitraCrops",
                    "Failed to load crops",
                    error
                )
            }
        )
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    icon = {
                        Text("🏠", modifier = Modifier.semantics { contentDescription = "హోమ్ ట్యాబ్" })
                    },
                    label = {
                        Text("హోమ్")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                    },
                    icon = {
                        Text("🌱", modifier = Modifier.semantics { contentDescription = "పంటల ట్యాబ్" })
                    },
                    label = {
                        Text("పంటలు")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                    },
                    icon = {
                        Text("🔔", modifier = Modifier.semantics { contentDescription = "హెచ్చరికల ట్యాబ్" })
                    },
                    label = {
                        Text("హెచ్చరికలు")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                    },
                    icon = {
                        Text("👤", modifier = Modifier.semantics { contentDescription = "నా వివరాల ట్యాబ్" })
                    },
                    label = {
                        Text("నా వివరాలు")
                    }
                )
            }
        }

    ) { paddingValues ->
        // Phase 9.7: centered maximum content width improves tablet readability.
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(durationMillis = 220)) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 160))
                    },
                    label = "BottomNavigationScreenTransition"
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            farmerCrops = farmerCrops,
                            onWeatherDataChanged = { currentWeatherData = it },
                            onCropsClick = { selectedTab = 1 },
                            onAlertsClick = { selectedTab = 2 },
                            onProfileClick = { selectedTab = 3 },
                            openScanner = openScannerFromProfile,
                            onScannerOpened = { openScannerFromProfile = false }
                        )
                        1 -> CropsScreen(
                            modifier = Modifier.fillMaxSize(),
                            weatherData = currentWeatherData
                        )
                        2 -> AlertsScreen(
                            alerts = if (currentWeatherData != null) {
                                generateSmartAlerts(
                                    weatherData = currentWeatherData!!,
                                    farmerCrops = farmerCrops
                                )
                            } else emptyList(),
                            weatherAvailable = currentWeatherData != null,
                            modifier = Modifier.fillMaxSize()
                        )
                        3 -> ProfileScreen(
                            modifier = Modifier.fillMaxSize(),
                            onCropsClick = { selectedTab = 1 },
                            onAiScanClick = {
                                openScannerFromProfile = true
                                selectedTab = 0
                            }
                        )
                    }
                }
            }
        }
    }
}


// =========================================================
// HOME SCREEN
// =========================================================

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    farmerCrops: List<Crop> = emptyList(),
    onWeatherDataChanged: (WeatherData?) -> Unit,
    onCropsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit,
    openScanner: Boolean = false,
    onScannerOpened: () -> Unit = {}
) {

    var selectedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var showScanner by remember {
        mutableStateOf(false)
    }

    var showScanHistory by remember {
        mutableStateOf(false)
    }

    // Phase 8.6: latest AI scan summary from Firestore
    var latestDashboardScan by remember {
        mutableStateOf<com.Afnan.rythumitraapp.data.ScanResult?>(null)
    }
    var dashboardScansLoading by remember {
        mutableStateOf(true)
    }
    var dashboardScansError by remember {
        mutableStateOf(false)
    }
    var dashboardScanLoadAttempt by remember {
        mutableIntStateOf(0)
    }
    val dashboardScanRepository = remember {
        com.Afnan.rythumitraapp.data.ScanRepository()
    }

    LaunchedEffect(dashboardScanLoadAttempt) {
        dashboardScansLoading = true
        dashboardScansError = false
        dashboardScanRepository.getScans(
            onSuccess = { scans ->
                latestDashboardScan = scans.maxByOrNull { it.createdAt }
                dashboardScansLoading = false
                dashboardScansError = false
            },
            onFailure = { exception ->
                dashboardScansLoading = false
                dashboardScansError = true
                android.util.Log.e(
                    "DashboardScanSummary",
                    "Failed to load latest AI scan",
                    exception
                )
            }
        )
    }

    fun getDashboardScanDetail(result: String, keyword: String): String {
        val matchingLine = result.lineSequence().firstOrNull {
            it.contains(keyword, ignoreCase = true)
        }
        return matchingLine
            ?.substringAfter(":", missingDelimiterValue = matchingLine)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "వివరాలు అందుబాటులో లేవు"
    }

    var showCameraPermissionMessage by remember {
        mutableStateOf(false)
    }

    val context =
        androidx.compose.ui.platform.LocalContext.current
    // =====================================================
    // DASHBOARD FARMER PROFILE
    // DISEASE SCANNER CARD
    // =====================================================

    val dashboardProfilePreferences = remember {
        context.getSharedPreferences(
            "rythu_mitra_profile",
            android.content.Context.MODE_PRIVATE
        )
    }

    val dashboardFarmerName =
        dashboardProfilePreferences.getString("farmer_name", "") ?: ""

    val dashboardVillage =
        dashboardProfilePreferences.getString("village", "") ?: ""

    val dashboardMainCrop =
        dashboardProfilePreferences.getString("main_crop", "") ?: ""

    val dashboardLandSize =
        dashboardProfilePreferences.getString("land_size", "") ?: ""

    var cameraImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var farmerLocation by remember {
        mutableStateOf<Location?>(null)
    }

    var weatherData by remember {
        mutableStateOf<WeatherData?>(null)
    }
    var weatherLoading by remember {
        mutableStateOf(false)
    }

    var weatherError by remember {
        mutableStateOf(false)
    }
    var weatherRetryAttempt by remember {
        mutableIntStateOf(0)
    }

    val dashboardAlerts =
        if (weatherData != null) {
            generateSmartAlerts(
                weatherData = weatherData!!,
                farmerCrops = farmerCrops
            )
        } else {
            emptyList()
        }

    var locationMessage by remember {
        mutableStateOf("📍 మీ ప్రదేశాన్ని గుర్తిస్తోంది...")
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            val uri = cameraImageUri

            if (success && uri != null) {

                try {

                    val inputStream =
                        context.contentResolver.openInputStream(uri)

                    val bitmap =
                        BitmapFactory.decodeStream(inputStream)

                    inputStream?.close()

                    if (bitmap != null) {
                        selectedBitmap = bitmap
                        showScanner = true
                    }

                } catch (e: Exception) {
                    android.util.Log.e(
                        "RythuMitraWeather",
                        "Weather request failed",
                        e
                    )

                    null

                } finally {

                    context.contentResolver.delete(
                        uri,
                        null,
                        null
                    )

                    cameraImageUri = null
                }

            } else {

                if (uri != null) {
                    context.contentResolver.delete(
                        uri,
                        null,
                        null
                    )
                }

                cameraImageUri = null
            }
        }

    fun openCamera() {

        val values = ContentValues().apply {

            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "rythu_mitra_${System.currentTimeMillis()}.jpg"
            )

            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/jpeg"
            )
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        if (uri != null) {

            cameraImageUri = uri
            cameraLauncher.launch(uri)

        } else {

            showCameraPermissionMessage = true
        }
    }

    LaunchedEffect(openScanner) {
        if (openScanner) {
            openCamera()
            onScannerOpened()
        }
    }
    // =====================================================
// LOCATION PERMISSION
// =====================================================

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {

                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context)

                try {

                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).addOnSuccessListener { location ->

                        if (location != null) {

                            farmerLocation = location
                            locationMessage =
                                "📍 మీ ప్రదేశం గుర్తించబడింది"

                        } else {

                            locationMessage =
                                "📍 ప్రదేశం గుర్తించబడలేదు."
                        }

                    }.addOnFailureListener {

                        locationMessage =
                            "📍 ప్రదేశాన్ని పొందడంలో సమస్య వచ్చింది."
                    }

                } catch (_: SecurityException) {

                    locationMessage =
                        "📍 ప్రదేశ అనుమతి అవసరం."
                }

            } else {

                locationMessage =
                    "📍 స్థానిక వాతావరణం కోసం ప్రదేశ అనుమతి అవసరం."
            }
        }

// =====================================================
// GET FARMER LOCATION
// =====================================================

    LaunchedEffect(Unit) {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {

            locationMessage =
                "📍 వాతావరణం కోసం ప్రదేశ అనుమతి అవసరం."

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        } else {

            locationMessage =
                "📍 మీ ప్రదేశాన్ని గుర్తిస్తోంది..."

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context)

            try {

                fusedLocationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    .addOnSuccessListener { location ->

                        if (location != null) {

                            farmerLocation = location

                            locationMessage =
                                "📍 మీ ప్రదేశం గుర్తించబడింది"

                        } else {

                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { lastLocation ->

                                    if (lastLocation != null) {

                                        farmerLocation =
                                            lastLocation

                                        locationMessage =
                                            "📍 మీ ప్రదేశం గుర్తించబడింది"

                                    } else {

                                        locationMessage =
                                            "📍 GPS ప్రదేశం ఇంకా అందుబాటులో లేదు."
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { error ->

                        android.util.Log.e(
                            "RythuMitraLocation",
                            "Location failed",
                            error
                        )

                        locationMessage =
                            "📍 ప్రదేశాన్ని పొందలేకపోయాము."
                    }

            } catch (_: SecurityException) {

                locationMessage =
                    "📍 ప్రదేశ అనుమతి అవసరం."
            }
        }
    }

    // =====================================================
    // FETCH LIVE WEATHER
    // =====================================================

    LaunchedEffect(farmerLocation, weatherRetryAttempt) {

        val location = farmerLocation

        if (location != null) {

            weatherLoading = true
            weatherError = false

            android.util.Log.d(
                "RythuMitraWeather",
                "Location received: $location"
            )

            val result =
                fetchWeather(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

            android.util.Log.d(
                "RythuMitraWeather",
                "Weather result: $result"
            )

            if (result != null) {

                weatherData = result
                weatherError = false

                onWeatherDataChanged(result)

            } else {

                weatherError = true
            }

            weatherLoading = false
        }
    }

    // =====================================================
    // GALLERY
    // =====================================================

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                try {

                    val inputStream =
                        context.contentResolver
                            .openInputStream(uri)

                    val bitmap =
                        BitmapFactory.decodeStream(inputStream)

                    inputStream?.close()

                    selectedBitmap = bitmap
                    showScanner = true

                } catch (_: Exception) {

                    selectedBitmap = null
                }
            }
        }


    // =====================================================
    // CAMERA PERMISSION
    // =====================================================

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                openCamera()

            } else {

                showCameraPermissionMessage = true
            }
        }
    // =====================================================
    // SCANNER
    // =====================================================

    if (showScanner) {

        ScannerScreen(

            bitmap = selectedBitmap,

            onBack = {

                showScanner = false
                selectedBitmap = null
            },

            onCamera = {

                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    openCamera()

                } else {

                    cameraPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            },

            onGallery = {

                galleryLauncher.launch("image/*")
            }
        )


        if (showCameraPermissionMessage) {

            AlertDialog(

                onDismissRequest = {
                    showCameraPermissionMessage = false
                },

                title = {
                    Text("కెమెరా అనుమతి")
                },

                text = {
                    Text(
                        "పంట ఫోటో తీయడానికి కెమెరా అనుమతి అవసరం."
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            showCameraPermissionMessage = false
                        }
                    ) {
                        Text("సరే")
                    }
                }
            )
        }

        return
    }
// AI RESULT HELPERS
// =====================================================
// SCAN HISTORY
// =====================================================

    if (showScanHistory) {
        ScanHistoryScreen(
            onBack = {
                showScanHistory = false
            }
        )

        return
    }

    // =====================================================
    // HOME UI
    // =====================================================

    val dashboardListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val dashboardCoroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = dashboardListState,
        modifier = modifier
            .fillMaxSize()
            .background(
                Color(0xFFF4F8F1)
            )
            .padding(16.dp)
    ) {

        item {

            Text(
                text = "రైతు మిత్ర 🌾",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF236B35)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "మీ వ్యవసాయానికి మీ స్నేహితుడు",
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
        // =================================================
        // FARMER PROFILE SUMMARY CARD
        // =================================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "👨‍🌾 నా వ్యవసాయ ప్రొఫైల్",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "👤 రైతు పేరు: ${
                            dashboardFarmerName.ifBlank {
                                "వివరాలు నమోదు చేయలేదు"
                            }
                        }",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF303B30)
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "📍 గ్రామం: ${
                            dashboardVillage.ifBlank {
                                "వివరాలు నమోదు చేయలేదు"
                            }
                        }",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF303B30)
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "🌱 ప్రధాన పంట: ${
                            dashboardMainCrop.ifBlank {
                                "వివరాలు నమోదు చేయలేదు"
                            }
                        }",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF303B30)
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "📏 పొలం విస్తీర్ణం: ${
                            dashboardLandSize.ifBlank {
                                "వివరాలు నమోదు చేయలేదు"
                            }
                        }",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF303B30)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }


        // =================================================
        // WEATHER
        // =================================================

        item {

            WeatherCard(
                weatherData = weatherData,
                weatherLoading = weatherLoading,
                weatherError = weatherError,
                onRetry = {
                    if (farmerLocation != null) {
                        weatherRetryAttempt++
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }


        // =================================================
        // CROP SUMMARY CARD
        // =================================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Text(
                        text = "🌾 నా పంటలు",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "మీరు నమోదు చేసిన పంటల వివరాలు",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (farmerCrops.isEmpty()) {

                        Text(
                            text = "ఇంకా పంటలను నమోదు చేయలేదు.",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "మీ పంటలను జోడించడానికి 'నా పంటలు' విభాగానికి వెళ్లండి.",
                            fontSize = 14.sp
                        )

                    } else {

                        farmerCrops.forEach { crop ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF1F8E9)
                                )
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {

                                    Text(
                                        text = "🌱 ${crop.name.ifBlank { "పేరు నమోదు కాలేదు" }}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF236B35)
                                    )

                                    Spacer(
                                        modifier = Modifier.height(5.dp)
                                    )

                                    Text(
                                        text = "పంట రకం: ${
                                            crop.cropType.ifBlank {
                                                "వివరాలు లేవు"
                                            }
                                        }",
                                        fontSize = 14.sp
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(
                                        text = "ప్రస్తుత దశ: ${
                                            crop.growthStage.ifBlank {
                                                "నమోదు చేయలేదు"
                                            }
                                        }",
                                        fontSize = 14.sp
                                    )

                                    if (crop.plantedDate.isNotBlank()) {

                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )

                                        Text(
                                            text = "నాటిన తేదీ: ${crop.plantedDate}",
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onCropsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🌿 అన్ని పంటలను చూడండి")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // =================================================
        // SMART FARMER ALERTS CARD
        // =================================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Text(
                        text = "🔔 ముఖ్యమైన రైతు హెచ్చరికలు",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (weatherData == null) {

                        Text(
                            text = "⏳ వాతావరణ సమాచారం వచ్చిన తర్వాత హెచ్చరికలు ఇక్కడ కనిపిస్తాయి.",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )

                    } else if (dashboardAlerts.isEmpty()) {

                        Text(
                            text = "✅ ప్రస్తుతం ముఖ్యమైన హెచ్చరికలు లేవు.",
                            fontSize = 15.sp,
                            color = Color(0xFF236B35)
                        )

                    } else {

                        Text(
                            text = "ప్రస్తుతం ${dashboardAlerts.size} హెచ్చరికలు ఉన్నాయి",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        dashboardAlerts.take(2).forEach { alert ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        if (alert.priority == 1) {
                                            Color(0xFFFFF3E0)
                                        } else {
                                            Color(0xFFF1F8E9)
                                        }
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {

                                    Text(
                                        text = alert.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF236B35)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = alert.message,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )

                                    if (alert.priority == 1) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🔴 అత్యవసర హెచ్చరిక",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onAlertsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("అన్ని హెచ్చరికలను చూడండి →")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // =================================================
        // AI SCAN SUMMARY CARD (PHASE 8.6)
        // =================================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "🧪 తాజా పంట పరీక్ష",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    when {
                        dashboardScansLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        dashboardScansError -> {
                            Column {
                                Text(
                                    text = "పంట పరీక్షల వివరాలను పొందలేకపోయాం. ఇంటర్నెట్‌ను తనిఖీ చేసి మళ్లీ ప్రయత్నించండి.",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { dashboardScanLoadAttempt++ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("మళ్లీ ప్రయత్నించండి")
                                }
                            }
                        }

                        latestDashboardScan == null -> {
                            Text(
                                text = "ఇంకా పంట పరీక్షలు లేవు. మొదటిసారి మీ పంటను పరీక్షించిన తర్వాత వివరాలు ఇక్కడ కనిపిస్తాయి.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                lineHeight = 21.sp
                            )
                        }

                        else -> {
                            val latestScan = latestDashboardScan!!
                            val scanResultText = latestScan.result

                            Text(
                                text = "పంట: ${latestScan.cropName.ifBlank { "తెలియదు" }}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💚 ఆరోగ్య స్థితి: ${getDashboardScanDetail(scanResultText, "ఆరోగ్య స్థితి")}",
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🦠 సమస్య / వ్యాధి: ${getDashboardScanDetail(scanResultText, "సమస్య / వ్యాధి")}",
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ తీవ్రత: ${getDashboardScanDetail(scanResultText, "తీవ్రత")}",
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "తాజా పరీక్ష: ${android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", java.util.Date(latestScan.createdAt))}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showScanHistory = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("పరీక్షల పూర్తి చరిత్ర చూడండి →")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // =================================================
        // QUICK ACTIONS (PHASE 8.7)
        // =================================================

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "⚡ త్వరిత సేవలు",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    openCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📷 పంట పరీక్ష", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onCropsClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🌱 నా పంటలు", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                dashboardCoroutineScope.launch {
                                    dashboardListState.animateScrollToItem(2)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("☀️ వాతావరణం", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onProfileClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("👤 నా ప్రొఫైల్", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onAlertsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔔 హెచ్చరికలు చూడండి")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // =================================================
        // DISEASE SCANNER CARD
        // =================================================

        item {

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📷",
                        fontSize = 60.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "పంటను స్కాన్ చేయండి",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "పంట ఆకు ఫోటో తీసి వ్యాధి లక్షణాలను తెలుసుకోండి",
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Button(

                        onClick = {

                            if (
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {

                                openCamera()

                            } else {

                                cameraPermissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "📷 కెమెరాతో స్కాన్ చేయండి",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedButton(

                        onClick = {
                            galleryLauncher.launch("image/*")
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "🖼️ ఫోన్‌లో ఉన్న ఫోటో ఎంచుకోండి",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }


        // =================================================
        // SERVICES TITLE
        // =================================================

        item {

            Text(
                text = "ముఖ్యమైన సేవలు",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }


        // =================================================
        // SERVICES ROW 1
        // =================================================

        item {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                ServiceCard(
                    emoji = "🌱",
                    title = "నా పంటలు",
                    modifier = Modifier.weight(1f),
                    onClick = onCropsClick
                )

                ServiceCard(
                    emoji = "☀️",
                    title = "వాతావరణం",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }


        // =================================================
        // SERVICES ROW 2
        // =================================================

        item {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                ServiceCard(
                    emoji = "💧",
                    title = "నీటి సలహా",
                    modifier = Modifier.weight(1f)
                )

                ServiceCard(
                    emoji = "🔔",
                    title = "సమాచారం",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
        // =================================================
        // AI SCAN HISTORY
        // =================================================

        item {

            ServiceCard(
                emoji = "📋",
                title = "పంట పరీక్షల చరిత్ర",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showScanHistory = true
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }

        // =================================================
        // ADVICE
        // =================================================

        item {

            Text(
                text = "ఈరోజు రైతు సలహా",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            AdviceCard()
        }
    }
}




// =========================================================
// AI RESULT HELPERS
// =========================================================

private fun parseAiResult(result: String): List<Pair<String, String>> {

    val sections = listOf(
        "🌱 పంట:",
        "💚 ఆరోగ్య స్థితి:",
        "🦠 సమస్య / వ్యాధి:",
        "⚠️ తీవ్రత:",
        "🔍 కనిపించే లక్షణాలు:",
        "💡 రైతుకు సలహా:",
        "🛡️ నివారణ:"
    )

    val parsed = mutableListOf<Pair<String, String>>()
    var currentTitle: String? = null
    val currentText = StringBuilder()

    fun saveCurrent() {
        val title = currentTitle
        val value = currentText.toString().trim()

        if (!title.isNullOrBlank() && value.isNotBlank()) {
            parsed.add(title to value)
        }

        currentText.clear()
    }

    result
        .replace("**", "")
        .replace("###", "")
        .lines()
        .forEach { rawLine ->

            val line = rawLine.trim()
            val matchedSection = sections.firstOrNull { section ->
                line.startsWith(section)
            }

            if (matchedSection != null) {
                saveCurrent()
                currentTitle = matchedSection.removeSuffix(":")
                currentText.append(
                    line.removePrefix(matchedSection).trim()
                )
            } else if (currentTitle != null && line.isNotBlank()) {
                if (currentText.isNotEmpty()) {
                    currentText.append("\n")
                }
                currentText.append(line)
            }
        }

    saveCurrent()
    return parsed
}

@Composable
private fun AiResultCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF236B35)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = value,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }

    Spacer(
        modifier = Modifier.height(10.dp)
    )
}

// =========================================================

private fun isImageQualityAcceptable(
    bitmap: Bitmap
): Boolean {

    // Very small images are not suitable for AI analysis
    if (bitmap.width < 400 || bitmap.height < 400) {
        return false
    }

    val sampleSize = 12

    var brightnessSum = 0.0
    var brightnessSquaredSum = 0.0

    var edgeDifferenceSum = 0.0

    var sampleCount = 0
    var edgeCount = 0

    var previousBrightness: Double? = null

    for (y in 0 until bitmap.height step sampleSize) {

        for (x in 0 until bitmap.width step sampleSize) {

            val pixel = bitmap.getPixel(x, y)

            val red = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)

            val brightness =
                (0.299 * red) +
                        (0.587 * green) +
                        (0.114 * blue)

            brightnessSum += brightness
            brightnessSquaredSum += brightness * brightness

            // Detect visible edges/details.
            // A very blurry or uniform image usually has
            // very little brightness change between nearby samples.
            if (previousBrightness != null) {

                edgeDifferenceSum +=
                    kotlin.math.abs(
                        brightness - previousBrightness
                    )

                edgeCount++
            }

            previousBrightness = brightness

            sampleCount++
        }
    }

    if (sampleCount == 0 || edgeCount == 0) {
        return false
    }

    val averageBrightness =
        brightnessSum / sampleCount

    val variance =
        (brightnessSquaredSum / sampleCount) -
                (averageBrightness * averageBrightness)

    val averageEdgeDifference =
        edgeDifferenceSum / edgeCount

    // Extremely dark image
    if (averageBrightness < 40) {
        return false
    }

    // Extremely bright / washed-out image
    if (averageBrightness > 235) {
        return false
    }

    // Very little visual information
    if (variance < 150) {
        return false
    }

    // Very blurry / almost uniform image
    if (averageEdgeDifference < 6) {
        return false
    }

    return true
}
// SCANNER SCREEN
// =========================================================

@Composable
fun ScannerScreen(

    bitmap: Bitmap?,

    onBack: () -> Unit,

    onCamera: () -> Unit,

    onGallery: () -> Unit

) {

    var isAnalyzing by remember {
        mutableStateOf(false)
    }

    var aiResult by remember {
        mutableStateOf<String?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }


    val coroutineScope =
        rememberCoroutineScope()


    // =====================================================
    // FIREBASE AI MODEL
    // =====================================================

// Optimized Gemini model for crop diagnosis
    val model = remember {

        val config = generationConfig {
            thinkingConfig = thinkingConfig {
                thinkingLevel = ThinkingLevel.LOW
            }
        }

        Firebase
            .ai(
                backend = GenerativeBackend.googleAI()
            )
            .generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = config
            )
    }
    val scanRepository = remember {
        com.Afnan.rythumitraapp.data.ScanRepository()
    }


    // =====================================================
    // ANALYZE CROP
    // =====================================================

    fun analyzeCrop() {

        if (bitmap == null) {

            errorMessage =
                "ముందుగా పంట ఫోటోను ఎంచుకోండి."

            return
        }

        if (!isImageQualityAcceptable(bitmap)) {

            errorMessage =
                "📷 ఫోటో స్పష్టంగా లేదు.\n\n" +
                        "దయచేసి ఆకు దగ్గరగా, మంచి వెలుతురులో " +
                        "మరొక ఫోటో తీయండి."

            return
        }

        if (isAnalyzing) {
            return
        }

        isAnalyzing = true
        aiResult = null
        errorMessage = null


        coroutineScope.launch {

            try {

                val prompt = content {

                    image(bitmap)

                    text(
                        """
                        You are an agricultural crop disease
                        identification assistant helping
                        Telugu-speaking farmers.

                        Carefully analyze the crop leaf image.

                        Identify the following if possible:

                        1. Crop name
                        2. Whether the crop looks healthy
                        3. Possible disease or problem
                        4. Visible symptoms
                        5. What the farmer should do now
                        6. General prevention advice

                        Important rules:

                        - Use only what can reasonably be seen
                          in the image.
                        - Do not invent a disease.
                        - If the image is unclear, say that it
                          is unclear.
                        - If you cannot confidently identify
                          the disease, say:
                          "ఖచ్చితంగా గుర్తించలేకపోయాను"
                        - Give the final answer in very simple
                          Telugu.
                        - Avoid dangerous or overly specific
                          pesticide instructions.
                        - If appropriate, recommend consulting
                          a local agriculture expert.

                        Use exactly these sections:

                        🌱 పంట:
                        💚 ఆరోగ్య స్థితి:
                        🦠 సమస్య / వ్యాధి:
                        ⚠️ తీవ్రత:
                        🔍 కనిపించే లక్షణాలు:
                        💡 రైతుకు సలహా:
                        🛡️ నివారణ:

                        If the image is unclear, say so clearly
                        and do not guess the crop or disease.
                        For severity, use only: తక్కువ, మధ్యస్థ, ఎక్కువ,
                        or గుర్తించలేకపోయాను.

                        Keep each section concise and use very
                        simple Telugu suitable for a farmer with
                        limited literacy.
                        """.trimIndent()
                    )
                }


                val response =
                    model.generateContent(prompt)


                val result =
                    response.text


                if (!result.isNullOrBlank()) {

                    val cleanResult = result.trim()

                    aiResult = cleanResult

                    val cropName = cleanResult
                        .lines()
                        .firstOrNull { it.contains("🌱 పంట:") }
                        ?.substringAfter(":")
                        ?.trim()
                        ?.removePrefix("*")
                        ?.removeSuffix("*")
                        ?.ifBlank { "తెలియదు" }
                        ?: "తెలియదు"

                    val scan = com.Afnan.rythumitraapp.data.ScanResult(
                        cropName = cropName,
                        result = cleanResult
                    )

                    scanRepository.addScan(
                        scan = scan,
                        onSuccess = {
                            android.util.Log.d(
                                "ScanHistory",
                                "Scan saved successfully"
                            )
                        },
                        onFailure = { exception ->
                            android.util.Log.e(
                                "ScanHistory",
                                "Failed to save scan",
                                exception
                            )
                        }
                    )

                } else {
                    errorMessage = "AI నుండి ఫలితం రాలేదు. దయచేసి మళ్లీ ప్రయత్నించండి."
                }

            } catch (e: Exception) {

                errorMessage =
                    "వ్యాధి గుర్తించడంలో సమస్య వచ్చింది.\n\n" +
                            (e.message
                                ?: "తెలియని సమస్య")

            } finally {

                isAnalyzing = false
            }
        }
    }


    // =====================================================
    // SCREEN
    // =====================================================

    Surface(

        modifier = Modifier.fillMaxSize(),

        color = Color(0xFFF4F8F1)
    ) {

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // =================================================
            // HEADER
            // =================================================

            item {

                TextButton(
                    onClick = onBack,
                    enabled = !isAnalyzing
                ) {

                    Text(
                        text = "← వెనక్కి",
                        fontSize = 17.sp
                    )
                }

                Text(
                    text = "పంట వ్యాధి పరీక్ష 🔍",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF236B35)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }


            // =================================================
            // IMAGE EXISTS
            // =================================================

            if (bitmap != null) {

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),

                        shape = RoundedCornerShape(22.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Image(

                            bitmap =
                                bitmap.asImageBitmap(),

                            contentDescription =
                                "పంట ఫోటో",

                            modifier =
                                Modifier.fillMaxSize(),

                            contentScale =
                                ContentScale.Crop
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }


                // =================================================
                // ANALYZE BUTTON
                // =================================================

                item {

                    Button(

                        onClick = {
                            analyzeCrop()
                        },

                        enabled = !isAnalyzing,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        if (isAnalyzing) {

                            CircularProgressIndicator(

                                modifier = Modifier
                                    .width(22.dp)
                                    .height(22.dp),

                                color = Color.White,

                                strokeWidth = 2.dp
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(10.dp)
                            )

                            Text(
                                text = "పరిశీలిస్తోంది...",
                                fontSize = 18.sp
                            )

                        } else {

                            Text(
                                text =
                                    "🔍 వ్యాధిని గుర్తించండి",
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }


                // =================================================
                // RETAKE BUTTON
                // =================================================

                item {

                    OutlinedButton(

                        onClick = onCamera,

                        enabled = !isAnalyzing,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "📷 మళ్లీ ఫోటో తీయండి",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    OutlinedButton(

                        onClick = onGallery,

                        enabled = !isAnalyzing,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "🖼️ వేరే ఫోటో ఎంచుకోండి",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )
                }


                // =================================================
                // ANALYZING
                // =================================================

                if (isAnalyzing) {

                    item {

                        Card(

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(18.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )
                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                CircularProgressIndicator()

                                Spacer(
                                    modifier =
                                        Modifier.height(12.dp)
                                )

                                Text(
                                    text =
                                        "పంట ఆకును AI పరిశీలిస్తోంది...",
                                    fontSize = 17.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(
                                    text =
                                        "దయచేసి కొద్దిసేపు వేచి ఉండండి.",
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )
                    }
                }


                // =================================================
                // ERROR
                // =================================================

                if (errorMessage != null) {

                    item {

                        Card(

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(18.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFFFFEBEE)
                                )
                        ) {

                            Column(

                                modifier =
                                    Modifier.padding(18.dp)
                            ) {

                                Text(
                                    text = "⚠️ సమస్య",
                                    fontSize = 20.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(
                                    text =
                                        errorMessage ?: "",
                                    fontSize = 15.sp
                                )

                                if (!isAnalyzing && bitmap != null && aiResult == null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = { analyzeCrop() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("🔄 మళ్లీ ప్రయత్నించండి")
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )
                    }
                }


                // =================================================
                // AI RESULT
                // =================================================

                if (aiResult != null) {

                    item {

                        Text(
                            text = "🤖 AI పంట పరీక్ష ఫలితం",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF236B35)
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }

                    val parsedSections =
                        parseAiResult(aiResult ?: "")

                    if (parsedSections.isNotEmpty()) {

                        parsedSections.forEach { (title, value) ->

                            item {
                                AiResultCard(
                                    title = title,
                                    value = value
                                )
                            }
                        }

                    } else {

                        item {

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {

                                    Text(
                                        text = aiResult ?: "",
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "⚠️ ఇది AI ఆధారిత ప్రాథమిక అంచనా మాత్రమే. " +
                                        "ఖచ్చితమైన నిర్ధారణ కోసం స్థానిక వ్యవసాయ నిపుణుడిని సంప్రదించండి.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )
                    }
                }


            } else {

                // =================================================
                // NO IMAGE
                // =================================================

                item {

                    Card(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(280.dp),

                        shape =
                            RoundedCornerShape(22.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column(

                            modifier =
                                Modifier.fillMaxSize(),

                            horizontalAlignment =
                                Alignment.CenterHorizontally,

                            verticalArrangement =
                                Arrangement.Center
                        ) {

                            Text(
                                text = "🌿",
                                fontSize = 70.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text =
                                    "పంట ఆకుకు ఫోటో తీయండి",
                                fontSize = 21.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "వ్యాధిని గుర్తించడానికి ఫోటో అవసరం.",
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )
                }


                // =================================================
                // CAMERA
                // =================================================

                item {

                    Button(

                        onClick = onCamera,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "📷 కెమెరా తెరవండి",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    // =================================================
                    // GALLERY
                    // =================================================

                    OutlinedButton(

                        onClick = onGallery,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "🖼️ గ్యాలరీ నుండి ఫోటో",
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// =========================================================
// WEATHER DATA
// =========================================================

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val rain: Double,
    val windSpeed: Double,
    val tomorrowMinTemperature: Double,
    val tomorrowMaxTemperature: Double,
    val tomorrowRainProbability: Int,
    val tomorrowWeatherCode: Int,
    val weatherCode: Int,
    val rainProbability: Int
)

fun getWeatherDescription(weatherCode: Int): String {

    return when (weatherCode) {

        0 -> "ఆకాశం నిర్మలంగా ఉంది"

        1 -> "ఆకాశం ఎక్కువగా నిర్మలంగా ఉంది"

        2 -> "ఆకాశం కొంత మేఘావృతంగా ఉంది"

        3 -> "ఆకాశం పూర్తిగా మేఘావృతంగా ఉంది"

        45, 48 -> "పొగమంచు ఉంది"

        51, 53, 55 -> "చినుకులు పడుతున్నాయి"

        56, 57 -> "చల్లటి చినుకులు పడుతున్నాయి"

        61, 63, 65 -> "వర్షం పడుతోంది"

        66, 67 -> "చల్లటి వర్షం పడుతోంది"

        71, 73, 75, 77 -> "మంచు పడుతోంది"

        80, 81, 82 -> "వర్షపు జల్లులు పడుతున్నాయి"

        85, 86 -> "మంచు జల్లులు పడుతున్నాయి"

        95, 96, 99 -> "ఉరుములతో కూడిన వర్షం ఉంది"

        else -> "వాతావరణ సమాచారం అందుబాటులో ఉంది"
    }
}
fun getWeatherIcon(weatherCode: Int): String {

    return when (weatherCode) {

        0 -> "☀️"

        1 -> "🌤️"

        2 -> "⛅"

        3 -> "☁️"

        45, 48 -> "🌫️"

        51, 53, 55,
        56, 57 -> "🌦️"

        61, 63, 65,
        66, 67 -> "🌧️"

        71, 73, 75, 77,
        85, 86 -> "❄️"

        80, 81, 82 -> "🌦️"

        95, 96, 99 -> "⛈️"

        else -> "🌤️"
    }
}
fun getFarmerWeatherAdvice(weatherData: WeatherData): String {

    return when {

        weatherData.rain > 2.0 ->
            "🌾 రైతు సూచన: వర్షం పడుతోంది. మందులు లేదా ఎరువులు పిచికారీ చేయడం వాయిదా వేయండి."

        weatherData.humidity >= 85 ->
            "🌾 రైతు సూచన: తేమ ఎక్కువగా ఉంది. పంటలో ఫంగస్ వ్యాధులపై జాగ్రత్తగా గమనించండి."

        weatherData.windSpeed >= 25.0 ->
            "🌾 రైతు సూచన: గాలి వేగంగా ఉంది. పిచికారీ చేయడం కొంతసేపు వాయిదా వేయండి."

        weatherData.temperature >= 38.0 ->
            "🌾 రైతు సూచన: ఉష్ణోగ్రత ఎక్కువగా ఉంది. పంటకు తగినంత నీరు అందేలా చూడండి."

        weatherData.temperature <= 15.0 ->
            "🌾 రైతు సూచన: ఉష్ణోగ్రత తక్కువగా ఉంది. చలి ప్రభావం నుంచి పంటను కాపాడండి."

        else ->
            "🌾 రైతు సూచన: వాతావరణ పరిస్థితులు సాధారణంగా ఉన్నాయి. పంటను సాధారణంగా గమనిస్తూ ఉండండి."
    }
    fun getTomorrowFarmerWeatherAdvice(
        weatherData: WeatherData
    ): String {

        return when {

            weatherData.tomorrowRainProbability >= 70 ->
                "🌾 రేపటి రైతు సూచన: వర్షం అవకాశం ఎక్కువగా ఉంది. పిచికారీ పనులను వాయిదా వేయడం మంచిది."

            weatherData.tomorrowRainProbability >= 40 ->
                "🌾 రేపటి రైతు సూచన: వర్షం వచ్చే అవకాశం ఉంది. పిచికారీ చేసే ముందు వాతావరణాన్ని గమనించండి."

            weatherData.tomorrowMaxTemperature >= 38.0 ->
                "🌾 రేపటి రైతు సూచన: ఉష్ణోగ్రత ఎక్కువగా ఉండే అవకాశం ఉంది. పంటకు తగినంత నీరు అందేలా చూడండి."

            weatherData.tomorrowMinTemperature <= 15.0 ->
                "🌾 రేపటి రైతు సూచన: ఉష్ణోగ్రత తక్కువగా ఉండే అవకాశం ఉంది. చలి ప్రభావం నుంచి పంటను కాపాడండి."

            weatherData.tomorrowWeatherCode in listOf(95, 96, 99) ->
                "🌾 రేపటి రైతు సూచన: ఉరుములతో కూడిన వర్షం అవకాశం ఉంది. పొలంలో జాగ్రత్తగా ఉండండి."

            else ->
                "🌾 రేపటి రైతు సూచన: వాతావరణ పరిస్థితులు సాధారణంగా ఉండే అవకాశం ఉంది. పంటను గమనిస్తూ ఉండండి."
        }
    }

    fun getWeatherSafetyAlert(
        weatherData: WeatherData
    ): String? {

        return when {

            weatherData.weatherCode in listOf(95, 96, 99) ->
                "⛈️ వాతావరణ హెచ్చరిక: ఉరుములతో కూడిన వర్షం ఉంది. పొలంలో జాగ్రత్తగా ఉండండి."

            weatherData.rainProbability >= 70 ->
                "🌧️ వాతావరణ హెచ్చరిక: వర్షం అవకాశం ఎక్కువగా ఉంది. పిచికారీ పనులను వాయిదా వేయండి."

            weatherData.windSpeed >= 25.0 ->
                "💨 వాతావరణ హెచ్చరిక: గాలి వేగం ఎక్కువగా ఉంది. పిచికారీ చేయడం వాయిదా వేయండి."

            weatherData.temperature >= 40.0 ->
                "🌡️ వాతావరణ హెచ్చరిక: ఉష్ణోగ్రత చాలా ఎక్కువగా ఉంది. పంటకు తగినంత నీరు అందేలా చూడండి."

            weatherData.tomorrowWeatherCode in listOf(95, 96, 99) ->
                "⛈️ రేపు ఉరుములతో కూడిన వర్షం అవకాశం ఉంది. పొలంలో జాగ్రత్తగా ఉండండి."

            weatherData.tomorrowRainProbability >= 80 ->
                "🌧️ రేపు వర్షం అవకాశం చాలా ఎక్కువగా ఉంది. పిచికారీ పనులను వాయిదా వేయండి."

            else ->
                null
        }
    }
}
suspend fun fetchWeather(
    latitude: Double,
    longitude: Double
): WeatherData? {

    return withContext(Dispatchers.IO) {

        try {

            val url = java.net.URL(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$latitude" +
                        "&longitude=$longitude" +
                        "&current=temperature_2m,relative_humidity_2m,precipitation,rain,wind_speed_10m,weather_code" +
                        "&hourly=precipitation_probability" +
                        "&forecast_hours=1" +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code" +
                        "&forecast_days=2" +
                        "&timezone=auto"
            )

            val connection =
                url.openConnection() as java.net.HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response =
                connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

            connection.disconnect()

            val json =
                org.json.JSONObject(response)

            val current =
                json.getJSONObject("current")

            val hourly =
                json.getJSONObject("hourly")

            val daily =
                json.getJSONObject("daily")

            val rainProbability =
                hourly
                    .getJSONArray("precipitation_probability")
                    .getInt(0)

            val tomorrowMinTemperature =
                daily
                    .getJSONArray("temperature_2m_min")
                    .getDouble(1)

            val tomorrowMaxTemperature =
                daily
                    .getJSONArray("temperature_2m_max")
                    .getDouble(1)

            val tomorrowRainProbability =
                daily
                    .getJSONArray("precipitation_probability_max")
                    .getInt(1)

            val tomorrowWeatherCode =
                daily
                    .getJSONArray("weather_code")
                    .getInt(1)

            return@withContext WeatherData(

                temperature =
                    current.getDouble("temperature_2m"),

                humidity =
                    current.getInt("relative_humidity_2m"),

                rain =
                    current.getDouble("rain"),

                windSpeed =
                    current.getDouble("wind_speed_10m"),

                weatherCode =
                    current.getInt("weather_code"),

                rainProbability =
                    rainProbability,

                tomorrowMinTemperature =
                    tomorrowMinTemperature,

                tomorrowMaxTemperature =
                    tomorrowMaxTemperature,

                tomorrowRainProbability =
                    tomorrowRainProbability,

                tomorrowWeatherCode =
                    tomorrowWeatherCode
            )

        } catch (e: Exception) {

            android.util.Log.e(
                "RythuMitraWeather",
                "Weather request failed",
                e
            )

            null
        }
    }
}

// =========================================================
// WEATHER CARD
// =========================================================
@Composable
fun WeatherCard(
    weatherData: WeatherData?,
    weatherLoading: Boolean,
    weatherError: Boolean,
    onRetry: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E7D32)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        weatherLoading -> "⏳"
                        weatherError -> "⚠️"
                        weatherData != null -> getWeatherIcon(weatherData.weatherCode)
                        else -> "🌤️"
                    },
                    fontSize = 42.sp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ఈరోజు వాతావరణం",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when {
                            weatherLoading -> "--°C"
                            weatherError -> "--°C"
                            weatherData != null -> "${weatherData.temperature.toInt()}°C"
                            else -> "--°C"
                        },
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp
                    )
                    if (weatherLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "వాతావరణ సమాచారం పొందుతోంది...",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Text(
                            text = when {
                                weatherError -> "వాతావరణ సమాచారం అందుబాటులో లేదు"
                                weatherData != null -> getWeatherDescription(weatherData.weatherCode)
                                else -> "వాతావరణ సమాచారం కోసం వేచి ఉంది..."
                            },
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            if (weatherError) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("🔄 మళ్లీ వాతావరణం పొందండి")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💧", fontSize = 17.sp)
                        Text(if (weatherData != null) "${weatherData.humidity}%" else "--", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("తేమ", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌧️", fontSize = 17.sp)
                        Text(if (weatherData != null) "${weatherData.rain} mm" else "--", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("వర్షం", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💨", fontSize = 17.sp)
                        Text(if (weatherData != null) "${weatherData.windSpeed}" else "--", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("గాలి km/h", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "☔ వర్షం అవకాశం: ${weatherData?.rainProbability?.let { "$it%" } ?: "--"}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            if (weatherData != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.13f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(13.dp)) {
                        Text(
                            text = "🌤️ రేపటి వాతావరణం",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Text(
                            text = "🌡️ ${weatherData.tomorrowMinTemperature.toInt()}°C - ${weatherData.tomorrowMaxTemperature.toInt()}°C",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "☔ వర్షం అవకాశం: ${weatherData.tomorrowRainProbability}%",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = getWeatherDescription(weatherData.tomorrowWeatherCode),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🌾 రైతుకు సూచన",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getFarmerWeatherAdvice(weatherData),
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (
                    weatherData.weatherCode == 95 ||
                    weatherData.weatherCode == 96 ||
                    weatherData.weatherCode == 99 ||
                    weatherData.rainProbability >= 70 ||
                    weatherData.windSpeed >= 25.0 ||
                    weatherData.temperature >= 40.0 ||
                    weatherData.tomorrowWeatherCode == 95 ||
                    weatherData.tomorrowWeatherCode == 96 ||
                    weatherData.tomorrowWeatherCode == 99 ||
                    weatherData.tomorrowRainProbability >= 80
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Text(
                            text = when {
                                weatherData.weatherCode == 95 || weatherData.weatherCode == 96 || weatherData.weatherCode == 99 ->
                                    "⛈️ వాతావరణ హెచ్చరిక: ఉరుములతో కూడిన వర్షం ఉంది. పొలంలో జాగ్రత్తగా ఉండండి."
                                weatherData.rainProbability >= 70 ->
                                    "🌧️ వాతావరణ హెచ్చరిక: వర్షం అవకాశం ఎక్కువగా ఉంది. పిచికారీ పనులను వాయిదా వేయండి."
                                weatherData.windSpeed >= 25.0 ->
                                    "💨 వాతావరణ హెచ్చరిక: గాలి వేగం ఎక్కువగా ఉంది. పిచికారీ చేయడం వాయిదా వేయండి."
                                weatherData.temperature >= 40.0 ->
                                    "🌡️ వాతావరణ హెచ్చరిక: ఉష్ణోగ్రత చాలా ఎక్కువగా ఉంది. పంటకు తగినంత నీరు అందేలా చూడండి."
                                else ->
                                    "⛈️ రేపు తీవ్రమైన వాతావరణం అవకాశం ఉంది. పొలంలో జాగ్రత్తగా ఉండండి."
                            },
                            modifier = Modifier.padding(12.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// =========================================================
// SERVICE CARD
// =========================================================

@Composable
fun ServiceCard(

    emoji: String,

    title: String,

    modifier: Modifier = Modifier,

    onClick: () -> Unit = {}

) {

    Card(

        modifier =
            modifier.height(130.dp),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        onClick = onClick
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),

            verticalArrangement =
                Arrangement.Center,

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = emoji,
                fontSize = 38.sp
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// =========================================================
// ADVICE CARD
// =========================================================

@Composable
fun AdviceCard() {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = "🌱 పంట సంరక్షణ",
                fontSize = 19.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    Color(0xFF236B35)
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "మీ పంటలను ప్రతిరోజూ గమనించండి. నేలలో తేమ సరైన స్థాయిలో ఉండేలా చూసుకోండి.",
                fontSize = 16.sp
            )
        }
    }
}

// =========================================================
// CROPS SCREEN
// =========================================================

private fun getCropIcon(crop: String): String {
    return when {
        crop.contains("వరి") ||
                crop.contains("rice", ignoreCase = true) -> "🌾"

        crop.contains("మొక్కజొన్న") ||
                crop.contains("corn", ignoreCase = true) -> "🌽"

        crop.contains("టమాట") ||
                crop.contains("tomato", ignoreCase = true) -> "🍅"

        crop.contains("పత్తి") ||
                crop.contains("cotton", ignoreCase = true) -> "🌿"

        crop.contains("మిర్చి") ||
                crop.contains("chilli", ignoreCase = true) -> "🌶️"

        else -> "🌱"
    }
}

@Composable
fun CropsScreen(
    modifier: Modifier = Modifier,
    weatherData: WeatherData? = null
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    // ---------------------------------------------------------
    // LOCAL STORAGE
    // ---------------------------------------------------------

    val preferences = remember {
        context.getSharedPreferences(
            "rythu_mitra_crops",
            android.content.Context.MODE_PRIVATE
        )
    }

    val savedCrops = remember {
        preferences.getStringSet(
            "crop_list",
            setOf(
                "🌾 వరి",
                "🌽 మొక్కజొన్న",
                "🍅 టమాటా"
            )
        )?.toList() ?: emptyList()
    }

    // UI continues to use String values
    val crops = remember {
        mutableStateListOf<String>().apply {
            addAll(savedCrops)
        }
    }

    // Firestore repository
    val repository = remember {
        com.Afnan.rythumitraapp.data.CropRepository()
    }

    // Firestore document IDs
    val cropIds = remember {
        androidx.compose.runtime.mutableStateMapOf<String, String>()
    }

    // Complete crop details
    val cropDetails = remember {
        androidx.compose.runtime.mutableStateMapOf<String, com.Afnan.rythumitraapp.data.Crop>()
    }
    var showAddDialog by remember {
        mutableStateOf(false)
    }
    var showEditDialog by remember {
        mutableStateOf(false)
    }

    var cropName by remember {
        mutableStateOf("")
    }

    var plantedDate by remember {
        mutableStateOf("")
    }

    var cropNotes by remember {
        mutableStateOf("")
    }

    var waterRequirement by remember {
        mutableStateOf("")
    }
    var editPlantedDate by remember {
        mutableStateOf("")
    }

    var editGrowthStage by remember {
        mutableStateOf("")
    }

    var editNotes by remember {
        mutableStateOf("")
    }

    var editWaterRequirement by remember {
        mutableStateOf("")
    }

    var cropType by remember {
        mutableStateOf("")
    }

    var selectedCrop by remember {
        mutableStateOf<String?>(null)
    }
    var editingCrop by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }
    var cropLoadAttempt by remember {
        mutableIntStateOf(0)
    }
    var cropLoadFailed by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }
    var cropInsight by remember {
        mutableStateOf<String?>(null)
    }
    var selectedScan by remember {
        mutableStateOf<com.Afnan.rythumitraapp.data.ScanResult?>(null)
    }
    val cropInsightModel = remember {
        Firebase
            .ai(
                backend = GenerativeBackend.googleAI()
            )
            .generativeModel(
                modelName = "gemini-3.8-flash"
            )
    }
    var allScans by remember {
        mutableStateOf<List<com.Afnan.rythumitraapp.data.ScanResult>>(emptyList())
    }

    val scanRepository = remember {
        com.Afnan.rythumitraapp.data.ScanRepository()
    }

    LaunchedEffect(Unit) {
        scanRepository.getScans(
            onSuccess = { scans ->
                allScans = scans
            },
            onFailure = { error ->
                android.util.Log.e(
                    "CropScanHistory",
                    "Failed to load scans",
                    error
                )
            }
        )
    }
    // ---------------------------------------------------------
    // SAVE LOCAL BACKUP
    // ---------------------------------------------------------

    fun saveCrops() {

        preferences.edit()
            .putStringSet(
                "crop_list",
                crops.toSet()
            )
            .apply()
    }

    fun generateCropInsight(
        crop: String,
        weatherData: WeatherData?
    ) {
        val details = cropDetails[crop]

        if (details == null) {
            cropInsight = "పంట వివరాలు అందుబాటులో లేవు."
            return
        }

        cropInsight = "🤖 AI సూచనలు తయారు చేస్తున్నాయి..."

        val prompt = """
        మీరు రైతులకు సహాయం చేసే తెలుగు వ్యవసాయ AI సహాయకుడు.

        క్రింది పంట వివరాలను పరిశీలించి రైతుకు ఉపయోగపడే
        సులభమైన మరియు ఆచరణాత్మక సూచనలు తెలుగులో ఇవ్వండి.

        పంట: ${details.name}
        పెరుగుదల దశ: ${details.growthStage}
        నాటిన తేదీ: ${details.plantedDate.ifBlank { "నమోదు కాలేదు" }}
        నీటి అవసరం: ${details.waterRequirement.ifBlank { "నమోదు కాలేదు" }}
        రైతు గమనికలు: ${details.notes.ifBlank { "గమనికలు లేవు" }}
        ప్రస్తుత వాతావరణ సమాచారం:
        ఉష్ణోగ్రత: ${'$'}{weatherData?.temperature?.toString() ?: "అందుబాటులో లేదు"} °C
        తేమ: ${'$'}{weatherData?.humidity?.toString() ?: "అందుబాటులో లేదు"} %
        వర్షం: ${'$'}{weatherData?.rain?.toString() ?: "అందుబాటులో లేదు"} mm
        గాలి వేగం: ${'$'}{weatherData?.windSpeed?.toString() ?: "అందుబాటులో లేదు"} km/h
        వర్షం అవకాశం: ${'$'}{weatherData?.rainProbability?.toString() ?: "అందుబాటులో లేదు"} %

        వాతావరణ పరిస్థితులను కూడా పరిగణనలోకి తీసుకుని
        పంటకు సంబంధించిన సూచనలు ఇవ్వండి.

       ఈ ప్రత్యేక పంటను దృష్టిలో ఉంచుకుని సూచనలు ఇవ్వండి.
       సాధారణ సమాధానాలు కాకుండా, ${'$'}{details.name} పంటకు
       సంబంధించిన ఆచరణాత్మక సూచనలు ఇవ్వండి.

       ప్రస్తుత పెరుగుదల దశను పరిగణనలోకి తీసుకోండి.
       నీటి అవసరాన్ని పరిగణనలోకి తీసుకుని నీటి నిర్వహణ గురించి చెప్పండి.
       పంట ఆరోగ్యం, పోషకాలు మరియు పురుగులు/వ్యాధుల పర్యవేక్షణ గురించి
       అవసరమైన సూచనలు మాత్రమే ఇవ్వండి.

       సూచనలను చిన్నగా, స్పష్టంగా, రైతుకు సులభంగా అర్థమయ్యే తెలుగులో ఇవ్వండి.
      """.trimIndent()

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val response = cropInsightModel.generateContent(prompt)
                val result = response.text
                    ?: "AI సూచనలు అందుబాటులో లేవు."

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    cropInsight = result
                }

            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    cropInsight = "⚠️ AI సూచనలు పొందడంలో సమస్య ఏర్పడింది."
                }
            }
        }
    }
    // ---------------------------------------------------------
    // FIRESTORE + LOCAL SYNCHRONIZATION
    // ---------------------------------------------------------

    LaunchedEffect(cropLoadAttempt) {

        isLoading = true
        cropLoadFailed = false
        repository.getCrops(

            onSuccess = { firestoreCrops ->

                cropLoadFailed = false
                errorMessage = null

                // Existing Firestore crops
                val firestoreNames = mutableSetOf<String>()

                firestoreCrops.forEach { firestoreCrop ->

                    val rawName = firestoreCrop.name.trim()

                    val displayName =
                        if (
                            rawName.startsWith("🌾 ") ||
                            rawName.startsWith("🌽 ") ||
                            rawName.startsWith("🍅 ") ||
                            rawName.startsWith("🌿 ") ||
                            rawName.startsWith("🌶️ ") ||
                            rawName.startsWith("🌱 ")
                        ) {
                            rawName
                        } else {
                            "${getCropIcon(rawName)} $rawName"
                        }

                    firestoreNames.add(displayName)

                    cropIds[displayName] = firestoreCrop.id
                    cropDetails[displayName] = firestoreCrop
                }

                // -------------------------------------------------
                // DISPLAY FIRESTORE DATA
                // -------------------------------------------------

                crops.clear()
                crops.addAll(firestoreNames)

                saveCrops()

                // -------------------------------------------------
                // SYNC OLD LOCAL CROPS TO FIRESTORE
                // -------------------------------------------------

                val localMissingCrops =
                    savedCrops.filter { localCrop ->
                        !firestoreNames.contains(localCrop)
                    }

                fun syncNext(index: Int) {

                    if (index >= localMissingCrops.size) {

                        isLoading = false
                        return
                    }

                    val localCrop =
                        localMissingCrops[index]

                    val nameOnly = localCrop
                        .removePrefix("🌾 ")
                        .removePrefix("🌽 ")
                        .removePrefix("🍅 ")
                        .removePrefix("🌿 ")
                        .removePrefix("🌶️ ")
                        .removePrefix("🌱 ")

                    val cropData =
                        com.Afnan.rythumitraapp.data.Crop(
                            name = nameOnly,
                            plantedDate = "",
                            growthStage = "పర్యవేక్షణలో"
                        )

                    repository.addCrop(

                        crop = cropData,

                        onSuccess = { documentId ->

                            if (!crops.contains(localCrop)) {
                                crops.add(localCrop)
                            }

                            cropIds[localCrop] = documentId

                            saveCrops()

                            syncNext(index + 1)
                        },

                        onFailure = { exception ->

                            errorMessage =
                                exception.message
                                    ?: "పంటను Firestoreలో సేవ్ చేయడం సాధ్యపడలేదు"

                            syncNext(index + 1)
                        }
                    )
                }

                syncNext(0)
            },

            onFailure = { exception ->

                // Firestore failed, use local backup
                isLoading = false
                cropLoadFailed = true

                errorMessage =
                    "☁️ Firestore అందుబాటులో లేదు. స్థానిక పంటల డేటాను చూపిస్తున్నాము."

                crops.clear()
                crops.addAll(savedCrops)
            }
        )
    }

    // ---------------------------------------------------------
    // MAIN SCREEN
    // ---------------------------------------------------------

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF4F8F1)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // -------------------------------------------------
            // HEADER
            // -------------------------------------------------

            Text(
                text = "నా పంటలు 🌱",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF236B35)
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "మీ పంటలను ఒకే చోట నిర్వహించండి",
                fontSize = 15.sp,
                color = Color.DarkGray
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // -------------------------------------------------
            // SUMMARY CARD
            // -------------------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE2F2D9)
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "🌱",
                        fontSize = 42.sp
                    )

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "${crops.size} పంటలు",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF236B35)
                        )

                        Text(
                            text = "మీ సాగు పంటల జాబితా",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // -------------------------------------------------
            // ADD CROP BUTTON
            // -------------------------------------------------

            Button(
                onClick = {
                    cropName = ""
                    plantedDate = ""
                    cropNotes = ""
                    waterRequirement = ""
                    showAddDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "＋ కొత్త పంటను చేర్చండి",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // -------------------------------------------------
            // ERROR / STATUS
            // -------------------------------------------------

            errorMessage?.let { message ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFE8E6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message,
                            fontSize = 14.sp
                        )
                        if (cropLoadFailed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    errorMessage = null
                                    cropLoadAttempt++
                                }
                            ) {
                                Text("మళ్లీ ప్రయత్నించండి")
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------
            // LOADING
            // -------------------------------------------------

            if (isLoading) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "పంటలను సమకాలీకరిస్తోంది...",
                            fontSize = 15.sp,
                            color = Color.DarkGray
                        )
                    }
                }

            } else if (crops.isEmpty()) {

                // -------------------------------------------------
                // EMPTY STATE
                // -------------------------------------------------

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🌱",
                                fontSize = 52.sp
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "ఇంకా పంటలు లేవు",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "పై ఉన్న బటన్‌ను ఉపయోగించి మీ మొదటి పంటను చేర్చండి.",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

            } else {

                // -------------------------------------------------
                // CROP LIST
                // -------------------------------------------------

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = crops,
                        key = { crop -> crop }
                    ) { crop ->

                        val latestScan = allScans
                            .filter { scan ->
                                scan.cropName.equals(
                                    crop.removePrefix("🌾 ")
                                        .removePrefix("🌽 ")
                                        .removePrefix("🍅 ")
                                        .removePrefix("🌿 ")
                                        .removePrefix("🌶️ ")
                                        .removePrefix("🌱 "),
                                    ignoreCase = true
                                )
                            }
                            .maxByOrNull { it.createdAt }
                        val cropScans = allScans
                            .filter { scan ->
                                scan.cropName.equals(
                                    crop.removePrefix("🌾 ")
                                        .removePrefix("🌽 ")
                                        .removePrefix("🍅 ")
                                        .removePrefix("🌿 ")
                                        .removePrefix("🌶️ ")
                                        .removePrefix("🌱 "),
                                    ignoreCase = true
                                )
                            }
                            .sortedByDescending { it.createdAt }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCrop = crop
                                },
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .background(
                                            Color(0xFFE8F5E9),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Text(
                                        text = getCropIcon(crop),
                                        fontSize = 32.sp
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.width(14.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = crop
                                            .removePrefix("🌾 ")
                                            .removePrefix("🌽 ")
                                            .removePrefix("🍅 ")
                                            .removePrefix("🌿 ")
                                            .removePrefix("🌶️ ")
                                            .removePrefix("🌱 "),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F4F2B)
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(
                                        text = "🌱 పంట స్థితి: పర్యవేక్షణలో",
                                        fontSize = 14.sp
                                    )

                                    if (latestScan != null) {
                                        Spacer(
                                            modifier = Modifier.height(6.dp)
                                        )

                                        Text(
                                            text = "🤖 పంట ఆరోగ్య నివేదిక",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Spacer(
                                            modifier = Modifier.height(2.dp)
                                        )

                                        Text(
                                            text = latestScan.result
                                                .lineSequence()
                                                .firstOrNull { line ->
                                                    line.contains("ఆరోగ్య స్థితి")
                                                }
                                                ?: "ఆరోగ్య స్థితి అందుబాటులో లేదు",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )

                                        Text(
                                            text = latestScan.result
                                                .lineSequence()
                                                .firstOrNull { line ->
                                                    line.contains("సమస్య / వ్యాధి")
                                                }
                                                ?: "సమస్య / వ్యాధి సమాచారం అందుబాటులో లేదు",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = latestScan.result
                                                .lineSequence()
                                                .firstOrNull { line ->
                                                    line.contains("తీవ్రత")
                                                }
                                                ?: "తీవ్రత సమాచారం అందుబాటులో లేదు",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = latestScan.result
                                                .lineSequence()
                                                .firstOrNull { line ->
                                                    line.contains("కనిపించే లక్షణాలు")
                                                }
                                                ?: "కనిపించే లక్షణాల సమాచారం అందుబాటులో లేదు",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = latestScan.result
                                                .lineSequence()
                                                .firstOrNull { line ->
                                                    line.contains("రైతుకు సలహా")
                                                }
                                                ?: "రైతుకు సలహా సమాచారం అందుబాటులో లేదు",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = latestScan.result
                                                .lineSequence()
                                                .firstOrNull { line ->
                                                    line.contains("నివారణ")
                                                }
                                                ?: "నివారణ సమాచారం అందుబాటులో లేదు",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "📜 AI పరీక్షలు: ${cropScans.size}",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                        cropScans
                                            .take(3)
                                            .forEach { scan ->

                                                Text(
                                                    text = "• ${java.text.SimpleDateFormat(
                                                        "dd MMM yyyy",
                                                        java.util.Locale.getDefault()
                                                    ).format(java.util.Date(scan.createdAt))}",
                                                    modifier = Modifier.clickable {
                                                        selectedScan = scan
                                                    },
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF2E7D32)
                                                )
                                            }
                                        Spacer(
                                            modifier = Modifier.height(6.dp)
                                        )

                                        TextButton(
                                            onClick = {
                                                selectedScan = latestScan
                                            }
                                        ) {
                                            Text("📋 పూర్తి పంట నివేదిక")
                                        }
                                    } else {
                                        Text(
                                            text = "🔍 AI పరీక్షతో ఆరోగ్యాన్ని తెలుసుకోండి",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {

                                        val firestoreId =
                                            cropIds[crop]

                                        if (firestoreId != null) {

                                            repository.deleteCrop(

                                                cropId = firestoreId,

                                                onSuccess = {

                                                    crops.remove(crop)
                                                    cropIds.remove(crop)
                                                    saveCrops()
                                                    errorMessage = null
                                                },

                                                onFailure = { exception ->

                                                    errorMessage =
                                                        exception.message
                                                            ?: "పంటను తొలగించడం సాధ్యపడలేదు"
                                                }
                                            )

                                        } else {

                                            crops.remove(crop)
                                            saveCrops()
                                        }
                                    }
                                ) {

                                    Text(
                                        text = "🗑️",
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "💡 పంటపై నొక్కితే మరిన్ని వివరాలు చూడవచ్చు.",
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }
                }
            }
        }
    }

    // =========================================================
    // ADD CROP DIALOG
    // =========================================================
    if (showAddDialog) {

        var selectedGrowthStage by remember {
            mutableStateOf("పర్యవేక్షణలో")
        }

        AlertDialog(

            onDismissRequest = {
                showAddDialog = false
            },

            title = {

                Text(
                    text = "🌱 కొత్త పంటను చేర్చండి",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = cropName,
                        onValueChange = {
                            cropName = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("పంట పేరు")
                        },
                        placeholder = {
                            Text("ఉదాహరణ: పత్తి")
                        },
                        singleLine = true
                    )
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = plantedDate,
                        onValueChange = {
                            plantedDate = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("నాటిన తేదీ")
                        },
                        placeholder = {
                            Text("ఉదాహరణ: 15-08-2026")
                        },
                        singleLine = true
                    )
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = cropNotes,
                        onValueChange = {
                            cropNotes = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("రైతు గమనికలు")
                        },
                        placeholder = {
                            Text("ఉదాహరణ: ఎరువు వేసాను")
                        },
                        minLines = 2,
                        maxLines = 4
                    )
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "💧 నీటి అవసరం",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    listOf(
                        "తక్కువ",
                        "మధ్యస్థం",
                        "ఎక్కువ"
                    ).forEach { requirement ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    waterRequirement = requirement
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = waterRequirement == requirement,
                                onClick = {
                                    waterRequirement = requirement
                                }
                            )

                            Text(
                                text = requirement,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                    Text(
                        text = "🌱 పంట పెరుగుదల దశ",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    listOf(
                        "పర్యవేక్షణలో",
                        "పెరుగుదలలో",
                        "పుష్పించే దశ",
                        "పంట తయారీ దశ"
                    ).forEach { stage ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGrowthStage = stage
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = selectedGrowthStage == stage,
                                onClick = {
                                    selectedGrowthStage = stage
                                }
                            )

                            Text(
                                text = stage,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val name = cropName.trim()

                        if (name.isNotEmpty()) {

                            val newCrop =
                                "${getCropIcon(name)} $name"

                            if (!crops.contains(newCrop)) {

                                val cropData =
                                    com.Afnan.rythumitraapp.data.Crop(
                                        name = name,
                                        plantedDate = plantedDate.trim(),
                                        growthStage = selectedGrowthStage ,
                                        notes = cropNotes.trim(),
                                        waterRequirement = waterRequirement
                                    )

                                repository.addCrop(

                                    crop = cropData,

                                    onSuccess = { documentId ->

                                        crops.add(newCrop)

                                        cropIds[newCrop] =
                                            documentId

                                        saveCrops()

                                        cropName = ""
                                        showAddDialog = false
                                        errorMessage = null
                                    },

                                    onFailure = { exception ->

                                        errorMessage =
                                            exception.message
                                                ?: "పంటను Firestoreలో సేవ్ చేయడం సాధ్యపడలేదు"
                                    }
                                )

                            } else {

                                errorMessage =
                                    "ఈ పంట ఇప్పటికే ఉంది."
                            }
                        }
                    }
                ) {

                    Text("చేర్చు")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showAddDialog = false
                    }
                ) {

                    Text("రద్దు")
                }
            }
        )
    }
    // =========================================================
    // CROP DETAILS DIALOG
    // =========================================================

    selectedCrop?.let { crop ->

        val cropNameOnly = crop
            .removePrefix("🌾 ")
            .removePrefix("🌽 ")
            .removePrefix("🍅 ")
            .removePrefix("🌿 ")
            .removePrefix("🌶️ ")
            .removePrefix("🌱 ")

        AlertDialog(

            onDismissRequest = {
                selectedCrop = null
            },

            title = {

                Text(
                    text = "${getCropIcon(crop)} $cropNameOnly",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text = "🌱 పంట స్థితి: ${
                            cropDetails[crop]?.growthStage ?: "పర్యవేక్షణలో"
                        }",
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "📅 నాటిన తేదీ: ${
                            cropDetails[crop]?.plantedDate?.takeIf { it.isNotBlank() }
                                ?: "తేదీ నమోదు కాలేదు"
                        }",
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "📝 రైతు గమనికలు: ${
                            cropDetails[crop]?.notes?.takeIf { it.isNotBlank() }
                                ?: "గమనికలు నమోదు కాలేదు"
                        }",
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "💧 నీటి అవసరం: ${
                            cropDetails[crop]?.waterRequirement?.takeIf { it.isNotBlank() }
                                ?: "నమోదు కాలేదు"
                        }",
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "🌾 పంట రకం: ${
                            cropDetails[crop]?.cropType?.takeIf { it.isNotBlank() }
                                ?: "పంట రకం నమోదు కాలేదు"
                        }",
                        fontSize = 16.sp
                    )

                    Text(
                        text = "🔍 వ్యాధులు లేదా సమస్యలను తెలుసుకోవడానికి AI పంట పరీక్షను ఉపయోగించండి.",
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            generateCropInsight(
                                crop = crop,
                                weatherData = weatherData
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🤖 స్మార్ట్ పంట సూచనలు")
                    }
                    if (cropInsight != null) {
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = cropInsight!!,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "💧 నీటి అవసరాన్ని వాతావరణం మరియు నేల పరిస్థితులను బట్టి నిర్ణయించండి.",
                        fontSize = 15.sp
                    )
                }
            },

            confirmButton = {

                Row {

                    TextButton(
                        onClick = {
                            val details = cropDetails[crop]

                            editPlantedDate = details?.plantedDate ?: ""
                            editGrowthStage = details?.growthStage ?: ""
                            editNotes = details?.notes ?: ""
                            editWaterRequirement = details?.waterRequirement ?: ""

                            editingCrop = crop
                            selectedCrop = null
                            showEditDialog = true
                        }
                    ) {
                        Text("✏️ మార్చు")
                    }

                    TextButton(
                        onClick = {
                            selectedCrop = null
                        }
                    ) {
                        Text("సరే")
                    }
                }
            },
        )
    }
    selectedScan?.let { scan ->

        AlertDialog(
            onDismissRequest = {
                selectedScan = null
            },
            title = {
                Text(
                    text = "🤖 పూర్తి AI పంట నివేదిక",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 500.dp)
                ) {
                    item {
                        Text(
                            text = scan.result,
                            fontSize = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedScan = null
                    }
                ) {
                    Text("మూసివేయి")
                }
            }
        )
    }
    if (showEditDialog) {

        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
            },

            title = {
                Text(
                    text = "✏️ పంట వివరాలను మార్చండి",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = editPlantedDate,
                        onValueChange = {
                            editPlantedDate = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("నాటిన తేదీ")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = {
                            editNotes = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("రైతు గమనికలు")
                        },
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "💧 నీటి అవసరం",
                        fontWeight = FontWeight.Bold
                    )

                    listOf(
                        "తక్కువ",
                        "మధ్యస్థం",
                        "ఎక్కువ"
                    ).forEach { requirement ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editWaterRequirement = requirement
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = editWaterRequirement == requirement,
                                onClick = {
                                    editWaterRequirement = requirement
                                }
                            )

                            Text(
                                text = requirement,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "🌱 పంట పెరుగుదల దశ",
                        fontWeight = FontWeight.Bold
                    )

                    listOf(
                        "పర్యవేక్షణలో",
                        "పెరుగుదలలో",
                        "పుష్పించే దశ",
                        "పంట తయారీ దశ"
                    ).forEach { stage ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editGrowthStage = stage
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = editGrowthStage == stage,
                                onClick = {
                                    editGrowthStage = stage
                                }
                            )

                            Text(
                                text = stage,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        val crop = editingCrop
                        val cropId = crop?.let { cropIds[it] }

                        if (crop != null && cropId != null) {

                            repository.updateCrop(
                                cropId = cropId,
                                plantedDate = editPlantedDate.trim(),
                                growthStage = editGrowthStage,
                                notes = editNotes.trim(),
                                waterRequirement = editWaterRequirement,

                                onSuccess = {

                                    val oldDetails = cropDetails[crop]

                                    if (oldDetails != null) {
                                        cropDetails[crop] = oldDetails.copy(
                                            plantedDate = editPlantedDate.trim(),
                                            growthStage = editGrowthStage,
                                            notes = editNotes.trim(),
                                            waterRequirement = editWaterRequirement
                                        )
                                    }

                                    showEditDialog = false
                                    editingCrop = null
                                    errorMessage = null
                                },

                                onFailure = { exception ->
                                    errorMessage = exception.message
                                        ?: "పంట వివరాలను మార్చడం సాధ్యపడలేదు"
                                }
                            )
                        } else {
                            errorMessage = "పంట వివరాలు కనుగొనబడలేదు"
                        }
                    }
                ) {
                    Text("సేవ్")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                    }
                ) {
                    Text("రద్దు")
                }
            }
        )
    }
}

// =========================================================
// ALERTS SCREEN
// =========================================================

@Composable
fun AlertsScreen(
    alerts: List<FarmerAlert> = emptyList(),
    weatherAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF4F8F1)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "రైతు హెచ్చరికలు 🔔",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF236B35)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            if (alerts.isNotEmpty()) {

                Text(
                    text = "🔔 ప్రస్తుతం ${alerts.size} హెచ్చరికలు ఉన్నాయి",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF236B35)
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            if (!weatherAvailable) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "⏳ వాతావరణ సమాచారం అందుబాటులో లేదు",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "స్థానిక వాతావరణ సమాచారం వచ్చిన తర్వాత ముఖ్యమైన హెచ్చరికలు ఇక్కడ కనిపిస్తాయి.",
                            fontSize = 16.sp
                        )
                    }
                }

            } else if (alerts.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "✅ ప్రస్తుతం మీ పంటకు ముఖ్యమైన హెచ్చరికలు లేవు",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "వాతావరణం లేదా పంటకు సంబంధించిన ముఖ్యమైన హెచ్చరికలు వచ్చినప్పుడు ఇక్కడ కనిపిస్తాయి.",
                            fontSize = 16.sp
                        )
                    }
                }

            } else {

                alerts.forEach { alert ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (alert.priority == 1) {
                                    Color(0xFFFFF3E0)
                                } else {
                                    Color.White
                                }
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text = alert.title,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF236B35)
                            )
                            if (alert.priority == 1) {

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = "🔴 అత్యవసర హెచ్చరిక",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                            } else {

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = "🟡 ముఖ్యమైన సమాచారం",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "👉 రైతు చేయాల్సింది:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF236B35)
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = alert.message,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
// =========================================================
// PROFILE SCREEN
// =========================================================

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onCropsClick: () -> Unit,
    onAiScanClick: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    // =====================================================
    // LOCAL STORAGE
    // =====================================================

    val preferences = remember {
        context.getSharedPreferences(
            "rythu_mitra_profile",
            android.content.Context.MODE_PRIVATE
        )
    }

    // =====================================================
    // PROFILE STATE
    // =====================================================

    var farmerName by remember {
        mutableStateOf(
            preferences.getString("farmer_name", "") ?: ""
        )
    }

    var village by remember {
        mutableStateOf(
            preferences.getString("village", "") ?: ""
        )
    }

    var mainCrop by remember {
        mutableStateOf(
            preferences.getString("main_crop", "") ?: ""
        )
    }

    var landSize by remember {
        mutableStateOf(
            preferences.getString("land_size", "") ?: ""
        )
    }

    var showSavedMessage by remember {
        mutableStateOf(false)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    // =====================================================
    // FIRESTORE PROFILE REPOSITORY
    // =====================================================

    val profileRepository = remember {
        com.Afnan.rythumitraapp.data.ProfileRepository()
    }

    // =====================================================
    // LOAD PROFILE FROM FIRESTORE
    // =====================================================

    LaunchedEffect(Unit) {

        profileRepository.loadProfile(

            onSuccess = { name, villageValue, crop, land ->

                // Update UI
                farmerName = name
                village = villageValue
                mainCrop = crop
                landSize = land

                // Synchronize local backup
                preferences.edit()
                    .putString("farmer_name", name)
                    .putString("village", villageValue)
                    .putString("main_crop", crop)
                    .putString("land_size", land)
                    .apply()
            },

            onFailure = {
                // Firestore unavailable:
                // keep existing SharedPreferences values
            }
        )
    }
    // =====================================================
    // UI
    // =====================================================

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F8F1))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // =================================================
        // HEADER
        // =================================================

        item {

            Text(
                text = "నా వివరాలు 👤",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF236B35)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "మీ వ్యవసాయ ప్రొఫైల్",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }

        // =================================================
        // FARMER HEADER CARD
        // =================================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "👨‍🌾",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (farmerName.isBlank()) {
                            "రైతు"
                        } else {
                            farmerName
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "మీ వ్యవసాయ వివరాలను ఇక్కడ నిర్వహించండి",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // =================================================
        // FARMER DETAILS
        // =================================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Text(
                        text = "👨‍🌾 రైతు వివరాలు",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF236B35)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // NAME

                    OutlinedTextField(
                        value = farmerName,
                        onValueChange = {
                            farmerName = it
                            showSavedMessage = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("👤 మీ పేరు")
                        },
                        placeholder = {
                            Text("ఉదా: రమేష్")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // VILLAGE

                    OutlinedTextField(
                        value = village,
                        onValueChange = {
                            village = it
                            showSavedMessage = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("📍 గ్రామం")
                        },
                        placeholder = {
                            Text("ఉదా: కర్నూలు")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // MAIN CROP

                    OutlinedTextField(
                        value = mainCrop,
                        onValueChange = {
                            mainCrop = it
                            showSavedMessage = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("🌱 ప్రధాన పంట")
                        },
                        placeholder = {
                            Text("ఉదా: వరి")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // LAND SIZE

                    OutlinedTextField(
                        value = landSize,
                        onValueChange = {
                            landSize = it
                            showSavedMessage = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("📐 పొలం విస్తీర్ణం")
                        },
                        placeholder = {
                            Text("ఉదా: 2 ఎకరాలు")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // =================================================
                    // SAVE BUTTON
                    // =================================================

                    Button(
                        onClick = {

                            // Save locally
                            preferences.edit()
                                .putString(
                                    "farmer_name",
                                    farmerName.trim()
                                )
                                .putString(
                                    "village",
                                    village.trim()
                                )
                                .putString(
                                    "main_crop",
                                    mainCrop.trim()
                                )
                                .putString(
                                    "land_size",
                                    landSize.trim()
                                )
                                .apply()

                            // Save to Firestore
                            isSaving = true
                            showSavedMessage = false

                            profileRepository.saveProfile(
                                farmerName = farmerName.trim(),
                                village = village.trim(),
                                mainCrop = mainCrop.trim(),
                                landSize = landSize.trim(),

                                onSuccess = {
                                    isSaving = false
                                    showSavedMessage = true
                                },

                                onFailure = {
                                    isSaving = false
                                    showSavedMessage = false
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {

                        Text(
                            text = if (isSaving) {
                                "⏳ సేవ్ అవుతోంది..."
                            } else {
                                "💾 వివరాలు సేవ్ చేయండి"
                            },
                            fontSize = 17.sp
                        )
                    }

                    // =================================================
                    // SUCCESS MESSAGE
                    // =================================================

                    if (showSavedMessage) {

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "✅ మీ వివరాలు సేవ్ చేయబడ్డాయి",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =================================================
        // QUICK INFORMATION
        // =================================================

        item {

            Text(
                text = "త్వరిత సమాచారం",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF236B35)
            )
        }

        // =================================================
        // MY CROPS
        // =================================================

        item {

            Card(
                onClick = onCropsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "🌱",
                        fontSize = 38.sp
                    )

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Column {

                        Text(
                            text = "నా పంటలు",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "మీరు సాగు చేస్తున్న పంటలను నిర్వహించండి",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // =================================================
        // AI CROP SCAN
        // =================================================

        item {

            Card(
                onClick = onAiScanClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "🤖",
                        fontSize = 38.sp
                    )

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Column {

                        Text(
                            text = "పంట ఆరోగ్య పరీక్ష",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "పంట ఆకు ఫోటోతో వ్యాధులను పరీక్షించండి",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // =================================================
        // FOOTER
        // =================================================

        item {

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "🌾 రైతు మిత్ర మీ వ్యవసాయానికి మీ స్నేహితుడు",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                color = Color(0xFF236B35),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}