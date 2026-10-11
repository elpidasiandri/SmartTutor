

package org.example.project.tutor.pdfPicker

import kotlinx.browser.document
import org.w3c.dom.events.Event
import org.w3c.files.File

class WebPdfDropHandler(
    private val onFileDropped: (File) -> Unit,
    private val onDragStateChanged: (Boolean) -> Unit,
    private val onInvalidDrop: (String) -> Unit = {},
) {
    private var isDragging = false

    private fun updateDragState(value: Boolean) {
        if (isDragging == value) return
        isDragging = value
        onDragStateChanged(value)
    }

    private val dragEnterListener: (Event) -> Unit = { event ->
        if (isFileDrag(event)) {
            event.preventDefault()
            updateDragState(true)
        }
    }

    private val dragOverListener: (Event) -> Unit = { event ->
        if (isFileDrag(event)) {
            event.preventDefault()
            updateDragState(true)
        }
    }

    private val dragLeaveListener: (Event) -> Unit = { event ->
        if (isOutsideViewport(event)) {
            updateDragState(false)
        }
    }

    private val dragEndListener: (Event) -> Unit = {
        updateDragState(false)
    }

    private val dropListener: (Event) -> Unit = { event ->
        if (isFileDrag(event)) {
            event.preventDefault()
            updateDragState(false)

            if (droppedFileCount(event) == 1) {
                getDroppedFile(event)?.let(onFileDropped)
            } else {
                onInvalidDrop(
                    "Please drop exactly one PDF file"
                )
            }
        }
    }

    fun start() {
        document.addEventListener("dragenter", dragEnterListener)
        document.addEventListener("dragover", dragOverListener)
        document.addEventListener("dragleave", dragLeaveListener)
        document.addEventListener("dragend", dragEndListener)
        document.addEventListener("drop", dropListener)
    }

    fun stop() {
        document.removeEventListener("dragenter", dragEnterListener)
        document.removeEventListener("dragover", dragOverListener)
        document.removeEventListener("dragleave", dragLeaveListener)
        document.removeEventListener("dragend", dragEndListener)
        document.removeEventListener("drop", dropListener)
        updateDragState(false)
    }
}

private fun getDroppedFile(event: Event): File? =
    droppedFileFromEvent(event)

@JsFun("""
    (event) => {
        const types = event.dataTransfer?.types;
        return types != null &&
            Array.from(types).includes("Files");
    }
""")
private external fun isFileDrag(event: Event): Boolean

@JsFun("""
    (event) => {
        const x = event.clientX;
        const y = event.clientY;
        return x <= 0 || y <= 0 ||
            x >= window.innerWidth ||
            y >= window.innerHeight;
    }
""")
private external fun isOutsideViewport(event: Event): Boolean

@JsFun("""
    (event) => event.dataTransfer?.files?.length ?? 0
""")
private external fun droppedFileCount(event: Event): Int

@JsFun("""
    (event) => {
        const files = event.dataTransfer?.files;
        return files && files.length === 1
            ? files[0]
            : null;
    }
""")
private external fun droppedFileFromEvent(event: Event): File?

