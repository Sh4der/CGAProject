package cga.exercise.components.geometry

import cga.exercise.components.gameobjects.Collision
import cga.exercise.components.gameobjects.Portal
import cga.exercise.components.shader.ShaderProgram

class Renderable(val meshes : MutableList<Mesh>) : Transformable(), IRenderable {


    fun x() = this.getWorldPosition().x
    fun y() = this.getWorldPosition().y
    fun z() = this.getWorldPosition().z

    override fun render(shaderProgram: ShaderProgram) {

        meshes.forEach {mesh ->
            shaderProgram.setUniform("model_matrix", getWorldModelMatrix())
            mesh.render(shaderProgram)
        }

    }

    //Checks if mesh is colliding with portal. If yes, don't render so the portal camera doesn't get blocked
    fun renderWithPortalCheck(shaderProgram: ShaderProgram, portal: Portal) {

        val col = Collision(0f,0f,0f,0f,0f,0f)

        meshes.forEach {mesh ->
            col.createCollisionFromMesh(mesh)
            if (!col.checkPointCollision(portal.portalWall.x(), portal.portalWall.y(), portal.portalWall.z())) {
                shaderProgram.setUniform("model_matrix", getWorldModelMatrix())
                mesh.render(shaderProgram)
            }
        }

    }

}