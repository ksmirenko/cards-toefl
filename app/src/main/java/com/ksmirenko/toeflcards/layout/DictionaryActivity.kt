package com.ksmirenko.toeflcards.layout

import android.R.id
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*

import com.ksmirenko.toeflcards.DictionaryFilterQueryProvider
import com.ksmirenko.toeflcards.FlexiDatabase
import com.ksmirenko.toeflcards.FlexiDatabaseProvider
import com.ksmirenko.toeflcards.R
import kotlinx.android.synthetic.main.activity_dictionary.*

/**
 * Dictionary activity - category selection screen.

 * @author Kirill Smirenko
 */
class DictionaryActivity : AppCompatActivity() {

    private var adapter: CursorAdapter? = null // FIXME: make it non-nullable

    @SuppressWarnings("ConstantConditions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)

        // setting up top action bar
        setSupportActionBar(toolbar_activity_dictionary)
        supportActionBar!!.setTitle(R.string.dictionary)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // filling the main list view
        val categoryId = intent.getLongExtra(ARG_CATEGORY_ID, 0)
        val db = FlexiDatabaseProvider.db
        val cursor = db.getDictionary(categoryId)
        //adapter = new DictionaryCursorAdapter(this, cursor);
        adapter = SimpleCursorAdapter(
            this,
            R.layout.listview_item_dictionary,
            cursor,
            FlexiDatabase.CardQuery.getCursorAdapterArg(),
            intArrayOf(R.id.textview_listitem_dict_front, R.id.textview_listitem_dict_back),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        adapter!!.filterQueryProvider = DictionaryFilterQueryProvider(categoryId, db)
        val listView = findViewById(R.id.listview_dictionary) as ListView?
        listView!!.adapter = adapter
        listView.isTextFilterEnabled = true

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_dictionary, menu)

        // associating searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search_dict).actionView as SearchView
        searchView.maxWidth = 10000
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter.filter(newText)
                adapter!!.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filter.filter(query)
                adapter!!.notifyDataSetChanged()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            adapter!!.filter.filter(query)
            adapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        /**
         * The argument representing ID of the category whose dictionary is viewed.
         */
        val ARG_CATEGORY_ID = "category_id"
    }
}
