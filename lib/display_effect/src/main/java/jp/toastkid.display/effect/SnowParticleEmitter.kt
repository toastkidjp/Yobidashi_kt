package jp.toastkid.display.effect

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SnowParticleEmitter(
    private val shaderProgram: Int,
    textureId: Int,
    pointSizeRatio: Float,
    particleCount: Int,
    velocityRatio: Float
): RenderObject {

    private var particleCount = DEFAULT_PARTICLE_COUNT

    private var colorBuffer: FloatBuffer? = null

    private var sizeBuffer: FloatBuffer? = null

    private var positionBuffer: FloatBuffer? = null

    private val positionHandle: Int = GLES20.glGetAttribLocation(shaderProgram, "a_position")

    private val colorHandle: Int = GLES20.glGetAttribLocation(shaderProgram, "a_color")

    private val pointSizeHandle: Int = GLES20.glGetAttribLocation(shaderProgram, "a_pointSize")

    private val texture0Handle: Int = GLES20.glGetUniformLocation(shaderProgram, "u_texture0")

    private val mvpMatrixHandle: Int = GLES20.glGetUniformLocation(shaderProgram, "u_mvpMatrix")

    private val textureId: Int
    private val pointSizeRatio: Float
    private val velocityRatio: Float

    private lateinit var projectionViewMatrix: FloatArray
    private lateinit var velocityBuffer: Array<FloatArray>
    private lateinit var timeSinceLastTurn: FloatArray
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var prevTime = 0L
    private var animationStartingTime = 0L
    private var internalYVelocityFactor = 1f
    private var visible = true

    init {
        this.textureId = textureId
        this.pointSizeRatio = pointSizeRatio
        this.particleCount = particleCount
        this.velocityRatio = velocityRatio
    }

    override fun initialize(projectionViewMatrix: FloatArray, viewWidth: Float, viewHeight: Float) {
        this.projectionViewMatrix = projectionViewMatrix
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight
        velocityBuffer = Array(particleCount) { FloatArray(2) }
        timeSinceLastTurn = FloatArray(particleCount)

        val maxX = viewWidth / 2
        val maxY = viewHeight / 2
        var sizeMin = viewWidth * pointSizeRatio
        var sizeMax = sizeMin
        val randRange = sizeMin * 0.005f / 2
        sizeMin -= randRange
        sizeMax += randRange

        val bbPosition = ByteBuffer.allocateDirect(particleCount * 3 * 4)
        bbPosition.order(ByteOrder.nativeOrder())
        positionBuffer = bbPosition.asFloatBuffer()
        positionBuffer?.position(0)

        val bbColor = ByteBuffer.allocateDirect(particleCount * 4 * 4)
        bbColor.order(ByteOrder.nativeOrder())
        colorBuffer = bbColor.asFloatBuffer()
        colorBuffer?.position(0)

        val bbSize = ByteBuffer.allocateDirect(particleCount * 4)
        bbSize.order(ByteOrder.nativeOrder())
        sizeBuffer = bbSize.asFloatBuffer()
        sizeBuffer?.position(0)

        val velocityX = viewWidth * PARTICLE_X_VELOCITY_FACTOR
        val velocityYMin = viewHeight * PARTICLE_Y_VELOCITY_FACTOR_MIN
        val velocityYMax = viewHeight * PARTICLE_Y_VELOCITY_FACTOR_MAX

        (0 until particleCount).forEach { i ->
            positionBuffer?.put(i * 3, getRandomFloat(-maxX, maxX))
            positionBuffer?.put(i * 3 + 1, getRandomFloat(maxY + 1f, maxY * 3))
            positionBuffer?.put(i * 3 + 2, 0f)

            velocityBuffer[i] = FloatArray(2)
            velocityBuffer[i][0] =
                velocityRatio * getRandomFloat(-velocityX, velocityX)
            velocityBuffer[i][1] =
                velocityRatio * -1f * getRandomFloat(velocityYMin, velocityYMax)

            colorBuffer?.put(i * 4, 1.0f)
            colorBuffer?.put(i * 4 + 1, 1.0f)
            colorBuffer?.put(i * 4 + 2, 1.0f)
            colorBuffer?.put(i * 4 + 3, 1.0f)
            sizeBuffer?.put(i, getRandomFloat(sizeMin, sizeMax))

            timeSinceLastTurn[i] = getRandomFloat(-5.0f, 0.0f)
        }

        positionBuffer?.position(0)
        colorBuffer?.position(0)
        sizeBuffer?.position(0)
        prevTime = 0
        animationStartingTime = 0
    }

    override fun update() {
        val nowMillis = System.currentTimeMillis()
        if (prevTime == 0L) {
            prevTime = nowMillis
            animationStartingTime = nowMillis
            internalYVelocityFactor = INTERNAL_START_FACTOR
        }

        val dTimeInSec = (nowMillis - prevTime) / 1000f
        val elapsed = nowMillis - animationStartingTime
        val viewMaxX = viewWidth / 2f
        val viewMaxY = viewHeight / 2f
        val thresholdX = viewMaxX + viewWidth * 0.05f
        val thresholdY = viewMaxY + viewHeight * 0.05f
        val elapsedInSecond = elapsed / 1000f
        internalYVelocityFactor = if (elapsedInSecond < INTERNAL_DURATION_IN_SEC) {
            val t = elapsedInSecond / INTERNAL_DURATION_IN_SEC
            INTERNAL_START_FACTOR + (INTERNAL_END_FACTOR - INTERNAL_START_FACTOR) * t
        } else INTERNAL_END_FACTOR

        (0 until particleCount).forEach { i ->
            var posX = positionBuffer!![i * 3]
            var posY = positionBuffer!![i * 3 + 1]
            var posZ = positionBuffer!![i * 3 + 2]

            run {
                timeSinceLastTurn[i] += dTimeInSec
                if (timeSinceLastTurn[i] >= TIME_TILL_TURN) {
                    velocityBuffer[i][0] = -velocityBuffer[i][0]
                    timeSinceLastTurn[i] = getRandomFloat(-5.0f, 0.0f)
                }

                val turnVelocityModifier =
                    timeSinceLastTurn[i] * TIME_TILL_TURN_NORMALIZED_UNIT
                posX += velocityBuffer[i][0] * turnVelocityModifier
                posY += velocityBuffer[i][1] * internalYVelocityFactor

                if (posY < -thresholdY || posX < -thresholdY || posX > thresholdX) {
                    posX = getRandomFloat(-viewMaxX, viewMaxX)
                    posY = viewMaxY + 1f
                    posZ = 0f
                }

                positionBuffer?.put(i * 3, posX)
                positionBuffer?.put(i * 3 + 1, posY)
                positionBuffer?.put(i * 3 + 2, posZ)
            }
        }
        positionBuffer?.position(0)
        colorBuffer?.position(0)
        prevTime = nowMillis
    }

    private fun getRandomFloat(min: Float, max: Float): Float {
        val r = Math.random().toFloat()
        return min + r * (max - min)
    }

    override fun render() {
        if (!visible) {
            return
        }

        GLES20.glUseProgram(shaderProgram)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, projectionViewMatrix, 0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(pointSizeHandle, 1, GLES20.GL_FLOAT, false, 0, sizeBuffer)
        GLES20.glEnableVertexAttribArray(pointSizeHandle)
        GLES20.glUniform1i(texture0Handle, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount)
    }

    override fun getModelMatrix(): FloatArray? = null

    override fun isVisible(): Boolean = visible

    override fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    override fun setColor(r: Float, g: Float, b: Float, a: Float) = Unit

    companion object {

        private const val DEFAULT_PARTICLE_COUNT = 300
        private const val PARTICLE_X_VELOCITY_FACTOR = 0.001f
        private const val PARTICLE_Y_VELOCITY_FACTOR_MIN = 0.0014f
        private const val PARTICLE_Y_VELOCITY_FACTOR_MAX = 0.0017f
        private const val TIME_TILL_TURN = 3.0f
        private const val TIME_TILL_TURN_NORMALIZED_UNIT = 1.0f / TIME_TILL_TURN
        private const val INTERNAL_DURATION_IN_SEC = 5f
        private const val INTERNAL_START_FACTOR = 2f
        private const val INTERNAL_END_FACTOR = 1f
    }

}
