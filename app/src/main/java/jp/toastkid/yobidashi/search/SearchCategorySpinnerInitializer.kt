package jp.toastkid.yobidashi.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Initializer of search category selector.
 *
 * @author toastkidjp
 */
object SearchCategorySpinnerInitializer {

    @LayoutRes
    private const val LAYOUT_ID = R.layout.item_spinner_search_category

    operator fun invoke(spinner: Spinner, category: SearchCategory? = null) {
        spinner.adapter = makeBaseAdapter(spinner.context)
        spinner.setSelection(findIndex(category, spinner))
    }

    private fun makeBaseAdapter(context: Context): BaseAdapter {
        val inflater = LayoutInflater.from(context)

        val searchCategories = SearchCategory.values()

        return object : BaseAdapter() {
            inner class ViewHolder(val icon: ImageView, val text: TextView)

            override fun getCount(): Int = searchCategories.size

            override fun getItem(position: Int): SearchCategory = searchCategories[position]

            override fun getItemId(position: Int): Long = searchCategories[position].id.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val searchCategory = searchCategories[position]

                if (convertView == null) {
                    val view = inflater.inflate(LAYOUT_ID, parent, false)
                    val icon = view.findViewById<ImageView>(R.id.search_category_image)
                    val text = view.findViewById<TextView>(R.id.search_category_text)
                    val viewHolder = ViewHolder(icon, text)
                    view.tag = viewHolder
                    bindItem(viewHolder, searchCategory)
                    return view
                }

                val viewHolder = (convertView.tag as ViewHolder)
                bindItem(viewHolder, searchCategory)
                return convertView
            }

            private fun bindItem(viewHolder: ViewHolder, searchCategory: SearchCategory) {
                viewHolder.icon
                        .setImageDrawable(AppCompatResources.getDrawable(context, searchCategory.iconId))
                viewHolder.text.setText(searchCategory.id)
            }
        }
    }


    private fun findIndex(category: SearchCategory?, spinner: Spinner): Int =
            SearchCategory.findIndex(
                    category?.name
                            ?: PreferenceApplier(spinner.context).getDefaultSearchEngine()
            )

}
