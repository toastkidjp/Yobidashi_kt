package jp.toastkid.yobidashi.home

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Menu's adapter.
 *
 * Initialize with context and menu click event consumer.
 *
 * @param context
 * @param consumer
 *
 * @author toastkidjp
 */
internal class Adapter(context: Context, consumer: Consumer<Menu>)
    : RecyclerView.Adapter<ViewHolder>() {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Menu.
     */
    private val menus: Array<Menu> = Menu.values()

    /**
     *  Color pair.
     */
    private val colorPair: ColorPair

    /**
     * Menu action subject.
     */
    private val menuSubject: PublishSubject<Menu>

    /**
     * Subscription disposable.
     */
    private val disposable: Disposable?

    init {
        val preferenceApplier = PreferenceApplier(context)
        colorPair = preferenceApplier.colorPair()
        menuSubject = PublishSubject.create<Menu>()
        disposable = menuSubject.subscribe(consumer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                    DataBindingUtil.inflate(inflater, LAYOUT_ID, parent, false)
            )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menus[position % menus.size]
        holder.setColorPair(colorPair)
        holder.setText(menu.titleId)
        holder.setImage(menu.iconId)
        holder.setOnClick(View.OnClickListener { menuSubject.onNext(menu) })
    }

    override fun getItemCount(): Int = MAXIMUM

    /**
     * Dispose subscription.
     */
    fun dispose() {
        disposable?.dispose()
    }

    companion object {

        /**
         * Layout ID.
         */
        private const val LAYOUT_ID = R.layout.item_home_menu

        /**
         * Maximum length of menus.
         */
        private val MAXIMUM = Menu.values().size * 20

        /**
         * Medium position of menus.
         */
        private val MEDIUM = MAXIMUM / 2

        /**
         * Return medium position of menus.
         * @return MEDIUM
         */
        fun mediumPosition(): Int = MEDIUM
    }
}
