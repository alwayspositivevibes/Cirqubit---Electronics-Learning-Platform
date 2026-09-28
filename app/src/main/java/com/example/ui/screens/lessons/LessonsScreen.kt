package com.example.ui.screens.lessons

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.data.model.ProgressStatus
import com.example.ui.AppDestination
import com.example.ui.components.CircuitDiagramView
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.LedGreen

@Composable
fun LessonsScreen(
    lessons: List<Lesson>,
    selectedLessonId: String?,
    onSelectLesson: (String) -> Unit,
    onCompleteLesson: (String) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedLessonId by remember(selectedLessonId, lessons) {
        val initial = lessons.firstOrNull { it.id == selectedLessonId && it.status != ProgressStatus.LOCKED }?.id
            ?: lessons.firstOrNull { it.status == ProgressStatus.UNLOCKED || it.status == ProgressStatus.IN_PROGRESS }?.id
            ?: lessons.firstOrNull()?.id
        mutableStateOf(initial)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Course Header Card
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
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "CORE COURSE 01",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        val completedCount = lessons.count { it.status == ProgressStatus.COMPLETED }
                        Text(
                            text = "$completedCount of 8 Completed",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Electronic Fundamentals",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Master the essential principles of electronics through structured modules, interactive circuit diagrams, and guided knowledge challenges.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Lesson Items
        items(lessons) { lesson ->
            val isExpanded = expandedLessonId == lesson.id
            LessonModuleCard(
                lesson = lesson,
                isExpanded = isExpanded,
                onCardClick = {
                    if (lesson.status != ProgressStatus.LOCKED) {
                        expandedLessonId = if (isExpanded) null else lesson.id
                        onSelectLesson(lesson.id)
                    }
                },
                onCompleteClick = {
                    onCompleteLesson(lesson.id)
                },
                onStartChallengeClick = {
                    onNavigate(AppDestination.CHALLENGES)
                }
            )
        }
    }
}

@Composable
fun LessonModuleCard(
    lesson: Lesson,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onStartChallengeClick: () -> Unit
) {
    val isLocked = lesson.status == ProgressStatus.LOCKED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.order}")
            .clickable(enabled = !isLocked) { onCardClick() },
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
                // Status Icon badge
                StatusBadge(lesson.status, lesson.order)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MODULE 0${lesson.order}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "•  ${lesson.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = lesson.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                StatusChip(lesson.status)
            }

            // Expanded Lesson Detailed Sections
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Learning Objectives
                    Text(
                        text = "🎯 Learning Objectives",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    lesson.objectives.forEach { obj ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(text = obj, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Concept Explanation
                    Text(
                        text = "📖 Core Concept",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.conceptExplanation,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Circuit Schematic / Visual
                    CircuitDiagramView(
                        diagramCode = lesson.circuitDiagramCode,
                        title = "Circuit Schematic: ${lesson.title}"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Component Details
                    Text(
                        text = "🔌 Component Guide",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.componentExplanation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Important Formula
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚡ IMPORTANT FORMULA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CircuitGold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.importantFormula,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.formulaExplanation,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. Worked Example
                    Text(
                        text = "🔬 Worked Example",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = lesson.workedExample,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. Key Takeaways
                    Text(
                        text = "💡 Key Takeaways",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    lesson.keyTakeaways.forEach { takeaway ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("✓ ", color = LedGreen, fontWeight = FontWeight.Bold)
                            Text(text = takeaway, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onStartChallengeClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Challenge")
                        }

                        if (lesson.status == ProgressStatus.UNLOCKED || lesson.status == ProgressStatus.IN_PROGRESS) {
                            Button(
                                onClick = onCompleteClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete (+${lesson.xpReward} XP)")
                            }
                        } else if (lesson.status == ProgressStatus.COMPLETED) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LedGreen.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LedGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Completed", color = LedGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
private fun StatusBadge(status: ProgressStatus, order: Int) {
    val bgColor = when (status) {
        ProgressStatus.COMPLETED -> LedGreen.copy(alpha = 0.2f)
        ProgressStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ProgressStatus.UNLOCKED -> Color(0xFFFF9100).copy(alpha = 0.2f)
        ProgressStatus.LOCKED -> Color.Gray.copy(alpha = 0.15f)
    }

    val iconColor = when (status) {
        ProgressStatus.COMPLETED -> LedGreen
        ProgressStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        ProgressStatus.UNLOCKED -> Color(0xFFFF9100)
        ProgressStatus.LOCKED -> Color.Gray
    }

    Surface(
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (status) {
                ProgressStatus.COMPLETED -> Icon(Icons.Default.Check, contentDescription = "Done", tint = iconColor, modifier = Modifier.size(20.dp))
                ProgressStatus.IN_PROGRESS -> Text("$order", fontWeight = FontWeight.Bold, color = iconColor)
                ProgressStatus.UNLOCKED -> Text("$order", fontWeight = FontWeight.Bold, color = iconColor)
                ProgressStatus.LOCKED -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatusChip(status: ProgressStatus) {
    val (text, color) = when (status) {
        ProgressStatus.COMPLETED -> "COMPLETED" to LedGreen
        ProgressStatus.IN_PROGRESS -> "IN PROGRESS" to MaterialTheme.colorScheme.primary
        ProgressStatus.UNLOCKED -> "AVAILABLE" to Color(0xFFFF9100)
        ProgressStatus.LOCKED -> "LOCKED" to Color.Gray
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
