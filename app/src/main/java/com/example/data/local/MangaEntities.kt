package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleArabic: String,
    val author: String,
    val artist: String,
    val coverDrawable: Int,
    val rating: Double,
    val ratingCount: String,
    val status: String,
    val releaseYear: Int,
    val ageRating: String,
    val genres: String, // comma-separated
    val synopsis: String,
    val isFavorite: Boolean,
    val readLater: Boolean,
    val folder: String,
    val currentChapter: Int,
    val totalChapters: Int,
    val readingProgress: Int,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val chapterNumber: Int,
    val title: String,
    val releaseDate: String,
    val isRead: Boolean,
    val isDownloaded: Boolean,
    val downloadSizeMb: Double,
    val pagesCount: Int
)

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val isBuiltIn: Boolean,
    val status: String,
    val description: String
)

@Entity(tableName = "downloads")
data class DownloadTaskEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val chapterNumber: Int,
    val mangaTitle: String,
    val chapterTitle: String,
    val coverDrawable: Int,
    val totalMb: Double,
    val downloadedMb: Double,
    val progress: Int, // 0 - 100
    val speedText: String, // e.g. "3.2 ميجابايت/ثانية"
    val timeRemainingText: String, // e.g. "00:00:24"
    val status: String, // "DOWNLOADING", "QUEUED", "COMPLETED", "PAUSED"
    val timestamp: Long = System.currentTimeMillis()
)
