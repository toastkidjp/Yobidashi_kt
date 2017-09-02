package jp.toastkid.jitte.browser.bookmark

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
import jp.toastkid.jitte.BaseActivity
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ActivityBookmarkBinding
import jp.toastkid.jitte.libs.ImageLoader
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.db.DbInitter

/**
 * @author toastkidjp
 */
class BookmarkActivity: BaseActivity() {

    private lateinit var binding: ActivityBookmarkBinding

    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityBookmarkBinding>(this, LAYOUT_ID)
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
        }
        return super.clickMenu(item)
    }

    override fun titleId(): Int = R.string.title_bookmark

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
    }

    companion object {
        @LayoutRes const val LAYOUT_ID: Int = R.layout.activity_bookmark

        /** Request code. */
        const val REQUEST_CODE: Int = 202

        fun makeIntent(context: Context): Intent {
            val intent: Intent = Intent(context, BookmarkActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}