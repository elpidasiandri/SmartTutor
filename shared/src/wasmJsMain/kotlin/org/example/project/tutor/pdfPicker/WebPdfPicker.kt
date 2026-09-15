package org.example.project.tutor.pdfPicker

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.tutor.models.domainModels.file.PickedFile
import org.example.project.tutor.repo.IPdfPicker
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import kotlin.coroutines.resume

class WebPdfPicker : IPdfPicker {
    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun pickPdf(): PickedFile? = suspendCancellableCoroutine { cont ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "application/pdf"

        input.onchange = {
            val file: File? = input.files?.item(0)

            if (file == null || file.type != "application/pdf") {
                cont.resume(null)
            } else {
                val reader = FileReader()
                reader.onload = {
                    val buffer = reader.result as ArrayBuffer
                    val uint8 = Uint8Array(buffer)

                    val bytes = ByteArray(uint8.length)
                    for (i in 0 until uint8.length) {
                        bytes[i] = uint8[i].toByte()
                    }

                    val pickedFile = PickedFile(
                        name = file.name,
                        bytes = bytes,
                        sizeBytes = file.size.toString().toLong(),
                        mimeType = file.type
                    )

                    cont.resume(pickedFile)
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.click()
    }
    @OptIn(ExperimentalWasmJsInterop::class)
    suspend fun convertFile(file: File): PickedFile? =
        suspendCancellableCoroutine { cont ->

            if (
                file.type != "application/pdf" &&
                !file.name.lowercase().endsWith(".pdf")
            ) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            val reader = FileReader()

            reader.onload = {
                val buffer = reader.result as ArrayBuffer
                val uint8 = Uint8Array(buffer)

                val bytes = ByteArray(uint8.length)

                for (i in 0 until uint8.length) {
                    bytes[i] = uint8[i].toByte()
                }

                cont.resume(
                    PickedFile(
                        name = file.name,
                        bytes = bytes,
                        sizeBytes = file.size.toString().toLong(),
                        mimeType = "application/pdf"
                    )
                )
            }

            reader.onerror = {
                cont.resume(null)
            }

            reader.readAsArrayBuffer(file)
        }
}
