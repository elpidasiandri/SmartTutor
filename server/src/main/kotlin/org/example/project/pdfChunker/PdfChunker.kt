package org.example.project.pdfChunker

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File

object PdfChunker {

    /**
     * Split PDF into smaller PDFs.
     * pagesPerChunk: πόσες σελίδες έχει κάθε chunk
     * overlapPages: πόσες σελίδες να επαναλαμβάνονται στο επόμενο chunk (0..3 είναι συνήθως ok)
     */
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

                // move start forward but keep overlap
                val nextStart = endExclusive - overlapPages
                start = if (nextStart > start) nextStart else endExclusive
                idx++
            }

            return outFiles
        }
    }
}
