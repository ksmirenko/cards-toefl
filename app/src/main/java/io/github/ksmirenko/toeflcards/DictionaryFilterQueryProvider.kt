package io.github.ksmirenko.toeflcards

import android.widget.FilterQueryProvider

/**
 * Filter query provider for Dictionary.
 */
class DictionaryFilterQueryProvider(
    private val categoryId: Long,
    private val db: ToeflCardsDatabase
) : FilterQueryProvider {
    // according to documentation, this is performed asynchronously
    override fun runQuery(constraint: CharSequence?) =
        db.getDictionaryFiltered(categoryId, constraint)
}