package io.github.ksmirenko.toeflcards.layout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import io.github.ksmirenko.toeflcards.ToeflCardsDatabase
import io.github.ksmirenko.toeflcards.ToeflCardsDatabaseProvider
import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.StringUtils
import io.github.ksmirenko.toeflcards.adapters.CardsPagerAdapter
import kotlinx.android.synthetic.main.activity_card.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.successUi
import java.util.*

/**
 * Activity for card viewing, i.e. main purpose of the app.

 * @author Kirill Smirenko
 */
class CardActivity : AppCompatActivity(), CardContainerFragment.Callbacks {
    // for extracting data from cursor
    private val COLUMN_INDEX = ToeflCardsDatabase.CardQuery.COLUMN_INDEX_ID

    private var cardCursor: Cursor? = null // FIXME: make it non-nullable

    private var moduleId: Long = 0
    private var cardsTotalCount: Int = 0
    private var cardsUnansweredIds: ArrayList<Long>? = null

    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // set up context
        appContext = applicationContext

        // hide action bar
        supportActionBar?.hide()

        // extracting module ID
        moduleId = intent.getLongExtra(ARG_MODULE_ID, 0)

        // extracting preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isBackFirst = prefs.getBoolean(getString(R.string.pref_back_first), false)
        val isRandom = prefs.getBoolean(getString(R.string.pref_shuffle), true)
        val isUnansweredOnly = prefs.getBoolean(getString(R.string.pref_unanswered), true)

        // obtaining cards
        ToeflCardsDatabaseProvider.initIfNull(applicationContext)
        val db = ToeflCardsDatabaseProvider.db
        task {
            cardCursor = db.getModuleCards(moduleId, isRandom, isUnansweredOnly)
        } then {
            if (isUnansweredOnly && cardCursor!!.count == 0) {
                cardCursor = db.getModuleCards(moduleId, isRandom, false)
                Toast.makeText(this, R.string.no_unanswered_cards, Toast.LENGTH_SHORT).show()
            }
        } successUi {
            // setting up adapter
            val pagerAdapter = CardsPagerAdapter(fragmentManager, cardCursor!!, isBackFirst)
            viewpager_card_container.adapter = pagerAdapter
            // initializing counters
            cardsTotalCount = cardCursor!!.count
            cardsUnansweredIds = ArrayList<Long>()
        }
    }

    override fun onBackPressed() {
        saveAndExit(true)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    // callback for cards' buttons
    override fun onCardButtonClicked(knowIt: Boolean) {
        val position = viewpager_card_container.currentItem
        // saving information about last viewed card
        if (!knowIt) {
            cardCursor!!.moveToPosition(position)
            cardsUnansweredIds!!.add(cardCursor!!.getLong(COLUMN_INDEX))
        }
        if (position + 1 >= cardsTotalCount) {
            saveAndExit(false)
        }
        else {
            viewpager_card_container.setCurrentItem(position + 1, true)
        }
    }

    // callback for quit button
    override fun onQuitButtonClicked() {
        saveAndExit(true)
    }

    private fun saveAndExit(wasInterrupted: Boolean) {
        if (wasInterrupted) {
            // running till the end of cardCursor and adding all remaining cards to unanswered
            var hasRemainingCards = cardCursor!!.moveToPosition(viewpager_card_container.currentItem)
            while (hasRemainingCards) {
                cardsUnansweredIds!!.add(cardCursor!!.getLong(COLUMN_INDEX))
                hasRemainingCards = cardCursor!!.moveToNext()
            }
        }

        // storing user result in an intent and closing the activity
        val intent = Intent()
        intent.putExtra(ModuleListFragment.RES_ARG_CARDS_UNANSWERED_CNT, cardsUnansweredIds!!.size)
        intent.putExtra(ModuleListFragment.RES_ARG_CARDS_UNANSWERED, StringUtils.listToString(cardsUnansweredIds!!))
        intent.putExtra(ModuleListFragment.RES_ARG_CARDS_TOTAL_CNT, cardsTotalCount)
        intent.putExtra(ModuleListFragment.RES_ARG_MODULE_ID, moduleId)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        // intent arguments
        val ARG_MODULE_ID = "module_id"

        /**
         * Context for the fragments to work normally.
         */
        var appContext: Context? = null
            private set
    }
}
