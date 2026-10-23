package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.AppColorTheme
import com.example.data.model.ReadingDirection
import com.example.data.repository.MangaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SubScreen {
    object Main : SubScreen()
    data class MangaDetail(val mangaId: String) : SubScreen()
    object Downloads : SubScreen()
    data class Browser(val url: String = "https://mangalik.net") : SubScreen()
    object Backup : SubScreen()
    data class ReaderFull(val mangaId: String, val chapterNumber: Int, val isOffline: Boolean = false) : SubScreen()
}

class MangaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MangaDatabase.getDatabase(application)
    val repository = MangaRepository(
        mangaDao = database.mangaDao(),
        chapterDao = database.chapterDao(),
        sourceDao = database.sourceDao(),
        downloadDao = database.downloadDao(),
        context = application
    )

    private val _currentTab = MutableStateFlow(com.example.ui.components.NavTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    private val _subScreenStack = MutableStateFlow<List<SubScreen>>(listOf(SubScreen.Main))
    val currentSubScreen = _subScreenStack.map { it.lastOrNull() ?: SubScreen.Main }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubScreen.Main)

    val allManga = repository.allManga.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites = repository.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val readLater = repository.readLater.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSources = repository.allSources.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDownloads = repository.allDownloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme & Settings State
    private val _selectedTheme = MutableStateFlow(AppColorTheme.BLUE)
    val selectedTheme = _selectedTheme.asStateFlow()

    private val _isAdBlockerEnabled = MutableStateFlow(true)
    val isAdBlockerEnabled = _isAdBlockerEnabled.asStateFlow()

    private val _readingDirection = MutableStateFlow(ReadingDirection.VERTICAL)
    val readingDirection = _readingDirection.asStateFlow()

    // Reader & OCR Settings
    private val _ocrEnabled = MutableStateFlow(true)
    val ocrEnabled = _ocrEnabled.asStateFlow()

    private val _arabicTranslationEnabled = MutableStateFlow(true)
    val arabicTranslationEnabled = _arabicTranslationEnabled.asStateFlow()

    private val _showOriginalText = MutableStateFlow(false)
    val showOriginalText = _showOriginalText.asStateFlow()

    private val _dualDisplayEnabled = MutableStateFlow(false)
    val dualDisplayEnabled = _dualDisplayEnabled.asStateFlow()

    private val _replaceBubbleMode = MutableStateFlow(true)
    val replaceBubbleMode = _replaceBubbleMode.asStateFlow()

    private val _fontSizePercent = MutableStateFlow(100)
    val fontSizePercent = _fontSizePercent.asStateFlow()

    private val _backgroundOpacity = MutableStateFlow(0.70f)
    val backgroundOpacity = _backgroundOpacity.asStateFlow()

    // Custom Library Folders
    private val _customFolders = MutableStateFlow(listOf("جميع المجلدات", "المتابعة", "المكتملة", "المفضلة", "قائمة الانتظار"))
    val customFolders = _customFolders.asStateFlow()

    // Toast/Snackbar notifications
    private val _notificationMessage = MutableSharedFlow<String>()
    val notificationMessage = _notificationMessage.asSharedFlow()

    private val downloadSimulationJobs = mutableMapOf<String, Job>()

    fun postNotification(message: String) {
        viewModelScope.launch {
            _notificationMessage.emit(message)
        }
    }

    fun addFolder(folderName: String) {
        if (folderName.isNotBlank() && !_customFolders.value.contains(folderName)) {
            _customFolders.value = _customFolders.value + folderName
            postNotification("تم إنشاء مجلد جديد: $folderName")
        }
    }

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setTab(tab: com.example.ui.components.NavTab) {
        _currentTab.value = tab
        if (_subScreenStack.value.size > 1) {
            _subScreenStack.value = listOf(SubScreen.Main)
        }
    }

    fun navigateTo(subScreen: SubScreen) {
        val current = _subScreenStack.value.toMutableList()
        current.add(subScreen)
        _subScreenStack.value = current
    }

    fun navigateBack(): Boolean {
        val current = _subScreenStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _subScreenStack.value = current
            return true
        }
        return false
    }

    fun toggleFavorite(mangaId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(mangaId, !currentStatus)
            _notificationMessage.emit(if (!currentStatus) "تمت الإضافة إلى المفضلة ❤️" else "تمت الإزالة من المفضلة")
        }
    }

    fun setTheme(theme: AppColorTheme) {
        _selectedTheme.value = theme
    }

    fun toggleAdBlocker() {
        _isAdBlockerEnabled.value = !_isAdBlockerEnabled.value
    }

    fun setReadingDirection(direction: ReadingDirection) {
        _readingDirection.value = direction
    }

    fun toggleOcr() {
        _ocrEnabled.value = !_ocrEnabled.value
    }

    fun toggleArabicTranslation() {
        _arabicTranslationEnabled.value = !_arabicTranslationEnabled.value
    }

    fun toggleShowOriginalText() {
        _showOriginalText.value = !_showOriginalText.value
    }

    fun toggleDualDisplay() {
        _dualDisplayEnabled.value = !_dualDisplayEnabled.value
    }

    fun setReplaceBubbleMode(replace: Boolean) {
        _replaceBubbleMode.value = replace
    }

    fun setFontSizePercent(percent: Int) {
        _fontSizePercent.value = percent
    }

    fun setBackgroundOpacity(opacity: Float) {
        _backgroundOpacity.value = opacity
    }

    fun startDownload(manga: MangaEntity, chapterNumber: Int, chapterTitle: String, sizeMb: Double) {
        val taskId = "dl_${manga.id}_$chapterNumber"
        viewModelScope.launch {
            repository.queueDownload(
                mangaId = manga.id,
                chapterNumber = chapterNumber,
                mangaTitle = manga.title,
                chapterTitle = chapterTitle,
                coverDrawable = manga.coverDrawable,
                sizeMb = sizeMb
            )
            _notificationMessage.emit("بدأ تنزيل ${manga.title} - $chapterTitle")
            simulateDownloadProgress(taskId, manga.id, chapterNumber, sizeMb)
        }
    }

    fun pauseDownload(taskId: String) {
        downloadSimulationJobs[taskId]?.cancel()
        viewModelScope.launch {
            repository.pauseDownload(taskId)
            _notificationMessage.emit("تم إيقاف التنزيل مؤقتاً")
        }
    }

    fun resumeDownload(task: DownloadTaskEntity) {
        viewModelScope.launch {
            repository.resumeDownload(task.id)
            simulateDownloadProgress(task.id, task.mangaId, task.chapterNumber, task.totalMb, startPercent = task.progress)
        }
    }

    fun deleteDownload(task: DownloadTaskEntity) {
        downloadSimulationJobs[task.id]?.cancel()
        viewModelScope.launch {
            repository.deleteDownload(task.id, task.mangaId, task.chapterNumber)
            _notificationMessage.emit("تم حذف الفصل المنزّل بنجاح")
        }
    }

    fun clearCompletedDownloads() {
        viewModelScope.launch {
            repository.clearCompletedDownloads()
            _notificationMessage.emit("تم حذف جميع التنزيلات المكتملة")
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache { bytes ->
                viewModelScope.launch {
                    val mb = String.format("%.1f", bytes / (1024.0 * 1024.0))
                    _notificationMessage.emit("تم تنظيف ذاكرة التخزين المؤقت بنجاح ($mb ميجابايت)")
                }
            }
        }
    }

    fun addCustomSource(name: String, url: String) {
        viewModelScope.launch {
            repository.addCustomSource(name, url)
            _notificationMessage.emit("تمت إضافة المصدر: $name")
        }
    }

    fun deleteSource(sourceId: String) {
        viewModelScope.launch {
            repository.deleteSource(sourceId)
            _notificationMessage.emit("تم حذف المصدر")
        }
    }

    fun resetToOfficialSources() {
        viewModelScope.launch {
            repository.syncOfficialSources()
            _notificationMessage.emit("تمت مزامنة المصادر الرسمية الثمانية وحذف المواقع القديمة ⚡")
        }
    }

    fun testSourceConnection(source: SourceEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanUrl = com.example.data.model.MangaSourceConfig.sanitizeUrl(source.url)
                val urlObj = java.net.URL(cleanUrl)
                val conn = urlObj.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
                conn.requestMethod = "HEAD"
                val code = conn.responseCode
                if (code in 200..399) {
                    onResult(true, "متصل بنجاح (HTTP $code) ✓")
                } else if (code == 403) {
                    onResult(true, "محمي بواسطة Cloudflare (يعمل في المتصفح الداخلي ⚡)")
                } else {
                    onResult(false, "استجابة غير متوقعة: HTTP $code")
                }
            } catch (e: java.net.UnknownHostException) {
                val mirrors = com.example.data.model.MangaSourceConfig.getMirrorsForSource(source.id)
                if (mirrors.size > 1) {
                    onResult(false, "حجب DNS على النطاق الرئيسي (تتوفر مرايا بديلة ⚡)")
                } else {
                    onResult(false, "خطأ في اسم النطاق DNS (ERR_NAME_NOT_RESOLVED)")
                }
            } catch (e: Exception) {
                onResult(false, "فشل الاتصال: ${e.message ?: "مهلة الاتصال"}")
            }
        }
    }

    fun exportSourcesJson(): String {
        val sources = allSources.value
        val array = org.json.JSONArray()
        sources.forEach { s ->
            val obj = org.json.JSONObject()
            obj.put("name", s.name)
            obj.put("url", s.url)
            obj.put("isBuiltIn", s.isBuiltIn)
            array.put(obj)
        }
        postNotification("تم تصدير ${sources.size} مصدر بصيغة JSON!")
        return array.toString(2)
    }

    private fun simulateDownloadProgress(
        taskId: String,
        mangaId: String,
        chapterNumber: Int,
        totalMb: Double,
        startPercent: Int = 0
    ) {
        downloadSimulationJobs[taskId]?.cancel()
        downloadSimulationJobs[taskId] = viewModelScope.launch {
            var currentPercent = startPercent
            while (currentPercent < 100) {
                delay(400)
                currentPercent += 5
                if (currentPercent > 100) currentPercent = 100
                val downloadedMb = (currentPercent / 100.0) * totalMb
                val speed = (3.0 + Math.random() * 2.0)
                val remainingSec = ((totalMb - downloadedMb) / speed).toInt().coerceAtLeast(1)
                val timeStr = String.format("00:00:%02d", remainingSec)

                val status = if (currentPercent >= 100) "COMPLETED" else "DOWNLOADING"
                database.downloadDao().updateTask(
                    DownloadTaskEntity(
                        id = taskId,
                        mangaId = mangaId,
                        chapterNumber = chapterNumber,
                        mangaTitle = if (mangaId == "solo_leveling") "Solo Leveling" else mangaId,
                        chapterTitle = "الفصل $chapterNumber",
                        coverDrawable = com.example.R.drawable.cover_solo_leveling,
                        totalMb = totalMb,
                        downloadedMb = downloadedMb,
                        progress = currentPercent,
                        speedText = if (status == "COMPLETED") "مكتمل" else String.format("%.1f ميجابايت/ثانية", speed),
                        timeRemainingText = if (status == "COMPLETED") "تم الحفظ محلياً" else timeStr,
                        status = status
                    )
                )

                if (status == "COMPLETED") {
                    database.chapterDao().setChapterDownloaded(mangaId, chapterNumber, true)
                    _notificationMessage.emit("اكتمل تنزيل الفصل $chapterNumber وجاهز للقراءة دون إنترنت!")
                }
            }
        }
    }
}
