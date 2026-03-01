package org.example.project.repo

import com.google.cloud.Timestamp
import com.google.cloud.firestore.Firestore
import kotlinx.serialization.json.Json
import org.example.project.tutor.networkModel.uploadPdf.JobStatus
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse

class FirestorePdfJobRepository(private val db: Firestore) : PdfJobRepository {

    private val col = db.collection("pdf_jobs")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    override fun createAndEnqueue(
        documentId: String,
        fileName: String,
        language: String,
        storagePath: String
    ) {
        val now = Timestamp.now()

        col.document(documentId).set(
            mapOf(
                "documentId" to documentId,
                "fileName" to fileName,
                "storagePath" to storagePath,
                "language" to language,

                "status" to JobStatus.Queued.value,
                "progress" to 0,
                "error" to null,

                "totalUnits" to 0,
                "lockedBy" to null,
                "lockUntil" to null,

                "partialResultJson" to null,
                "resultJson" to null,
                "lastLlmRaw" to null,

                "createdAt" to now,
                "updatedAt" to now,
            )
        ).get()

        db.collection("pdfs").document(documentId)
            .set(
                mapOf(
                    "documentId" to documentId,
                    "fileName" to fileName,
                    "status" to JobStatus.Queued.value,
                    "progress" to 0,
                    "createdAt" to now,
                    "updatedAt" to now,
                )
            ).get()
    }

    override fun claimNextJob(workerId: String): PdfJob? {
        val now = Timestamp.now()
        val lockSeconds = 120L
        val lockUntil = Timestamp.ofTimeSecondsAndNanos(now.seconds + lockSeconds, 0)

        val snap = col.whereIn("status", listOf(JobStatus.Queued.value, JobStatus.Processing.value))
            .limit(20)
            .get()
            .get()

        val candidates = snap.documents.mapNotNull { doc ->
            val status = doc.getString("status") ?: return@mapNotNull null
            val lockUntilTs = safeTimestamp(doc, "lockUntil")
            val expired = lockUntilTs == null || lockUntilTs.seconds <= now.seconds

            val claimable =
                (status == JobStatus.Queued.value) ||
                        (status == JobStatus.Processing.value && expired)

            if (!claimable) return@mapNotNull null

            PdfJob(
                documentId = doc.id,
                fileName = doc.getString("fileName") ?: "",
                language = doc.getString("language") ?: "el",
                storagePath = doc.getString("storagePath") ?: ""
            )
        }.sortedBy { it.documentId }

        val chosen = candidates.firstOrNull() ?: return null
        val docRef = col.document(chosen.documentId)

        val ok = db.runTransaction { tx ->
            val current = tx.get(docRef).get()
            val status = current.getString("status") ?: return@runTransaction false
            val lockUntilCurrent = safeTimestamp(current, "lockUntil")
            val expired = lockUntilCurrent == null || lockUntilCurrent.seconds <= now.seconds

            val canClaim =
                (status == JobStatus.Queued.value) || (status == JobStatus.Processing.value && expired)
            if (!canClaim) return@runTransaction false

            tx.update(
                docRef,
                mapOf(
                    "status" to JobStatus.Processing.value,
                    "lockedBy" to workerId,
                    "lockUntil" to lockUntil,
                    "updatedAt" to Timestamp.now(),
                )
            )
            true
        }.get()

        return if (ok) chosen else null
    }

    override fun heartbeat(documentId: String, workerId: String) {
        val now = Timestamp.now()
        val lockSeconds = 120L
        val lockUntil = Timestamp.ofTimeSecondsAndNanos(now.seconds + lockSeconds, 0)

        col.document(documentId).update(
            mapOf(
                "lockedBy" to workerId,
                "lockUntil" to lockUntil,
                "updatedAt" to now
            )
        ).get()

        mirrorToUserIndex(documentId, now, status = null, progress = null)
    }

