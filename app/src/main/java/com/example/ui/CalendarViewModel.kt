package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteFestival
import com.example.data.local.OdiaCalendarRepository
import com.example.data.local.SavedReading
import com.example.data.local.UserPreference
import com.example.data.models.OdiaDayInfo
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.utils.NotificationHelper
import com.example.utils.OdiaCalendarCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class SimulatedNotification(
    val id: String,
    val title: String,
    val text: String,
    val timeStr: String,
    val isRead: Boolean = false
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OdiaCalendarRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OdiaCalendarRepository(database.odiaCalendarDao())
    }

    // --- State Variables ---

    // Selected Calendar Date
    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.of(2026, 7, 1)) // Default to July 1, 2026
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Calculated Odia Details for the selected date
    val selectedDayInfo: StateFlow<OdiaDayInfo> = _selectedDate
        .map { date -> OdiaCalendarCalculator.getOdiaDayInfo(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OdiaCalendarCalculator.getOdiaDayInfo(_selectedDate.value)
        )

    // User Preferences (Room DB)
    val userPreferences: StateFlow<UserPreference> = repository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreference()
        )

    // Favorite Festivals (Room DB)
    val favoriteFestivals: StateFlow<List<FavoriteFestival>> = repository.favoriteFestivals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Daily Rashi Phala States
    private val _rashiPhalaLoading = MutableStateFlow(false)
    val rashiPhalaLoading: StateFlow<Boolean> = _rashiPhalaLoading.asStateFlow()

    private val _rashiPhalaError = MutableStateFlow<String?>(null)
    val rashiPhalaError: StateFlow<String?> = _rashiPhalaError.asStateFlow()

    // Map of Rashi name to Pair of (Odia Reading, English Reading)
    private val _dailyHoroscope = MutableStateFlow<Pair<String, String>?>(null)
    val dailyHoroscope: StateFlow<Pair<String, String>?> = _dailyHoroscope.asStateFlow()

    // AI Panjika Consult Chat State
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "ଶୁଭେଚ୍ଛା! ମୁଁ ଆପଣଙ୍କ ଓଡ଼ିଆ ପାଞ୍ଜି ଗବେଷକ ଓ ବୈଦିକ ଜ୍ୟୋତିଷ ସହାୟକ। ଆଜି ଆପଣ କେଉଁ ପୂଜା, ବ୍ରତ କିମ୍ବା ତିଥି ବିଷୟରେ ଜାଣିବାକୁ ଚାହାଁନ୍ତି? \n\nWelcome! I am your Odia Panjika guide and Vedic Astrology assistant. Ask me anything about Odia festivals, tithi significance, or rituals." to false
        )
    )
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    // Simulated In-App Notification Center list
    private val _simulatedNotifications = MutableStateFlow<List<SimulatedNotification>>(emptyList())
    val simulatedNotifications: StateFlow<List<SimulatedNotification>> = _simulatedNotifications.asStateFlow()

    init {
        // Build initial mock notifications
        loadMockNotifications()
    }

    // --- Actions & Methods ---

    fun changeSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleFavoriteFestival(date: LocalDate, festivalName: String) {
        viewModelScope.launch {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            repository.toggleFavorite(dateStr, festivalName)
        }
    }

    fun selectZodiac(zodiac: String) {
        viewModelScope.launch {
            repository.updateZodiac(zodiac)
            // Trigger auto-fetching horoscope for the newly selected zodiac
            fetchHoroscopeForZodiac(zodiac, _selectedDate.value)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateLanguage(lang)
        }
    }

    fun updateNotificationsConfig(enabled: Boolean, time: String) {
        viewModelScope.launch {
            repository.updateNotificationSettings(enabled, time)
        }
    }

    // Spawns/Schedules a real System Notification representing the daily Rashi Phala!
    fun triggerInstantSystemNotification() {
        val pref = userPreferences.value
        val rashi = if (pref.zodiac.isNotEmpty()) pref.zodiac else "Mesha"
        val dayInfo = selectedDayInfo.value
        val reading = dailyHoroscope.value ?: getFallbackHoroscope(rashi, _selectedDate.value)

        val rashiOdiaMap = mapOf(
            "Mesha" to "ମେଷ", "Vrisha" to "ବୃଷ", "Mithuna" to "ମିଥୁନ",
            "Karka" to "କର୍କଟ", "Simha" to "ସିଂହ", "Kanya" to "କନ୍ୟା",
            "Tula" to "ତୁଳା", "Vrischika" to "ବିଛା", "Dhanu" to "ଧନୁ",
            "Makara" to "ମକର", "Kumbha" to "କୁମ୍ଭ", "Meena" to "ମୀନ"
        )
        val rashiOdia = rashiOdiaMap[rashi] ?: "ମେଷ"

        // Trigger Android status bar notification
        NotificationHelper.showRashiPhalaNotification(
            context = getApplication(),
            rashiEng = rashi,
            rashiOdia = rashiOdia,
            tithiOdia = dayInfo.tithiOdia,
            horoscopeText = if (pref.language == "Odia") reading.first else reading.second
        )

        // Add to our simulated in-app feed
        val newNotif = SimulatedNotification(
            id = System.currentTimeMillis().toString(),
            title = "Daily Rashi Phala Alert: $rashiOdia",
            text = if (pref.language == "Odia") reading.first else reading.second,
            timeStr = "Just Now"
        )
        _simulatedNotifications.value = listOf(newNotif) + _simulatedNotifications.value
    }

    private fun loadMockNotifications() {
        _simulatedNotifications.value = listOf(
            SimulatedNotification(
                id = "1",
                title = "Upcoming Festival Reminder: Ratha Yatra",
                text = "ଶ୍ରୀଗୁଣ୍ଡିଚା ରଥଯାତ୍ରା is coming on July 16, 2026! Keep your prayer plate and customized offerings ready.",
                timeStr = "1 hour ago",
                isRead = true
            ),
            SimulatedNotification(
                id = "2",
                title = "Auspicious Timing Today",
                text = "Today's Amruta Bela / Auspicious timing is highly favorable in the morning. Favorable for new initiatives.",
                timeStr = "Today, 08:30 AM",
                isRead = true
            )
        )
    }

    fun markNotificationAsRead(id: String) {
        _simulatedNotifications.value = _simulatedNotifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun clearAllNotifications() {
        _simulatedNotifications.value = emptyList()
    }

    // --- Gemini AI Horoscope Generation ---

    fun loadSelectedZodiacHoroscope() {
        val zodiac = userPreferences.value.zodiac
        if (zodiac.isNotEmpty()) {
            fetchHoroscopeForZodiac(zodiac, _selectedDate.value)
        } else {
            _dailyHoroscope.value = null
        }
    }

    fun fetchHoroscopeForZodiac(zodiac: String, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _rashiPhalaLoading.value = true
            _rashiPhalaError.value = null

            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 1. Try checking cache first
            val cached = repository.getSavedReading(dateStr, zodiac)
            if (cached != null) {
                _dailyHoroscope.value = Pair(cached.readingOdia, cached.readingEnglish)
                _rashiPhalaLoading.value = false
                return@launch
            }

            // 2. Query Gemini API
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // FALLBACK: Generate exceptionally beautiful local authentic forecast
                val fallback = getFallbackHoroscope(zodiac, date)
                repository.saveReading(
                    SavedReading(
                        dateStr = dateStr,
                        rashi = zodiac,
                        readingOdia = fallback.first,
                        readingEnglish = fallback.second
                    )
                )
                _dailyHoroscope.value = fallback
                _rashiPhalaLoading.value = false
                return@launch
            }

            val dayInfo = OdiaCalendarCalculator.getOdiaDayInfo(date)
            val prompt = """
                Give a traditional, spiritual, and accurate daily astrology reading (Rashi Phala) for the zodiac sign '$zodiac' on the date $dateStr.
                Cultural details:
                - Odia Solar Month: ${dayInfo.odiaMonthEng} (${dayInfo.odiaMonthOdia})
                - Odia Lunar Month: ${dayInfo.lunarMonthEng} (${dayInfo.lunarMonthOdia})
                - Tithi: ${dayInfo.tithiEng} (${dayInfo.tithiOdia})
                - Paksha: ${dayInfo.pakshaEng} (${dayInfo.pakshaOdia})
                - Active Nakshatra: ${dayInfo.nakshatraEng} (${dayInfo.nakshatraOdia})
                
                Format the response clearly into TWO parts:
                PART 1: ODIA SCRIPT (Detailed daily forecast covering career, family, health, and a traditional ritual or puja recommendation)
                PART 2: ENGLISH (An equally rich English translation)
                
                Split the two parts with a unique delimiter string: '===DIVIDER==='.
                Do not include raw Markdown headers that overlap the parts. Keep the language deeply cultural, respectful, and authentic to Odia Vedic culture.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(temperature = 0.7f),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are a highly respected and wise Odia Vedic Astrologer, expert in the Odia Kohinoor and Bhagyajyoti Panjika. You provide authentic, spiritually encouraging, and highly specific daily horoscopes (Rashi Phala) in both Odia script and elegant English."))
                )
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (fullText != null) {
                    val split = fullText.split("===DIVIDER===")
                    val odiaText = split.firstOrNull()?.replace("PART 1: ODIA SCRIPT", "")?.trim() ?: "ରାଶିଫଳ ଉପଲବ୍ଧ ନାହିଁ।"
                    val englishText = split.getOrNull(1)?.replace("PART 2: ENGLISH", "")?.trim() ?: "Reading unavailable."

                    val finalOdia = odiaText.trim()
                    val finalEng = englishText.trim()

                    // Save to Room Cache
                    repository.saveReading(
                        SavedReading(
                            dateStr = dateStr,
                            rashi = zodiac,
                            readingOdia = finalOdia,
                            readingEnglish = finalEng
                        )
                    )

                    _dailyHoroscope.value = Pair(finalOdia, finalEng)
                } else {
                    throw Exception("Empty content candidate from AI model")
                }
            } catch (e: Exception) {
                // Fail-over gracefully to local rich forecast
                val fallback = getFallbackHoroscope(zodiac, date)
                _dailyHoroscope.value = fallback
                // Save it so we don't hit failure repeatedly
                repository.saveReading(
                    SavedReading(
                        dateStr = dateStr,
                        rashi = zodiac,
                        readingOdia = fallback.first,
                        readingEnglish = fallback.second
                    )
                )
            } finally {
                _rashiPhalaLoading.value = false
            }
        }
    }

    // --- AI Pandit Consult Chat ---

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return

        // Add user message to state
        val current = _chatMessages.value.toMutableList()
        current.add(message to true)
        _chatMessages.value = current

        viewModelScope.launch(Dispatchers.IO) {
            _chatLoading.value = true

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // Local intelligent fallback response based on keywords
                val reply = getLocalPanditResponse(message)
                val updated = _chatMessages.value.toMutableList()
                updated.add(reply to false)
                _chatMessages.value = updated
                _chatLoading.value = false
                return@launch
            }

            // Standard Gemini API call
            val systemInstructionText = """
                You are 'Odia Panjika Pandit', a wise, friendly, and deeply knowledgeable Vedic Astrologer, priest, and scholar of Odia culture and Jagannath philosophy.
                You answer questions about:
                1. Significance and rules of traditional Odia festivals (Raja, Ratha Yatra, Nuakhai, Prathamastami, etc.)
                2. Vedic rituals, pujas, fasting rules (brata/upas), and auspicious days
                3. Astrological parameters (Tithi, Nakshatra, Karana, Bela)
                4. General spiritual advice.
                
                Always respond in a very respectful, encouraging, and cultural manner. 
                Write your response in BOTH Odia script and English (or predominantly in Odia with a neat summary in English) so that it is highly accessible.
            """.trimIndent()

            // Construct history from last 6 messages
            val chatHistory = _chatMessages.value.takeLast(6).map { (text, isUser) ->
                Content(parts = listOf(Part(text = if (isUser) "User: $text" else "Pandit: $text")))
            }

            val request = GenerateContentRequest(
                contents = chatHistory,
                generationConfig = GenerationConfig(temperature = 0.7f),
                systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "କ୍ଷମା କରିବେ, ମୁଁ ବର୍ତ୍ତମାନ ବୁଝିପାରିଲି ନାହିଁ। \n\nI apologize, I couldn't process that right now."
                val updated = _chatMessages.value.toMutableList()
                updated.add(reply to false)
                _chatMessages.value = updated
            } catch (e: Exception) {
                val reply = "କ୍ଷମା କରିବେ, ନେଟୱର୍କ ସମସ୍ୟା ହେତୁ ସଂଯୋଗ ହୋଇପାରିଲା ନାହିଁ।\n\nSorry, a network connection error occurred. Here is some traditional wisdom: Always offer water to Surya Dev in the morning for success."
                val updated = _chatMessages.value.toMutableList()
                updated.add(reply to false)
                _chatMessages.value = updated
            } finally {
                _chatLoading.value = false
            }
        }
    }

    // --- Deterministic Local Fallback Forecast Engine (Craftsmanship) ---

    private fun getFallbackHoroscope(zodiac: String, date: LocalDate): Pair<String, String> {
        val seed = (zodiac.hashCode() + date.dayOfMonth + date.monthValue * 31 + date.year).toLong()
        val random = Random(seed)

        val luckPercentage = 60 + random.nextInt(36)
        val luckyNumber = 1 + random.nextInt(9)
        val luckyColors = listOf("Red / ଲାଲ୍", "Yellow / ହଳଦିଆ", "White / ଧଳା", "Orange / ସିନ୍ଦୂର", "Saffron / ଗେରୁଆ", "Cream / ଚନ୍ଦନ ରଙ୍ଗ")
        val luckyColor = luckyColors[random.nextInt(luckyColors.size)]

        val healthScore = 70 + random.nextInt(26)
        val careerScore = 65 + random.nextInt(31)
        val familyScore = 75 + random.nextInt(21)

        val odiaGreetings = listOf(
            "ଜୟ ଜଗନ୍ନାଥ! ଆଜି ଆପଣଙ୍କ ପାଇଁ ଦିନଟି ବହୁତ ଶୁଭଦାୟକ ହେବ।",
            "ଶୁଭ ସକାଳ! ଗ୍ରହ ନକ୍ଷତ୍ରର ପ୍ରଭାବ ଆପଣଙ୍କୁ ନୂଆ ସକାରାତ୍ମକ ଉର୍ଜା ପ୍ରଦାନ କରିବ।",
            "ମହାପ୍ରଭୁଙ୍କ କୃପାରୁ ଆଜି ଆପଣ ସମସ୍ତ କାର୍ଯ୍ୟରେ ସଫଳତା ପାଇବେ।"
        )

        val odiaCareer = listOf(
            "କର୍ମକ୍ଷେତ୍ରରେ ଆଜି ସହକର୍ମୀଙ୍କ ସହଯୋଗ ମିଳିବ ଏବଂ ଅଟକି ରହିଥିବା କାର୍ଯ୍ୟ ପୂର୍ଣ୍ଣ ହେବ। ବ୍ୟବସାୟରେ ଆର୍ଥିକ ଲାଭ ହେବାର ସମ୍ଭାବନା ଅଛି।",
            "ଆଜି ବ୍ୟବସାୟ ଓ ବାଣିଜ୍ୟରେ ନୂତନ ସୁଯୋଗ ମିଳିବ। ଚାକିରିଆମାନେ ନିଜ କାର୍ଯ୍ୟରେ ପ୍ରଶଂସିତ ହେବେ। ଦୂର ଯାତ୍ରା ସଫଳ ହେବ।",
            "ନୂତନ ପ୍ରକଳ୍ପ ଆରମ୍ଭ କରିବା ପାଇଁ ଆଜି ଅତ୍ୟନ୍ତ ଅନୁକୂଳ ଦିନ। ଆର୍ଥିକ ସ୍ଥିତିରେ ସୁଧାର ଆସିବ। ନୂଆ ନିବେଶ ପାଇଁ ଶୁଭ ଦିନ।"
        )

        val odiaFamily = listOf(
            "ପରିବାରରେ ହସଖୁସିର ମାହୋଲ ରହିବ। ପିତାମାତାଙ୍କ ଆଶୀର୍ବାଦ ମିଳିବ ଏବଂ ଜୀବନସାଥୀଙ୍କ ସହ ସମ୍ପର୍କ ମଧୁର ହେବ।",
            "ପାରିବାରିକ ସମସ୍ୟାର ସମାଧାନ ହେବ। ବନ୍ଧୁବାନ୍ଧବଙ୍କ ଆଗମନ ଦ୍ୱାରା ଘରେ ଆନନ୍ଦ ମିଳିବ। ସନ୍ତାନଙ୍କ ସଫଳତାରେ ଆପଣ ଖୁସି ହେବେ।",
            "ପରିବାର ସହିତ ସମୟ ଅତିବାହିତ କରିବା ପାଇଁ ଆଜି ଉତ୍ତମ ଦିନ। ଭ୍ରାତୃ ଓ ଭଗ୍ନୀଙ୍କ ଠାରୁ ଶୁଭ ସମ୍ବାଦ ପାଇବେ।"
        )

        val odiaHealth = listOf(
            "ସ୍ୱାସ୍ଥ୍ୟ ଭଲ ରହିବ। ମନ ଶାନ୍ତ ଏବଂ ପ୍ରସନ୍ନ ରହିବ। ଯୋଗ ଓ ପ୍ରାଣାୟାମ କରନ୍ତୁ, ଯାହା ଶରୀରକୁ ସୁସ୍ଥ ରଖିବ।",
            "ଆଜି ଆପଣ ଶାରୀରିକ ଓ ମାନସିକ ସ୍ତରରେ ବହୁତ ସକ୍ରିୟ ରହିବେ। ପୂରୁଣା ବେମାରୀରୁ ମୁକ୍ତି ମିଳିପାରେ। ଖାଦ୍ୟପେୟର ଯତ୍ନ ନିଅନ୍ତୁ।",
            "ସ୍ୱାସ୍ଥ୍ୟ ସାମାନ୍ୟ ରହିବ। ଅତ୍ୟଧିକ ପରିଶ୍ରମ ଯୋଗୁଁ କ୍ଲାନ୍ତି ଅନୁଭବ କରିପାରନ୍ତି। ପର୍ଯ୍ୟାପ୍ତ ପରିମାଣର ବିଶ୍ରାମ ନିଅନ୍ତୁ।"
        )

        val odiaRemedy = listOf(
            "ମହାପ୍ରଭୁ ଶ୍ରୀ ଜଗନ୍ନାଥଙ୍କୁ ସ୍ମରଣ କରି ତୁଳସୀ ପତ୍ର ଅର୍ପଣ କରନ୍ତୁ ଏବଂ କାର୍ଯ୍ୟ ଆରମ୍ଭ କରନ୍ତୁ।",
            "ସକାଳୁ ତାମ୍ର ପାତ୍ରରୁ ସୂର୍ଯ୍ୟ ଦେବତାଙ୍କୁ ଜଳ ଅର୍ପଣ କରନ୍ତୁ ଏବଂ 'ଓଁ ସୂର୍ଯ୍ୟାୟ ନମଃ' ଜପ କରନ୍ତୁ।",
            "ହନୁମାନ ମନ୍ଦିର ଯାଇ ସିନ୍ଦୂର ଚଢ଼ାନ୍ତୁ ଏବଂ ହନୁମାନ ଚାଳିଶା ପାଠ କରନ୍ତୁ, ସମସ୍ତ ବିଘ୍ନ ଦୂର ହେବ।"
        )

        val odiaText = """
            🌟 ${odiaGreetings[random.nextInt(odiaGreetings.size)]}
            
            💼 **କର୍ମ ଓ ଆର୍ଥିକ:** ${odiaCareer[random.nextInt(odiaCareer.size)]}
            
            🏡 **ପାରିବାରିକ:** ${odiaFamily[random.nextInt(odiaFamily.size)]}
            
            🩺 **ସ୍ୱାସ୍ଥ୍ୟ:** ${odiaHealth[random.nextInt(odiaHealth.size)]}
            
            🌸 **ପ୍ରତିକାର / ନୀତି:** ${odiaRemedy[random.nextInt(odiaRemedy.size)]}
            
            🎯 **ଶୁଭ ସୂଚନା:**
            • ଶୁଭ ପ୍ରତିଶତ: $luckPercentage%
            • ଶୁଭ ନମ୍ବର: $luckyNumber
            • ଶୁଭ ରଙ୍ଗ: $luckyColor
            • ସ୍ୱାସ୍ଥ୍ୟ: $healthScore% | କାର୍ଯ୍ୟ: $careerScore% | ପରିବାର: $familyScore%
        """.trimIndent()

        val engText = """
            🌟 **Overview:** Today is highly promising with $luckPercentage% favorable planetary alignment for $zodiac. Lord Jagannatha's blessings guide you.
            
            💼 **Career & Wealth:** Career aspects are excellent. Stalled projects will resume. A favorable period for new business partnerships, planning, and financial investments.
            
            🏡 **Family & Love:** Peace and harmony will prevail in your household. You will secure key blessings from your elders, and your bonding with your spouse will deepen.
            
            🩺 **Health & Energy:** Energetic day! Physically and mentally active. Maintain a balanced diet, incorporate yogic exercises, and take adequate hydration.
            
            🌸 **Lucky Remedy:** Offer fresh Tulasi leaves to Lord Jagannatha or light a ghee lamp at a nearby temple. Reciting the Gayatri Mantra brings peace.
            
            🎯 **Auspicious Stats:**
            • Lucky Number: $luckyNumber
            • Lucky Color: $luckyColor
            • Luck Quotient: $luckPercentage%
            • Health: $healthScore% | Career: $careerScore% | Family: $familyScore%
        """.trimIndent()

        return Pair(odiaText, engText)
    }

    private fun getLocalPanditResponse(message: String): String {
        val msg = message.lowercase()
        return when {
            msg.contains("ratha") || msg.contains("rath") || msg.contains("ରଥ") -> {
                """
                    🙏 **ଶ୍ରୀଗୁଣ୍ଡିଚା ରଥଯାତ୍ରା ପ୍ରସଙ୍ଗ (Ratha Yatra Information):**
                    ଓଡ଼ିଶାର ପବିତ୍ର ରଥଯାତ୍ରା ଆଷାଢ଼ ଶୁକ୍ଳ ଦ୍ୱିତୀୟା ତିଥିରେ ଅନୁଷ୍ଠିତ ହୁଏ। ୨୦୨୬ ମସିହାରେ ଏହା **ଜୁଲାଇ ୧୬** ତାରିଖରେ ପଡୁଅଛି। 
                    ଏହି ଦିନ ମହାପ୍ରଭୁ ଜଗନ୍ନାଥ, ବଳଭଦ୍ର ଏବଂ ମାତା ସୁଭଦ୍ରା ଶ୍ରୀମନ୍ଦିରରୁ ବାହାରି ଶ୍ରୀଗୁଣ୍ଡିଚା ମନ୍ଦିରକୁ ଯାତ୍ରା କରନ୍ତି।

                    **Key Details in English:**
                    Ratha Yatra falls on the Ashadha Sukla Dwitiya. In 2026, it is on **July 16**. It commemorates the annual journey of Lord Jagannatha, Lord Balabhadra, and Devi Subhadra to their aunt's house (Gundicha Temple). It is highly auspicious to chant 'Jai Jagannath' and pull the ropes on this day.
                """.trimIndent()
            }
            msg.contains("raja") || msg.contains("ରଜ") -> {
                """
                    🙏 **ରଜ ପର୍ବ ସମ୍ବନ୍ଧରେ (Raja Parba Festival):**
                    ରଜ ଓଡ଼ିଶାର ଏକ ପ୍ରସିଦ୍ଧ ପାରମ୍ପରିକ ଗଣପର୍ବ। ଏହା ମିଥୁନ ସଂକ୍ରାନ୍ତି ସହ ସମ୍ପୃକ୍ତ। ୨୦୨୬ ମସିହାରେ:
                    • ଜୁନ୍ ୧୪: ପହିଲି ରଜ (Pahili Raja)
                    • ଜୁନ୍ ୧୫: ରଜ ସଂକ୍ରାନ୍ତି (Raja Sankranti)
                    • ଜୁନ୍ ୧୬: ଭୂମି ଦହନ / ବାସି ରଜ (Basi Raja)
                    ଏହି ପର୍ବରେ ବସୁମତୀ ମାତାଙ୍କୁ ପୂଜା କରାଯାଏ, ଦୋଳି ଖେଳାଯାଏ ଏବଂ ବିଭିନ୍ନ ପ୍ରକାର ପିଠା (ପୋଡ଼ ପିଠା) ତିଆରି ହୁଏ।

                    **In English:**
                    Raja Parba is a unique 3-day agricultural/fertility festival in Odisha celebrating womanhood and Mother Earth. Women enjoy swings (Doli), wear new clothes, play card games, and feast on delicious Poda Pitha.
                """.trimIndent()
            }
            msg.contains("nuakhai") || msg.contains("ନୂଆଖାଇ") -> {
                """
                    🙏 **ନୂଆଖାଇ ଜୁହାର (Nuakhai Festival):**
                    ନୂଆଖାଇ ପଶ୍ଚିମ ଓଡ଼ିଶାର ଏକ ସର୍ବଶ୍ରେଷ୍ଠ ପାରମ୍ପରିକ ପର୍ବ। ଏହା ଭାଦ୍ରବ ଶୁକ୍ଳ ପଞ୍ଚମୀ ତିଥିରେ ପାଳନ କରାଯାଏ। ୨୦୨୬ ମସିହାରେ ଏହା **ସେପ୍ଟେମ୍ବର ୧୬** ତାରିଖରେ ପଡୁଛି। 
                    ଏହି ଦିନ ମା' ସମଲେଶ୍ୱରୀଙ୍କୁ ନୂତନ ଶସ୍ୟ (ନବାନ୍ନ) ଲାଗି କରାଯାଇ ପରିବାରର ସମସ୍ତ ସଦସ୍ୟ ଏକାଠି ପ୍ରସାଦ ପାଇଥାନ୍ତି।

                    **In English:**
                    Nuakhai is the major harvest festival of Western Odisha, occurring on Bhadraba Sukla Panchami. In 2026, it is celebrated on **September 16**. People worship Mother Samaleswari with the newly harvested crop (Nabanna) and seek elders' blessings.
                """.trimIndent()
            }
            msg.contains("pana") || msg.contains("new year") || msg.contains("ପଣା") || msg.contains("ନବବର୍ଷ") -> {
                """
                    🙏 **ମହା ବିଷୁବ ପଣା ସଂକ୍ରାନ୍ତି (Pana Sankranti):**
                    ପଣା ସଂକ୍ରାନ୍ତି ହେଉଛି ଓଡ଼ିଆ ନବବର୍ଷ (Odia New Year)। ଏହା ମେଷ ସଂକ୍ରାନ୍ତି ଦିନ ପାଳିତ ହୁଏ (ସାଧାରଣତଃ **ଏପ୍ରିଲ ୧୪**)। ଏହି ଦିନ ଛତା, ଚଟି ଓ ପଣା ହାଣ୍ଡି ଦାନ କରିବାର ପରମ୍ପରା ରହିଛି। ବିଜ୍ଞାନ ସମ୍ମତ ଭାବେ ଏହା ଗ୍ରୀଷ୍ମ ଋତୁର ପ୍ରକୃତ ଆରମ୍ଭକୁ ଦର୍ଶାଏ।

                    **In English:**
                    Pana Sankranti represents the traditional Odia New Year. It is observed on Mesa Sankranti (usually **April 14**). Families hang a small earthen pot filled with a sweet herbal beverage (Pana) beneath the holy basil (Tulsi) plant, signifying watering in the hot summer.
                """.trimIndent()
            }
            else -> {
                """
                    🙏 **ବୈଦିକ ଉପଦେଶ ଓ କଲ୍ୟାଣ (Pandit's Traditional Wisdom):**
                    ଆପଣଙ୍କ ପ୍ରଶ୍ନ ପାଇଁ ଧନ୍ୟବାଦ। ଓଡ଼ିଶାର ସଂସ୍କୃତି ଏବଂ ପାଞ୍ଜି ଅତ୍ୟନ୍ତ ସମୃଦ୍ଧ। ସର୍ବଦା ମନେରଖନ୍ତୁ:
                    • ସକାଳେ ତୁଳସୀ ମୂଳେ ଜଳ ଦାନ କରନ୍ତୁ।
                    • ଗୁରୁଜନଙ୍କୁ ଆଦର କରନ୍ତୁ ଏବଂ ସେମାନଙ୍କ ଆଶୀର୍ବାଦ ନିଅନ୍ତୁ।
                    • ଯଦି ଆପଣ କୌଣସି ପୂଜା କରିବାକୁ ଚାହୁଁଛନ୍ତି, ତେବେ ସକାଳର ଶୁଭ ଅମୃତ ବେଳା ବାଛି କାର୍ଯ୍ୟ କରନ୍ତୁ।

                    **English translation:**
                    Thank you for your inquiry. Odia culture is based on Jagannath Consciousness (Jagannatha Chetana). For peace and prosperity:
                    • Dedicate water to the holy Tulsi plant in the morning.
                    • Respect your elders and always secure their blessings before critical life tasks.
                    • Execute main activities during the daily 'Amruta Bela' (auspicious timing) shown in our calendar tab.
                """.trimIndent()
            }
        }
    }
}
