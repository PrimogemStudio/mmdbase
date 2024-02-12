package com.primogemstudio.mmdbase.model

import glm_.vec3.Vec3
import glm_.vec4.Vec4

enum class SphereTextureMode {
    None,
    Mul,
    Add
}

data class MMDMaterial(
    var m_diffuse: Vec3 = Vec3(),
    var m_alpha: Float = 0f,
    var m_specular: Vec3 = Vec3(),
    var m_specularPower: Float = 0f,
    var m_ambient: Vec3 = Vec3(),
    var m_edgeFlag: UByte = 0u,
    var m_edgeSize: Float = 0f,
    var m_edgeColor: Vec4 = Vec4(),
    var m_texture: String = "",
    var m_spTexture: String = "",
    var m_spTextureMode: SphereTextureMode = SphereTextureMode.None,
    var m_toonTexture: String = "",
    var m_textureMulFactor: Vec4 = Vec4(),
    var m_spTextureMulFactor: Vec4 = Vec4(),
    var m_toonTextureMulFactor: Vec4 = Vec4(),
    var m_textureAddFactor: Vec4 = Vec4(),
    var m_spTextureAddFactor: Vec4 = Vec4(),
    var m_toonTextureAddFactor: Vec4 = Vec4(),
    var m_bothFace: Boolean = false,
    var m_groundShadow: Boolean = false,
    var m_shadowCaster: Boolean = false,
    var m_shadowReceiver: Boolean = false
)