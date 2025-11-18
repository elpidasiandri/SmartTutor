package org.example.project.settings.settingsView.di

import org.example.project.settings.settingsView.viewModel.SettingsController
import org.koin.dsl.module

val settingsModule = module {
    single {
        SettingsController()
    }
}