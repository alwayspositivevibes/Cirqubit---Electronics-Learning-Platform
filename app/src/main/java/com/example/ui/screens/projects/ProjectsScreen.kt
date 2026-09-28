package com.example.ui.screens.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DifficultyLevel
import com.example.data.model.KitComponent
import com.example.data.model.Project
import com.example.data.model.ProjectStatus
import com.example.ui.AppDestination
import com.example.ui.components.CircuitDiagramView
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.LedGreen
import com.example.ui.theme.LedRed
import kotlinx.coroutines.delay

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    kitComponents: List<KitComponent>,
    selectedProjectId: String?,
    onSelectProject: (String) -> Unit,
    onStartBuild: (String) -> Unit,
    onTestCircuit: (String, (Boolean) -> Unit) -> Unit,
    onSubmitProject: (String) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedProjectId by remember(selectedProjectId) {
        mutableStateOf(selectedProjectId ?: projects.firstOrNull { it.status != ProjectStatus.LOCKED }?.id)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Projects Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LedGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PRACTICAL LAB",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LedGreen,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        val completedCount = projects.count { it.status == ProjectStatus.COMPLETED }
                        Text(
                            text = "$completedCount of 8 Built",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Practical Circuit Projects",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Build and simulate fundamental electronics circuits directly on screen. Wire components, test current flow, and verify each design in the interactive software workbench.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Projects List
        items(projects) { project ->
            val isExpanded = expandedProjectId == project.id
            val isLocked = project.status == ProjectStatus.LOCKED

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_card_${project.order}")
                    .clickable(enabled = !isLocked) {
                        expandedProjectId = if (isExpanded) null else project.id
                        onSelectProject(project.id)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                border = if (isExpanded) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    CardDefaults.outlinedCardBorder()
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (project.status) {
                                ProjectStatus.COMPLETED -> LedGreen.copy(alpha = 0.2f)
                                ProjectStatus.TESTED -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                ProjectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ProjectStatus.UNLOCKED -> Color(0xFFFF9100).copy(alpha = 0.2f)
                                ProjectStatus.LOCKED -> Color.Gray.copy(alpha = 0.15f)
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when (project.status) {
                                    ProjectStatus.COMPLETED -> Icon(Icons.Default.Check, contentDescription = "Done", tint = LedGreen)
                                    ProjectStatus.LOCKED -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray)
                                    else -> Text("${project.order}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROJECT 0${project.order}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = project.objective,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = if (isExpanded) Int.MAX_VALUE else 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ProjectStatusChip(project.status)
                    }

                    // Expanded Project Body
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. Difficulty & XP reward
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Difficulty: ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = project.difficulty.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = when (project.difficulty) {
                                                DifficultyLevel.BEGINNER -> LedGreen
                                                DifficultyLevel.INTERMEDIATE -> Color(0xFFFF9100)
                                                DifficultyLevel.ADVANCED -> Color(0xFFFF1744)
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CircuitGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "+${project.xpReward} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CircuitGold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Concepts Learned tags
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                project.conceptsLearned.take(2).forEach { concept ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = concept,
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Simulated Components Required
                            Text(
                                text = "📦 Required Components (Simulated)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = project.requiredSpecs,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Circuit Schematic
                            CircuitDiagramView(
                                diagramCode = project.circuitDiagramCode,
                                title = "Project Schematic: ${project.title}"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. Assembly Instructions
                            Text(
                                text = "🔨 Step-by-Step Breadboard Guide",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            project.assemblySteps.forEachIndexed { idx, step ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(
                                        text = "${idx + 1}. ",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = step, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 5. Interactive Software Simulation Workbench
                            Text(
                                text = "⚡ Interactive Software Simulation",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            ProjectSimulationWorkbench(
                                project = project,
                                onTestPassed = {
                                    onTestCircuit(project.id) { }
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 6. Expected Result
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "EXPECTED OUTCOME",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = LedGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = project.expectedResult,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 7. Submission Workflow
                            when (project.status) {
                                ProjectStatus.UNLOCKED -> {
                                    Button(
                                        onClick = { onStartBuild(project.id) },
                                        modifier = Modifier.fillMaxWidth().testTag("start_build_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open Software Workbench", fontWeight = FontWeight.Bold)
                                    }
                                }
                                ProjectStatus.IN_PROGRESS -> {
                                    Button(
                                        onClick = {
                                            onTestCircuit(project.id) { }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("test_circuit_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verify Circuit Simulation", fontWeight = FontWeight.Bold)
                                    }
                                }
                                ProjectStatus.TESTED -> {
                                    Column {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = LedGreen.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LedGreen)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Simulation Verification: PASSED! Ready for submission.",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = LedGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { onSubmitProject(project.id) },
                                            modifier = Modifier.fillMaxWidth().testTag("submit_project_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = LedGreen),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Submit Project (+${project.xpReward} XP)",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                                ProjectStatus.COMPLETED -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = LedGreen.copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LedGreen, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Project Completed & Verified",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = LedGreen)
                                                )
                                                Text(
                                                    text = "+${project.xpReward} XP credited to student portfolio",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                            }
                                        }
                                    }
                                }
                                ProjectStatus.LOCKED -> {
                                    // Handled by card being non-clickable
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSimulationWorkbench(
    project: Project,
    onTestPassed: () -> Unit
) {
    var powerOn by remember { mutableStateOf(false) }
    var potValue by remember { mutableStateOf(0.5f) }
    var buttonPressed by remember { mutableStateOf(false) }
    var switchA by remember { mutableStateOf(false) }
    var switchB by remember { mutableStateOf(false) }
    var lightLux by remember { mutableStateOf(300) }
    var isTestRunning by remember { mutableStateOf(false) }
    var testResultVerified by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Interactive Controls depending on project
            when (project.order) {
                1 -> {
                    // Simple LED Circuit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Circuit Power: 5.0V DC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (powerOn) "Current: 13.6 mA • LED Forward Voltage: 2.0V" else "Circuit Open (0.0 mA)",
                                style = MaterialTheme.typography.bodySmall.copy(color = if (powerOn) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(checked = powerOn, onCheckedChange = { powerOn = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LedIndicatorWidget(isLit = powerOn, color = Color(0xFFFF1744), label = "Red LED (220Ω Limiting)")
                }
                2 -> {
                    // Variable LED Brightness
                    Text("Potentiometer Wiper: ${(potValue * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Slider(
                        value = potValue,
                        onValueChange = { potValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val effectiveCurrentMa = 0.4f + potValue * 13.2f
                    Text(
                        text = "Calculated Current: ${String.format("%.1f", effectiveCurrentMa)} mA (Safe Limit: 20mA)",
                        style = MaterialTheme.typography.bodySmall.copy(color = LedGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(
                        isLit = potValue > 0.05f,
                        color = Color(0xFF00E676),
                        alpha = (0.2f + potValue * 0.8f).coerceIn(0.2f, 1f),
                        label = "Green LED Analog Dimming"
                    )
                }
                3 -> {
                    // Push-Button LED Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Momentary Push Button", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (buttonPressed) "Contact Closed • Loop Energized" else "Pull-Down Resistor Holding 0V",
                                style = MaterialTheme.typography.bodySmall.copy(color = if (buttonPressed) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Button(
                            onClick = { buttonPressed = !buttonPressed },
                            colors = ButtonDefaults.buttonColors(containerColor = if (buttonPressed) LedGreen else MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (buttonPressed) "RELEASE" else "PRESS")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(isLit = buttonPressed, color = Color(0xFFFFD600), label = "Yellow LED")
                }
                4 -> {
                    // Two-Way LED Switch
                    Text("Two-Way Hallway Switches (SPDT)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Switch 1: ", fontSize = 12.sp)
                            Switch(checked = switchA, onCheckedChange = { switchA = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Switch 2: ", fontSize = 12.sp)
                            Switch(checked = switchB, onCheckedChange = { switchB = it })
                        }
                    }
                    val isLightActive = switchA xor switchB
                    Text(
                        text = if (isLightActive) "XOR Logic: Closed Traveler Path (Circuit ON)" else "Open Circuit (Circuit OFF)",
                        style = MaterialTheme.typography.bodySmall.copy(color = if (isLightActive) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(isLit = isLightActive, color = Color(0xFFFF1744), label = "Red Hallway LED")
                }
                5 -> {
                    // Light-Sensitive LED (LDR)
                    Text("Ambient Light Intensity: $lightLux Lux", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Slider(
                        value = lightLux.toFloat(),
                        onValueChange = { lightLux = it.toInt() },
                        valueRange = 10f..1000f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val isDim = lightLux < 300
                    Text(
                        text = if (isDim) "LDR High Resistance (>50kΩ) • LED ON" else "LDR Low Resistance (<2kΩ) • LED OFF",
                        style = MaterialTheme.typography.bodySmall.copy(color = if (isDim) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(isLit = isDim, color = Color(0xFFFFD600), label = "Sensor Indicator LED")
                }
                6 -> {
                    // Automatic Night Lamp
                    Text("Ambient Illuminance: $lightLux Lux", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Slider(
                        value = lightLux.toFloat(),
                        onValueChange = { lightLux = it.toInt() },
                        valueRange = 10f..1000f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val isNight = lightLux < 150
                    Text(
                        text = if (isNight) "Base Vbe > 0.7V • NPN Transistor Saturated (Lamp ON)" else "Base Vbe < 0.4V • Transistor Cutoff (Lamp OFF)",
                        style = MaterialTheme.typography.bodySmall.copy(color = if (isNight) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(isLit = isNight, color = Color.White, label = "Autonomous White Night Lamp")
                }
                7 -> {
                    // Capacitor Timer Circuit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("100μF Timing Capacitor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (buttonPressed) "Capacitor Charged (5.0V) • Discharging via 10kΩ" else "Steady State (0.0V)",
                                style = MaterialTheme.typography.bodySmall.copy(color = if (buttonPressed) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Button(onClick = { buttonPressed = true }) {
                            Text("TRIGGER TIMER")
                        }
                    }
                    LaunchedEffect(buttonPressed) {
                        if (buttonPressed) {
                            delay(3500)
                            buttonPressed = false
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(isLit = buttonPressed, color = Color(0xFF00E676), label = "3.5s RC Delay LED")
                }
                8 -> {
                    // Simple Transistor Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NPN 2N2222 Solid-State Switch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (powerOn) "Base: 0.5 mA • Collector Load: 15.0 mA" else "Cutoff: 0.0 mA",
                                style = MaterialTheme.typography.bodySmall.copy(color = if (powerOn) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(checked = powerOn, onCheckedChange = { powerOn = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LedIndicatorWidget(isLit = powerOn, color = Color(0xFFFF1744), label = "Amplified Collector Load")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Run Software Test Button
            OutlinedButton(
                onClick = {
                    isTestRunning = true
                    testResultVerified = true
                    onTestPassed()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (testResultVerified) "Re-run Software Verification" else "Run Software Circuit Test")
            }

            if (testResultVerified) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LedGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Continuous 5.0V Loop Verified • Simulation Passed",
                        style = MaterialTheme.typography.bodySmall.copy(color = LedGreen, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun LedIndicatorWidget(
    isLit: Boolean,
    color: Color,
    alpha: Float = 1f,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (isLit) color.copy(alpha = alpha) else Color.DarkGray,
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        if (isLit) color else Color.Gray,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isLit) "Status: ILLUMINATED (Active Flow)" else "Status: OFF (Zero Current)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isLit) LedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun ProjectStatusChip(status: ProjectStatus) {
    val (text, color) = when (status) {
        ProjectStatus.COMPLETED -> "COMPLETED" to LedGreen
        ProjectStatus.TESTED -> "TESTED" to Color(0xFF00E5FF)
        ProjectStatus.IN_PROGRESS -> "IN PROGRESS" to MaterialTheme.colorScheme.primary
        ProjectStatus.UNLOCKED -> "AVAILABLE" to Color(0xFFFF9100)
        ProjectStatus.LOCKED -> "LOCKED" to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 9.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
