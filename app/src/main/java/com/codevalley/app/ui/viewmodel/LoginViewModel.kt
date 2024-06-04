package com.codevalley.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codevalley.app.repository.UserRepository
import com.codevalley.app.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf("")

    fun login(): Boolean {
        if (email == "") {
            errorMessage = "Please enter an email"
            return false
        }
        if (password == "") {
            errorMessage = "Please enter your password"
            return false
        }
        else {
            var success = false
            viewModelScope.launch {
                try {
                    TokenManager.token = userRepository.login(email, password).accessToken
                    success = true
                } catch (e: Exception) {
                    errorMessage = "Email or password incorrect"
                    TokenManager.token = null
                }
            }
            return success
        }
    }
}
