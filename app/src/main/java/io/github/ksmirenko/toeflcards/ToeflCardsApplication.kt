package io.github.ksmirenko.toeflcards

import android.app.Application
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant

/**
 * Custom Application which is needed to work with Kovenant properly.
 */
class ToeflCardsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configure Kovenant with standard dispatchers suitable for an Android environment.
        startKovenant()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Dispose of the Kovenant thread pools.
        stopKovenant()
    }
}