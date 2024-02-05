package com.primogemstudio.mmdbase.io

import glm_.vec3.Vec3
import glm_.vec4.Vec4

data class MMDMaterial(
    var m_diffuse: Vec3 = Vec3(1f),
    var m_alpha: Float = 1f,
    var m_specular: Vec3 = Vec3(0f),
    var m_specularPower: Float = 1f,
    var m_ambient: Vec3 = Vec3(0.2f),
    var m_edgeFlag: UByte = 0u,
    var m_edgeSize: Float = 0f,
    var m_edgeColor: Vec4 = Vec4(0f, 0f, 0f, 1f),
    var m_texture: String = "",
    var m_spTexture: String = "",
    var m_spTextureMode: SphereTextureMode = SphereTextureMode.None,
    var m_toonTexture: String = "",
    var m_textureMulFactor: Vec4 = Vec4(1f),
    var m_spTextureMulFactor: Vec4 = Vec4(1f),
    var m_toonTextureMulFactor: Vec4 = Vec4(1f),
    var m_bothFace: Boolean = false,
    var m_groundShadow: Boolean = true,
    var m_shadowCaster: Boolean = true,
    var m_shadowReceiver: Boolean = true
) {
    enum class SphereTextureMode {
        None,
        Mul,
        Add,
    }
}
