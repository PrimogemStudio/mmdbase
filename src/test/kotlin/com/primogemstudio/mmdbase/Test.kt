package com.primogemstudio.mmdbase

import com.primogemstudio.mmdbase.io.ModelDataInputStream
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val inp = ModelDataInputStream(Files.newInputStream(Paths.get("E:/mmd/actions/idle.vmd")))
    val data = inp.readVMDFile()
    println(data)
}