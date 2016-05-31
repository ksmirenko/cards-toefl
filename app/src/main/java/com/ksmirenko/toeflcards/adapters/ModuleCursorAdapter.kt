package com.ksmirenko.toeflcards.adapters

import android.content.Context
import android.database.CharArrayBuffer
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ksmirenko.toeflcards.FlexiDatabase
import com.ksmirenko.toeflcards.R
import kotlinx.android.synthetic.main.listview_item_modules.view.*

/**
 * CursorAdapter for module selection.
 *
 * @author Kirill Smirenko
 */
class ModuleCursorAdapter(context : Context, cursor : Cursor?) : CursorAdapter(context, cursor, 0) {
    companion object {
        private val NAME_BUFFER_SIZE = 256
    }

    override fun newView(context : Context, cursor : Cursor, parent : ViewGroup) : View {
        val view = LayoutInflater.from(context).inflate(R.layout.listview_item_modules, parent, false)
        view.tag = ModuleListItemViewHolder(
            view.icon_catscr_module as ImageView,
            view.textview_catscr_module_name as TextView,
            CharArrayBuffer(NAME_BUFFER_SIZE)
        )
        return view
    }

    override fun bindView(view : View, context : Context, cursor : Cursor) {
        val holder = view.tag as ModuleListItemViewHolder;
        cursor.copyStringToBuffer(FlexiDatabase.ModuleQuery.COLUMN_INDEX_NAME, holder.nameBuffer)
        holder.nameView.setText(holder.nameBuffer.data, 0, holder.nameBuffer.sizeCopied)
        // set an icon, if available for this module
        try {
            holder.iconView.setImageResource(iconResIds[cursor.getInt(0)])
        }
        catch (e : IndexOutOfBoundsException) {
        }
    }

    /**
     * Icons for modules of 'Cards for the TOEFL test'
     */
    private val iconResIds = arrayOf(
        R.drawable.ic_module_advanced, // default for index 0
        R.drawable.ic_module_advanced,
        R.drawable.ic_module_advanced,
        R.drawable.ic_module_advanced,
        R.drawable.ic_module_anthropology,
        R.drawable.ic_module_art,
        R.drawable.ic_module_disaster,
        R.drawable.ic_module_employment,
        R.drawable.ic_module_entertainment,
        R.drawable.ic_module_evolution,
        R.drawable.ic_module_expertise,
        R.drawable.ic_module_family_relationships,
        R.drawable.ic_module_fashion,
        R.drawable.ic_module_finance,
        R.drawable.ic_module_food_crops,
        R.drawable.ic_module_friendship,
        R.drawable.ic_module_fuel,
        R.drawable.ic_module_history,
        R.drawable.ic_module_illness,
        R.drawable.ic_module_memory,
        R.drawable.ic_module_military_operations,
        R.drawable.ic_module_negative_emotions,
        R.drawable.ic_module_paranormal,
        R.drawable.ic_module_passion,
        R.drawable.ic_module_personal_property,
        R.drawable.ic_module_politics,
        R.drawable.ic_module_rebels,
        R.drawable.ic_module_scientific_terms,
        R.drawable.ic_module_social_inequality,
        R.drawable.ic_module_spirituality,
        R.drawable.ic_module_trade,
        R.drawable.ic_module_wealth,
        R.drawable.ic_module_word
    )

    private data class ModuleListItemViewHolder(
        var iconView : ImageView,
        var nameView : TextView,
        var nameBuffer : CharArrayBuffer
    ) {}
}