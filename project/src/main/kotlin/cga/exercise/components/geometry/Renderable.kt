package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram

class Renderable(val meshes : MutableList<Mesh>) : Transformable(), IRenderable{




    override fun render(shaderProgram: ShaderProgram) {

        meshes.forEach {mesh ->
            shaderProgram.setUniform("model_matrix", getWorldModelMatrix())
            mesh.render(shaderProgram)
        }


    }
}