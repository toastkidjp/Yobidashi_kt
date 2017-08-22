package jp.toastkid.jitte.libs

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView
import android.widget.Toast

import jp.toastkid.jitte.libs.preference.ColorPair

/**
 * Simple toasting utilities.

 * @author toastkidjp
 */
object Toaster {

    /**
     * Short toasting.
     * @param context
     * *
     * @param messageId
     */
    fun tShort(
            context: Context,
            @StringRes messageId: Int
    ) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
    }

    /**
     * Short toasting.
     * @param context
     * *
     * @param message
     */
    fun tShort(
            context: Context,
            message: String
    ) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show simple snackbar on short time.

     * @param view
     * *
     * @param messageId
     * *
     * @param pair
     */
    fun snackShort(
            view: View,
            @StringRes messageId: Int,
            pair: ColorPair
    ): Snackbar {
        return snackShort(view, view.context.getString(messageId), pair)
    }

    /**
     * Show simple snackbar on short time.

     * @param view
     * *
     * @param message
     * *
     * @param pair
     */
    fun snackShort(
            view: View,
            message: String,
            pair: ColorPair
    ): Snackbar {
        return snack(view, message, pair, Snackbar.LENGTH_SHORT)
    }

    /**
     * Show simple snackbar on indefinite duration.
     * @param view
     * *
     * @param message
     * *
     * @param pair
     */
    fun snackIndefinite(
            view: View,
            message: String,
            pair: ColorPair
    ): Snackbar {
        return snack(view, message, pair, Snackbar.LENGTH_INDEFINITE)
    }

    /**
     * Show simple snackbar on specified duration.

     * @param view
     * *
     * @param message
     * *
     * @param pair
     * *
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
        (snackbarView.findViewById(android.support.design.R.id.snackbar_text) as TextView)
                .setTextColor(pair.fontColor())
        snackbar.show()
        return snackbar
    }

    /**
     * Show snackbar on long time.
     * @param view
     * *
     * @param message
     * *
     * @param actionTextId
     * *
     * @param action
     * *
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
                actionTextId,
                action,
                pair,
                Snackbar.LENGTH_LONG
        )
    }

    /**
     * Show snackbar on long time.
     * @param view
     * *
     * @param messageId
     * *
     * @param actionTextId
     * *
     * @param action
     * *
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
                actionTextId,
                action,
                pair,
                Snackbar.LENGTH_LONG
        )
    }

    /**
     * Show snackbar on long time.
     * @param view
     * *
     * @param messageId
     * *
     * @param actionTextId
     * *
     * @param action
     * *
     * @param pair
     */
    fun withAction(
            view: View,
            messageId: String,
            @StringRes actionTextId: Int,
            action: View.OnClickListener,
            pair: ColorPair
    ): Snackbar {
        return withAction(view, messageId, actionTextId, action, pair, Snackbar.LENGTH_INDEFINITE)
    }

    /**
     * Show snackbar on long time.
     * @param view
     * *
     * @param messageId
     * *
     * @param actionTextId
     * *
     * @param action
     * *
     * @param pair
     */
    private fun withAction(
            view: View,
            messageId: String,
            @StringRes actionTextId: Int,
            action: View.OnClickListener,
            pair: ColorPair,
            duration: Int
    ): Snackbar {
        val snackbar = Snackbar.make(view, messageId, duration)
        snackbar.setAction(actionTextId, action)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(pair.bgColor())
        (snackbarView.findViewById(android.support.design.R.id.snackbar_text) as TextView)
                .setTextColor(pair.fontColor())
        (snackbarView.findViewById(android.support.design.R.id.snackbar_action) as TextView)
                .setTextColor(pair.fontColor())
        snackbar.show()
        return snackbar
    }


}
