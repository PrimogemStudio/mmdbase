package com.primogemstudio.mmdbase.model

import glm_.glm
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.abs

class MMDCamera {
    var m_interest = Vec3(0f, 10f, 0f)
    var m_rotate = Vec3(0f, 0f, 0f)
    var m_distance = 50f
    var m_fov = glm.radians(30.0f)
}

class MMDLookAtCamera(cam: MMDCamera) {
    var m_center = Vec3()
    var m_eye = Vec3()
    var m_up = Vec3()

    init {
        var view = Mat4(1f)
        view = glm.translate(view, Vec3(0f, 0f, abs(cam.m_distance)))
        // view = glm.mat4_cast(cam.m_rotate) * view
        // val degree = glm.degrees(cam.m_rotate)
        var rot = Mat4(1f)
        rot = glm.rotate(rot, cam.m_rotate.y, Vec3(0, 1, 0))
        rot = glm.rotate(rot, cam.m_rotate.z, Vec3(0, 0, -1))
        rot = glm.rotate(rot, cam.m_rotate.x, Vec3(1, 0, 0))
        view = rot * view

        m_eye = Vec3(view[3]) + cam.m_interest
        m_center = Mat3(view) * Vec3(0, 0, -1) + m_eye
        m_up = Mat3(view) * Vec3(0, 1, 0)
    }
}