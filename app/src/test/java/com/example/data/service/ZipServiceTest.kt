package com.example.data.service

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipServiceTest {
    @get:Rule val temp = TemporaryFolder()
    private fun archive(name: String, contents: String): File {
        val file = temp.newFile()
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry(name))
            zip.write(contents.toByteArray())
            zip.closeEntry()
        }
        return file
    }
    @Test fun extractsAndListsNestedFiles() {
        val zip = archive("album/song.txt", "hello")
        assertEquals(listOf("album/song.txt"), ZipService.entries(zip))
        val result = ZipService.extract(zip, File(temp.root, "out"))
        assertEquals("hello", File(result, "album/song.txt").readText())
    }
    @Test fun rejectsTraversalAndCleansPartialOutput() {
        val zip = archive("../escape.txt", "danger")
        val output = File(temp.root, "out")
        try { ZipService.extract(zip, output); fail("expected rejection") } catch (_: IllegalArgumentException) {}
        assertFalse(output.exists())
        assertFalse(File(temp.root, "escape.txt").exists())
    }
    @Test fun refusesToOverwriteExistingDestination() {
        val zip = archive("file.txt", "new")
        val output = temp.newFolder("exists")
        val keep = File(output, "keep.txt").apply { writeText("old") }
        try { ZipService.extract(zip, output); fail("expected rejection") } catch (_: IllegalArgumentException) {}
        assertEquals("old", keep.readText())
    }
}
