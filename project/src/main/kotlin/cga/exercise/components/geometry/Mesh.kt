package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*

/**
 * Creates a Mesh object from vertexdata, intexdata and a given set of vertex attributes
 *
 * @param vertexdata plain float array of vertex data
 * @param indexdata  index data
 * @param attributes vertex attributes contained in vertex data
 * @throws Exception If the creation of the required OpenGL objects fails, an exception is thrown
 *
 * Created by Fabian on 16.09.2017.
 */
class Mesh(vertexdata: FloatArray, indexdata: IntArray, attributes: Array<VertexAttribute>, val material: Material?) {
    //private data
    private var vao = 0
    private var vbo = 0
    private var ibo = 0
    private var indexcount = 0

    init {
        // todo: place your code here
        indexcount = indexdata.size

        // todo: generate IDs
        vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        ibo = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)

        val indexBuffer = BufferUtils.createIntBuffer(indexcount)
        indexBuffer.put(indexdata)
        indexBuffer.flip()
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW)

        vbo = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        val vertexBuffer = BufferUtils.createFloatBuffer(vertexdata.size)
        vertexBuffer.put(vertexdata)
        vertexBuffer.flip()
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)
        for (i : Int  in 0..attributes.size-1)
        {
            GL20.glVertexAttribPointer(i, attributes[i].n, attributes[i].type, false, attributes[i].stride, attributes[i].offset.toLong())
            GL20.glEnableVertexAttribArray(i)
        }

        // todo: bind your objects

        // todo: upload your mesh data
        GL30.glBindVertexArray(0)
    }

    /**
     * renders the mesh
     */
    fun render() {
        // todo: place your code here
        // call the rendering method every frame

        GL30.glBindVertexArray(vao)
                GL11.glDrawElements(GL11.GL_TRIANGLES, indexcount, GL11.GL_UNSIGNED_INT, 0)
        GL30.glBindVertexArray(0)

    }

    /**
     * renders the mesh
     */
    fun render(shaderProgram : ShaderProgram) {
        material?.bind(shaderProgram)
        render()
        material?.unbind()
    }

    /**
     * Deletes the previously allocated OpenGL objects for this mesh
     */
    fun cleanup() {
        if (ibo != 0) GL15.glDeleteBuffers(ibo)
        if (vbo != 0) GL15.glDeleteBuffers(vbo)
        if (vao != 0) GL30.glDeleteVertexArrays(vao)
    }
}