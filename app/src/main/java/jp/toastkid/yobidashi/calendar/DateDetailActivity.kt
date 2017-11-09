package jp.toastkid.yobidashi.calendar

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.databinding.ActivityDateDetailBinding
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.search.SearchAction
import java.util.*

/**
 * Displaying date detail activity.
 *
 * @author toastkidjp
 */
class DateDetailActivity : BaseActivity() {

    /**
     * Data-Binding object.
     */
    private lateinit var binding: ActivityDateDetailBinding

    /**
     * Analytics logger wrapper.
     */
    private var logSender: LogSender? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityDateDetailBinding>(this, LAYOUT_ID)

        logSender = LogSender(this)

        if (binding.toolbar != null) {
            initToolbar(binding.toolbar as Toolbar)
        }

        initButtons()

        binding.background.setOnClickListener { finish() }
    }

    /**
     * Set onClick listeners.
     */
    private fun initButtons() {
        val month = intent.getIntExtra(KEY_MONTH, 0)
        val dayOfMonth = intent.getIntExtra(KEY_DAY_OF_MONTH, 1)
        val dateTitle = DateTitleFactory.makeDateTitle(this, month, dayOfMonth)
        val bundle = Bundle().apply { putString("daily", dateTitle) }

        binding.openArticle.setOnClickListener {
            logSender?.send("cal_wkp", bundle)
            CalendarArticleLinker(this, month, dayOfMonth).invoke()
            finish()
        }
        binding.addSchedule.setOnClickListener {
            logSender?.send("cal_schdl", bundle)
            val eventStartMs = GregorianCalendar(
                    intent.getIntExtra(KEY_YEAR, 2017), month, dayOfMonth).timeInMillis
            startActivity(IntentFactory.makeCalendar(eventStartMs))
        }
        binding.search.setOnClickListener {
            logSender!!.send("cal_srch", bundle)
            SearchAction(this, preferenceApplier.getDefaultSearchEngine(), dateTitle).invoke()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.toolbar?.let { applyColorToToolbar(it) }
    }

    override fun titleId(): Int = R.string.title_activity_date_detail

    companion object {

        /**
         * Layout ID.
         */
        private const val LAYOUT_ID = R.layout.activity_date_detail

        /**
         * Extra key of year.
         */
        private const val KEY_YEAR = "year"

        /**
         * Extra key of month.
         */
        private const val KEY_MONTH = "month"

        /**
         * Extra key of day-of-month.
         */
        private const val KEY_DAY_OF_MONTH = "dayOfMonth"

        /**
         * Make launcher intent.
         *
         * @param context
         * @param year
         * @param month 0-11
         * @param dayOfMonth
         *
         * @return [Intent]
         */
        fun makeIntent(context: Context, year: Int, month: Int, dayOfMonth: Int): Intent {
            val intent = Intent(context, DateDetailActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(KEY_YEAR, year)
            intent.putExtra(KEY_MONTH, month)
            intent.putExtra(KEY_DAY_OF_MONTH, dayOfMonth)
            return intent
        }
    }

}