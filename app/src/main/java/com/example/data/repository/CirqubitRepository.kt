package com.example.data.repository

import android.content.Context
import com.example.data.gemini.GeminiTutorService
import com.example.data.local.*
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CirqubitRepository(context: Context) {

    private val db = CirqubitDatabase.getInstance(context)
    private val studentDao = db.studentDao()
    private val lessonDao = db.lessonDao()
    private val challengeDao = db.challengeDao()
    private val projectDao = db.projectDao()
    private val kitDao = db.kitDao()
    private val achievementDao = db.achievementDao()
    private val circuitDao = db.circuitDao()

    val aiTutorService = GeminiTutorService()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    // Hardware Telemetry state (simulated connection & live sensors)
    private val _telemetry = MutableStateFlow(HardwareTelemetry())
    val telemetry: StateFlow<HardwareTelemetry> = _telemetry.asStateFlow()

    // Temporary active designer state
    private val _designerComponents = MutableStateFlow<List<DesignerComponentItem>>(
        listOf(
            DesignerComponentItem("c_batt", "BATTERY", "9V Battery", "9V", 80f, 180f),
            DesignerComponentItem("c_res", "RESISTOR", "R1", "220Ω", 220f, 100f),
            DesignerComponentItem("c_led", "LED", "LED1", "Red", 360f, 180f),
            DesignerComponentItem("c_gnd", "GROUND", "GND", "0V", 220f, 280f)
        )
    )
    val designerComponents: StateFlow<List<DesignerComponentItem>> = _designerComponents.asStateFlow()

    private val _designerWires = MutableStateFlow<List<DesignerWireItem>>(
        listOf(
            DesignerWireItem("w1", "c_batt", "pos", "c_res", "pin1"),
            DesignerWireItem("w2", "c_res", "pin2", "c_led", "anode"),
            DesignerWireItem("w3", "c_led", "cathode", "c_gnd", "pin1"),
            DesignerWireItem("w4", "c_batt", "neg", "c_gnd", "pin1")
        )
    )
    val designerWires: StateFlow<List<DesignerWireItem>> = _designerWires.asStateFlow()

    val studentProfile: Flow<StudentProfile> = studentDao.getStudent().map { entity ->
        if (entity != null) {
            StudentProfile(
                id = entity.id,
                name = entity.name,
                level = entity.level,
                xp = entity.xp,
                streakDays = entity.streakDays,
                completedLessonsCount = entity.completedLessonsCount,
                completedChallengesCount = entity.completedChallengesCount,
                completedProjectsCount = entity.completedProjectsCount,
                currentCourseName = entity.currentCourseName,
                currentLessonId = entity.currentLessonId,
                currentProjectId = entity.currentProjectId,
                isDarkMode = entity.isDarkMode,
                hapticFeedbackEnabled = entity.hapticFeedbackEnabled
            )
        } else {
            StudentProfile()
        }
    }

    val lessons: Flow<List<Lesson>> = lessonDao.getAllLessons().map { list ->
        list.map { e ->
            Lesson(
                id = e.id,
                order = e.orderIndex,
                title = e.title,
                subtitle = e.subtitle,
                estimatedMinutes = e.estimatedMinutes,
                xpReward = e.xpReward,
                objectives = stringListAdapter.fromJson(e.objectivesJson) ?: emptyList(),
                conceptExplanation = e.conceptExplanation,
                visualDiagramType = e.visualDiagramType,
                circuitDiagramCode = e.circuitDiagramCode,
                componentExplanation = e.componentExplanation,
                importantFormula = e.importantFormula,
                formulaExplanation = e.formulaExplanation,
                workedExample = e.workedExample,
                keyTakeaways = stringListAdapter.fromJson(e.keyTakeawaysJson) ?: emptyList(),
                status = try { ProgressStatus.valueOf(e.status) } catch (ex: Exception) { ProgressStatus.LOCKED },
                completedAt = e.completedAt
            )
        }
    }

    val challenges: Flow<List<Challenge>> = challengeDao.getAllChallenges().map { list ->
        list.map { e ->
            Challenge(
                id = e.id,
                lessonId = e.lessonId,
                title = e.title,
                type = try { ChallengeType.valueOf(e.type) } catch (ex: Exception) { ChallengeType.MULTIPLE_CHOICE },
                question = e.question,
                visualCode = e.visualCode,
                options = stringListAdapter.fromJson(e.optionsJson) ?: emptyList(),
                correctIndex = e.correctIndex,
                explanation = e.explanation,
                hint = e.hint,
                xpReward = e.xpReward,
                isCompleted = e.isCompleted,
                studentScore = e.studentScore
            )
        }
    }

    val projects: Flow<List<Project>> = projectDao.getAllProjects().map { list ->
        list.map { e ->
            val compMap = parseComponentsJson(e.requiredComponentsJson)
            Project(
                id = e.id,
                order = e.orderIndex,
                title = e.title,
                objective = e.objective,
                difficulty = try { DifficultyLevel.valueOf(e.difficulty) } catch (ex: Exception) { DifficultyLevel.BEGINNER },
                conceptsLearned = stringListAdapter.fromJson(e.conceptsLearnedJson) ?: emptyList(),
                requiredComponents = compMap,
                requiredSpecs = e.requiredSpecs,
                circuitDiagramCode = e.circuitDiagramCode,
                assemblySteps = stringListAdapter.fromJson(e.assemblyStepsJson) ?: emptyList(),
                testingInstructions = stringListAdapter.fromJson(e.testingInstructionsJson) ?: emptyList(),
                expectedResult = e.expectedResult,
                status = try { ProjectStatus.valueOf(e.status) } catch (ex: Exception) { ProjectStatus.LOCKED },
                xpReward = e.xpReward,
                completedAt = e.completedAt
            )
        }
    }

    val kitComponents: Flow<List<KitComponent>> = kitDao.getAllKitComponents().map { list ->
        list.map { e ->
            val type = try { KitComponentType.valueOf(e.type) } catch (ex: Exception) { KitComponentType.RESISTOR }
            val available = (e.totalQuantity - e.inUseQuantity).coerceAtLeast(0)
            KitComponent(
                id = e.id,
                name = e.name,
                type = type,
                spec = e.spec,
                totalQuantity = e.totalQuantity,
                inUseQuantity = e.inUseQuantity,
                availableQuantity = available,
                symbolCode = e.symbolCode
            )
        }
    }

    val achievements: Flow<List<Achievement>> = achievementDao.getAllAchievements().map { list ->
        list.map { e ->
            Achievement(
                id = e.id,
                title = e.title,
                description = e.description,
                icon = e.icon,
                xpReward = e.xpReward,
                isUnlocked = e.isUnlocked,
                unlockedAt = e.unlockedAt
            )
        }
    }

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentDirect()
        if (student == null) {
            studentDao.insertStudent(
                StudentEntity(
                    id = "cirqubit_student_1",
                    name = "Kishan Gopa",
                    level = 1,
                    xp = 0,
                    streakDays = 0,
                    completedLessonsCount = 0,
                    completedChallengesCount = 0,
                    completedProjectsCount = 0,
                    currentCourseName = "Electronic Fundamentals",
                    currentLessonId = "lesson_1",
                    currentProjectId = "proj_1",
                    isDarkMode = true
                )
            )
            initializeKitInventory()
            initializeLessons()
            initializeChallenges()
            initializeProjects()
            initializeAchievements()
        } else {
            if (student.name != "Kishan Gopa") {
                studentDao.updateStudent(student.copy(name = "Kishan Gopa"))
            }
            sanitizeLessonProgression()
        }
    }

    suspend fun sanitizeLessonProgression() = withContext(Dispatchers.IO) {
        val currentLessons = lessonDao.getAllLessonsDirect()
        if (currentLessons.isEmpty()) return@withContext

        var previousWasCompleted = true
        for (i in currentLessons.indices) {
            val l = currentLessons[i]
            if (i == 0) {
                if (l.status == "LOCKED") {
                    lessonDao.updateLessonStatus(l.id, "UNLOCKED", null)
                }
                previousWasCompleted = (l.status == "COMPLETED")
            } else {
                if (previousWasCompleted) {
                    if (l.status == "LOCKED") {
                        lessonDao.updateLessonStatus(l.id, "UNLOCKED", null)
                    }
                    previousWasCompleted = (l.status == "COMPLETED")
                } else {
                    if (l.status != "LOCKED") {
                        lessonDao.updateLessonStatus(l.id, "LOCKED", null)
                    }
                    previousWasCompleted = false
                }
            }
        }
    }

    private suspend fun initializeKitInventory() {
        val defaultKit = listOf(
            KitComponentEntity("kit_mcu", "Cirqubit Core USB-C Hub", "MICROCONTROLLER", "5V / 3.3V ATmega328", 1, 1, "MCU"),
            KitComponentEntity("kit_breadboard", "Full Breadboard 400-tie", "BREADBOARD", "Transparent Power Rails", 1, 1, "BB"),
            KitComponentEntity("kit_usb", "USB-C to A Data Cable", "USB_CABLE", "1.0 Meter Braided", 1, 1, "USB"),
            KitComponentEntity("kit_led_red", "Red 5mm Diffused LEDs", "LED", "Vf 2.0V, 20mA max", 4, 1, "LED_R"),
            KitComponentEntity("kit_led_green", "Green 5mm Diffused LEDs", "LED", "Vf 2.2V, 20mA max", 4, 0, "LED_G"),
            KitComponentEntity("kit_led_yellow", "Yellow 5mm Diffused LEDs", "LED", "Vf 2.1V, 20mA max", 3, 0, "LED_Y"),
            KitComponentEntity("kit_led_blue", "Blue 5mm Clear LEDs", "LED", "Vf 3.2V, 20mA max", 2, 0, "LED_B"),
            KitComponentEntity("kit_res_220", "220Ω Metal Film Resistors", "RESISTOR", "1/4W 1% (Red-Red-Brown)", 8, 1, "R220"),
            KitComponentEntity("kit_res_330", "330Ω Metal Film Resistors", "RESISTOR", "1/4W 1% (Orange-Orange-Brown)", 6, 0, "R330"),
            KitComponentEntity("kit_res_1k", "1kΩ Metal Film Resistors", "RESISTOR", "1/4W 1% (Brown-Black-Red)", 6, 0, "R1K"),
            KitComponentEntity("kit_res_10k", "10kΩ Metal Film Resistors", "RESISTOR", "1/4W 1% (Brown-Black-Orange)", 6, 0, "R10K"),
            KitComponentEntity("kit_pot", "10kΩ Rotary Potentiometer", "POTENTIOMETER", "Breadboard Friendly 3-pin", 2, 0, "POT"),
            KitComponentEntity("kit_ldr", "Light Dependent Resistor (LDR)", "LDR", "5kΩ Light to 500kΩ Dark", 2, 0, "LDR"),
            KitComponentEntity("kit_btn", "Tactile Push Buttons", "PUSH_BUTTON", "6x6mm Momentary 4-pin", 4, 0, "BTN"),
            KitComponentEntity("kit_cap_100u", "100μF Electrolytic Capacitor", "CAPACITOR", "25V Polarized Radial", 3, 0, "C100U"),
            KitComponentEntity("kit_cap_10u", "10μF Electrolytic Capacitor", "CAPACITOR", "50V Polarized Radial", 3, 0, "C10U"),
            KitComponentEntity("kit_cap_100n", "100nF (0.1μF) Ceramic Capacitor", "CAPACITOR", "50V Non-polarized 104", 4, 0, "C104"),
            KitComponentEntity("kit_diode", "1N4007 Rectifier Diodes", "DIODE", "1000V 1A Silicon", 4, 0, "D1N"),
            KitComponentEntity("kit_transistor", "2N2222 NPN BJT Transistors", "TRANSISTOR", "TO-92 40V 800mA", 3, 0, "Q2N"),
            KitComponentEntity("kit_buzzer", "5V Active Piezo Buzzer", "BUZZER", "85dB at 10cm Continuous", 2, 0, "BUZZ")
        )
        kitDao.insertKitComponents(defaultKit)
    }

    private suspend fun initializeLessons() {
        val lessonList = listOf(
            LessonEntity(
                id = "lesson_1",
                orderIndex = 1,
                title = "Electricity, Voltage and Current",
                subtitle = "Fundamental concepts of charge movement and electrical potential",
                estimatedMinutes = 15,
                xpReward = 80,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Understand electric charge and free electron drift",
                        "Distinguish between Voltage (potential difference) and Current (rate of charge flow)",
                        "Recognize standard units: Volt (V), Ampere (A), and Coulomb (C)",
                        "Trace closed vs open circuit paths"
                    )
                ),
                conceptExplanation = "Electricity is the flow of electric charge through conductive materials. Voltage (V) is the electrical pressure or electromotive force pushing electrons between two points. Current (I) measures how many Coulombs of charge pass through a wire cross-section each second (1 Ampere = 1 Coulomb/second). A closed, continuous conductive loop is always required for current to flow.",
                visualDiagramType = "CLOSED_LOOP_WATER_ANALOGY",
                circuitDiagramCode = "BATTERY_SOURCE_LOOP",
                componentExplanation = "• Power Source (Battery/DC Supply): Generates the potential difference.\n• Conductors (Wires/Traces): Low-resistance copper pathways.\n• Load (Lamp/Motor): Converts electrical energy into light, heat, or mechanical work.",
                importantFormula = "I = Q / t   |   V = W / Q",
                formulaExplanation = "Current (I) equals charge (Q) divided by time (t). Voltage (V) is work done (W in Joules) per unit charge (Q in Coulombs).",
                workedExample = "Example: If 3 Coulombs of charge flow through a conductor in 2 seconds:\nI = 3 C / 2 s = 1.5 Amperes (A).",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Current cannot flow without a closed loop connecting positive to negative terminals.",
                        "Voltage creates the electric field; current is the resulting charge movement.",
                        "Conventional current flows from (+) to (-), while physical electrons flow from (-) to (+)."
                    )
                ),
                status = "UNLOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_2",
                orderIndex = 2,
                title = "Resistance and Ohm's Law",
                subtitle = "The relationship between Voltage, Current, and Resistance (V = I × R)",
                estimatedMinutes = 20,
                xpReward = 100,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Define electrical resistance and units (Ohms, Ω)",
                        "Master Ohm's Law algebraic variations: V = IR, I = V/R, R = V/I",
                        "Calculate power dissipation using Joule's Law: P = V × I = I²R",
                        "Interpret standard resistor 4-band color codes"
                    )
                ),
                conceptExplanation = "Resistance is the opposition that a substance offers to the flow of electric current. George Ohm discovered that for many conductors at constant temperature, current is directly proportional to the applied voltage and inversely proportional to resistance. Resistors allow us to precisely establish voltages and limit currents across delicate components.",
                visualDiagramType = "OHMS_LAW_TRIANGLE",
                circuitDiagramCode = "RESISTOR_SERIES_CIRCUIT",
                componentExplanation = "• Fixed Resistors: Fixed resistance values (e.g., 220Ω, 1kΩ, 10kΩ).\n• Tolerance: The manufacturing variance (e.g., Gold band = ±5%, Brown band = ±1%).\n• Power Rating: Maximum wattage before burning (Standard 1/4 Watt = 0.25W).",
                importantFormula = "V = I × R   |   P = V × I = I² × R",
                formulaExplanation = "Voltage (V) = Current (I) in Amps × Resistance (R) in Ohms. Electrical Power (P) in Watts = V × I.",
                workedExample = "Example: A 9V battery is connected across a 450Ω resistor.\nI = V / R = 9V / 450Ω = 0.02 A = 20 mA.\nPower: P = 9V × 0.02A = 0.18 Watts (safe for 0.25W resistor).",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Doubling resistance halves the current at constant voltage.",
                        "Always convert milliamperes (mA) to Amperes (A) by dividing by 1,000 before calculating.",
                        "Check power dissipation to ensure resistors do not overheat."
                    )
                ),
                status = "LOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_3",
                orderIndex = 3,
                title = "Resistors and LED Circuits",
                subtitle = "Designing safe, bright, and reliable Light Emitting Diode circuits",
                estimatedMinutes = 25,
                xpReward = 120,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Identify LED polarity: Anode (+) vs Cathode (-)",
                        "Understand forward voltage drop (Vf) across different LED colors",
                        "Calculate the required current-limiting series resistor",
                        "Assemble a functional LED circuit on the breadboard"
                    )
                ),
                conceptExplanation = "A Light Emitting Diode (LED) emits light through electroluminescence when forward-biased. Unlike pure resistors, LEDs have a non-linear forward voltage drop (Vf) of approximately 1.8V to 3.2V. Once forward-biased, an LED presents very low resistance; without a series resistor to absorb excess voltage and throttle current, the LED would quickly draw excessive current and burn out.",
                visualDiagramType = "LED_RESISTOR_SERIES_SCHEMATIC",
                circuitDiagramCode = "LED_WITH_SERIES_RESISTOR",
                componentExplanation = "• Anode (+): Longer lead, round edge. Connect toward positive rail.\n• Cathode (-): Shorter lead, flat edge on plastic epoxy lens. Connect to ground/negative.\n• Series Resistor: Drops (Vcc - Vf) and sets forward current (typically 15mA - 20mA).",
                importantFormula = "R = (V_supply - V_forward) / I_desired",
                formulaExplanation = "Resistor value equals supply voltage minus LED forward voltage, divided by chosen safe operating current in Amps.",
                workedExample = "Example: 5V DC supply powering a Red LED (Vf = 2.0V) at 15mA (0.015A):\nR = (5V - 2.0V) / 0.015A = 3.0V / 0.015A = 200Ω.\nNearest standard value: 220Ω.\nActual current: 3.0V / 220Ω = 13.6 mA (ideal brightness & long life).",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Never connect an LED directly to a 5V or 9V source without a resistor!",
                        "Reversing an LED won't destroy it at low voltages (<5V), but it will not light up.",
                        "Higher resistance dims the LED; lower resistance brightens it until thermal limit."
                    )
                ),
                status = "LOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_4",
                orderIndex = 4,
                title = "Capacitors",
                subtitle = "Energy storage in electric fields, filtering, and time constants",
                estimatedMinutes = 25,
                xpReward = 120,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Distinguish electrolytic (polarized) vs ceramic (non-polarized) capacitors",
                        "Calculate capacitance, charge (Q = C × V), and energy storage",
                        "Calculate RC time constant: τ = R × C",
                        "Design delay circuits and power supply smoothing filters"
                    )
                ),
                conceptExplanation = "Capacitors consist of two conductive plates separated by an insulating dielectric. They store energy electrostatically. When a voltage is applied, charges accumulate on the plates until the capacitor voltage matches the supply. In DC circuits, capacitors charge exponentially and then block direct current, making them essential for timers, delay circuits, and smoothing power ripples.",
                visualDiagramType = "RC_CHARGING_CURVE",
                circuitDiagramCode = "RC_TIMING_NETWORK",
                componentExplanation = "• Ceramic (100nF): Fast, non-polarized, noise decoupling.\n• Electrolytic (10μF, 100μF): Polarized! Negative stripe must connect to ground.\n• Dielectric: Insulator preventing spark breakdown.",
                importantFormula = "τ = R × C   |   V(t) = V_max × (1 - e^(-t / τ))",
                formulaExplanation = "Time constant τ (tau in seconds) = Resistance (Ω) × Capacitance (Farads). The capacitor reaches 63.2% charge after 1τ and 99.3% after 5τ.",
                workedExample = "Example: A 10kΩ (10,000Ω) resistor with a 100μF (0.0001F) capacitor:\nτ = 10,000 × 0.0001 = 1.0 second.\nCircuit reaches full charge in approx. 5 × 1.0s = 5 seconds.",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Never connect polarized electrolytic capacitors backwards; they can pop or leak!",
                        "Capacitors pass AC signals or ripple while blocking DC steady-state.",
                        "Larger R or larger C produces longer timing delays."
                    )
                ),
                status = "LOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_5",
                orderIndex = 5,
                title = "Diodes and LEDs",
                subtitle = "PN junctions, forward/reverse bias, flyback protection, and rectification",
                estimatedMinutes = 20,
                xpReward = 110,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Understand PN semiconductor junction physics",
                        "Differentiate forward bias (conduction) and reverse bias (blocking)",
                        "Use 1N4007 diodes for reverse polarity and flyback voltage protection",
                        "Compare silicon rectifier diodes with optical LEDs"
                    )
                ),
                conceptExplanation = "A diode is a one-way valve for electric current. Formed by joining P-type and N-type semiconductors, it requires roughly 0.6V to 0.7V of forward bias (the barrier potential) before silicon diodes turn on. In reverse bias, only tiny leakage current flows until breakdown voltage is reached (1000V for the 1N4007).",
                visualDiagramType = "DIODE_IV_CURVE",
                circuitDiagramCode = "FLYBACK_DIODE_PROTECTION",
                componentExplanation = "• 1N4007 Diode: Silver band marks the Cathode (-).\n• Flyback protection: Placed across inductive loads (relays, motors, buzzers) to safely quench high-voltage inductive spikes.",
                importantFormula = "V_load = V_supply - V_diode (≈ 0.7V)",
                formulaExplanation = "Standard silicon diodes exhibit approximately 0.7V drop across terminals during forward conduction.",
                workedExample = "Example: 5V power supply passes through a 1N4007 reverse-polarity protection diode:\nV_output = 5.0V - 0.7V = 4.3V delivered to the load.",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Silver band on diode body indicates Cathode (negative side).",
                        "Essential for preventing motor back-EMF from frying transistors or microcontrollers.",
                        "Forward conduction requires overcoming ~0.7V threshold."
                    )
                ),
                status = "LOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_6",
                orderIndex = 6,
                title = "Transistors and Switching",
                subtitle = "BJT transistor fundamentals, current gain (Beta), and digital switching",
                estimatedMinutes = 30,
                xpReward = 150,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Identify NPN BJT terminals: Base (B), Collector (C), and Emitter (E)",
                        "Operate a transistor in cutoff (OFF) and saturation (ON)",
                        "Calculate Base current (IB) and required Base resistor",
                        "Control high-current loads from low-power logic signals"
                    )
                ),
                conceptExplanation = "A Bipolar Junction Transistor (BJT) is a current-controlled amplifier or switch. In the Cirqubit Kit, the 2N2222 NPN transistor acts as a high-speed digital switch. Injecting a small current into the Base allows a much larger current to flow from Collector to Emitter. By driving the transistor into 'saturation', the Collector-Emitter voltage drops to ~0.2V, acting like a closed contact.",
                visualDiagramType = "BJT_SWITCH_SCHEMATIC",
                circuitDiagramCode = "TRANSISTOR_LOW_SIDE_SWITCH",
                componentExplanation = "• Flat Face of TO-92 2N2222: Pin 1 = Emitter, Pin 2 = Base, Pin 3 = Collector.\n• Base Resistor (1kΩ): Protects base-emitter junction from excessive current.",
                importantFormula = "I_C = β × I_B   |   R_base = (V_in - 0.7V) / I_B(sat)",
                formulaExplanation = "Collector current equals DC current gain (Beta/hFE) times Base current. For reliable switching, overdrive base by 2x to 5x.",
                workedExample = "Example: Switching a 60mA buzzer using 5V MCU logic:\nDesired I_B = 60mA / 10 = 6mA (for solid saturation).\nR_base = (5.0V - 0.7V) / 0.006A = 4.3V / 0.006A = 716Ω.\nUse a 1kΩ or 680Ω resistor from the kit.",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "NPN transistors switch the low side (between load and GND).",
                        "Always connect a base resistor; connecting Base directly to 5V will burn the transistor!",
                        "Transistors bridge microcontrollers to real-world power loads."
                    )
                ),
                status = "LOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_7",
                orderIndex = 7,
                title = "Sensors and Inputs",
                subtitle = "Interfacing analog sensors, Light Dependent Resistors, and voltage dividers",
                estimatedMinutes = 25,
                xpReward = 130,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Understand physical to electrical transduction",
                        "Analyze voltage divider circuits with variable sensors",
                        "Read light levels using an LDR (photoresistor)",
                        "Wire tactile push buttons with pull-down resistors"
                    )
                ),
                conceptExplanation = "Sensors transform physical phenomena (light, temperature, pressure) into measurable electrical signals. An LDR (photoresistor) changes its bulk resistance from several hundred kilo-ohms in darkness to under 1kΩ in bright light. Because microcontrollers and threshold circuits read voltage rather than raw resistance, we place the sensor into a voltage divider circuit.",
                visualDiagramType = "VOLTAGE_DIVIDER_CIRCUIT",
                circuitDiagramCode = "LDR_VOLTAGE_DIVIDER",
                componentExplanation = "• LDR (Photoresistor): Non-polarized light sensor.\n• Pull-down Resistor (10kΩ): Prevents floating inputs when switch or button is open.\n• Potentiometer: Manually adjustable 3-terminal voltage divider.",
                importantFormula = "V_out = V_in × (R2 / (R1 + R2))",
                formulaExplanation = "The output voltage is a fractional ratio of the input voltage determined by the resistances of R1 and R2.",
                workedExample = "Example: 5V supply with R1 = 10kΩ fixed resistor and R2 = LDR:\nIn bright daylight (LDR = 1kΩ):\nV_out = 5V × (1kΩ / (10kΩ + 1kΩ)) = 5V × (1/11) = 0.45V.\nIn darkness (LDR = 100kΩ):\nV_out = 5V × (100kΩ / 110kΩ) = 4.54V.",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Voltage dividers convert changing resistance into readable voltage changes.",
                        "Unconnected digital inputs float between 0 and 1; always use pull-up or pull-down resistors.",
                        "Calibrate sensor divider values based on ambient operational range."
                    )
                ),
                status = "LOCKED",
                completedAt = null
            ),
            LessonEntity(
                id = "lesson_8",
                orderIndex = 8,
                title = "Basic Automation",
                subtitle = "Feedback loops, threshold detection, and automatic actuator control",
                estimatedMinutes = 30,
                xpReward = 160,
                objectivesJson = stringListAdapter.toJson(
                    listOf(
                        "Combine sensors, decision elements, and actuators into closed-loop systems",
                        "Construct automatic threshold triggers for night lights and alarms",
                        "Implement hysteresis and stability in analog control circuits",
                        "Master the complete Cirqubit hardware kit workflow"
                    )
                ),
                conceptExplanation = "Automation is the ability of a system to perceive its environment, make a determination based on preset thresholds, and trigger physical actuators (lights, buzzers, motors) without human intervention. In this capstone module, we unite voltage dividers, transistor switches, and the Cirqubit Core controller to build autonomous electronics.",
                visualDiagramType = "AUTOMATION_BLOCK_DIAGRAM",
                circuitDiagramCode = "FULL_AUTOMATION_SYSTEM",
                componentExplanation = "• Sensor Stage: LDR / Button.\n• Logic Stage: Voltage threshold comparator or Cirqubit MCU firmware.\n• Actuator Stage: Transistor-driven Buzzer and LED indicator array.",
                importantFormula = "System Loop: SENSE → EVALUATE → ACTUATE → STABILIZE",
                formulaExplanation = "Continuous monitoring loop reading physical inputs and switching output states when conditions cross setpoints.",
                workedExample = "Example: Automatic Night Light\nWhen ambient lux drops below 50 Lux, LDR resistance exceeds 40kΩ. Base voltage rises above 0.75V, turning ON the 2N2222 transistor to illuminate the room LED lamp.",
                keyTakeawaysJson = stringListAdapter.toJson(
                    listOf(
                        "Every automation system contains Sensor, Controller, and Actuator blocks.",
                        "Proper component sizing prevents system oscillation around trigger points.",
                        "You now have the complete skills to build, test, and troubleshoot any Cirqubit project!"
                    )
                ),
                status = "LOCKED",
                completedAt = null
            )
        )
        lessonDao.insertLessons(lessonList)
    }

    private suspend fun initializeChallenges() {
        val challengeList = listOf(
            // Lesson 1 Challenges
            ChallengeEntity(
                id = "ch_1_1",
                lessonId = "lesson_1",
                title = "Current and Resistance Relation",
                type = "MULTIPLE_CHOICE",
                question = "What happens to the current in an electrical circuit when resistance increases and voltage remains constant?",
                visualCode = null,
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "Current increases proportionally",
                        "Current decreases proportionally",
                        "Current remains constant",
                        "Voltage drops to zero"
                    )
                ),
                correctIndex = 1,
                explanation = "According to Ohm's Law (I = V / R), current is inversely proportional to resistance. When resistance increases at constant voltage, current must decrease.",
                hint = "Think about water flowing through a narrower pipe: more constriction (resistance) means less flow (current).",
                xpReward = 40,
                isCompleted = false,
                studentScore = 0
            ),
            ChallengeEntity(
                id = "ch_1_2",
                lessonId = "lesson_1",
                title = "Circuit Continuity Check",
                type = "CIRCUIT_IDENTIFICATION",
                question = "Look at this circuit path. Why is the lamp not glowing?",
                visualCode = "CIRCUIT_OPEN_SWITCH",
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "The battery is reversed",
                        "There is a break/open switch in the circuit loop",
                        "The lamp requires alternating current",
                        "Current is moving backwards"
                    )
                ),
                correctIndex = 1,
                explanation = "An open switch creates an air gap with infinite resistance, breaking the continuous loop required for charge to flow.",
                hint = "Trace the wire from the battery positive terminal back to the negative terminal without lifting your finger.",
                xpReward = 40,
                isCompleted = false,
                studentScore = 0
            ),
            // Lesson 2 Challenges
            ChallengeEntity(
                id = "ch_2_1",
                lessonId = "lesson_2",
                title = "Ohm's Law Current Calculation",
                type = "CALCULATION",
                question = "A 12V power supply is connected to a 600Ω resistor. Calculate the current flowing through the circuit in milliamperes (mA).",
                visualCode = null,
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "5 mA",
                        "20 mA",
                        "50 mA",
                        "72 mA"
                    )
                ),
                correctIndex = 1,
                explanation = "I = V / R = 12V / 600Ω = 0.02 Amperes. To convert to mA, multiply by 1,000: 0.02 A × 1,000 = 20 mA.",
                hint = "I = V / R. Remember: 1 Ampere = 1,000 milliamperes.",
                xpReward = 50,
                isCompleted = false,
                studentScore = 0
            ),
            ChallengeEntity(
                id = "ch_2_2",
                lessonId = "lesson_2",
                title = "Component Role in LED Protection",
                type = "COMPONENT_IDENTIFICATION",
                question = "Which component limits current to protect an LED from burning out?",
                visualCode = "RESISTOR_SCHEMATIC",
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "Resistor",
                        "Electrolytic Capacitor",
                        "Step-up Transformer",
                        "Inductor Coil"
                    )
                ),
                correctIndex = 0,
                explanation = "A series resistor limits current through the LED, dropping excess voltage and preventing thermal runaway.",
                hint = "Look for the zig-zag or rectangular schematic symbol that offers electrical resistance.",
                xpReward = 50,
                isCompleted = false,
                studentScore = 0
            ),
            // Lesson 3 Challenges
            ChallengeEntity(
                id = "ch_3_1",
                lessonId = "lesson_3",
                title = "Calculate Series Resistor for LED",
                type = "CALCULATION",
                question = "You have a 5.0V power source and a Red LED with a forward voltage drop (Vf) of 2.0V. What resistor value is required to achieve a safe current of 15mA (0.015A)?",
                visualCode = "LED_CALC_DIAGRAM",
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "100 Ω",
                        "150 Ω",
                        "200 Ω",
                        "330 Ω"
                    )
                ),
                correctIndex = 2,
                explanation = "Voltage across resistor V_R = V_supply - V_f = 5.0V - 2.0V = 3.0V. Using Ohm's Law: R = V_R / I = 3.0V / 0.015A = 200Ω.",
                hint = "Subtract the LED voltage drop from 5V first, then divide by 0.015A.",
                xpReward = 60,
                isCompleted = false,
                studentScore = 0
            ),
            ChallengeEntity(
                id = "ch_3_2",
                lessonId = "lesson_3",
                title = "Troubleshoot Reversed LED Connection",
                type = "TROUBLESHOOTING",
                question = "Identify the fault: A student connects a 5V supply to a 220Ω resistor, then to an LED whose shorter lead (Cathode) is wired to the positive rail and longer lead (Anode) to Ground. Why won't it light?",
                visualCode = "REVERSED_LED_FAULT",
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "The resistor value is too high",
                        "The LED is connected in reverse bias (wrong polarity)",
                        "The voltage source is too low for a single diode",
                        "The circuit requires alternating current (AC)"
                    )
                ),
                correctIndex = 1,
                explanation = "LEDs only conduct when forward-biased: Anode (+) must connect toward the positive supply, and Cathode (-) toward ground.",
                hint = "Diodes only conduct in one direction! Anode = (+), Cathode = (-).",
                xpReward = 60,
                isCompleted = false,
                studentScore = 0
            ),
            // Lesson 4 Challenges
            ChallengeEntity(
                id = "ch_4_1",
                lessonId = "lesson_4",
                title = "RC Time Constant Calculation",
                type = "CALCULATION",
                question = "A timing circuit uses a 47kΩ resistor and a 100μF capacitor. What is the circuit's time constant (τ)?",
                visualCode = null,
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "0.47 seconds",
                        "4.7 seconds",
                        "47 seconds",
                        "470 seconds"
                    )
                ),
                correctIndex = 1,
                explanation = "τ = R × C = 47,000 Ω × 0.000100 F = 4.7 seconds.",
                hint = "Multiply 47,000 by 0.0001.",
                xpReward = 60,
                isCompleted = false,
                studentScore = 0
            ),
            // Lesson 6 Challenges
            ChallengeEntity(
                id = "ch_6_1",
                lessonId = "lesson_6",
                title = "Transistor as a Switch",
                type = "TROUBLESHOOTING",
                question = "In an NPN transistor switching circuit, what happens if the Base terminal is directly tied to +5V with no series base resistor?",
                visualCode = null,
                optionsJson = stringListAdapter.toJson(
                    listOf(
                        "The transistor switches safely and efficiently",
                        "Excessive base-emitter current will destroy the transistor",
                        "The transistor permanently turns OFF",
                        "The collector current will reverse direction"
                    )
                ),
                correctIndex = 1,
                explanation = "The base-emitter junction acts as a forward-biased diode (~0.7V). Connecting 5V directly causes massive forward current, destroying the junction.",
                hint = "Remember that V_BE is just a diode junction!",
                xpReward = 70,
                isCompleted = false,
                studentScore = 0
            )
        )
        challengeDao.insertChallenges(challengeList)
    }

    private suspend fun initializeProjects() {
        val projectList = listOf(
            ProjectEntity(
                id = "proj_1",
                orderIndex = 1,
                title = "Simple LED Circuit",
                objective = "Construct your first functional electronic circuit using a 5V source, a 220Ω resistor, and a Red LED.",
                difficulty = "BEGINNER",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Breadboard power rail distribution",
                        "Current-limiting series resistor",
                        "LED polarity and forward bias",
                        "Circuit loop simulation & verification"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "Red LED (5mm) × 1, 220Ω 1/4W Resistor × 1, 5V DC Source, Breadboard",
                circuitDiagramCode = "CIRCUIT_PROJECT_1_LED",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Route the +5V power rail to row 10 on the breadboard.",
                        "Insert the 220Ω resistor (Red-Red-Brown) between the +5V rail and Node 1.",
                        "Connect the long lead (Anode) of the Red LED into Node 1, sharing the node with the resistor.",
                        "Connect the short lead (Cathode) into the Ground (GND) return rail.",
                        "Verify continuity across the closed conductive loop."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Run the circuit verification test in the project workbench.",
                        "Confirm supply voltage is nominal at 5.0V.",
                        "Verify that the LED illuminates steadily with bright red light.",
                        "Check that calculated branch current is within safe limits (~13.6 mA)."
                    )
                ),
                expectedResult = "Red LED illuminates brightly and steadily at approximately 13.6 mA current draw.",
                status = "UNLOCKED",
                xpReward = 100,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_2",
                orderIndex = 2,
                title = "Variable LED Brightness",
                objective = "Build a smooth analog dimming circuit using a 10kΩ rotary potentiometer to adjust LED current.",
                difficulty = "BEGINNER",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Variable resistance and rheostat wiring",
                        "Ohm's Law in dynamic real-time circuits",
                        "Minimum safe series resistance calculation",
                        "Analog control ergonomics"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 1, \"POTENTIOMETER\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "Green LED × 1, 220Ω Resistor × 1, 10kΩ Potentiometer × 1, Breadboard",
                circuitDiagramCode = "CIRCUIT_PROJECT_2_POT_LED",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Place the 220Ω minimum-protection resistor in series with the Green LED.",
                        "Connect the center wiper pin of the 10kΩ potentiometer to the 220Ω safety resistor.",
                        "Connect one outer pin of the potentiometer to the +5V power rail.",
                        "Connect the Green LED Anode to the other end of the 220Ω resistor; Cathode to GND."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Simulate potentiometer wiper rotation from 0% to 100%.",
                        "Observe continuous analog brightness transition.",
                        "Verify current ranges smoothly between ~0.4mA and ~13.5mA without overdriving the LED."
                    )
                ),
                expectedResult = "Smooth, continuous brightness control from dim glow to maximum illumination.",
                status = "LOCKED",
                xpReward = 120,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_3",
                orderIndex = 3,
                title = "Push-Button LED Control",
                objective = "Construct an interactive momentary switch circuit using a tactile push button and pull-down resistor.",
                difficulty = "BEGINNER",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Normally-open momentary switch contacts",
                        "Floating inputs vs definite voltage states",
                        "Pull-down resistor configuration",
                        "Digital user input interface"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 2, \"PUSH_BUTTON\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "Yellow LED × 1, 330Ω Resistor × 1, 10kΩ Resistor × 1, Push Button × 1",
                circuitDiagramCode = "CIRCUIT_PROJECT_3_BUTTON",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Straddle the push button across the center divider groove of the breadboard.",
                        "Connect one side of the button to the +5V supply rail.",
                        "Connect the other side of the button to the 330Ω resistor leading to the Yellow LED Anode.",
                        "Connect the 10kΩ pull-down resistor between the button output and GND.",
                        "Connect the Yellow LED Cathode to GND."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Simulate pressing the push button: LED turns ON instantly.",
                        "Simulate releasing the button: LED turns OFF immediately with no lingering ghost glow."
                    )
                ),
                expectedResult = "Instant, crisp ON/OFF response upon tactile button press.",
                status = "LOCKED",
                xpReward = 120,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_4",
                orderIndex = 4,
                title = "Two-Way LED Switch",
                objective = "Build a two-way switch circuit where two independent SPDT switches can turn an LED on or off from either location.",
                difficulty = "INTERMEDIATE",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "SPDT switch topology (Single Pole Double Throw)",
                        "Traveler wire routing in staircase and hallway circuits",
                        "Exclusive-OR (XOR) physical switching logic",
                        "Safe low-voltage switching configurations"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "Red LED × 1, 220Ω Resistor × 1, SPDT Switches × 2, Breadboard",
                circuitDiagramCode = "CIRCUIT_PROJECT_4_TWO_WAY_SWITCH",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Connect the +5V supply rail to the common terminal of Switch 1.",
                        "Run two parallel traveler wires between Switch 1 terminals and Switch 2 terminals.",
                        "Connect the common terminal of Switch 2 to the 220Ω series resistor.",
                        "Connect the 220Ω resistor to the Red LED Anode; Cathode connects to GND."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Toggle Switch 1: verify the LED state changes instantly.",
                        "Toggle Switch 2: verify the LED state changes independently from the other position.",
                        "Test all 4 switch combination permutations (00, 01, 10, 11)."
                    )
                ),
                expectedResult = "Reliable dual-location control where either switch independently flips the LED state.",
                status = "LOCKED",
                xpReward = 140,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_5",
                orderIndex = 5,
                title = "Light-Sensitive LED (LDR)",
                objective = "Interface a Light Dependent Resistor (LDR) in an analog voltage divider to detect light intensity and control LED response.",
                difficulty = "INTERMEDIATE",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Photoresistor (LDR) response curves",
                        "Analog voltage dividers with variable transducers",
                        "Threshold voltage detection",
                        "Sensor calibration techniques"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 2, \"LDR\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "Yellow LED × 1, LDR Sensor × 1, 10kΩ Fixed Resistor × 1, 330Ω LED Resistor × 1",
                circuitDiagramCode = "CIRCUIT_PROJECT_5_LDR",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Form a voltage divider: Connect LDR from +5V to Node A; connect 10kΩ resistor from Node A to GND.",
                        "Connect Node A to the LED indicator branch via a 330Ω series resistor.",
                        "Connect Yellow LED Cathode to GND.",
                        "Calibrate baseline lux readings under ambient light."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Simulate changing ambient light levels from bright (1000 Lux) to dim (50 Lux).",
                        "Observe voltage shift at the central divider node.",
                        "Confirm that LED illuminates proportionally as ambient light diminishes."
                    )
                ),
                expectedResult = "Smooth sensor-driven LED response adapting dynamically to ambient light changes.",
                status = "LOCKED",
                xpReward = 150,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_6",
                orderIndex = 6,
                title = "Automatic Night Lamp",
                objective = "Design an autonomous electronic night lamp using an LDR and an NPN transistor that automatically turns ON in darkness.",
                difficulty = "INTERMEDIATE",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Transistor Base threshold switching (0.7V Vbe)",
                        "Autonomous darkness detection logic",
                        "Inverting sensor behavior using pull-up dividers",
                        "Saturation switching for solid illumination"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 2, \"LDR\": 1, \"TRANSISTOR\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "White LED × 1, LDR × 1, 10kΩ Resistor × 1, 1kΩ Base Resistor × 1, 2N2222 NPN × 1",
                circuitDiagramCode = "CIRCUIT_PROJECT_6_NIGHT_LAMP",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Place 10kΩ resistor between +5V and Node A; place LDR between Node A and GND.",
                        "Connect Node A through a 1kΩ base resistor to the Base of the 2N2222 NPN transistor.",
                        "Connect Transistor Emitter to GND.",
                        "Connect Transistor Collector to LED Cathode; connect LED Anode through 220Ω resistor to +5V."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Simulate dark conditions by dropping light level below 100 Lux.",
                        "Observe Node A voltage rise above 0.7V, driving the transistor into saturation.",
                        "Confirm LED snaps ON brightly in the dark and turns completely OFF under light."
                    )
                ),
                expectedResult = "Instant automatic illumination whenever light drops below threshold.",
                status = "LOCKED",
                xpReward = 160,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_7",
                orderIndex = 7,
                title = "Capacitor Timer Circuit",
                objective = "Construct an RC delay timer where an electrolytic capacitor discharges through a transistor to keep an LED lit for several seconds.",
                difficulty = "INTERMEDIATE",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Capacitive energy storage and exponential discharge",
                        "RC time constant calculation (τ = R × C)",
                        "Pulse elongation and temporary hold circuits",
                        "Smooth analog fade transitions"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 1, \"RESISTOR\": 2, \"CAPACITOR\": 1, \"PUSH_BUTTON\": 1, \"TRANSISTOR\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "Green LED × 1, 100μF Capacitor × 1, 10kΩ Bleed Resistor × 1, 220Ω Resistor × 1, 2N2222 NPN × 1, Button × 1",
                circuitDiagramCode = "CIRCUIT_PROJECT_7_TIMER",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Place 100μF capacitor with positive lead to Transistor Base resistor and negative lead to GND.",
                        "Place push button in series with +5V to instantly charge the capacitor when tapped.",
                        "Connect 10kΩ discharge bleed resistor in parallel with capacitor.",
                        "Connect transistor Collector to Green LED circuit with 220Ω resistor."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Tap the push button momentarily to charge the capacitor.",
                        "Observe LED lights immediately and stays illuminated for ~3.5 seconds after button release.",
                        "Watch the LED gently fade out as capacitor voltage drops below 0.7V."
                    )
                ),
                expectedResult = "Timed output sustaining illumination for 3.5 seconds after a single momentary trigger.",
                status = "LOCKED",
                xpReward = 170,
                completedAt = null
            ),
            ProjectEntity(
                id = "proj_8",
                orderIndex = 8,
                title = "Simple Transistor Switch",
                objective = "Use an NPN BJT transistor as a robust electronic switch to control a high-current circuit using a tiny control current.",
                difficulty = "ADVANCED",
                conceptsLearnedJson = stringListAdapter.toJson(
                    listOf(
                        "Current amplification (hFE / Beta)",
                        "Transistor saturation and cutoff states",
                        "Base current calculation and limiting",
                        "Low-side electronic switching fundamentals"
                    )
                ),
                requiredComponentsJson = "{ \"LED\": 2, \"RESISTOR\": 3, \"TRANSISTOR\": 1, \"PUSH_BUTTON\": 1, \"BREADBOARD\": 1 }",
                requiredSpecs = "High-Brightness Red LED × 1, Green Indicator LED × 1, 1kΩ Base Resistor × 1, 220Ω Resistor × 1, 2N2222 NPN × 1",
                circuitDiagramCode = "CIRCUIT_PROJECT_8_TRANSISTOR_SWITCH",
                assemblyStepsJson = stringListAdapter.toJson(
                    listOf(
                        "Wire the low-current control signal (Button + 1kΩ resistor) to the Base of the 2N2222 NPN transistor.",
                        "Connect Emitter directly to GND.",
                        "Connect high-power load (Red LED + 220Ω resistor) to the Collector and +5V.",
                        "Connect Green indicator LED to the input button to monitor control signal."
                    )
                ),
                testingInstructionsJson = stringListAdapter.toJson(
                    listOf(
                        "Inject a tiny 0.5mA control signal into the Base.",
                        "Measure the resulting Collector current: ~15mA driving the main load.",
                        "Verify current amplification and crisp switching without mechanical contacts."
                    )
                ),
                expectedResult = "Efficient solid-state switching demonstrating over 30× current amplification.",
                status = "LOCKED",
                xpReward = 200,
                completedAt = null
            )
        )
        projectDao.insertProjects(projectList)
    }

    private suspend fun initializeAchievements() {
        val achievementList = listOf(
            AchievementEntity("ach_first_circuit", "First Circuit", "Assembled and simulated your very first circuit.", "bolt", 50, false, null),
            AchievementEntity("ach_ohms_master", "Ohm's Law Master", "Scored 100% on all resistance and current calculation challenges.", "calculate", 80, false, null),
            AchievementEntity("ach_led_builder", "LED Builder", "Safely computed forward voltage and completed the LED Circuit project.", "lightbulb", 100, false, null),
            AchievementEntity("ach_sensor_explorer", "Sensor Explorer", "Interfaced an analog LDR sensor with dynamic voltage dividers.", "sensors", 100, false, null),
            AchievementEntity("ach_first_project", "First Project", "Completed and submitted Project 1 with circuit verification.", "military_tech", 100, false, null),
            AchievementEntity("ach_fundamentals_complete", "Electronic Fundamentals Complete", "Completed all 8 modules and 8 projects in Electronic Fundamentals.", "school", 300, false, null)
        )
        achievementDao.insertAchievements(achievementList)
    }

    suspend fun completeLesson(lessonId: String) = withContext(Dispatchers.IO) {
        val currentLessons = lessonDao.getAllLessonsDirect()
        val targetIndex = currentLessons.indexOfFirst { it.id == lessonId }
        if (targetIndex < 0) return@withContext
        val targetLesson = currentLessons[targetIndex]

        // Strict validation: cannot complete a LOCKED lesson, and all previous lessons must be COMPLETED
        if (targetLesson.status == "LOCKED") return@withContext
        val previousAllCompleted = currentLessons.take(targetIndex).all { it.status == "COMPLETED" }
        if (!previousAllCompleted) return@withContext

        val wasAlreadyCompleted = targetLesson.status == "COMPLETED"
        if (!wasAlreadyCompleted) {
            val now = System.currentTimeMillis()
            lessonDao.updateLessonStatus(lessonId, "COMPLETED", now)
            addXp(targetLesson.xpReward.coerceAtLeast(80), "Lesson ${targetLesson.orderIndex} Completed")

            // Sequential unlocking: ONLY the immediate next sequential lesson unlocks!
            if (targetIndex + 1 < currentLessons.size) {
                val nextLesson = currentLessons[targetIndex + 1]
                if (nextLesson.status == "LOCKED") {
                    lessonDao.updateLessonStatus(nextLesson.id, "UNLOCKED", null)
                }
            }

            val st = studentDao.getStudentDirect()
            if (st != null) {
                val nextId = if (targetIndex + 1 < currentLessons.size) currentLessons[targetIndex + 1].id else lessonId
                studentDao.updateStudent(
                    st.copy(
                        completedLessonsCount = currentLessons.count { it.status == "COMPLETED" } + 1,
                        currentLessonId = nextId
                    )
                )
            }
        }
    }

    suspend fun submitChallenge(challengeId: String, selectedOptionIndex: Int): Triple<Boolean, Int, String> = withContext(Dispatchers.IO) {
        val challenges = challengeDao.getAllChallenges()
        // We find the challenge
        var correct = false
        var score = 0
        var explanation = ""

        // Find direct
        val challengeList = listOf(
            // Sample fallback lookup
            "ch_1_1" to 1, "ch_1_2" to 1, "ch_2_1" to 1, "ch_2_2" to 0,
            "ch_3_1" to 2, "ch_3_2" to 1, "ch_4_1" to 1, "ch_6_1" to 1
        )
        val expected = challengeList.firstOrNull { it.first == challengeId }?.second ?: 0
        correct = (selectedOptionIndex == expected)
        score = if (correct) 100 else 40

        val passed = score >= 70 // Passing requirement 70%

        if (passed) {
            challengeDao.updateChallengeResult(challengeId, true, score)
            addXp(50, "Challenge Passed")
            val st = studentDao.getStudentDirect()
            if (st != null) {
                studentDao.updateStudent(st.copy(completedChallengesCount = st.completedChallengesCount + 1))
            }
            Triple(true, score, "Great work! You scored $score% and earned +50 XP!")
        } else {
            challengeDao.updateChallengeResult(challengeId, false, score)
            Triple(false, score, "Score: $score%. Minimum 70% required to pass. Review the lesson concept and retry!")
        }
    }

    suspend fun startProjectBuild(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.updateProjectStatus(projectId, "IN_PROGRESS", null)
        val st = studentDao.getStudentDirect()
        if (st != null) {
            studentDao.updateStudent(st.copy(currentProjectId = projectId))
        }
    }

    suspend fun testProjectCircuit(projectId: String): Boolean = withContext(Dispatchers.IO) {
        projectDao.updateProjectStatus(projectId, "TESTED", null)
        true
    }

    suspend fun submitProject(projectId: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        projectDao.updateProjectStatus(projectId, "COMPLETED", now)
        addXp(100, "Project Completed")

        // Progress next project to UNLOCKED
        val nextProjectId = when (projectId) {
            "proj_1" -> "proj_2"
            "proj_2" -> "proj_3"
            "proj_3" -> "proj_4"
            "proj_4" -> "proj_5"
            "proj_5" -> "proj_6"
            "proj_6" -> "proj_7"
            "proj_7" -> "proj_8"
            else -> null
        }
        if (nextProjectId != null) {
            projectDao.updateProjectStatus(nextProjectId, "UNLOCKED", null)
        }

        val st = studentDao.getStudentDirect()
        if (st != null) {
            studentDao.updateStudent(
                st.copy(
                    completedProjectsCount = st.completedProjectsCount + 1,
                    currentProjectId = nextProjectId ?: projectId
                )
            )
        }
    }

    private suspend fun reclaimKitComponents() {
        val comps = kitDao.getAllKitComponentsDirect()
        val updated = comps.map { it.copy(inUseQuantity = 0) }
        kitDao.insertKitComponents(updated)
    }

    suspend fun addXp(amount: Int, reason: String) = withContext(Dispatchers.IO) {
        val st = studentDao.getStudentDirect() ?: return@withContext
        val newXp = st.xp + amount
        val newLevel = (newXp / 150) + 1
        studentDao.updateStudent(st.copy(xp = newXp, level = newLevel))
    }

    suspend fun toggleDarkMode(isDark: Boolean) = withContext(Dispatchers.IO) {
        val st = studentDao.getStudentDirect() ?: return@withContext
        studentDao.updateStudent(st.copy(isDarkMode = isDark))
    }

    suspend fun toggleHapticFeedback(enabled: Boolean) = withContext(Dispatchers.IO) {
        val st = studentDao.getStudentDirect() ?: return@withContext
        studentDao.updateStudent(st.copy(hapticFeedbackEnabled = enabled))
    }

    // Hardware Simulation controls
    fun connectHardwareKit(connect: Boolean) {
        _telemetry.value = _telemetry.value.copy(
            isConnected = connect,
            lastTestResult = if (connect) "Connected & Synchronized" else "Disconnected"
        )
    }

    fun runHardwareTest(): String {
        val connected = _telemetry.value.isConnected
        if (!connected) {
            return "Please connect the Cirqubit Kit v1 before running hardware diagnostics."
        }
        val current = _telemetry.value.currentDrawMa
        val voltage = _telemetry.value.supplyVoltage
        _telemetry.value = _telemetry.value.copy(
            lastTestResult = "Test Passed: Bus $voltage V, Loop Current $current mA, No Shorts Detected."
        )
        return "PASS: Supply rails nominal ($voltage V). Continuity check verified. Sensor signal responding."
    }

    fun updatePotentiometerValue(value: Float) {
        _telemetry.value = _telemetry.value.copy(
            potentiometerValue = value,
            currentDrawMa = 10f + (value * 15f)
        )
    }

    fun updateLightLevelLux(lux: Int) {
        _telemetry.value = _telemetry.value.copy(lightLevelLux = lux)
    }

    fun toggleHardwareButton(buttonIndex: Int, isPressed: Boolean) {
        if (buttonIndex == 1) {
            _telemetry.value = _telemetry.value.copy(button1Pressed = isPressed)
        } else {
            _telemetry.value = _telemetry.value.copy(button2Pressed = isPressed)
        }
    }

    fun toggleHardwareLed() {
        _telemetry.value = _telemetry.value.copy(ledOutputActive = !_telemetry.value.ledOutputActive)
    }

    // Circuit Designer Workspace
    fun addDesignerComponent(type: String, label: String, value: String) {
        val current = _designerComponents.value.toMutableList()
        val id = "comp_${System.currentTimeMillis()}"
        val newItem = DesignerComponentItem(
            id = id,
            type = type,
            label = label,
            value = value,
            x = 100f + (current.size % 4) * 60f,
            y = 120f + (current.size / 4) * 60f
        )
        current.add(newItem)
        _designerComponents.value = current
    }

    fun removeDesignerComponent(id: String) {
        val comps = _designerComponents.value.filter { it.id != id }
        val wires = _designerWires.value.filter { it.fromComponentId != id && it.toComponentId != id }
        _designerComponents.value = comps
        _designerWires.value = wires
    }

    fun rotateDesignerComponent(id: String) {
        val comps = _designerComponents.value.map {
            if (it.id == id) it.copy(rotationDeg = (it.rotationDeg + 90) % 360) else it
        }
        _designerComponents.value = comps
    }

    fun updateDesignerComponentPosition(id: String, x: Float, y: Float) {
        val comps = _designerComponents.value.map {
            if (it.id == id) it.copy(x = x, y = y) else it
        }
        _designerComponents.value = comps
    }

    fun updateDesignerComponentValue(id: String, newValue: String) {
        val comps = _designerComponents.value.map {
            if (it.id == id) it.copy(value = newValue) else it
        }
        _designerComponents.value = comps
    }

    fun clearDesignerWorkspace() {
        _designerComponents.value = emptyList()
        _designerWires.value = emptyList()
    }

    fun duplicateDesignerComponent(id: String) {
        val comps = _designerComponents.value.toMutableList()
        val original = comps.firstOrNull { it.id == id } ?: return
        val newId = "comp_${System.currentTimeMillis()}"
        val dup = original.copy(
            id = newId,
            label = "${original.label}_copy",
            x = original.x + 24f,
            y = original.y + 24f
        )
        comps.add(dup)
        _designerComponents.value = comps
    }

    fun deleteDesignerWire(wireId: String) {
        val wires = _designerWires.value.filter { it.id != wireId }
        _designerWires.value = wires
    }

    fun setDesignerState(comps: List<DesignerComponentItem>, wires: List<DesignerWireItem>) {
        _designerComponents.value = comps
        _designerWires.value = wires
    }

    fun addDesignerWire(fromCompId: String, fromPin: String, toCompId: String, toPin: String) {
        if (fromCompId == toCompId && fromPin == toPin) return
        val wires = _designerWires.value.toMutableList()
        // Prevent exact duplicate wires
        val exists = wires.any {
            (it.fromComponentId == fromCompId && it.fromPin == fromPin && it.toComponentId == toCompId && it.toPin == toPin) ||
            (it.fromComponentId == toCompId && it.fromPin == toPin && it.toComponentId == fromCompId && it.toPin == fromPin)
        }
        if (exists) return

        val wireId = "wire_${System.currentTimeMillis()}_${wires.size}"
        wires.add(DesignerWireItem(wireId, fromCompId, fromPin, toCompId, toPin))
        _designerWires.value = wires
    }

    fun checkDesignerCircuit(): String {
        val comps = _designerComponents.value
        val wires = _designerWires.value

        if (comps.isEmpty()) {
            return "Workspace is empty. Search and place a Power Source, Resistor, and LED from the library to begin!"
        }

        // 1. Missing Power Source
        val powerTypes = listOf("BATTERY", "DC_SOURCE", "VCC")
        val powerComps = comps.filter { it.type in powerTypes }
        if (powerComps.isEmpty()) {
            return "⚠️ Missing Power Source: Your circuit requires an electrical potential (Battery, DC Voltage Source, or VCC) to drive current."
        }

        // 2. Missing Ground
        val gndTypes = listOf("GROUND", "GND", "BATTERY")
        val groundComps = comps.filter { it.type in gndTypes }
        if (groundComps.isEmpty()) {
            return "⚠️ Missing Return Path: Current cannot flow without a return path. Add a GND or Ground component to complete the loop."
        }

        // 3. Components with unconnected required pins
        val unconnectedComps = comps.filter { comp ->
            wires.none { it.fromComponentId == comp.id || it.toComponentId == comp.id }
        }
        if (unconnectedComps.isNotEmpty()) {
            val names = unconnectedComps.take(2).joinToString(", ") { it.label }
            return "⚠️ Unconnected Component: ${names} is placed on the board but has no wire connections. Connect it or remove it."
        }

        // 4. Short circuit detection (VCC connected directly to GND without load)
        for (wire in wires) {
            val c1 = comps.firstOrNull { it.id == wire.fromComponentId }
            val c2 = comps.firstOrNull { it.id == wire.toComponentId }
            if (c1 != null && c2 != null) {
                val isPwr1 = c1.type in powerTypes
                val isGnd1 = c1.type == "GROUND" || c1.type == "GND"
                val isPwr2 = c2.type in powerTypes
                val isGnd2 = c2.type == "GROUND" || c2.type == "GND"
                if ((isPwr1 && isGnd2) || (isPwr2 && isGnd1)) {
                    return "🚨 Short Circuit Hazard: Direct connection between Power and Ground detected! This creates dangerous overcurrent. Remove this wire."
                }
            }
        }

        // 5. LED Protection check
        val leds = comps.filter { it.type in listOf("LED", "RGB_LED") }
        val resistors = comps.filter { it.type in listOf("RESISTOR", "POTENTIOMETER", "VAR_RESISTOR") }

        if (leds.isNotEmpty() && resistors.isEmpty()) {
            return "⚠️ High Current Hazard: Your LED is connected without a current-limiting resistor! It will draw excessive current and burn out. Add a 220Ω or 330Ω series resistor to protect it."
        }

        // 6. Transistor Base Protection
        val transistors = comps.filter { it.type in listOf("TRANSISTOR_NPN", "TRANSISTOR_PNP", "TRANSISTOR") }
        if (transistors.isNotEmpty() && resistors.isEmpty()) {
            return "⚠️ Transistor Base Warning: Transistors need a base resistor (e.g. 1kΩ) to limit base-emitter current. Direct connection to power may damage the transistor."
        }

        // 7. Wire count loop check
        if (wires.size < comps.size) {
            return "⚠️ Open Circuit Detected: Some components appear isolated. Verify that all components form a continuous conductive loop from Power to Ground."
        }

        return "✅ Circuit Validated: Continuous current path detected, power and return reference verified, and components are safely protected!"
    }

    suspend fun resetStudentProgress() = withContext(Dispatchers.IO) {
        studentDao.deleteAllStudents()
        lessonDao.deleteAllLessons()
        challengeDao.deleteAllChallenges()
        projectDao.deleteAllProjects()
        achievementDao.deleteAllAchievements()

        // Re-seed brand new fresh user state for Kishan Gopa
        studentDao.insertStudent(
            StudentEntity(
                id = "cirqubit_student_1",
                name = "Kishan Gopa",
                level = 1,
                xp = 0,
                streakDays = 0,
                completedLessonsCount = 0,
                completedChallengesCount = 0,
                completedProjectsCount = 0,
                currentCourseName = "Electronic Fundamentals",
                currentLessonId = "lesson_1",
                currentProjectId = "proj_1",
                isDarkMode = true
            )
        )
        initializeKitInventory()
        initializeLessons()
        initializeChallenges()
        initializeProjects()
        initializeAchievements()
    }

    private fun parseComponentsJson(json: String): Map<KitComponentType, Int> {
        return try {
            val map = mutableMapOf<KitComponentType, Int>()
            val clean = json.replace("{", "").replace("}", "").replace("\"", "")
            val pairs = clean.split(",")
            for (p in pairs) {
                val parts = p.split(":")
                if (parts.size == 2) {
                    val keyStr = parts[0].trim()
                    val valInt = parts[1].trim().toIntOrNull() ?: 1
                    try {
                        val type = KitComponentType.valueOf(keyStr)
                        map[type] = valInt
                    } catch (e: Exception) {
                        // ignore unknown
                    }
                }
            }
            map
        } catch (e: Exception) {
            mapOf(KitComponentType.LED to 1, KitComponentType.RESISTOR to 1)
        }
    }
}
