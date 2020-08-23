package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL42


/**
 * This is a Framebuffer to get Textures of the geometry (position/normals/albedo)
 * @property gPosition Texture2D
 * @property gNormal Texture2D
 * @property gDiffTex Texture2D
 * @property depthRenderbuffer Int
 * @constructor
 */
class GeometryFramebuffer(_width : Int, _height : Int) : Framebuffer(_width, _height) {

    //texture to store the position data of the scene
    lateinit var gPosition : Texture2D
        private set
    //texture to store normals of the scene
    lateinit var gNormal : Texture2D
        private set
    //texture to store the diff texture color
    lateinit var gDiffTex : Texture2D
        private set
    //texture to store the emit texture color
    lateinit var gEmitTex : Texture2D
        private set
    //texture to store the spec texture color
    lateinit var gSpecTex : Texture2D
        private set
    //texture to store the shininess of every pixel
    lateinit var gShininess : Texture2D
        private set
    //texture to store the shininess of every pixel
    lateinit var gEmitColor : Texture2D
        private set
    //texture to store the information wether an pixel is an portal or not
    lateinit var gIsPortal : Texture2D
        private set
    //depth buffer
    var depthRenderbuffer : Int = 0
        private set


    /**
     * here we configure all textures/buffer of the Framebuffer
     *
     */
    override fun configureFramebuffer() {
        gPosition = createTextureAttachment(0, false, GL42.GL_RGBA16F, GL11.GL_RGBA, GL11.GL_FLOAT)
        gPosition.setTexParams(GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, GL12.GL_NEAREST, GL12.GL_NEAREST)
        gNormal = createTextureAttachment(1, false, GL42.GL_RGBA16F, GL11.GL_RGBA, GL11.GL_FLOAT)
        gNormal.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gDiffTex = createTextureAttachment(2, false, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE)
        gDiffTex.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gEmitTex = createTextureAttachment(3, false, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE)
        gEmitTex.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gSpecTex = createTextureAttachment(4, false, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE)
        gSpecTex.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gShininess = createTextureAttachment(5, false, GL11.GL_RED, GL11.GL_RED, GL11.GL_FLOAT)
        gShininess.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gEmitColor = createTextureAttachment(6, false, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_FLOAT)
        gEmitColor.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gIsPortal = createTextureAttachment(7, false, GL42.GL_RED, GL42.GL_RED, GL11.GL_FLOAT)
        gIsPortal.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)

        depthRenderbuffer = createDepthBuffer()
    }

    /**
     * this is called before all objects going to render
     *
     */
    override fun initRender(shader : ShaderProgram) {
        GL11.glEnable(GL11.GL_DEPTH_TEST); GLError.checkThrow()
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT); GLError.checkThrow()
    }

}