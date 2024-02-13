package com.primogemstudio.mmdbase.model

class MMDMorph {
    var m_name: String = ""
    var m_weight: Float = 0f
    var m_saveAnimWeight: Float = 0f
        private set
    fun saveBaseAnimation() { m_saveAnimWeight = m_weight }
    fun loadBaseAnimation() { m_weight = m_saveAnimWeight }
    fun clearBaseAnimation() { m_saveAnimWeight = 0f }
}