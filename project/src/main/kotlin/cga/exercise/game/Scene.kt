package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.framebuffer.GeometryFramebuffer
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import cga.framework.OBJLoader
import cga.framework.OBJLoader.OBJMesh
import cga.framework.OBJLoader.OBJResult
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import java.nio.ByteBuffer
import javax.swing.Spring.height
import kotlin.system.exitProcess


/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {

    private val staticShader: ShaderProgram
    private val gBufferShader : ShaderProgram
    private val screenShader : ShaderProgram

    private val cam : TronCamera

    private var ground : Renderable
    private var lightCycle : Renderable?

    private var pointLight : PointLight
    private var spotLight : SpotLight

    private val screenQuadMesh : Mesh

    val testTex :Texture2D

    private var currentImage :Int

    private val gBufferObject : GeometryFramebuffer

    //scene setup
    init {

        //initial opengl state
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glDisable(GL_CULL_FACE); GLError.checkThrow()
        //glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()

        staticShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        gBufferShader = ShaderProgram("assets/shaders/g_Buffer_vert.glsl", "assets/shaders/g_Buffer_frag.glsl")
        screenShader = ShaderProgram("assets/shaders/screen_vert.glsl", "assets/shaders/screen_frag.glsl")

        //Create the mesh
        val stride: Int = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0) //position
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4) //textureCoordinate
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4) //normalval
        val vertexAttributes = arrayOf<VertexAttribute>(attrPos, attrTC, attrNorm)


        lightCycle = ModelLoader.loadModel("assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj", Math.toRadians(-90f), Math.toRadians(90f), 0f)
        if(lightCycle == null)
        {
            exitProcess(1)
        }
        lightCycle?.meshes?.get(2)?.material?.emitColor = Vector3f(1f, 0f, 0f)
        lightCycle?.scaleLocal(Vector3f(0.8f))


        val diffTex = Texture2D("assets/textures/ground_diff.png", true)
        diffTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST)
        val emitTex = Texture2D("assets/textures/ground_emit.png", true)
        emitTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        val specTex = Texture2D("assets/textures/ground_spec.png", true)
        specTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        println(emitTex.texID)
        val groundMaterial = Material(diffTex,
                                    emitTex,
                                    specTex,
                            50f,
                                    Vector2f(64.0f, 64.0f)); GLError.checkThrow()


        //load an object and create a mesh
        val resGround : OBJResult = OBJLoader.loadOBJ("assets/models/ground.obj")
        //Get the first mesh of the first object
        val groundMesh: OBJMesh = resGround.objects[0].meshes[0]
        val meshGround = Mesh(groundMesh.vertexData, groundMesh.indexData, vertexAttributes, groundMaterial)

        ground = Renderable(mutableListOf(meshGround))
        ground.meshes[0].material?.emitColor = Vector3f(0f, 1f, 0f)


        cam = TronCamera()
        cam.rotateLocal(-35f, 0f, 0f)
        cam.translateLocal(Vector3f(0f,  0f, 4f))

        cam.parent = lightCycle!!

        pointLight = PointLight(Vector3f(0f, 1f, 0f), Vector3i(255, 0, 255))
        pointLight.parent = lightCycle

        spotLight = SpotLight(Vector3f(0f, 1f, 0f), Vector3i(255, 255, 255), 16.5f, 20.5f)

        spotLight.parent = lightCycle


        val quadArray = floatArrayOf(
                -1f, -1f,  0f, 0f,
                1f, 1f, 1f, 1f,
                -1f, 1f,  0f, 1f,
                1f, -1f, 1f, 0f
        )

        val quadIndices = intArrayOf(
                0, 1, 2,
                0, 3, 1
        )


        //Create the mesh
        val strideScreenQuad: Int = 4 * 4
        val attrPosScreenQuad = VertexAttribute(2, GL_FLOAT, strideScreenQuad, 0) //position
        val attrTCScreenQuad = VertexAttribute(2, GL_FLOAT, strideScreenQuad, 2 * 4) //textureCoordinate
        val vertexAttributesScreenQuad = arrayOf<VertexAttribute>(attrPosScreenQuad, attrTCScreenQuad)

        screenQuadMesh = Mesh(quadArray, quadIndices, vertexAttributesScreenQuad, null)


        testTex = Texture2D("assets/textures/index.jpg", false)
        testTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST)


        gBufferObject = GeometryFramebuffer(window.framebufferWidth, window.framebufferHeight)
        currentImage = gBufferObject.gPosition.texID

    }

    fun render(dt: Float, t: Float) {


        gBufferObject.startRender(gBufferShader)
        cam.bind(gBufferShader); GLError.checkThrow()
        ground.render(gBufferShader); GLError.checkThrow()
        lightCycle?.render(gBufferShader); GLError.checkThrow()
        gBufferObject.stopRender()



        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glClear(GL_COLOR_BUFFER_BIT); GLError.checkThrow()
        glDisable(GL_DEPTH_TEST)
        screenShader.use(); GLError.checkThrow()
        //testTex.bind(0)
        //screenShader.setUniform("specTex", 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, currentImage)
        screenShader.setUniform("tex", 0)
        screenQuadMesh.render(); GLError.checkThrow()
    }


    fun update(dt: Float, t: Float) {
        var speed = 0f
        var rotationDirection = 0f
        val turningCycleRadius = 3f

        if(window.getKeyState(GLFW.GLFW_KEY_W)) {
            speed = -5f
        }
        else if(window.getKeyState(GLFW.GLFW_KEY_S)) {
            speed = 5f
        }
        if(window.getKeyState(GLFW.GLFW_KEY_A)) {
            rotationDirection = -1f
        }
        else if(window.getKeyState(GLFW.GLFW_KEY_D)) {
            rotationDirection = 1f
        }

        if(rotationDirection == 0f){
            lightCycle?.translateLocal(Vector3f(0f, 0f, speed * dt))
        }
        else if(speed != 0f)
        {
            lightCycle?.rotateAroundPoint(0f,  (360 * speed)/(2f*Math.PI.toFloat() * turningCycleRadius) * rotationDirection * dt, 0f, lightCycle!!.getWorldPosition().add(lightCycle!!.getXAxis().mul(turningCycleRadius*rotationDirection)))
        }
        lightCycle?.meshes?.get(2)?.material?.emitColor = Vector3f((Math.sin(t) + 1f)/2, (Math.sin(t*2) + 1f)/2, (Math.sin(t*3) + 1f)/2)

        if(window.getKeyState(GLFW.GLFW_KEY_1)) {
            currentImage = gBufferObject.gPosition.texID
        }else if(window.getKeyState(GLFW.GLFW_KEY_2)) {
            currentImage = gBufferObject.gNormal.texID
        }else if(window.getKeyState(GLFW.GLFW_KEY_3)) {
            currentImage = gBufferObject.gAlbedo.texID
        }
    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    var oldMousePosX = 0.0
    var oldMousePosY = 0.0

    fun onMouseMove(xpos: Double, ypos: Double) {

        //cam.rotateAroundPoint((oldMousePosY-ypos).toFloat() * 0.002f, (oldMousePosX - xpos).toFloat() * 0.002f, 0f, Vector3f(0f))
        cam.rotateAroundPoint(0f, (oldMousePosX - xpos).toFloat() * 0.02f, 0f, Vector3f(0f))

        oldMousePosX = xpos
        oldMousePosY = ypos
    }


    fun cleanup() {}
}
