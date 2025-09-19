package com.lerchenflo.hallenmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform