package org.example.project.tutor.mappers.learningUnits

import org.example.project.tutor.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain
import org.example.project.tutor.domainModels.tutorChat.ChatDomain
import org.example.project.tutor.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.domainModels.tutorChat.LUStatusDomain
import org.example.project.tutor.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.domainModels.tutorChat.LearningUnitInstanceDomain
import org.example.project.tutor.domainModels.tutorChat.MaterialDomain
import org.example.project.tutor.domainModels.tutorChat.NextActionDomain
import kotlinx.datetime.Clock
import org.example.project.tutor.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.domainModels.chatHistory.LearningUnitHistoryDomain
import org.example.project.tutor.domainModels.chatHistory.SubjectHistoryDomain
import org.example.project.tutor.domainModels.learningUnits.DskContentDomain
import org.example.project.tutor.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.domainModels.learningUnits.LearningUnitsPageDomain
import org.example.project.tutor.domainModels.learningUnits.QuotationDomain
import org.example.project.tutor.domainModels.learningUnits.UnitImageDomain
import org.example.project.tutor.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.domainModels.tutorChat.SenderType
import org.example.project.tutor.networkModel.learningUnits.DskItemDto
import org.example.project.tutor.networkModel.learningUnits.ExerciseDto
import org.example.project.tutor.networkModel.learningUnits.LanguageOfLearningUnitsEnum
import org.example.project.tutor.networkModel.learningUnits.LearningUnitDto
import org.example.project.tutor.networkModel.learningUnits.LearningUnitsPrototypeDto
import org.example.project.tutor.networkModel.learningUnits.QuotationDto
import org.example.project.tutor.networkModel.learningUnits.UnitImageDto
import org.example.project.utils.nonNull
fun String?.toLanguageEnum(): LanguageOfLearningUnitsEnum =
    when (this?.lowercase()) {
        LanguageOfLearningUnitsEnum.English.value -> LanguageOfLearningUnitsEnum.English
        LanguageOfLearningUnitsEnum.Greek.value -> LanguageOfLearningUnitsEnum.Greek
        else -> LanguageOfLearningUnitsEnum.Greek
    }

fun LearningUnitsPrototypeDto.toDomain(): LearningUnitsPrototypeDomain {
    val units = learningUnits?.mapIndexedNotNull { index, dto ->
        dto?.toDomain(defaultIndex = index + 1)
    }?:emptyList()
    return LearningUnitsPrototypeDomain(
        title = prototypeTitle.orEmpty(),
        language = language.toLanguageEnum(),
        totalUnits = totalUnits ?: units.size,
        learningUnits = units
    )
}
fun LearningUnitDto.toDomain(defaultIndex: Int): LearningUnitContentDomain =
    LearningUnitContentDomain(
        id = id ?: "lu_$defaultIndex",
        title = title.orEmpty(),
        numbering = numbering ?: defaultIndex,

        // Προτιμάς summary για chat:
        text = (text ?: basetext).orEmpty(),

        dsk = domainSpecificKnowledge?.mapNotNull { it?.toDomain() }?:emptyList(),
        images = images?.mapNotNull { it?.toDomain() }?:emptyList(),
        quotations = quotations?.mapNotNull { it?.toDomain() }?:emptyList(),
        exercises = exercises?.mapNotNull { it?.toDomain() }?:emptyList()
    )

fun DskItemDto.toDomain(): DskContentDomain =
    DskContentDomain(
        type = type,
        dsc = content.orEmpty(),
        caption = caption.orEmpty(),
        numbering = numbering ?: 0
    )

fun UnitImageDto.toDomain(): UnitImageDomain =
    UnitImageDomain(
        relativePath = relativePath.orEmpty(),
        caption = caption.orEmpty(),
        numbering = numbering ?: 0
    )

fun QuotationDto.toDomain(): QuotationDomain =
    QuotationDomain(
        imageRelativePath = image?.relativePath.orEmpty(),
        text = text.orEmpty(),
        numbering = numbering ?: 0
    )

fun ExerciseDto.toDomain(): ExerciseDomain =
    ExerciseDomain(
        type = type,
        question = question.orEmpty(),
        options = options,
        answerIndex = answerIndex,
        explanation = explanation.orEmpty(),
        sampleAnswer = sampleAnswer.orEmpty()
    )


