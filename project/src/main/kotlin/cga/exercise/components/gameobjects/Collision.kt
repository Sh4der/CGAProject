package cga.exercise.components.gameobjects

import cga.exercise.components.geometry.Mesh
import cga.framework.ModelLoader
import org.joml.Vector3f

class Collision (var x1: Float, var y1: Float, var z1: Float, var x2: Float, var y2: Float, var z2: Float, val portalable : Boolean? = null) {

    fun checkPointCollision(check_x: Float, check_y: Float, check_z: Float) : Boolean {
        if (check_x >= x1 && check_x <= x2
                && check_y >= y1 && check_y < y2
                && check_z >= z1 && check_z <= z2) {
            return true
        }

        return false
    }

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

    fun getCollisionSide(check_x: Float, check_y: Float, check_z: Float) : Float { //Returns degrees for portal rotation
        /*              TOP
                 |---------------------------------------------------|
          LEFT  |  |---------------------------------------------|  |  RIGHT
                |---------------------------------------------------|
                        BOTTOM
        */

        if (check_x >= x1+0.2f && check_x <= x2-0.2f && check_z <= z1+0.2f && check_z >= z1) { //TOP
            return 90f
        }
        else if (check_x >= x1+0.2f && check_x <= x2-0.2f && check_z >= z2-0.2f && check_z <= z2) { //BOTTOM
            return 270f
        }
        else if (check_x >= x1 && check_x <= x1+0.2f && check_z >= z1 && check_z <= z2) { //LEFT
            return 180f
        }
        else if (check_x <= x2 && check_x >= x2-0.2f && check_z >= z1 && check_z <= z2) { //RIGHT
            return 0f
        }

        return 22f

    }

}