package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram

interface ILight {
    val name : String
    fun bind(shaderProgram: ShaderProgram, name: String)
}