fun buildChatHistoryFromUnits(
    units: List<LearningUnitContentDomain>,
    materialId: String,
    unlockedCount: Int,
    subjectTitle: String,
): ChatHistoryDomain {
    val visible = units.take(unlockedCount)

    return ChatHistoryDomain(
        subjects = listOf(
            SubjectHistoryDomain(
                title = subjectTitle.ifBlank { "Material" },
                subjectId = materialId,
                learningUnits = visible.map { lu ->
                    LearningUnitHistoryDomain(
                        luId = lu.id,
                        luTitle = lu.title,
                        materialId = materialId,
                        chatId = "" // later
                    )
                }
            )
        )
    )
}

fun buildChatSessionStart(
    materialId: String,
    fileName: String,
    units: List<LearningUnitContentDomain>,
    activeIndex: Int = 0,
): ChatSessionStartDomain {

    val total = units.size.coerceAtLeast(0)
    val lu = units.getOrNull(activeIndex)
    val ts = Clock.System.now().toEpochMilliseconds()
    val introText = buildString {
        appendLine("📘 ${lu?.title.orEmpty()}")
        appendLine()
        appendLine(lu?.text.orEmpty())

        val dsk = lu?.dsk.orEmpty()
        if (dsk.isNotEmpty()) {
            appendLine()
            appendLine("🔑 Σημεία-κλειδιά:")
            dsk.sortedBy { it.numbering }.forEach { item ->
                appendLine("• ${item.caption}")
            }
        }
    }
    val initialMessages = if (introText.isNotBlank()) {
        listOf(
            ChatMessageDomain(
                id = "m_${lu?.id.orEmpty()}_intro",
                sender = SenderType.AI.name,
                content = introText,
                timestamp = ts,
                type = "TEXT",
                imagePath = lu?.images.nonNull
            )
        )
    } else emptyList()

    return ChatSessionStartDomain(
        material = MaterialDomain(
            materialId = materialId,
            fileUploaded = true,
            fileName = fileName,
            canUpload = total == 0,
            totalUnits = total
        ),
        learningUnit = LearningUnitDomain(
            luId = lu?.id.orEmpty(),
            title = lu?.title.orEmpty(),
            orderIndex = lu?.numbering ?: 0,
            quizAvailable = true,
            analysisCompleted = true,
            quizCompleted = false,
            nextAction = NextActionDomain.CONTINUE_CHAT
        ),
        instance = LearningUnitInstanceDomain(
            instanceId = "instance_${lu?.id.orEmpty()}",
            status = LUStatusDomain.ACTIVE,
            progressPercent = 0,
            startedAt = ts,
            lastUpdatedAt = ts,
            canContinue = true,
            canRestart = false,
            isLocked = false
        ),
        chat = ChatDomain(
            chatSessionId = "chat_${lu?.id.orEmpty()}",
            createNewSession = false,
            messages = initialMessages
        )
    )
}


fun LearningUnitsPrototypeDomain.toChatHistoryDomain(
    materialId: String,
    subjectTitle: String = "My Material",
): ChatHistoryDomain {
    val units = this.learningUnits.map { lu ->
        LearningUnitHistoryDomain(
            luId = "lu_${lu.numbering}",
            luTitle = lu.title,
            materialId = materialId,
            chatId = ""
        )
    }

    val subject = SubjectHistoryDomain(
        title = subjectTitle,
        subjectId = materialId,
        learningUnits = units
    )
    return ChatHistoryDomain(subjects = listOf(subject))
}

fun buildEmptyChatSessionStart(): ChatSessionStartDomain {
    val ts = Clock.System.now().toEpochMilliseconds()
    return ChatSessionStartDomain(
        material = MaterialDomain(
            materialId = "",
            fileUploaded = false,
            fileName = "",
            canUpload = true,
            totalUnits = 0
        ),
        learningUnit = LearningUnitDomain(
            luId = "",
            title = "",
            orderIndex = 0,
            quizAvailable = false,
            analysisCompleted = false,
            quizCompleted = false,
            nextAction = NextActionDomain.NONE
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
            chatSessionId = "chat_empty_$ts",
            createNewSession = true,
            messages = emptyList()
        )
    )
}

