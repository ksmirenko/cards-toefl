package io.github.ksmirenko.toeflcards

import android.content.Context

/**
 * A singleton that provides ToeflCardsDatabase to the app.
 *
 * @author Kirill Smirenko
 */
object ToeflCardsDatabaseProvider {
    private val dbFilename = "toeflcards.db"

    private var database : ToeflCardsDatabase? = null

    fun initIfNull(context : Context) {
        if (database == null) {
            database = ToeflCardsDatabase(context, dbFilename)
        }
    }

    fun hasDb() = database != null

    val db : ToeflCardsDatabase
        get() {
            if (database == null) {
                throw IllegalStateException(
                    "ToeflCardsDatabaseProvider must be initialized prior to usage.")
            }
            return database as ToeflCardsDatabase
        }
}