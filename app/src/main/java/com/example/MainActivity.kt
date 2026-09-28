package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppDestination
import com.example.ui.CirqubitViewModel
import com.example.ui.screens.aitutor.AiTutorScreen
import com.example.ui.screens.calculator.CalculatorsScreen
import com.example.ui.screens.challenges.ChallengesScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.designer.CircuitDesignerScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.lessons.LessonsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.CirqubitTheme
import com.example.ui.theme.LedGreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CirqubitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val student by viewModel.studentProfile.collectAsStateWithLifecycle()
            val lessons by viewModel.lessons.collectAsStateWithLifecycle()
            val challenges by viewModel.challenges.collectAsStateWithLifecycle()
            val projects by viewModel.projects.collectAsStateWithLifecycle()
            val kitComponents by viewModel.kitComponents.collectAsStateWithLifecycle()
            val achievements by viewModel.achievements.collectAsStateWithLifecycle()
            val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
            val designerComps by viewModel.designerComponents.collectAsStateWithLifecycle()
            val designerWires by viewModel.designerWires.collectAsStateWithLifecycle()
            val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
            val selectedLessonId by viewModel.selectedLessonId.collectAsStateWithLifecycle()
            val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
            val tutorMessages by viewModel.tutorMessages.collectAsStateWithLifecycle()
            val isTutorThinking by viewModel.isTutorThinking.collectAsStateWithLifecycle()
            val tutorMode by viewModel.tutorMode.collectAsStateWithLifecycle()
            val leaderboard by viewModel.leaderboardEntries.collectAsStateWithLifecycle()
            val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(notificationMessage) {
                if (notificationMessage != null) {
                    snackbarHostState.showSnackbar(
                        message = notificationMessage!!,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.dismissNotification()
                }
            }

            // Back handler: pop to Dashboard if on another screen
            BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
                viewModel.navigateTo(AppDestination.DASHBOARD)
            }

            CirqubitTheme(darkTheme = student.isDarkMode) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(300.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Header
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = "Cirqubit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Cirqubit",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Learn. Build. Experiment.",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                // Helper Composable for Drawer Navigation Items
                                val navItem: @Composable (AppDestination, androidx.compose.ui.graphics.vector.ImageVector) -> Unit = { dest, icon ->
                                    val isSelected = currentDestination == dest
                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(imageVector = icon, contentDescription = dest.title)
                                        },
                                        label = {
                                            Text(
                                                text = dest.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.navigateTo(dest)
                                            scope.launch { drawerState.close() }
                                        },
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }

                                // 1. LEARNING SECTION
                                Text(
                                    text = "LEARNING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                )
                                navItem(AppDestination.DASHBOARD, Icons.Default.Dashboard)
                                navItem(AppDestination.LESSONS, Icons.Default.School)
                                navItem(AppDestination.CHALLENGES, Icons.Default.Quiz)
                                navItem(AppDestination.PROJECTS, Icons.Default.Build)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // 2. TOOLS & WORKBENCH SECTION
                                Text(
                                    text = "WORKBENCH & TOOLS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                )
                                navItem(AppDestination.DESIGNER, Icons.Default.Polyline)
                                navItem(AppDestination.CALCULATOR, Icons.Default.Calculate)
                                navItem(AppDestination.AI_TUTOR, Icons.Default.Psychology)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // 3. COMMUNITY & PROGRESS
                                Text(
                                    text = "COMMUNITY & PROGRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                )
                                navItem(AppDestination.LEADERBOARD, Icons.Default.Leaderboard)
                                navItem(AppDestination.PROFILE, Icons.Default.AccountCircle)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // 4. APP PREFERENCES
                                Text(
                                    text = "PREFERENCES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                )
                                navItem(AppDestination.SETTINGS, Icons.Default.Settings)
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            CirqubitTopAppBar(
                                currentDestination = currentDestination,
                                studentXp = student.xp,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        },
                        bottomBar = {
                            CirqubitBottomNavigationBar(
                                currentDestination = currentDestination,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDestination) {
                                AppDestination.DASHBOARD -> DashboardScreen(
                                    student = student,
                                    lessons = lessons,
                                    projects = projects,
                                    achievements = achievements,
                                    leaderboard = leaderboard,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onSelectLesson = { viewModel.selectLesson(it) },
                                    onSelectProject = { viewModel.selectProject(it) }
                                )
                                AppDestination.LESSONS -> LessonsScreen(
                                    lessons = lessons,
                                    selectedLessonId = selectedLessonId,
                                    onSelectLesson = { viewModel.selectLesson(it) },
                                    onCompleteLesson = { viewModel.completeLesson(it) },
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                AppDestination.CHALLENGES -> ChallengesScreen(
                                    challenges = challenges,
                                    onSubmitChallenge = { id, opt, cb ->
                                        viewModel.submitChallenge(id, opt, cb)
                                    },
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                AppDestination.PROJECTS -> ProjectsScreen(
                                    projects = projects,
                                    kitComponents = kitComponents,
                                    selectedProjectId = selectedProjectId,
                                    onSelectProject = { viewModel.selectProject(it) },
                                    onStartBuild = { viewModel.startProjectBuild(it) },
                                    onTestCircuit = { id, cb -> viewModel.testProjectCircuit(id, cb) },
                                    onSubmitProject = { viewModel.submitProject(it) },
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                AppDestination.DESIGNER -> CircuitDesignerScreen(
                                    components = designerComps,
                                    wires = designerWires,
                                    onAddComponent = { type, label, value ->
                                        viewModel.addDesignerComponent(type, label, value)
                                    },
                                    onRemoveComponent = { viewModel.removeDesignerComponent(it) },
                                    onRotateComponent = { viewModel.rotateDesignerComponent(it) },
                                    onUpdatePosition = { id, x, y ->
                                        viewModel.updateDesignerPosition(id, x, y)
                                    },
                                    onClearWorkspace = { viewModel.clearDesigner() },
                                    onCheckCircuit = { viewModel.checkCircuit() },
                                    onAddWire = { fromId, fromPin, toId, toPin ->
                                        viewModel.addDesignerWire(fromId, fromPin, toId, toPin)
                                    },
                                    onDeleteWire = { viewModel.deleteDesignerWire(it) },
                                    onDuplicateComponent = { viewModel.duplicateDesignerComponent(it) },
                                    onUpdateComponentValue = { id, value ->
                                        viewModel.updateDesignerComponentValue(id, value)
                                    },
                                    onRestoreState = { comps, wrs ->
                                        viewModel.setDesignerState(comps, wrs)
                                    }
                                )
                                AppDestination.CALCULATOR -> CalculatorsScreen()
                                AppDestination.AI_TUTOR -> AiTutorScreen(
                                    student = student,
                                    messages = tutorMessages,
                                    isThinking = isTutorThinking,
                                    tutorMode = tutorMode,
                                    onSetMode = { viewModel.setTutorMode(it) },
                                    onSendMessage = { viewModel.sendTutorQuery(it) }
                                )
                                AppDestination.LEADERBOARD -> LeaderboardScreen(entries = leaderboard)
                                AppDestination.PROFILE -> ProfileScreen(
                                    student = student,
                                    achievements = achievements,
                                    projects = projects
                                )
                                AppDestination.SETTINGS -> SettingsScreen(
                                    student = student,
                                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                    onToggleHaptic = { viewModel.toggleHapticFeedback(it) },
                                    onResetProgress = { viewModel.resetStudentProgress() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CirqubitTopAppBar(
    currentDestination: AppDestination,
    studentXp: Int,
    onOpenDrawer: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cirqubit",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                if (currentDestination != AppDestination.DASHBOARD) {
                    Text(
                        text = " • ${currentDestination.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("menu_button")) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            // Live Compact XP Indicator
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CircuitGold.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CircuitGold.copy(alpha = 0.35f)),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = CircuitGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$studentXp XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CircuitGold
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun CirqubitBottomNavigationBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    val navItems = listOf(
        AppDestination.DASHBOARD to Icons.Default.Dashboard,
        AppDestination.LESSONS to Icons.Default.School,
        AppDestination.CHALLENGES to Icons.Default.Quiz,
        AppDestination.PROJECTS to Icons.Default.Build,
        AppDestination.DESIGNER to Icons.Default.Polyline
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        navItems.forEach { (dest, icon) ->
            val isSelected = currentDestination == dest
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = dest.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = dest.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(dest) },
                modifier = Modifier.testTag("nav_${dest.name.lowercase()}")
            )
        }
    }
}
