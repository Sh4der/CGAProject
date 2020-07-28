package cga.exercise.components.framebuffer

import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12


/**
 * This is a Framebuffer to get Textures of the geometry (position/normals/albedo)
 * @property gPosition Texture2D
 * @property gNormal Texture2D
 * @property gAlbedo Texture2D
 * @property depthRenderbuffer Int
 * @constructor
 */
class GeometryFramebuffer(_width : Int, _height : Int) : Framebuffer(_width, _height) {

    lateinit var gPosition : Texture2D
        private set
    lateinit var gNormal : Texture2D
        private set
    lateinit var gAlbedo : Texture2D
        private set
    var depthRenderbuffer : Int = 0
        private set


    /**
     * here we configure all textures/buffer of the Framebuffer
     *
     */
    override fun configureFramebuffer() {
        gPosition = createTextureAttachment(0, false)
        gPosition.setTexParams(GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, GL12.GL_NEAREST, GL12.GL_NEAREST)
        gNormal = createTextureAttachment(1, false)
        gNormal.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)
        gAlbedo = createTextureAttachment(2, false)
        gAlbedo.setTexParams(GL12.GL_NEAREST, GL12.GL_NEAREST)

        depthRenderbuffer = createDepthBuffer()
    }

    /**
     * this is called before all objects going to render
     *
     */
    override fun initRender() {
        GL11.glEnable(GL11.GL_DEPTH_TEST); GLError.checkThrow()
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT); GLError.checkThrow()
    }

}