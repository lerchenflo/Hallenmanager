package com.lerchenflo.hallenmanager.di

import com.lerchenflo.hallenmanager.core.di.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

val IosHttpModule = module {
    single<HttpClient> {createHttpClient(Darwin.create())}
}