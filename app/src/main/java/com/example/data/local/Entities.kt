package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChallengeType
import com.example.data.model.DifficultyLevel
import com.example.data.model.KitComponentType
import com.example.data.model.ProgressStatus
import com.example.data.model.ProjectStatus

@Entity(tableName = "student_profile")
data class StudentEntity(
    @PrimaryKey val id: String = "cirqubit_student_1",
    val name: String = "Kishan Gopa",
    val level: Int = 1,
    val xp: Int = 0,
    val streakDays: Int = 0,
    val completedLessonsCount: Int = 0,
    val completedChallengesCount: Int = 0,
    val completedProjectsCount: Int = 0,
    val currentCourseName: String = "Electronic Fundamentals",
    val currentLessonId: String = "lesson_1",
    val currentProjectId: String = "proj_1",
    val isDarkMode: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val orderIndex: Int,
    val title: String,
    val subtitle: String,
    val estimatedMinutes: Int,
    val xpReward: Int,
    val objectivesJson: String,
    val conceptExplanation: String,
    val visualDiagramType: String,
    val circuitDiagramCode: String,
    val componentExplanation: String,
    val importantFormula: String,
    val formulaExplanation: String,
    val workedExample: String,
    val keyTakeawaysJson: String,
    val status: String, // LOCKED, UNLOCKED, IN_PROGRESS, COMPLETED
    val completedAt: Long?
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val title: String,
    val type: String, // MULTIPLE_CHOICE, COMPONENT_IDENTIFICATION, etc.
    val question: String,
    val visualCode: String?,
    val optionsJson: String,
    val correctIndex: Int,
    val explanation: String,
    val hint: String,
    val xpReward: Int,
    val isCompleted: Boolean,
    val studentScore: Int
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val orderIndex: Int,
    val title: String,
    val objective: String,
    val difficulty: String,
    val conceptsLearnedJson: String,
    val requiredComponentsJson: String,
    val requiredSpecs: String,
    val circuitDiagramCode: String,
    val assemblyStepsJson: String,
    val testingInstructionsJson: String,
    val expectedResult: String,
    val status: String, // LOCKED, UNLOCKED, IN_PROGRESS, TESTED, COMPLETED
    val xpReward: Int,
    val completedAt: Long?
)

@Entity(tableName = "kit_components")
data class KitComponentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val spec: String,
    val totalQuantity: Int,
    val inUseQuantity: Int,
    val symbolCode: String
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

@Entity(tableName = "saved_circuits")
data class SavedCircuitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val componentsJson: String,
    val wiresJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
