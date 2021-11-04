package jp.toastkid.yobidashi.browser.tls

import android.content.Context
import android.net.http.SslCertificate
import android.net.http.SslError
import android.text.format.DateFormat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * @author toastkidjp
 */
class TlsErrorMessageGeneratorTest {

    @InjectMockKs
    private lateinit var tlsErrorMessageGenerator: TlsErrorMessageGenerator

    @MockK
    private lateinit var error: SslError

    @MockK
    private lateinit var cert: SslCertificate

    @MockK
    private lateinit var dName: SslCertificate.DName

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { error.certificate }.returns(cert)
        every { error.url }.returns("https://www.bad.com")
        every { cert.issuedTo }.returns(dName)
        every { dName.cName }.returns("c_name")
        every { cert.issuedBy }.returns(dName)
        every { dName.cName }.returns("d_name")
        every { cert.validNotAfterDate }.returns(Date())
        every { context.getString(any()) }.returns("test")

        mockkStatic(DateFormat::class)
        every { DateFormat.format(any(), any<Date>()) }.returns("2021/11/11")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNullCase() {
        assertTrue(
            tlsErrorMessageGenerator.invoke(context, null).isEmpty()
        )
    }

    @Test
    fun testSslExpiredCase() {
        every { error.primaryError }.returns(SslError.SSL_EXPIRED)

        tlsErrorMessageGenerator.invoke(context, error)

        verify(exactly = 1) { error.certificate }
        verify(exactly = 1) { error.url }
        verify(atLeast = 2) { context.getString(any()) }
    }

    @Test
    fun testIdMismatchCase() {
        every { error.primaryError }.returns(SslError.SSL_IDMISMATCH)

        tlsErrorMessageGenerator.invoke(context, error)

        verify(exactly = 1) { error.certificate }
        verify(exactly = 1) { error.url }
        verify(atLeast = 2) { context.getString(any()) }
        verify(exactly = 1) { cert.issuedTo }
    }

}