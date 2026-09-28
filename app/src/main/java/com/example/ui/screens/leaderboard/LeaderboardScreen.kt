package com.example.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.LedGreen

@Composable
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    var selectedScope by remember { mutableStateOf(0) } // 0 = Global, 1 = Weekly, 2 = Class / Friends

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Leaderboard Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CircuitGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "HONOR BOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CircuitGold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Electronics Standings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Rankings calculated from verified project submissions, challenge scores, and circuit debugging speed.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        listOf("Global", "Weekly", "Class / Friends").forEachIndexed { index, title ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedScope == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedScope = index }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedScope == index) MaterialTheme.colorScheme.onPrimary else Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top 3 Podium
        item {
            val top3 = entries.take(3)
            if (top3.size >= 3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place
                        PodiumColumn(top3[1], 2, Color(0xFFB0BEC5), 80.dp)
                        // 1st Place
                        PodiumColumn(top3[0], 1, CircuitGold, 110.dp)
                        // 3rd Place
                        PodiumColumn(top3[2], 3, Color(0xFFCD7F32), 65.dp)
                    }
                }
            }
        }

        // Student Ranking List
        item {
            Text(
                text = "Course Leaderboard",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(entries) { entry ->
            val isCurrent = entry.isCurrentStudent

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = if (isCurrent) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
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
                    // Rank
                    Box(
                        modifier = Modifier.width(36.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "#${entry.rank}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (entry.rank) {
                                    1 -> CircuitGold
                                    2 -> Color(0xFFB0BEC5)
                                    3 -> Color(0xFFCD7F32)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        )
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(entry.avatarColor), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.name.first().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.name + if (isCurrent) " (You)" else "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Text(
                            text = "Lvl ${entry.level} • ${entry.badge} • ${entry.completedProjects} Projects Built",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Text(
                        text = "${entry.xp} XP",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(entry: LeaderboardEntry, rank: Int, crownColor: Color, height: androidx.compose.ui.unit.Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = crownColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(entry.avatarColor), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.name.split(" ").first(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1
        )
        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = crownColor.copy(alpha = 0.25f),
            modifier = Modifier.fillMaxWidth().height(height)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = crownColor)
                )
            }
        }
    }
}
