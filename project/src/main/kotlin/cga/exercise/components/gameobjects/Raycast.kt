package cga.exercise.components.gameobjects

import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.Transformable
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.util.*

class Raycast(var x: Float, var y: Float, var z: Float, val mm: Matrix4f) : Transformable() {

    val speed = -0.001f
    val startx = x // XYZ is not needed, because we have the model matrix
    val starty = y
    val startz = z

    //Returns position on collision with wall
    fun moveUntilCollision(col: CollisionPool) : Vector4f{

        this.setModelMatrix(mm)

        //The Raycast object moves towards the point the player is looking at and checks, if it collides with anything.
        for (i in 0 .. 100000) {

            translateLocal(Vector3f(0f,0f, speed))
            if (col.checkPointCollision(this.getWorldPosition().x,this.getWorldPosition().y,this.getWorldPosition().z)) {
                val getColMask = col.checkPointCollisionEntity(this.getWorldPosition().x,this.getWorldPosition().y,this.getWorldPosition().z) //With which object does it collide?
                val getRotation = getColMask.getCollisionSide(this.getWorldPosition().x,this.getWorldPosition().y,this.getWorldPosition().z, startx, starty, startz) //Get rotation for the portal

                //Check if portal can be placed on this position
                if (getRotation.x != 22f && ((getColMask.x2 - getColMask.x1 >= 3 || getColMask.z2 - getColMask.z1 >=3) && getColMask.y2 - getColMask.y1 >= 6)) {
                    var xret = this.getWorldPosition().x
                    var zret = this.getWorldPosition().z

                    //Checks if rotation is top or bottom and places the portal depending on if the space on the wall is enough
                    if (getRotation.x == 90f || getRotation.x == 270f) {
                        if (getColMask.x2 - getColMask.x1 >= 3) {
                            //if (!col.checkNeighborX(getColMask.x1, getColMask.y1, getColMask.z1, getColMask.x2, getColMask.y2, getColMask.z2)) {
                                xret = Math.max(Math.min(this.getWorldPosition().x, getColMask.x2 - 2.5f), getColMask.x1 + 2.5f)
                            //}
                            zret = getRotation.y
                        }
                        else {
                            return Vector4f(-9999f) //Not placeable
                        }
                    }

                    //Checks if rotation is left or right and places the portal depending on if the space on the wall is enough
                    if (getRotation.x == 0f || getRotation.x == 180f) {
                        if (getColMask.z2 - getColMask.z1 >= 3) {
                            //if (!col.checkNeighborZ(getColMask.x1, getColMask.y1, getColMask.z1, getColMask.x2, getColMask.y2, getColMask.z2)) {
                                zret = Math.max(Math.min(this.getWorldPosition().z, getColMask.z2 - 2.5f), getColMask.z1 + 2.5f)
                            //}
                            xret = getRotation.y
                        }
                        else {
                            return Vector4f(-9999f) //Not placeable
                        }
                    }
                    val yret = Math.max(Math.min(this.getWorldPosition().y, getColMask.y2-3f), getColMask.y1+3f)
                    return Vector4f(xret, yret, zret, getRotation.x) //Return placeable position for portal
                }
                else {
                    return Vector4f(-9999f) //Collied with floor or ceiling
                }
            }
        }

        return Vector4f(-9999f) //Nothing collides with the raycast

    }

}