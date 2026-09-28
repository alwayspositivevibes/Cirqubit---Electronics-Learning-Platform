package com.example.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.LedGreen
import com.example.ui.theme.LedRed
import kotlin.math.pow
import kotlin.math.sqrt

enum class CalculatorTab(val title: String) {
    OHMS_LAW("Ohm's Law"),
    POWER("Power (Watts)"),
    LED_RESISTOR("LED Resistor"),
    RESISTOR_COLOR("Color Code"),
    SCIENTIFIC("Scientific")
}

@Composable
fun CalculatorsScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CalculatorTab.OHMS_LAW) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Header
        Column {
            Text(
                text = "Electronics Calculators",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Precision engineering tools for circuit design & lab experiments",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            CalculatorTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                CalculatorTab.OHMS_LAW -> OhmsLawCalculator()
                CalculatorTab.POWER -> PowerCalculator()
                CalculatorTab.LED_RESISTOR -> LedResistorCalculator()
                CalculatorTab.RESISTOR_COLOR -> ResistorColorCodeCalculator()
                CalculatorTab.SCIENTIFIC -> ScientificCalculator()
            }
        }
    }
}

// 1. Ohm's Law Calculator
@Composable
fun OhmsLawCalculator() {
    var voltageInput by remember { mutableStateOf("5.0") }
    var currentMaInput by remember { mutableStateOf("20.0") }
    var resistanceInput by remember { mutableStateOf("250.0") }
    var solveFor by remember { mutableStateOf("R") } // "V", "I", "R"

    val v = voltageInput.toDoubleOrNull() ?: 0.0
    val iMa = currentMaInput.toDoubleOrNull() ?: 0.0
    val iAmps = iMa / 1000.0
    val r = resistanceInput.toDoubleOrNull() ?: 0.0

    val calculatedValue = when (solveFor) {
        "V" -> "${String.format("%.3f", iAmps * r)} Volts"
        "I" -> "${String.format("%.2f", if (r > 0) (v / r) * 1000.0 else 0.0)} mA"
        else -> "${String.format("%.1f", if (iAmps > 0) v / iAmps else 0.0)} Ω"
    }

    val powerWatts = when (solveFor) {
        "V" -> (iAmps * r) * iAmps
        "I" -> v * (if (r > 0) v / r else 0.0)
        else -> v * iAmps
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select parameter to calculate:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = solveFor == "V",
                            onClick = { solveFor = "V" },
                            label = { Text("Voltage (V)") }
                        )
                        FilterChip(
                            selected = solveFor == "I",
                            onClick = { solveFor = "I" },
                            label = { Text("Current (I)") }
                        )
                        FilterChip(
                            selected = solveFor == "R",
                            onClick = { solveFor = "R" },
                            label = { Text("Resistance (R)") }
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (solveFor != "V") {
                        OutlinedTextField(
                            value = voltageInput,
                            onValueChange = { voltageInput = it },
                            label = { Text("Voltage (V) in Volts") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("ohms_voltage_input")
                        )
                    }

                    if (solveFor != "I") {
                        OutlinedTextField(
                            value = currentMaInput,
                            onValueChange = { currentMaInput = it },
                            label = { Text("Current (I) in Milliamperes (mA)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("ohms_current_input")
                        )
                    }

                    if (solveFor != "R") {
                        OutlinedTextField(
                            value = resistanceInput,
                            onValueChange = { resistanceInput = it },
                            label = { Text("Resistance (R) in Ohms (Ω)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("ohms_resistance_input")
                        )
                    }
                }
            }
        }

        // Result Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CircuitGold)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CALCULATED $solveFor (OHM'S LAW: V = I × R)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CircuitGold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = calculatedValue,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Power Dissipation: ${String.format("%.4f", powerWatts)} W (${String.format("%.1f", powerWatts * 1000)} mW)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )
                }
            }
        }
    }
}

