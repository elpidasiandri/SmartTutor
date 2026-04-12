package org.example.project.tutor.module

import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.example.project.AppConfig
import org.example.project.settings.logout.module.logoutModule
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.network.createAndroidHttpClient
import org.example.project.tutor.repo.loadResource.ILoadResourceRepo
import org.example.project.tutor.repo.ITutorRepo
import org.example.project.tutor.repo.TutorIntroRepoImpl
import org.example.project.tutor.repo.loadResource.LocalLearningUnitsRepositoryImpl
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
import org.example.project.tutor.useCases.PdfUploadServiceUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase
import org.example.project.tutor.viewModel.TutorIntroViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tutorModule = module {
    single<HttpClient> { createAndroidHttpClient() }
    factory<CoroutineDispatcher> { Dispatchers.IO }
    single<ITutorRepo> {
        TutorIntroRepoImpl(
            httpClient = get(),
            baseUrl = "http://10.0.2.2:8081",
            FirebaseAuth.getInstance()
        )
    }
    single { AppConfig(baseUrl = "http://10.0.2.2:8081") }
    single<ILoadResourceRepo> { LocalLearningUnitsRepositoryImpl() }
    single { LearningUnitsUseCase(get()) }
    single { PdfUploadServiceUseCase(get()) }
    single { GetAllHistoryChatUseCase(get()) }
    single { GetChatByIdUseCase(get()) }
    single { AskTutorUseCase(get()) }
    single { DeleteChatUseCase(get()) }
    single { LoadTutorFromPdfUseCase(get(), get()) }
    factory {
        GetIdTokenSuspendUseCase(get())
    }
    single<TutorPlatform> {
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

    viewModel {
        TutorIntroViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            FirebaseAuth.getInstance(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    includes(logoutModule)
}