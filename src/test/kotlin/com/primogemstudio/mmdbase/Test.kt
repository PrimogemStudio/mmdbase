package com.primogemstudio.mmdbase

import com.primogemstudio.mmdbase.io.ModelDataInputStream
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.file.Files
import java.nio.file.Paths

//定义顶点与片源着色器的函数字符串
const val vertexShaderSource: String = "#version 330 core\n"+
"layout (location = 0) in vec3 aPos;\n"+
"void main()\n"+
"{\n"+
"   gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);\n"+
"}"
//第一个片源着色器代码
const val fragmentShaderSource: String = "#version 330 core\n"+
"out vec4 FragColor;\n"+
"void main()\n"+
"{\n"+
"   FragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n"+
"}\n"
var rt_mat = Mat4()
fun main() {
    val inp = ModelDataInputStream(Files.newInputStream(Paths.get("E:/mmd/lumine/lumine.pmx")))
    val data = inp.readPMXFile()
    println(data)

    System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll")

    /*val ps = PrintStream(File("vtx.dmp"))
    data.m_vertices.forEach {
        ps.println("[${it.m_position.x}, ${it.m_position.y}, ${it.m_position.z}]")
    }

    val ps2 = PrintStream(File("faces.dmp"))
    data.m_faces.forEach {
        ps2.println("[${it.m_vertices[0]}, ${it.m_vertices[1]}, ${it.m_vertices[2]}]")
    }*/

    glfwInit()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(800, 600, "Hello OpenGL!", NULL, NULL)
    if (window == NULL) {
        println("failed to create window")
        glfwTerminate()
        return
    }

    glfwMakeContextCurrent(window)
    glfwSetFramebufferSizeCallback(window) { _, width, height -> glViewport(0, 0, width, height) }

    glfwSetCursorPosCallback(window) {_, x, y ->
        val warr = IntArray(1)
        val harr = IntArray(1)
        glfwGetWindowSize(window, warr, harr)
        rt_mat = Mat4().rotateY((x / warr[0].toFloat() * 2f).toFloat() - 1f, Mat4())
            .rotateX((y / harr[0].toFloat() * 2f).toFloat() - 1f, Mat4())
    }

    GL.createCapabilities()

    val vertexShader = glCreateShader(GL_VERTEX_SHADER)
    glShaderSource(vertexShader, vertexShaderSource)
    glCompileShader(vertexShader)
    val success = IntArray(1)
    glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success)
    if (success[0] == 0) {
        println("vertex shader compile failed: ${glGetShaderInfoLog(vertexShader)}")
    }

    val fragShader = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragShader, fragmentShaderSource)
    glCompileShader(fragShader)
    glGetShaderiv(fragShader, GL_COMPILE_STATUS, success)
    if (success[0] == 0) {
        println("fragment shader compile failed: ${glGetShaderInfoLog(fragShader)}")
    }

    val shaderProgram = glCreateProgram()
    glAttachShader(shaderProgram, vertexShader)
    glAttachShader(shaderProgram, fragShader)
    glLinkProgram(shaderProgram)
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, success)
    if (success[0] == 0) {
        println("shader linking failed: ${glGetProgramInfoLog(shaderProgram)}")
    }
    glDeleteShader(fragShader)
    glDeleteShader(vertexShader)

    /*val vertices01 = floatArrayOf(
        0.1f, 0.9f, 0.0f,
        0.9f, 0.9f, 0.0f,
        0.9f, 0.1f, 0.0f,
        0.1f, 0.1f, 0.0f
    )
    val indices = intArrayOf(
        0, 1, 3,
        1, 2, 3
    )*/
    var vertices01 = data.m_vertices.map { it.m_position }.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
    val indices = data.m_faces.map { it.m_vertices }.flatMap { listOf(it[0], it[1], it[1], it[2], it[2], it[0]) }.toIntArray()
    val VAOs = glGenVertexArrays()
    val VBOs = glGenBuffers()
    val EBO = glGenBuffers()

    glBindVertexArray(VAOs)
    glBindBuffer(GL_ARRAY_BUFFER, VBOs)
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)

    GL15.glBufferData(GL_ARRAY_BUFFER, vertices01, GL_STATIC_DRAW)
    GL15.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
    glEnableVertexAttribArray(0)

    glfwSwapInterval(0)
    while (!glfwWindowShouldClose(window)) {
        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)

        vertices01 = data.m_vertices
            .map { it.m_position * 0.08f }
            .map { glm.rotate(rt_mat.toQuat(), it) }
            .flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
        GL15.glBufferData(GL_ARRAY_BUFFER, vertices01, GL_STATIC_DRAW)

        glUseProgram(shaderProgram)
        glBindVertexArray(VAOs)
        GL11.glDrawElements(GL_LINES, indices.size, GL_UNSIGNED_INT, 0)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }
}