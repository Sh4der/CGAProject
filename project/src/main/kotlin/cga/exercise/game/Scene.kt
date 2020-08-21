package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.framebuffer.SimpleFramebuffer
import cga.exercise.components.framebuffer.GeometryFramebuffer
import cga.exercise.components.framebuffer.LightningFramebuffer
import cga.exercise.components.framebuffer.SSAOTextureFramebuffer
import cga.exercise.components.gameobjects.*
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.light.Lightpool
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
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.exitProcess
import java.math.BigDecimal
import java.math.RoundingMode


class Scene(private val window: GameWindow) {

    private val staticShader: ShaderProgram
    private val gBufferShader : ShaderProgram
    private val screenShader : ShaderProgram
    private val ssaoColorShader : ShaderProgram
    private val blurShader : ShaderProgram
    private val lightningShader : ShaderProgram
    private val portalShader : ShaderProgram
    private val crosshairShader : ShaderProgram

    private val cam : TronCamera
    private val camOverview : TronCamera

    private var ground : Renderable
    private var wall : Renderable
    private var wall2 : Renderable
    private var testLevel : Renderable?
    private var buttonStart : Renderable?
    private var buttonEnd : Renderable?

    private var player : Renderable?
    private var animatedPlayer : Animation
    private var portalGun : Renderable?
    private var portalGunPortal1 : Renderable?
    private var portalGunPortal2 : Renderable?

    private var pointLight : PointLight
    private var spotLight : SpotLight
    private var spotLightPortal1 : SpotLight
    private var spotLightPortal2 : SpotLight

    private val screenQuadMesh : Mesh

    val testTex :Texture2D

    private var currentImage : Texture2D

    private val gBufferObject : GeometryFramebuffer
    private val ssaoTextureFramebuffer :SSAOTextureFramebuffer
    private val blurFramebuffer : SimpleFramebuffer

    private val gBufferObjectPortal1 : GeometryFramebuffer
    private val ssaoTextureFramebufferPortal1 :SSAOTextureFramebuffer
    private val blurFramebufferPortal1 : SimpleFramebuffer
    private val lightningFramebuffer : LightningFramebuffer

    private val gBufferObjectPortal2 : GeometryFramebuffer
    private val ssaoTextureFramebufferPortal2 :SSAOTextureFramebuffer
    private val blurFramebufferPortal2 : SimpleFramebuffer


    //Portal vars
    private var portal1 : Portal
    private var portal2 : Portal


    //Other
    private val rob : Renderable?

    private val lightPool  = Lightpool()

    private var cellShading : Int = 0



    //Collisions
    private val collisionPool = CollisionPool()

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
        crosshairShader = ShaderProgram("assets/shaders/screen_vert.glsl", "assets/shaders/crosshairShader_frag.glsl")