// 2. Power Calculator
@Composable
fun PowerCalculator() {
    var vInput by remember { mutableStateOf("12.0") }
    var iInput by remember { mutableStateOf("0.5") }
    var rInput by remember { mutableStateOf("24.0") }

    val v = vInput.toDoubleOrNull() ?: 0.0
    val i = iInput.toDoubleOrNull() ?: 0.0
    val r = rInput.toDoubleOrNull() ?: 0.0

    val p1 = v * i
    val p2 = i.pow(2) * r
    val p3 = if (r > 0) v.pow(2) / r else 0.0

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Joule's Power Law Formulas", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("P = V × I   |   P = I² × R   |   P = V² / R", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary))

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = vInput,
                        onValueChange = { vInput = it },
                        label = { Text("Voltage V (Volts)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = iInput,
                        onValueChange = { iInput = it },
                        label = { Text("Current I (Amperes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rInput,
                        onValueChange = { rInput = it },
                        label = { Text("Resistance R (Ohms)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CircuitTrace)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("POWER CALCULATIONS", style = MaterialTheme.typography.labelSmall.copy(color = CircuitTrace, fontWeight = FontWeight.Bold))
                    Text("From V × I: ${String.format("%.3f", p1)} Watts", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                    Text("From I² × R: ${String.format("%.3f", p2)} Watts", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                    Text("From V² / R: ${String.format("%.3f", p3)} Watts", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                }
            }
        }
    }
}

// 3. LED Resistor Calculator
@Composable
fun LedResistorCalculator() {
    var vSupply by remember { mutableStateOf("5.0") }
    var vForward by remember { mutableStateOf("2.0") } // Red ~2V, Green ~2.2V, Blue ~3.2V
    var iForwardMa by remember { mutableStateOf("15.0") }

    val vs = vSupply.toDoubleOrNull() ?: 5.0
    val vf = vForward.toDoubleOrNull() ?: 2.0
    val ifMa = iForwardMa.toDoubleOrNull() ?: 15.0
    val ifAmps = ifMa / 1000.0

    val rNeeded = if (ifAmps > 0 && vs > vf) (vs - vf) / ifAmps else 0.0
    val pResistor = if (rNeeded > 0) (vs - vf) * ifAmps else 0.0

    // Find nearest standard E12/E24 resistor
    val standardResistors = listOf(100, 120, 150, 180, 220, 270, 330, 390, 470, 560, 680, 820, 1000)
    val nearest = standardResistors.minByOrNull { kotlin.math.abs(it - rNeeded) } ?: 220

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Formula: R = (V_supply - V_forward) / I_desired", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary))

                    OutlinedTextField(
                        value = vSupply,
                        onValueChange = { vSupply = it },
                        label = { Text("Supply Voltage Vcc (e.g., 5V USB)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("led_supply_voltage_input")
                    )

                    OutlinedTextField(
                        value = vForward,
                        onValueChange = { vForward = it },
                        label = { Text("LED Forward Voltage Drop Vf (e.g., 2.0V)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("led_forward_voltage_input")
                    )

                    // Quick LED Color Presets
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = vForward == "2.0", onClick = { vForward = "2.0" }, label = { Text("Red (2.0V)") })
                        FilterChip(selected = vForward == "2.2", onClick = { vForward = "2.2" }, label = { Text("Green (2.2V)") })
                        FilterChip(selected = vForward == "3.2", onClick = { vForward = "3.2" }, label = { Text("Blue (3.2V)") })
                    }

                    OutlinedTextField(
                        value = iForwardMa,
                        onValueChange = { iForwardMa = it },
                        label = { Text("Desired Current in mA (typically 10-20mA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("led_current_input")
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, LedGreen)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("RECOMMENDED CURRENT-LIMITING RESISTOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = LedGreen))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${String.format("%.1f", rNeeded)} Ω (Exact)",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace, color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Nearest Standard Kit Resistor: $nearest Ω",
                        style = MaterialTheme.typography.titleMedium.copy(color = CircuitGold, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Resistor Power Dissipation: ${String.format("%.3f", pResistor)} W (Safe for 1/4W resistor)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )
                }
            }
        }
    }
}

// 4. Resistor Color Code Calculator
@Composable
fun ResistorColorCodeCalculator() {
    val colorBands = listOf(
        "Black" to Color(0xFF000000),
        "Brown" to Color(0xFF795548),
        "Red" to Color(0xFFD32F2F),
        "Orange" to Color(0xFFFF9800),
        "Yellow" to Color(0xFFFFEB3B),
        "Green" to Color(0xFF4CAF50),
        "Blue" to Color(0xFF2196F3),
        "Violet" to Color(0xFF9C27B0),
        "Gray" to Color(0xFF9E9E9E),
        "White" to Color(0xFFFFFFFF)
    )

    var band1 by remember { mutableStateOf(2) } // Red = 2
    var band2 by remember { mutableStateOf(2) } // Red = 2
    var multiplier by remember { mutableStateOf(1) } // Brown = 10^1
    var tolerance by remember { mutableStateOf(1) } // Gold = ±5%

    val baseValue = (band1 * 10 + band2) * (10.0.pow(multiplier))

    val displayString = if (baseValue >= 1000000) {
        "${baseValue / 1000000} MΩ"
    } else if (baseValue >= 1000) {
        "${baseValue / 1000} kΩ"
    } else {
        "$baseValue Ω"
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            // Visual Resistor Graphic
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("4-BAND COLOR CODE PREVIEW", style = MaterialTheme.typography.labelSmall.copy(color = CircuitGold, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stylized Resistor Body
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(44.dp)
                            .background(Color(0xFFD7CCC8), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.width(14.dp).fillMaxHeight().background(colorBands[band1].second))
                            Box(modifier = Modifier.width(14.dp).fillMaxHeight().background(colorBands[band2].second))
                            Box(modifier = Modifier.width(14.dp).fillMaxHeight().background(colorBands[multiplier].second))
                            Box(modifier = Modifier.width(14.dp).fillMaxHeight().background(Color(0xFFFFD700))) // Gold
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "$displayString ± 5%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Band 1: ${colorBands[band1].first} ($band1)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(colorBands.size) { i ->
                            Surface(
                                shape = CircleShape,
                                color = colorBands[i].second,
                                border = androidx.compose.foundation.BorderStroke(2.dp, if (band1 == i) CircuitGold else Color.Gray),
                                modifier = Modifier.size(32.dp).clickable { band1 = i }
                            ) {}
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Band 2: ${colorBands[band2].first} ($band2)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(colorBands.size) { i ->
                            Surface(
                                shape = CircleShape,
                                color = colorBands[i].second,
                                border = androidx.compose.foundation.BorderStroke(2.dp, if (band2 == i) CircuitGold else Color.Gray),
                                modifier = Modifier.size(32.dp).clickable { band2 = i }
                            ) {}
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Multiplier (Band 3): 10^$multiplier", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(7) { i ->
                            Surface(
                                shape = CircleShape,
                                color = colorBands[i].second,
                                border = androidx.compose.foundation.BorderStroke(2.dp, if (multiplier == i) CircuitGold else Color.Gray),
                                modifier = Modifier.size(32.dp).clickable { multiplier = i }
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

// 5. Scientific Calculator
@Composable
fun ScientificCalculator() {
    var display by remember { mutableStateOf("0") }
    var memory by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }

    fun onDigit(d: String) {
        if (display == "0" || resetOnNextDigit) {
            display = d
            resetOnNextDigit = false
        } else {
            display += d
        }
    }

    fun onOp(op: String) {
        val current = display.toDoubleOrNull() ?: 0.0
        memory = current
        pendingOp = op
        resetOnNextDigit = true
    }

    fun onEqual() {
        val current = display.toDoubleOrNull() ?: 0.0
        val mem = memory
        if (mem != null && pendingOp != null) {
            val res = when (pendingOp) {
                "+" -> mem + current
                "-" -> mem - current
                "×" -> mem * current
                "÷" -> if (current != 0.0) mem / current else Double.NaN
                "^" -> mem.pow(current)
                else -> current
            }
            display = if (res.isNaN()) "Error" else if (res == res.toLong().toDouble()) res.toLong().toString() else String.format("%.4f", res)
            memory = null
            pendingOp = null
            resetOnNextDigit = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Calculator Screen
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = display,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1
                )
            }
        }

        // Keypad Grid
        val buttonRows = listOf(
            listOf("C", "√", "^", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "π", "=")
        )

        buttonRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { btn ->
                    Button(
                        onClick = {
                            when (btn) {
                                "C" -> {
                                    display = "0"
                                    memory = null
                                    pendingOp = null
                                }
                                "√" -> {
                                    val v = display.toDoubleOrNull() ?: 0.0
                                    display = if (v >= 0) sqrt(v).toString() else "Error"
                                    resetOnNextDigit = true
                                }
                                "π" -> {
                                    display = "3.14159"
                                    resetOnNextDigit = true
                                }
                                "+", "-", "×", "÷", "^" -> onOp(btn)
                                "=" -> onEqual()
                                "." -> {
                                    if (!display.contains(".")) display += "."
                                }
                                else -> onDigit(btn)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (btn) {
                                "=" -> CircuitGold
                                "+", "-", "×", "÷", "^", "√" -> MaterialTheme.colorScheme.primary
                                "C" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when (btn) {
                                "=" -> Color.Black
                                else -> Color.White
                            }
                        ),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(btn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
