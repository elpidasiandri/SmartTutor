package org.example.project.tutor.di

import io.ktor.client.HttpClient
import org.example.project.AppConfig
import org.example.project.network.createWebHttpClient
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
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase
import org.example.project.tutor.viewModel.TutorController
import org.koin.dsl.module

val tutorModule = module {
    single<HttpClient> { createWebHttpClient() }
    single { AppConfig(baseUrl = "http://localhost:8081") }
    single<ILoadResourceRepo> { LocalLearningUnitsRepositoryImpl() }
    single<ITutorRepo> {
        WebTutorRepoImpl(
            firebase = initFirebase().app,
            httpClient = get(),
            baseUrl = "http://localhost:8081"
        )
    }
    single { LearningUnitsUseCase(get()) }
    factory { GetIdTokenSuspendUseCase(get()) }
    single { PdfUploadServiceUseCase(get()) }
    factory { GetAllHistoryChatUseCase(get()) }
    single { AskTutorUseCase(get()) }
    factory { GetChatByIdUseCase(get()) }
    single { DeleteChatUseCase(get()) }
    factory<IAnalyticsTracker> { WebAnalyticsTrackerImpl() }
    single { LoadTutorFromPdfUseCase(get(), get()) }

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
    single {
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
        )
    }
    includes(logoutModule)
}
