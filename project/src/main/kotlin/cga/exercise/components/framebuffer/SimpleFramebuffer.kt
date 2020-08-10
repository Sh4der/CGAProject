package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.lwjgl.opengl.GL11

class SimpleFramebuffer(_width : Int, _height : Int) : Framebuffer(_width, _height) {

    /**
     * output texture
     */
    lateinit var framebufferTexture : Texture2D
        private set

    /**
     * here we configure all textures/buffer of the Framebuffer
     *
     */
    override fun configureFramebuffer() {
        //create the outputtexture
        framebufferTexture = createTextureAttachment(0, false)
        framebufferTexture.setTexParams(GL11.GL_NEAREST, GL11.GL_NEAREST)

    }
    /**
     * this is called before all objects going to render
     *
     */
    override fun initRender(shader: ShaderProgram) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); GLError.checkThrow()
    }
}