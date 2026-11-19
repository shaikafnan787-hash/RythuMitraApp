package com.Afnan.rythumitraapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getProfileDocument() =
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("profile")
            .document("info")

    fun saveProfile(
        farmerName: String,
        village: String,
        mainCrop: String,
        landSize: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {

        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        val profileData = hashMapOf(
            "farmerName" to farmerName,
            "village" to village,
            "mainCrop" to mainCrop,
            "landSize" to landSize
        )

        getProfileDocument()
            .set(profileData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    fun loadProfile(
        onSuccess: (
            farmerName: String,
            village: String,
            mainCrop: String,
            landSize: String
        ) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("User is not authenticated"))
            return
        }

        getProfileDocument()
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val farmerName =
                        document.getString("farmerName") ?: ""

                    val village =
                        document.getString("village") ?: ""

                    val mainCrop =
                        document.getString("mainCrop") ?: ""

                    val landSize =
                        document.getString("landSize") ?: ""

                    onSuccess(
                        farmerName,
                        village,
                        mainCrop,
                        landSize
                    )

                } else {
                    onFailure(Exception("Profile does not exist"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}