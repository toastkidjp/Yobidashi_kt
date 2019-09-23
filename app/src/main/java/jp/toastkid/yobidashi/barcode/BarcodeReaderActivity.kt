package jp.toastkid.yobidashi.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import jp.toastkid.yobidashi.R

/**
 * Barcode reader activity.
 *
 * @author toastkidjp
 */
class BarcodeReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
                R.id.content,
                BarcodeReaderFragment(),
                BarcodeReaderFragment::class.java.simpleName
        )
        transaction.commitAllowingStateLoss()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_barcode_reader

        /**
         * Make this activity's intent.
         *
         * @param context [Context]
         * @return [Intent]
         */
        fun makeIntent(context: Context) =
                Intent(context, BarcodeReaderActivity::class.java)
                        .also { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }
}
