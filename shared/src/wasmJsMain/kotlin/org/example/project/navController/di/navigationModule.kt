package org.example.project.navController.di

import org.example.project.navController.NavController
import org.koin.dsl.module

val navigationModule = module {
    single { NavController() }
}
