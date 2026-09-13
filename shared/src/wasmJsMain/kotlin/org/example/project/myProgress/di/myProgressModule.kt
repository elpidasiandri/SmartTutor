package org.example.project.myProgress.di

import org.example.project.AppConfig
import org.example.project.myProgress.MyProgressController
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.repo.WebProgressRepoImpl
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetExerciseStatsUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.WebAnalyticsTrackerImpl
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.koin.dsl.module

val myProgressModule = module {
    factory<IAnalyticsTracker> { WebAnalyticsTrackerImpl() }
    factory { GetProgressOverviewUseCase(get()) }
    factory { GetChatByIdUseCase(get()) }
    factory { GetChatProgressListUseCase(get()) }
    factory { GetExerciseStatsUseCase(get()) }
    single { AppConfig(baseUrl = "https://smarttutor-production-5d4d.up.railway.app") }

    factory<IProgressRepo> {
        WebProgressRepoImpl(
            httpClient = get(),
            baseUrl = "https://smarttutor-production-5d4d.up.railway.app"
        )
    }

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

    factory {
        MyProgressController(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

}