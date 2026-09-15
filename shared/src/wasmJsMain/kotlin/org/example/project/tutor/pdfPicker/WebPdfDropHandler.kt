package org.example.project.tutor.pdfPicker

import kotlinx.browser.document
import org.w3c.dom.events.Event
import org.w3c.files.File

class WebPdfDropHandler(
    private val onFileDropped: (File) -> Unit
) {

    private val dragOverListener: (Event) -> Unit = { event ->
        event.preventDefault()
    }

    private val dropListener: (Event) -> Unit = { event ->
        event.preventDefault()

        val file = getDroppedFile(event)

        if (file != null) {
            onFileDropped(file)
        }
    }

    fun start() {
        document.addEventListener(
            type = "dragover",
            callback = dragOverListener
        )

        document.addEventListener(
            type = "drop",
            callback = dropListener
        )
    }

    fun stop() {
        document.removeEventListener(
            type = "dragover",
            callback = dragOverListener
        )

        document.removeEventListener(
            type = "drop",
            callback = dropListener
        )
    }
}

private fun getDroppedFile(event: Event): File? {
    return droppedFileFromEvent(event)
}

@JsFun(
    """
    (event) => {
        const files = event.dataTransfer?.files;
        if (!files || files.length === 0) {
            return null;
        }
        return files[0];
    }
    """
)
private external fun droppedFileFromEvent(event: Event): File?