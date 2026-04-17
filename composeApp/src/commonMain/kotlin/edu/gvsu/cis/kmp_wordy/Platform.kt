package edu.gvsu.cis.kmp_wordy

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform