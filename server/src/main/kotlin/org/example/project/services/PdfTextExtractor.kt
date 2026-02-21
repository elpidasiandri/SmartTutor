package org.example.project.services

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

class PdfTextExtractor {
    fun extractText(pdf: File): String {
        PDDocument.load(pdf).use { doc ->
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            return stripper.getText(doc).orEmpty()
        }
    }
}
