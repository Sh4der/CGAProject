package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.opengl.GL13

class Material(var diff: Texture2D,
               var emit: Texture2D,
               var specular: Texture2D,
               var shininess: Float = 50.0f,
               var tcMultiplier : Vector2f = Vector2f(1.0f)){

    var emitColor = Vector3f(1f)

    fun bind(shaderProgram: ShaderProgram) {
        // todo 3.2
        shaderProgram.use()
        shaderProgram.setUniform("tcMultiplier", tcMultiplier)
        emit.bind(0)
        shaderProgram.setUniform("emitTex", 0)
        specular.bind(1)
        shaderProgram.setUniform("diffTex", 1)
        diff.bind(2)
        shaderProgram.setUniform("specTex", 2)
        shaderProgram.setUniform("shininess", shininess)
        shaderProgram.setUniform("emitColor", emitColor)
    }

    fun unbind()
    {
        emit.unbind()
    }
}