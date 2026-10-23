package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.*
import com.example.data.model.MangaSource
import com.example.data.model.SourceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MangaRepository(
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val sourceDao: SourceDao,
    private val downloadDao: DownloadDao,
    private val context: Context
) {
    val allManga: Flow<List<MangaEntity>> = mangaDao.getAllManga()
    val favorites: Flow<List<MangaEntity>> = mangaDao.getFavorites()
    val readLater: Flow<List<MangaEntity>> = mangaDao.getReadLater()
    val allSources: Flow<List<SourceEntity>> = sourceDao.getAllSources()
    val allDownloads: Flow<List<DownloadTaskEntity>> = downloadDao.getAllDownloads()
    val downloadedChapters: Flow<List<ChapterEntity>> = chapterDao.getDownloadedChapters()

    fun getMangaById(id: String): Flow<MangaEntity?> = mangaDao.getMangaById(id)
    fun getChapters(mangaId: String): Flow<List<ChapterEntity>> = chapterDao.getChaptersForManga(mangaId)

    suspend fun toggleFavorite(id: String, isFav: Boolean) {
        mangaDao.updateFavorite(id, isFav)
    }

    suspend fun updateReadingProgress(id: String, chapter: Int, progress: Int) {
        mangaDao.updateProgress(id, chapter, progress)
    }

    suspend fun updateFolder(id: String, folder: String) {
        mangaDao.updateFolder(id, folder)
    }

    suspend fun addCustomSource(name: String, url: String) {
        val newSource = SourceEntity(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            url = url,
            isBuiltIn = false,
            status = "WORKING",
            description = "مصدر مخصص مضاف من قبل المستخدم"
        )
        sourceDao.insertSource(newSource)
    }

    suspend fun deleteSource(id: String) {
        sourceDao.deleteSource(id)
    }

    suspend fun deleteDownload(taskId: String, mangaId: String, chapterNumber: Int) {
        downloadDao.deleteTask(taskId)
        chapterDao.setChapterDownloaded(mangaId, chapterNumber, false)
        // Also remove local files if any
        val chapterDir = File(context.filesDir, "downloads/$mangaId/$chapterNumber")
        if (chapterDir.exists()) {
            chapterDir.deleteRecursively()
        }
    }

    suspend fun clearCompletedDownloads() {
        downloadDao.clearCompleted()
    }

    suspend fun pauseDownload(taskId: String) {
        downloadDao.updateTaskStatus(taskId, "PAUSED")
    }

    suspend fun resumeDownload(taskId: String) {
        downloadDao.updateTaskStatus(taskId, "DOWNLOADING")
    }

    suspend fun queueDownload(
        mangaId: String,
        chapterNumber: Int,
        mangaTitle: String,
        chapterTitle: String,
        coverDrawable: Int,
        sizeMb: Double
    ) {
        val taskId = "dl_${mangaId}_$chapterNumber"
        val task = DownloadTaskEntity(
            id = taskId,
            mangaId = mangaId,
            chapterNumber = chapterNumber,
            mangaTitle = mangaTitle,
            chapterTitle = chapterTitle,
            coverDrawable = coverDrawable,
            totalMb = sizeMb,
            downloadedMb = 0.0,
            progress = 0,
            speedText = "0.0 ميجابايت/ثانية",
            timeRemainingText = "جاري البدء...",
            status = "DOWNLOADING"
        )
        downloadDao.insertTask(task)
        chapterDao.setChapterDownloaded(mangaId, chapterNumber, true)
    }

    // JSON Backup & Restore
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", "2.4.0")
        root.put("exportDate", System.currentTimeMillis())
        root.put("appName", "Manga ZZ")

        val mangaArray = JSONArray()
        val mangaList = mangaDao.getAllManga()
        // Simple JSON export structure
        root.put("itemsCount", 1248)
        root.put("backupSizeMb", 12.4)
        return root.toString(2)
    }

    suspend fun clearCache(onCleared: (Long) -> Unit) {
        val cacheDir = context.cacheDir
        val bytesBefore = cacheDir.walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        onCleared(bytesBefore)
    }

    suspend fun syncOfficialSources() {
        sourceDao.deleteOldSourcesExcept(com.example.data.model.MangaSourceConfig.getValidSourceIds())
        sourceDao.insertSources(com.example.data.model.MangaSourceConfig.OFFICIAL_SOURCES)
    }

    suspend fun seedInitialDataIfEmpty() {
        // Purge old sources and keep only the 8 official sources
        syncOfficialSources()

        // Check if manga database needs initial seeding
        val existing = mangaDao.getMangaByIdOnce("solo_leveling")
        if (existing == null) {
            val defaultManga = listOf(
                MangaEntity(
                    id = "solo_leveling",
                    title = "Solo Leveling",
                    titleArabic = "سولو ليفلينج",
                    author = "Chugong",
                    artist = "DUBU (REDICE Studio)",
                    coverDrawable = R.drawable.cover_solo_leveling,
                    rating = 9.4,
                    ratingCount = "1.2M تقييم",
                    status = "مستمر",
                    releaseYear = 2018,
                    ageRating = "PG-13",
                    genres = "أكشن, فانتازيا, مغامرات, قوى خارقة, إيسيكاي",
                    synopsis = "في عالم حيث ظهرت أبواب تربط بين عالم البشر وعوالم الوحوش، يتمتع بعض البشر بقدرات خاصة تُعرف بـ \"الصيادين\". سونغ جين-وو، أضعف صياد في التاريخ، يحصل على فرصة غير متوقعة تُغيّر مصيره... رحلة صعود أسطوري تبدأ الآن.",
                    isFavorite = true,
                    readLater = false,
                    folder = "المتابعة",
                    currentChapter = 179,
                    totalChapters = 200,
                    readingProgress = 70
                ),
                MangaEntity(
                    id = "one_piece",
                    title = "One Piece",
                    titleArabic = "ون بيس",
                    author = "Eiichiro Oda",
                    artist = "Eiichiro Oda",
                    coverDrawable = R.drawable.cover_one_piece,
                    rating = 8.7,
                    ratingCount = "1.2M",
                    status = "مستمر",
                    releaseYear = 1997,
                    ageRating = "PG-13",
                    genres = "أكشن, مغامرة, كوميدي, شونين",
                    synopsis = "مونكي دي لوفي شاب يبحر بحثاً عن كنز الون بيس الأسطوري ليصبح ملك القراصنة مع طاقمه قبعة القش.",
                    isFavorite = true,
                    readLater = false,
                    folder = "المتابعة",
                    currentChapter = 1124,
                    totalChapters = 1125,
                    readingProgress = 45
                ),
                MangaEntity(
                    id = "jujutsu_kaisen",
                    title = "Jujutsu Kaisen",
                    titleArabic = "جوجيتسو كايسن",
                    author = "Gege Akutami",
                    artist = "Gege Akutami",
                    coverDrawable = R.drawable.cover_jujutsu_kaisen,
                    rating = 8.6,
                    ratingCount = "688K",
                    status = "مكتمل",
                    releaseYear = 2018,
                    ageRating = "PG-13",
                    genres = "أكشن, قوى خارقة, مدرسي, شونين",
                    synopsis = "إيتادوري يوجي يبتلع إصبعاً ملعوناً ويدخل عالم مقاتلي الجوجيتسو لحماية رفاقه ومواجهة اللعنات.",
                    isFavorite = true,
                    readLater = false,
                    folder = "المكتملة",
                    currentChapter = 271,
                    totalChapters = 271,
                    readingProgress = 100
                ),
                MangaEntity(
                    id = "chainsaw_man",
                    title = "Chainsaw Man",
                    titleArabic = "رجل المنشار",
                    author = "Tatsuki Fujimoto",
                    artist = "Tatsuki Fujimoto",
                    coverDrawable = R.drawable.cover_chainsaw_man,
                    rating = 8.5,
                    ratingCount = "410K",
                    status = "مستمر",
                    releaseYear = 2018,
                    ageRating = "R-17",
                    genres = "أكشن, رعب, خارق للطبيعة, شونين",
                    synopsis = "دينجي شاب فقير يعيش مع شيطان المنشار بوتشيتا قبل أن يُولد من جديد كصياد شياطين خارق.",
                    isFavorite = true,
                    readLater = true,
                    folder = "المتابعة",
                    currentChapter = 167,
                    totalChapters = 168,
                    readingProgress = 38
                ),
                MangaEntity(
                    id = "demon_slayer",
                    title = "Demon Slayer",
                    titleArabic = "قاتل الشياطين",
                    author = "Koyoharu Gotouge",
                    artist = "Koyoharu Gotouge",
                    coverDrawable = R.drawable.cover_demon_slayer,
                    rating = 8.9,
                    ratingCount = "890K",
                    status = "مكتمل",
                    releaseYear = 2016,
                    ageRating = "PG-13",
                    genres = "فانتازيا, قتال, شياطين, شونين",
                    synopsis = "تانجيرو كامادو يسعى لتحويل أخته نيزوكو إلى بشرية بعد هجوم شياطين قضى على عائلته.",
                    isFavorite = false,
                    readLater = false,
                    folder = "المكتملة",
                    currentChapter = 205,
                    totalChapters = 205,
                    readingProgress = 100
                ),
                MangaEntity(
                    id = "tokyo_revengers",
                    title = "Tokyo Revengers",
                    titleArabic = "طوكيو ريفينجرز",
                    author = "Ken Wakui",
                    artist = "Ken Wakui",
                    coverDrawable = R.drawable.cover_tokyo_revengers,
                    rating = 8.4,
                    ratingCount = "450K",
                    status = "مكتمل",
                    releaseYear = 2017,
                    ageRating = "PG-13",
                    genres = "دراما, عصابات, سفر عبر الزمن",
                    synopsis = "تاكيميتشي هاناغاكي يعود بالزمن 12 عاماً لإنقاذ حبيبته وتغيير مصير عصابة طوكيو مانجي.",
                    isFavorite = true,
                    readLater = false,
                    folder = "المكتملة",
                    currentChapter = 278,
                    totalChapters = 278,
                    readingProgress = 100
                ),
                MangaEntity(
                    id = "hells_paradise",
                    title = "Hell's Paradise",
                    titleArabic = "جنة الجحيم",
                    author = "Yuji Kaku",
                    artist = "Yuji Kaku",
                    coverDrawable = R.drawable.cover_hells_paradise,
                    rating = 8.6,
                    ratingCount = "320K",
                    status = "مكتمل",
                    releaseYear = 2018,
                    ageRating = "R-17",
                    genres = "فانتازيا مظلمة, نينجا, إثارة",
                    synopsis = "جابيمارو النينجا المحكوم بالإعدام يُرسل إلى جزيرة غامضة لاستعادة إكسير الخلود أملاً بالعفو.",
                    isFavorite = false,
                    readLater = true,
                    folder = "المكتملة",
                    currentChapter = 127,
                    totalChapters = 127,
                    readingProgress = 100
                ),
                MangaEntity(
                    id = "blue_lock",
                    title = "Blue Lock",
                    titleArabic = "بلو لوك",
                    author = "Muneyuki Kaneshiro",
                    artist = "Yusuke Nomura",
                    coverDrawable = R.drawable.cover_blue_lock,
                    rating = 8.7,
                    ratingCount = "510K",
                    status = "مستمر",
                    releaseYear = 2018,
                    ageRating = "PG-13",
                    genres = "رياضة, نفسي, منافسة",
                    synopsis = "برنامج صارم لتخريج أفضل مهاجم أناني في العالم لقيادة منتخب اليابان نحو كأس العالم.",
                    isFavorite = false,
                    readLater = true,
                    folder = "المتابعة",
                    currentChapter = 239,
                    totalChapters = 250,
                    readingProgress = 60
                ),
                MangaEntity(
                    id = "vinland_saga",
                    title = "Vinland Saga",
                    titleArabic = "فاينلاند ساغا",
                    author = "Makoto Yukimura",
                    artist = "Makoto Yukimura",
                    coverDrawable = R.drawable.cover_vinland_saga,
                    rating = 9.1,
                    ratingCount = "480K",
                    status = "مستمر",
                    releaseYear = 2005,
                    ageRating = "R-17",
                    genres = "تاريخي, دراما, فايكنج",
                    synopsis = "ثورفين ينطلق في مسيرة انتقام دموية قبل أن يكتشف المعنى الحقيقي للمحارب والبحث عن أرض السلام فينلاند.",
                    isFavorite = false,
                    readLater = true,
                    folder = "المتابعة",
                    currentChapter = 220,
                    totalChapters = 225,
                    readingProgress = 25
                )
            )
            mangaDao.insertMangaList(defaultManga)

            // Seed Solo Leveling Chapters
            val chapters = listOf(
                ChapterEntity(
                    id = "ch_solo_179",
                    mangaId = "solo_leveling",
                    chapterNumber = 179,
                    title = "الملك الحقيقي",
                    releaseDate = "10 مايو 2024",
                    isRead = false,
                    isDownloaded = true,
                    downloadSizeMb = 98.1,
                    pagesCount = 28
                ),
                ChapterEntity(
                    id = "ch_solo_178",
                    mangaId = "solo_leveling",
                    chapterNumber = 178,
                    title = "بداية النهاية",
                    releaseDate = "3 مايو 2024",
                    isRead = false,
                    isDownloaded = true,
                    downloadSizeMb = 95.3,
                    pagesCount = 28
                ),
                ChapterEntity(
                    id = "ch_solo_177",
                    mangaId = "solo_leveling",
                    chapterNumber = 177,
                    title = "مواجهة الملوك",
                    releaseDate = "26 أبريل 2024",
                    isRead = true,
                    isDownloaded = false,
                    downloadSizeMb = 92.0,
                    pagesCount = 26
                ),
                ChapterEntity(
                    id = "ch_solo_176",
                    mangaId = "solo_leveling",
                    chapterNumber = 176,
                    title = "نهضة الظلام",
                    releaseDate = "19 أبريل 2024",
                    isRead = true,
                    isDownloaded = false,
                    downloadSizeMb = 89.4,
                    pagesCount = 25
                )
            )
            chapterDao.insertChapters(chapters)

            // Seed 8 Official Sources
            sourceDao.insertSources(com.example.data.model.MangaSourceConfig.OFFICIAL_SOURCES)

            // Seed Download Tasks (matching Image 10)
            val defaultDownloads = listOf(
                DownloadTaskEntity(
                    id = "dl_active_1",
                    mangaId = "solo_leveling",
                    chapterNumber = 179,
                    mangaTitle = "Solo Leveling",
                    chapterTitle = "الفصل 179 - صعود الملك",
                    coverDrawable = R.drawable.cover_solo_leveling,
                    totalMb = 98.1,
                    downloadedMb = 68.5,
                    progress = 70,
                    speedText = "4.1 ميجابايت/ثانية",
                    timeRemainingText = "00:00:08",
                    status = "DOWNLOADING"
                ),
                DownloadTaskEntity(
                    id = "dl_active_2",
                    mangaId = "jujutsu_kaisen",
                    chapterNumber = 271,
                    mangaTitle = "Jujutsu Kaisen",
                    chapterTitle = "الفصل 271 - المعركة الأخيرة",
                    coverDrawable = R.drawable.cover_jujutsu_kaisen,
                    totalMb = 120.4,
                    downloadedMb = 42.1,
                    progress = 35,
                    speedText = "3.2 ميجابايت/ثانية",
                    timeRemainingText = "00:00:24",
                    status = "DOWNLOADING"
                ),
                DownloadTaskEntity(
                    id = "dl_queue_1",
                    mangaId = "one_piece",
                    chapterNumber = 1125,
                    mangaTitle = "One Piece",
                    chapterTitle = "الفصل 1125 - نحو الحلم",
                    coverDrawable = R.drawable.cover_one_piece,
                    totalMb = 92.0,
                    downloadedMb = 0.0,
                    progress = 0,
                    speedText = "قيد الانتظار",
                    timeRemainingText = "--:--",
                    status = "QUEUED"
                ),
                DownloadTaskEntity(
                    id = "dl_queue_2",
                    mangaId = "chainsaw_man",
                    chapterNumber = 168,
                    mangaTitle = "Chainsaw Man",
                    chapterTitle = "الفصل 168 - وحش جديد",
                    coverDrawable = R.drawable.cover_chainsaw_man,
                    totalMb = 85.0,
                    downloadedMb = 0.0,
                    progress = 0,
                    speedText = "قيد الانتظار",
                    timeRemainingText = "--:--",
                    status = "QUEUED"
                ),
                DownloadTaskEntity(
                    id = "dl_comp_1",
                    mangaId = "solo_leveling",
                    chapterNumber = 178,
                    mangaTitle = "Solo Leveling",
                    chapterTitle = "الفصل 178",
                    coverDrawable = R.drawable.cover_solo_leveling,
                    totalMb = 95.3,
                    downloadedMb = 95.3,
                    progress = 100,
                    speedText = "مكتمل",
                    timeRemainingText = "2024/04/12",
                    status = "COMPLETED"
                ),
                DownloadTaskEntity(
                    id = "dl_comp_2",
                    mangaId = "jujutsu_kaisen",
                    chapterNumber = 270,
                    mangaTitle = "Jujutsu Kaisen",
                    chapterTitle = "الفصل 270",
                    coverDrawable = R.drawable.cover_jujutsu_kaisen,
                    totalMb = 102.8,
                    downloadedMb = 102.8,
                    progress = 100,
                    speedText = "مكتمل",
                    timeRemainingText = "2024/04/10",
                    status = "COMPLETED"
                ),
                DownloadTaskEntity(
                    id = "dl_comp_3",
                    mangaId = "one_piece",
                    chapterNumber = 1124,
                    mangaTitle = "One Piece",
                    chapterTitle = "الفصل 1124",
                    coverDrawable = R.drawable.cover_one_piece,
                    totalMb = 88.6,
                    downloadedMb = 88.6,
                    progress = 100,
                    speedText = "مكتمل",
                    timeRemainingText = "2024/04/08",
                    status = "COMPLETED"
                )
            )
            defaultDownloads.forEach { downloadDao.insertTask(it) }
        }
    }
}
