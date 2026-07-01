package com.example.utils

import com.example.data.models.OdiaDayInfo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object OdiaCalendarCalculator {

    private val ODIA_MONTHS_LUNAR = listOf(
        "Baisakha" to "ବୈଶାଖ",
        "Jyestha" to "ଜ୍ୟେଷ୍ଠ",
        "Ashadha" to "ଆଷାଢ଼",
        "Shrabana" to "ଶ୍ରାବଣ",
        "Bhadraba" to "ଭାଦ୍ରବ",
        "Aswina" to "ଆଶ୍ୱିନ",
        "Kartika" to "କାର୍ତ୍ତିକ",
        "Margasira" to "ମାର୍ଗଶିର",
        "Pausha" to "ପୌଷ",
        "Magha" to "ମାଘ",
        "Phalguna" to "ଫାଲ୍ଗୁନ",
        "Chaitra" to "ଚୈତ୍ର"
    )

    private val ODIA_MONTHS_SOLAR = listOf(
        "Mesa" to "ମେଷ",
        "Vrisha" to "ବୃଷ",
        "Mithuna" to "ମିଥୁନ",
        "Karka" to "କର୍କଟ",
        "Simha" to "ସିଂହ",
        "Kanya" to "କନ୍ୟା",
        "Tula" to "ତୁଳା",
        "Vrischika" to "ବିଛା",
        "Dhanu" to "ଧନୁ",
        "Makara" to "ମକର",
        "Kumbha" to "କୁମ୍ଭ",
        "Meena" to "ମୀନ"
    )

    private val TITHIS = listOf(
        "Pratipada" to "ପ୍ରତିପଦା",
        "Dwitiya" to "ଦ୍ୱିତୀୟା",
        "Tritiya" to "ତୃତୀୟା",
        "Chaturthi" to "ଚତୁର୍ଥୀ",
        "Panchami" to "ପଞ୍ଚମୀ",
        "Shasthi" to "ଷଷ୍ଠୀ",
        "Saptami" to "ସପ୍ତମୀ",
        "Ashtami" to "ଅଷ୍ଟମୀ",
        "Navami" to "ନବମୀ",
        "Dashami" to "ଦଶମୀ",
        "Ekadashi" to "ଏକାଦଶୀ",
        "Dwadashi" to "ଦ୍ୱାଦଶୀ",
        "Trayodashi" to "ତ୍ରୟୋଦଶୀ",
        "Chaturdashi" to "ଚତୁର୍ଦ୍ଦଶୀ",
        "Purnima" to "ପୂର୍ଣ୍ଣିମା", // 15th Sukla
        "Amavasya" to "ଅମାବାସ୍ୟା" // 15th Krishna
    )

    private val NAKSHATRAS = listOf(
        "Ashwini" to "ଅଶ୍ୱିନୀ", "Bharani" to "ଭରଣୀ", "Krittika" to "କୃତ୍ତିକା",
        "Rohini" to "ରୋହିଣୀ", "Mrigashira" to "ମୃଗଶିରା", "Ardra" to "ଆର୍ଦ୍ରା",
        "Punarvasu" to "ପୁନର୍ବସୁ", "Pushya" to "ପୁଷ୍ୟା", "Ashlesha" to "ଅଶ୍ଳେଷା",
        "Magha" to "ମଘା", "Purva Phalguni" to "ପୂର୍ବ ଫାଲ୍ଗୁନୀ", "Uttara Phalguni" to "ଉତ୍ତର ଫାଲ୍ଗୁନୀ",
        "Hasta" to "ହସ୍ତା", "Chitra" to "ଚିତ୍ରା", "Swati" to "ସ୍ୱାତୀ",
        "Visakha" to "ବିଶାଖା", "Anuradha" to "ଅନୁରାଧା", "Jyeshtha" to "ଜ୍ୟେଷ୍ଠା",
        "Mula" to "ମୂଳା", "Purva Ashadha" to "ପୂର୍ବ ଆଷାଢ଼ା", "Uttara Ashadha" to "ଉତ୍ତର ଆଷାଢ଼ା",
        "Shravana" to "ଶ୍ରାବଣୀ", "Dhanishta" to "ଧନିଷ୍ଠା", "Shatabhisha" to "ଶତଭିଷା",
        "Purva Bhadrapada" to "ପୂର୍ବ ଭାଦ୍ରପଦ", "Uttara Bhadrapada" to "ଉତ୍ତର ଭାଦ୍ରପଦ", "Revati" to "ରେବତୀ"
    )

    // Accurate static mapping of festivals for 2026
    private val FESTIVALS_2026 = mapOf(
        LocalDate.of(2026, 1, 1) to ("New Year's Day" to "ଇଂରାଜୀ ନବବର୍ଷ"),
        LocalDate.of(2026, 1, 14) to ("Makara Sankranti" to "ମକର ସଂକ୍ରାନ୍ତି"),
        LocalDate.of(2026, 1, 23) to ("Netaji Jayanti" to "ନେତାଜୀ ଜୟନ୍ତୀ"),
        LocalDate.of(2026, 1, 26) to ("Republic Day" to "ସାଧାରଣତନ୍ତ୍ର ଦିବସ"),
        LocalDate.of(2026, 1, 28) to ("Samba Dashami" to "ଶାମ୍ବ ଦଶମୀ"),
        LocalDate.of(2026, 2, 11) to ("Saraswati Puja" to "ସରସ୍ୱତୀ ପୂଜା / ବସନ୍ତ ପଞ୍ଚମୀ"),
        LocalDate.of(2026, 3, 6) to ("Maha Shivaratri" to "ମହା ଶିବରାତ୍ରି / ଜାଗର"),
        LocalDate.of(2026, 3, 22) to ("Dola Purnima" to "ଦୋଳ ପୂର୍ଣ୍ଣିମା"),
        LocalDate.of(2026, 3, 23) to ("Holi" to "ହୋଲି"),
        LocalDate.of(2026, 4, 1) to ("Utkala Dibasa" to "ଉତ୍କଳ ଦିବସ"),
        LocalDate.of(2026, 4, 14) to ("Pana Sankranti (Odia New Year)" to "ପଣା ସଂକ୍ରାନ୍ତି / ମହାବିଷୁବ ସଂକ୍ରାନ୍ତି"),
        LocalDate.of(2026, 4, 20) to ("Akshaya Tritiya" to "ଅକ୍ଷୟ ତୃତୀୟା (ଚନ୍ଦନ ଯାତ୍ରା ଆରମ୍ଭ)"),
        LocalDate.of(2026, 6, 14) to ("Pahili Raja" to "ପହିଲି ରଜ"),
        LocalDate.of(2026, 6, 15) to ("Raja Sankranti" to "ରଜ ସଂକ୍ରାନ୍ତି"),
        LocalDate.of(2026, 6, 16) to ("Bhuin Dahana / Raja Basi" to "ଭୂମି ଦହନ / ବାସି ରଜ"),
        LocalDate.of(2026, 6, 29) to ("Deva Snana Purnima" to "ଦେବ ସ୍ନାନ ପୂର୍ଣ୍ଣିମା"),
        LocalDate.of(2026, 7, 16) to ("Ratha Yatra" to "ରଥଯାତ୍ରା (ଶ୍ରୀଗୁଣ୍ଡିଚା)"),
        LocalDate.of(2026, 7, 24) to ("Bahuda Yatra" to "ବାହୁଡ଼ା ଯାତ୍ରା"),
        LocalDate.of(2026, 7, 26) to ("Suna Besha" to "ସୁନାବେଶ"),
        LocalDate.of(2026, 8, 28) to ("Gamha Purnima (Rakhi)" to "ଗହ୍ମା ପୂର୍ଣ୍ଣିମା (ରାକ୍ଷୀ ବନ୍ଧନ)"),
        LocalDate.of(2026, 9, 15) to ("Ganesha Chaturthi" to "ଗଣେଶ ଚତୁର୍ଥୀ"),
        LocalDate.of(2026, 9, 16) to ("Nuakhai" to "ନୂଆଖାଇ"),
        LocalDate.of(2026, 10, 10) to ("Mahalaya" to "ମହାଳୟା"),
        LocalDate.of(2026, 10, 16) to ("Durga Puja (Maha Sasthi)" to "ଦୁର୍ଗା ପୂଜା (ମହାଷଷ୍ଠୀ)"),
        LocalDate.of(2026, 10, 17) to ("Maha Saptami" to "ମହାସପ୍ତମୀ / କୁମାର ପୂଜା ଆରମ୍ଭ"),
        LocalDate.of(2026, 10, 18) to ("Maha Ashtami" to "ମହାଷ୍ଟମୀ"),
        LocalDate.of(2026, 10, 19) to ("Maha Navami" to "ମହାନବମୀ"),
        LocalDate.of(2026, 10, 20) to ("Vijayadashami / Dussehra" to "ବିଜୟାଦଶମୀ / ଦଶହରା"),
        LocalDate.of(2026, 10, 25) to ("Kumar Purnima" to "କୁମାର ପୂର୍ଣ୍ଣିମା"),
        LocalDate.of(2026, 11, 8) to ("Deepavali / Kali Puja" to "ଦୀପାବଳି / କାଳୀ ପୂଜା"),
        LocalDate.of(2026, 11, 24) to ("Kartika Purnima (Boita Bandana)" to "କାର୍ତ୍ତିକ ପୂର୍ଣ୍ଣିମା / ବୋଇତ ବନ୍ଦାଣ"),
        LocalDate.of(2026, 12, 2) to ("Prathamastami" to "ପ୍ରଥମାଷ୍ଟମୀ"),
        LocalDate.of(2026, 12, 25) to ("Christmas Day" to "ବଡ଼ଦିନ (ଖ୍ରୀଷ୍ଟମାସ)")
    )

    // Accurate static mapping of festivals for 2027
    private val FESTIVALS_2027 = mapOf(
        LocalDate.of(2027, 1, 1) to ("New Year's Day" to "ଇଂରାଜୀ ନବବର୍ଷ"),
        LocalDate.of(2027, 1, 14) to ("Makara Sankranti" to "ମକର ସଂକ୍ରାନ୍ତି"),
        LocalDate.of(2027, 1, 17) to ("Samba Dashami" to "ଶାମ୍ବ ଦଶମୀ"),
        LocalDate.of(2027, 1, 26) to ("Republic Day" to "ସାଧାରଣତନ୍ତ୍ର ଦିବସ"),
        LocalDate.of(2027, 1, 30) to ("Netaji Jayanti" to "ନେତାଜୀ ଜୟନ୍ତୀ"),
        LocalDate.of(2027, 1, 31) to ("Saraswati Puja" to "ସରସ୍ୱତୀ ପୂଜା"),
        LocalDate.of(2027, 3, 6) to ("Maha Shivaratri" to "ମହା ଶିବରାତ୍ରି"),
        LocalDate.of(2027, 3, 22) to ("Dola Purnima" to "ଦୋଳ ପୂର୍ଣ୍ଣିମା"),
        LocalDate.of(2027, 3, 23) to ("Holi" to "ହୋଲି"),
        LocalDate.of(2027, 4, 1) to ("Utkala Dibasa" to "ଉତ୍କଳ ଦିବସ"),
        LocalDate.of(2027, 4, 14) to ("Pana Sankranti (Odia New Year)" to "ପଣା ସଂକ୍ରାନ୍ତି / ମହାବିଷୁବ ସଂକ୍ରାନ୍ତି"),
        LocalDate.of(2027, 5, 9) to ("Akshaya Tritiya" to "ଅକ୍ଷୟ ତୃତୀୟା"),
        LocalDate.of(2027, 6, 14) to ("Pahili Raja" to "ପହିଲି ରଜ"),
        LocalDate.of(2027, 6, 15) to ("Raja Sankranti" to "រଜ ସଂକ୍ରାନ୍ତି"),
        LocalDate.of(2027, 6, 16) to ("Bhuin Dahana" to "ଭୂମି ଦହନ"),
        LocalDate.of(2027, 7, 5) to ("Ratha Yatra" to "ରଥଯାତ୍ରା"),
        LocalDate.of(2027, 10, 9) to ("Dussehra" to "ଦଶହରା"),
        LocalDate.of(2027, 11, 13) to ("Kartika Purnima" to "କାର୍ତ୍ତିକ ପୂର୍ଣ୍ଣିମା")
    )

    // Base astronomical reference: July 14, 2026 is Ashadha Amavasya (New Moon).
    private val LUNAR_REF_DATE = LocalDate.of(2026, 7, 14)
    private const val LUNAR_CYCLE = 29.530588853 // Synodic month in days

    fun getOdiaDayInfo(date: LocalDate): OdiaDayInfo {
        // 1. Calculate Solar Month & Day
        val (solarMonth, solarDay) = getSolarMonthAndDay(date)

        // 2. Calculate Lunar Age to find Tithi, Paksha & Lunar Month
        val daysDiff = ChronoUnit.DAYS.between(LUNAR_REF_DATE, date)
        val lunarAge = ((daysDiff % LUNAR_CYCLE) + LUNAR_CYCLE) % LUNAR_CYCLE

        // Define Paksha and Tithi
        val isSukla = lunarAge <= 14.765
        val pakshaNameEng = if (isSukla) "Sukla Paksha" else "Krishna Paksha"
        val pakshaNameOdia = if (isSukla) "ଶୁକ୍ଳ ପକ୍ଷ" else "କୃଷ୍ଣ ପକ୍ଷ"

        val tithiValue = if (isSukla) lunarAge else lunarAge - 14.765
        // Scale to 15 parts
        var tithiIndex = (tithiValue / 0.9843).toInt()
        if (tithiIndex < 0) tithiIndex = 0
        if (tithiIndex > 14) tithiIndex = 14

        // If very close to 15th tithi, use Purnima/Amavasya
        val isLastTithi = tithiIndex == 14
        val tithiPair = TITHIS[tithiIndex]
        val tithiNameEng = if (isLastTithi) {
            if (isSukla) "Purnima" else "Amavasya"
        } else {
            tithiPair.first
        }
        val tithiNameOdia = if (isLastTithi) {
            if (isSukla) "ପୂର୍ଣ୍ଣିମା" else "ଅମାବାସ୍ୟା"
        } else {
            tithiPair.second
        }

        // 3. Approximate Lunar Month
        // Ashadha Amavasya is July 14, 2026.
        // We can offset the lunar month index based on new moons from July 2026.
        // Ashadha (Index 2) -> July 2026 (New Moon starts Shrabana after July 14).
        val lunarMonthOffset = Math.floor(daysDiff / LUNAR_CYCLE).toInt()
        val baseLunarMonthIndex = 2 // Ashadha
        val calculatedLunarMonthIndex = ((baseLunarMonthIndex + lunarMonthOffset) % 12 + 12) % 12
        val lunarMonth = ODIA_MONTHS_LUNAR[calculatedLunarMonthIndex]

        // 4. Nakshatra (approximate daily rotation)
        val nakshatraIndex = ((daysDiff % 27 + 27) % 27).toInt()
        val nakshatra = NAKSHATRAS[nakshatraIndex]

        // 5. Festival mapping
        val festivalPair = when (date.year) {
            2026 -> FESTIVALS_2026[date]
            2027 -> FESTIVALS_2027[date]
            else -> null
        }

        // Determine if general day is auspicious
        // Tuesdays/Saturdays or specific inauspicious tithis
        val isAuspicious = date.dayOfWeek.value != 2 && date.dayOfWeek.value != 6 && tithiIndex != 3 && tithiIndex != 8

        // Auspicious times based on weekday
        val dayVal = date.dayOfWeek.value
        val auspiciousTimes = when (dayVal) {
            1 -> "09:15 AM - 11:00 AM" // Mon
            2 -> "11:30 AM - 01:00 PM" // Tue
            3 -> "07:30 AM - 09:15 AM" // Wed
            4 -> "10:45 AM - 12:30 PM" // Thu
            5 -> "08:15 AM - 10:00 AM" // Fri
            6 -> "01:30 PM - 03:00 PM" // Sat
            else -> "09:00 AM - 10:45 AM" // Sun
        }
        val inauspiciousTimes = when (dayVal) {
            1 -> "07:30 AM - 09:00 AM"
            2 -> "03:00 PM - 04:30 PM"
            3 -> "12:00 PM - 01:30 PM"
            4 -> "01:30 PM - 03:00 PM"
            5 -> "10:30 AM - 12:00 PM"
            6 -> "09:00 AM - 10:30 AM"
            else -> "04:30 PM - 06:00 PM"
        }

        return OdiaDayInfo(
            englishDate = date,
            odiaMonthEng = solarMonth.first,
            odiaMonthOdia = solarMonth.second,
            lunarMonthEng = lunarMonth.first,
            lunarMonthOdia = lunarMonth.second,
            odiaDay = solarDay,
            tithiEng = tithiNameEng,
            tithiOdia = tithiNameOdia,
            pakshaEng = pakshaNameEng,
            pakshaOdia = pakshaNameOdia,
            nakshatraEng = nakshatra.first,
            nakshatraOdia = nakshatra.second,
            festivalEng = festivalPair?.first,
            festivalOdia = festivalPair?.second,
            isAuspicious = isAuspicious,
            auspiciousTimings = auspiciousTimes,
            inauspiciousTimings = inauspiciousTimes
        )
    }

    private fun getSolarMonthAndDay(date: LocalDate): Pair<Pair<String, String>, Int> {
        val month = date.monthValue
        val day = date.dayOfMonth

        // Determine solar month and day of that solar month based on Sankranti transitions.
        val solarMonthIndex: Int
        val solarDay: Int

        when (month) {
            1 -> { // Jan
                if (day < 14) {
                    solarMonthIndex = 8 // Dhanu
                    solarDay = day + 16 // Approx day of Dhanu
                } else {
                    solarMonthIndex = 9 // Makara
                    solarDay = day - 14 + 1
                }
            }
            2 -> { // Feb
                if (day < 13) {
                    solarMonthIndex = 9 // Makara
                    solarDay = day + 17
                } else {
                    solarMonthIndex = 10 // Kumbha
                    solarDay = day - 13 + 1
                }
            }
            3 -> { // Mar
                if (day < 14) {
                    solarMonthIndex = 10 // Kumbha
                    solarDay = day + 16
                } else {
                    solarMonthIndex = 11 // Meena
                    solarDay = day - 14 + 1
                }
            }
            4 -> { // Apr
                if (day < 14) {
                    solarMonthIndex = 11 // Meena
                    solarDay = day + 17
                } else {
                    solarMonthIndex = 0 // Mesa
                    solarDay = day - 14 + 1
                }
            }
            5 -> { // May
                if (day < 15) {
                    solarMonthIndex = 0 // Mesa
                    solarDay = day + 17
                } else {
                    solarMonthIndex = 1 // Vrisha
                    solarDay = day - 15 + 1
                }
            }
            6 -> { // Jun
                if (day < 15) {
                    solarMonthIndex = 1 // Vrisha
                    solarDay = day + 16
                } else {
                    solarMonthIndex = 2 // Mithuna
                    solarDay = day - 15 + 1
                }
            }
            7 -> { // Jul
                if (day < 16) {
                    solarMonthIndex = 2 // Mithuna
                    solarDay = day + 16
                } else {
                    solarMonthIndex = 3 // Karka
                    solarDay = day - 16 + 1
                }
            }
            8 -> { // Aug
                if (day < 16) {
                    solarMonthIndex = 3 // Karka
                    solarDay = day + 15
                } else {
                    solarMonthIndex = 4 // Simha
                    solarDay = day - 16 + 1
                }
            }
            9 -> { // Sep
                if (day < 16) {
                    solarMonthIndex = 4 // Simha
                    solarDay = day + 15
                } else {
                    solarMonthIndex = 5 // Kanya
                    solarDay = day - 16 + 1
                }
            }
            10 -> { // Oct
                if (day < 17) {
                    solarMonthIndex = 5 // Kanya
                    solarDay = day + 15
                } else {
                    solarMonthIndex = 6 // Tula
                    solarDay = day - 17 + 1
                }
            }
            11 -> { // Nov
                if (day < 16) {
                    solarMonthIndex = 6 // Tula
                    solarDay = day + 14
                } else {
                    solarMonthIndex = 7 // Vrischika
                    solarDay = day - 16 + 1
                }
            }
            else -> { // Dec
                if (day < 16) {
                    solarMonthIndex = 7 // Vrischika
                    solarDay = day + 15
                } else {
                    solarMonthIndex = 8 // Dhanu
                    solarDay = day - 16 + 1
                }
            }
        }

        return ODIA_MONTHS_SOLAR[solarMonthIndex] to solarDay
    }
}
