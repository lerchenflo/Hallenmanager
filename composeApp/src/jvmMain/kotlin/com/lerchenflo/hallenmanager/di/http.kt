package com.lerchenflo.hallenmanager.di

import com.lerchenflo.hallenmanager.core.di.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

val desktopHttpModule = module {
    single<HttpClient> {createHttpClient(OkHttp.create())}
}