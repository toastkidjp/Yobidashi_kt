package jp.toastkid.yobidashi.speed_dial

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSpeedDialMenuBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Menu's adapter.

 * @author toastkidjp
 */
internal class Adapter
/**
 * Initialize with context and menu click event consumer.

 * @param context
 * *
 * @param consumer
 */
(context: Context, consumer: Consumer<Menu>) : RecyclerView.Adapter<ViewHolder>() {

    /** Layout inflater.  */
    private val inflater: LayoutInflater

    /** Menu.  */
    private val menus: Array<Menu>

    /** Color pair.  */
    private val colorPair: ColorPair

    /** Menu action publisher.  */
    private val menuPublishProcessor: PublishProcessor<Menu>

    /** Subscription disposable.  */
    private val disposable: Disposable?

    init {
        inflater = LayoutInflater.from(context)
        menus = Menu.values()
        val preferenceApplier = PreferenceApplier(context)
        colorPair = preferenceApplier.colorPair()
        menuPublishProcessor = PublishProcessor.create<Menu>()
        disposable = menuPublishProcessor.subscribe(consumer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemSpeedDialMenuBinding>(inflater, LAYOUT_ID, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menus[position % menus.size]
        holder.setColorPair(colorPair)
        holder.setText(menu.titleId)
        holder.setImage(menu.iconId)
        holder.setOnClick(View.OnClickListener { v -> menuPublishProcessor.onNext(menu) })
    }

    override fun getItemCount(): Int {
        return MAXIMUM
    }

    /**
     * Dispose subscription.
     */
    fun dispose() {
        disposable?.dispose()
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.item_speed_dial_menu

        /** Maximum length of menus.  */
        private val MAXIMUM = Menu.values().size * 20

        /** Medium position of menus.  */
        private val MEDIUM = MAXIMUM / 2

        /**
         * Return medium position of menus.
         * @return
         */
        fun mediumPosition(): Int {
            return MEDIUM
        }
    }
}
