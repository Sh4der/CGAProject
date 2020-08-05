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
import cga.framework.OBJLoader
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.system.exitProcess

class Portal(val window: GameWindow, val screenShader: ShaderProgram, var x: Float = 0.0f, var y: Float = 0.0f, var z: Float = 0.0f, var rotx: Float = 90f, var roty: Float = 180f, var rotz: Float = 90f) {
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
    private val camera : TronCamera


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
        val resPortalWall : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/ground.obj")
        //Get the first mesh of the first object
        val portalWallMesh: OBJLoader.OBJMesh = resPortalWall.objects[0].meshes[0]
        val meshPortalWall = Mesh(portalWallMesh.vertexData, portalWallMesh.indexData, vertexAttributes, portalMaterial)
        val meshPortalWallCam = Mesh(portalWallMesh.vertexData, portalWallMesh.indexData, vertexAttributes, portalMaterialCam)

        portalWall = Renderable(mutableListOf(meshPortalWall))

        portalCam = Renderable(mutableListOf(meshPortalWallCam)) //For visualizing the camera for each portal
        portalCam.meshes[0].material?.emitColor = Vector3f(0f, 1f, 0f)
        portalCam.scaleLocal(Vector3f(0.1f))

        //Test transformations
        portalWall.rotateLocal(rotx,roty,rotz)
        portalWall.translateGlobal(Vector3f(x,y,z))
        portalWall.scaleLocal(Vector3f(0.256f,0.144f,0.144f))

        //Create Framebuffer
        framebuffer = GeometryFramebuffer(window.framebufferWidth, window.framebufferHeight)

        //Define camera
        camera = TronCamera()
        //camera.rotateLocal(0f,-90f,0f)
        camera.translateLocal(Vector3f(0f,2f,0f))
        //camera.translateGlobal(Vector3f(0f))
    }

    fun setCameraParent(p: Renderable, c: Renderable?) {
        val pWorldPos = p.getWorldPosition()
        val playerWorldPos = c?.getWorldPosition()
        if (playerWorldPos == null) {
            exitProcess(0)
        }
        else {
            //println(playerWorldPos.z + pWorldPos.z - portalWall.getWorldPosition().z)

            camera.setRotationA(c.getRotationA())
            camera.setPosition(playerWorldPos.x + pWorldPos.x - portalWall.getWorldPosition().x, 2f, playerWorldPos.z + pWorldPos.z - portalWall.getWorldPosition().z)
            //println(camera.getWorldPosition())
            //camera.parent = p

            //portalCam.setRotationA(c.getRotationA())
            portalCam.scaleLocal(Vector3f(0.1f))
            portalCam.setPosition(camera.getWorldPosition().x, 1f, camera.getWorldPosition().z)

            //println(portalCam.getWorldPosition() == camera.getWorldPosition())
            //println(camera.getWorldPosition())
        }
    }

    fun generateTexture() {
        screenShader.use(); GLError.checkThrow()
        val currentImage = framebuffer.gAlbedo
        currentImage.bind(0)
        screenShader.setUniform("tex", 0)
        portalTexture = framebuffer.gAlbedo
    }

    fun renderToFramebufferStart(shaderProgram: ShaderProgram) {
        framebuffer.startRender(shaderProgram); GLError.checkThrow()
        camera.bind(shaderProgram)
    }

    fun renderToFramebufferStop() {
        framebuffer.stopRender(); GLError.checkThrow()
    }

    fun render(shaderProgram: ShaderProgram) {
        //portalTexture = tex

        portalMaterial = Material(portalTexture, portalTexture, portalTexture, 1000f, Vector2f(1.0f, 1.0f)); GLError.checkThrow()
        portalWall.meshes[0].material = portalMaterial

        portalWall.render(shaderProgram)
        //portalCam.render(shaderProgram)
    }

}