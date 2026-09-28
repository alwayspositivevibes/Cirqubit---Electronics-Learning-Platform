package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.LedGreen
import com.example.ui.theme.LedRed

@Composable
fun CircuitDiagramView(
    diagramCode: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val traceColor = CircuitTrace
    val compColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (!title.isNullOrBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "SCHEMATIC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFF070C14), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Grid dots
                val gridSpacing = 20.dp.toPx()
                var x = gridSpacing
                while (x < w) {
                    var y = gridSpacing
                    while (y < h) {
                        drawCircle(Color(0xFF1A2638), radius = 1.5f, center = Offset(x, y))
                        y += gridSpacing
                    }
                    x += gridSpacing
                }

                val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                val thinStroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

                when (diagramCode) {
                    "LED_WITH_SERIES_RESISTOR", "CIRCUIT_PROJECT_1_LED" -> {
                        // Loop from Battery -> Resistor -> LED -> Battery
                        val topY = h * 0.25f
                        val botY = h * 0.75f
                        val leftX = w * 0.2f
                        val rightX = w * 0.8f
                        val midX1 = w * 0.45f
                        val midX2 = w * 0.65f

                        // Top wire
                        drawLine(traceColor, Offset(leftX, topY), Offset(midX1 - 25f, topY), strokeWidth = 3f)
                        // Resistor (zig-zag)
                        val rPath = Path().apply {
                            moveTo(midX1 - 25f, topY)
                            lineTo(midX1 - 15f, topY - 12f)
                            lineTo(midX1 - 5f, topY + 12f)
                            lineTo(midX1 + 5f, topY - 12f)
                            lineTo(midX1 + 15f, topY + 12f)
                            lineTo(midX1 + 25f, topY)
                        }
                        drawPath(rPath, CircuitGold, style = thinStroke)
                        drawLine(traceColor, Offset(midX1 + 25f, topY), Offset(rightX, topY), strokeWidth = 3f)

                        // Right wire down to LED
                        drawLine(traceColor, Offset(rightX, topY), Offset(rightX, h * 0.4f), strokeWidth = 3f)

                        // LED Diode symbol at right
                        val ledCenterY = h * 0.5f
                        val ledPath = Path().apply {
                            moveTo(rightX - 16f, ledCenterY - 14f)
                            lineTo(rightX + 16f, ledCenterY - 14f)
                            lineTo(rightX, ledCenterY + 10f)
                            close()
                        }
                        drawPath(ledPath, LedRed)
                        // Cathode bar
                        drawLine(Color.White, Offset(rightX - 16f, ledCenterY + 10f), Offset(rightX + 16f, ledCenterY + 10f), strokeWidth = 3f)
                        // Light emission arrows
                        drawLine(CircuitGold, Offset(rightX + 18f, ledCenterY - 6f), Offset(rightX + 30f, ledCenterY - 14f), strokeWidth = 2f)
                        drawLine(CircuitGold, Offset(rightX + 18f, ledCenterY + 4f), Offset(rightX + 30f, ledCenterY - 4f), strokeWidth = 2f)

                        // Down to bottom wire
                        drawLine(traceColor, Offset(rightX, ledCenterY + 10f), Offset(rightX, botY), strokeWidth = 3f)
                        drawLine(traceColor, Offset(rightX, botY), Offset(leftX, botY), strokeWidth = 3f)

                        // Left DC battery
                        drawLine(traceColor, Offset(leftX, topY), Offset(leftX, h * 0.45f), strokeWidth = 3f)
                        // Long plate (+)
                        drawLine(Color.White, Offset(leftX - 18f, h * 0.45f), Offset(leftX + 18f, h * 0.45f), strokeWidth = 4f)
                        // Short plate (-)
                        drawLine(Color(0xFF90CAF9), Offset(leftX - 10f, h * 0.55f), Offset(leftX + 10f, h * 0.55f), strokeWidth = 5f)
                        drawLine(traceColor, Offset(leftX, h * 0.55f), Offset(leftX, botY), strokeWidth = 3f)

                        // Ground symbol at bottom
                        drawLine(Color.White, Offset(leftX, botY), Offset(leftX, botY + 14f), strokeWidth = 2f)
                        drawLine(Color.White, Offset(leftX - 12f, botY + 14f), Offset(leftX + 12f, botY + 14f), strokeWidth = 3f)
                        drawLine(Color.White, Offset(leftX - 8f, botY + 18f), Offset(leftX + 8f, botY + 18f), strokeWidth = 2f)
                        drawLine(Color.White, Offset(leftX - 4f, botY + 22f), Offset(leftX + 4f, botY + 22f), strokeWidth = 1.5f)
                    }
                    "CIRCUIT_PROJECT_2_POT_LED" -> {
                        // Potentiometer + LED
                        val topY = h * 0.25f
                        val botY = h * 0.75f
                        val leftX = w * 0.18f
                        val rightX = w * 0.82f

                        drawLine(traceColor, Offset(leftX, topY), Offset(w * 0.4f, topY), strokeWidth = 3f)
                        // Pot box / zigzag
                        val potPath = Path().apply {
                            moveTo(w * 0.4f, topY)
                            lineTo(w * 0.43f, topY - 10f)
                            lineTo(w * 0.46f, topY + 10f)
                            lineTo(w * 0.49f, topY - 10f)
                            lineTo(w * 0.52f, topY + 10f)
                            lineTo(w * 0.55f, topY)
                        }
                        drawPath(potPath, CircuitGold, style = thinStroke)
                        // Wiper arrow
                        drawLine(CircuitGold, Offset(w * 0.48f, topY + 18f), Offset(w * 0.48f, topY + 4f), strokeWidth = 2f)

                        drawLine(traceColor, Offset(w * 0.55f, topY), Offset(rightX, topY), strokeWidth = 3f)
                        drawLine(traceColor, Offset(rightX, topY), Offset(rightX, botY), strokeWidth = 3f)
                        drawLine(traceColor, Offset(rightX, botY), Offset(leftX, botY), strokeWidth = 3f)
                        drawLine(traceColor, Offset(leftX, topY), Offset(leftX, botY), strokeWidth = 3f)

                        // Green LED
                        drawCircle(LedGreen, radius = 9f, center = Offset(rightX, h * 0.5f))
                    }
                    else -> {
                        // Generic high tech circuit schematic
                        val midY = h * 0.5f
                        drawLine(traceColor, Offset(w * 0.15f, midY), Offset(w * 0.85f, midY), strokeWidth = 3f)
                        drawCircle(Color(0xFF00E5FF), radius = 6f, center = Offset(w * 0.15f, midY))
                        drawCircle(CircuitGold, radius = 6f, center = Offset(w * 0.5f, midY))
                        drawCircle(LedGreen, radius = 6f, center = Offset(w * 0.85f, midY))
                        // Ground connection
                        drawLine(Color.White, Offset(w * 0.5f, midY), Offset(w * 0.5f, midY + 30f), strokeWidth = 2f)
                        drawLine(Color.White, Offset(w * 0.5f - 12f, midY + 30f), Offset(w * 0.5f + 12f, midY + 30f), strokeWidth = 3f)
                    }
                }
            }
        }
    }
}
