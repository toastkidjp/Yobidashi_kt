package jp.toastkid.yobidashi.calendar

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.databinding.FragmentCalendarBinding
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.main.MainActivity

/**
 * Calendar fragment.

 * @author toastkidjp
 */
class CalendarFragment : BaseFragment() {

    /** Data binding object.  */
    private var binding: FragmentCalendarBinding? = null

    /** Analytics logger wrapper.  */
    private var logSender: LogSender? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        logSender = LogSender(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentCalendarBinding>(inflater!!, R.layout.fragment_calendar, container, false)
        initCalendarView()
        return binding!!.root
    }

    /**
     * Initialize calendar view.
     */
    private fun initCalendarView() {
        binding!!.calendar.date = System.currentTimeMillis()
        binding!!.calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val context = context
            val dateTitle = DateTitleFactory.makeDateTitle(context, month, dayOfMonth)
            AlertDialog.Builder(context)
                    .setTitle(dateTitle)
                    .setItems(R.array.calendar_menu) { d, index ->
                        val bundle = Bundle()
                        bundle.putString("daily", dateTitle)
                        if (index == 0) {
                            logSender!!.send("cal_wkp", bundle)
                            CalendarArticleLinker(context, month, dayOfMonth).invoke()
                            return@new AlertDialog.Builder(context)
                                    .setTitle(dateTitle)
                                    .setItems
                        }
                        if (index == 1) {
                            logSender!!.send("cal_schdl", bundle)
                            startActivity(IntentFactory.makeCalendar(view.date))
                            return@new AlertDialog.Builder(context)
                                    .setTitle(dateTitle)
                                    .setItems
                        }
                        if (index == 2) {
                            logSender!!.send("cal_srch", bundle)
                            startActivity(MainActivity.makeSearchIntent(context, dateTitle))
                        }
                    }
                    .setCancelable(true)
                    .setOnCancelListener { v -> logSender!!.send("cal_x") }
                    .setPositiveButton(R.string.close) { d, i -> d.dismiss() }
                    .show()
        }
    }

    @StringRes
    override fun titleId(): Int {
        return R.string.title_calendar
    }
}
