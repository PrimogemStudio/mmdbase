package com.primogemstudio.mmdbase

import com.primogemstudio.mmdbase.io.ModelDataInputStream
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
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
//第一个片源着色器代码
const val fragmentShaderSource2: String = "#version 330 core\n"+
        "out vec4 FragColor;\n"+
        "void main()\n"+
        "{\n"+
        "   FragColor = vec4(1.0f, 0.0f, 0.0f, 1.0f);\n"+
        "}\n"
var rt_mat = Mat4()
fun main() {
    val inp = ModelDataInputStream(Files.newInputStream(Paths.get("E:/mmd/lumine/lumine.pmx")))
    val data = inp.readPMXFile()
    println(data)

    System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll")

    glfwInit()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(800, 800, "Hello OpenGL!", NULL, NULL)
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
        rt_mat = Mat4().rotateY((x / warr[0].toFloat() * 4f).toFloat(), Mat4())
            .rotateX((y / harr[0].toFloat() * 4f).toFloat() - 2f, Mat4())
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

    val fragShader2 = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragShader2, fragmentShaderSource2)
    glCompileShader(fragShader2)
    glGetShaderiv(fragShader2, GL_COMPILE_STATUS, success)
    if (success[0] == 0) {
        println("fragment shader compile failed: ${glGetShaderInfoLog(fragShader2)}")
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

    val shaderProgram2 = glCreateProgram()
    glAttachShader(shaderProgram2, vertexShader)
    glAttachShader(shaderProgram2, fragShader2)
    glLinkProgram(shaderProgram2)
    glGetProgramiv(shaderProgram2, GL_LINK_STATUS, success)
    if (success[0] == 0) {
        println("shader linking failed: ${glGetProgramInfoLog(shaderProgram2)}")
    }
    glDeleteShader(fragShader2)
    glDeleteShader(vertexShader)

    var vertices01 = data.m_vertices.map { it.m_position }.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
    var vertices02 = data.m_bones.map { it.m_position }.flatMap { listOf(
        it.x, it.y, it.z,
        it.x, it.y - 0.01f, it.z,
        it.x - 0.01f, it.y - 0.01f, it.z,
        it.x - 0.01f, it.y, it.z)
    }.toFloatArray()
    val indices = data.m_faces.map { it.m_vertices }.flatMap { listOf(it[0], it[1], it[1], it[2], it[2], it[0]) }.toIntArray()
    val indices02 = vertices02.indices.toList().toIntArray()
    val VAOs = glGenVertexArrays()
    val VAOb = glGenVertexArrays()
    val VBOs = glGenBuffers()
    val VBOb = glGenBuffers()
    val EBO = glGenBuffers()
    val EBOb = glGenBuffers()

    glBindVertexArray(VAOs)
    glBindBuffer(GL_ARRAY_BUFFER, VBOs)
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)

    GL15.glBufferData(GL_ARRAY_BUFFER, vertices01, GL_STATIC_DRAW)
    GL15.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
    glEnableVertexAttribArray(0)

    glBindVertexArray(VAOb)
    glBindBuffer(GL_ARRAY_BUFFER, VBOb)
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOb)

    GL15.glBufferData(GL_ARRAY_BUFFER, vertices02, GL_STATIC_DRAW)
    GL15.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices02, GL_STATIC_DRAW)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
    glEnableVertexAttribArray(0)

    glfwSwapInterval(0)
    while (!glfwWindowShouldClose(window)) {
        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glBindVertexArray(VAOs)
        glBindBuffer(GL_ARRAY_BUFFER, VBOs)
        vertices01 = data.m_vertices
            .map { it.m_position * 0.08f }
            .map { it - Vec3(0f, 0.5f, 0f) }
            .map { glm.rotate(rt_mat.toQuat(), it) }
            .flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
        GL15.glBufferData(GL_ARRAY_BUFFER, vertices01, GL_STATIC_DRAW)
        vertices02 = data.m_bones
            .map { it.m_position * 0.08f }
            .map { it - Vec3(0f, 0.5f, 0f) }
            .map { glm.rotate(rt_mat.toQuat(), it) }
            .flatMap { listOf(it.x, it.y, it.z,
                it.x, it.y - 0.01f, it.z,
                it.x - 0.01f, it.y - 0.01f, it.z,
                it.x - 0.01f, it.y, it.z)  }.toFloatArray()
        glBindVertexArray(VAOb)
        glBindBuffer(GL_ARRAY_BUFFER, VBOb)
        GL15.glBufferData(GL_ARRAY_BUFFER, vertices02, GL_STATIC_DRAW)

        glUseProgram(shaderProgram)
        glBindVertexArray(VAOs)
        glBindBuffer(GL_ARRAY_BUFFER, VBOs)
        GL11.glDrawElements(GL_LINES, indices.size, GL_UNSIGNED_INT, 0)

        glUseProgram(shaderProgram2)
        glBindVertexArray(VAOb)
        glBindBuffer(GL_ARRAY_BUFFER, VBOb)
        GL11.glDrawElements(GL_LINES, indices02.size, GL_UNSIGNED_INT, 0)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }
}