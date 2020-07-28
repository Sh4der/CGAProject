package cga.exercise.components.texture

import cga.framework.GLError
import cga.framework.GLError.checkEx
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.EXTTextureFilterAnisotropic
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer


/**
 * Created by Fabian on 16.09.2017.
 */
class Texture2D: ITexture{
    var texID: Int = -1
        private set

    constructor(imageData: ByteBuffer?, width: Int, height: Int, genMipMaps: Boolean)
    {
        try {
            processTexture(imageData, width, height, genMipMaps, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE); GLError.checkThrow()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    constructor(imageData: ByteBuffer?, width: Int, height: Int, genMipMaps: Boolean, internalformat : Int, format : Int, type : Int)
    {
        try {
            processTexture(imageData, width, height, genMipMaps, internalformat, format, type); GLError.checkThrow()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }



    companion object {
        //create texture from file
        //don't support compressed textures for now
        //instead stick to pngs
        operator fun invoke(path: String, genMipMaps: Boolean): Texture2D {
            val x = BufferUtils.createIntBuffer(1)
            val y = BufferUtils.createIntBuffer(1)
            val readChannels = BufferUtils.createIntBuffer(1)
            //flip y coordinate to make OpenGL happy
            STBImage.stbi_set_flip_vertically_on_load(true)
            val imageData = STBImage.stbi_load(path, x, y, readChannels, 4)
                    ?: throw Exception("Image file \"" + path + "\" couldn't be read:\n" + STBImage.stbi_failure_reason())

            try {
                return Texture2D(imageData, x.get(), y.get(), genMipMaps)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                throw ex
            } finally {
                STBImage.stbi_image_free(imageData)
            }
        }
    }

    override fun processTexture(imageData: ByteBuffer?, width: Int, height: Int, genMipMaps: Boolean, internalformat : Int, format: Int, type : Int) {
        texID = GL11.glGenTextures(); GLError.checkThrow()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID); GLError.checkThrow()
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalformat, width, height, 0, format, type, imageData); GLError.checkThrow()

        if(genMipMaps)
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); GLError.checkThrow()
    }

    override fun setTexParams(wrapS: Int, wrapT: Int, minFilter: Int, magFilter: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID); GLError.checkThrow()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapS)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter)


        GL11.glTexParameterf(GL11.GL_TEXTURE_2D,
                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                16.0f)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); GLError.checkThrow()
    }

    override fun setTexParams(minFilter: Int, magFilter: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID); GLError.checkThrow()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter)

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D,
                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                16.0f)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); GLError.checkThrow()
    }

    override fun bind(textureUnit: Int) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID)
    }

    override fun unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }

    override fun cleanup() {
        unbind()
        if (texID != 0) {
            GL11.glDeleteTextures(texID)
            texID = 0
        }
    }
}