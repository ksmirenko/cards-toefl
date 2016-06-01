package com.ksmirenko.toeflcards.layout

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.ksmirenko.toeflcards.FlexiDatabase
import com.ksmirenko.toeflcards.FlexiDatabaseProvider
import com.ksmirenko.toeflcards.R
import com.ksmirenko.toeflcards.StringUtils
import com.ksmirenko.toeflcards.adapters.CardsPagerAdapter
import kotlinx.android.synthetic.main.activity_card.*
import java.util.ArrayList

/**
 * Activity for card viewing, i.e. main purpose of the app.

 * @author Kirill Smirenko
 */
class CardActivity : AppCompatActivity(), CardContainerFragment.Callbacks {
    // for extracting data from cursor
    private val COLUMN_INDEX = FlexiDatabase.CardQuery.COLUMN_INDEX_ID

    private var cardCursor: Cursor? = null // FIXME: make it non-nullable

    private var moduleId: Long = 0
    private var cardsTotalCount: Int = 0
    private var cardsUnansweredIds: ArrayList<Long>? = null

    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // hide action bar
        supportActionBar?.hide()

        // extracting arguments
        val intent = intent
        moduleId = intent.getLongExtra(ARG_MODULE_ID, 0)
        val isBackFirst = intent.getBooleanExtra(ARG_IS_BACK_FIRST, false)
        val isRandom = intent.getBooleanExtra(ARG_IS_RANDOM, true)
        val isUnansweredOnly = intent.getBooleanExtra(ARG_IS_UNANSWERED_ONLY, false)

        // obtaining cards
        val db = FlexiDatabaseProvider.db
        cardCursor = db.getModuleCards(moduleId, isRandom, isUnansweredOnly)
        if (isUnansweredOnly && cardCursor!!.count == 0) {
            cardCursor = db.getModuleCards(moduleId, isRandom, false)
            Toast.makeText(this, R.string.no_unanswered_cards, Toast.LENGTH_SHORT).show()
        }

        // setting up adapter
        val pagerAdapter = CardsPagerAdapter(fragmentManager, cardCursor!!, isBackFirst)
        viewpager_card_container.adapter = pagerAdapter

        // initializing counters
        cardsTotalCount = cardCursor!!.count
        cardsUnansweredIds = ArrayList<Long>()
    }

    override fun onBackPressed() {
        saveAndExit(true)
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
        val ARG_IS_BACK_FIRST = "back_first"
        val ARG_IS_RANDOM = "random"
        val ARG_IS_UNANSWERED_ONLY = "unanswered"
    }
}
