package jp.toastkid.yobidashi.calendar

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.databinding.FragmentCalendarBinding

/**
 * Calendar fragment.
 *
 * @author toastkidjp
 */
class CalendarFragment : BaseFragment() {

    /** Data binding object.  */
    private var binding: FragmentCalendarBinding? = null

    /** Analytics logger wrapper.  */
    private var logSender: LogSender? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logSender = LogSender(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentCalendarBinding>(
                inflater, R.layout.fragment_calendar, container, false)
        initCalendarView()
        return binding?.root
    }

    /**
     * Initialize calendar view.
     */
    private fun initCalendarView() {
        binding?.calendar?.date = System.currentTimeMillis()
        binding?.calendar?.setOnDateChangeListener{ view, year, month, dayOfMonth ->
            val fragmentActivity = activity ?: return@setOnDateChangeListener
            startActivity(DateDetailActivity.makeIntent(fragmentActivity, year, month, dayOfMonth))
        }
    }

    @StringRes
    override fun titleId(): Int = R.string.title_calendar
}
