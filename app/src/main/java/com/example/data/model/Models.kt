package com.example.data.model

enum class ProgressStatus {
    LOCKED,
    UNLOCKED,
    IN_PROGRESS,
    COMPLETED
}

enum class ProjectStatus {
    LOCKED,
    UNLOCKED,
    IN_PROGRESS,
    TESTED,
    COMPLETED
}

enum class ChallengeType {
    MULTIPLE_CHOICE,
    COMPONENT_IDENTIFICATION,
    CIRCUIT_IDENTIFICATION,
    CALCULATION,
    TROUBLESHOOTING,
    PRACTICAL_HARDWARE
}

enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class KitComponentType {
    MICROCONTROLLER,
    LED,
    RESISTOR,
    PUSH_BUTTON,
    POTENTIOMETER,
    LDR,
    CAPACITOR,
    DIODE,
    TRANSISTOR,
    BUZZER,
    BREADBOARD,
    USB_CABLE
}

data class KitComponent(
    val id: String,
    val name: String,
    val type: KitComponentType,
    val spec: String,
    val totalQuantity: Int,
    val inUseQuantity: Int,
    val availableQuantity: Int,
    val symbolCode: String
)

data class Lesson(
    val id: String,
    val order: Int,
    val title: String,
    val subtitle: String,
    val estimatedMinutes: Int,
    val xpReward: Int,
    val objectives: List<String>,
    val conceptExplanation: String,
    val visualDiagramType: String,
    val circuitDiagramCode: String,
    val componentExplanation: String,
    val importantFormula: String,
    val formulaExplanation: String,
    val workedExample: String,
    val keyTakeaways: List<String>,
    val status: ProgressStatus = ProgressStatus.LOCKED,
    val completedAt: Long? = null
)

data class Challenge(
    val id: String,
    val lessonId: String,
    val title: String,
    val type: ChallengeType,
    val question: String,
    val visualCode: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val hint: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val studentScore: Int = 0
)

data class Project(
    val id: String,
    val order: Int,
    val title: String,
    val objective: String,
    val difficulty: DifficultyLevel,
    val conceptsLearned: List<String>,
    val requiredComponents: Map<KitComponentType, Int>,
    val requiredSpecs: String,
    val circuitDiagramCode: String,
    val assemblySteps: List<String>,
    val testingInstructions: List<String>,
    val expectedResult: String,
    val status: ProjectStatus = ProjectStatus.LOCKED,
    val xpReward: Int = 100,
    val completedAt: Long? = null
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

data class LeaderboardEntry(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val xp: Int,
    val level: Int,
    val completedProjects: Int,
    val rank: Int = 0,
    val isCurrentStudent: Boolean = false,
    val badge: String = "Apprentice"
)

data class StudentProfile(
    val id: String = "cirqubit_student_1",
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

data class HardwareTelemetry(
    val isConnected: Boolean = false,
    val deviceName: String = "Cirqubit Kit v1 (USB-C)",
    val firmwareVersion: String = "v1.4.2-preview",
    val potentiometerValue: Float = 0.45f, // 0.0 to 1.0
    val lightLevelLux: Int = 420,
    val button1Pressed: Boolean = false,
    val button2Pressed: Boolean = false,
    val ledOutputActive: Boolean = true,
    val buzzerActive: Boolean = false,
    val supplyVoltage: Float = 5.02f,
    val currentDrawMa: Float = 18.4f,
    val lastTestResult: String = "Ready for Test"
)

data class DesignerComponentItem(
    val id: String,
    val type: String, // BATTERY, RESISTOR, LED, CAPACITOR, DIODE, SWITCH, PUSH_BUTTON, POTENTIOMETER, LDR, TRANSISTOR, BUZZER, GROUND
    val label: String,
    val value: String,
    val x: Float,
    val y: Float,
    val rotationDeg: Int = 0
)

data class DesignerWireItem(
    val id: String,
    val fromComponentId: String,
    val fromPin: String, // "pin1", "pin2", "anode", "cathode", "pos", "neg"
    val toComponentId: String,
    val toPin: String
)
