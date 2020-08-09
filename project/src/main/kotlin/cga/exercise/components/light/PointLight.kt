package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f
import org.joml.Vector3i

open class PointLight(position: Vector3f, var color: Vector3i =  Vector3i(255)) : Transformable(), ILight {

    override val name = "pointlight"

    var constantAttenuation : Float = 0.3f
    var linearAttenuation : Float = 0.5f
    var quadraticAttenuation : Float = 0.1f

    init {
        translateLocal(position)
    }

    override fun bind(shaderProgram: ShaderProgram, name: String) {
        shaderProgram.use()

        shaderProgram.setUniform("${name}Color", Vector3f(color).mul(1f/255f))
        shaderProgram.setUniform("${name}Position", getWorldPosition())
        shaderProgram.setUniform("${name}ConstantAttenuation", constantAttenuation)
        shaderProgram.setUniform("${name}LinearAttenuation", linearAttenuation)
        shaderProgram.setUniform("${name}QuadraticAttenuation", quadraticAttenuation)
    }
}