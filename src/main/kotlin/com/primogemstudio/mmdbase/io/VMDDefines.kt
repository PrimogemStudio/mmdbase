package com.primogemstudio.mmdbase.io

import org.joml.Quaternionf
import org.joml.Vector3f

class VMDHeader {
    var m_header: String = ""
    var m_modelName: String = ""
}

class VMDMotion {
    var m_boneName: String = ""
    var m_frame: UInt = 0u
    var m_translate: Vector3f = Vector3f()
    var m_quaternion: Quaternionf = Quaternionf()
    @OptIn(ExperimentalUnsignedTypes::class)
    var m_interpolation: UByteArray = UByteArray(64)
}

class VMDMorph {
    var m_blendShapeName: String = ""
    var m_frame: UInt = 0u
    var m_weight: Float = 0f
}

class VMDCamera {
    var m_frame: UInt = 0u
    var m_distance: Float = 0f
    var m_interest: Vector3f = Vector3f()
    var m_rotate: Vector3f = Vector3f()
    @OptIn(ExperimentalUnsignedTypes::class)
    var m_interpolation: UByteArray = UByteArray(24)
    var m_viewAngle: UInt = 0u
    var m_isPerspective: UByte = 0u
}

class VMDLight {
    var m_frame: UInt = 0u
    var m_color: Vector3f = Vector3f()
    var m_position: Vector3f = Vector3f()
}

class VMDShadow {
    var m_frame: UInt = 0u
    var m_shadowType: UByte = 0u	// 0:Off 1:mode1 2:mode2
    var m_distance: Float = 0f
}

class VMDIkInfo {
    var m_name: String = ""
    var m_enable: UByte = 0u
}

class VMDIk {
    var m_frame: UInt = 0u
    var m_show: UByte = 0u
    var m_ikInfos: Array<VMDIkInfo> = emptyArray()
}

class VMDFile {
    var m_header: VMDHeader = VMDHeader()
    var m_motions: Array<VMDMotion> = emptyArray()
    var m_morphs: Array<VMDMorph> = emptyArray()
    var m_cameras: Array<VMDCamera> = emptyArray()
    var m_lights: Array<VMDLight> = emptyArray()
    var m_shadows: Array<VMDShadow> = emptyArray()
    var m_iks: Array<VMDIk> = emptyArray()
}