package cga.exercise.components.gameobjects

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.framebuffer.GeometryFramebuffer
import cga.exercise.components.framebuffer.SimpleFramebuffer
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import cga.framework.OBJLoader
import org.joml.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL43
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.system.exitProcess

class Portal(val window: GameWindow, val screenShader: ShaderProgram, val frameColor: Vector3f, var x: Float = 0.0f, var y: Float = 0.0f, var z: Float = 0.0f, var rotx: Float = 90f, var roty: Float = 180f, var rotz: Float = 90f) {
    /*
    What does a portal need?
    - 3d Object (Wall)
    - 3d Object (Border) - Later
    - Camera
    - Framebuffer
    - Material
    - Function to generate current Material (Texture from camera)
    */

    var framebuffer : SimpleFramebuffer
    private var portalCamTexture : Texture2D
    private var portalMaterial : Material
    private var portalMaterialCam : Material
    val portalWall : Renderable
    val portalCam : Renderable
    val portalFrame : Renderable?
    val camera : TronCamera
    private var collisionBox3Dp1 : Vector3f //part1
    private var collisionBox3Dp2 : Vector3f //part2
    private var collisionAlmostBox3Dp1 : Vector3f //part1
    private var collisionAlmostBox3Dp2 : Vector3f //part2
    var portalRecCollision : Collision
    private var portalWallScale : Float

    var goingOutCoord = Vector3f(0f)

    private var initSet = Vector4f(x,y,z,roty)
    //private val texture :Texture2D

