package org.example.project.pdfChunker

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import kotlin.math.min
object PdfChunker {

    fun splitPdfIntoChunks(
        input: File,
        outDir: File,
        pagesPerChunk: Int = 12,
        overlapPages: Int = 1
    ): List<File> {
        require(pagesPerChunk in 1..200) { "pagesPerChunk must be reasonable" }
        require(overlapPages in 0..minOf(3, pagesPerChunk - 1)) { "overlapPages out of range" }

        outDir.mkdirs()

        PDDocument.load(input).use { doc ->
            val total = doc.numberOfPages
            val outFiles = mutableListOf<File>()

            var start = 0
            var idx = 1

            while (start < total) {
                val endExclusive = minOf(start + pagesPerChunk, total)

                PDDocument().use { part ->
                    for (p in start until endExclusive) {
                        part.addPage(doc.getPage(p))
                    }
                    val f = File(outDir, "chunk_${idx.toString().padStart(3, '0')}.pdf")
                    part.save(f)
                    outFiles.add(f)
                }

                val nextStart = endExclusive - overlapPages
                start = if (nextStart > start) nextStart else endExclusive
                idx++
            }

            return outFiles
        }
    }

    data class TocEntry(val title: String, val logicalPage: Int)

    fun splitPdfByTocChapters(
        input: File,
        outDir: File,
        tocStartPage: Int = 1,
        tocEndPageInclusive: Int = 6,
        pageNumberOffset: Int = 0,
        overlapPages: Int = 1,
        fallbackPagesPerChunk: Int = 12,
    ): List<File> {
        require(tocStartPage >= 1) { "tocStartPage must be >= 1" }
        require(tocEndPageInclusive >= tocStartPage) { "tocEndPageInclusive must be >= tocStartPage" }
        require(overlapPages in 0..3) { "overlapPages out of range" }

        outDir.mkdirs()

        PDDocument.load(input).use { doc ->
            val totalPages = doc.numberOfPages
            val tocText = extractTextRange(doc, tocStartPage, min(tocEndPageInclusive, totalPages))

            val entries = parseGreekChapterToc(tocText)
                .distinctBy { it.logicalPage }
                .sortedBy { it.logicalPage }

            if (entries.isEmpty()) {
                return splitPdfIntoChunks(input, outDir, fallbackPagesPerChunk, overlapPages)
            }

            val starts0 = entries.mapNotNull { e ->
                val pdfStart1Based = e.logicalPage + pageNumberOffset
                val idx0 = pdfStart1Based - 1
                if (idx0 in 0 until totalPages) idx0 else null
            }.distinct().sorted()

            if (starts0.isEmpty()) {
                return splitPdfIntoChunks(input, outDir, fallbackPagesPerChunk, overlapPages)
            }

            val ranges = mutableListOf<Pair<Int, Int>>()
            for (i in starts0.indices) {
                val start = starts0[i]
                val endExclusive = if (i == starts0.lastIndex) totalPages else starts0[i + 1]
                if (endExclusive > start) ranges.add(start to endExclusive)
            }

            val outFiles = mutableListOf<File>()
            var idx = 1

            for (ri in ranges.indices) {
                val (start0, endEx0) = ranges[ri]

                val actualStart0 = start0
                val actualEndEx0 = endEx0

                PDDocument().use { part ->
                    for (p in actualStart0 until actualEndEx0) {
                        part.addPage(doc.getPage(p))
                    }
                    val safeName = idx.toString().padStart(3, '0')
                    val f = File(outDir, "chapter_${safeName}.pdf")
                    part.save(f)
                    outFiles.add(f)
                }

                if (overlapPages > 0 && ri < ranges.lastIndex) {
                    val next = ranges[ri + 1]
                    val shiftedStart = (next.first - overlapPages).coerceAtLeast(0)
                    ranges[ri + 1] = shiftedStart to next.second
                }

                idx++
            }

            return outFiles
        }
    }

    private fun extractTextRange(doc: PDDocument, startPage1Based: Int, endPage1Based: Int): String {
        val stripper = PDFTextStripper()
        stripper.startPage = startPage1Based
        stripper.endPage = endPage1Based
        return stripper.getText(doc)
    }

    private fun parseGreekChapterToc(tocText: String): List<TocEntry> {
        val lines = tocText.split('\n').map { it.trim() }.filter { it.isNotBlank() }
        val entries = mutableListOf<TocEntry>()

        val chapterWord = Regex("""\b(Κεφάλαιο|ΚΕΦΑΛΑΙΟ)\b""")
        val lastInt = Regex("""(\d{1,4})\s*$""")

        for (ln in lines) {
            if (!chapterWord.containsMatchIn(ln)) continue
            val m = lastInt.find(ln) ?: continue
            val page = m.groupValues[1].toIntOrNull() ?: continue
            val title = ln.removeRange(m.range).trim().trimEnd('.', '-', '·', '•')
            entries.add(TocEntry(title = title, logicalPage = page))
        }

        return entries
    }
}