        //Create the mesh
        val stride: Int = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0) //position
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4) //textureCoordinate
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4) //normalval
        val vertexAttributes = arrayOf<VertexAttribute>(attrPos, attrTC, attrNorm)


        //lightCycle = ModelLoader.loadModel("assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj", Math.toRadians(-90f), Math.toRadians(90f), 0f)
        player = ModelLoader.loadModel("assets/chell/0.obj", Math.toRadians(0f), Math.toRadians(180f), 0f)
        if(player == null)
        {
            exitProcess(1)
        }
        player?.meshes?.get(2)?.material?.emitColor = Vector3f(1f, 0f, 0f)
        player?.scaleLocal(Vector3f(1f))
        player?.setPosition(11.7f,1.2637f,57f)

        //Load in portal gun
        portalGun = ModelLoader.loadModel("assets/models/Portal Gun/Portal Gun.obj", 0f,0f,0f)
        portalGun?.scaleLocal(Vector3f(0.4f))
        portalGun?.translateLocal(Vector3f(1.2f,-1f,-1f))
        portalGun?.rotateLocal(0f,-35f,0f)

        //Load in portal gun for portal1
        portalGunPortal1 = ModelLoader.loadModel("assets/models/Portal Gun/Portal Gun.obj", 0f,0f,0f)
        portalGunPortal1?.scaleLocal(Vector3f(0.4f))
        portalGunPortal1?.translateLocal(Vector3f(1.2f,-1f,-1f))
        portalGunPortal1?.rotateLocal(0f,-35f,0f)

        //Load in portal gun for portal1
        portalGunPortal2 = ModelLoader.loadModel("assets/models/Portal Gun/Portal Gun.obj", 0f,0f,0f)
        portalGunPortal2?.scaleLocal(Vector3f(0.4f))
        portalGunPortal2?.translateLocal(Vector3f(1.2f,-1f,-1f))
        portalGunPortal2?.rotateLocal(0f,-35f,0f)



        val diffTex = Texture2D("assets/textures/con_wall_1.png", true)
        diffTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST)
        val emitTex = Texture2D("assets/textures/con_wall_1_emit.png", true)
        emitTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        val specTex = Texture2D("assets/textures/con_wall_1.png", true)
        specTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        val groundMaterial = Material(diffTex,
                emitTex,
                specTex,
                50f,
                Vector2f(64.0f, 64.0f)); GLError.checkThrow()

        val diffTexMetal1 = Texture2D("assets/textures/metal_floor.png", true)
        diffTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST)
        val emitTexMetal1 = Texture2D("assets/textures/con_wall_1_emit.png", true)
        emitTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        val specTexMetal1 = Texture2D("assets/textures/metal_floor.png", true)
        specTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)


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
        wall2.translateLocal(Vector3f(8f,0f,0f))
        wall2.rotateLocal(90f, 0f, 90f)


        cam = TronCamera()
        cam.rotateLocal(0f, 0f, 0f)
        cam.translateLocal(Vector3f(0f, 2f, 0f))
        cam.parent = player!!
        portalGun?.parent = cam

        camOverview = TronCamera()
        camOverview.rotateLocal(-45f, 0f, 0f)
        camOverview.translateLocal(Vector3f(0f, 10f, 30f))
        //camOverview.parent = lightCycle!!

        pointLight = PointLight(Vector3f(0f, 1f, 0f), Vector3i(255, 255, 255))
        //pointLight.parent = player
        //lightpool.add(pointLight)


        for (i in 0..40)
        {
            val light = PointLight(Vector3f((Random.nextFloat() * 2.0f - 1.0f) * 20f, 1f, (Random.nextFloat() * 2.0f - 1.0f) * 40f), Vector3i(100 + Random.nextInt(155), 100 + Random.nextInt(155), 100 + Random.nextInt(155)))
            lightPool.add(light)
        }
        for (i in 0..20)
        {
            val light = SpotLight(Vector3f((Random.nextFloat() * 2.0f - 1.0f) * 20f, (Random.nextFloat() * 2.0f - 1.0f) * 40f, (Random.nextFloat() * 2.0f - 1.0f) * 20f), Vector3i(100 + Random.nextInt(155), 100 + Random.nextInt(155), 100 + Random.nextInt(155)), 16.5f, 20.5f)
            light.rotateLocal((Random.nextFloat() * 2.0f - 1.0f) * 360f, (Random.nextFloat() * 2.0f - 1.0f) * 360f, (Random.nextFloat() * 2.0f - 1.0f) * 360f)
            lightPool.add(light)
        }


        spotLight = SpotLight(Vector3f(0f, 1f, 0f), Vector3i(0, 0, 0), 16.5f, 20.5f)
        spotLightPortal1 = SpotLight(Vector3f(0f, 1f, 0f), Vector3i(255, 255, 255), 16.5f, 20.5f)
        spotLightPortal2 = SpotLight(Vector3f(0f, 1f, 0f), Vector3i(255, 255, 255), 16.5f, 20.5f)

        spotLight.parent = cam
        lightPool.add(spotLight)


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
        blurFramebuffer = SimpleFramebuffer(window.framebufferWidth, window.framebufferHeight)
        lightningFramebuffer = LightningFramebuffer(window.framebufferWidth, window.framebufferHeight)
        currentImage = blurFramebuffer.framebufferTexture

        gBufferObjectPortal1 = GeometryFramebuffer(window.framebufferWidth, window.framebufferHeight)
        ssaoTextureFramebufferPortal1 = SSAOTextureFramebuffer(window.framebufferWidth, window.framebufferHeight)
        blurFramebufferPortal1 = SimpleFramebuffer(window.framebufferWidth, window.framebufferHeight)

        gBufferObjectPortal2 = GeometryFramebuffer(window.framebufferWidth, window.framebufferHeight)
        ssaoTextureFramebufferPortal2 = SSAOTextureFramebuffer(window.framebufferWidth, window.framebufferHeight)
        blurFramebufferPortal2 = SimpleFramebuffer(window.framebufferWidth, window.framebufferHeight)


        //Portal Setup
        portal1 = Portal(window, screenShader, Vector3f(11f / 255f, 106 / 255f, 230 / 255f), 999f, 999f, -0.235f, 0f, 270f, 0f)
        portal2 = Portal(window, screenShader, Vector3f(230f / 255f, 106 / 255f, 11 / 255f), 999f, 999f, 5f, 0f, 180f, 0f)


        rob = ModelLoader.loadModel("assets/models/kugel.obj", 0f, 0f, 0f)
        rob?.setPosition(-10f,0f,10f)
        rob?.scaleLocal(Vector3f(2f))
        rob?.meshes?.get(0)?.material?.emit = emitTex
        rob?.meshes?.get(0)?.material?.specular = specTex
        rob?.meshes?.get(0)?.material?.diff = diffTex

        testLevel = ModelLoader.loadModel("assets/models/mainLevel.obj", 0f, 0f, 0f)
        testLevel?.setEmitColor(Vector3f(1f))
        for (m in testLevel?.meshes!!) {
            m.material?.emit = emitTex
            m.material?.specular = specTex
            m.material?.diff = diffTex
            //m.material?.tcMultiplier = Vector2f(m.getHeight(), Math.max(m.getDepth(), m.getWidth()))
            //m.material = Material(Texture2D("assets/textures/con_wall_1.png", true), Texture2D("assets/textures/con_wall_1_emit.png", false), Texture2D("assets/textures/con_wall_1.png", true), 60f, Vector2f(m.getHeight() * 4, Math.max(m.getDepth(), m.getWidth()) * 4));
        }


        //Add collision from a 3d model
        collisionPool.addCollisionFromObject("assets/models/mainLevel.obj", Vector3f(0f))
        collisionPool.addCollisionFromObject("assets/models/button.obj", Vector3f(0f), Vector3f(-14f,0f,17f))
        collisionPool.addCollisionFromObject("assets/models/button.obj", Vector3f(0f), Vector3f(-9f,17f,11f))


        //Animation test
        animatedPlayer = Animation("assets/char/char_", 0, 19, 0f, 180f, 0f)
        animatedPlayer.setParent(player!!)
        animatedPlayer.scaleLocal(Vector3f(0.3f))


        //Add Button
        buttonStart = ModelLoader.loadModel("assets/models/button.obj", 0f, 0f, 0f)
        buttonEnd = ModelLoader.loadModel("assets/models/button.obj", 0f, 0f, 0f)

        buttonStart?.setPosition(-14f,0f,17f)
        buttonEnd?.setPosition(-9f,17f,11f)

        for (m in buttonStart?.meshes!!) {
            m.material?.emit = Texture2D("assets/textures/ground_spec.png", true)
        }
        buttonStart?.meshes?.get(0)?.material?.emitColor = Vector3f(0.025f)
        buttonStart?.meshes?.get(1)?.material?.emitColor = Vector3f(0.008f, 1f, 0f)

        for (m in buttonEnd?.meshes!!) {
            m.material?.emit = Texture2D("assets/textures/ground_spec.png", true)
        }
        buttonEnd?.meshes?.get(0)?.material?.emitColor = Vector3f(0.025f)
        buttonEnd?.meshes?.get(1)?.material?.emitColor = Vector3f(1f, 0f, 0f)

    }

    fun render(dt: Float, t: Float) {


        //------------------------Nico---------------------//
        //Set the cameras for the two portals

        portal1.setCameraParent(portal2, player, cam)
        portal2.setCameraParent(portal1, player, cam)
        portalGunPortal1?.parent = portal1.camera
        portalGunPortal2?.parent = portal2.camera
        spotLightPortal1.parent = portal1.camera
        spotLightPortal2.parent = portal2.camera

        portal1.setToInitPos()
        portal2.setToInitPos()


        //Portal 1

        gBufferObjectPortal1.startRender(gBufferShader)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        portal1.bindPortalCamera(gBufferShader); GLError.checkThrow()
        animatedPlayer.render(gBufferShader, dt)
        buttonStart?.render(gBufferShader)
        buttonEnd?.render(gBufferShader)
        testLevel?.renderWithPortalCheck(gBufferShader, portal2)
        portalGunPortal1?.render(gBufferShader)
        glDisable(GL_CULL_FACE)
        portal1.renderWithPortalCheck(gBufferShader, portal2, dt)
        glEnable(GL_CULL_FACE)
        gBufferObjectPortal1.stopRender()

        ssaoTextureFramebufferPortal1.startRender(ssaoColorShader, gBufferObjectPortal1)
        portal1.bindPortalCameraPRojectionMatrix(ssaoColorShader); GLError.checkThrow()
        screenQuadMesh.render()
        ssaoTextureFramebufferPortal1.stopRender()

        blurFramebufferPortal1.startRender(blurShader)
        ssaoTextureFramebufferPortal1.ssaoColorTexture.bind(0)
        blurShader.setUniform("ssaoInput", 0)
        screenQuadMesh.render()
        blurFramebufferPortal1.stopRender()


        portal1.renderToFramebufferStart(lightningShader)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        portal1.bindPortalCameraViewMatrix(lightningShader)
        lightPool.bind(lightningShader)
        lightningShader.setUniform("cellShading", cellShading)
        gBufferObjectPortal1.gPosition.bind(0)
        lightningShader.setUniform("gPosition", 0)
        gBufferObjectPortal1.gNormal.bind(1)
        lightningShader.setUniform("gNormal", 1)
        gBufferObjectPortal1.gDiffTex.bind(2)
        lightningShader.setUniform("gDiff", 2)
        gBufferObjectPortal1.gEmitTex.bind(3)
        lightningShader.setUniform("gEmit", 3)
        gBufferObjectPortal1.gEmitTex.bind(4)
        lightningShader.setUniform("gSpec", 4)
        gBufferObjectPortal1.gShininess.bind(5)
        lightningShader.setUniform("gShininess", 5)
        gBufferObjectPortal1.gEmitColor.bind(6)
        lightningShader.setUniform("gEmitColor", 6)
        gBufferObjectPortal1.gIsPortal.bind(8)
        lightningShader.setUniform("gIsPortal", 8)

        blurFramebufferPortal1.framebufferTexture.bind(7)
        lightningShader.setUniform("ssao", 7)
        screenQuadMesh.render(); GLError.checkThrow()
        portal1.renderToFramebufferStop()


        //Portal 2

        gBufferObjectPortal2.startRender(gBufferShader)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        portal2.bindPortalCamera(gBufferShader); GLError.checkThrow()
        animatedPlayer.render(gBufferShader, dt)
        buttonStart?.render(gBufferShader)
        buttonEnd?.render(gBufferShader)
        testLevel?.renderWithPortalCheck(gBufferShader, portal1)
        portalGunPortal2?.render(gBufferShader)
        glDisable(GL_CULL_FACE)
        portal2.renderWithPortalCheck(gBufferShader, portal1, dt)
        glEnable(GL_CULL_FACE)
        gBufferObjectPortal2.stopRender()

        ssaoTextureFramebufferPortal2.startRender(ssaoColorShader, gBufferObjectPortal2)
        portal2.bindPortalCameraPRojectionMatrix(ssaoColorShader); GLError.checkThrow()
        screenQuadMesh.render()
        ssaoTextureFramebufferPortal2.stopRender()

        blurFramebufferPortal2.startRender(blurShader)
        ssaoTextureFramebufferPortal2.ssaoColorTexture.bind(0)
        blurShader.setUniform("ssaoInput", 0)
        screenQuadMesh.render()
        blurFramebufferPortal2.stopRender()

        portal2.renderToFramebufferStart(lightningShader)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        portal2.bindPortalCameraViewMatrix(lightningShader)
        lightPool.bind(lightningShader)
        lightningShader.setUniform("cellShading", cellShading)
        gBufferObjectPortal2.gPosition.bind(0)
        lightningShader.setUniform("gPosition", 0)
        gBufferObjectPortal2.gNormal.bind(1)
        lightningShader.setUniform("gNormal", 1)
        gBufferObjectPortal2.gDiffTex.bind(2)
        lightningShader.setUniform("gDiff", 2)
        gBufferObjectPortal2.gEmitTex.bind(3)
        lightningShader.setUniform("gEmit", 3)
        gBufferObjectPortal2.gEmitTex.bind(4)
        lightningShader.setUniform("gSpec", 4)
        gBufferObjectPortal2.gShininess.bind(5)
        lightningShader.setUniform("gShininess", 5)
        gBufferObjectPortal2.gEmitColor.bind(6)
        lightningShader.setUniform("gEmitColor", 6)
        gBufferObjectPortal2.gIsPortal.bind(8)
        lightningShader.setUniform("gIsPortal", 8)

        blurFramebufferPortal2.framebufferTexture.bind(7)
        lightningShader.setUniform("ssao", 7)

        screenQuadMesh.render(); GLError.checkThrow()
        portal2.renderToFramebufferStop()






        //Screen rendering
        portal1.setPositionRotationClamp(player!!)
        portal2.setPositionRotationClamp(player!!)

        gBufferObject.startRender(gBufferShader)
        cam.bind(gBufferShader); GLError.checkThrow()
        portalGun?.render(gBufferShader); GLError.checkThrow()
        testLevel?.render(gBufferShader)
        buttonStart?.render(gBufferShader)
        buttonEnd?.render(gBufferShader)
        glDisable(GL_CULL_FACE)
        portal1.render(gBufferShader, dt)
        portal2.render(gBufferShader, dt)
        glEnable(GL_CULL_FACE)
        gBufferObject.stopRender()

        ssaoTextureFramebuffer.startRender(ssaoColorShader, gBufferObject)
        cam.bindProjectionMatrix(ssaoColorShader)
        screenQuadMesh.render()
        ssaoTextureFramebuffer.stopRender()

        blurFramebuffer.startRender(blurShader)
        ssaoTextureFramebuffer.ssaoColorTexture.bind(0)
        blurShader.setUniform("ssaoInput", 0)
        screenQuadMesh.render()
        blurFramebuffer.stopRender()



        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glClear(GL_COLOR_BUFFER_BIT); GLError.checkThrow()
        glDisable(GL_DEPTH_TEST)

        lightningFramebuffer.startRender(lightningShader, gBufferObject, blurFramebuffer)
        lightningShader.use(); GLError.checkThrow()
        cam.bindViewMatrix(lightningShader)
        lightPool.bind(lightningShader)
        lightningShader.setUniform("cellShading", cellShading)

        gBufferObject.gPosition.bind(0)
        lightningShader.setUniform("gPosition", 0)
        gBufferObject.gNormal.bind(1)
        lightningShader.setUniform("gNormal", 1)
        gBufferObject.gDiffTex.bind(2)
        lightningShader.setUniform("gDiff", 2)
        gBufferObject.gEmitTex.bind(3)
        lightningShader.setUniform("gEmit", 3)
        gBufferObject.gEmitTex.bind(4)
        lightningShader.setUniform("gSpec", 4)
        gBufferObject.gShininess.bind(6)
        lightningShader.setUniform("gShininess", 6)
        gBufferObject.gEmitColor.bind(6)
        lightningShader.setUniform("gEmitColor", 6)
        gBufferObject.gIsPortal.bind(8)
        lightningShader.setUniform("gIsPortal", 8)

        blurFramebuffer.framebufferTexture.bind(7)
        lightningShader.setUniform("ssao", 7)

        screenQuadMesh.render(); GLError.checkThrow()
        lightningFramebuffer.stopRender()

        blurFramebuffer.startRender(crosshairShader)
        lightningFramebuffer.framebufferTexture.bind(0)
        crosshairShader.setUniform("tex", 0)
        crosshairShader.setUniform("screenSize", Vector2f(window.framebufferWidth.toFloat(), window.framebufferHeight.toFloat()))
        screenQuadMesh.render()
        blurFramebuffer.stopRender()


        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glClear(GL_COLOR_BUFFER_BIT); GLError.checkThrow()
        glDisable(GL_DEPTH_TEST)
        screenShader.use(); GLError.checkThrow()
        blurFramebuffer.framebufferTexture.bind(0)
        screenShader.setUniform("tex", 0)
        screenQuadMesh.render(); GLError.checkThrow()

    }






    private var startTime = 0f
    private var endTime = 0f



    private var deltaF = 0f;
    private var deltaC = 0f;

    //Movement vars
    var k_u = false; var k_d = false; var k_l = false; var k_r = false; var k_a = false //Key directions
    var hspeed = 0f //Horizontal speed
    var vspeed = 0f //Vertical speed
    val accel_const = 1.4f //Acceleration
    val movespeedmax_const = 12f //Maximum Movementspeed
    var direction = 0f// Player direction
    var touchx = false//Check if the player is colliding with anything - x axis
    var touchz = false//Check if the player is colliding with anything - z axis
    var playerNewX = 0f
    var playerNewZ = 0f

    //Jumping
    var yspeed = 0f
    var ygrav = 0.005f
    var canjump = false
    val yheight = 0.4
    val jumpspeed = 0.2f
    val terminalvelocity = 0.3f

    var ytop = player!!.y() + yheight //Unused, because it is in the getPlayerCollision() function
    var ybottom = player!!.y() //Unused, because it is in the getPlayerCollision() function

    var eKeyState = false
    var leftMBState = false
    var rightMBState = false

    fun update(dt: Float, t: Float) {

        // COMPLEX PLAYER MOVEMENT FOR SMOOTH COLLISION DETECTION AND REACTION
        val movespeedmax = movespeedmax_const * dt
        //val accel = accel_const * dt

        val accel = accel_const * 4 * dt
        hspeed = 0f
        vspeed = 0f

        //Key states
        k_u = window.getKeyState(GLFW.GLFW_KEY_W) //Up
        k_d = window.getKeyState(GLFW.GLFW_KEY_S) //Down
        k_l = window.getKeyState(GLFW.GLFW_KEY_A) //Left
        k_r = window.getKeyState(GLFW.GLFW_KEY_D) //Right
        k_a = window.getKeyState(GLFW.GLFW_KEY_SPACE) //Space

        hspeed *= 0.8f //Decrease speed over time <--- DOESNT WORK WITH THE CURRECT PORTALS!
        vspeed *= 0.8f
        direction = player?.getYDirDeg()!!.toFloat()

        if (k_u) {
            hspeed = hspeed + lengthdir_x(accel,Math.toRadians(direction))//median(arrayListOf(-movespeedmax,movespeedmax,hspeed + lengthdir_x(accel,Math.toRadians(direction))))
            vspeed = vspeed + lengthdir_z(accel,Math.toRadians(direction))//median(arrayListOf(-movespeedmax,movespeedmax,vspeed + lengthdir_z(accel,Math.toRadians(direction))))
        }
        if (k_d) {
            hspeed = hspeed - lengthdir_x(accel,Math.toRadians(direction))//median(arrayListOf(-movespeedmax,movespeedmax,hspeed - lengthdir_x(accel,Math.toRadians(direction))))
            vspeed = vspeed - lengthdir_z(accel,Math.toRadians(direction))//median(arrayListOf(-movespeedmax,movespeedmax,vspeed - lengthdir_z(accel,Math.toRadians(direction))))
        }
        if (k_l) {
            hspeed = hspeed + lengthdir_x(accel,Math.toRadians(direction + 90))//median(arrayListOf(-movespeedmax,movespeedmax,hspeed + lengthdir_x(accel,Math.toRadians(direction + 90))))
            vspeed = vspeed + lengthdir_z(accel,Math.toRadians(direction + 90))//median(arrayListOf(-movespeedmax,movespeedmax,vspeed + lengthdir_z(accel,Math.toRadians(direction + 90))))
        }
        if (k_r) {
            hspeed = hspeed + lengthdir_x(accel,Math.toRadians(direction - 90))//median(arrayListOf(-movespeedmax,movespeedmax,hspeed + lengthdir_x(accel,Math.toRadians(direction - 90))))
            vspeed = vspeed + lengthdir_z(accel,Math.toRadians(direction - 90))//median(arrayListOf(-movespeedmax,movespeedmax,vspeed + lengthdir_z(accel,Math.toRadians(direction - 90))))
        }

        //Limits speed, so player can't go faster when pressing diagonal keys (A + W for example) -> Doesnt work right now
        //hspeed = Math.max(Math.min(hspeed, movespeedmax), -movespeedmax)
        //vspeed = Math.max(Math.min(vspeed, movespeedmax), -movespeedmax)

        //println("HSPEED: ${hspeed} | VSPEED: $vspeed}")
        //println(direction)

        //Check for portal almost collision
        if ((portal1.checkAlmostCollision(player?.getWorldPosition()!!.x, player?.getWorldPosition()!!.y, player?.getWorldPosition()!!.z)) || (portal2.checkAlmostCollision(player?.getWorldPosition()!!.x, player?.getWorldPosition()!!.y, player?.getWorldPosition()!!.z))) {
            touchx = false
            touchz = false
        }
        else {
            touchx = collisionPool.checkRectangleCollision(getPlayerCollision(player!!.x() + hspeed, player!!.y(), player!!.z()))
            touchz = collisionPool.checkRectangleCollision(getPlayerCollision(player!!.x(), player!!.y(), player!!.z() + vspeed))
        }

        //Check for X Axis
        if (!touchx) {
            //player?.setPosition(player?.getWorldPosition()!!.x + hspeed,0f,0f)
            playerNewX = player?.getWorldPosition()!!.x + hspeed
        }
        else {
            hspeed = 0f
        }

        //Check for Z Axis
        if (!touchz) {
            playerNewZ = player?.getWorldPosition()!!.z + vspeed
        }
        else {
            vspeed = 0f
        }

        //Set Player Position
        player?.setPosition(playerNewX,player!!.y(), playerNewZ)


        //Gravity & Jumping
        if (collisionPool.checkRectangleCollision(getPlayerCollision(player!!.x(), player!!.y() + yspeed, player!!.z()))) {
            yspeed = 0f
            //Jumping when on the floor
            if (k_a && canjump) {
                yspeed = jumpspeed
                canjump = false
            }
        }
        else{
            player?.setPosition(player!!.x(), player!!.y() + yspeed, player!!.z())
            yspeed = Math.max(-terminalvelocity,yspeed - ygrav)
        }

        //Not pressing the jump key
        if(!k_a) {
            canjump = true
            if (yspeed > 8) {
                yspeed = 8f
            }
        }


        //Animation
        animatedPlayer.movement = (hspeed != 0f && vspeed != 0f)
        animatedPlayer.update()


        //Shoot portals to walls
        if (window.getMouseKeyState(GLFW.GLFW_MOUSE_BUTTON_1)) {
            if (!leftMBState) {
                val raycast = Raycast(player!!.x(), player!!.y() + 2f, player!!.z(), cam.getWorldModelMatrix())
                val collisionPos = raycast.moveUntilCollision(collisionPool)
                if (collisionPos != Vector4f(-9999f)) {
                    portal1.setPositionRotation(collisionPos, collisionPool, testLevel!!)
                }
                leftMBState = true
            }
        }
        else {leftMBState = false}

        if (window.getMouseKeyState(GLFW.GLFW_MOUSE_BUTTON_2)) {
            if (!rightMBState) {
                val raycast = Raycast(player!!.x(), player!!.y() + 2f, player!!.z(), cam.getWorldModelMatrix())
                val collisionPos = raycast.moveUntilCollision(collisionPool)
                if (collisionPos != Vector4f(-9999f)) {
                    portal2.setPositionRotation(collisionPos, collisionPool, testLevel!!)
                }
                rightMBState = true
            }
        }
        else {rightMBState = false}



        // Calmp portal position to avoid z-fighting
        portal1.setPositionRotationClamp(player!!)
        portal2.setPositionRotationClamp(player!!)



        //Check if player goes through portal
        if (portal1.checkCollision(player?.getWorldPosition()!!.x, player?.getWorldPosition()!!.y, player?.getWorldPosition()!!.z)) {
            //lightCycle?.setPosition(portal1.portalCam.getWorldPosition().x - 0.35f, portal1.portalCam.getWorldPosition().y, portal1.portalCam.getWorldPosition().z) //Teleports player to the other portal
            player?.setRotationA(portal1.portalCam.getRotationA())
            player?.setPosition(portal1.goingOutCoord.x, portal1.goingOutCoord.y-2f, portal1.goingOutCoord.z)
        }
        else if (portal2.checkCollision(player?.getWorldPosition()!!.x, player?.getWorldPosition()!!.y, player?.getWorldPosition()!!.z)) {
            //lightCycle?.setPosition(portal2.portalCam.getWorldPosition().x + 0.35f, portal2.portalCam.getWorldPosition().y, portal2.portalCam.getWorldPosition().z) //Teleports player to the other portal
            player?.setRotationA(portal2.portalCam.getRotationA())
            player?.setPosition(portal2.goingOutCoord.x, portal2.goingOutCoord.y-2f, portal2.goingOutCoord.z)
        }

        portal1.setCameraParent(portal2, player, cam)
        portal2.setCameraParent(portal1, player, cam)




        //Debugging Controls
        if(window.getKeyState(GLFW.GLFW_KEY_1)) {
            currentImage = gBufferObject.gPosition
        }else if(window.getKeyState(GLFW.GLFW_KEY_2)) {
            currentImage = gBufferObject.gNormal
        }else if(window.getKeyState(GLFW.GLFW_KEY_3)) {
            currentImage = gBufferObject.gDiffTex
        }else if(window.getKeyState(GLFW.GLFW_KEY_4)) {
            currentImage = gBufferObject.gEmitTex
        }else if(window.getKeyState(GLFW.GLFW_KEY_5)) {
            currentImage = gBufferObject.gSpecTex
        }else if(window.getKeyState(GLFW.GLFW_KEY_6)) {
            currentImage = gBufferObject.gShininess
        }else if(window.getKeyState(GLFW.GLFW_KEY_7)) {
            currentImage = ssaoTextureFramebuffer.ssaoNoiseTexture
        }else if(window.getKeyState(GLFW.GLFW_KEY_8)) {
            currentImage = ssaoTextureFramebuffer.ssaoColorTexture
        }else if(window.getKeyState(GLFW.GLFW_KEY_9)) {
            currentImage = blurFramebuffer.framebufferTexture
        }
        if(window.getKeyState(GLFW.GLFW_KEY_F) && t - deltaF >= 0.5f) {
            deltaF = t
           if(spotLight.color == Vector3i(255, 255, 255))
               spotLight.color = Vector3i(0, 0, 0)
            else
               spotLight.color = Vector3i(255, 255, 255)

        }
        if(window.getKeyState(GLFW.GLFW_KEY_C) && t - deltaC >= 0.5f) {
            deltaC = t
            cellShading = if (cellShading == 1) 0 else 1

        }

        //Press the use-key
        if (window.getKeyState(GLFW.GLFW_KEY_E)) {
            if (!eKeyState) {
                eKeyState = true
                startButtonTimer(t)
                endButtonTimer(t)
            }
        }
        else {
            eKeyState = false
        }

        //Reset player position
        if (window.getKeyState(GLFW.GLFW_KEY_O)) {
            player?.setPosition(11.7f,1.2637f,57f)
        }

        //When player falls into the void
        if (player?.y()!! < -10) {
            player?.setPosition(11.7f,1f,57f)
        }

    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    var lastX = 0.0
    var lastY = 0.0
    var sensitivity = 0.03f

    fun onMouseMove(xpos: Double, ypos: Double) {

        player?.rotateLocal(0f, (lastX - xpos).toFloat() * sensitivity, 0f)
        cam.rotateLocal((lastY - ypos).toFloat() * sensitivity, 0f, 0f)
        portal1.camera.rotateLocal((lastY - ypos).toFloat() * sensitivity, 0f, 0f)
        portal2.camera.rotateLocal((lastY - ypos).toFloat() * sensitivity, 0f, 0f)
        if (cam.getXDirDeg() > 180f) {
            cam.rotateLocal(-(lastY - ypos).toFloat() * sensitivity, 0f, 0f)
            portal1.camera.rotateLocal(-(lastY - ypos).toFloat() * sensitivity, 0f, 0f)
            portal2.camera.rotateLocal(-(lastY - ypos).toFloat() * sensitivity, 0f, 0f)
        }
        else if (cam.getXDir() < 0f) {
            cam.rotateLocal(-(lastY - ypos).toFloat() * sensitivity, 0f, 0f)
            portal1.camera.rotateLocal(-(lastY - ypos).toFloat() * sensitivity, 0f, 0f)
            portal2.camera.rotateLocal(-(lastY - ypos).toFloat() * sensitivity, 0f, 0f)
        }

        lastX = xpos
        lastY = ypos
    }


    fun cleanup() {}


    fun lengthdir_x(length: Float, dir: Float) : Float = (length * (Math.cos(dir.toDouble()))).toFloat()

    fun lengthdir_z(length: Float, dir: Float) : Float = (length * (-Math.sin(dir.toDouble()))).toFloat()

    fun getPlayerCollision(x: Float, y: Float, z: Float) = Collision(x-0.2f, y, z-0.2f, x+0.2f, y+2f, z+0.2f)

    fun pointDistance3d(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float):Float = Math.sqrt((x2-x1).pow(2) + (y2-y1).pow(2) + (z2-z1).pow(2))

    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (round((this * multiplier).toInt()) / multiplier).toFloat()
    }

    fun startButtonTimer(t: Float) {
        if (startTime == 0f && pointDistance3d(player!!.x(), player!!.y(), player!!.z(), buttonStart!!.x(), buttonStart!!.y(), buttonStart!!.z()) < 0.8f) {
            startTime = t
            println("TIMER START!")
        }
        else if (startTime > 0 && pointDistance3d(player!!.x(), player!!.y(), player!!.z(), buttonStart!!.x(), buttonStart!!.y(), buttonStart!!.z()) < 0.8f) {
            startTime = 0f
            println("TIMER STOPPED!")
        }
    }

    fun endButtonTimer(t: Float) {
        if (startTime > 0 && pointDistance3d(player!!.x(), player!!.y(), player!!.z(), buttonEnd!!.x(), buttonEnd!!.y(), buttonEnd!!.z()) < 0.8f) {
            endTime = t
            println("TIMER END: ${BigDecimal((endTime - startTime).toDouble()).setScale(2, RoundingMode.HALF_EVEN)}")
            startTime = 0f
        }
    }
}
