package jp.toastkid.display.effect

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView

class SnowRendererView(context: Context) : GLSurfaceView(context) {

    init {
        val renderer = SnowParticleGlRenderer(context)
        setEGLContextClientVersion(2)
        setZOrderOnTop(true)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

}
