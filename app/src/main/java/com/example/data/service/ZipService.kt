package com.example.data.service

import java.io.File
import java.util.zip.ZipFile

object ZipService {
    private const val MAX_ENTRIES = 10000
    private const val MAX_BYTES = 1024L * 1024 * 1024

    fun entries(archive: File): List<String> = ZipFile(archive).use { zip ->
        val entries = zip.entries().toList()
        require(entries.size <= MAX_ENTRIES) { "Arşivde çok fazla dosya var" }
        entries.map { it.name }
    }

    private fun safeTarget(root: File, name: String): File {
        require(!name.startsWith('/') && !name.contains('\\')) { "Geçersiz arşiv yolu" }
        val target = File(root, name).canonicalFile
        require(target.path.startsWith(root.canonicalPath + File.separator)) { "Geçersiz arşiv yolu" }
        return target
    }

    fun extract(archive: File, destination: File, only: String? = null): File {
        require(!destination.exists()) { "Hedef zaten var" }
        require(destination.mkdirs()) { "Hedef klasör oluşturulamadı" }
        try {
            var total = 0L
            var count = 0
            var selected: File? = null
            ZipFile(archive).use { zip ->
                val all = zip.entries()
                while (all.hasMoreElements()) {
                    val entry = all.nextElement()
                    require(++count <= MAX_ENTRIES) { "Arşivde çok fazla dosya var" }
                    if (only != null && entry.name != only) continue
                    val target = safeTarget(destination, entry.name)
                    if (entry.isDirectory) { require(target.mkdirs() || target.isDirectory); continue }
                    require(target.parentFile?.let { it.mkdirs() || it.isDirectory } == true)
                    require(!target.exists()) { "Arşivde aynı isim birden fazla kez var" }
                    zip.getInputStream(entry).use { input ->
                        target.outputStream().use { output ->
                            val buffer = ByteArray(32768)
                            while (true) {
                                val n = input.read(buffer)
                                if (n < 0) break
                                total += n
                                require(total <= MAX_BYTES) { "Arşiv çıkarma boyutu sınırı aşıldı" }
                                output.write(buffer, 0, n)
                            }
                        }
                    }
                    selected = target
                }
            }
            if (only != null) return selected ?: error("Dosya bulunamadı")
            return destination
        } catch (e: Exception) {
            destination.deleteRecursively()
            throw e
        }
    }
}
