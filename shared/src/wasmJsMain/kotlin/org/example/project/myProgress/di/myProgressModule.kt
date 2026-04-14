package org.example.project.myProgress.di

import org.example.project.myProgress.MyProgressController
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.WebAnalyticsTrackerImpl
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.koin.dsl.module

val myProgressModule = module {
    factory { GetAllHistoryChatUseCase(get()) }
    factory { GetChatByIdUseCase(get()) }
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

    single {
        MyProgressController(
            get(),
            get(),
            get(),
            get(),
        )
    }

}