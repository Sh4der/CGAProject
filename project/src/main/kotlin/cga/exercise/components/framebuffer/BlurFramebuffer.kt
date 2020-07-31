package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.exercise.game.Scene
import cga.framework.GLError
import org.lwjgl.opengl.GL11

class BlurFramebuffer(_width : Int, _height : Int) : Framebuffer(_width, _height) {

    lateinit var blurFRamebufferTexture : Texture2D
        private set

    fun startRender(shader : ShaderProgram, ssaoTextureFramebuffer: SSAOTextureFramebuffer)
    {
        startRender(shader)
        ssaoTextureFramebuffer.ssaoColorTexture.bind(0)
        shader.setUniform("ssaoInput", 0)
    }

    override fun configureFramebuffer() {
        blurFRamebufferTexture = createTextureAttachment(0, false, GL11.GL_RED, GL11.GL_RED, GL11.GL_FLOAT)
        blurFRamebufferTexture.setTexParams(GL11.GL_NEAREST, GL11.GL_NEAREST)

    }

    override fun initRender(shader: ShaderProgram) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); GLError.checkThrow()
    }
}