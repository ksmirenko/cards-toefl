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
import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.StringUtils
import io.github.ksmirenko.toeflcards.ToeflCardsDatabase
import io.github.ksmirenko.toeflcards.ToeflCardsDatabaseProvider
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

    private lateinit var cardCursor: Cursor

    private var moduleId: Long = 0
    private var cardsTotalCount: Int = 0
    private lateinit var cardsUnansweredIds: ArrayList<Long>

    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // set up context
        appContext = applicationContext

        // hide action bar
        supportActionBar?.hide()

        // extracting module ID
        moduleId = intent.getLongExtra(ARG_MODULE_ID, MODULE_ID_TRAINING)
        val isTraining = moduleId == MODULE_ID_TRAINING

        // extracting preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isBackFirst = prefs.getBoolean(getString(R.string.pref_back_first), false)
        val isRandom = prefs.getBoolean(getString(R.string.pref_shuffle), true)
        val isUnansweredOnly = prefs.getBoolean(getString(R.string.pref_unanswered), true)

        // obtaining cards
        ToeflCardsDatabaseProvider.initIfNull(applicationContext)
        val db = ToeflCardsDatabaseProvider.db
        task {
            cardCursor =
                    if (isTraining)
                        db.getTrainingCards()
                    else
                        db.getModuleCards(moduleId, isRandom, isUnansweredOnly)
        } then {
            if (!isTraining && isUnansweredOnly && cardCursor.count == 0) {
                cardCursor = db.getModuleCards(moduleId, isRandom, false)
                Toast.makeText(this, R.string.no_unanswered_cards, Toast.LENGTH_SHORT).show()
            }
        } successUi {
            // setting up adapter
            val pagerAdapter = CardsPagerAdapter(fragmentManager, cardCursor, isBackFirst)
            viewpager_card_container.adapter = pagerAdapter
            // initializing counters
            cardsTotalCount = cardCursor.count
        }
        cardsUnansweredIds = ArrayList<Long>()
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
            cardCursor.moveToPosition(position)
            cardsUnansweredIds.add(cardCursor.getLong(COLUMN_INDEX))
        }
        if (position + 1 >= cardsTotalCount) {
            saveAndExit(false)
        } else {
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
            var hasRemainingCards = cardCursor.moveToPosition(viewpager_card_container.currentItem)
            while (hasRemainingCards) {
                cardsUnansweredIds.add(cardCursor.getLong(COLUMN_INDEX))
                hasRemainingCards = cardCursor.moveToNext()
            }
        }

        // storing user result in an intent and closing the activity
        with(Intent()) {
            putExtra(ModuleListFragment.RES_ARG_CARDS_UNANSWERED_CNT, cardsUnansweredIds.size)
            putExtra(ModuleListFragment.RES_ARG_CARDS_UNANSWERED, StringUtils.listToString(cardsUnansweredIds))
            putExtra(ModuleListFragment.RES_ARG_CARDS_TOTAL_CNT, cardsTotalCount)
            putExtra(ModuleListFragment.RES_ARG_MODULE_ID, moduleId)
            this@CardActivity.setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    companion object {
        /**
         * Intent argument
         */
        const val ARG_MODULE_ID = "module_id"

        /**
         * The value that should be passed as ARG_MODULE_ID when loading a training
         * instead of a module.
         */
        const val MODULE_ID_TRAINING = -2L

        /**
         * Context for the fragments to work normally.
         */
        lateinit var appContext: Context
            private set
    }
}
