package com.tim.data.repository

import com.tim.data.db.CatDao
import com.tim.data.db.CatLogDao
import com.tim.data.model.CatDailyLogEntity
import com.tim.data.model.CatEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CatRepository(
    private val catDao: CatDao,
    private val catLogDao: CatLogDao
) {
    val allCats: Flow<List<CatEntity>> = catDao.getAllCats()

    fun getLogsForDate(dateString: String): Flow<List<CatDailyLogEntity>> {
        return catLogDao.getAllLogsForDate(dateString)
    }

    fun getLogForCatAndDate(catId: Int, dateString: String): Flow<CatDailyLogEntity?> {
        return catLogDao.getLogForCatAndDate(catId, dateString)
    }

    fun getLogsForCat(catId: Int): Flow<List<CatDailyLogEntity>> {
        return catLogDao.getLogsForCat(catId)
    }

    suspend fun toggleTask(
        catId: Int,
        dateString: String,
        taskType: TaskType,
        newValue: Boolean? = null
    ) {
        val existingLog = catLogDao.getLogForCatAndDateSync(catId, dateString)
            ?: CatDailyLogEntity(catId = catId, dateString = dateString)

        val updatedLog = when (taskType) {
            TaskType.MORNING_MEAL -> existingLog.copy(
                morningMeal = newValue ?: !existingLog.morningMeal,
                updatedAt = System.currentTimeMillis()
            )
            TaskType.EVENING_MEAL -> existingLog.copy(
                eveningMeal = newValue ?: !existingLog.eveningMeal,
                updatedAt = System.currentTimeMillis()
            )
            TaskType.POOP -> existingLog.copy(
                poop = newValue ?: !existingLog.poop,
                updatedAt = System.currentTimeMillis()
            )
            TaskType.MEDICATION -> existingLog.copy(
                medication = newValue ?: !existingLog.medication,
                updatedAt = System.currentTimeMillis()
            )
        }

        catLogDao.upsertLog(updatedLog)
    }

    suspend fun updateDailyNotes(catId: Int, dateString: String, notes: String) {
        val existingLog = catLogDao.getLogForCatAndDateSync(catId, dateString)
            ?: CatDailyLogEntity(catId = catId, dateString = dateString)

        catLogDao.upsertLog(existingLog.copy(notes = notes, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addCat(
        name: String,
        breed: String,
        colorHex: String,
        avatarEmoji: String,
        requiresMedication: Boolean = false,
        medicationNotes: String = ""
    ): Long {
        val currentCount = catDao.getCatCount()
        return catDao.insertCat(
            CatEntity(
                name = name,
                breed = breed,
                colorHex = colorHex,
                avatarEmoji = avatarEmoji,
                requiresMedication = requiresMedication,
                medicationNotes = if (requiresMedication) medicationNotes else "",
                displayOrder = currentCount
            )
        )
    }

    suspend fun updateCat(cat: CatEntity) {
        catDao.updateCat(cat)
    }

    suspend fun reorderCats(reorderedList: List<CatEntity>) {
        val updatedCats = reorderedList.mapIndexed { index, cat ->
            cat.copy(displayOrder = index)
        }
        catDao.updateCats(updatedCats)
    }

    suspend fun deleteCat(catId: Int) {
        catLogDao.deleteLogsForCat(catId)
        catDao.deleteCat(catId)
    }

    suspend fun exportToCsv(selectedCatId: Int? = null): String {
        val allCatsList = catDao.getAllCatsSync()
        val catMap = allCatsList.associateBy { it.id }

        val logs = if (selectedCatId != null && selectedCatId > 0) {
            catLogDao.getLogsForCatSync(selectedCatId)
        } else {
            catLogDao.getAllLogsSync()
        }

        val sb = StringBuilder()
        sb.append("Cat Name,Breed,Date,Morning Meal,Evening Meal,Poop,Medication,Notes\n")

        for (log in logs) {
            val cat = catMap[log.catId]
            val catName = cat?.name ?: "Unknown Cat"
            val breed = cat?.breed ?: ""
            val morning = if (log.morningMeal) "Yes" else "No"
            val evening = if (log.eveningMeal) "Yes" else "No"
            val poop = if (log.poop) "Yes" else "No"
            val med = if (cat?.requiresMedication == true) {
                if (log.medication) "Yes" else "No"
            } else {
                "N/A"
            }
            val sanitizedNotes = log.notes.replace("\"", "\"\"")

            sb.append("\"$catName\",\"$breed\",\"${log.dateString}\",\"$morning\",\"$evening\",\"$poop\",\"$med\",\"$sanitizedNotes\"\n")
        }

        return sb.toString()
    }

    enum class TaskType {
        MORNING_MEAL,
        EVENING_MEAL,
        POOP,
        MEDICATION
    }
}
