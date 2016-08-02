package io.github.ksmirenko.toeflcards

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

/**
 * The application's SQL database manager.
 * Not a singleton, so it's up to whichever class uses this DB to create an instance and
 * pass it where needed. Or, use [ToeflCardsDatabaseProvider], which is a singleton.
 * Also, the file app/src/main/assets/databases/toeflcards.db should be provided.
 *
 * @author Kirill Smirenko
 */
class ToeflCardsDatabase(context: Context, dbname: String) :
    SQLiteAssetHelper(context, dbname, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1

        private val COLLATION = " COLLATE UNICODE"
    }

    /**
     * Returns a Cursor to cards of the specified module.
     */
    fun getDictionary(categoryId: Long): Cursor? = readableDatabase.query(
        CardEntry.TABLE_NAME,
        CardQuery.getQueryArg(),
        CardEntry.COLUMN_NAME_CATEGORY_ID + "=?",
        arrayOf(categoryId.toString()),
        null, null, CardEntry.COLUMN_NAME_FRONT_CONTENT + COLLATION)

    /**
     * Returns a Cursor to cards of the specified module
     * whose front or back content begins with [constraint].
     */
    fun getDictionaryFiltered(categoryId: Long, constraint: CharSequence?): Cursor? {
        val constr = "${constraint.toString()}%"
        return readableDatabase.query(
            CardEntry.TABLE_NAME,
            CardQuery.getQueryArg(),
            "${CardEntry.COLUMN_NAME_CATEGORY_ID}=? AND " +
                "(${CardEntry.COLUMN_NAME_FRONT_CONTENT} like ? " +
                "OR ${CardEntry.COLUMN_NAME_BACK_CONTENT} like ?)",
            arrayOf(categoryId.toString(), constr, constr),
            null, null, CardEntry.COLUMN_NAME_FRONT_CONTENT + COLLATION)
    }

    /**
     * Returns a Cursor to (all or unanswered last time) cards of the specified module.
     */
    fun getModuleCards(moduleId: Long, isRandom: Boolean, isUnansweredOnly: Boolean): Cursor {
        val moduleCursor = readableDatabase.query(
            ModuleEntry.TABLE_NAME,
            arrayOf(if (isUnansweredOnly) ModuleEntry.COLUMN_NAME_UNANSWERED else ModuleEntry.COLUMN_NAME_CARDS),
            ModuleEntry._ID + "=?",
            arrayOf(moduleId.toString()),
            null, null, null)
        moduleCursor.moveToFirst()
        val moduleCardsRaw: String? = moduleCursor.getString(0)
        // if there is no data about unanswered, return all
        val moduleCards = if (isUnansweredOnly && moduleCardsRaw == null) {
            val anotherModuleCursor = readableDatabase.query(
                ModuleEntry.TABLE_NAME,
                arrayOf(ModuleEntry.COLUMN_NAME_CARDS),
                ModuleEntry._ID + "=?",
                arrayOf(moduleId.toString()),
                null, null, null)
            anotherModuleCursor.moveToFirst()
            val moduleCardsResult = anotherModuleCursor.getString(0)
            anotherModuleCursor.close()
            moduleCardsResult
        }
        else {
            moduleCardsRaw!!
        }
        moduleCursor.close()
        return readableDatabase.query(
            CardEntry.TABLE_NAME,
            CardQuery.getQueryArg(),
            CardEntry._ID + " in " + StringUtils.stringToSqlReadyString(moduleCards),
            null, null, null,
            if (isRandom) "RANDOM()" else null)
    }

    /**
     * Returns a Cursor to modules for the specified category.
     */
    fun getModules(categoryId: Long): Cursor? = readableDatabase.query(
        ModuleEntry.TABLE_NAME,
        ModuleQuery.getNamesQueryArg(),
        ModuleEntry.COLUMN_NAME_CATEGORY_ID + "=?",
        arrayOf(categoryId.toString()),
        null, null, ModuleEntry.COLUMN_NAME_NAME + COLLATION)

    /**
     * Saves user progress on a module.
     * @param moduleId Module ID.
     * @param unanswered Information about unanswered cards in the format provided by [StringUtils] object.
     */
    fun updateModuleProgress(moduleId: Long, unanswered: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(ModuleEntry.COLUMN_NAME_UNANSWERED, unanswered)
        db.update(ModuleEntry.TABLE_NAME, values, ModuleEntry._ID + "=?", arrayOf(moduleId.toString()))
    }

    /**
     * Contract for extracting a Card from SQL row.
     */
    class CardQuery {
        companion object {
            val COLUMN_INDEX_ID = 0
            val COLUMN_INDEX_FRONT = 1
            val COLUMN_INDEX_BACK = 2
            fun getQueryArg() = arrayOf(CardEntry._ID, CardEntry.COLUMN_NAME_FRONT_CONTENT,
                CardEntry.COLUMN_NAME_BACK_CONTENT)

            fun getCursorAdapterArg() = arrayOf(CardEntry.COLUMN_NAME_FRONT_CONTENT,
                CardEntry.COLUMN_NAME_BACK_CONTENT)
        }
    }

    /**
     * Contract for extracting a Module from SQL row.
     */
    class ModuleQuery {
        companion object {
            val COLUMN_INDEX_NAME = 1
            fun getNamesQueryArg() = arrayOf(ModuleEntry._ID, ModuleEntry.COLUMN_NAME_NAME)
        }
    }

    /**
     * Card storing contract.
     */
    private class CardEntry : BaseColumns {
        companion object {
            val _ID = BaseColumns._ID
            val TABLE_NAME = "cards"
            val COLUMN_NAME_CATEGORY_ID = "cardCatId"
            val COLUMN_NAME_FRONT_CONTENT = "front"
            val COLUMN_NAME_BACK_CONTENT = "back"
        }
    }

    /**
     * Module storing contract.
     */
    private class ModuleEntry : BaseColumns {
        companion object {
            val _ID = BaseColumns._ID
            val TABLE_NAME = "modules"
            val COLUMN_NAME_CATEGORY_ID = "moduleCatId"
            val COLUMN_NAME_NAME = "moduleName"
            val COLUMN_NAME_CARDS = "moduleCards" // this one is actually an array, so we'll have to decode it
            val COLUMN_NAME_UNANSWERED = "moduleUnanswred" // and this one
        }
    }
}