package com.tim.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tim.data.model.CatDailyLogEntity
import com.tim.data.model.CatEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [CatEntity::class, CatDailyLogEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
    abstract fun catLogDao(): CatLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cat_care_tracker.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial data asynchronously on creation
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(getDatabase(context))
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val catDao = db.catDao()
            val catLogDao = db.catLogDao()

            if (catDao.getCatCount() == 0) {
                val cat1Id = catDao.insertCat(
                    CatEntity(
                        name = "Mochi",
                        breed = "Orange Tabby",
                        colorHex = "#FF8C38",
                        avatarEmoji = "🐱",
                        requiresMedication = true,
                        medicationNotes = "1 drop eye drops in morning",
                        displayOrder = 0
                    )
                ).toInt()

                val cat2Id = catDao.insertCat(
                    CatEntity(
                        name = "Luna",
                        breed = "Siamese Mix",
                        colorHex = "#2A9D8F",
                        avatarEmoji = "🐈",
                        requiresMedication = false,
                        medicationNotes = "",
                        displayOrder = 1
                    )
                ).toInt()

                // Seed logs for the past 7 days for historical trends
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()

                for (i in 0..6) {
                    val dateStr = dateFormat.format(calendar.time)
                    
                    // Sample logs for Mochi (requires medication)
                    catLogDao.upsertLog(
                        CatDailyLogEntity(
                            catId = cat1Id,
                            dateString = dateStr,
                            morningMeal = true,
                            eveningMeal = true,
                            poop = i % 2 == 0 || i == 0,
                            medication = true,
                            notes = if (i == 0) "Happy and playful today!" else ""
                        )
                    )

                    // Sample logs for Luna (no medication required)
                    catLogDao.upsertLog(
                        CatDailyLogEntity(
                            catId = cat2Id,
                            dateString = dateStr,
                            morningMeal = true,
                            eveningMeal = i != 3,
                            poop = true,
                            medication = false,
                            notes = if (i == 0) "Ate all breakfast!" else ""
                        )
                    )

                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }
            }
        }
    }
}
