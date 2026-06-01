package org.example.project.tutor.module

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.example.project.AppConfig
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.repo.ProgressRepoImpl
import org.example.project.myProgress.useCases.DeleteChatProgressUseCase
import org.example.project.myProgress.useCases.GetExerciseStatsUseCase
import org.example.project.myProgress.useCases.UpsertChatProgressUseCase
import org.example.project.settings.logout.module.logoutModule
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.network.createAndroidHttpClient
import org.example.project.tutor.repo.AnalyticsTrackerImpl
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.repo.loadResource.ILoadResourceRepo
import org.example.project.tutor.repo.ITutorRepo
import org.example.project.tutor.repo.TutorIntroRepoImpl
import org.example.project.tutor.repo.loadResource.LocalLearningUnitsRepositoryImpl
import org.example.project.tutor.useCases.AddMoreExercisesUseCase
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
import org.example.project.tutor.useCases.MarkUnitAsReadUseCase
import org.example.project.tutor.useCases.PdfUploadServiceUseCase
import org.example.project.tutor.useCases.SaveExerciseAnswerUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase
import org.example.project.tutor.viewModel.TutorIntroViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tutorModule = module {
    single<HttpClient> { createAndroidHttpClient() }
    factory<CoroutineDispatcher> { Dispatchers.IO }
    single<ITutorRepo> {
        TutorIntroRepoImpl(
            httpClient = get(),
            baseUrl = "https://smarttutor-production-5d4d.up.railway.app",
            FirebaseAuth.getInstance()
        )
    }
    factory <IProgressRepo> {
        ProgressRepoImpl(
            httpClient = get(),
            baseUrl = "https://smarttutor-production-5d4d.up.railway.app",
        )
    }

    single { AppConfig(baseUrl = "https://smarttutor-production-5d4d.up.railway.app") }
    single<ILoadResourceRepo> { LocalLearningUnitsRepositoryImpl() }

    single { provideFirebaseAnalytics(androidContext()) }
    single<IAnalyticsTracker> { AnalyticsTrackerImpl(get()) }
    single { UpsertChatProgressUseCase(get()) }
    single { DeleteChatProgressUseCase(get()) }
    single { LearningUnitsUseCase(get()) }
    single { SaveExerciseAnswerUseCase(get()) }
    single { PdfUploadServiceUseCase(get()) }
    single { MarkUnitAsReadUseCase(get()) }
    single { GetAllHistoryChatUseCase(get()) }
    single { GetChatByIdUseCase(get()) }
    single { AskTutorUseCase(get()) }
    single { AddMoreExercisesUseCase(get()) }
    single { DeleteChatUseCase(get()) }
    factory { GetExerciseStatsUseCase(get()) }
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

@Suppress("MissingPermission")
fun provideFirebaseAnalytics(context: android.content.Context): FirebaseAnalytics {
    return FirebaseAnalytics.getInstance(context)
}