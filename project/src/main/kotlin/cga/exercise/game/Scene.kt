package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.framebuffer.BlurFramebuffer
import cga.exercise.components.framebuffer.GeometryFramebuffer
import cga.exercise.components.framebuffer.SSAOTextureFramebuffer
import cga.exercise.components.gameobjects.Portal
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
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import kotlin.system.exitProcess


/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {

    private val staticShader: ShaderProgram
    private val gBufferShader : ShaderProgram
    private val screenShader : ShaderProgram
    private val ssaoColorShader : ShaderProgram
    private val blurShader : ShaderProgram
    private val lightningShader : ShaderProgram
    private val portalShader : ShaderProgram

    private val cam : TronCamera
    private val camOverview : TronCamera

    private var ground : Renderable
    private var wall : Renderable
    private var wall2 : Renderable

    private var lightCycle : Renderable?

    private var pointLight : PointLight
    private var spotLight : SpotLight

    private val screenQuadMesh : Mesh

    val testTex :Texture2D

    private var currentImage : Texture2D

    private val gBufferObject : GeometryFramebuffer
    private val ssaoTextureFramebuffer :SSAOTextureFramebuffer
    private val blurFramebuffer : BlurFramebuffer



    //Portal vars
    private var portal1 : Portal
    private var portal2 : Portal

    private val rob : Renderable?


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
        ssaoColorShader = ShaderProgram("assets/shaders/screen_vert.glsl", "assets/shaders/ssaoColor_frag.glsl")
        blurShader = ShaderProgram("assets/shaders/screen_vert.glsl", "assets/shaders/textureBlur_frag.glsl")
        lightningShader = ShaderProgram("assets/shaders/screen_vert.glsl", "assets/shaders/ssaoLightning_frag.glsl")
        screenShader = ShaderProgram("assets/shaders/screen_vert.glsl", "assets/shaders/screen_frag.glsl")
        portalShader = ShaderProgram("assets/shaders/portal_vert.glsl", "assets/shaders/portal_frag.glsl")

        //Create the mesh
        val stride: Int = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0) //position
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4) //textureCoordinate
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4) //normalval
        val vertexAttributes = arrayOf<VertexAttribute>(attrPos, attrTC, attrNorm)


        //lightCycle = ModelLoader.loadModel("assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj", Math.toRadians(-90f), Math.toRadians(90f), 0f)
        lightCycle = ModelLoader.loadModel("assets/chell/0.obj", Math.toRadians(0f), Math.toRadians(180f), 0f)
        if(lightCycle == null)
        {
            exitProcess(1)
        }
        lightCycle?.meshes?.get(2)?.material?.emitColor = Vector3f(1f, 0f, 0f)
        lightCycle?.scaleLocal(Vector3f(1f))


        val diffTex = Texture2D("assets/textures/con_wall_1.png", true)
        diffTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST)
        val emitTex = Texture2D("assets/textures/con_wall_1.png", true)
        emitTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        val specTex = Texture2D("assets/textures/con_wall_1.png", true)
        specTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

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
        ground.meshes[0].material?.emitColor = Vector3f(1f, 1f, 1f)

        wall = Renderable(mutableListOf(meshGround))
        wall.rotateLocal(90f, 0f, 0f)
        wall2 = Renderable(mutableListOf(meshGround))
        wall2.rotateLocal(90f, 0f, 90f)


        cam = TronCamera()
        cam.rotateLocal(0f, 0f, 0f)
        cam.translateLocal(Vector3f(0f, 2f, 0f))
        cam.parent = lightCycle!!

        camOverview = TronCamera()
        camOverview.rotateLocal(-45f, 0f, 0f)
        camOverview.translateLocal(Vector3f(0f, 10f, 30f))
        //camOverview.parent = lightCycle!!

        pointLight = PointLight(Vector3f(0f, 1f, 0f), Vector3i(255, 255, 255))
        pointLight.parent = lightCycle

        spotLight = SpotLight(Vector3f(0f, 1f, 0f), Vector3i(255, 255, 255), 16.5f, 20.5f)

        spotLight.parent = lightCycle


        val quadArray = floatArrayOf(
                -1f, -1f, 0f, 0f,
                1f, 1f, 1f, 1f,
                -1f, 1f, 0f, 1f,
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
        ssaoTextureFramebuffer = SSAOTextureFramebuffer(window.framebufferWidth, window.framebufferHeight)
        blurFramebuffer = BlurFramebuffer(window.framebufferWidth, window.framebufferHeight)
        currentImage = blurFramebuffer.blurFramebufferTexture


        //Portal Setup
        portal1 = Portal(window, screenShader, Vector3f(11f / 255f, 106 / 255f, 230 / 255f), -8f, 3f, -5f, 0f, 270f, 0f)
        portal2 = Portal(window, screenShader, Vector3f(230f / 255f, 106 / 255f, 11 / 255f), 8.1f, 3f, 5f, 0f, 180f, 0f)


        rob = ModelLoader.loadModel("J:/untitled.dae", 0f, 0f, 0f)
        rob?.scaleLocal(Vector3f(0.3f))
        rob?.meshes?.get(0)?.material?.emit = emitTex
        rob?.meshes?.get(0)?.material?.specular = specTex
        rob?.meshes?.get(0)?.material?.diff = diffTex

    }

    fun render(dt: Float, t: Float) {

        //------------------------Lukas---------------------//

        gBufferObject.startRender(gBufferShader)
        cam.bind(gBufferShader); GLError.checkThrow()
        ground.render(gBufferShader); GLError.checkThrow()
        lightCycle?.render(gBufferShader); GLError.checkThrow()
        rob?.render(gBufferShader)
        wall.render(gBufferShader)
        wall2.render(gBufferShader)
        gBufferObject.stopRender()

        ssaoTextureFramebuffer.startRender(ssaoColorShader, gBufferObject)
        cam.bind(ssaoColorShader)
        screenQuadMesh.render()
        ssaoTextureFramebuffer.stopRender()

        blurFramebuffer.startRender(blurShader, ssaoTextureFramebuffer.ssaoColorTexture)
        screenQuadMesh.render()
        blurFramebuffer.stopRender()

        //
        //Rendert auf den Bildschirm kann aus kommentiert werden
        //
        /*glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glClear(GL_COLOR_BUFFER_BIT); GLError.checkThrow()
        glDisable(GL_DEPTH_TEST)
        screenShader.use(); GLError.checkThrow()
        currentImage.bind(0)
        screenShader.setUniform("tex", 0)
        screenQuadMesh.render(); GLError.checkThrow()*/

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glClear(GL_COLOR_BUFFER_BIT); GLError.checkThrow()
        glDisable(GL_DEPTH_TEST)

        lightningShader.use(); GLError.checkThrow()
        cam.bind(lightningShader)
        pointLight.bind(lightningShader, "pointlight.")
        lightningShader.setUniform("lightPosition", pointLight.getWorldPosition())
        gBufferObject.gPosition.bind(0)
        lightningShader.setUniform("gPosition", 0)
        gBufferObject.gNormal.bind(1)
        lightningShader.setUniform("gNormal", 1)
        gBufferObject.gAlbedo.bind(2)
        lightningShader.setUniform("gAlbedo", 2)
        blurFramebuffer.blurFramebufferTexture.bind(3)
        lightningShader.setUniform("ssao", 3)
        screenQuadMesh.render(); GLError.checkThrow()

        //println("${buf.get(0)} : ${buf.get(1)} : ${buf.get(2)} : ${buf.get(3)}")

        /*glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT); GLError.checkThrow()
        glEnable(GL11.GL_DEPTH_TEST)
        staticShader.use()
        cam.bind(staticShader)
        blurFramebuffer.blurFramebufferTexture.bind(5)
        staticShader.setUniform("ssao", 5)
        pointLight.bind(staticShader, "pointLight")
        spotLight.bind(staticShader,"spotLight", Matrix4f())
        lightCycle?.render(staticShader)
        ground.render(staticShader)
        rob?.render(staticShader)*/


        //------------------------Janine---------------------//

        //
        //Rendert auf den Bildschirm kann aus kommentiert werden
        //
        /*glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT); GLError.checkThrow()
        glEnable(GL11.GL_DEPTH_TEST)
        staticShader.use()
        cam.bind(staticShader)
        pointLight.bind(staticShader, "pointLight")
        spotLight.bind(staticShader,"spotLight", Matrix4f())
        lightCycle?.render(staticShader)
        ground.render(staticShader)*/


        //------------------------Nico---------------------//
        //Set the cameras for the two portals
        portal1.setCameraParent(portal2, lightCycle)
        portal2.setCameraParent(portal1, lightCycle)

        //Render Texture from portal cameras
        portal1.generateTexture()
        portal2.generateTexture()

        //
        //Render auf FBO -> Portals
        //
        portal1.renderToFramebufferStart(staticShader)
        //cam.bind(staticShader)
        pointLight.bind(staticShader, "pointLight")
        spotLight.bind(staticShader, "spotLight", Matrix4f())
        lightCycle?.render(staticShader)
        ground.render(staticShader)
        portalShader.use()
        portal1.bindPortalCamera(portalShader)
        portal1.render(portalShader)
        //portal2.render(portalShader)
        portal1.renderToFramebufferStop()

        portal2.renderToFramebufferStart(staticShader)
        //cam.bind(staticShader)
        pointLight.bind(staticShader, "pointLight")
        spotLight.bind(staticShader, "spotLight", Matrix4f())
        lightCycle?.render(staticShader)
        ground.render(staticShader)
        portalShader.use()
        portal2.bindPortalCamera(portalShader)
        //portal1.render(portalShader)
        portal2.render(portalShader)
        portal2.renderToFramebufferStop()

        //
        //Rendert auf den Bildschirm kann aus kommentiert werden
        //
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT); GLError.checkThrow()
        staticShader.use()
        cam.bind(staticShader)
        pointLight.bind(staticShader, "pointLight")
        spotLight.bind(staticShader, "spotLight", Matrix4f())
        lightCycle?.render(staticShader)
        ground.render(staticShader)
        portalShader.use()
        cam.bind(portalShader)
        glDisable(GL_CULL_FACE); GLError.checkThrow()
        portal1.render(portalShader)
        portal2.render(portalShader)
        glEnable(GL_CULL_FACE); GLError.checkThrow()

    }


    fun update(dt: Float, t: Float) {
        //var speed = 0f
        var vspeed = 0f
        var hspeed = 0f
        //var rotationDirection = 0f
        //val turningCycleRadius = 3f

        if(window.getKeyState(GLFW.GLFW_KEY_W)) {
            vspeed = -5f
        }
        else if(window.getKeyState(GLFW.GLFW_KEY_S)) {
            vspeed = 5f
        }
        if(window.getKeyState(GLFW.GLFW_KEY_A)) {
            //rotationDirection = -1f
            hspeed = -5f
        }
        else if(window.getKeyState(GLFW.GLFW_KEY_D)) {
            //rotationDirection = 1f
            hspeed = 5f
        }

        lightCycle?.translateLocal(Vector3f(hspeed * dt, 0f, vspeed * dt))

        /*if(rotationDirection == 0f){
            lightCycle?.translateLocal(Vector3f(0f, 0f, speed * dt))
        }
        else if(speed != 0f)
        {
            lightCycle?.rotateAroundPoint(0f,  (360 * speed)/(2f*Math.PI.toFloat() * turningCycleRadius) * rotationDirection * dt, 0f, lightCycle!!.getWorldPosition().add(lightCycle!!.getXAxis().mul(turningCycleRadius*rotationDirection)))
        }*/
        //lightCycle?.meshes?.get(2)?.material?.emitColor = Vector3f((Math.sin(t) + 1f)/2, (Math.sin(t*2) + 1f)/2, (Math.sin(t*3) + 1f)/2)

        if(window.getKeyState(GLFW.GLFW_KEY_1)) {
            currentImage = gBufferObject.gPosition
        }else if(window.getKeyState(GLFW.GLFW_KEY_2)) {
            currentImage = gBufferObject.gNormal
        }else if(window.getKeyState(GLFW.GLFW_KEY_3)) {
            currentImage = gBufferObject.gAlbedo
        }else if(window.getKeyState(GLFW.GLFW_KEY_4)) {
            currentImage = ssaoTextureFramebuffer.ssaoNoiseTexture
        }else if(window.getKeyState(GLFW.GLFW_KEY_5)) {
            currentImage = ssaoTextureFramebuffer.ssaoColorTexture
        }else if(window.getKeyState(GLFW.GLFW_KEY_6)) {
            currentImage = blurFramebuffer.blurFRamebufferTexture
        }


        //Check if player goes through portal
        if (portal1.checkCollision(lightCycle?.getWorldPosition()!!.x, lightCycle?.getWorldPosition()!!.y, lightCycle?.getWorldPosition()!!.z)) {
            //lightCycle?.setPosition(portal1.portalCam.getWorldPosition().x - 0.35f, portal1.portalCam.getWorldPosition().y, portal1.portalCam.getWorldPosition().z) //Teleports player to the other portal
            lightCycle?.setRotationA(portal1.portalCam.getRotationA())
            lightCycle?.setPosition(portal1.goingOutCoord.x, portal1.goingOutCoord.y, portal1.goingOutCoord.z)
        }
        else if (portal2.checkCollision(lightCycle?.getWorldPosition()!!.x, lightCycle?.getWorldPosition()!!.y, lightCycle?.getWorldPosition()!!.z)) {
            //lightCycle?.setPosition(portal2.portalCam.getWorldPosition().x + 0.35f, portal2.portalCam.getWorldPosition().y, portal2.portalCam.getWorldPosition().z) //Teleports player to the other portal
            lightCycle?.setRotationA(portal2.portalCam.getRotationA())
            lightCycle?.setPosition(portal2.goingOutCoord.x, portal2.goingOutCoord.y, portal2.goingOutCoord.z)
        }

    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    var oldMousePosX = 0.0
    var oldMousePosY = 0.0

    fun onMouseMove(xpos: Double, ypos: Double) {

        //cam.rotateAroundPoint((oldMousePosY-ypos).toFloat() * 0.002f, (oldMousePosX - xpos).toFloat() * 0.002f, 0f, Vector3f(0f))
        //cam.rotateAroundPoint(0f, (oldMousePosX - xpos).toFloat() * 0.02f, 0f, Vector3f(0f))
        lightCycle?.rotateLocal(0f, (oldMousePosX - xpos).toFloat() * 0.02f, 0f)

        oldMousePosX = xpos
        oldMousePosY = ypos
    }


    fun cleanup() {}
}
