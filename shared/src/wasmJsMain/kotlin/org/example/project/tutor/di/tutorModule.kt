package org.example.project.tutor.di

import io.ktor.client.HttpClient
import org.example.project.AppConfig
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.repo.WebProgressRepoImpl
import org.example.project.myProgress.useCases.DeleteChatProgressUseCase
import org.example.project.myProgress.useCases.UpsertChatProgressUseCase
import org.example.project.network.createWebHttpClient
import org.example.project.registration.AuthUserHolder
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseContext
import org.example.project.registration.firebase.initFirebase
import org.example.project.settings.logOut.logoutModule
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.WebAnalyticsTrackerImpl
import org.example.project.tutor.repo.ITutorRepo
import org.example.project.tutor.repo.WebTutorRepoImpl
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.repo.loadResource.ILoadResourceRepo
import org.example.project.tutor.repo.loadResource.LocalLearningUnitsRepositoryImpl
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
import org.example.project.tutor.useCases.PdfUploadServiceUseCase
import org.example.project.tutor.useCases.SaveExerciseAnswerUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase
import org.example.project.tutor.viewModel.TutorController
import org.koin.dsl.module

val tutorModule = module {
    single<HttpClient> { createWebHttpClient() }
    single { AppConfig(baseUrl = "https://smarttutor-production-5d4d.up.railway.app") }
    single<ILoadResourceRepo> { LocalLearningUnitsRepositoryImpl() }
    single<ITutorRepo> {
        WebTutorRepoImpl(
            firebase = get<FirebaseContext>().app,
            httpClient = get(),
            baseUrl = "https://smarttutor-production-5d4d.up.railway.app"
        )
    }


    factory<IProgressRepo> {
        WebProgressRepoImpl(
            httpClient = get(),
            baseUrl = "https://smarttutor-production-5d4d.up.railway.app"
        )
    }


    single { LearningUnitsUseCase(get()) }
    single { PdfUploadServiceUseCase(get()) }
    single { SaveExerciseAnswerUseCase(get()) }
    single { GetAllHistoryChatUseCase(get()) }
    single { AskTutorUseCase(get()) }
    single { GetChatByIdUseCase(get()) }
    single { DeleteChatUseCase(get()) }
    single { LoadTutorFromPdfUseCase(get(), get()) }
    single { UpsertChatProgressUseCase(get()) }
    single { DeleteChatProgressUseCase(get()) }

    factory<IAnalyticsTracker> { WebAnalyticsTrackerImpl() }
    factory<TutorPlatform> {
        object : TutorPlatform {
            override fun speak(text: String) {

            }

            override fun startVoice(onResult: (String) -> Unit) {

            }

            override fun stopVoice() {

            }

            override fun log(tag: String, message: String, t: Throwable?) {

            }
        }
    }
    factory { GetIdTokenSuspendUseCase(get()) }

    factory {
        TutorController(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            userIdProvider = { AuthUserHolder.currentUser?.uid.orEmpty() },
            saveExerciseAnswerUseCase = get()
        )
    }
    includes(logoutModule)
}
