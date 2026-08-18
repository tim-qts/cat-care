package com.tim.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cats")
data class CatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val breed: String = "",
    val colorHex: String = "#FF8C38",
    val avatarEmoji: String = "🐱",
    val requiresMedication: Boolean = false,
    val medicationNotes: String = "",
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cat_daily_logs",
    indices = [Index(value = ["catId", "dateString"], unique = true)]
)
data class CatDailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val catId: Int,
    val dateString: String, // Format: YYYY-MM-DD
    val morningMeal: Boolean = false,
    val eveningMeal: Boolean = false,
    val poop: Boolean = false,
    val medication: Boolean = false,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getCompletedCount(requiresMedication: Boolean = true): Int {
        return (if (morningMeal) 1 else 0) +
                (if (eveningMeal) 1 else 0) +
                (if (poop) 1 else 0) +
                (if (requiresMedication && medication) 1 else 0)
    }

    fun getTotalTasks(requiresMedication: Boolean = true): Int {
        return if (requiresMedication) 4 else 3
    }

    fun isFullyCompleted(requiresMedication: Boolean = true): Boolean {
        return getCompletedCount(requiresMedication) == getTotalTasks(requiresMedication)
    }

    // Default 4-task getter for backward compatibility
    val completedCount: Int
        get() = getCompletedCount(true)

    val totalTasks: Int
        get() = 4

    val isFullyCompleted: Boolean
        get() = isFullyCompleted(true)
}
