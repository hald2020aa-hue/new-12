package com.example.data.model

data class Manga(
    val id: String,
    val title: String,
    val titleArabic: String,
    val author: String,
    val artist: String,
    val coverDrawable: Int,
    val rating: Double,
    val ratingCount: String,
    val status: String, // مستمر or مكتمل
    val releaseYear: Int,
    val ageRating: String, // PG-13
    val genres: List<String>,
    val synopsis: String,
    val isFavorite: Boolean = false,
    val readLater: Boolean = false,
    val folder: String = "المتابعة",
    val currentChapter: Int = 1,
    val totalChapters: Int = 179,
    val readingProgress: Int = 0 // 0 to 100
)

data class Chapter(
    val id: String,
    val mangaId: String,
    val chapterNumber: Int,
    val title: String,
    val releaseDate: String,
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadSizeMb: Double = 95.3,
    val pagesCount: Int = 28
)

data class MangaSource(
    val id: String,
    val name: String,
    val url: String,
    val isBuiltIn: Boolean,
    val status: SourceStatus = SourceStatus.WORKING,
    val description: String = ""
)

enum class SourceStatus(val label: String, val hexColor: Long) {
    WORKING("يعمل بشكل جيد", 0xFF00E676),
    SLOW("بطيء أحياناً", 0xFFFFB300),
    DOWN("غير متاح حالياً", 0xFFFF3D00)
}

data class SpeechBubbleTranslation(
    val id: String,
    val originalText: String,
    val arabicTranslation: String,
    val xPercent: Float, // relative to page
    val yPercent: Float,
    val widthPercent: Float,
    val heightPercent: Float
)

enum class ReadingDirection {
    VERTICAL, // عمودي (Webtoon)
    HORIZONTAL // أفقي (Manga RTL)
}

enum class AppColorTheme(val titleArabic: String, val primaryHex: Long, val secondaryHex: Long) {
    BLUE("الأزرق (الافتراضي)", 0xFF00D2FF, 0xFF0077FE),
    RED("الأحمر", 0xFFFF2A55, 0xFFC4143A),
    GREEN("الأخضر", 0xFF00E676, 0xFF00B0FF),
    PURPLE("البنفسجي", 0xFFA855F7, 0xFF6366F1),
    GOLD("ذهبي داكن", 0xFFF59E0B, 0xFFD97706)
}
