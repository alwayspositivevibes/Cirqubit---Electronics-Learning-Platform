package com.example.ui.screens.challenges

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Challenge
import com.example.data.model.ChallengeType
import com.example.ui.AppDestination
import com.example.ui.components.CircuitDiagramView
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.LedGreen
import com.example.ui.theme.LedRed

@Composable
fun ChallengesScreen(
    challenges: List<Challenge>,
    onSubmitChallenge: (String, Int, (Boolean, Int, String) -> Unit) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChallengeId by remember {
        mutableStateOf(challenges.firstOrNull { !it.isCompleted }?.id ?: challenges.firstOrNull()?.id)
    }

    val activeChallenge = challenges.firstOrNull { it.id == selectedChallengeId } ?: challenges.firstOrNull()
    var selectedOptionIndex by remember(selectedChallengeId) { mutableStateOf<Int?>(null) }
    var submissionResult by remember(selectedChallengeId) { mutableStateOf<Triple<Boolean, Int, String>?>(null) }
    var showHint by remember(selectedChallengeId) { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Challenges Header Card
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
                            color = Color(0xFFFF9100).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "KNOWLEDGE BENCH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9100),
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        val passedCount = challenges.count { it.isCompleted }
                        Text(
                            text = "$passedCount/${challenges.size} Passed",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Electronics Mastery Challenges",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Test your theoretical understanding and troubleshooting skill. 70% passing score required to earn XP.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Active Challenge Card
        if (activeChallenge != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_challenge_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChallengeTypeBadge(activeChallenge.type)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CircuitGold.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = CircuitGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${activeChallenge.xpReward} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CircuitGold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = activeChallenge.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = activeChallenge.question,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                        )

                        // Visual Schematic if provided
                        if (activeChallenge.visualCode != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            CircuitDiagramView(
                                diagramCode = activeChallenge.visualCode,
                                title = "Challenge Reference Schematic"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Answer Options
                        activeChallenge.options.forEachIndexed { index, optionText ->
                            val isSelected = selectedOptionIndex == index
                            val isSubmitted = submissionResult != null
                            val isCorrect = index == activeChallenge.correctIndex

                            val borderColor = when {
                                isSubmitted && isCorrect -> LedGreen
                                isSubmitted && isSelected && !submissionResult!!.first -> LedRed
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            }

                            val optionBg = when {
                                isSubmitted && isCorrect -> LedGreen.copy(alpha = 0.15f)
                                isSubmitted && isSelected && !submissionResult!!.first -> LedRed.copy(alpha = 0.15f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("challenge_option_$index")
                                    .clickable(enabled = submissionResult == null) {
                                        selectedOptionIndex = index
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = optionBg),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            val letter = ('A'.code + index).toChar().toString()
                                            Text(
                                                text = letter,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Hint Expander
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showHint = !showHint }
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showHint) "Hide Hint" else "Need a Hint?")
                            }

                            Text(
                                text = "Passing score: 70%",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        if (showHint) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CircuitGold.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CircuitGold.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = CircuitGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeChallenge.hint,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }

                        // Immediate Feedback Banner after Submission
                        if (submissionResult != null) {
                            val (passed, score, message) = submissionResult!!
                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (passed) LedGreen.copy(alpha = 0.15f) else LedRed.copy(alpha = 0.15f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (passed) LedGreen else LedRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (passed) LedGreen else LedRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (passed) "CHALLENGE PASSED ($score%)" else "SCORE BELOW 70% ($score%)",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (passed) LedGreen else LedRed
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = message, style = MaterialTheme.typography.bodySmall)

                                    if (passed) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = activeChallenge.explanation,
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    submissionResult = null
                                                    selectedOptionIndex = null
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Retry")
                                            }

                                            Button(
                                                onClick = { onNavigate(AppDestination.LESSONS) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Review Lesson")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Submit Button
                        if (submissionResult == null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val opt = selectedOptionIndex
                                    if (opt != null) {
                                        onSubmitChallenge(activeChallenge.id, opt) { passed, score, msg ->
                                            submissionResult = Triple(passed, score, msg)
                                        }
                                    }
                                },
                                enabled = selectedOptionIndex != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_challenge_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Answer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Challenge List Switcher
        item {
            Text(
                text = "All Course Challenges",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(challenges) { ch ->
            val isCurrent = ch.id == selectedChallengeId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedChallengeId = ch.id
                        selectedOptionIndex = null
                        submissionResult = null
                        showHint = false
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                border = if (isCurrent) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    CardDefaults.outlinedCardBorder()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (ch.isCompleted) LedGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (ch.isCompleted) Icons.Default.Check else Icons.Default.Quiz,
                                contentDescription = null,
                                tint = if (ch.isCompleted) LedGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ch.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${ch.type.name.replace("_", " ")} • +${ch.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    if (ch.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LedGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PASSED",
                                style = MaterialTheme.typography.labelSmall.copy(color = LedGreen, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeTypeBadge(type: ChallengeType) {
    val (label, color) = when (type) {
        ChallengeType.MULTIPLE_CHOICE -> "MULTIPLE CHOICE" to MaterialTheme.colorScheme.primary
        ChallengeType.CALCULATION -> "CALCULATION" to CircuitGold
        ChallengeType.TROUBLESHOOTING -> "FAULT TROUBLESHOOTING" to LedRed
        ChallengeType.CIRCUIT_IDENTIFICATION -> "CIRCUIT ID" to Color(0xFF00E5FF)
        ChallengeType.COMPONENT_IDENTIFICATION -> "COMPONENT ID" to Color(0xFFFF9100)
        ChallengeType.PRACTICAL_HARDWARE -> "PRACTICAL HARDWARE" to LedGreen
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
