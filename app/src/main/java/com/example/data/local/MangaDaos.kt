package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga ORDER BY lastReadTimestamp DESC")
    fun getAllManga(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE isFavorite = 1 ORDER BY lastReadTimestamp DESC")
    fun getFavorites(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE readLater = 1 ORDER BY lastReadTimestamp DESC")
    fun getReadLater(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE id = :id")
    fun getMangaById(id: String): Flow<MangaEntity?>

    @Query("SELECT * FROM manga WHERE id = :id")
    suspend fun getMangaByIdOnce(id: String): MangaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangaList(list: List<MangaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)

    @Update
    suspend fun updateManga(manga: MangaEntity)

    @Query("UPDATE manga SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("UPDATE manga SET readingProgress = :progress, currentChapter = :chapter, lastReadTimestamp = :time WHERE id = :id")
    suspend fun updateProgress(id: String, chapter: Int, progress: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE manga SET folder = :folder WHERE id = :id")
    suspend fun updateFolder(id: String, folder: String)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY chapterNumber DESC")
    fun getChaptersForManga(mangaId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId AND chapterNumber = :num LIMIT 1")
    suspend fun getChapter(mangaId: String, num: Int): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE isDownloaded = 1")
    fun getDownloadedChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("UPDATE chapters SET isDownloaded = :downloaded WHERE mangaId = :mangaId AND chapterNumber = :num")
    suspend fun setChapterDownloaded(mangaId: String, num: Int, downloaded: Boolean)

    @Query("UPDATE chapters SET isRead = :isRead WHERE mangaId = :mangaId AND chapterNumber = :num")
    suspend fun setChapterRead(mangaId: String, num: Int, isRead: Boolean)
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY isBuiltIn DESC, name ASC")
    fun getAllSources(): Flow<List<SourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteSource(id: String)

    @Query("SELECT * FROM sources")
    suspend fun getAllSourcesList(): List<SourceEntity>

    @Query("DELETE FROM sources WHERE id NOT IN (:validIds)")
    suspend fun deleteOldSourcesExcept(validIds: List<String>)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTaskEntity)

    @Update
    suspend fun updateTask(task: DownloadTaskEntity)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM downloads")
    suspend fun clearAll()
}
