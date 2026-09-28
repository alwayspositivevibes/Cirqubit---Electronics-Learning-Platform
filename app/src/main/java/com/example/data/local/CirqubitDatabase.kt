package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentEntity::class,
        LessonEntity::class,
        ChallengeEntity::class,
        ProjectEntity::class,
        KitComponentEntity::class,
        AchievementEntity::class,
        SavedCircuitEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CirqubitDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun projectDao(): ProjectDao
    abstract fun kitDao(): KitDao
    abstract fun achievementDao(): AchievementDao
    abstract fun circuitDao(): CircuitDao

    companion object {
        @Volatile
        private var INSTANCE: CirqubitDatabase? = null

        fun getInstance(context: Context): CirqubitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CirqubitDatabase::class.java,
                    "cirqubit_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
