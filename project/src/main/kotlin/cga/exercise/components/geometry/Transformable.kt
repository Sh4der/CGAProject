package cga.exercise.components.geometry

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Math


open class Transformable : ITransformable {

    private var model_matrix = Matrix4f()

    var parent : Transformable? = null

    override fun rotateLocal(pitch: Float, yaw: Float, roll: Float) {
        model_matrix.rotateXYZ(Math.toRadians(pitch), Math.toRadians(yaw), Math.toRadians(roll))
    }

    override fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        val vec = Vector3f(altMidpoint)

        val transposeMatrix = Matrix4f().translate(vec).rotateXYZ(Math.toRadians(pitch), Math.toRadians(yaw), Math.toRadians(roll)).translate(vec.negate())
        model_matrix = transposeMatrix.mul(model_matrix)
    }

    final override fun translateLocal(deltaPos: Vector3f) {
        model_matrix.translate(deltaPos)
    }

    override fun translateGlobal(deltaPos: Vector3f) {
        val translation = Matrix4f().setTranslation(deltaPos)
        model_matrix = translation.mul(model_matrix)
    }

    override fun scaleLocal(scale: Vector3f) {
        model_matrix.scale(scale)
    }

    override fun getPosition() : Vector3f = model_matrix.getTranslation(Vector3f())

    override fun getWorldPosition(): Vector3f = getWorldModelMatrix().getTranslation(Vector3f())

    override fun getXAxis(): Vector3f {
        val worldMatrix = getLocalModelMatrix()
        val xAxis = Vector3f(worldMatrix.m00(), worldMatrix.m01(), worldMatrix.m02())
        return xAxis.normalize()
    }

    override fun getYAxis(): Vector3f {
        val worldMatrix = getLocalModelMatrix()
        val yAxis = Vector3f(worldMatrix.m10(), worldMatrix.m11(), worldMatrix.m12())

        return yAxis.normalize()
    }

    override fun getZAxis(): Vector3f {
        val worldMatrix = getLocalModelMatrix()
        val zAxis = Vector3f(worldMatrix.m20(), worldMatrix.m21(), worldMatrix.m22())

        return zAxis.normalize()
    }

    override fun getWorldXAxis(): Vector3f {
        val worldMatrix = getWorldModelMatrix()
        val xAxis = Vector3f(worldMatrix.m00(), worldMatrix.m01(), worldMatrix.m02())

        return xAxis.normalize()
    }

    override fun getWorldYAxis(): Vector3f {
        val worldMatrix = getWorldModelMatrix()
        val yAxis = Vector3f(worldMatrix.m10(), worldMatrix.m11(), worldMatrix.m12())

        return yAxis.normalize()
    }

    override fun getWorldZAxis(): Vector3f {
        val worldMatrix = getWorldModelMatrix()
        val zAxis = Vector3f(worldMatrix.m20(), worldMatrix.m21(), worldMatrix.m22())

        return zAxis.normalize()    }

    override fun getWorldModelMatrix(): Matrix4f =
            parent?.getWorldModelMatrix()?.mul(model_matrix) ?: Matrix4f().mul(model_matrix)


    override fun getLocalModelMatrix(): Matrix4f = model_matrix

}