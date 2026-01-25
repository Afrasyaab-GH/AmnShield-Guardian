package org.alhaq.deenshield.guardian.data

import android.content.Context
import androidx.room.Room
import org.alhaq.deenshield.guardian.model.Block
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlockRepository private constructor(private val db: LocalDatabase) {

    companion object {
        @Volatile 
        private var INSTANCE: BlockRepository? = null
        
        fun get(context: Context): BlockRepository {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            return synchronized(this) {
                val instance = INSTANCE
                if (instance != null) {
                    instance
                } else {
                    val database = Room.databaseBuilder(context.applicationContext, LocalDatabase::class.java, "blocker.db").build()
                    BlockRepository(database).also { INSTANCE = it }
                }
            }
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
        // Validate block data before saving
        if (block.name.isBlank()) {
            throw IllegalArgumentException("Block name cannot be empty")
        }
        
        // Validate and clean domains
        val (validDomains, domainErrors) = org.alhaq.deenshield.guardian.util.BlockUtils.validateDomainList(
            block.websites.joinToString(",")
        )
        if (validDomains.isEmpty() && block.websites.isNotEmpty()) {
            android.util.Log.w("BlockRepository", "Domain validation errors: $domainErrors")
        }
        
        // Validate and clean keywords
        val (validKeywords, keywordErrors) = org.alhaq.deenshield.guardian.util.BlockUtils.validateKeywordList(
            block.keywords.joinToString(",")
        )
        if (validKeywords.isEmpty() && block.keywords.isNotEmpty()) {
            android.util.Log.w("BlockRepository", "Keyword validation errors: $keywordErrors")
        }
        
        val rule = RuleEntity(
            name = block.name.trim(),
            apps = block.appIds.joinToString(","),
            websites = validDomains.joinToString(","),
            keywords = validKeywords.joinToString(","),
            fullBlock = block.fullBlock,
            dailyLimit = block.dailyLimitMinutes,
            hourlyLimit = block.hourlyLimitMinutes
        )
        val id = db.ruleDao().insert(rule)
        val schedules = block.weeklySchedule.flatMap { (day, ranges) ->
            ranges.map { r -> ScheduleEntity(ruleId = id, weekday = day, ranges = "${'$'}{r.first}-${'$'}{r.last}") }
        }
        if (schedules.isNotEmpty()) db.scheduleDao().insertAll(*schedules.toTypedArray())
        
        android.util.Log.d("BlockRepository", "Saved block: ${block.name} with ${validDomains.size} domains and ${validKeywords.size} keywords")
    }
}
