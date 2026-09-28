package com.example.ui.screens.designer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.DesignerComponentItem
import com.example.ui.theme.CircuitGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentPropertiesSheet(
    component: DesignerComponentItem,
    onUpdateValue: (String) -> Unit,
    onRotate: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentValue by remember(component.value) { mutableStateOf(component.value) }

    val presets = remember(component.type) {
        when (component.type) {
            "RESISTOR", "VAR_RESISTOR" -> listOf("100Ω", "220Ω", "330Ω", "470Ω", "1kΩ", "4.7kΩ", "10kΩ", "100kΩ")
            "POTENTIOMETER" -> listOf("1kΩ", "5kΩ", "10kΩ", "50kΩ", "100kΩ")
            "CAPACITOR", "POL_CAPACITOR" -> listOf("100pF", "1nF", "10nF", "100nF", "1μF", "10μF", "100μF", "470μF")
            "BATTERY", "DC_SOURCE" -> listOf("1.5V", "3.3V", "5V", "9V", "12V")
            "VCC" -> listOf("+3.3V", "+5V", "+12V")
            "LED" -> listOf("Red (2.0V)", "Green (2.2V)", "Yellow (2.1V)", "Blue (3.2V)", "White (3.2V)")
            "DIODE" -> listOf("1N4007", "1N4148", "1N5819 (Schottky)")
            "ZENER_DIODE" -> listOf("3.3V", "5.1V", "9.1V", "12V")
            "TRANSISTOR_NPN" -> listOf("2N2222", "BC547", "TIP120 (Darlington)")
            "TRANSISTOR_PNP" -> listOf("2N3906", "BC557")
            "MOSFET" -> listOf("IRF540N", "BS170", "2N7000")
            "LDR" -> listOf("10kΩ Dark", "50kΩ Dark", "100kΩ Dark")
            "BUZZER" -> listOf("5V Active", "5V Passive", "12V Active")
            else -> listOf("Standard", "Custom")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Component Properties",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${component.label} • Type: ${component.type}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Properties")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Value Edit Field
            Text(
                text = "SPECIFICATION / VALUE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = currentValue,
                onValueChange = {
                    currentValue = it
                    onUpdateValue(it)
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("property_value_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Preset Chips
            Text(
                text = "QUICK VALUE PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentValue == preset) CircuitGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentValue == preset) CircuitGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.clickable {
                            currentValue = preset
                            onUpdateValue(preset)
                        }
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (currentValue == preset) CircuitGold else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions: Rotate, Duplicate, Delete
            Text(
                text = "WORKSPACE ACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRotate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rotate 90° (${component.rotationDeg}°)")
                }

                OutlinedButton(
                    onClick = onDuplicate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Duplicate")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onDelete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_component_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Component")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
