package cga.exercise.components.geometry

import cga.exercise.components.gameobjects.Collision
import cga.exercise.components.gameobjects.Portal
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Vector3f

class Renderable(val meshes : MutableList<Mesh>) : Transformable(), IRenderable {

    //Get Position information
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

    fun setEmitColor(color: Vector3f) {
        meshes.forEach {mesh ->
            mesh.material?.emitColor = color
        }
    }

    fun renderWithPortalCheckDebugging(shaderProgram: ShaderProgram, portal: Portal) {

        val col = Collision(0f,0f,0f,0f,0f,0f)

        meshes.forEach {mesh ->
            col.createCollisionFromMesh(mesh)
            if (!col.checkRectangleCollision(portal.portalRecCollision)) {
            //if (!col.checkPointCollision(portal.portalWall.x(), portal.portalWall.y(), portal.portalWall.z())) {
                shaderProgram.setUniform("model_matrix", getWorldModelMatrix())
                mesh.material?.emitColor = Vector3f(1f)
                mesh.render(shaderProgram)
            }
            else {
                shaderProgram.setUniform("model_matrix", getWorldModelMatrix())
                mesh.material?.emitColor = Vector3f(0f)
                mesh.render(shaderProgram)
            }
        }

    }

    fun checkCollisionWithPortal(portal: Portal) : Collision {

        val col = Collision(0f,0f,0f,0f,0f,0f)

        meshes.forEach {mesh ->
            col.createCollisionFromMesh(mesh)
            if (col.checkRectangleCollision(portal.portalRecCollision) && !(col.checkPointCollision(portal.x, portal.y, portal.z))) {
                //Collide
                return col
            }
        }

        return Collision(0f,0f,0f,0f,0f,0f)

    }

    fun setMaterial(mat: Material) {
        meshes.forEach {mesh ->
            mesh.material = mat
        }
    }

    override fun toString(): String {
        return "X: ${x()}, Y: ${y()}, Z: ${z()}"
    }

}