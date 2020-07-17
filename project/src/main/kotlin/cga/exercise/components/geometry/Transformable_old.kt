package cga.exercise.components.geometry

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector4f


open class Transformable_old : ITransformable {

    private var localPos : Vector3f = Vector3f()
    private var localRot : Vector3f = Vector3f()
    private var localScale : Vector3f = Vector3f(1f)

    var parent : Transformable_old = EmptyTransformable

    override fun rotateLocal(pitch: Float, yaw: Float, roll: Float) {
        localRot.add(pitch, yaw, roll)
    }

    override fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        var tmpPos = this.localPos.sub(altMidpoint)
        tmpPos = tmpPos.rotateX(pitch)
        tmpPos = tmpPos.rotateY(yaw)
        tmpPos = tmpPos.rotateZ(roll)

        tmpPos = tmpPos.add(altMidpoint)

        this.localPos = tmpPos

    }

    override fun translateLocal(deltaPos: Vector3f) {
        //localPos = localPos.add(deltaPos)
        val transformResult = getLocalModelMatrix().mul(Matrix4f().translate(deltaPos.x, deltaPos.y, deltaPos.z))
        var transform = Vector3f();
        localPos = transformResult.getTranslation(transform)
    }

    override fun translateGlobal(deltaPos: Vector3f) {
        this.localPos.add(deltaPos)
    }

    override fun scaleLocal(scale: Vector3f) {
        this.localScale = scale
    }

    override fun getPosition() : Vector3f = localPos

    override fun getWorldPosition(): Vector3f {
        val worldMatrix = getWorldModelMatrix()
        val worldPosition = Vector3f(worldMatrix.m30(), worldMatrix.m31(), worldMatrix.m32())
        return worldPosition
    }

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

    override fun getWorldModelMatrix(): Matrix4f = parent.getLocalModelMatrix().mul(getLocalModelMatrix())

    override fun getLocalModelMatrix(): Matrix4f {
        var localModelMatrix = Matrix4f()
        localModelMatrix.translate(localPos)
        localModelMatrix.rotate(Math.toRadians(localRot.x.toDouble()).toFloat(), Vector3f(1f, 0f, 0f))
        localModelMatrix.rotate(Math.toRadians(localRot.y.toDouble()).toFloat(), Vector3f(0f, 1f, 0f))
        localModelMatrix.rotate(Math.toRadians(localRot.z.toDouble()).toFloat(), Vector3f(0f, 0f, 1f))
        localModelMatrix.scale(localScale)

        return localModelMatrix
    }

    companion object EmptyTransformable : Transformable_old()
    {
        override fun rotateLocal(pitch: Float, yaw: Float, roll: Float) {}

        override fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {}

        override fun translateLocal(deltaPos: Vector3f) {}

        override fun translateGlobal(deltaPos: Vector3f) {}

        override fun scaleLocal(scale: Vector3f) {}

        override fun getWorldPosition(): Vector3f = Vector3f()

        override fun getXAxis(): Vector3f = Vector3f()

        override fun getYAxis(): Vector3f = Vector3f()

        override fun getZAxis(): Vector3f = Vector3f()

        override fun getWorldXAxis(): Vector3f = Vector3f()

        override fun getWorldYAxis(): Vector3f = Vector3f()

        override fun getWorldZAxis(): Vector3f = Vector3f()

        override fun getWorldModelMatrix(): Matrix4f = Matrix4f()

        override fun getLocalModelMatrix(): Matrix4f = Matrix4f()

    }

}