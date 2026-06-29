package com.alhaq.amnshield.guardian.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val apps: String,          // comma-separated package IDs
    val websites: String,      // comma-separated domains
    val keywords: String,      // comma-separated keywords
    val fullBlock: Boolean,
    val dailyLimit: Int,
    val hourlyLimit: Int
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleId: Long,
    val weekday: Int,
    val ranges: String // e.g., "540-600,780-840"
)

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY id DESC")
    suspend fun getAll(): List<RuleEntity>

    @Insert
    suspend fun insert(rule: RuleEntity): Long

    @Update
    suspend fun update(rule: RuleEntity)

    @Delete
    suspend fun delete(rule: RuleEntity)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE ruleId = :ruleId")
    suspend fun forRule(ruleId: Long): List<ScheduleEntity>

    @Insert
    suspend fun insertAll(vararg schedules: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE ruleId = :ruleId")
    suspend fun deleteForRule(ruleId: Long)
}

@Database(entities = [RuleEntity::class, ScheduleEntity::class], version = 1, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun scheduleDao(): ScheduleDao
}

