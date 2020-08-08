package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL30
import java.nio.ByteBuffer
import kotlin.system.exitProcess

/**
 * This is the sup class of all Framebuffers
 * @property width Int width of the framebuffer
 * @property height Int height of the framebuffer
 * @property framebufferID Int id of the framebuffer
 * @property allAttachments ArrayList<Int> list of all attachments in this Framebuffer
 * @constructor
 */
abstract class Framebuffer(val width : Int, val height : Int) {

    /**
     * framebufferID
     */
    var framebufferID : Int = 0
        private set

    /**
     * List of all attachments
     */
    private val allAttachments = ArrayList<Int>()

    /**
     * In this Constructor we create the Framebuffer and call the Configure Framebuffer from our child
     * the framebuffer is unbinded after this
     */
    init {
        createFrameBuffer(); GLError.checkThrow()
        bind(); GLError.checkThrow()
        configureFramebuffer(); GLError.checkThrow()
        GL30.glDrawBuffers(allAttachments.toIntArray())

        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE){
            println("Error")
            exitProcess(1)
        }

        unbind()
    }

    /**
     * here the child class can add textures to the framebuffer
     *
     */
    protected abstract fun configureFramebuffer()

    /**
     * here we do the init like clear or lgenable for the Framebuffer
     *
     */
    protected abstract fun initRender(shader : ShaderProgram)

    /**
     * This method creates a new FRamebuffer and saves the id in framebufferID
     *
     */
    private fun createFrameBuffer()
    {
        framebufferID =  GL30.glGenFramebuffers(); GLError.checkThrow()
    }

    /**
     * this is the prerender function it must be called before we render an object on the framebuffer
     * @param shader ShaderProgram
     */
    fun startRender(shader : ShaderProgram)
    {
        bind()
        shader.use()
        initRender(shader)
    }

    /**
     * this stops rendering on the framebuffer (unbind it)
     *
     */
    fun stopRender() = unbind()

    /**
     * this binds the framebuffer
     *
     */
    private fun bind()
    {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID); GLError.checkThrow()
    }

    /**
     * This unbinds the framebuffer
     *
     */
    private fun unbind()
    {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0); GLError.checkThrow()
    }

    /**
     * this creates a new texture on attachment (attachment). You can set Mipmaps on or off. The Texture formats are default
     * @param attachment Int
     * @param genMipMap Boolean
     * @return Texture2D
     */
    protected fun createTextureAttachment(attachment : Int, genMipMap : Boolean) : Texture2D = createTextureAttachment(attachment, genMipMap,  GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE)


    /**
     * this creates a new texture on attachment (attachment). You can set Mipmaps on or off. The Texture format and type can be configured
     * @param attachment Int
     * @param genMipMap Boolean
     * @param internalformat Int
     * @param format Int
     * @param type Int
     * @return Texture2D
     */
    protected fun createTextureAttachment(attachment : Int, genMipMap : Boolean, internalformat : Int, format : Int, type : Int) : Texture2D
    {
        val tex = Texture2D(null as ByteBuffer?, width, height, genMipMap, internalformat, format, type); GLError.checkThrow()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.texID); GLError.checkThrow()
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + attachment, GL11.GL_TEXTURE_2D, tex.texID, 0); GLError.checkThrow()
        allAttachments.add(GL30.GL_COLOR_ATTACHMENT0 + attachment); GLError.checkThrow()
        return tex
    }

    /**
     * Here we create a depth texture
     * @return Texture2D
     */
    protected fun createDepthTexture() : Texture2D
    {
        val depthTex = Texture2D(null as ByteBuffer?, width, height, true, GL14.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT)
        depthTex.setTexParams(GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, GL11.GL_NEAREST, GL11.GL_NEAREST)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTex.texID)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTex.texID, 0)
        return depthTex
    }

    /**
     * Here we create a depth Renderbuffer
     * @return Int
     */
    protected fun createDepthBuffer() : Int
    {
        val rboID = GL30.glGenRenderbuffers()
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboID)
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height)
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rboID)
        return rboID
    }
}