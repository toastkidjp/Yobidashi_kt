package jp.toastkid.yobidashi.torch

import com.journeyapps.barcodescanner.camera.CameraManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * @author toastkidjp
 */
class TorchTest {

    @Test
    fun testInstanceThrowing() {
        val cameraManager = mockk<CameraManager>()
        val torch = Torch(cameraManager) { assertTrue(true) }
        every { cameraManager.isOpen }.throws(RuntimeException())
        torch.switch()
        verify(atLeast = 1, atMost = 1) { cameraManager.isOpen }
        verify(atLeast = 0, atMost = 0) { cameraManager.open() }
    }

    @Test
    fun testNotCallCloseWhenTorchOff() {
        val cameraManager = mockk<CameraManager>()
        val torch = Torch(cameraManager) { fail() }
        every { cameraManager.isOpen }.returns(false)
        every { cameraManager.open() }.answers {  }
        every { cameraManager.setTorch(any()) }.answers {  }
        every { cameraManager.isTorchOn }.returns(true)
        torch.switch()
        verify(atLeast = 1, atMost = 1) { cameraManager.isOpen }
        verify(atLeast = 1, atMost = 1) { cameraManager.open() }
        verify(atLeast = 1, atMost = 1) { cameraManager.setTorch(any()) }
        verify(atLeast = 0, atMost = 0) { cameraManager.close() }
    }

    @Test
    fun testDisposeNotCallClose() {
        val cameraManager = mockk<CameraManager>()
        val torch = Torch(cameraManager) { fail() }
        every { cameraManager.isOpen }.returns(false)
        torch.dispose()
        verify(atLeast = 1, atMost = 1) { cameraManager.isOpen }
        verify(atLeast = 0, atMost = 0) { cameraManager.close() }
    }

    @Test
    fun testDispose() {
        val cameraManager = mockk<CameraManager>()
        val torch = Torch(cameraManager) { fail() }
        every { cameraManager.isOpen }.returns(true)
        every { cameraManager.close() }.answers {  }
        torch.dispose()
        verify(atLeast = 1, atMost = 1) { cameraManager.isOpen }
        verify(atLeast = 1, atMost = 1) { cameraManager.close() }
    }

}