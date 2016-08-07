package io.github.ksmirenko.toeflcards.layout

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.ToeflCardsDatabaseProvider
import io.github.ksmirenko.toeflcards.adapters.MainScreenPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Activity for screen with the list of modules and dictionary.
 * @author Kirill Smirenko
 */
class MainActivity : AppCompatActivity() {
    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init database
        ToeflCardsDatabaseProvider.initIfNull(this@MainActivity)

        // remove action bar's evevation
        supportActionBar?.elevation = 0F

        // add module list fragment
        val viewPager = viewpager_main_screen
        viewPager.adapter = MainScreenPagerAdapter(supportFragmentManager, this)
        tablayout_main_screen.setupWithViewPager(viewPager)

        // debug code: clear prefs to show hint dialog on every launch
        //getSharedPreferences("ShouldShowHint", 0).edit().clear().apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_module_settings -> {
                val prefsIntent = Intent(this, ModuleSettingsActivity::class.java)
                startActivity(prefsIntent)
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

    private fun showAboutDialog() {
        val d = AlertDialog.Builder(this)
            .setPositiveButton(android.R.string.ok, null)
            .setMessage(R.string.text_about)
            .setTitle(R.string.app_name)
            .create()
        d.show()
        (d.findViewById(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
    }
}
