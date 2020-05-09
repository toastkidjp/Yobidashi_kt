package jp.toastkid.yobidashi.settings.color

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.databinding.FragmentSettingsColorBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier
import timber.log.Timber

/**
 * Color setting activity.
 *
 * TODO clean up with ViewModel.
 *
 * @author toastkidjp
 */
class ColorSettingFragment : Fragment(),
        CommonFragmentAction,
        TitleIdSupplier,
        ClearColorsDialogFragment.Callback {

    /**
     * Initial background color.
     */
    private var initialBgColor: Int = 0

    /**
     * Initial font color.
     */
    private var initialFontColor: Int = 0

    /**
     * Data-Binding object.
     */
    private var binding: FragmentSettingsColorBinding? = null

    /**
     * Saved color's adapter.
     */
    private var adapter: SavedColorAdapter? = null

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var repository: SavedColorRepository

    /**
     * Subscribed disposables.
     */
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_settings_color,
                container,
                false
        )
        binding?.fragment = this

        val context = context ?: return null
        preferenceApplier = PreferenceApplier(context)

        setHasOptionsMenu(true)

        return binding?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorPair = colorPair()

        initPalettes()
        initSavedColors()

        initialBgColor = colorPair.bgColor()
        binding?.settingsColorPrev?.setBackgroundColor(initialBgColor)
        binding?.backgroundPalette?.color = initialBgColor

        initialFontColor = colorPair.fontColor()
        binding?.settingsColorPrev?.setTextColor(initialFontColor)
        binding?.fontPalette?.color = initialFontColor
    }

    /**
     * Initialize background and font palettes.
     */
    private fun initPalettes() {
        binding?.backgroundPalette?.also {
            it.addSVBar(binding?.backgroundSvbar)
            it.addOpacityBar(binding?.backgroundOpacitybar)
            it.setOnColorChangedListener { color ->
                binding?.settingsColorOk?.setBackgroundColor(color)
            }
        }

        binding?.fontPalette?.also {
            it.addSVBar(binding?.fontSvbar)
            it.addOpacityBar(binding?.fontOpacitybar)
            it.setOnColorChangedListener { color ->
                binding?.settingsColorOk?.setTextColor(color)
            }
        }

        refresh()
    }

    /**
     * Initialize saved color's section.
     */
    private fun initSavedColors() {
        val activityContext = context ?: return

        repository = DatabaseFinder().invoke(activityContext).savedColorRepository()

        adapter = SavedColorAdapter(repository)
        binding?.savedColors?.adapter = adapter
        binding?.savedColors?.layoutManager =
                LinearLayoutManager(activityContext, LinearLayoutManager.HORIZONTAL, false)
        binding?.clearSavedColor?.setOnClickListener{
            val fragmentManager = fragmentManager ?: return@setOnClickListener
            val clearColorsDialogFragment = ClearColorsDialogFragment()
            clearColorsDialogFragment.setTargetFragment(this, 1)
            clearColorsDialogFragment.show(
                    fragmentManager,
                    ClearColorsDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onClickClearColor() {
        adapter?.clear()
    }

    /**
     * Bind value and action to holder's view.
     *
     * @param holder Holder
     * @param color  [SavedColor] object
     */
    private fun bindView(holder: SavedColorHolder, color: SavedColor) {
        color.setTo(holder.textView)
        holder.textView.setOnClickListener { commitNewColor(color.bgColor, color.fontColor) }
        holder.remove.setOnClickListener {
            Completable.fromAction {
                repository.delete(color)
                adapter?.deleteAt(color)
            }
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe(
                            { },
                            Timber::e
                    )
                    ?.addTo(disposables)
            snackShort(R.string.settings_color_delete)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Refresh with current color.
     */
    private fun refresh() {
        binding?.settingsColorOk?.also { colorPair().setTo(it) }
        adapter?.refresh()
    }

    /**
     * OK button's action.
     */
    fun ok() {
        val bgColor = binding?.backgroundPalette?.color ?: Color.BLACK
        val fontColor = binding?.fontPalette?.color ?: Color.WHITE

        commitNewColor(bgColor, fontColor)

        Completable.fromAction {
            val savedColor = SavedColor.make(bgColor, fontColor)
            repository.add(savedColor)
            adapter?.add(savedColor)
        }
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        { adapter?.notifyDataSetChanged() },
                        Timber::e
                )
                ?.addTo(disposables)
    }

    /**
     * Commit new color.
     *
     * @param bgColor   Background color int
     * @param fontColor Font color int
     */
    private fun commitNewColor(bgColor: Int, fontColor: Int) {
        preferenceApplier.color = bgColor
        preferenceApplier.fontColor = fontColor

        refresh()

        binding?.backgroundPalette?.color = bgColor
        binding?.fontPalette?.color = fontColor
        activity?.let { Updater.update(it) }

        snackShort(R.string.settings_color_done_commit)
    }

    /**
     * Reset button's action.
     */
    fun reset() {
        preferenceApplier.color = initialBgColor
        preferenceApplier.fontColor = initialFontColor

        refresh()
        activity?.let { Updater.update(it) }
        snackShort(R.string.settings_color_done_reset)
    }

    override fun titleId(): Int = R.string.title_settings_color

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.color_setting_toolbar_menu, menu)

        menu.let { menuNonNull ->
            menuNonNull.findItem(R.id.color_settings_toolbar_menu_add_recommend)
                    ?.setOnMenuItemClickListener {
                        val fragmentManager = fragmentManager ?: return@setOnMenuItemClickListener false
                        RecommendColorDialogFragment().show(
                                fragmentManager,
                                RecommendColorDialogFragment::class.java.simpleName
                        )
                        true
            }
            menuNonNull.findItem(R.id.color_settings_toolbar_menu_add_random)
                    ?.setOnMenuItemClickListener {
                        val activityContext = context ?: return@setOnMenuItemClickListener true
                        RandomColorInsertion()(activityContext).addTo(disposables)
                        snackShort(R.string.done_addition)
                        true
                    }
        }
    }

    private fun snackShort(@StringRes messageId: Int) {
        binding?.root?.let {
            Toaster.snackShort(it, messageId, colorPair())
        }
    }

    /**
     * Saved color's adapter.
     */
    private inner class SavedColorAdapter(private val repository: SavedColorRepository)
        : RecyclerView.Adapter<SavedColorHolder>() {

        private val items = mutableListOf<SavedColor>()

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): SavedColorHolder {
            val inflater = LayoutInflater.from(context)
            return SavedColorHolder(inflater.inflate(R.layout.saved_color, parent, false))
        }

        override fun onBindViewHolder(holder: SavedColorHolder, position: Int) {
            bindView(holder, items.get(position))
        }

        override fun getItemCount(): Int = items.count()

        fun refresh(): Disposable {
            items.clear()
            return Maybe.fromCallable { repository.findAll() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                items.addAll(it)
                                notifyDataSetChanged()
                            },
                            Timber::e
                    )
        }

        fun deleteAt(savedColor: SavedColor): Disposable {
            return delete(savedColor)
        }

        private fun delete(favoriteSearch: SavedColor): Disposable {
            return Completable.fromAction {
                repository.delete(favoriteSearch)
                items.remove(favoriteSearch)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { notifyDataSetChanged() },
                            Timber::e
                    )
        }

        fun add(savedColor: SavedColor) {
            items.add(savedColor)
        }

        fun clear() {
            Completable.fromAction {
                repository.deleteAll()
                items.clear()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                notifyDataSetChanged()
                                val root = binding?.root ?: return@subscribe
                                Toaster.snackShort(
                                        root,
                                        R.string.settings_color_delete,
                                        colorPair()
                                )
                            },
                            Timber::e
                    )
                    .addTo(disposables)
        }
    }

    private fun colorPair() = preferenceApplier.colorPair()

    override fun onDetach() {
        disposables.clear()
        super.onDetach()
    }

}
