package com.Afnan.rythumitraapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CropRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun getCropsCollection() =
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("crops")

    fun addCrop(
        crop: Crop,
        onSuccess: (String) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        val document = getCropsCollection().document()

        val cropData = hashMapOf(
            "id" to document.id,
            "name" to crop.name,
            "cropType" to crop.cropType,
            "plantedDate" to crop.plantedDate,
            "growthStage" to crop.growthStage,
            "notes" to crop.notes,
            "waterRequirement" to crop.waterRequirement,
            "createdAt" to crop.createdAt
        )

        document.set(cropData)
            .addOnSuccessListener {
                onSuccess(document.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getCrops(
        onSuccess: (List<Crop>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        getCropsCollection()
            .get()
            .addOnSuccessListener { snapshot ->

                val crops = snapshot.documents.map { document ->

                    Crop(
                        id = document.getString("id")
                            ?: document.id,

                        name = document.getString("name")
                            ?: "",

                        cropType = document.getString("cropType")
                            ?: "",

                        plantedDate = document.getString("plantedDate")
                            ?: "",

                        growthStage = document.getString("growthStage")
                            ?: "",

                        createdAt = document.getLong("createdAt")
                            ?: 0L,
                        notes = document.getString("notes")
                            ?: "",
                            waterRequirement = document.getString("waterRequirement")
                            ?: ""
                    )
                }

                onSuccess(crops)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteCrop(
        cropId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        getCropsCollection()
            .document(cropId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    fun updateCrop(
        cropId: String,
        plantedDate: String,
        growthStage: String,
        notes: String,
        waterRequirement: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        val cropData = mutableMapOf<String, Any>(
            "plantedDate" to plantedDate,
            "growthStage" to growthStage,
            "notes" to notes,
            "waterRequirement" to waterRequirement
        )

        getCropsCollection()
            .document(cropId)
            .update(cropData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}