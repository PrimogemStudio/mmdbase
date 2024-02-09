package com.primogemstudio.mmdbase

import com.primogemstudio.mmdbase.io.ModelDataInputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val inp = ModelDataInputStream(Files.newInputStream(Paths.get("E:/mmd/lumine/lumine.pmx")))
    val data = inp.readPMXFile()
    println(data)

    val ps = PrintStream(File("vtx.dmp"))
    data.m_vertices.forEach {
        ps.println("[${it.m_position.x}, ${it.m_position.y}, ${it.m_position.z}]")
    }

    val ps2 = PrintStream(File("faces.dmp"))
    data.m_faces.forEach {
        ps2.println("[${it.m_vertices[0]}, ${it.m_vertices[1]}, ${it.m_vertices[2]}]")
    }
}