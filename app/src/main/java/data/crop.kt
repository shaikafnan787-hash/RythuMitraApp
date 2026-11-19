package com.Afnan.rythumitraapp.data

data class Crop(
    val id: String = "",
    val name: String = "",
    val cropType: String = "",
    val plantedDate: String = "",
    val growthStage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val waterRequirement: String = ""
)