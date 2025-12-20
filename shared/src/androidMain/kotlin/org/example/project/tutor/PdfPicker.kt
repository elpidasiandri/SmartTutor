package org.example.project.tutor

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.tutor.domainModels.file.PickedFile
import org.example.project.tutor.repo.IPdfPicker
import kotlin.coroutines.resume

class AndroidPdfPicker(
    private val fragment: Fragment
) : IPdfPicker {

    private val launcher = fragment.registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        continuation?.resume(uriToPickedFile(uri))
    }

    private var continuation: CancellableContinuation<PickedFile?>? = null

    override suspend fun pickPdf(): PickedFile? = suspendCancellableCoroutine { cont ->
        continuation = cont
        launcher.launch(arrayOf("application/pdf"))
    }

    private fun uriToPickedFile(uri: Uri?): PickedFile? {
        if (uri == null) return null
        val resolver = fragment.requireContext().contentResolver
        val mimeType = resolver.getType(uri) ?: ""
        val bytes = resolver.openInputStream(uri)?.readBytes() ?: return null
        return PickedFile(
            name = uri.lastPathSegment ?: "file.pdf",
            bytes = bytes,
            sizeBytes = bytes.size.toLong(),
            mimeType = mimeType
        )
    }
}
