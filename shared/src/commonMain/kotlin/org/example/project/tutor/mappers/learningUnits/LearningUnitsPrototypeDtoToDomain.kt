package org.example.project.tutor.mappers.learningUnits

import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitsPrototypeDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.models.domainModels.tutorChat.LUStatusDomain
import org.example.project.tutor.models.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.models.domainModels.tutorChat.LearningUnitInstanceDomain
import org.example.project.tutor.models.domainModels.tutorChat.MaterialDomain
import kotlinx.datetime.Clock
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseType
import org.example.project.tutor.models.domainModels.learningUnits.QuotationDomain
import org.example.project.tutor.models.domainModels.learningUnits.UnitImageDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.models.domainModels.tutorChat.SenderType
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import org.example.project.tutor.networkModel.uploadPdf.QuotationDto
import org.example.project.tutor.networkModel.uploadPdf.UnitImageDto
import org.example.project.utils.nonNull
import kotlin.uuid.ExperimentalUuidApi

fun String?.toLanguageEnum(): LanguageOfLearningUnitsEnum =
    when (this?.lowercase()) {
        LanguageOfLearningUnitsEnum.English.value -> LanguageOfLearningUnitsEnum.English
        LanguageOfLearningUnitsEnum.Greek.value -> LanguageOfLearningUnitsEnum.Greek
        else -> LanguageOfLearningUnitsEnum.Greek
    }

fun LearningUnitsPrototypeDto.toDomain(): LearningUnitsPrototypeDomain {
    val units = learningUnits.mapIndexed { index, dto ->
        dto.toDomain(defaultIndex = index + 1)
    }
    return LearningUnitsPrototypeDomain(
        title = prototypeTitle.orEmpty(),
        language = language.toLanguageEnum().name,
        totalUnits = totalUnits ?: units.size,
        learningUnits = units
    )
}

fun LearningUnitDto.toDomain(defaultIndex: Int): LearningUnitContentDomain =
    LearningUnitContentDomain(
        title = title.orEmpty(),
        numbering = numbering ?: defaultIndex,
        text = textInfo.orEmpty(),
        images = images.map { it.toDomain() },
        quotations = quotations.map { it.toDomain() },
        exercises = exercises.map { it.toDomain() },
        isRead = isRead ?: false
    )

fun UnitImageDto.toDomain(): UnitImageDomain =
    UnitImageDomain(
        relativePath = relativePath.orEmpty(),
        caption = caption.orEmpty(),
        numbering = numbering ?: 0
    )

fun QuotationDto.toDomain(): QuotationDomain =
    QuotationDomain(
        text = text.orEmpty(),
        numbering = numbering ?: 0
    )

fun ExerciseDto.toDomain(): ExerciseDomain {
    return ExerciseDomain(
        type = type.toExerciseType(),
        difficulty = difficulty.orEmpty(),
        estimatedMinutes = estimatedMinutes ?: 0,
        question = question.orEmpty(),
        options = options,
        answerIndex = answerIndex ?: 0,
        hint = hint.orEmpty(),
        explanation = explanation.orEmpty(),
        solution = solution.orEmpty(),
        points = points.nonNull,
        maxPoints = maxPoints.nonNull,
        awardedPoints = awardedPoints.nonNull,
        gradingStatus = gradingStatus.nonNull,
    )
}


fun String?.toExerciseType(): ExerciseType = when (this?.trim()?.lowercase()) {
    "mcq" -> ExerciseType.MCQ
    "true_false" -> ExerciseType.TRUE_FALSE
    "open", "OPEN", "Open" -> ExerciseType.OPEN
    else -> ExerciseType.NONE
}

