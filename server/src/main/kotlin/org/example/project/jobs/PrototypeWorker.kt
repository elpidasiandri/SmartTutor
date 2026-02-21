package org.example.project.jobs

import kotlinx.coroutines.*
import org.example.project.pdf.PromptLlmClient
import java.io.File
import java.util.UUID

//
//class PrototypeBuildWorker(
//    private val repo: PrototypeRepository,
//    private val llm: PromptLlmClient,
//) {
//    private val workerId = "proto_worker_" + UUID.randomUUID().toString().take(8)
//
//    fun start(scope: CoroutineScope) {
//        scope.launch(Dispatchers.IO) {
//            while (isActive) {
//                try {
//                    // simplest: scan for queued prototypes
//                    val next = findNextQueuedPrototypeId() // implement simple query in repo if you want
//                    if (next == null) {
//                        delay(800)
//                        continue
//                    }
//                    buildPrototype(next)
//                } catch (t: Throwable) {
//                    delay(1000)
//                }
//            }
//        }
//    }
//
//    private fun findNextQueuedPrototypeId(): String? {
//        // TODO: implement in repo: query prototypes where status in (queued)
//        return null
//    }
//
//    private suspend fun buildPrototype(protoId: String) {
//        try {
//            repo.setStatus(protoId, status = "processing", progress = 1)
//
//            val (pdfFile, language, fileName) = when (protoId) {
//                "java_gr" -> Triple(File("assets/java_gr.pdf"), "el", "java_gr.pdf")
//                "java_en" -> Triple(File("assets/java_en.pdf"), "en", "java_en.pdf")
//                else -> error("Unknown protoId=$protoId")
//            }
//            if (!pdfFile.exists()) error("Missing pdf at ${pdfFile.path}")
//
//            // 1) outline
//            val outline = llm.generateOutlineFromPdfSmart(
//                pdfFile = pdfFile,
//                language = language,
//                fileName = fileName
//            ) { p ->
//                repo.setStatus(protoId, "processing", progress = (p * 10 / 100).coerceIn(1, 10))
//            }
//
//            val items = outline.items.take(14)
//            repo.setStatus(protoId, "processing", progress = 10, totalUnits = items.size, builtUnits = 0)
//
//            // 2) units one-by-one (so never max tokens)
//            var built = 0
//            for ((idx, item) in items.withIndex()) {
//                val unitNumber = idx + 1
//
//                val unitOnly = llm.generateSingleUnitFromOutlineItem(
//                    outlineItem = item,
//                    language = language,
//                    fileName = fileName
//                ) { }
//
//                val exercises = llm.generateExercisesForUnit(
//                    unit = unitOnly,
//                    language = language,
//                    fileName = fileName,
//                    targetMinutes = 55
//                ) { }
//
//                var unit = unitOnly.copy(
//                    numbering = unitNumber,
//                    exercises = exercises
//                )
//
//                // 3) image generation (if prompt exists)
//                val img0 = unit.images.firstOrNull()
//                if (img0?.prompt?.isNotBlank() == true) {
//                    val relPath = llm.generateAndSaveImage(
//                        documentId = protoId,
//                        unitNumber = unitNumber,
//                        imageIndex = 1,
//                        prompt = img0.prompt!!
//                    )
//                    unit = unit.copy(
//                        images = listOf(img0.copy(relativePath = relPath))
//                    )
//                }
//
//                // 4) save unit incrementally
//                repo.upsertUnit(protoId, unitNumber, unit)
//
//                built++
//                val prog = 10 + (built * 85 / items.size) // 10..95
//                repo.setStatus(protoId, "processing", progress = prog, builtUnits = built)
//            }
//
//            repo.setStatus(protoId, "done", progress = 100, builtUnits = built)
//
//        } catch (t: Throwable) {
//            repo.setError(protoId, t.message ?: "Unknown error")
//        }
//    }
//}
