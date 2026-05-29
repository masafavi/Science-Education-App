package com.example.data

import com.example.data.database.ActivityLog
import com.example.data.database.ActivityLogDao
import com.example.data.database.User
import com.example.data.database.UserDao
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao
) {
    val allStudents: Flow<List<User>> = userDao.getAllStudents()
    val allLogs: Flow<List<ActivityLog>> = activityLogDao.getAllLogs()

    suspend fun getUserByPhone(phone: String): User? = userDao.getUserByPhone(phone)
    fun getUserFlowByPhone(phone: String): Flow<User?> = userDao.getUserFlowByPhone(phone)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun deleteUser(phone: String) = userDao.deleteUser(phone)
    
    suspend fun logActivity(phone: String, action: String) {
        activityLogDao.insertLog(ActivityLog(phoneNumber = phone, action = action))
    }
    
    fun getLogsForStudent(phone: String): Flow<List<ActivityLog>> {
        return activityLogDao.getLogsForUser(phone)
    }
}
