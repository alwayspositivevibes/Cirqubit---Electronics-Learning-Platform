package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Achievement
import com.example.data.model.Project
import com.example.data.model.ProjectStatus
import com.example.data.model.StudentProfile
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.LedGreen

@Composable
fun ProfileScreen(
    student: StudentProfile,
    achievements: List<Achievement>,
    projects: List<Project>,
    modifier: Modifier = Modifier
) {
    val completedProjects = projects.filter { it.status == ProjectStatus.COMPLETED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(3.dp, CircuitGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.first().toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Level ${student.level} • Circuit Apprentice",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${student.xp}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CircuitGold))
                            Text("Total XP", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${student.streakDays} Days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF9100)))
                            Text("Streak", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${completedProjects.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LedGreen))
                            Text("Projects", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${student.completedChallengesCount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF)))
                            Text("Quizzes", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }
            }
        }

        // Verified Course Certificate Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1322)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CircuitGold.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CircuitGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ACADEMIC CREDENTIAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CircuitGold,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Icon(Icons.Default.Verified, contentDescription = null, tint = CircuitGold, modifier = Modifier.size(22.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Certificate of Electronic Fundamentals",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Candidate: ${student.name}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CircuitGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Issued by Cirqubit Learning Academy upon completion of all 8 laboratory projects & circuit simulation challenges.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (completedProjects.size.toFloat() / 8f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = CircuitGold,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${completedProjects.size} of 8 Projects verified towards graduation",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
            }
        }

        // Badges / Achievements
        item {
            Text(
                text = "Badges & Achievements",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(achievements) { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (ach.isUnlocked) CircuitGold.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = ach.title,
                            tint = if (ach.isUnlocked) CircuitGold else Color.Gray,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        )
                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (ach.isUnlocked) LedGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (ach.isUnlocked) "+${ach.xpReward} XP" else "LOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) LedGreen else Color.Gray
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Project Portfolio
        item {
            Text(
                text = "Completed Projects Portfolio",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (completedProjects.isEmpty()) {
            item {
                Text(
                    text = "No completed projects yet. Open the Projects section to build your first circuit!",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            items(completedProjects) { proj ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LedGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROJECT 0${proj.order}: ${proj.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(Icons.Default.Verified, contentDescription = null, tint = LedGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = proj.objective,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Components: ${proj.requiredSpecs}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}
