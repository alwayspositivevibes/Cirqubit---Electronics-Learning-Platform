package com.example.ui.screens.designer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.DesignerComponentItem
import com.example.data.model.DesignerWireItem

enum class ComponentCategory(val title: String) {
    ALL("All"),
    BASIC("Basic"),
    SEMICONDUCTORS("Semiconductors"),
    INPUT_OUTPUT("Input / Output"),
    POWER("Power"),
    DIGITAL_IC("Digital / IC")
}

data class PinDef(
    val id: String,
    val label: String,
    val relX: Float, // -1f (left), 0f (center), 1f (right)
    val relY: Float  // -1f (top), 0f (center), 1f (bottom)
)

data class LibraryComponent(
    val type: String,
    val name: String,
    val category: ComponentCategory,
    val description: String,
    val defaultValue: String,
    val prefix: String,
    val icon: ImageVector,
    val pins: List<PinDef>
)

enum class DesignerTool(val label: String, val icon: ImageVector) {
    SELECT("Select", Icons.Default.NearMe),
    MOVE("Move", Icons.Default.OpenWith),
    WIRE("Wire", Icons.Default.Timeline),
    PAN("Pan", Icons.Default.PanTool)
}

data class DesignerHistorySnapshot(
    val components: List<DesignerComponentItem>,
    val wires: List<DesignerWireItem>
)

