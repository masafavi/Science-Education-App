package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val role: String, // "ADMIN" or "STUDENT"
    val isApproved: Boolean = true, // By default student might need admin approval, or admin manually adds them. If admin adds them, they are in the DB and approved.
    val lastLoginTime: Long = 0
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
