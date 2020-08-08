package cga.exercise.components.gameobjects

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.framebuffer.GeometryFramebuffer
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

    private var framebuffer : GeometryFramebuffer
    private var portalTexture : Texture2D
    private var portalCamTexture : Texture2D
    private var portalMaterial : Material
    private var portalMaterialCam : Material
    val portalWall : Renderable
    val portalCam : Renderable
    val portalFrame : Renderable?
    private val camera : TronCamera
    private var collisionBox3Dp1 : Vector3f //part1
    private var collisionBox3Dp2 : Vector3f //part2

    var goingOutCoord = Vector3f(0f)


    init {
        //Create the mesh
        val stride: Int = 8 * 4
        val attrPos = VertexAttribute(3, GL11.GL_FLOAT, stride, 0) //position
        val attrTC = VertexAttribute(2, GL11.GL_FLOAT, stride, 3 * 4) //textureCoordinate
        val attrNorm = VertexAttribute(3, GL11.GL_FLOAT, stride, 5 * 4) //normalval
        val vertexAttributes = arrayOf<VertexAttribute>(attrPos, attrTC, attrNorm)

        //Use Ground Textures as default textures, 'cause I'm too lazy to create default portal textures
        portalTexture = Texture2D("assets/textures/ground_diff.png", true)
        portalCamTexture = Texture2D("assets/textures/ground_diff.png", true)
        portalTexture.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_NEAREST, GL11.GL_NEAREST)
        portalMaterial = Material(portalTexture, portalTexture, portalTexture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()
        portalMaterialCam = Material(portalCamTexture, portalCamTexture, portalCamTexture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()

        //load an object and create a mesh
        val resPortalWall : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/portal/portal.obj")

        val portalWallMesh: OBJLoader.OBJMesh = resPortalWall.objects[0].meshes[0]
        val meshPortalWall = Mesh(portalWallMesh.vertexData, portalWallMesh.indexData, vertexAttributes, portalMaterial)
        val meshPortalWallCam = Mesh(portalWallMesh.vertexData, portalWallMesh.indexData, vertexAttributes, portalMaterialCam)

        portalWall = Renderable(mutableListOf(meshPortalWall))

        portalCam = Renderable(mutableListOf(meshPortalWallCam)) //For visualizing the camera for each portal
        portalCam.meshes[0].material?.emitColor = Vector3f(0f, 1f, 0f)
        portalCam.scaleLocal(Vector3f(0.1f))
        portalCam.rotateLocal(0f, roty, 0f)

        //Load in Portal Frame
        /*val resPortalFrame : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj")
        val portalFrameMesh: OBJLoader.OBJMesh = resPortalFrame.objects[0].meshes[0]
        val meshPortalFrame = Mesh(portalWallMesh.vertexData, portalFrameMesh.indexData, vertexAttributes, portalMaterialCam)

        portalFrame = Renderable(mutableListOf(meshPortalFrame))*/
        //portalFrame = ModelLoader.loadModel("assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj", Math.toRadians(0f), Math.toRadians(180f), 0f)
        portalFrame = ModelLoader.loadModel("assets/models/portal/portal_frame.obj", Math.toRadians(0f), Math.toRadians(180f), 0f)

        //Portal & Frame transformation
        portalWall.rotateLocal(rotx,roty,rotz)
        portalWall.translateGlobal(Vector3f(x,y,z))
        portalWall.scaleLocal(Vector3f(1.2f, 0.8f, 0.8f))

        portalFrame?.rotateLocal(rotx,roty,rotz)
        portalFrame?.translateGlobal(Vector3f(x,y,z))
        portalFrame?.scaleLocal(Vector3f(1f, 0.8f, 0.8f))

        //Create Framebuffer
        framebuffer = GeometryFramebuffer(window.framebufferWidth, window.framebufferHeight)

        //Define camera
        camera = TronCamera()
        //camera.rotateLocal(0f,-90f,0f)
        camera.translateLocal(Vector3f(0f,2f,0f))
        //camera.translateGlobal(Vector3f(0f))

        //Create Collisionbox
        collisionBox3Dp1 = Vector3f(x-0.15f, y-3f, z-2f)
        collisionBox3Dp2 = Vector3f(x+0.15f, y+3f, z+2f)
    }

    // Set Camera Parents is now reworked and does something different.
    // It sets the position & rotation of the portal cameras (relative to player and portal position)
    fun setCameraParent(p: Portal, c: Renderable?) {
        val pWorldPos = p.portalWall.getWorldPosition()
        val playerWorldPos = c?.getWorldPosition()
        if (playerWorldPos == null) {
            exitProcess(0)
        }
        else {
            /*
            Set Portal Cam with Matrix Multiplication -> I can't make it work...
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


            // Very messy portal-player transition code, but it works.
            var rangecheck = 1

            if (pointDistance(portalWall.getWorldPosition().x, portalWall.getWorldPosition().y, 0f,0f) < pointDistance(pWorldPos.x, pWorldPos.y, 0f, 0f)) {
                rangecheck = -1
            }
            else {
                rangecheck = 1
            }

            // Camera rotation
            val camdir = (c.getYDirDeg() + (p.roty - roty) + (180f * rangecheck.toFloat())).toFloat()
            camera.setModelMatrix(Matrix4f())//setRotation(0f,camdir,0f)
            camera.rotateLocal(0f,camdir+270,0f)

            val checkdir = ((p.roty - roty) + (180 * rangecheck)).toInt()

            println(camdir)

            if (checkdir == 0 || checkdir == 360 || checkdir == -360) {
                val setx = playerWorldPos.x - portalWall.getWorldPosition().x + pWorldPos.x
                val setz = playerWorldPos.z - portalWall.getWorldPosition().z + pWorldPos.z
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                camera.setPosition(setx, sety, setz)
            }

            if (checkdir == 180 || checkdir == -180) {
                val setx = -(playerWorldPos.x - portalWall.getWorldPosition().x) + pWorldPos.x
                val setz = -(playerWorldPos.z - portalWall.getWorldPosition().z) + pWorldPos.z
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                camera.setPosition(setx, sety, setz)
            }

            if (checkdir == 90 || checkdir == -270 || checkdir == 450) {
                val setz = -(playerWorldPos.x - portalWall.getWorldPosition().x) + pWorldPos.z
                val setx = playerWorldPos.z - portalWall.getWorldPosition().z + pWorldPos.x
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                camera.setPosition(setx, sety, setz)
            }

            if (checkdir == -90 || checkdir == 270 || checkdir == -450) {
                val setz = playerWorldPos.x - portalWall.getWorldPosition().x + pWorldPos.z
                val setx = -(playerWorldPos.z - portalWall.getWorldPosition().z) + pWorldPos.x
                val sety = playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y + 2f
                camera.setPosition(setx, sety, setz)
            }

            // Set the coordinates to where the player should get out
            // Also, update Collisionbox
            if (roty == 0f) {
                collisionBox3Dp1 = Vector3f(x-0.15f, y-3f, z-2f)
                collisionBox3Dp2 = Vector3f(x+0.15f, y+3f, z+2f)
            }
            else if (roty == 180f) {
                collisionBox3Dp1 = Vector3f(x-0.15f, y-3f, z-2f)
                collisionBox3Dp2 = Vector3f(x+0.15f, y+3f, z+2f)
            }
            else if (roty == 270f) {
                collisionBox3Dp1 = Vector3f(x-2f, y-3f, z-0.15f)
                collisionBox3Dp2 = Vector3f(x+2f, y+3f, z+0.15f)
            }
            else if (roty == 90f) {
                collisionBox3Dp1 = Vector3f(x-2f, y-3f, z-0.15f)
                collisionBox3Dp2 = Vector3f(x+2f, y+3f, z+0.15f)
            }

            if (p.roty == 0f) {
                goingOutCoord = Vector3f(portalCam.getWorldPosition().x + 0.35f, portalCam.getWorldPosition().y, portalCam.getWorldPosition().z)
            }
            else if (p.roty == 180f) {
                goingOutCoord = Vector3f(portalCam.getWorldPosition().x - 0.35f, portalCam.getWorldPosition().y, portalCam.getWorldPosition().z)
            }
            else if (p.roty == 270f) {
                goingOutCoord = Vector3f(portalCam.getWorldPosition().x, portalCam.getWorldPosition().y, portalCam.getWorldPosition().z + 0.35f)
            }
            else if (p.roty == 90f) {
                goingOutCoord = Vector3f(portalCam.getWorldPosition().x, portalCam.getWorldPosition().y, portalCam.getWorldPosition().z - 0.35f)
            }

            //camera.setRotationA(c.getRotationA())
            //camera.setPosition(playerWorldPos.x + pWorldPos.x - portalWall.getWorldPosition().x, playerWorldPos.y + pWorldPos.y - portalWall.getWorldPosition().y+2f, playerWorldPos.z + pWorldPos.z - portalWall.getWorldPosition().z)

            //Needed to transition the player from portal a to portal b
            portalCam.setRotationA(camera.getRotationA())
            portalCam.scaleLocal(Vector3f(0.1f))
            portalCam.setPosition(camera.getWorldPosition().x, camera.getWorldPosition().y-2f, camera.getWorldPosition().z)

            //Debugging
            //println(portalCam.getWorldPosition() == camera.getWorldPosition())
            //println(camera.getWorldPosition())
        }
    }

    // Generates portal texture
    fun generateTexture() {
        screenShader.use(); GLError.checkThrow()
        val currentImage = framebuffer.gAlbedo
        currentImage.bind(0)
        screenShader.setUniform("tex", 0)
        portalTexture = framebuffer.gAlbedo
    }

    // Starts rendering to Framebuffer
    fun renderToFramebufferStart(shaderProgram: ShaderProgram) {
        framebuffer.startRender(shaderProgram); GLError.checkThrow()
        camera.bind(shaderProgram)
    }

    // Stops rendering to Framebuffer
    fun renderToFramebufferStop() {
        framebuffer.stopRender(); GLError.checkThrow()
    }

    // Binds Portal Camera
    fun bindPortalCamera(shaderProgram: ShaderProgram) {
        camera.bind(shaderProgram)
    }

    // Renders portals with the portalShader (which will be set when calling this function)
    fun render(shaderProgram: ShaderProgram) {

        portalMaterial = Material(portalTexture, portalTexture, portalTexture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()
        portalWall.meshes[0].material = portalMaterial

        shaderProgram.setUniform("frameColor", Vector3f(1f, 1f, 1f))
        portalWall.render(shaderProgram)
        shaderProgram.setUniform("frameColor", frameColor)
        portalFrame?.render(shaderProgram)

        //Only for debugging
        //portalCam.render(shaderProgram) //This is not a camera, but rather a 3d object that shows the position and rotation of the camera. Used for debugging.
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

    fun pointDistance(x1: Float, y1: Float, x2: Float, y2: Float):Float {
        val distance = Math.sqrt(
                (x2-x1).pow(2) + (y2-y1).pow(2)
        )

        return distance
    }

}