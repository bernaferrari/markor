package com.bernaferrari.remarkor.domain.service

import kotlinx.datetime.LocalDateTime

/**
 * Reminder data class.
 */
data class Reminder(
    val id: String,
    val title: String,
    val message: String,
    val filePath: String,
    val triggerAt: LocalDateTime,
    val isRepeating: Boolean = false,
    val repeatInterval: ReminderInterval = ReminderInterval.NONE,
    val isEnabled: Boolean = true
)

/**
 * Reminder repeat interval options.
 */
enum class ReminderInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Platform-agnostic reminder service interface.
 * Use expect/actual to implement platform-specific notification systems.
 */
interface ReminderService {
    /**
     * Schedule a reminder.
     * @return The reminder ID, or null if scheduling failed.
     */
    suspend fun scheduleReminder(reminder: Reminder): String?
    
    /**
     * Cancel a reminder.
     */
    suspend fun cancelReminder(reminderId: String)
    
    /**
     * Get all scheduled reminders.
     */
    suspend fun getAllReminders(): List<Reminder>
    
    /**
     * Get reminders for a specific file.
     */
    suspend fun getRemindersForFile(filePath: String): List<Reminder>
    
    /**
     * Update an existing reminder.
     */
    suspend fun updateReminder(reminder: Reminder): Boolean
    
    /**
     * Enable or disable a reminder.
     */
    suspend fun setReminderEnabled(reminderId: String, enabled: Boolean)
}
