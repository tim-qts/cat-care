package com.tim.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tim.data.model.CatDailyLogEntity
import com.tim.data.model.CatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cats ORDER BY displayOrder ASC, createdAt ASC")
    fun getAllCats(): Flow<List<CatEntity>>

    @Query("SELECT * FROM cats ORDER BY displayOrder ASC, createdAt ASC")
    suspend fun getAllCatsSync(): List<CatEntity>

    @Query("SELECT * FROM cats WHERE id = :id")
    suspend fun getCatById(id: Int): CatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: CatEntity): Long

    @Update
    suspend fun updateCat(cat: CatEntity)

    @Update
    suspend fun updateCats(cats: List<CatEntity>)

    @Query("DELETE FROM cats WHERE id = :catId")
    suspend fun deleteCat(catId: Int)

    @Query("SELECT COUNT(*) FROM cats")
    suspend fun getCatCount(): Int
}

@Dao
interface CatLogDao {
    @Query("SELECT * FROM cat_daily_logs WHERE catId = :catId AND dateString = :dateString")
    fun getLogForCatAndDate(catId: Int, dateString: String): Flow<CatDailyLogEntity?>

    @Query("SELECT * FROM cat_daily_logs WHERE catId = :catId AND dateString = :dateString")
    suspend fun getLogForCatAndDateSync(catId: Int, dateString: String): CatDailyLogEntity?

    @Query("SELECT * FROM cat_daily_logs WHERE dateString = :dateString")
    fun getAllLogsForDate(dateString: String): Flow<List<CatDailyLogEntity>>

    @Query("SELECT * FROM cat_daily_logs WHERE catId = :catId ORDER BY dateString DESC")
    fun getLogsForCat(catId: Int): Flow<List<CatDailyLogEntity>>

    @Query("SELECT * FROM cat_daily_logs WHERE catId = :catId ORDER BY dateString DESC")
    suspend fun getLogsForCatSync(catId: Int): List<CatDailyLogEntity>

    @Query("SELECT * FROM cat_daily_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<CatDailyLogEntity>>

    @Query("SELECT * FROM cat_daily_logs ORDER BY dateString DESC")
    suspend fun getAllLogsSync(): List<CatDailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: CatDailyLogEntity)

    @Query("DELETE FROM cat_daily_logs WHERE catId = :catId")
    suspend fun deleteLogsForCat(catId: Int)
}
