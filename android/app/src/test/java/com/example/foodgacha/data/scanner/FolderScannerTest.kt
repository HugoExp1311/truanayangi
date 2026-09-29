package com.example.foodgacha.data.scanner

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FolderScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val scanner = FolderScanner()

    @Test
    fun scan_discoversDeeplyNestedFoldersWithImages() = runBlocking {
        // Setup nested structure:
        // root/
        //   Author1/
        //     MangaA/
        //       01.webp
        //       02.jpg
        //     MangaB/
        //       01.png
        //   Author2/
        //     Category/
        //       SubCategory/
        //         MangaC/
        //           cover.jpeg
        val root = tempFolder.root

        val mangaA = File(root, "Author1/MangaA").apply { mkdirs() }
        File(mangaA, "01.webp").writeText("dummy")
        File(mangaA, "02.jpg").writeText("dummy")
        File(mangaA, "ignore.txt").writeText("dummy")

        val mangaB = File(root, "Author1/MangaB").apply { mkdirs() }
        File(mangaB, "01.png").writeText("dummy")

        val mangaC = File(root, "Author2/Category/SubCategory/MangaC").apply { mkdirs() }
        File(mangaC, "cover.jpeg").writeText("dummy")

        val emptyFolder = File(root, "Author3/Empty").apply { mkdirs() }

        val scanned = scanner.scan(root)

        assertEquals(3, scanned.size)

        val names = scanned.map { it.folderName }.toSet()
        assertTrue(names.contains("MangaA"))
        assertTrue(names.contains("MangaB"))
        assertTrue(names.contains("MangaC"))

        val itemA = scanned.first { it.folderName == "MangaA" }
        assertEquals(2, itemA.images.size)
        assertEquals(listOf("01.webp", "02.jpg"), itemA.images.map { it.name })
    }
}
