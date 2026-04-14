package org.example.project.tutor.repo

import org.example.project.tutor.models.domainModels.file.PickedFile

interface IPdfPicker {
    suspend fun pickPdf(): PickedFile?
}
