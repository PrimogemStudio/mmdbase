package com.primogemstudio.mmdbase.model

import com.primogemstudio.mmdbase.abstraction.IMMDObject
import glm_.glm
import glm_.quat.Quat
import glm_.vec3.Vec3

class MMDIkSolver: IMMDObject {
    data class IKChain(
        var m_node: MMDNode? = null,
        var m_enableAxisLimit: Boolean = false,
        var m_limitMax: Vec3 = Vec3(),
        var m_limitMin: Vec3 = Vec3(),
        var m_prevAngle: Vec3 = Vec3(),
        var m_saveIKRot: Quat = Quat(),
        var m_planeModeAngle: Float = 0f
    )

    enum class SolveAxis { X, Y, Z }

    private var m_chains = mutableListOf<IKChain>()
    var m_ikNode: MMDNode? = null
    var m_ikTarget: MMDNode? = null
    var m_iterateCount: UInt = 1u
        private set
    var m_limitAngle = glm.PI2f * 2f
        private set
    var m_enable = true
    var m_baseAnimEnable = true
        private set

    fun GetName() = m_ikNode?.m_name?: ""
    fun AddIKChain(node: MMDNode?, isKnee: Boolean = false) = AddIKChain(IKChain(
        m_node = node,
        m_enableAxisLimit = isKnee,
        m_limitMin = if (isKnee) Vec3(glm.radians(0.5f), 0f, 0f) else Vec3(),
        m_limitMax = if (isKnee) Vec3(glm.radians(180f), 0f, 0f) else Vec3(),
        m_saveIKRot = Quat(1f, 0f, 0f, 0f)
    ))
    fun AddIKChain(
        node: MMDNode?,
        axisLimit: Boolean,
        limitMin: Vec3?,
        limitMax: Vec3?
    ) = AddIKChain(IKChain(
        m_node = node,
        m_enableAxisLimit = axisLimit,
        m_limitMin = limitMin!!,
        m_limitMax = limitMax!!,
        m_saveIKRot = Quat(1f, 0f, 0f, 0f)
    ))
    fun Solve() {
        if (!m_enable || m_ikNode == null || m_ikTarget == null) return

        m_chains.forEach { chain ->
            chain.m_prevAngle = Vec3()
            chain.m_node?.m_ikRotate = Quat(1f, 0f, 0f, 0f)
            chain.m_planeModeAngle = 0f

            chain.m_node?.UpdateLocalTransform()
            chain.m_node?.UpdateGlobalTransform()
        }

        var maxDist = Float.MAX_VALUE
        for (i in 0u ..< m_iterateCount) {
            SolveCore(i)

            val gtPos = { a: MMDNode -> Vec3(a.m_global[3]) }
            val targetPos = gtPos(m_ikTarget!!)
            val ikPos = gtPos(m_ikNode!!)
            val dist = glm.length(targetPos - ikPos)

            if (dist < maxDist) {
                maxDist = dist
                m_chains.forEach { chain -> chain.m_saveIKRot = chain.m_node?.m_ikRotate!! }
            }
            else {
                m_chains.forEach { chain ->
                    chain.m_node?.m_ikRotate = chain.m_saveIKRot
                    chain.m_node?.UpdateLocalTransform()
                    chain.m_node?.UpdateGlobalTransform()
                }
                break
            }
        }
    }
    override fun SaveBaseAnimation() { m_baseAnimEnable = m_enable }
    override fun LoadBaseAnimation() { m_enable = m_baseAnimEnable }
    override fun ClearBaseAnimation() { m_baseAnimEnable = true }
    fun AddIKChain(chain: IKChain?) = m_chains.add(chain!!)
    fun SolveCore(iteration: UInt) = null
    fun SolvePlane(iteration: UInt, chainIdx: Int, solveAxis: SolveAxis) = null
}

fun NormalizeAngle(angle: Float): Float {
    var ret = angle
    while (ret >= glm.PI2f) ret -= glm.PI2f
    while (ret < 0) ret += glm.PI2f
    return ret
}

fun DiffAngle(a: Float, b: Float): Float {
    val diff = NormalizeAngle(a) - NormalizeAngle(b)
    return if (diff > glm.PIf) diff - glm.PI2f
    else if (diff < -glm.PIf) diff + glm.PI2f
    else diff
}