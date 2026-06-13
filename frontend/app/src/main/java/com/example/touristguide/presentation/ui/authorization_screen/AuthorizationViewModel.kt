package com.example.touristguide.presentation.ui.authorization_screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationViewModel(
    private val repository: Repository
) : ViewModel() {

    var login by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun onLoginChange(value: String) {
        login = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun clearFields() {
        login = ""
        password = ""
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.login(login.trim(), password)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Login error: ${e.message}.")
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Ошибка входа")
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.register(login.trim(), password)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Register error: ${e.message}.")
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Ошибка регистрации")
                }
            }
        }
    }
}
