package cga.exercise.components.gameobjects

class Collision (var x1: Float, var y1: Float, var z1: Float, var x2: Float, var y2: Float, var z2: Float) {

    fun checkPointCollision(check_x: Float, check_y: Float, check_z: Float) : Boolean {
        if (check_x >= x1 && check_x <= x2
                && check_y >= y1 && check_y < y2
                && check_z >= z1 && check_z <= z2) {
            return true
        }

        return false
    }

}