package org.example.project.myProgress.di

import org.example.project.AppConfig
import org.example.project.myProgress.MyProgressController
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.repo.WebProgressRepoImpl
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.WebAnalyticsTrackerImpl
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.koin.dsl.module

val myProgressModule = module {
    factory<IAnalyticsTracker> { WebAnalyticsTrackerImpl() }
    single { GetProgressOverviewUseCase(get()) }
    single { GetChatProgressListUseCase(get()) }
    single { AppConfig(baseUrl = "http://localhost:8081") }

    factory<IProgressRepo> {
        WebProgressRepoImpl(
            httpClient = get(),
            baseUrl = "http://localhost:8081"
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

    single {
        MyProgressController(
            get(),
            get(),
            get(),
            get(),
        )
    }

}