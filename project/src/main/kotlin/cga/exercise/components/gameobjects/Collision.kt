package cga.exercise.components.gameobjects

import cga.exercise.components.geometry.Mesh
import cga.framework.ModelLoader
import org.joml.Vector2f
import org.joml.Vector3f
import java.util.*

class Collision (var x1: Float, var y1: Float, var z1: Float, var x2: Float, var y2: Float, var z2: Float, val portalable : Boolean? = null) {

    // Updates the collision mask points
    fun updateCollision(newx1: Float, newy1: Float, newz1: Float, newx2: Float, newy2: Float, newz2: Float) {
        x1 = newx1
        x2 = newx2
        y1 = newy1
        y2 = newy2
        z1 = newz1
        z2 = newz2
    }

    // Checks point in 3d box collision
    fun checkPointCollision(check_x: Float, check_y: Float, check_z: Float) : Boolean {
        if (check_x >= x1 && check_x <= x2
                && check_y >= y1 && check_y < y2
                && check_z >= z1 && check_z <= z2) {
            return true
        }

        return false
    }

    // Checks rectangle in rectangle collsion (3d Box)
    fun checkRectangleCollision(col: Collision) : Boolean {
        if (!(col.x1 >= this.x2 || this.x1 >= col.x2) &&! (col.y1 >= this.y2 || this.y1 >= col.y2) && !(col.z1 >= this.z2 || this.z1 >= col.z2)) {
            return true
        }
        return false
    }

    // Generates Collision Mask from Mesh
    fun createCollisionFromMesh(mesh: Mesh) {

        var lowest = Vector3f(9999f)
        var lowestSample = Vector3f(9999f)

        var c = 0
        for (v in mesh.vertexdata) {
            if (c == 0) {
                lowestSample.x = v
                if (lowestSample.x < lowest.x) {
                    lowest.x = lowestSample.x
                }
            }
            else if (c == 1) {
                lowestSample.y = v
                if (lowestSample.y < lowest.y) {
                    lowest.y = lowestSample.y
                }
            }
            else if (c == 2) {
                lowestSample.z = v
                if (lowestSample.z < lowest.z) {
                    lowest.z = lowestSample.z
                }
            }
            c++
            if (c == 8) {
                c = 0
            }
        }

        var biggest = Vector3f(-9999f)
        var biggestSample = Vector3f(-9999f)

        c = 0
        for (v in mesh.vertexdata) {
            if (c == 0) {
                biggestSample.x = v
                if (biggestSample.x > biggest.x) {
                    biggest.x = biggestSample.x
                }
            }
            else if (c == 1) {
                biggestSample.y = v
                if (biggestSample.y > biggest.y) {
                    biggest.y = biggestSample.y
                }
            }
            else if (c == 2) {
                biggestSample.z = v
                if (biggestSample.z > biggest.z) {
                    biggest.z = biggestSample.z
                }
            }
            c++
            if (c == 8) {
                c = 0
            }
        }

        x1 = lowest.x
        y1 = lowest.y
        z1 = lowest.z
        x2 = biggest.x
        y2 = biggest.y
        z2 = biggest.z

    }

    // Calculates the side on the wall, which the portal is being shot on
    fun getCollisionSide(check_x: Float, check_y: Float, check_z: Float, startx: Float, starty: Float, startz: Float) : Vector2f { //Returns degrees for portal rotation
        /*              TOP
                 |---------------------------------------------------|
          LEFT  |  |---------------------------------------------|  |  RIGHT    <---- Wall
                |---------------------------------------------------|
                        BOTTOM
        */

        if (check_x >= x1+0.2f && check_x <= x2-0.2f && check_z <= z1+0.2f && check_z >= z1
                && startz <= z1) { //TOP
            return Vector2f(90f, z1)
        }
        else if (check_x >= x1+0.2f && check_x <= x2-0.2f && check_z >= z2-0.2f && check_z <= z2
                && startz >= z2) { //BOTTOM
            return Vector2f(270f, z2)
        }
        else if (check_x >= x1 && check_x <= x1+0.2f && check_z >= z1 && check_z <= z2
                && startx <= x1) { //LEFT
            return Vector2f(180f, x1)
        }
        else if (check_x <= x2 && check_x >= x2-0.2f && check_z >= z1 && check_z <= z2
                && startx >= x2) { //RIGHT
            return Vector2f(0f, x2)
        }

        return Vector2f(22f, 0f)

    }

    override fun toString(): String {
        return "x1: $x1, y1: $y1, z1: $z1, x2: $x2, y2: $y2, z2: $z2"
    }

}