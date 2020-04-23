package jp.toastkid.yobidashi.libs.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author kHRYSTAL <723526676@qq.com>
 * @author toastkidjp
 */
class CircularViewMode() {

    private var rightCircleOffset = 700
    private var leftCircleOffset = -300
    private var degToRad = 1.0f / 180.0f * Math.PI.toFloat()
    private var scalingRatio = 0.001f
    private var translationRatio = 0.09f

    private var leftMode = false

    fun setLeftMode() {
        leftMode = true
    }

    fun setRightMode() {
        leftMode = false
    }

    fun applyToView(v: View, parent: RecyclerView) {
        val halfHeight = v.height * 0.5f
        val parentHalfHeight = parent.height * 0.5f
        val y = v.y
        val rot = parentHalfHeight - halfHeight - y

        v.pivotX = 0.0f
        v.pivotY = halfHeight
        v.rotation = rot * 0.05f
        v.translationX = (-Math.cos((rot * translationRatio * degToRad).toDouble()) + 1).toFloat() * getCircleOffset()

        val scale = 1.0f - Math.abs(parentHalfHeight - halfHeight - y) * scalingRatio
        v.scaleX = scale
        v.scaleY = scale
    }

    private fun getCircleOffset() = if (leftMode) leftCircleOffset else rightCircleOffset
}
