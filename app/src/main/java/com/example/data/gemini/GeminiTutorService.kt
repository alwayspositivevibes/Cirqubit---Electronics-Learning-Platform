package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String = "user",
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String = "HIGH"
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float = 0.7f,
    val thinkingConfig: ThinkingConfig? = ThinkingConfig("HIGH")
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = GenerationConfig()
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

class GeminiTutorService {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun askTutor(
        studentQuery: String,
        currentCourse: String,
        currentLesson: String,
        currentProject: String,
        mode: String // "hint", "explanation", "example", "deep_dive"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getSmartFallbackTutorResponse(studentQuery, currentLesson, currentProject, mode)
        }

        try {
            val systemPrompt = """
                You are the Cirqubit AI Electronics Tutor — a friendly, knowledgeable, and encouraging electronics engineering mentor.
                You help students understand all aspects of electronics: from fundamentals (voltage, current, Ohm's Law, Kirchhoff's Laws) to analog components (resistors, capacitors, diodes, LEDs, transistors, op-amps) and practical troubleshooting.
                Context: Student is enrolled in '$currentCourse'. (Active reference: $currentLesson / $currentProject).
                Requested style/mode: $mode.
                
                Guidelines:
                - Answer general electronics questions enthusiastically (e.g. "What is voltage?", "Explain Ohm's Law", "Quiz me on capacitors", "How does a transistor switch?"). Never refuse a question just because it is outside the current lesson.
                - Adapt to the student's level: use simple intuitive physical analogies (like water pressure for voltage, water flow for current, a constriction for resistance) for beginners, and clear mathematical formulas for technical queries.
                - If the student is working on an active challenge quiz, guide them with progressive hints rather than immediately giving away the direct multiple-choice letter.
                - Use clear formatting with bullet points and bold headers.
            """.trimIndent()

            val fullUserMessage = "$systemPrompt\n\nStudent question: $studentQuery"

            val requestData = GenerateContentRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = fullUserMessage))
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
                )
            )

            val jsonAdapter = moshi.adapter(GenerateContentRequest::class.java)
            val requestBodyJson = jsonAdapter.toJson(requestData)

            val requestBody = requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiTutor", "API response error: ${response.code}, falling back to built-in tutor")
                return@withContext getSmartFallbackTutorResponse(studentQuery, currentLesson, currentProject, mode)
            }

            val responseJson = response.body?.string() ?: ""
            val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)
            val parsed = responseAdapter.fromJson(responseJson)

            val candidateText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!candidateText.isNullOrBlank()) {
                candidateText
            } else {
                getSmartFallbackTutorResponse(studentQuery, currentLesson, currentProject, mode)
            }
        } catch (e: Exception) {
            Log.e("GeminiTutor", "Error calling Gemini API: ${e.message}", e)
            getSmartFallbackTutorResponse(studentQuery, currentLesson, currentProject, mode)
        }
    }

    private fun getSmartFallbackTutorResponse(
        query: String,
        lesson: String,
        project: String,
        mode: String
    ): String {
        val qLower = query.lowercase()

        return when {
            qLower.contains("led") && (qLower.contains("not work") || qLower.contains("burn") || qLower.contains("off")) -> {
                when (mode) {
                    "hint" -> "💡 **Tutor Hint:** Remember that LEDs are polarized diodes! They only conduct current in one direction. Check the longer lead (Anode) and verify you placed a current-limiting resistor (e.g., 220Ω) in series."
                    "example" -> "🔬 **Example:** For a standard 5V supply and Red LED (V_f ≈ 2.0V, I = 15mA):\n• Voltage across resistor: V_R = 5V - 2.0V = 3.0V\n• Resistor: R = 3.0V / 0.015A = 200Ω (use a 220Ω resistor from your kit)."
                    "deep_dive" -> "🔍 **Deep Dive:** An LED has an exponential I-V characteristic beyond its forward knee voltage (~1.8V to 3.2V depending on color). Without a series resistor, even a slight increase in voltage leads to massive current draw, immediately burning the diode junction. Always verify polarity: Anode (+) → Resistor → Vcc, Cathode (-) → GND."
                    else -> "📘 **Explanation:** In an LED circuit, three things must be true:\n1. **Polarity:** Anode (+) must point toward positive supply; Cathode (-) toward ground.\n2. **Current Limiting:** A resistor in series limits current to safe levels (10-20mA).\n3. **Closed Loop:** Ensure your breadboard rails have a continuous path back to GND."
                }
            }
            qLower.contains("ohm") || qLower.contains("v=ir") || qLower.contains("resistance") || qLower.contains("calculate") -> {
                when (mode) {
                    "hint" -> "💡 **Tutor Hint:** Ohm's law links Voltage (V), Current (I), and Resistance (R). Write down the two values you know: V = I × R, I = V / R, or R = V / I. Watch out for milliamps: 20mA = 0.020A!"
                    "example" -> "🔬 **Worked Example:** If you apply 9V across a 470Ω resistor:\n• I = V / R = 9V / 470Ω ≈ 0.0191 A = 19.1 mA.\n• Power dissipated: P = V × I = 9V × 0.0191A ≈ 0.17W (safe for a standard 1/4W resistor)."
                    "deep_dive" -> "🔍 **Deep Dive:** Resistance is the ratio of electric field to current density (Ohmic materials). In practical circuits, resistors also experience thermal drift. Always verify that power rating P = I²R doesn't exceed your component rating (usually 0.25W in the Cirqubit Kit)."
                    else -> "📘 **Explanation:** Ohm's Law states that current is directly proportional to voltage and inversely proportional to resistance. If resistance increases and voltage remains constant, the current must decrease!"
                }
            }
            qLower.contains("transistor") || qLower.contains("switch") || qLower.contains("npn") -> {
                when (mode) {
                    "hint" -> "💡 **Tutor Hint:** Think of an NPN transistor (like the 2N2222 in your kit) as an electronic valve. A tiny current entering the Base pin turns ON a large current flowing from Collector to Emitter."
                    "example" -> "🔬 **Example:** A microcontroller pin delivers 5V through a 1kΩ base resistor to the Base. With V_BE ≈ 0.7V, I_B = (5 - 0.7) / 1000 = 4.3mA. With gain β = 100, the transistor can easily switch a 100mA buzzer or relay."
                    "deep_dive" -> "🔍 **Deep Dive:** When used as a switch, we drive the BJT into saturation: V_CE(sat) drops to ~0.2V. We provide base overdrive (I_B > I_C / 10) to guarantee hard saturation, minimizing power dissipation in the transistor."
                    else -> "📘 **Explanation:** Transistors allow a low-power control signal (like a sensor or MCU pin) to switch higher-power devices (like motors, high-power LEDs, or buzzers) without overloading sensitive electronics."
                }
            }
            qLower.contains("capacitor") || qLower.contains("charge") || qLower.contains("filter") -> {
                when (mode) {
                    "hint" -> "💡 **Tutor Hint:** Capacitors store energy in an electric field. In DC circuits, they resist instantaneous changes in voltage. Watch out for polarity on electrolytic capacitors—the stripe marks the negative lead!"
                    else -> "📘 **Explanation:** Capacitors charge up to the supply voltage through a resistor following an exponential curve: V(t) = V_max × (1 - e^(-t / RC)). The product R × C is the time constant (tau). After 5 tau, it is practically 99% charged."
                }
            }
            qLower.contains("voltage") && !qLower.contains("divider") -> {
                "⚡ **Voltage Explained:**\nVoltage (measured in Volts, V) is electrical potential difference — the electromotive force that pushes electric charge through a circuit.\n\n🌊 **Beginner Analogy:** Think of a water tank. Voltage is like the water pressure at the bottom of the tank. The higher the tank (greater voltage), the more pressure pushes water through the pipes (current)!"
            }
            qLower.contains("current") && (qLower.contains("flow") || qLower.contains("what is")) -> {
                "🌊 **Electric Current Explained:**\nCurrent (measured in Amperes or mA, I) is the physical rate at which electric charge flows past a point in a circuit.\n\n• 1 Ampere = 1 Coulomb of charge (6.24 × 10¹⁸ electrons) passing per second.\n• Current only flows when there is a complete, unbroken conductive loop between high potential (+) and low potential (-/GND)."
            }
            qLower.contains("kirchhoff") || qLower.contains("kvl") || qLower.contains("kcl") -> {
                "📐 **Kirchhoff's Laws:**\n1. **KCL (Current Law):** The total current entering a junction must equal the total current leaving that junction (conservation of charge: Σ I_in = Σ I_out).\n2. **KVL (Voltage Law):** The directed sum of electrical potential differences around any closed circuit loop is zero (conservation of energy: Σ V = 0)."
            }
            qLower.contains("difference") && qLower.contains("resistor") && qLower.contains("capacitor") -> {
                "⚖️ **Resistor vs Capacitor:**\n\n• **Resistor (Ω):** Dissipates energy as heat. It resists instantaneous current flow according to Ohm's Law (V = IR). It does NOT store energy.\n• **Capacitor (F):** Stores energy electrostatically in an electric field between two plates. It resists sudden changes in voltage and blocks steady-state DC while passing AC ripples."
            }
            qLower.contains("quiz") -> {
                "🧠 **Quick Electronics Quiz:**\n\n**Question:** You have a 9V battery and a 300Ω resistor. According to Ohm's Law (I = V / R), what current flows through the circuit?\n\n*A) 3 mA*\n*B) 30 mA*\n*C) 300 mA*\n\n*(Think about: 9 / 300 = 0.03 Amperes. How many milliamperes is that? Reply with your answer!)*"
            }
            qLower.contains("beginner") || qLower.contains("simple") -> {
                "🌱 **Electronics in Simple Terms:**\nThink of every circuit as a water system:\n• **Voltage (V):** The water pump pressure pushing.\n• **Current (I):** The amount of water moving through the pipe.\n• **Resistance (R):** A narrow valve or constriction slowing the flow.\n• **Switch:** A tap that can turn on or off.\n• **Capacitor:** A flexible rubber membrane balloon storing water under pressure."
            }
            else -> {
                when (mode) {
                    "hint" -> "💡 **Tutor Hint:** For '$lesson', break the problem down into components, connections, and electrical values. Check power, ground, and whether each component has a complete return path."
                    "example" -> "🔬 **Example Application:** In practical circuit design, start by calculating component values from specs, breadboard the prototype, probe with a voltmeter, and verify behavior before finalizing."
                    "deep_dive" -> "🔍 **Deep Dive Analysis:** Modern electronics balances digital control with analog physical interfaces. Understanding impedance matching, noise margins, and decoupling capacitors ensures reliable circuits in real-world environments."
                    else -> "📘 **Instructor Guidance:** You're doing great in **$lesson**! Always trace current flow from the positive supply, through each element in the loop, back to GND. If you're stuck on a challenge or project, try asking about component orientation, resistor values, or breadboard connections!"
                }
            }
        }
    }
}
