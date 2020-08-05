package jp.toastkid.yobidashi.libs

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.lib.preference.ColorPair

/**
 * Simple toasting utilities.
 *
 * @author toastkidjp
 */
object Toaster {

    /**
     * Short toasting.
     *
     * @param context
     * @param messageId
     */
    fun tShort(context: Context, @StringRes messageId: Int) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
    }

    /**
     * Short toasting.
     *
     * @param context
     * @param message
     */
    fun tShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show simple snackbar on short time.
     *
     * @param view
     * @param messageId
     * @param pair
     */
    fun snackShort(
            view: View,
            @StringRes messageId: Int,
            pair: ColorPair
    ): Snackbar = snackShort(view, view.context.getString(messageId), pair)

    /**
     * Show simple snackbar on short time.
     *
     * @param view
     * @param message
     * @param pair
     */
    fun snackShort(
            view: View,
            message: String,
            pair: ColorPair
    ): Snackbar = snack(view, message, pair, Snackbar.LENGTH_SHORT)

    /**
     * Show simple snackbar on indefinite duration.
     *
     * @param view
     * @param message
     * @param pair
     */
    fun snackIndefinite(
            view: View,
            message: String,
            pair: ColorPair
    ): Snackbar = snack(view, message, pair, Snackbar.LENGTH_INDEFINITE)

    /**
     * Show simple snackbar on specified duration.
     *
     * @param view
     * @param message
     * @param pair
     * @param duration
     */
    fun snack(
            view: View,
            message: String,
            pair: ColorPair,
            duration: Int
    ): Snackbar {
        val snackbar = Snackbar.make(view, message, duration)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(pair.bgColor())
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(pair.fontColor())
        snackbar.show()
        return snackbar
    }

    /**
     * Show snackbar on long time.
     *
     * @param view
     * @param message
     * @param actionTextId
     * @param action
     * @param pair
     */
    fun snackLong(
            view: View,
            message: String,
            @StringRes actionTextId: Int,
            action: View.OnClickListener,
            pair: ColorPair
    ): Snackbar {
        return withAction(
                view,
                message,
                view.context.getString(actionTextId),
                action,
                pair,
                Snackbar.LENGTH_LONG
        )
    }

    /**
     * Show snackbar on long time.
     *
     * @param view
     * @param messageId
     * @param actionTextId
     * @param action
     * @param pair
     */
    fun snackLong(
            view: View,
            @StringRes messageId: Int,
            @StringRes actionTextId: Int,
            action: View.OnClickListener,
            pair: ColorPair
    ): Snackbar {
        return withAction(
                view,
                view.context.getString(messageId),
                view.context.getString(actionTextId),
                action,
                pair,
                Snackbar.LENGTH_LONG
        )
    }

    /**
     * Show snackbar on long time.
     *
     * @param view
     * @param messageId
     * @param actionTextId
     * @param action
     * @param pair
     * @param duration default = Snackbar.LENGTH_INDEFINITE
     */
    fun withAction(
            view: View,
            @StringRes messageId: Int,
            @StringRes actionTextId: Int,
            action: View.OnClickListener,
            pair: ColorPair,
            duration: Int = Snackbar.LENGTH_INDEFINITE
    ) = withAction(view, view.context.getString(messageId), view.context.getString(actionTextId), action, pair, duration)

    /**
     * Show snackbar on long time.
     *
     * @param view
     * @param message
     * @param actionText
     * @param action
     * @param pair
     * @param duration default = Snackbar.LENGTH_INDEFINITE
     */
    fun withAction(
            view: View,
            message: String,
            @StringRes actionTextId: Int,
            action: View.OnClickListener,
            pair: ColorPair,
            duration: Int = Snackbar.LENGTH_INDEFINITE
    ) = withAction(view, message, view.context.getString(actionTextId), action, pair, duration)

    /**
     * Show snackbar on long time.
     *
     * @param view
     * @param message
     * @param actionText
     * @param action
     * @param pair
     * @param duration default = Snackbar.LENGTH_INDEFINITE
     */
    fun withAction(
            view: View,
            message: String,
            actionText: String,
            action: View.OnClickListener,
            pair: ColorPair,
            duration: Int = Snackbar.LENGTH_INDEFINITE
    ): Snackbar {
        val snackbar = Snackbar.make(view, message, duration)
        snackbar.setAction(actionText, action)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(pair.bgColor())
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(pair.fontColor())
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
                .setTextColor(pair.fontColor())
        snackbar.show()
        return snackbar
    }

}