    init {
        //Create Framebuffer
        framebuffer = SimpleFramebuffer(window.framebufferWidth, window.framebufferHeight)

        /*texture = Texture2D(null as ByteBuffer?, framebuffer.width, framebuffer.height, false)
        texture.setTexParams(GL11.GL_NEAREST, GL11.GL_NEAREST)*/

        //Use Ground Textures as default textures, 'cause I'm too lazy to create default portal textures
        portalCamTexture = Texture2D("assets/textures/ground_diff.png", true)
        //portalMaterial = Material(texture, Texture2D("assets/textures/con_wall_1_emit.png", false), texture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()
        portalMaterial = Material(framebuffer.framebufferTexture, Texture2D("assets/textures/con_wall_1_emit.png", false), framebuffer.framebufferTexture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()
        portalMaterial.emitColor = Vector3f(1f)
        portalMaterialCam = Material(portalCamTexture, portalCamTexture, portalCamTexture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()
        portalMaterialCam.emitColor = Vector3f(1f)

        portalWall = ModelLoader.loadModel("assets/models/portal/portal_flat.obj", 0f,0f,0f)!! //Renderable(mutableListOf(meshPortalWall))
        portalWall.setEmitColor(Vector3f(1f))
        portalWall.meshes.get(0).material = portalMaterial

        //Portl Cam renders an object on the position of the portal camera. This is just used for debugging.
        //load an object and create a mesh -> OLD CODE, BUT IT WORKS!
        val stride: Int = 8 * 4
        val attrPos = VertexAttribute(3, GL11.GL_FLOAT, stride, 0) //position
        val attrTC = VertexAttribute(2, GL11.GL_FLOAT, stride, 3 * 4) //textureCoordinate
        val attrNorm = VertexAttribute(3, GL11.GL_FLOAT, stride, 5 * 4) //normalval
        val vertexAttributes = arrayOf<VertexAttribute>(attrPos, attrTC, attrNorm)
        val resPortalWall : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/portal/portal_flat.obj")
        val portalWallMesh: OBJLoader.OBJMesh = resPortalWall.objects[0].meshes[0]
        val meshPortalWallCam = Mesh(portalWallMesh.vertexData, portalWallMesh.indexData, vertexAttributes, portalMaterialCam)
        portalCam = Renderable(mutableListOf(meshPortalWallCam)) //For visualizing the camera for each portal
        portalCam.meshes[0].material?.emitColor = Vector3f(frameColor)

        //Load in Portal Frame
        portalFrame = ModelLoader.loadModel("assets/models/portal/portal_frame_flat.obj", Math.toRadians(0f), Math.toRadians(180f), 0f)
        portalFrame?.meshes?.get(0)?.material?.emitColor = frameColor
        portalFrame?.meshes?.get(0)?.material?.diff = Texture2D("assets/textures/ground_diff.png", false)
        portalFrame?.meshes?.get(0)?.material?.emit = Texture2D("assets/textures/ground_diff.png", false)
        portalFrame?.meshes?.get(0)?.material?.specular = Texture2D("assets/textures/ground_diff.png", false)

        portalWallScale = 0.0f //This is used for the scaling animation of the portal
        //Portal & Frame transformation
        portalWall.rotateLocal(rotx,roty,rotz)
        portalWall.translateGlobal(Vector3f(x,y,z))
        portalWall.scaleLocal(Vector3f(1.18f, 0.01f+portalWallScale, 0.0f+portalWallScale))

        portalFrame?.rotateLocal(rotx,roty,rotz)
        portalFrame?.translateGlobal(Vector3f(x,y,z))
        portalFrame?.scaleLocal(Vector3f(0.8f, portalWallScale, portalWallScale))

        //Define camera
        camera = TronCamera()
        camera.translateLocal(Vector3f(0f,0f,0f))

        //Create Collisionbox
        collisionBox3Dp1 = Vector3f(x-0.15f, y-3f, z-2f)
        collisionBox3Dp2 = Vector3f(x+0.15f, y+3f, z+2f)
        collisionAlmostBox3Dp1 = Vector3f(x-0.15f, y-3f, z-2f)
        collisionAlmostBox3Dp2 = Vector3f(x+0.15f, y+3f, z+2f)

        //Rectangle Collision
        portalRecCollision = Collision(x-1f, y-1f, z-1f,x+1f, y+1f, z+1f)
    }

    /**
     * sets the texture of the portal
     * @param texture Texture2D
     */
    fun setTexture(texture : Texture2D)
    {
        portalMaterial.diff = texture
        portalMaterial.specular = texture
    }

    // It sets the position & rotation of the portal cameras (relative to player and portal position)
    fun updatePortalSettings(p: Portal, c: Renderable?, playerCam: TronCamera) {
        val pWorldPos = p.portalWall.getWorldPosition()
        val playerWorldPos = c?.getWorldPosition()
        if (playerWorldPos == null) {
            exitProcess(0)
        }
        else {

            /*
            //Set Portal Cam with Matrix Multiplication -> I can't make it work...
            val otherPortalMatrix = Matrix4f(p.getWorldModelMatrix())
            val otherPortalMatrix2 = Matrix4f(p.getWorldModelMatrix())
            val myPortalMatrix = Matrix4f(portalWall.getWorldModelMatrix())
            val playerMatrix = Matrix4f(c.getWorldModelMatrix())

            //val mdummy = otherPortalMatrix.mul(myPortalMatrix).mul(playerMatrix)
            val mdummy = otherPortalMatrix2.mul(otherPortalMatrix.mul(playerMatrix))
            val mpos = Vector3f()
            val mrot = AxisAngle4f()
            mdummy.getColumn(3,mpos)
            mdummy.getRotation(mrot)

            camera.setRotationA(mrot)
            camera.setPosition(mpos.x, 2f, mpos.z)
            */


            // Place portal camera in the correct position

            var rangecheck = 1

            // Check which portal is further to the middle, to determine in what relative position they stay
            if (pointDistance(portalWall.getWorldPosition().x, portalWall.getWorldPosition().y, 0f,0f) < pointDistance(pWorldPos.x, pWorldPos.y, 0f, 0f)) {
                rangecheck = -1
            }
            else {
                rangecheck = 1
            }

            // Camera rotation
            val camdir = (c.getYDirDeg() + (p.roty - roty) + (180f * rangecheck.toFloat())).toFloat()
            portalCam.setModelMatrix(Matrix4f())//setRotation(0f,camdir,0f)
            portalCam.rotateLocal(0f,camdir+270,0f)

            val checkdir = ((p.roty - roty) + (180 * rangecheck)).toInt()

            // Offset for teleporting the player -> Further out the portal
            var camOffsetx = 0f
            var camOffsetz = 0f
            val offsetConst = 0.7f

            if (p.roty == 0f) {
                camOffsetx = offsetConst
                camOffsetz = 0f
            }
            else if (p.roty == 180f) {
                camOffsetx = -offsetConst
                camOffsetz = 0f
            }
            else if (p.roty == 270f) {
                camOffsetx = 0f
                camOffsetz = offsetConst
            }
            else if (p.roty == 90f) {
                camOffsetx = 0f
                camOffsetz = -offsetConst
            }

            // Set the correct portal camera position based on the difference in rotation between the two portals
            if (checkdir == 0 || checkdir == 360 || checkdir == -360) {
                val setx = playerWorldPos.x - portalWall.getWorldPosition().x + pWorldPos.x + camOffsetx
                val setz = playerWorldPos.z - portalWall.getWorldPosition().z + pWorldPos.z + camOffsetz
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                portalCam.setPosition(setx, sety, setz)
            }

            if (checkdir == 180 || checkdir == -180) {
                val setx = -(playerWorldPos.x - portalWall.getWorldPosition().x) + pWorldPos.x + camOffsetx
                val setz = -(playerWorldPos.z - portalWall.getWorldPosition().z) + pWorldPos.z + camOffsetz
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                portalCam.setPosition(setx, sety, setz)
            }

            if (checkdir == 90 || checkdir == -270 || checkdir == 450) {
                val setz = -(playerWorldPos.x - portalWall.getWorldPosition().x) + pWorldPos.z + camOffsetz
                val setx = playerWorldPos.z - portalWall.getWorldPosition().z + pWorldPos.x + camOffsetx
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                portalCam.setPosition(setx, sety, setz)
            }

            if (checkdir == -90 || checkdir == 270 || checkdir == -450) {
                val setz = playerWorldPos.x - portalWall.getWorldPosition().x + pWorldPos.z + camOffsetz
                val setx = -(playerWorldPos.z - portalWall.getWorldPosition().z) + pWorldPos.x + camOffsetx
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                portalCam.setPosition(setx, sety, setz)
            }

            // Set the coordinates to where the player should get out
            // Also, update Collisionbox
            if (roty == 0f) {
                collisionBox3Dp1 = Vector3f(x-0.3f, y-3f, z-1.5f)
                collisionBox3Dp2 = Vector3f(x+0.3f, y+3f, z+1.5f)
                collisionAlmostBox3Dp1 = Vector3f(x-1f, y-3f, z-1.5f)
                collisionAlmostBox3Dp2 = Vector3f(x+1f, y+3f, z+1.5f)
            }
            else if (roty == 180f) {
                collisionBox3Dp1 = Vector3f(x-0.3f, y-3f, z-1.5f)
                collisionBox3Dp2 = Vector3f(x+0.3f, y+3f, z+1.5f)
                collisionAlmostBox3Dp1 = Vector3f(x-1f, y-3f, z-1.5f)
                collisionAlmostBox3Dp2 = Vector3f(x+1f, y+3f, z+1.5f)
            }
            else if (roty == 270f) {
                collisionBox3Dp1 = Vector3f(x-1.5f, y-3f, z-0.3f)
                collisionBox3Dp2 = Vector3f(x+1.5f, y+3f, z+0.3f)
                collisionAlmostBox3Dp1 = Vector3f(x-1.5f, y-3f, z-1f)
                collisionAlmostBox3Dp2 = Vector3f(x+1.5f, y+3f, z+1f)
            }
            else if (roty == 90f) {
                collisionBox3Dp1 = Vector3f(x-1.5f, y-3f, z-0.3f)
                collisionBox3Dp2 = Vector3f(x+1.5f, y+3f, z+0.3f)
                collisionAlmostBox3Dp1 = Vector3f(x-1.5f, y-3f, z-1f)
                collisionAlmostBox3Dp2 = Vector3f(x+1.5f, y+3f, z+1f)
            }


            camera.parent = portalCam
            goingOutCoord = Vector3f(portalCam.getWorldPosition().x, portalCam.getWorldPosition().y, portalCam.getWorldPosition().z)

            //Debugging
            //println(portalCam.getWorldPosition() == camera.getWorldPosition())
            //println(camera.getWorldPosition())
        }
    }

    // Starts rendering to Framebuffer
    fun renderToFramebufferStart(shaderProgram: ShaderProgram) {
        framebuffer.startRender(shaderProgram); GLError.checkThrow()
    }

    // Stops rendering to Framebuffer
    fun renderToFramebufferStop() {
        framebuffer.stopRender(); GLError.checkThrow()
    }

    // Binds Portal Camera
    fun bindPortalCamera(shaderProgram: ShaderProgram) {
        camera.bind(shaderProgram)
    }
    // Binds Portal Camera
    fun bindPortalCameraViewMatrix(shaderProgram: ShaderProgram) {
        camera.bindViewMatrix(shaderProgram)
    }
    // Binds Portal Camera
    fun bindPortalCameraPRojectionMatrix(shaderProgram: ShaderProgram) {
        camera.bindProjectionMatrix(shaderProgram)
    }

    // Renders portals with the portalShader (which will be set when calling this function)
    fun render(shaderProgram: ShaderProgram, dt: Float) {

        //Portal animation (grow)
        if (portalWallScale < 1f) {
            portalWallScale += 2f * dt
            portalWall.scaleLocal(Vector3f(1.18f, portalWallScale, 0.0f+portalWallScale))
            portalFrame?.scaleLocal(Vector3f(1f, portalWallScale, portalWallScale))
        }

        shaderProgram.use()

        shaderProgram.setUniform("isPortal", 1.0f)
        portalWall.render(shaderProgram)
        shaderProgram.setUniform("isPortal", 0.0f)
        portalFrame?.render(shaderProgram)

        /*GL43.glCopyImageSubData(framebuffer.framebufferTexture.texID, GL11.GL_TEXTURE_2D, 0, 0, 0, 0,
                texture.texID, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, framebuffer.width, framebuffer.height, 1); GLError.checkThrow()*/

        //Only for debugging
        //portalCam.render(shaderProgram) //This is not a camera, but rather a 3d object that shows the position and rotation of the camera. Used for debugging.
    }

    fun renderWithPortalCheck(shaderProgram: ShaderProgram, otherPortal: Portal, dt: Float) {
        if (pointDistance(this.x, this.y, otherPortal.x, otherPortal.y) >= 1.5f) {
            render(shaderProgram, dt)
        }
    }

    // Renders only the portal frame (not used right now)
    fun renderFrameOnly(shaderProgram: ShaderProgram) {
        shaderProgram.use()

        shaderProgram.setUniform("isPortal", 0.0f)
        portalFrame?.render(shaderProgram)
    }

    // Check for collision - If a point is inside a rectangle -> Collide
    fun checkCollision(check_x: Float, check_y: Float, check_z: Float) : Boolean {
        if (check_x >= collisionBox3Dp1.x && check_x <= collisionBox3Dp2.x
                && check_y >= collisionBox3Dp1.y && check_y <= collisionBox3Dp2.y
                && check_z >= collisionBox3Dp1.z && check_z <= collisionBox3Dp2.z) {
            return true
        }

        return false
    }

    // Check if the player is near the portal, but not inside
    fun checkAlmostCollision(check_x: Float, check_y: Float, check_z: Float) : Boolean {
        if (check_x >= collisionAlmostBox3Dp1.x && check_x <= collisionAlmostBox3Dp2.x
                && check_y >= collisionAlmostBox3Dp1.y && check_y <= collisionAlmostBox3Dp2.y
                && check_z >= collisionAlmostBox3Dp1.z && check_z <= collisionAlmostBox3Dp2.z) {
            return true
        }

        return false
    }

    // Returns distance from point a to point b in 2d
    fun pointDistance(x1: Float, y1: Float, x2: Float, y2: Float):Float = Math.sqrt((x2-x1).pow(2) + (y2-y1).pow(2))

    // Set the position and rotation of the portal
    fun setPositionRotation(pos: Vector4f, colPool: CollisionPool, level: Renderable?) {

        initSet = pos

        x = pos.x
        y = Math.max(3f,pos.y)
        z = pos.z

        roty = pos.w

        //portalRecCollision.updateCollision(x-1f, y-3f, z-1f,x+1f, y+1f, z+1f)
        portalRecCollision.updateCollision(x-1f, y-3f, z-1f,x+1f, y, z+1f)

        //Check if portal is maybe inside the floor
        val colFloor = level?.checkCollisionWithPortal(this)!!

        if (colFloor.y2 != 0f && colFloor.y2 >= y-3f) {
            y = colFloor.y2 + 3f
        }

        initSet.y = y

        //Portal & Frame transformation
        portalWall.setRotation(Math.toRadians(rotx),Math.toRadians(roty),Math.toRadians(rotz))
        portalWall.setPosition(x,y,z)
        portalWall.translateLocal(Vector3f(-0.234f,0f,0f))
        portalWall.scaleLocal(Vector3f(1.18f, 0.81f, 0.81f))

        portalFrame?.setRotation(Math.toRadians(rotx),Math.toRadians(roty),Math.toRadians(rotz))
        portalFrame?.setPosition(x,y,z)
        portalFrame?.translateLocal(Vector3f(-0.234f,0f,0f))
        portalFrame?.scaleLocal(Vector3f(0.8f, 0.8f, 0.8f))

        //Start portal animation
        portalWallScale = 0f

        // Update X, Y, Z
        x = portalWall.x()
        y = portalWall.y()
        z = portalWall.z()
    }

    //function that should help reduce or eliminate z fighting
    fun setPositionRotationClamp(player: Renderable) {
        val pos = initSet
        x = pos.x
        y = Math.max(3f,pos.y)
        z = pos.z

        roty = pos.w

        val clampValueMin = -0.2365f//-0.235995f
        val clampValueMax = -0.234f

        val distanceToPlayer = pointDistance(x,z,player.x(),player.z())

        val clampedDistance = Math.min(clampValueMax,Math.max(distanceToPlayer/1000 + clampValueMin, -0.235990f))

        //println(clampedDistance)

        //Portal & Frame transformation
        portalWall.setRotation(Math.toRadians(rotx),Math.toRadians(roty),Math.toRadians(rotz))
        portalWall.setPosition(x,y,z)
        portalWall.translateLocal(Vector3f(clampedDistance,0f,0f))
        portalWall.scaleLocal(Vector3f(1.18f, 0.81f, 0.81f))

        portalFrame?.setRotation(Math.toRadians(rotx),Math.toRadians(roty),Math.toRadians(rotz))
        portalFrame?.setPosition(x,y,z)
        portalFrame?.translateLocal(Vector3f(clampedDistance,0f,0f))
        portalFrame?.scaleLocal(Vector3f(0.8f, 0.8f, 0.8f))

        // Update X, Y, Z
        x = portalWall.x()
        y = portalWall.y()
        z = portalWall.z()
    }

    // For portal in portal rendering, so z-fighting doesn't happen
    fun setToInitPos() {
        //Portal & Frame transformation
        portalWall.setRotation(Math.toRadians(rotx),Math.toRadians(roty),Math.toRadians(rotz))
        portalWall.setPosition(initSet.x,initSet.y,initSet.z)
        portalWall.translateLocal(Vector3f(-0.234f,0f,0f))
        portalWall.scaleLocal(Vector3f(1.18f, 0.81f, 0.81f))

        portalFrame?.setRotation(Math.toRadians(rotx),Math.toRadians(roty),Math.toRadians(rotz))
        portalFrame?.setPosition(initSet.x,initSet.y,initSet.z)
        portalFrame?.translateLocal(Vector3f(-0.234f,0f,0f))
        portalFrame?.scaleLocal(Vector3f(0.8f, 0.8f, 0.8f))
    }

}