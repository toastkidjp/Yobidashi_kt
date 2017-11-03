package jp.toastkid.yobidashi.browser.bookmark

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.view.MenuItem
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBookmarkBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller

/**
 * Bookmark list activity.
 *
 * @author toastkidjp
 */
class BookmarkActivity: BaseActivity() {

    /**
     * Data binding object.
     */
    private lateinit var binding: ActivityBookmarkBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        val relation = DbInitter.init(this).relationOfBookmark()

        binding.historiesView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> Toaster.snackShort(binding.root, history.title, colorPair()) }
        )
        binding.historiesView.adapter = adapter
        binding.historiesView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int) = false
        }
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        val fromPos = viewHolder.adapterPosition
                        val toPos = target.adapterPosition
                        adapter.notifyItemMoved(fromPos, toPos)
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        if (direction != ItemTouchHelper.RIGHT) {
                            return
                        }
                        adapter.removeAt(viewHolder.adapterPosition)
                    }
                }).attachToRecyclerView(binding.historiesView)

        initToolbar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.bookmark)

        adapter.showRoot()
    }

    /**
     * Finish this activity with result.
     *
     * @param uri
     */
    private fun finishWithResult(uri: Uri) {
        val intent = Intent()
        intent.setData(uri)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()

        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_clear_bookmark)
                        .setMessage(Html.fromHtml(getString(R.string.confirm_clear_all_settings)))
                        .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                        .setPositiveButton(R.string.ok) { d, i ->
                            adapter.clearAll{ Toaster.snackShort(binding.root, R.string.done_clear, colorPair())}
                            d.dismiss()
                        }
                        .setCancelable(true)
                        .show()
                return true
            }
            R.id.add_default -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_add_default_bookmark)
                        .setMessage(R.string.message_add_default_bookmark)
                        .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                        .setPositiveButton(R.string.ok) { d, i ->
                            BookmarkInitializer.invoke(this)
                            adapter.showRoot()
                            Toaster.snackShort(binding.root, R.string.done_addition, colorPair())
                            d.dismiss()
                        }
                        .setCancelable(true)
                        .show()
                return true
            }
            R.id.add_folder -> {
                val inputLayout = TextInputs.make(this)
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.title_dialog_input_file_name))
                        .setView(inputLayout)
                        .setCancelable(true)
                        .setPositiveButton(R.string.save) { d, i ->
                            inputLayout.editText?.text?.toString()?.let {
                                Completable.fromAction {
                                    BookmarkInsertion(
                                            this,
                                            it,
                                            parent = adapter.currentFolderName(),
                                            folder = true
                                    ).insert() }
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe { adapter.reload() }
                            }
                        }
                        .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                        .show()
            }
            R.id.to_top -> {
                RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
                return true
            }
            R.id.to_bottom -> {
                RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
                return true
            }
        }
        return super.clickMenu(item)
    }

    override fun onBackPressed() {
        if (adapter.back()) {
            return
        }
        super.onBackPressed()
    }

    override fun titleId(): Int = R.string.title_bookmark

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_bookmark

        /**
         * Request code.
         */
        const val REQUEST_CODE: Int = 202

        /**
         * Make launching intent.
         *
         * @param context [Context]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BookmarkActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}