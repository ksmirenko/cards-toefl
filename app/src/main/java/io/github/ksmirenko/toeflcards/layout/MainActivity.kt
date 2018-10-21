package io.github.ksmirenko.toeflcards.layout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast

import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.ToeflCardsDatabaseProvider
import io.github.ksmirenko.toeflcards.adapters.MainScreenPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Activity for screen with the list of modules and dictionary.
 * @author Kirill Smirenko
 */
class MainActivity : AppCompatActivity() {
    private lateinit var fabTraining: FloatingActionButton

    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init database
        ToeflCardsDatabaseProvider.initIfNull(this@MainActivity)

        // remove action bar's evevation
        supportActionBar?.elevation = 0F


        // setup the FAB
        fabTraining = fab_training
        with(fabTraining) {
            setOnClickListener {
                // launch card view activity with a specific argument
                val detailIntent = Intent(context, CardActivity::class.java)
                detailIntent.putExtra(CardActivity.ARG_MODULE_ID, CardActivity.MODULE_ID_TRAINING)
                startActivityForResult(detailIntent, RES_REQUEST_CODE_TRAINING)
            }
        }

        // setup the module list fragment
        with(viewpager_main_screen) {
            adapter = MainScreenPagerAdapter(supportFragmentManager, this@MainActivity)
            tablayout_main_screen.setupWithViewPager(this)
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float,
                                            positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0) {
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(windowToken, 0)
                        fabTraining.show()
                    }
                    else
                        fabTraining.hide()
                }
            })
        }

        // debug code: clear prefs to show hint dialog on every launch
        //getSharedPreferences("ShouldShowHint", 0).edit().clear().apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_module_settings -> {
                val prefsIntent = Intent(this, ModuleSettingsActivity::class.java)
                startActivity(prefsIntent)
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // Processes activity result returned after training and shows progress.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RES_REQUEST_CODE_TRAINING && resultCode == Activity.RESULT_OK) {
            val unansweredCount =
                data!!.getIntExtra(ModuleListFragment.RES_ARG_CARDS_UNANSWERED_CNT, -1)
            val totalCount = data.getIntExtra(ModuleListFragment.RES_ARG_CARDS_TOTAL_CNT, -1)
            Toast.makeText(
                this@MainActivity,
                "${getString(R.string.cards_answered)} ${totalCount - unansweredCount}/$totalCount",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showAboutDialog() {
        val d: AlertDialog = AlertDialog.Builder(this)
            .setPositiveButton(android.R.string.ok, null)
            .setMessage(R.string.text_about)
            .setTitle(R.string.app_name)
            .create()
        d.show()
        (d.findViewById<TextView>(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
    }

    companion object {
        private const val RES_REQUEST_CODE_TRAINING = 2
    }
}
