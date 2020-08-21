package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.joml.Vector2f
import org.lwjgl.opengl.GL11

class LightningFramebuffer(_weight : Int, _height : Int) : Framebuffer(_weight, _height){
    /**
     * output texture
     */
    lateinit var framebufferTexture : Texture2D
        private set

    /**
     * load all necessary data into the shader
     * @param shader ShaderProgram
     * @param gFramebuffer GeometryFramebuffer
     */
    fun startRender(shader : ShaderProgram, gBufferObject : GeometryFramebuffer, blurFramebuffer : SimpleFramebuffer)
    {
        startRender(shader)
        gBufferObject.gPosition.bind(0)
        shader.setUniform("gPosition", 0)
        gBufferObject.gNormal.bind(1)
        shader.setUniform("gNormal", 1)
        gBufferObject.gDiffTex.bind(2)
        shader.setUniform("gDiff", 2)
        gBufferObject.gEmitTex.bind(3)
        shader.setUniform("gEmit", 3)
        gBufferObject.gEmitTex.bind(4)
        shader.setUniform("gSpec", 4)
        gBufferObject.gShininess.bind(6)
        shader.setUniform("gShininess", 6)
        gBufferObject.gEmitColor.bind(6)
        shader.setUniform("gEmitColor", 6)
        gBufferObject.gIsPortal.bind(8)
        shader.setUniform("gIsPortal", 8)

        blurFramebuffer.framebufferTexture.bind(7)
        shader.setUniform("ssao", 7)

    }

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
        GL11.glDisable(GL11.GL_DEPTH_TEST)

    }
}