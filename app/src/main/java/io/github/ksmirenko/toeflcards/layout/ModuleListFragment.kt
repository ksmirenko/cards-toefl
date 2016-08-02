package io.github.ksmirenko.toeflcards.layout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ListView
import android.widget.Toast

import io.github.ksmirenko.toeflcards.ToeflCardsDatabaseProvider
import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.adapters.ModuleCursorAdapter
import kotlinx.android.synthetic.main.fragment_modules_list.view.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.successUi

/**
 * Fragment for category screen, contains a dictionary button and list of modules.

 * @author Kirill Smirenko
 */
class ModuleListFragment : Fragment() {
    private val categoryId = 1L

    private lateinit var moduleListView: ListView
    private var modulesAdapter: CursorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ToeflCardsDatabaseProvider.initIfNull(context)
        // load the list of modules in background thread
        val db = ToeflCardsDatabaseProvider.db
        task {
            db.getModules(categoryId)
        } successUi {
            cursor ->
            modulesAdapter = ModuleCursorAdapter(context, cursor)
            setupListAdapter()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_modules_list, container, false)
        moduleListView = rootView.listview_modules

        // filling the list with modules and setting up onClick
        if (modulesAdapter != null) {
            setupListAdapter()
        }

        // attach the floating action button
        val fab = rootView.fab_dictionary
        fab.attachToListView(moduleListView)
        fab.setOnClickListener {
            // launch dictionary activity
            val dictIntent = Intent(context, DictionaryActivity::class.java)
            dictIntent.putExtra(DictionaryActivity.ARG_CATEGORY_ID, categoryId)
            startActivity(dictIntent)
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val unansweredCount = data!!.getIntExtra(RES_ARG_CARDS_UNANSWERED_CNT, -1)
            val totalCount = data.getIntExtra(RES_ARG_CARDS_TOTAL_CNT, -1)
            val unanswered = data.getStringExtra(RES_ARG_CARDS_UNANSWERED)
            val moduleId = data.getLongExtra(RES_ARG_MODULE_ID, -1)
            val db = ToeflCardsDatabaseProvider.db
            task {
                db.updateModuleProgress(moduleId, unanswered)
            } successUi {
                Toast.makeText(
                    context,
                    getString(R.string.cards_answered) + " "
                        + (totalCount - unansweredCount) + "/" + totalCount,
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListAdapter() {
        with(moduleListView) {
            adapter = modulesAdapter
            setOnItemClickListener { parent, view, position, id ->
                // launch card view activity
                val detailIntent = Intent(context, CardActivity::class.java)
                detailIntent.putExtra(CardActivity.ARG_MODULE_ID, id)
                startActivityForResult(detailIntent, RES_REQUEST_CODE)
            }
        }
    }

    companion object {
        // Request code and arguments for CardActivity result
        val RES_REQUEST_CODE = 1
        val RES_ARG_CARDS_UNANSWERED = "CARDS_UNANSWERED"
        val RES_ARG_CARDS_UNANSWERED_CNT = "CARDS_UNANSWERED_CNT"
        val RES_ARG_CARDS_TOTAL_CNT = "CARDS_TOTAL_CNT"
        val RES_ARG_MODULE_ID = "MODULE_ID"
    }
}