    override fun setProgress(documentId: String, progress: Int) {
        val now = Timestamp.now()
        val p = progress.coerceIn(0, 100)

        col.document(documentId).update(
            mapOf(
                "progress" to p,
                "updatedAt" to now
            )
        ).get()

        mirrorToUserIndex(documentId, now, status = null, progress = p)
    }

    override fun savePartialResult(documentId: String, partial: LearningUnitsPrototypeDto, progress: Int) {
        val now = Timestamp.now()
        val p = progress.coerceIn(0, 99)
        val partialJson = json.encodeToString(LearningUnitsPrototypeDto.serializer(), partial)

        col.document(documentId).update(
            mapOf(
                "status" to JobStatus.Processing.value,
                "progress" to p,
                "partialResultJson" to partialJson,
                "partialTotalUnits" to (partial.totalUnits ?: partial.learningUnits.size),
                "updatedAt" to now
            )
        ).get()

        mirrorToUserIndex(documentId, now, status = JobStatus.Processing.value, progress = p)
    }

    override fun completeSuccess(documentId: String, result: LearningUnitsPrototypeDto) {
        val now = Timestamp.now()
        val resultJson = json.encodeToString(LearningUnitsPrototypeDto.serializer(), result)

        col.document(documentId).update(
            mapOf(
                "status" to JobStatus.Done.value,
                "progress" to 100,
                "error" to null,
                "language" to result.language,
                "totalUnits" to (result.totalUnits ?: result.learningUnits.size),
                "resultJson" to resultJson,
                "updatedAt" to now
            )
        ).get()

        mirrorToUserIndex(documentId, now, status = JobStatus.Done.value, progress = 100)
    }

    override fun completeError(documentId: String, message: String) {
        val now = Timestamp.now()

        col.document(documentId).update(
            mapOf(
                "status" to JobStatus.Failed.value,
                "error" to message.take(2000),
                "updatedAt" to now
            )
        ).get()

        mirrorToUserIndex(documentId, now, status = JobStatus.Failed.value, progress = null)
    }

    override fun saveLastLlmRaw(documentId: String, raw: String) {
        col.document(documentId).update(
            mapOf(
                "lastLlmRaw" to raw.take(200_000),
                "updatedAt" to Timestamp.now()
            )
        ).get()
    }

    override fun getLastLlmRaw(documentId: String): String? {
        val doc = col.document(documentId).get().get()
        if (!doc.exists()) return null
        return doc.getString("lastLlmRaw")
    }

    override fun getStatus(documentId: String): PdfJobStatusResponse? {
        val doc = col.document(documentId).get().get()
        if (!doc.exists()) return null

        val status = doc.getString("status") ?: "unknown"
        val progress = (doc.getLong("progress") ?: 0L).toInt()
        val error = doc.getString("error")

        val partialJson = doc.getString("partialResultJson")
        val resultJson = doc.getString("resultJson")

        val partial = if (!partialJson.isNullOrBlank()) {
            runCatching { json.decodeFromString(LearningUnitsPrototypeDto.serializer(), partialJson) }.getOrNull()
        } else null

        val result = if (!resultJson.isNullOrBlank()) {
            runCatching { json.decodeFromString(LearningUnitsPrototypeDto.serializer(), resultJson) }.getOrNull()
        } else null

        return PdfJobStatusResponse(
            documentId = doc.id,
            status = status,
            progress = progress,
            error = error,
            partialResult = partial,
            result = result
        )
    }

    private fun mirrorToUserIndex(
        documentId: String,
        now: Timestamp,
        status: String?,
        progress: Int?
    ) {
        val doc = col.document(documentId).get().get()
        if (!doc.exists()) return

        val updates = mutableMapOf<String, Any>("updatedAt" to now)
        if (status != null) updates["status"] = status
        if (progress != null) updates["progress"] = progress

        db.collection("pdfs").document(documentId)
            .update(updates)
            .get()
    }

    private fun safeTimestamp(
        doc: com.google.cloud.firestore.DocumentSnapshot,
        field: String
    ): Timestamp? {
        return try {
            doc.getTimestamp(field)
        } catch (_: Throwable) {
            null
        }
    }
}
