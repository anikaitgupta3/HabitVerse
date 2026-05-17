package com.anikaitgupta.habitverse.data

import com.anikaitgupta.habitverse.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth): AuthRepository {
    override suspend fun createAccount(email: String, password: String) {
        //TODO("Not yet implemented")
        firebaseAuth.createUserWithEmailAndPassword(email,password).await()

    }

    override suspend fun signIn(email: String, password: String) {
        //TODO("Not yet implemented")
        firebaseAuth.signInWithEmailAndPassword(email,password).await()
    }

    override suspend fun logout() {
        //TODO("Not yet implemented")
        firebaseAuth.signOut()
    }

    override fun checkLoggedIn(): Boolean {
        //TODO("Not yet implemented")
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            return true
        }
        else{
            return false
        }
    }

    override suspend fun getUserId(): String? {
        //TODO("Not yet implemented")
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun sendPasswordResetMail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun deleteAccount() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}