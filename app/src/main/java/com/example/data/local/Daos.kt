package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_profile WHERE id = :id LIMIT 1")
    fun getStudent(id: String = "cirqubit_student_1"): Flow<StudentEntity?>

    @Query("SELECT * FROM student_profile WHERE id = :id LIMIT 1")
    suspend fun getStudentDirect(id: String = "cirqubit_student_1"): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM student_profile")
    suspend fun deleteAllStudents()
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    suspend fun getAllLessonsDirect(): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateLessonStatus(id: String, status: String, completedAt: Long?)

    @Query("DELETE FROM lessons")
    suspend fun deleteAllLessons()
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY lessonId ASC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE lessonId = :lessonId")
    fun getChallengesForLesson(lessonId: String): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("UPDATE challenges SET isCompleted = :isCompleted, studentScore = :score WHERE id = :id")
    suspend fun updateChallengeResult(id: String, isCompleted: Boolean, score: Int)

    @Query("DELETE FROM challenges")
    suspend fun deleteAllChallenges()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY orderIndex ASC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateProjectStatus(id: String, status: String, completedAt: Long?)

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()
}

@Dao
interface KitDao {
    @Query("SELECT * FROM kit_components ORDER BY name ASC")
    fun getAllKitComponents(): Flow<List<KitComponentEntity>>

    @Query("SELECT * FROM kit_components ORDER BY name ASC")
    suspend fun getAllKitComponentsDirect(): List<KitComponentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitComponents(components: List<KitComponentEntity>)

    @Update
    suspend fun updateKitComponent(component: KitComponentEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun unlockAchievement(id: String, unlockedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()
}

@Dao
interface CircuitDao {
    @Query("SELECT * FROM saved_circuits ORDER BY createdAt DESC")
    fun getAllSavedCircuits(): Flow<List<SavedCircuitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCircuit(circuit: SavedCircuitEntity)

    @Query("DELETE FROM saved_circuits WHERE id = :id")
    suspend fun deleteCircuit(id: String)
}
