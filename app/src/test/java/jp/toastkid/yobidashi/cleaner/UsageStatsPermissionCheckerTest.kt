package jp.toastkid.yobidashi.cleaner

import android.app.AppOpsManager
import android.os.Process
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class UsageStatsPermissionCheckerTest {

    @Test
    fun test() {
        val usageStatsPermissionChecker = UsageStatsPermissionChecker()
        mockkStatic(android.os.Process::class)
        every { Process.myUid() }.answers { 2 }

        val appOps = mockk<AppOpsManager>()
        every { appOps.checkOpNoThrow(any(), any(), any()) }.answers { 0 }

        usageStatsPermissionChecker.invoke(appOps, "jp.toastkid.test")

        verify(exactly = 1) { Process.myUid() }
        verify(exactly = 1) { appOps.checkOpNoThrow(any(), any(), any()) }
    }

}