package cga.exercise.components.gameobjects

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
}