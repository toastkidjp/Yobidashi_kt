package jp.toastkid.yobidashi.libs.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.customtabs.CustomTabsIntent

import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Common [android.content.Intent] factory.

 * @author toastkidjp
 */
object IntentFactory {

    /**
     * Make sharing message intent.
     * @param message
     * *
     * @return Intent
     */
    fun makeShare(message: String): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        return intent
    }

    /**
     * Make intent of sharing with twitter.
     * @return CustomTabsIntent
     */
    fun makeTwitter(
            context: Context,
            pair: ColorPair,
            @DrawableRes iconId: Int
    ): CustomTabsIntent {
        return CustomTabsFactory.make(context, pair.bgColor(), pair.fontColor(), iconId).build()
    }

    /**
     * Make pick image intent.
     * @return Intent
     */
    fun makePickImage(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        return intent
    }

    /**
     * Make launching calendar intent.
     * @param eventStartMs
     * *
     * @return Intent
     */
    fun makeCalendar(eventStartMs: Long): Intent {
        val intent = Intent(Intent.ACTION_EDIT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra("beginTime", eventStartMs)
        intent.putExtra("endTime", eventStartMs)
        return intent
    }

    /**
     * Make launching Google Play intent.
     * @param packageName
     * *
     * @return Google play intent.
     */
    fun googlePlay(packageName: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=" + packageName)
        return intent
    }

    /**
     * Share image uri.
     * @param uri
     * *
     * @return
     */
    fun shareImage(uri: Uri): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "image/*")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        return intent
    }

    /**
     * Make opening browser intent.
     * @param uri
     * *
     * @return
     */
    fun openBrowser(uri: Uri): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = uri
        return intent
    }
}