object ComponentLibrary {
    val items = listOf(
        // BASIC
        LibraryComponent(
            type = "RESISTOR",
            name = "Resistor",
            category = ComponentCategory.BASIC,
            description = "Fixed resistance to limit current or divide voltage.",
            defaultValue = "220Ω",
            prefix = "R",
            icon = Icons.Default.ElectricMeter,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "POTENTIOMETER",
            name = "Potentiometer",
            category = ComponentCategory.BASIC,
            description = "Three-terminal rotary resistor with adjustable center wiper.",
            defaultValue = "10kΩ",
            prefix = "POT",
            icon = Icons.Default.Tune,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0.5f),
                PinDef("wiper", "W", 0f, -1f),
                PinDef("pin2", "2", 1f, 0.5f)
            )
        ),
        LibraryComponent(
            type = "VAR_RESISTOR",
            name = "Variable Resistor",
            category = ComponentCategory.BASIC,
            description = "Two-terminal adjustable rheostat.",
            defaultValue = "10kΩ",
            prefix = "VR",
            icon = Icons.Default.LinearScale,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "CAPACITOR",
            name = "Capacitor",
            category = ComponentCategory.BASIC,
            description = "Non-polarized ceramic capacitor for decoupling and filtering.",
            defaultValue = "100nF",
            prefix = "C",
            icon = Icons.Default.SettingsInputComponent,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "POL_CAPACITOR",
            name = "Polarized Capacitor",
            category = ComponentCategory.BASIC,
            description = "Electrolytic capacitor. Long lead is (+), short lead is (-).",
            defaultValue = "100μF",
            prefix = "CP",
            icon = Icons.Default.SettingsInputComponent,
            pins = listOf(
                PinDef("pos", "+", -1f, 0f),
                PinDef("neg", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "INDUCTOR",
            name = "Inductor",
            category = ComponentCategory.BASIC,
            description = "Coiled conductor storing energy in a magnetic field.",
            defaultValue = "10mH",
            prefix = "L",
            icon = Icons.Default.GraphicEq,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),

        // SEMICONDUCTORS
        LibraryComponent(
            type = "DIODE",
            name = "Diode",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "Rectifier PN junction allowing current flow in only one direction.",
            defaultValue = "1N4007",
            prefix = "D",
            icon = Icons.Default.PlayArrow,
            pins = listOf(
                PinDef("anode", "+", -1f, 0f),
                PinDef("cathode", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "LED",
            name = "LED",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "Light Emitting Diode with forward voltage drop (~2.0V).",
            defaultValue = "Red (2.0V)",
            prefix = "LED",
            icon = Icons.Default.Lightbulb,
            pins = listOf(
                PinDef("anode", "A(+)", -1f, 0f),
                PinDef("cathode", "K(-)", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "ZENER_DIODE",
            name = "Zener Diode",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "Maintains constant reverse breakdown voltage for reference/clamping.",
            defaultValue = "5.1V",
            prefix = "ZD",
            icon = Icons.Default.PlayArrow,
            pins = listOf(
                PinDef("anode", "+", -1f, 0f),
                PinDef("cathode", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "PHOTODIODE",
            name = "Photodiode",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "Semiconductor diode converting light photon absorption into current.",
            defaultValue = "IR Photo",
            prefix = "PD",
            icon = Icons.Default.BrightnessMedium,
            pins = listOf(
                PinDef("anode", "+", -1f, 0f),
                PinDef("cathode", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "TRANSISTOR_NPN",
            name = "Transistor (NPN)",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "BJT current amplifier/switch. Controlled by Base current.",
            defaultValue = "2N2222",
            prefix = "Q",
            icon = Icons.Default.Hub,
            pins = listOf(
                PinDef("base", "B", -1f, 0f),
                PinDef("collector", "C", 0.5f, -1f),
                PinDef("emitter", "E", 0.5f, 1f)
            )
        ),
        LibraryComponent(
            type = "TRANSISTOR_PNP",
            name = "Transistor (PNP)",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "BJT high-side switch turned on when base is pulled low.",
            defaultValue = "2N3906",
            prefix = "Q",
            icon = Icons.Default.Hub,
            pins = listOf(
                PinDef("base", "B", -1f, 0f),
                PinDef("emitter", "E", 0.5f, -1f),
                PinDef("collector", "C", 0.5f, 1f)
            )
        ),
        LibraryComponent(
            type = "MOSFET",
            name = "MOSFET (N-Ch)",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "Voltage-controlled Field Effect Transistor for high-efficiency switching.",
            defaultValue = "IRF540N",
            prefix = "M",
            icon = Icons.Default.Hub,
            pins = listOf(
                PinDef("gate", "G", -1f, 0f),
                PinDef("drain", "D", 0.5f, -1f),
                PinDef("source", "S", 0.5f, 1f)
            )
        ),
        LibraryComponent(
            type = "VOLTAGE_REG",
            name = "Voltage Regulator",
            category = ComponentCategory.SEMICONDUCTORS,
            description = "Linear 3-pin IC providing steady regulated DC output.",
            defaultValue = "LM7805 (5V)",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in", "IN", -1f, 0f),
                PinDef("gnd", "GND", 0f, 1f),
                PinDef("out", "OUT", 1f, 0f)
            )
        ),

        // INPUT / OUTPUT
        LibraryComponent(
            type = "PUSH_BUTTON",
            name = "Push Button",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Normally-open momentary tactile tactile switch.",
            defaultValue = "Tactile",
            prefix = "SW",
            icon = Icons.Default.RadioButtonChecked,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "SWITCH",
            name = "Switch (SPST)",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Single-pole single-throw toggle switch.",
            defaultValue = "SPST",
            prefix = "SW",
            icon = Icons.Default.ToggleOn,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "TOGGLE_SWITCH",
            name = "Toggle Switch (SPDT)",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Single-pole double-throw switch for routing signals.",
            defaultValue = "SPDT",
            prefix = "SW",
            icon = Icons.Default.AltRoute,
            pins = listOf(
                PinDef("com", "COM", -1f, 0f),
                PinDef("pin1", "1", 1f, -0.6f),
                PinDef("pin2", "2", 1f, 0.6f)
            )
        ),
        LibraryComponent(
            type = "LDR",
            name = "LDR (Photoresistor)",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Light Dependent Resistor; resistance decreases as light increases.",
            defaultValue = "10kΩ Dark",
            prefix = "LDR",
            icon = Icons.Default.Brightness6,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "THERMISTOR",
            name = "Thermistor (NTC)",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Temperature-sensitive resistor with negative temperature coefficient.",
            defaultValue = "10kΩ NTC",
            prefix = "TH",
            icon = Icons.Default.DeviceThermostat,
            pins = listOf(
                PinDef("pin1", "1", -1f, 0f),
                PinDef("pin2", "2", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "IR_SENSOR",
            name = "IR Sensor",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Infrared receiver module for proximity or remote sensing.",
            defaultValue = "38kHz",
            prefix = "IR",
            icon = Icons.Default.Sensors,
            pins = listOf(
                PinDef("vcc", "VCC", -0.6f, -1f),
                PinDef("out", "OUT", 0f, 1f),
                PinDef("gnd", "GND", 0.6f, -1f)
            )
        ),
        LibraryComponent(
            type = "ULTRASONIC",
            name = "Ultrasonic Sensor",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Distance measurement sensor with Transmitter and Receiver transducers.",
            defaultValue = "HC-SR04",
            prefix = "US",
            icon = Icons.Default.Sensors,
            pins = listOf(
                PinDef("vcc", "VCC", -0.8f, 1f),
                PinDef("trig", "TRIG", -0.25f, 1f),
                PinDef("echo", "ECHO", 0.25f, 1f),
                PinDef("gnd", "GND", 0.8f, 1f)
            )
        ),
        LibraryComponent(
            type = "BUZZER",
            name = "Buzzer",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Piezoelectric sound generator with internal oscillator.",
            defaultValue = "5V Active",
            prefix = "BZ",
            icon = Icons.Default.VolumeUp,
            pins = listOf(
                PinDef("pos", "+", -1f, 0f),
                PinDef("neg", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "SPEAKER",
            name = "Speaker",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Electromagnetic voice-coil transducer for audio waveforms.",
            defaultValue = "8Ω 0.5W",
            prefix = "SPK",
            icon = Icons.Default.Speaker,
            pins = listOf(
                PinDef("pin1", "+", -1f, 0f),
                PinDef("pin2", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "MOTOR",
            name = "DC Motor",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Continuous rotation direct current electric motor.",
            defaultValue = "5V DC",
            prefix = "M",
            icon = Icons.Default.Sync,
            pins = listOf(
                PinDef("pos", "+", -1f, 0f),
                PinDef("neg", "-", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "SERVO_MOTOR",
            name = "Servo Motor",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Angle-controlled actuator with built-in closed loop feedback.",
            defaultValue = "SG90 (180°)",
            prefix = "SRV",
            icon = Icons.Default.Build,
            pins = listOf(
                PinDef("sig", "PWM", -0.6f, 1f),
                PinDef("vcc", "5V", 0f, 1f),
                PinDef("gnd", "GND", 0.6f, 1f)
            )
        ),
        LibraryComponent(
            type = "RELAY",
            name = "Relay",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Electromechanical switch isolating low-power logic from high-power loads.",
            defaultValue = "5V SPDT",
            prefix = "RLY",
            icon = Icons.Default.ElectricalServices,
            pins = listOf(
                PinDef("c1", "COIL1", -1f, -0.5f),
                PinDef("c2", "COIL2", -1f, 0.5f),
                PinDef("com", "COM", 1f, -0.5f),
                PinDef("no", "NO", 1f, 0.5f)
            )
        ),
        LibraryComponent(
            type = "SEVEN_SEG",
            name = "7-Segment Display",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Single digit numerical display with common cathode.",
            defaultValue = "Common Cathode",
            prefix = "DISP",
            icon = Icons.Default.Pin,
            pins = listOf(
                PinDef("a", "A", -0.6f, -1f),
                PinDef("b", "B", 0.6f, -1f),
                PinDef("vcc", "V+", 0f, -1f),
                PinDef("gnd", "GND", 0f, 1f)
            )
        ),
        LibraryComponent(
            type = "RGB_LED",
            name = "RGB LED",
            category = ComponentCategory.INPUT_OUTPUT,
            description = "Four-pin tri-color LED capable of mixing Red, Green, and Blue.",
            defaultValue = "Common Cathode",
            prefix = "RGB",
            icon = Icons.Default.Palette,
            pins = listOf(
                PinDef("r", "R", -0.75f, 1f),
                PinDef("g", "G", -0.25f, 1f),
                PinDef("b", "B", 0.25f, 1f),
                PinDef("gnd", "GND", 0.75f, 1f)
            )
        ),

        // POWER
        LibraryComponent(
            type = "BATTERY",
            name = "Battery",
            category = ComponentCategory.POWER,
            description = "Chemical cell voltage supply (9V standard).",
            defaultValue = "9V",
            prefix = "BT",
            icon = Icons.Default.BatteryChargingFull,
            pins = listOf(
                PinDef("pos", "+", 0f, -1f),
                PinDef("neg", "-", 0f, 1f)
            )
        ),
        LibraryComponent(
            type = "DC_SOURCE",
            name = "DC Voltage Source",
            category = ComponentCategory.POWER,
            description = "Regulated constant direct current supply.",
            defaultValue = "5V DC",
            prefix = "V",
            icon = Icons.Default.Power,
            pins = listOf(
                PinDef("pos", "+", 0f, -1f),
                PinDef("neg", "-", 0f, 1f)
            )
        ),
        LibraryComponent(
            type = "VCC",
            name = "VCC",
            category = ComponentCategory.POWER,
            description = "Positive power rail node (+5V or +3.3V).",
            defaultValue = "+5V",
            prefix = "VCC",
            icon = Icons.Default.Bolt,
            pins = listOf(
                PinDef("vcc", "VCC", 0f, 1f)
            )
        ),
        LibraryComponent(
            type = "GROUND",
            name = "GND",
            category = ComponentCategory.POWER,
            description = "Zero-volt reference and circuit return path.",
            defaultValue = "0V",
            prefix = "GND",
            icon = Icons.Default.VerticalAlignBottom,
            pins = listOf(
                PinDef("gnd", "GND", 0f, -1f)
            )
        ),

        // DIGITAL / IC
        LibraryComponent(
            type = "AND_GATE",
            name = "AND Gate",
            category = ComponentCategory.DIGITAL_IC,
            description = "Output HIGH only if all inputs are HIGH (Y = A · B).",
            defaultValue = "74HC08",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in1", "A", -1f, -0.4f),
                PinDef("in2", "B", -1f, 0.4f),
                PinDef("out", "Y", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "OR_GATE",
            name = "OR Gate",
            category = ComponentCategory.DIGITAL_IC,
            description = "Output HIGH if any input is HIGH (Y = A + B).",
            defaultValue = "74HC32",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in1", "A", -1f, -0.4f),
                PinDef("in2", "B", -1f, 0.4f),
                PinDef("out", "Y", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "NOT_GATE",
            name = "NOT Gate (Inverter)",
            category = ComponentCategory.DIGITAL_IC,
            description = "Inverts digital logic state (Y = A').",
            defaultValue = "74HC04",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in", "A", -1f, 0f),
                PinDef("out", "Y", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "NAND_GATE",
            name = "NAND Gate",
            category = ComponentCategory.DIGITAL_IC,
            description = "Universal logic gate; inverted AND operation.",
            defaultValue = "74HC00",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in1", "A", -1f, -0.4f),
                PinDef("in2", "B", -1f, 0.4f),
                PinDef("out", "Y", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "NOR_GATE",
            name = "NOR Gate",
            category = ComponentCategory.DIGITAL_IC,
            description = "Universal logic gate; inverted OR operation.",
            defaultValue = "74HC02",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in1", "A", -1f, -0.4f),
                PinDef("in2", "B", -1f, 0.4f),
                PinDef("out", "Y", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "XOR_GATE",
            name = "XOR Gate",
            category = ComponentCategory.DIGITAL_IC,
            description = "Exclusive OR; HIGH when inputs differ.",
            defaultValue = "74HC86",
            prefix = "U",
            icon = Icons.Default.Memory,
            pins = listOf(
                PinDef("in1", "A", -1f, -0.4f),
                PinDef("in2", "B", -1f, 0.4f),
                PinDef("out", "Y", 1f, 0f)
            )
        ),
        LibraryComponent(
            type = "TIMER_555",
            name = "555 Timer IC",
            category = ComponentCategory.DIGITAL_IC,
            description = "Precision timing IC for monostable pulses and astable oscillators.",
            defaultValue = "NE555",
            prefix = "U",
            icon = Icons.Default.Timer,
            pins = listOf(
                PinDef("vcc", "VCC", -0.7f, -1f),
                PinDef("trig", "TRIG", -1f, 0f),
                PinDef("out", "OUT", 1f, 0f),
                PinDef("gnd", "GND", 0f, 1f)
            )
        )
    )

    fun getPinsForType(type: String): List<PinDef> {
        val found = items.firstOrNull { it.type.equals(type, ignoreCase = true) }
        if (found != null) return found.pins
        // Fallback generic 2-pin
        return listOf(
            PinDef("pin1", "1", -1f, 0f),
            PinDef("pin2", "2", 1f, 0f)
        )
    }

    fun getComponent(type: String): LibraryComponent? {
        return items.firstOrNull { it.type.equals(type, ignoreCase = true) }
    }
}
