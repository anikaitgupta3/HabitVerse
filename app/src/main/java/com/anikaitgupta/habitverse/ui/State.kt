package com.anikaitgupta.habitverse.ui

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
sealed class ForgotPasswordState {
    object Success : ForgotPasswordState()
    data class Error(val message: String): ForgotPasswordState()
    object Idle: ForgotPasswordState()
}
sealed class DeleteAccountState {
    object Success : DeleteAccountState()
    data class Error(val message: String): DeleteAccountState()
    object Idle: DeleteAccountState()
}