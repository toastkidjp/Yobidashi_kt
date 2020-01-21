package jp.toastkid.yobidashi.libs

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.UiThread
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection

/**
 * @author toastkidjp
 */
object ImageDownloader {

    /**
     * HTTP Client.
     */
    private val HTTP_CLIENT by lazy { HttpClientFactory.make() }

    private val bitmapCompressor = BitmapCompressor()

    /**
     * Store image to file.
     *
     * @param url URL string.
     * @param contextSupplier Supply [Context] on demand
     * @param consumer callback of subscription, this functions run on UI thread.
     */
    operator fun invoke(
            url: String,
            contextSupplier: () -> Context,
            @UiThread consumer: Consumer<File>
    ): Disposable {
        val context = contextSupplier()
        if (PreferenceApplier(context).wifiOnly
                && WifiConnectionChecker.isNotConnecting(context)
        ) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return Disposables.empty()
        }
        return Single.fromCallable { HTTP_CLIENT.newCall(Request.Builder().url(url).build()).execute() }
                .subscribeOn(Schedulers.io())
                .filter { it.code() == HttpURLConnection.HTTP_OK }
                .map {
                    val storeroom = FilesDir(context, BackgroundSettingActivity.BACKGROUND_DIR)
                    val file = storeroom.assignNewFile(Uri.parse(url))
                    bitmapCompressor(BitmapFactory.decodeStream(it.body()?.byteStream()), file)
                    file
                }
                .observeOn(Schedulers.computation())
                .subscribe(consumer, Consumer { Timber.e(it) })
    }
}