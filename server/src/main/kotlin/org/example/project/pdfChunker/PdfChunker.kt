package org.example.project.pdfChunker

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
object PdfChunker {

    fun splitPdfIntoChunks(
        input: File,
        outDir: File,
        pagesPerChunk: Int = 6,
        overlapPages: Int = 0
    ): List<File> {
        require(pagesPerChunk in 1..200) {
            "pagesPerChunk must be reasonable"
        }

        require(
            overlapPages in 0..minOf(
                3,
                pagesPerChunk - 1
            )
        ) {
            "overlapPages out of range"
        }

        outDir.mkdirs()

        PDDocument.load(input).use { doc ->
            val total = doc.numberOfPages
            val outFiles = mutableListOf<File>()

            var start = 0
            var idx = 1

            while (start < total) {
                val endExclusive = minOf(
                    start + pagesPerChunk,
                    total
                )

                PDDocument().use { part ->
                    for (p in start until endExclusive) {
                        part.importPage(
                            doc.getPage(p)
                        )
                    }

                    val file = File(
                        outDir,
                        "chunk_${idx.toString().padStart(3, '0')}.pdf"
                    )

                    part.save(file)
                    outFiles.add(file)
                }

                val nextStart = endExclusive - overlapPages

                start = if (nextStart > start) {
                    nextStart
                } else {
                    endExclusive
                }

                idx++
            }

            return outFiles
        }
    }

    data class TocEntry(val title: String, val logicalPage: Int)

}
