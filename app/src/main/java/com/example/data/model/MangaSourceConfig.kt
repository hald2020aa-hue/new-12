package com.example.data.model

import com.example.data.local.SourceEntity

object MangaSourceConfig {
    /**
     * القائمة الرسمية المعتمدة للمصادر الثمانية في تطبيق Manga ZZ:
     * 1. MangaDar (https://mangadar.com)
     * 2. مانجا ليك — Manga Lik (https://mangalik.net)
     * 3. Azora Manga (https://azorafly.com)
     * 4. مانجاتك — Mangatek (https://mangatek.com)
     * 5. ProComic (https://procomic.pro)
     * 6. مانجا ستارز — Manga Starz (https://starzmanga.com)
     * 7. Team-X (https://olympustaff.com)
     * 8. Swat Manga (https://meshmanga.com)
     */
    val OFFICIAL_SOURCES = listOf(
        SourceEntity(
            id = "src_mangadar",
            name = "MangaDar",
            url = "https://mangadar.com",
            isBuiltIn = true,
            status = "WORKING",
            description = "موقع مانجا ومانهوا مترجمة للعربية"
        ),
        SourceEntity(
            id = "src_mangalik",
            name = "مانجا ليك — Manga Lik",
            url = "https://mangalik.net",
            isBuiltIn = true,
            status = "WORKING",
            description = "أشهر موقع عربي للمانجا والمانهوا المترجمة"
        ),
        SourceEntity(
            id = "src_azora",
            name = "Azora Manga",
            url = "https://azorafly.com",
            isBuiltIn = true,
            status = "WORKING",
            description = "الموقع الرسمي لفريق أزورا مانجا"
        ),
        SourceEntity(
            id = "src_mangatek",
            name = "مانجاتك — Mangatek",
            url = "https://mangatek.com",
            isBuiltIn = true,
            status = "WORKING",
            description = "موقع مانجاتك لقراءة وتنزيل المانجا المترجمة"
        ),
        SourceEntity(
            id = "src_procomic",
            name = "ProComic",
            url = "https://procomic.pro",
            isBuiltIn = true,
            status = "WORKING",
            description = "موقع بروكوميك للمانهوا والكوميكس"
        ),
        SourceEntity(
            id = "src_mangastarz",
            name = "مانجا ستارز — Manga Starz",
            url = "https://starzmanga.com",
            isBuiltIn = true,
            status = "WORKING",
            description = "موقع مانجا ستارز للمانجا الحصرية المترجمة"
        ),
        SourceEntity(
            id = "src_teamx",
            name = "Team-X",
            url = "https://olympustaff.com",
            isBuiltIn = true,
            status = "WORKING",
            description = "موقع فريق تيم إكس للترجمة"
        ),
        SourceEntity(
            id = "src_swat",
            name = "Swat Manga",
            url = "https://meshmanga.com",
            isBuiltIn = true,
            status = "WORKING",
            description = "موقع سوات مانجا للمانهوا الكورية المترجمة"
        )
    )

    val DEFAULT_SOURCE_URL = "https://mangalik.net"
    val DEFAULT_SOURCE_ID = "src_mangalik"

    fun getValidSourceIds(): List<String> = OFFICIAL_SOURCES.map { it.id }
}
