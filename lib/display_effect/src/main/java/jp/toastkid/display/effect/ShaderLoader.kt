package jp.toastkid.display.effect

import android.opengl.GLES20

class ShaderLoader {

    operator fun invoke(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, SNOW_VERTEX_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, SNOW_FRAGMENT_CODE)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, shaderCode: String?): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val SNOW_FRAGMENT_CODE = """
precision mediump float;
varying vec4 v_color;
uniform sampler2D u_texture0;

void main() {
    gl_FragColor = v_color * texture2D(u_texture0, gl_PointCoord);
}
        """

        private const val SNOW_VERTEX_CODE = """
attribute vec4 a_position;
attribute vec4 a_color;
attribute float a_pointSize;

uniform mat4 u_mvpMatrix;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    gl_Position = u_mvpMatrix * a_position;
    v_color = a_color;
    gl_PointSize = a_pointSize;
}
"""
    }

}
