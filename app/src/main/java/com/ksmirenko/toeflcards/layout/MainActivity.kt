package com.ksmirenko.toeflcards.layout

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

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

        // add module list fragment
        if (savedInstanceState == null) {
            val fragment = ModuleListFragment()
            supportFragmentManager.beginTransaction().add(R.id.category_detail_container, fragment).commit()
        }

        // TODO: remove debug code
        // clear prefs to show hint dialog
//        getSharedPreferences("ShouldShowHint", 0).edit().clear().apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_module_settings -> {
                // TODO: module settings
                return true
            }
            R.id.action_about -> {
                showAboutDialog()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun showAboutDialog() {
        val d = AlertDialog.Builder(appContext)
            .setPositiveButton(android.R.string.ok, null)
            .setMessage(R.string.text_about)
            .setTitle(R.string.app_name)
            .create();
        d.show();
        (d.findViewById(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
    }

    companion object {
        /**
         * Context for the fragments to work normally.
         */
        var appContext: Context? = null
            private set
    }
}
