package com.Afnan.rythumitraapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ScanRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getScansCollection() =
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("scans")

    fun addScan(
        scan: ScanResult,
        onSuccess: (String) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        val document = getScansCollection().document()

        val scanData = hashMapOf(
            "id" to document.id,
            "cropName" to scan.cropName,
            "result" to scan.result,
            "createdAt" to scan.createdAt
        )

        document.set(scanData)
            .addOnSuccessListener {
                onSuccess(document.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getScans(
        onSuccess: (List<ScanResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        getScansCollection()
            .get()
            .addOnSuccessListener { snapshot ->

                val scans = snapshot.documents.map { document ->

                    ScanResult(
                        id = document.getString("id")
                            ?: document.id,

                        cropName = document.getString("cropName")
                            ?: "",

                        result = document.getString("result")
                            ?: "",

                        createdAt = document.getLong("createdAt")
                            ?: 0L
                    )
                }

                onSuccess(scans)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteScan(
        scanId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        getScansCollection()
            .document(scanId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}