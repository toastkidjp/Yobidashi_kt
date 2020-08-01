package jp.toastkid.lib.permission

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class RuntimePermissionsTest {

    @MockK
    private lateinit var fragment: RuntimePermissionProcessorFragment

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        mockkObject(RuntimePermissionProcessorFragment)
        every { RuntimePermissionProcessorFragment.obtainFragment(any()) }.answers { fragment }
        every { fragment.request(any()) }.answers { mockk() }

        val runtimePermissions = RuntimePermissions(mockk())
        runtimePermissions.request("test")

        verify(exactly = 1) { fragment.request("test") }
    }
}