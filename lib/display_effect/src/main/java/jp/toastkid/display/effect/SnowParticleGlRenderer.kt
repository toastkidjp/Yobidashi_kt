package jp.toastkid.display.effect

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import jp.toastkid.display.effect.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan

class SnowParticleGlRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val matrixProjection = FloatArray(16)
    private val matrixView = FloatArray(16)
    private val matrixProjectionAndView = FloatArray(16)

    private var screenWidth = -1f
    private var screenHeight = -1f
    private val renderingObjects: MutableList<RenderObject> = mutableListOf()

    override fun onDrawFrame(unused: GL10) {
        renderingObjects.forEach {
            it.update()
        }
        render()
    }

    private fun render() {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDepthMask(false)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        renderingObjects.forEach {
            it.render()
        }

        GLES20.glDepthMask(true)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()

        GLES20.glViewport(0, 0, width, height)

        (0..15).forEach {
            matrixProjection[it] = 0.0f
            matrixView[it] = 0.0f
            matrixProjectionAndView[it] = 0.0f
        }

        val fovY = 45f
        val distance = screenHeight / 2f / tan(fovY / 2f)
        Matrix.orthoM(
            matrixProjection,
            0,
            -screenWidth / 2,
            screenWidth / 2,
            -screenHeight / 2,
            screenHeight / 2,
            1f,
            distance * 2
        )
        Matrix.setLookAtM(
            matrixView,
            0,
            0f,
            0f,
            distance,
            0f,
            0f,
            0f,
            0f,
            1.0f,
            0.0f,
        )
        Matrix.multiplyMM(matrixProjectionAndView, 0, matrixProjection, 0, matrixView, 0)

        renderingObjects.forEach {
            it.initialize(matrixProjectionAndView, screenWidth, screenHeight)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1f)
        try {
            makeRenderObject()
        } catch (t: Throwable) {
            t.printStackTrace()
            renderingObjects.clear()
        }
    }

    private fun makeRenderObject() {
        renderingObjects.clear()
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_snow)
        val textureId = TextureLoader().invoke(drawable?.toBitmap())

        val program = ShaderLoader().invoke()

        renderingObjects.add(
            SnowParticleEmitter(program, textureId, 0.025f, 30, 1f)
        )
        renderingObjects.add(
            SnowParticleEmitter(program, textureId, 0.015f, 100, 0.7f)
        )
        renderingObjects.add(
            SnowParticleEmitter(program, textureId, 0.008f, 150, 0.5f)
        )
    }

}
