package com.development.forge.server.di

import com.development.forge.server.task.TaskRepository
import org.koin.dsl.module

val appModule = module {
    single { TaskRepository }
}
