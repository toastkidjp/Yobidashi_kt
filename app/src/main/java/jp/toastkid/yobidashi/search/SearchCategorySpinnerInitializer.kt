package jp.toastkid.yobidashi.search

import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */

object SearchCategorySpinnerInitializer {

    fun initialize(spinner: Spinner) {
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return SearchCategory.values().size
            }

            override fun getItem(position: Int): Any {
                return SearchCategory.values()[position]
            }

            override fun getItemId(position: Int): Long {
                return SearchCategory.values()[position].id.toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val searchCategory = SearchCategory.values()[position]

                val context = spinner.context
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.item_spinner_search_category, parent, false)
                val imageView = view.findViewById(R.id.search_category_image) as ImageView
                imageView.setImageDrawable(AppCompatResources.getDrawable(context, searchCategory.iconId))
                val textView = view.findViewById(R.id.search_category_text) as TextView
                textView.setText(searchCategory.id)
                return view
            }
        }
        spinner.adapter = adapter
        spinner.setSelection(SearchCategory.findIndex(
                PreferenceApplier(spinner.context).getDefaultSearchEngine()))
    }

}
