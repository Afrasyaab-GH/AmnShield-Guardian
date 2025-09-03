package com.deenshield.blocker.data

import android.content.Context
import androidx.room.Room
import com.deenshield.blocker.model.Block
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlockRepository private constructor(private val db: LocalDatabase) {

    companion object {
        @Volatile private var INSTANCE: BlockRepository? = null
        fun get(context: Context): BlockRepository = INSTANCE ?: synchronized(this) {
            val database = Room.databaseBuilder(context.applicationContext, LocalDatabase::class.java, "blocker.db").build()
            BlockRepository(database).also { INSTANCE = it }
        }
    }

    suspend fun loadBlocks(): List<Block> = withContext(Dispatchers.IO) {
        val rules = db.ruleDao().getAll()
        rules.map { r ->
            val schedules = db.scheduleDao().forRule(r.id)
            Block(
                id = r.id.toString(),
                name = r.name,
                appIds = r.apps.split(',').filter { it.isNotBlank() },
                websites = r.websites.split(',').filter { it.isNotBlank() },
                keywords = r.keywords.split(',').filter { it.isNotBlank() },
                fullBlock = r.fullBlock,
                weeklySchedule = schedules.groupBy { it.weekday }.mapValues { (_, list) ->
                    list.flatMap { it.ranges.split(',').filter { s -> s.contains('-') }.map { s ->
                        val (a,b) = s.split('-').map { it.toInt() }; IntRange(a,b)
                    } }
                },
                dailyLimitMinutes = r.dailyLimit,
                hourlyLimitMinutes = r.hourlyLimit
            )
        }
    }

    suspend fun saveBlock(block: Block) = withContext(Dispatchers.IO) {
        val rule = RuleEntity(
            name = block.name,
            apps = block.appIds.joinToString(","),
            websites = block.websites.joinToString(","),
            keywords = block.keywords.joinToString(","),
            fullBlock = block.fullBlock,
            dailyLimit = block.dailyLimitMinutes,
            hourlyLimit = block.hourlyLimitMinutes
        )
        val id = db.ruleDao().insert(rule)
        val schedules = block.weeklySchedule.flatMap { (day, ranges) ->
            ranges.map { r -> ScheduleEntity(ruleId = id, weekday = day, ranges = "${'$'}{r.first}-${'$'}{r.last}") }
        }
        if (schedules.isNotEmpty()) db.scheduleDao().insertAll(*schedules.toTypedArray())
    }
}
