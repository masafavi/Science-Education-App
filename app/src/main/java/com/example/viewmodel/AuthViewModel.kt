package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.SessionManager
import com.example.data.database.AppDatabase
import com.example.data.database.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.userDao(), db.activityLogDao())
    val sessionManager = SessionManager(application)

    val students: StateFlow<List<User>> = repository.allStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
            
            onSuccess(role)
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
