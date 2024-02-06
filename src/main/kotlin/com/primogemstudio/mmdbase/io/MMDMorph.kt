package com.primogemstudio.mmdbase.io

class MMDMorph {
    var m_name = ""
    var m_weight = 0f
    private var m_saveAnimWeight = 0f

    fun SaveBaseAnimation() {
        m_saveAnimWeight = m_weight
    }

    fun LoadBaseAnimation() {
        m_weight = m_saveAnimWeight
    }

    fun ClearBaseAnimation() {
        m_saveAnimWeight = 0f
    }

    fun GetBaseAnimationWeight(): Float = m_saveAnimWeight
}