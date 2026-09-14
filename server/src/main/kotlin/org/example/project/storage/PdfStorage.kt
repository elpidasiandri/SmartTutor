package org.example.project.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import java.io.File

class PdfStorage(
    private val storage: Storage,
    private val bucketName: String,
) {

    fun upload(documentId: String, localFile: File, ): String {
        val objectPath = "pdf_uploads/$documentId/source.pdf"

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectPath))
            .setContentType("application/pdf")
            .build()

        storage.createFrom(blobInfo, localFile.toPath())

        return objectPath
    }

    fun downloadToTemp(documentId: String, objectPath: String, ): File {
        val blob = storage.get(
            BlobId.of(bucketName, objectPath)
        ) ?: error(
            "Uploaded PDF not found in Cloud Storage: $objectPath"
        )

        val workDir = File(System.getProperty("java.io.tmpdir"), "smarttutor/$documentId").apply {
            deleteRecursively()
            mkdirs()
        }

        val tempPdf = File(workDir, "source.pdf")

        blob.downloadTo(tempPdf.toPath())

        require(tempPdf.exists() && tempPdf.length() > 0L) {
            "Downloaded PDF is empty: $objectPath"
        }

        return tempPdf
    }
}