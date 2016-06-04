package io.github.ksmirenko.toeflcards

import android.content.Context

/**
 * A singleton that provides FlexiDatabase to the app.
 *
 * @author Kirill Smirenko
 */
object FlexiDatabaseProvider {
    private val dbFilename = "toeflcards.db"

    private var database : FlexiDatabase? = null

    fun initIfNull(context : Context) {
        if (database == null) {
            database = FlexiDatabase(context, dbFilename)
        }
    }

    fun hasDb() = database != null

    val db : FlexiDatabase
        get() {
            if (database == null) {
                throw IllegalStateException(
                    "FlexiDatabaseProvider must be initialized prior to usage.")
            }
            return database as FlexiDatabase
        }
}