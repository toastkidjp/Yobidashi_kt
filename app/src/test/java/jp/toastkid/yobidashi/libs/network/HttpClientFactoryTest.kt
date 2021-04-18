package jp.toastkid.yobidashi.libs.network

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class HttpClientFactoryTest {

    @Test
    fun test() {
        val client = HttpClientFactory.withTimeout(1)
        assertEquals(1000, client.connectTimeoutMillis)
        assertEquals(1000, client.readTimeoutMillis)
        assertEquals(1000, client.writeTimeoutMillis)
    }

}