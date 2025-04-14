package org.example.openaitts

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform