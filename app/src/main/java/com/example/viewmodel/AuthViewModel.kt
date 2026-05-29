package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.SessionManager
import com.example.data.database.AppDatabase
import com.example.data.database.User
import com.example.BuildConfig
import com.example.data.network.SmsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.userDao(), db.activityLogDao())
    val sessionManager = SessionManager(application)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val phone = sessionManager.getCurrentPhone()
        if (phone != null) {
            listenToUser(phone)
        }
    }

    val students: StateFlow<List<User>> = repository.allStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var generatedOtp: String? = null

    fun sendOtp(phone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val key = BuildConfig.KAVENEGAR_API_KEY
            if (key.isEmpty() || key == "MY_KAVENEGAR_API_KEY") {
                // Fallback to offline/test mode
                generatedOtp = "1234"
                onResult(true, "کد تست 1234 ارسال شد.")
                return@launch
            }

            try {
                // Generate real 4-digit code
                val code = Random.nextInt(1000, 9999).toString()
                SmsClient.service.sendOtp(
                    apiKey = key,
                    phoneNumber = phone,
                    otpCode = code,
                    template = "scienceapp" // User must configure this template in Kavenegar
                )
                generatedOtp = code
                onResult(true, "کد تایید ارسال شد.")
            } catch (e: Exception) {
                generatedOtp = "1234" // Fallback on failure
                onResult(false, "خطا در ارسال پیامک. با کد تست ورود کنید.")
            }
        }
    }

    fun login(phone: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            // Check if Admin
            val role = if (phone == "09937551391") "ADMIN" else "STUDENT"
            
            // Register or update last login
            val existing = repository.getUserByPhone(phone)
            val userToSave = existing?.copy(lastLoginTime = System.currentTimeMillis()) 
                    ?: User(phone, role, true, System.currentTimeMillis())
            
            repository.insertUser(userToSave)
            
            sessionManager.loginUser(phone, role)
            repository.logActivity(phone, "User logged in")
            
            // Start listening to user flow
            listenToUser(phone)
            
            onSuccess(role)
        }
    }

    private fun listenToUser(phone: String) {
        viewModelScope.launch {
            repository.getUserFlowByPhone(phone).collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun addStudent(phone: String) {
        viewModelScope.launch {
            repository.insertUser(User(phoneNumber = phone, role = "STUDENT", isApproved = true))
        }
    }

    fun removeStudent(phone: String) {
        viewModelScope.launch {
            repository.deleteUser(phone)
        }
    }

    fun getStudentLogs(phone: String): StateFlow<List<com.example.data.database.ActivityLog>> {
        return repository.getLogsForStudent(phone).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}
