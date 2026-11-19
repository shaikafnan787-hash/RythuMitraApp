package com.Afnan.rythumitraapp.data

data class ScanResult(
    val id: String = "",
    val cropName: String = "",
    val result: String = "",
    val createdAt: Long = System.currentTimeMillis()
)