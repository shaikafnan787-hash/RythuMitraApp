package com.Afnan.rythumitraapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {

    private const val TAG = "RythuMitraAuth"

    fun signInAnonymously(
        onSuccess: (String) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val auth = FirebaseAuth.getInstance()

        // If the user already has an account, use it.
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d(TAG, "User already signed in. UID: ${currentUser.uid}")
            onSuccess(currentUser.uid)
            return
        }

        // Create a new anonymous Firebase user.
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid

                if (uid != null) {
                    Log.d(TAG, "Anonymous sign-in successful. UID: $uid")
                    onSuccess(uid)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Anonymous sign-in failed", exception)
                onFailure(exception)
            }
    }
}