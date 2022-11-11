package jp.toastkid.display.effect

interface RenderObject {
    fun initialize(projectionViewMatrix: FloatArray, viewWidth: Float, viewHeight: Float)
    fun update()
    fun render()
    fun getModelMatrix(): FloatArray?
    fun setVisible(visible: Boolean)
    fun isVisible(): Boolean
    fun setColor(r: Float, g: Float, b: Float, a: Float)
}
