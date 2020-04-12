package jp.toastkid.yobidashi.tab

import android.net.Uri
import java.util.*

/**
 * Uri queue for opening on background.
 * Implements for enabling search on background.
 * TODO Delete it.
 * @author toastkidjp
 */
object BackgroundTabQueue {

    /**
     * Tab material's queue.
     */
    private val queue: Queue<Pair<String, Uri>> = ArrayDeque<Pair<String, Uri>>(10)

    /**
     * Add tab material.
     *
     * @param title Tab's title
     * @param uri Tab's Uri
     */
    fun add(title: String, uri: Uri) {
        queue.add(Pair<String, Uri>(title, uri))
    }

    /**
     * Iterate with specified consumer.
     *
     * @param consumer
     */
    fun iterate(consumer: (Pair<String, Uri>) -> Unit) {
        while (queue.isNotEmpty()) { consumer(queue.poll()) }
    }
}