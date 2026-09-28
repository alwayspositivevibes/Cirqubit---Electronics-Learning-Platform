package com.example.ui.screens.hardware

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HardwareTelemetry
import com.example.ui.theme.*

@Composable
fun HardwareConnectScreen(
    telemetry: HardwareTelemetry,
    onConnectKit: (Boolean) -> Unit,
    onRunHardwareTest: () -> String,
    onUpdatePotentiometer: (Float) -> Unit,
    onUpdateLightLevel: (Int) -> Unit,
    onToggleButton: (Int, Boolean) -> Unit,
    onToggleLed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var testResultOutput by remember { mutableStateOf<String?>(null) }
    var isSimulatingConnecting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hardware Kit Header & Physical Image
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
                                text = "HARDWARE INTERFACE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Connection Status Pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (telemetry.isConnected) HardwareOnline else HardwareOffline,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (telemetry.isConnected) "CONNECTED" else "DISCONNECTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (telemetry.isConnected) HardwareOnline else HardwareOffline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cirqubit Kit v1",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Reusable modular electronics laboratory. Connect over USB-C to stream live pin voltages, verify student wiring, and calibrate sensors.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Physical Kit Image Asset
                    Image(
                        painter = painterResource(id = R.drawable.img_cirqubit_kit),
                        contentDescription = "Cirqubit Kit v1 Physical Hardware",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Connection Action Button Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = telemetry.deviceName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Firmware: ${telemetry.firmwareVersion} • Virtual COM 115200 baud",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Button(
                            onClick = {
                                if (telemetry.isConnected) {
                                    onConnectKit(false)
                                    testResultOutput = null
                                } else {
                                    isSimulatingConnecting = true
                                    onConnectKit(true)
                                    isSimulatingConnecting = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (telemetry.isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("connect_kit_button")
                        ) {
                            Icon(
                                imageVector = if (telemetry.isConnected) Icons.Default.UsbOff else Icons.Default.Usb,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (telemetry.isConnected) "Disconnect" else "Connect Kit")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulation disclaimer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Simulation Mode: This prototype simulates hardware packet communication over a virtual USB driver. The architecture is prepared for direct physical USB OTG communication.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live Telemetry Gauges (Visible when connected)
        item {
            Text(
                text = "Live Hardware Telemetry & Sensor Controls",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // 1. Potentiometer & Voltage
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = CircuitGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "10kΩ Potentiometer (Analog Pin A0)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        val potVoltage = (telemetry.potentiometerValue * 5.0f)
                        Text(
                            text = String.format("%.2f V", potVoltage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CircuitGold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = telemetry.potentiometerValue,
                        onValueChange = { onUpdatePotentiometer(it) },
                        enabled = telemetry.isConnected,
                        modifier = Modifier.testTag("potentiometer_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0Ω (Min)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${(telemetry.potentiometerValue * 100).toInt()}% Wiper", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("10,000Ω (Max)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }

        // 2. Light Dependent Resistor (LDR) Lux
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brightness6, contentDescription = null, tint = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Light Level Sensor LDR (Pin A1)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "${telemetry.lightLevelLux} Lux",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = telemetry.lightLevelLux.toFloat(),
                        onValueChange = { onUpdateLightLevel(it.toInt()) },
                        valueRange = 10f..1000f,
                        enabled = telemetry.isConnected,
                        modifier = Modifier.testTag("ldr_lux_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dark (10 Lux)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = if (telemetry.lightLevelLux < 100) "Night Mode" else "Daylight",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.lightLevelLux < 100) Color(0xFFFF9100) else LedGreen
                        )
                        Text("Bright (1,000 Lux)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }

        // 3. Buttons & Actuators
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tactile Buttons Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Push Buttons",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    val current = telemetry.button1Pressed
                                    onToggleButton(1, !current)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (telemetry.button1Pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                enabled = telemetry.isConnected,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (telemetry.button1Pressed) "BTN1 ON" else "BTN 1", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val current = telemetry.button2Pressed
                                    onToggleButton(2, !current)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (telemetry.button2Pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                enabled = telemetry.isConnected,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (telemetry.button2Pressed) "BTN2 ON" else "BTN 2", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // LED Output Toggle Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Status Output LED",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onToggleLed,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (telemetry.ledOutputActive) LedGreen else Color(0xFF334155)
                            ),
                            enabled = telemetry.isConnected,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (telemetry.ledOutputActive) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (telemetry.ledOutputActive) "LED LIT" else "LED OFF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 4. Power & Loop Diagnostics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070D18)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BUS ELECTRICAL STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CircuitTrace,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Supply Voltage", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "${telemetry.supplyVoltage} V",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Column {
                            Text("Current Draw", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                String.format("%.1f mA", telemetry.currentDrawMa),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = CircuitGold
                                )
                            )
                        }
                        Column {
                            Text("Loop Continuity", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                if (telemetry.isConnected) "VERIFIED" else "OPEN",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (telemetry.isConnected) LedGreen else HardwareOffline
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. Run Hardware Test Diagnostic
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automated Hardware Test",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Injects digital signal pulses and reads back analog pin values to verify breadboard circuit integrity.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            testResultOutput = onRunHardwareTest()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("run_hardware_test_button"),
                        enabled = telemetry.isConnected
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Hardware Test", fontWeight = FontWeight.Bold)
                    }

                    if (testResultOutput != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LedGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testResultOutput!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = LedGreen
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
