package cga.exercise.components.gameobjects

import cga.exercise.components.geometry.Material
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import cga.framework.ModelLoader
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11

class CollisionPool {

    // Pool of Collision objects (all are box shaped)
    val collisionPool = arrayListOf<Collision>()

    fun addCollision(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
        collisionPool.add(Collision(x1 ,y1 ,z1 ,x2, y2, z2))
    }

    fun addCollision(collision: Collision) {
        collisionPool.add(collision)
    }

    // Checks point collision with every collision entity
    fun checkPointCollision(check_x: Float, check_y: Float, check_z: Float) : Boolean {

        for (c in collisionPool) {
            if (c.checkPointCollision(check_x, check_y, check_z)) { return true }
        }

        return false
    }

    // Checks point collision with every collision entity
    fun checkPointCollisionEntity(check_x: Float, check_y: Float, check_z: Float) : Collision {

        for (c in collisionPool) {
            if (c.checkPointCollision(check_x, check_y, check_z)) { return c }
        }

        return Collision(0f,0f,0f,0f,0f,0f)
    }

    fun checkRectangleCollision(col: Collision) : Boolean {
        for (c in collisionPool) {
            if (!(col.x1 >= c.x2 || c.x1 >= col.x2) &&! (col.y1 >= c.y2 || c.y1 >= col.y2) && !(col.z1 >= c.z2 || c.z1 >= col.z2)) {
                return true
            }
        }
        return false
    }

    fun addCollisionFromObject(path: String, rot: Vector3f) {

        // Create textures to compare with meshs' current texture and define if a portal can be created on this mesh or not.
        // Doesn't work at the moment
        val diffTex = Texture2D("assets/textures/con_wall_1.png", true)
        diffTex.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_NEAREST, GL11.GL_NEAREST)

        val diffTexMetal1 = Texture2D("assets/textures/metal_floor.png", true)
        diffTexMetal1.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_NEAREST, GL11.GL_NEAREST)


        val obj = ModelLoader.loadModel(path, rot.x, rot.y, rot.z)

        println("SIZE ${obj?.meshes?.size}")
        println("SIZE ${obj?.meshes?.get(0)?.vertexdata?.size}")

        var meshNumber = 0
        for (m in obj?.meshes!!) {

            var lowest = Vector3f(9999f)
            var lowestSample = Vector3f(9999f)

            var c = 0
            for (v in obj?.meshes?.get(meshNumber)?.vertexdata!!) {
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

            println(lowest)

            var biggest = Vector3f(-9999f)
            var biggestSample = Vector3f(-9999f)

            c = 0
            for (v in obj?.meshes?.get(meshNumber)?.vertexdata!!) {
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

            println(biggest)

            var portalable = true
            /*if (obj?.meshes?.get(0)?.material?.diff) {
                portalable = false
                println("#########################")
            }*/
            //addCollision(lowest.x, lowest.y, lowest.z, biggest.x, biggest.y, biggest.z)
            addCollision(Collision(lowest.x, lowest.y, lowest.z, biggest.x, biggest.y, biggest.z, portalable))

            meshNumber++
        }

    }
}