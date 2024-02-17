package com.primogemstudio.mmdbase.model

import com.primogemstudio.mmdbase.abstraction.IMMDObject

class MMDMorph: IMMDObject {
    var m_name: String = ""
    var m_weight: Float = 0f
    var m_saveAnimWeight: Float = 0f
        private set
    override fun SaveBaseAnimation() { m_saveAnimWeight = m_weight }
    override fun LoadBaseAnimation() { m_weight = m_saveAnimWeight }
    override fun ClearBaseAnimation() { m_saveAnimWeight = 0f }
}