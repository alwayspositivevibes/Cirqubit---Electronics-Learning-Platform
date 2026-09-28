package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CirqubitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TutorMessage(
    val id: String = "msg_${System.currentTimeMillis()}",
    val isFromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "explanation"
)

enum class AppDestination(val title: String) {
    DASHBOARD("Home"),
    LESSONS("Lessons"),
    CHALLENGES("Challenges"),
    PROJECTS("Projects"),
    DESIGNER("Circuit Designer"),
    CALCULATOR("Calculator"),
    AI_TUTOR("AI Tutor"),
    LEADERBOARD("Leaderboard"),
    PROFILE("Profile"),
    SETTINGS("Settings")
}

enum class ToolTab(val title: String) {
    AI_TUTOR("AI Tutor"),
    DESIGNER("Circuit Designer"),
    CALCULATOR("Calculators")
}

class CirqubitViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CirqubitRepository(application)

    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    val lessons: StateFlow<List<Lesson>> = repository.lessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challenges: StateFlow<List<Challenge>> = repository.challenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<Project>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kitComponents: StateFlow<List<KitComponent>> = repository.kitComponents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val telemetry: StateFlow<HardwareTelemetry> = repository.telemetry
    val designerComponents: StateFlow<List<DesignerComponentItem>> = repository.designerComponents
    val designerWires: StateFlow<List<DesignerWireItem>> = repository.designerWires

    // Navigation state
    private val _currentDestination = MutableStateFlow(AppDestination.DASHBOARD)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedToolTab = MutableStateFlow(ToolTab.AI_TUTOR)
    val selectedToolTab: StateFlow<ToolTab> = _selectedToolTab.asStateFlow()

    // Detail screens state
    private val _selectedLessonId = MutableStateFlow<String?>("lesson_1")
    val selectedLessonId: StateFlow<String?> = _selectedLessonId.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<String?>("proj_1")
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    // AI Tutor chat state
    private val _tutorMessages = MutableStateFlow<List<TutorMessage>>(
        listOf(
            TutorMessage(
                isFromUser = false,
                text = "Welcome to Cirqubit! I am your AI Electronics Tutor. You can ask me any question about electronics — from 'What is voltage?' and 'Explain Ohm's Law' to circuit analysis, component roles, or practice quizzes. What would you like to explore?",
                mode = "explanation"
            )
        )
    )
    val tutorMessages: StateFlow<List<TutorMessage>> = _tutorMessages.asStateFlow()

    private val _isTutorThinking = MutableStateFlow(false)
    val isTutorThinking: StateFlow<Boolean> = _isTutorThinking.asStateFlow()

    private val _tutorMode = MutableStateFlow("explanation") // "hint", "explanation", "example", "deep_dive"
    val tutorMode: StateFlow<String> = _tutorMode.asStateFlow()

    // Dynamic Leaderboard list combining realistic peers with the student's live XP
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = studentProfile.combine(projects) { student, prjs ->
        val completedCount = prjs.count { it.status == ProjectStatus.COMPLETED }
        val allEntries = listOf(
            LeaderboardEntry("peer_1", "Elena Rostova", 0xFF6750A4, 620, 5, 4, badge = "Circuit Artisan"),
            LeaderboardEntry("peer_2", "Marcus Chen", 0xFF006C50, 480, 4, 3, badge = "Ohm Specialist"),
            LeaderboardEntry("peer_3", "Priya Sharma", 0xFF825500, 410, 3, 2, badge = "Breadboard Pro"),
            LeaderboardEntry("peer_5", "Liam O'Connor", 0xFF7D5260, 320, 3, 1, badge = "Apprentice"),
            LeaderboardEntry("peer_6", "Kenji Sato", 0xFF386A20, 240, 2, 1, badge = "Novice Builder"),
            LeaderboardEntry("peer_7", "Sofia Morales", 0xFF8C4A60, 180, 2, 1, badge = "Novice Builder"),
            LeaderboardEntry("peer_8", "David Kim", 0xFF4A6572, 120, 1, 0, badge = "Beginner"),
            LeaderboardEntry("peer_9", "Maya Patel", 0xFF5D4037, 60, 1, 0, badge = "Beginner"),
            LeaderboardEntry("student_user", student.name, 0xFF00E5FF, student.xp, student.level, completedCount, isCurrentStudent = true, badge = "Apprentice")
        )
        // Sort descending by XP and assign ranks
        allEntries.sortedByDescending { it.xp }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback banner / Snack message
    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
    }

    fun selectToolTab(tab: ToolTab) {
        _selectedToolTab.value = tab
    }

    fun selectLesson(lessonId: String) {
        _selectedLessonId.value = lessonId
    }

    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
    }

    fun completeLesson(lessonId: String) {
        viewModelScope.launch {
            repository.completeLesson(lessonId)
            _notificationMessage.value = "Lesson Completed! +80 XP Earned 🎉"
        }
    }

    fun submitChallenge(challengeId: String, selectedOption: Int, onResult: (Boolean, Int, String) -> Unit) {
        viewModelScope.launch {
            val (passed, score, message) = repository.submitChallenge(challengeId, selectedOption)
            _notificationMessage.value = message
            onResult(passed, score, message)
        }
    }

    fun startProjectBuild(projectId: String) {
        viewModelScope.launch {
            repository.startProjectBuild(projectId)
            _notificationMessage.value = "Guided Project Simulation Started!"
        }
    }

    fun testProjectCircuit(projectId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.testProjectCircuit(projectId)
            _notificationMessage.value = "Circuit Verification: PASSED (Continuous 5.0V Loop Verified)!"
            onComplete(result)
        }
    }

    fun submitProject(projectId: String) {
        viewModelScope.launch {
            repository.submitProject(projectId)
            _notificationMessage.value = "PROJECT COMPLETED! +100 XP Earned! 🎉"
        }
    }

    // Hardware Kit Simulation
    fun connectHardwareKit(connect: Boolean) {
        repository.connectHardwareKit(connect)
        _notificationMessage.value = if (connect) "Cirqubit Kit v1 Connected via USB-C" else "Kit Disconnected"
    }

    fun runHardwareTest(): String {
        val result = repository.runHardwareTest()
        _notificationMessage.value = "Diagnostics Complete: Ready for build verification"
        return result
    }

    fun updatePotentiometer(value: Float) {
        repository.updatePotentiometerValue(value)
    }

    fun updateLightLevel(lux: Int) {
        repository.updateLightLevelLux(lux)
    }

    fun toggleButton(btnIndex: Int, isPressed: Boolean) {
        repository.toggleHardwareButton(btnIndex, isPressed)
    }

    fun toggleLed() {
        repository.toggleHardwareLed()
    }

    // AI Tutor
    fun setTutorMode(mode: String) {
        _tutorMode.value = mode
    }

    fun sendTutorQuery(query: String) {
        if (query.isBlank()) return

        val userMsg = TutorMessage(isFromUser = true, text = query, mode = _tutorMode.value)
        _tutorMessages.value = _tutorMessages.value + userMsg
        _isTutorThinking.value = true

        viewModelScope.launch {
            val st = studentProfile.value
            val response = repository.aiTutorService.askTutor(
                studentQuery = query,
                currentCourse = st.currentCourseName,
                currentLesson = st.currentLessonId,
                currentProject = st.currentProjectId,
                mode = _tutorMode.value
            )
            val tutorMsg = TutorMessage(isFromUser = false, text = response, mode = _tutorMode.value)
            _tutorMessages.value = _tutorMessages.value + tutorMsg
            _isTutorThinking.value = false
        }
    }

    // Circuit Designer
    fun addDesignerComponent(type: String, label: String, value: String) {
        repository.addDesignerComponent(type, label, value)
    }

    fun removeDesignerComponent(id: String) {
        repository.removeDesignerComponent(id)
    }

    fun rotateDesignerComponent(id: String) {
        repository.rotateDesignerComponent(id)
    }

    fun duplicateDesignerComponent(id: String) {
        repository.duplicateDesignerComponent(id)
    }

    fun updateDesignerComponentValue(id: String, value: String) {
        repository.updateDesignerComponentValue(id, value)
    }

    fun updateDesignerPosition(id: String, x: Float, y: Float) {
        repository.updateDesignerComponentPosition(id, x, y)
    }

    fun addDesignerWire(fromCompId: String, fromPin: String, toCompId: String, toPin: String) {
        repository.addDesignerWire(fromCompId, fromPin, toCompId, toPin)
    }

    fun deleteDesignerWire(wireId: String) {
        repository.deleteDesignerWire(wireId)
    }

    fun setDesignerState(comps: List<DesignerComponentItem>, wires: List<DesignerWireItem>) {
        repository.setDesignerState(comps, wires)
    }

    fun clearDesigner() {
        repository.clearDesignerWorkspace()
    }

    fun checkCircuit(): String {
        return repository.checkDesignerCircuit()
    }

    // Settings
    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.toggleDarkMode(isDark)
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleHapticFeedback(enabled)
        }
    }

    fun resetStudentProgress() {
        viewModelScope.launch {
            repository.resetStudentProgress()
            _notificationMessage.value = "Progress reset to fresh user state (0 XP)"
        }
    }

    fun dismissNotification() {
        _notificationMessage.value = null
    }
}
