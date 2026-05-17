package com.anikaitgupta.habitverse.domain

interface AuthRepository {
    suspend fun createAccount(email: String,password: String)
    suspend fun signIn(email: String,password: String)
    suspend fun logout()
    fun checkLoggedIn(): Boolean
    suspend fun getUserId(): String?
    suspend fun sendPasswordResetMail(email: String)

    suspend fun deleteAccount()

}