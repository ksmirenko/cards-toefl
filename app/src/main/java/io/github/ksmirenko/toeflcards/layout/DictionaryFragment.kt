package io.github.ksmirenko.toeflcards.layout

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.SimpleCursorAdapter
import io.github.ksmirenko.toeflcards.DictionaryFilterQueryProvider
import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.ToeflCardsDatabase
import io.github.ksmirenko.toeflcards.ToeflCardsDatabaseProvider
import kotlinx.android.synthetic.main.fragment_dictionary.*
import kotlinx.android.synthetic.main.fragment_dictionary.view.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

/**
 * Fragment that contains the dictionary and SearchView related to it.
 */
class DictionaryFragment : Fragment() {
    private var adapter: CursorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // fill the main list view
        val db = ToeflCardsDatabaseProvider.db
        task {
            db.getDictionary()
        } successUi {
            cursor ->
            adapter = SimpleCursorAdapter(
                context,
                R.layout.listview_item_dictionary,
                cursor,
                ToeflCardsDatabase.CardQuery.getCursorAdapterArg(),
                intArrayOf(R.id.textview_listitem_dict_front, R.id.textview_listitem_dict_back),
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
            )
            adapter?.filterQueryProvider = DictionaryFilterQueryProvider(db)
            val dictionaryListView = listview_dictionary
            dictionaryListView.adapter = adapter
            dictionaryListView.isTextFilterEnabled = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_dictionary, container, false)

        // setup searchView and associate searchable configuration
        val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        with(rootView.searchview_dictionary) {
            setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN && isIconified) {
                    this.onActionViewExpanded()
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    adapter?.filter?.filter(newText)
                    adapter?.notifyDataSetChanged()
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    adapter?.filter?.filter(query)
                    adapter?.notifyDataSetChanged()
                    return true
                }
            })
        }

        return rootView
    }
}