fun buildLearningTabMessage(unit: LearningUnitContentDomain): String {
    val sb = StringBuilder()

    sb.append("${unit.numbering}. ${unit.title}\n\n")
    if (unit.text.isNotBlank()) sb.append(unit.text.trim()).append("\n\n")

    if (unit.quotations.isNotEmpty()) {
        sb.append("Quotations\n")
        unit.quotations.sortedBy { it.numbering }.forEach { q ->
            sb.append("[Hot Tip = quotation]\n")
            sb.append(q.text.trim()).append("\n\n")
        }
    }

    if (unit.images.isNotEmpty()) {
        sb.append("Images\n")
        unit.images.sortedBy { it.numbering }.forEach { img ->
            val num = img.relativePath.substringAfter("image_").substringBefore(".png")
                .ifBlank { img.numbering.toString() }

            sb.append("[image $num]\n")
            if (img.caption.isNotBlank()) sb.append(img.caption.trim()).append("\n")
            sb.append("\n")
        }
    }
    return sb.toString().trim()
}
fun buildExercisesTabMessage(
    unit: LearningUnitContentDomain,
    selectedExerciseIndex: Int = 0
): String {
    val sb = StringBuilder()

    sb.append("Exercises — ${unit.numbering}. ${unit.title}\n\n")

    if (unit.exercises.isEmpty()) {
        sb.append("No exercises found for this unit.")
        return sb.toString()
    }

    val safeIndex = selectedExerciseIndex.coerceIn(0, unit.exercises.lastIndex)
    val ex = unit.exercises[safeIndex]

    sb.append("Exercise ${safeIndex + 1}/${unit.exercises.size} (${ex.type}, ${ex.difficulty}, ~${ex.estimatedMinutes} min)\n")
    sb.append(ex.question.trim()).append("\n\n")

    if (ex.options.isNotEmpty()) {
        ex.options.forEachIndexed { idx, opt ->
            val letter = ('A' + idx)
            sb.append("$letter) ").append(opt).append("\n")
        }
        sb.append("\n")
    }

    if (ex.hint.isNotBlank()) sb.append("Hint: ").append(ex.hint.trim()).append("\n")
    if (ex.explanation.isNotBlank()) sb.append("Explanation: ").append(ex.explanation.trim()).append("\n")
    if (ex.solution.isNotBlank()) sb.append("Solution: ").append(ex.solution.trim()).append("\n")

    return sb.toString().trim()
}

@OptIn(ExperimentalUuidApi::class)
fun buildChatSessionStart(
    fileName: String,
    units: List<LearningUnitContentDomain>,
    activeIndex: Int = 0,
): ChatSessionStartDomain {

    val total = units.size.coerceAtLeast(0)
    val lu = units.getOrNull(activeIndex)
    val ts = Clock.System.now().toEpochMilliseconds()

    val introText = buildString {
        appendLine(lu?.title.orEmpty())
        appendLine()
        appendLine(lu?.text.orEmpty())
    }

    val initialMessages = if (introText.isNotBlank()) {
        listOf(
            ChatMessageDomain(
                sender = SenderType.AI.name,
                content = introText,
                timestamp = ts,
                type = "TEXT",
                imagePath = lu?.images.orEmpty(),
                id = ts.toString() + introText.take(10)
            )
        )
    } else emptyList()
    val sessionId = "session_${Clock.System.now().toEpochMilliseconds()}"

    return ChatSessionStartDomain(
        material = MaterialDomain(
            fileUploaded = true,
            fileName = fileName,
            canUpload = true,
            totalUnits = total
        ),
        learningUnit = LearningUnitDomain(
            title = lu?.title.orEmpty(),
            orderIndex = lu?.numbering ?: 0,
            quizAvailable = true,
            analysisCompleted = true,
            quizCompleted = false,
        ),
        instance = LearningUnitInstanceDomain(
            status = LUStatusDomain.ACTIVE,
            progressPercent = 0,
            startedAt = ts,
            lastUpdatedAt = ts,
            canContinue = true,
            canRestart = false,
            isLocked = false
        ),
        chat = ChatDomain(
            sessionId,
            createNewSession = false,
            messages = initialMessages
        )
    )
}

fun buildEmptyChatSessionStart(): ChatSessionStartDomain {
    val ts = Clock.System.now().toEpochMilliseconds()
    val sessionId = "session_${Clock.System.now().toEpochMilliseconds()}"

    return ChatSessionStartDomain(
        material = MaterialDomain(
            fileUploaded = false,
            fileName = "",
            canUpload = true,
            totalUnits = 0
        ),
        learningUnit = LearningUnitDomain(
            title = "",
            orderIndex = 0,
            quizAvailable = false,
            analysisCompleted = false,
            quizCompleted = false,
        ),
        instance = LearningUnitInstanceDomain(
            instanceId = "",
            status = LUStatusDomain.ACTIVE,
            progressPercent = 0,
            startedAt = ts,
            lastUpdatedAt = ts,
            canContinue = false,
            canRestart = false,
            isLocked = false
        ),
        chat = ChatDomain(
            sessionId,
            createNewSession = true,
            messages = emptyList()
        )
    )
}

