package jp.toastkid.yobidashi.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Initializer of search category selector.
 *
 * @author toastkidjp
 */
object SearchCategorySpinnerInitializer {

    operator fun invoke(spinner: Spinner, category: SearchCategory? = null) {
        spinner.adapter = makeBaseAdapter(spinner)
        spinner.setSelection(findIndex(category, spinner))
    }

    private fun makeBaseAdapter(spinner: Spinner): BaseAdapter {
        val searchCategories = SearchCategory.values()

        return object : BaseAdapter() {
            override fun getCount(): Int = searchCategories.size

            override fun getItem(position: Int): SearchCategory = searchCategories[position]

            override fun getItemId(position: Int): Long = searchCategories[position].id.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val searchCategory = searchCategories[position]

                val context = spinner.context
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.item_spinner_search_category, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.search_category_image)
                imageView.setImageDrawable(AppCompatResources.getDrawable(context, searchCategory.iconId))
                val textView = view.findViewById<TextView>(R.id.search_category_text)
                textView.setText(searchCategory.id)
                return view
            }
        }
    }


    private fun findIndex(category: SearchCategory?, spinner: Spinner): Int =
            SearchCategory.findIndex(
                    category?.name
                            ?: PreferenceApplier(spinner.context).getDefaultSearchEngine()
            )

}
