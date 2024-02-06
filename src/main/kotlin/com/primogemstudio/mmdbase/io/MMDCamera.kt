package com.primogemstudio.mmdbase.io

import glm_.glm
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.abs

class MMDCamera {
    var m_interest = Vec3(0, 10, 0)
    var m_rotate = Vec3(0, 0, 0)
    var m_distance = 50f
    var m_fov = glm.radians(30f)
}

class MMDLookAtCamera(cam: MMDCamera) {
    var m_center = Vec3()
    var m_eye = Vec3()
    var m_up = Vec3()

    init {
        var view = Mat4(1f)
        view = glm.translate(view, Vec3(0, 0, abs(cam.m_distance)))
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