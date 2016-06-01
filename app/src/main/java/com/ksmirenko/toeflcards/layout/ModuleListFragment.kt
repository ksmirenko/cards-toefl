package com.ksmirenko.toeflcards.layout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.Toast

import com.ksmirenko.toeflcards.FlexiDatabaseProvider
import com.ksmirenko.toeflcards.R
import com.ksmirenko.toeflcards.adapters.ModuleCursorAdapter
import kotlinx.android.synthetic.main.fragment_modules_list.view.*
import kotlinx.android.synthetic.main.fragment_module_settings.view.*

/**
 * Fragment for category screen, contains a dictionary button and list of modules.

 * @author Kirill Smirenko
 */
class ModuleListFragment : Fragment() {
    private val categoryId = 1L
    private var modulesAdapter: CursorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FlexiDatabaseProvider.db
        val cursor = db.getModules(categoryId)
        modulesAdapter = ModuleCursorAdapter(context, cursor)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_modules_list, container, false)
        val listView = rootView.listview_modules

        // filling the list with modules and setting up onClick
        if (modulesAdapter != null) {
            listView.adapter = modulesAdapter
            listView.setOnItemClickListener { parent, view, position, id ->
                // preparing module settings dialog
                val moduleId = id
                val context = view.context
                val inflateService = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val dlgView = inflateService.inflate(R.layout.fragment_module_settings, null, false)
                dlgView.switch_dlg_module_whichside.setOnCheckedChangeListener { buttonView, isChecked ->
                    dlgView.textview_dlg_module_whichside.setText(
                        if (isChecked) R.string.back_side_first else R.string.front_side_first
                    )
                }
                // showing module settings dialog dialog
                AlertDialog.Builder(context).setView(dlgView).setPositiveButton("OK") { dialog, id ->
                    dialog.dismiss()
                    // launching card view activity
                    val detailIntent = Intent(getContext(), CardActivity::class.java)
                    detailIntent.putExtra(CardActivity.ARG_MODULE_ID, moduleId)
                    detailIntent.putExtra(CardActivity.ARG_IS_BACK_FIRST,
                        dlgView.switch_dlg_module_whichside.isChecked)
                    detailIntent.putExtra(CardActivity.ARG_IS_RANDOM,
                        dlgView.cb_dlg_module_random.isChecked)
                    detailIntent.putExtra(CardActivity.ARG_IS_UNANSWERED_ONLY,
                        dlgView.cb_dlg_module_unanswered.isChecked)
                    startActivityForResult(detailIntent, RES_REQUEST_CODE)
                }.show()
            }
        }

        // attach the floating action button
        val fab = rootView.fab_dictionary
        fab.attachToListView(listView)
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
            val db = FlexiDatabaseProvider.db
            db.updateModuleProgress(moduleId, unanswered)
            Toast.makeText(
                context,
                getString(R.string.cards_answered) + " "
                    + (totalCount - unansweredCount) + "/" + totalCount,
                Toast.LENGTH_SHORT).show()
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
