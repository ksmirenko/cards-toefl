package com.ksmirenko.toeflcards.layout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

import com.ksmirenko.toeflcards.FlexiDatabaseProvider
import com.ksmirenko.toeflcards.R

/**
 * Activity for category screen.

 * @author Kirill Smirenko
 */
class MainActivity : AppCompatActivity() {
    private val categoryId = 1L
    private val dbFilename = "toeflcards.db"

    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.appContext = this@MainActivity
        setContentView(R.layout.activity_main)

        // init database
        if (!FlexiDatabaseProvider.hasDb()) {
            FlexiDatabaseProvider.init(appContext!!, dbFilename)
        }

        // set up top action bar
        val toolbar = findViewById(R.id.toolbar_activity_category) as Toolbar?
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            val fragment = ModuleListFragment()
            supportFragmentManager.beginTransaction().add(R.id.category_detail_container, fragment).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_view_dictionary) {
            // launcing dictionary activity
            val dictIntent = Intent(this, DictionaryActivity::class.java)
            dictIntent.putExtra(DictionaryActivity.ARG_CATEGORY_ID, categoryId)
            startActivity(dictIntent)
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        /**
         * Context for the fragments to work normally.
         */
        var appContext: Context? = null
            private set
    }
}
