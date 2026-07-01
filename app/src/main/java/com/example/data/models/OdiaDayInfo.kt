package com.example.data.models

import java.time.LocalDate

data class OdiaDayInfo(
    val englishDate: LocalDate,
    val odiaMonthEng: String,          // e.g. "Mithuna"
    val odiaMonthOdia: String,         // e.g. "ମିଥୁନ"
    val lunarMonthEng: String,         // e.g. "Ashadha"
    val lunarMonthOdia: String,        // e.g. "ଆଷାଢ଼"
    val odiaDay: Int,                  // Day of the solar month, e.g. 17
    val tithiEng: String,              // e.g. "Pratipada"
    val tithiOdia: String,             // e.g. "ପ୍ରତିପଦା"
    val pakshaEng: String,             // e.g. "Sukla Paksha"
    val pakshaOdia: String,            // e.g. "ଶୁକ୍ଳ ପକ୍ଷ"
    val nakshatraEng: String,          // e.g. "Ashwini"
    val nakshatraOdia: String,         // e.g. "ଅଶ୍ୱିନୀ"
    val festivalEng: String? = null,
    val festivalOdia: String? = null,
    val isAuspicious: Boolean = true,
    val auspiciousTimings: String = "",
    val inauspiciousTimings: String = ""
)
