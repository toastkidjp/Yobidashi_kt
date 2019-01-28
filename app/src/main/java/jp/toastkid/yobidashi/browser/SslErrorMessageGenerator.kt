package jp.toastkid.yobidashi.browser

import android.content.Context
import android.net.http.SslCertificate
import android.net.http.SslError
import jp.toastkid.yobidashi.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author toastkidjp
 */
internal object SslErrorMessageGenerator {

    private val dateFormatHolder: ThreadLocal<DateFormat> = object : ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat
                = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    }

    fun generate(context: Context, error: SslError?): String {
        if (error == null) {
            return ""
        }
        val cert: SslCertificate = error.getCertificate()
        val dateFormat = dateFormatHolder.get()

        val firstLine = context.getString(R.string.message_ssl_error_first_line)

        val cause = when (error.getPrimaryError()) {
            SslError.SSL_EXPIRED ->
                context.getString(R.string.message_ssl_error_expired) + dateFormat.format(cert.validNotAfterDate)
            SslError.SSL_IDMISMATCH ->
                context.getString(R.string.message_ssl_error_id_mismatch) + cert.issuedTo.cName
            SslError.SSL_NOTYETVALID ->
                context.getString(R.string.message_ssl_error_not_yet_valid) + dateFormat.format(cert.validNotBeforeDate)
            SslError.SSL_UNTRUSTED ->
                context.getString(R.string.message_ssl_error_untrusted) + cert.issuedBy.dName
            else ->
                context.getString(R.string.message_ssl_error_unknown)
        }
        return firstLine + cause
    }
}