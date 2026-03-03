package com.example.habitverse.ui

sealed class LoginState {
    object Success : LoginState()
    data class Error(val message: String): LoginState()
    object Idle: LoginState()
}
sealed class RegistrationState {
    object Success : RegistrationState()
    data class Error(val message: String): RegistrationState()
    object Idle: RegistrationState()
}