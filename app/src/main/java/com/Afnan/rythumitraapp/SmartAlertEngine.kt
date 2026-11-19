package com.Afnan.rythumitraapp

import com.Afnan.rythumitraapp.data.Crop

fun generateSmartAlerts(
    weatherData: WeatherData,
    farmerCrops: List<Crop> = emptyList()
): List<FarmerAlert> {

    val alerts = mutableListOf<FarmerAlert>()

    // 🌸 Flowering stage crop alert
    if (
        farmerCrops.any {
            it.growthStage.contains("పుష్ప", ignoreCase = true)
        }
    ) {

        val floweringCrop =
            farmerCrops.firstOrNull {
                it.growthStage.contains("పుష్ప", ignoreCase = true)
            }

        if (floweringCrop != null) {

            alerts.add(
                FarmerAlert(
                    title = "🌸 పుష్పించే దశ హెచ్చరిక",
                    message = "${floweringCrop.name} పంట పుష్పించే దశలో ఉంది. నీరు మరియు పంట ఆరోగ్యాన్ని జాగ్రత్తగా గమనించండి.",
                    type = "flowering",
                    priority = 2
                )
            )
        }
    }

    // 🌱 Growth stage crop alert
    val monitoringCrop =
        farmerCrops.firstOrNull {
            it.growthStage.contains("పర్యవేక్ష", ignoreCase = true)
        }

    if (monitoringCrop != null) {

        alerts.add(
            FarmerAlert(
                title = "🌱 పంట పర్యవేక్షణ సూచన",
                message = "${monitoringCrop.name} పంటను క్రమం తప్పకుండా గమనించండి. ఆకులు, పురుగులు, నీటి అవసరం మరియు పంట ఆరోగ్యాన్ని పరిశీలించండి.",
                type = "monitoring",
                priority = 2
            )
        )
    }

    // 🌧️ Rain alert
    if (weatherData.rainProbability >= 70) {

        alerts.add(
            FarmerAlert(
                title = "🌧️ వర్షం హెచ్చరిక",
                message = "వర్షం వచ్చే అవకాశం ఎక్కువగా ఉంది. పిచికారీ పనులను వాయిదా వేయండి.",
                type = "rain",
                priority = 1
            )
        )
    }

    // 💨 Wind alert
    if (weatherData.windSpeed >= 25.0) {

        alerts.add(
            FarmerAlert(
                title = "💨 గాలి హెచ్చరిక",
                message = "గాలి వేగం ఎక్కువగా ఉంది. పిచికారీ చేయడం వాయిదా వేయండి.",
                type = "wind",
                priority = 1
            )
        )
    }

    // 🌡️ Heat alert
    if (weatherData.temperature >= 40.0) {

        alerts.add(
            FarmerAlert(
                title = "🌡️ వేడి హెచ్చరిక",
                message = "ఉష్ణోగ్రత చాలా ఎక్కువగా ఉంది. పంటకు తగినంత నీరు అందేలా చూడండి.",
                type = "heat",
                priority = 1
            )
        )
    }

    // 🦠 Disease risk alert
    if (weatherData.humidity >= 85) {

        alerts.add(
            FarmerAlert(
                title = "🦠 పంట వ్యాధి హెచ్చరిక",
                message = "తేమ ఎక్కువగా ఉంది. ఫంగస్ వ్యాధుల లక్షణాలను గమనించండి.",
                type = "disease",
                priority = 2
            )
        )
    }

    // 🌧️ Tomorrow rain alert
    if (weatherData.tomorrowRainProbability >= 80) {

        alerts.add(
            FarmerAlert(
                title = "🌧️ రేపటి వర్షం హెచ్చరిక",
                message = "రేపు వర్షం వచ్చే అవకాశం చాలా ఎక్కువగా ఉంది. పిచికారీ పనులను వాయిదా వేయండి.",
                type = "tomorrow_rain",
                priority = 1
            )
        )
    }

    // ⛈️ Tomorrow storm alert
    if (
        weatherData.tomorrowWeatherCode == 95 ||
        weatherData.tomorrowWeatherCode == 96 ||
        weatherData.tomorrowWeatherCode == 99
    ) {

        alerts.add(
            FarmerAlert(
                title = "⛈️ రేపటి వాతావరణ హెచ్చరిక",
                message = "రేపు ఉరుములతో కూడిన వర్షం అవకాశం ఉంది. పొలంలో జాగ్రత్తగా ఉండండి.",
                type = "storm",
                priority = 1
            )
        )
    }

    return alerts.sortedBy {
        it.priority
    }
}