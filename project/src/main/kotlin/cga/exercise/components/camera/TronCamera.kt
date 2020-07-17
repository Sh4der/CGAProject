package cga.exercise.components.camera

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f

class TronCamera : Transformable(), ICamera {


    override fun getCalculateViewMatrix(): Matrix4f {

        val viewMatrix = Matrix4f().lookAt(getWorldPosition(), getWorldPosition().sub(getWorldZAxis()), getWorldYAxis())

        return viewMatrix
    }

    override fun getCalculateProjectionMatrix(): Matrix4f {
        return Matrix4f().perspective(Math.toRadians(90.0).toFloat(), 16f/9f, 0.1f, 100f)
    }

    override fun bind(shader: ShaderProgram) {
        shader.setUniform("view_matrix", getCalculateViewMatrix())
        shader.setUniform("projection_matrix", getCalculateProjectionMatrix())
    }
}