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
            override fun getCount(): Int = searchCategories.size

            override fun getItem(position: Int): SearchCategory = searchCategories[position]

            override fun getItemId(position: Int): Long = searchCategories[position].id.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val searchCategory = searchCategories[position]

                val view = inflater.inflate(LAYOUT_ID, parent, false)
                view.findViewById<ImageView>(R.id.search_category_image)
                        .setImageDrawable(AppCompatResources.getDrawable(context, searchCategory.iconId))
                view.findViewById<TextView>(R.id.search_category_text)
                        .setText(searchCategory.id)
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
