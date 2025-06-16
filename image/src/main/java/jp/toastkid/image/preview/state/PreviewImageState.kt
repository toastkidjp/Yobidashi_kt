package jp.toastkid.image.preview.state

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.abs

class PreviewImageState {

    private val scale = Animatable(1f)

    fun scale() = scale.value

    private val rotationY = mutableFloatStateOf(0f)

    fun rotationY() = rotationY.floatValue

    fun flip() {
        rotationY.value = if (rotationY.value == 0f) 180f else 0f
    }

    private val rotationZ = Animatable(0f)

    fun rotationZ() = rotationZ.value

    suspend fun rotateLeft() {
        rotationZ.animateTo(rotationZ.value - 90f)
    }

    suspend fun rotateRight() {
        rotationZ.animateTo(rotationZ.value + 90f)
    }

    private val offset = mutableStateOf(Offset.Zero)

    fun offset() = offset.value

    suspend fun onGesture(offsetChange: Offset, zoomChange: Float, rotationChange: Float) {
        rotationZ.snapTo(rotationZ.value + rotationChange)
        scale.snapTo(scale.value * zoomChange)
        val absX = abs(offsetChange.x)
        val absY = abs(offsetChange.y)
        offset.value += when {
            scale.value != 1f -> offsetChange
            absX > absY -> Offset(offsetChange.x, 0f)
            absY > absX -> Offset(0f, offsetChange.y)
            else -> Offset(offsetChange.x, 0f)
        }
    }

    suspend fun zoom(currentSize: Size, newOffset: Offset) {
        val unset = scale.value != 1f

        val newScale = if (unset) 1f else 3f

        if (unset) {
            rotationY.floatValue = 0f
            rotationZ.snapTo(0f)
            this.offset.value = Offset.Zero
            scale.animateTo(1f)
            return
        }

        val newLayoutRect = currentSize / 2f

        this.offset.value = Offset(
            (-1 * (newOffset.x - newLayoutRect.width)).coerceIn(-newLayoutRect.width, newLayoutRect.width),
            (-1 * (newOffset.y - newLayoutRect.height)).coerceIn(-newLayoutRect.height, newLayoutRect.height)
        )
        scale.animateTo(newScale)
    }

}