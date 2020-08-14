package cga.exercise.components.gameobjects

import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.Transformable
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

class Raycast(var x: Float, var y: Float, var z: Float, val mm: Matrix4f) : Transformable() {

    val speed = -0.001f

    //Returns position on collision with wall
    fun moveUntilCollision(col: CollisionPool) : Vector4f{

        this.setModelMatrix(mm)

        for (i in 0 .. 40000) {
            /*
            x += xdir*speed
            y += ydir*speed
            z += zdir*speed
            */
            translateLocal(Vector3f(0f,0f,speed))
            if (col.checkPointCollision(this.getWorldPosition().x,this.getWorldPosition().y,this.getWorldPosition().z)) {
                val getRotation = col.checkPointCollisionEntity(this.getWorldPosition().x,this.getWorldPosition().y,this.getWorldPosition().z).getCollisionSide(this.getWorldPosition().x,this.getWorldPosition().y,this.getWorldPosition().z)
                return Vector4f(this.getWorldPosition().x,this.getWorldPosition().y+1f,this.getWorldPosition().z,getRotation)
            }
        }

        return Vector4f(0f)

    }

}