package io.github.ksmirenko.toeflcards

import android.widget.FilterQueryProvider

/**
 * Filter query provider for Dictionary.
 */
class DictionaryFilterQueryProvider(val categoryId: Long, val db: ToeflCardsDatabase)
: FilterQueryProvider {
    override fun runQuery(constraint: CharSequence?) =
            db.getDictionaryFiltered(categoryId, constraint)
}