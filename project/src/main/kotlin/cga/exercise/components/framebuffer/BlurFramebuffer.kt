package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.exercise.game.Scene
import cga.framework.GLError
import org.joml.Vector2f
import org.lwjgl.opengl.GL11

class BlurFramebuffer(_width : Int, _height : Int) : Framebuffer(_width, _height) {

    /**
     * output texture
     */
    lateinit var blurFramebufferTexture : Texture2D
        private set

    fun startRender(shader : ShaderProgram, texture: Texture2D)
    {
        //call the startRender from superclass
        startRender(shader)
        //bind input texture
        texture.bind(0)
        shader.setUniform("ssaoInput", 0)
    }

    /**
     * here we configure all textures/buffer of the Framebuffer
     *
     */
    override fun configureFramebuffer() {
        //create the outputtexture
        blurFramebufferTexture = createTextureAttachment(0, false)
        blurFramebufferTexture.setTexParams(GL11.GL_NEAREST, GL11.GL_NEAREST)

    }
    /**
     * this is called before all objects going to render
     *
     */
    override fun initRender(shader: ShaderProgram) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); GLError.checkThrow()
    }
}