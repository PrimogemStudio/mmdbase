package com.primogemstudio.mmdbase.io

import glm_.glm
import glm_.mat3x3.Mat3
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import java.util.Vector
import kotlin.math.*

class MMDIkSolver {
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
    private var m_chains = Vector<IKChain>()
    var m_ikNode: MMDNode? = null
    var m_ikTarget: MMDNode? = null
    var m_iterateCount = 1u
    var m_limitAngle: Float = glm.PIf * 2f
    var m_enable = true
    var m_baseAnimEnable = true
        private set
    fun GetName(): String? = m_ikNode?.m_name
    fun AddIKChain(chain: IKChain) = m_chains.add(chain)
    fun SolveCore(iteration: UInt) {
        val ikPos = Vec3(m_ikNode!!.m_global[3])
        for (chainIdx in m_chains.indices) {
            val chain = m_chains[chainIdx]
            val chainNode = chain.m_node

            if (chainNode == m_ikTarget) continue
            if (chain.m_enableAxisLimit) {
                if ((chain.m_limitMin.x != 0f || chain.m_limitMax.x != 0f) &&
                    (chain.m_limitMin.y == 0f || chain.m_limitMax.y == 0f) &&
                    (chain.m_limitMin.z == 0f || chain.m_limitMax.z == 0f)) {
                    SolvePlane(iteration, chainIdx, SolveAxis.X)
                    continue
                }
                else if ((chain.m_limitMin.y != 0f || chain.m_limitMax.y != 0f) &&
                    (chain.m_limitMin.x == 0f || chain.m_limitMax.x == 0f) &&
                    (chain.m_limitMin.z == 0f || chain.m_limitMax.z == 0f)) {
                    SolvePlane(iteration, chainIdx, SolveAxis.Y)
                    continue
                }
                else if ((chain.m_limitMin.z != 0f || chain.m_limitMax.z != 0f) &&
                    (chain.m_limitMin.x == 0f || chain.m_limitMax.x == 0f) &&
                    (chain.m_limitMin.y == 0f || chain.m_limitMax.y == 0f)) {
                    SolvePlane(iteration, chainIdx, SolveAxis.Z)
                    continue
                }
            }

            val targetPos = Vec3(m_ikTarget!!.m_global[3])

            val invChain = glm.inverse(chain.m_node!!.m_global)

            val chainIkPos = Vec3(invChain * Vec4(ikPos, 1))
            val chainTargetPos = Vec3(invChain * Vec4(targetPos, 1))

            val chainIkVec = glm.normalize(chainIkPos)
            val chainTargetVec = glm.normalize(chainTargetPos)

            val dot = glm.clamp(glm.dot(chainTargetVec, chainIkVec), -1f, 1f)

            var angle = acos(dot)
            val angleDeg = glm.degrees(angle)
            if (angleDeg < 1.0e-3f) continue

            angle = glm.clamp(angle, -m_limitAngle, m_limitAngle)
            val cross = glm.normalize(glm.cross(chainTargetVec, chainIkVec))
            val rot = glm.rotate(Quat(1, 0, 0, 0), angle, cross)

            var chainRot = chainNode!!.m_ikRotate * chainNode.m_animRotate * rot
            if (chain.m_enableAxisLimit) {
                var chainRotM = glm.mat3_cast(chainRot)
                val rotXYZ = Decompose(chainRotM, chain.m_prevAngle)

                var clampXYZ = glm.clamp(rotXYZ, chain.m_limitMin, chain.m_limitMax)
                clampXYZ = glm.clamp(clampXYZ - chain.m_prevAngle, -m_limitAngle, m_limitAngle) + chain.m_prevAngle
                var r = glm.rotate(Quat(1, 0, 0, 0), clampXYZ.x, Vec3(1, 0, 0))
                r = glm.rotate(r, clampXYZ.y, Vec3(0, 1, 0))
                r = glm.rotate(r, clampXYZ.z, Vec3(0, 0, 1))
                chainRotM = glm.mat3_cast(r)
                chain.m_prevAngle = clampXYZ

                chainRot = glm.quat_cast(chainRotM)
            }

            val ikRot = chainRot * glm.inverse(chainNode.m_animRotate)
            chainNode.m_ikRotate = ikRot

            chainNode.UpdateLocalTransform()
            chainNode.UpdateGlobalTransform()
        }
    }
    fun SolvePlane(iteration: UInt, chainIdx: Int, solveAxis: SolveAxis) {
        var RotateAxisIndex = 0
        var RotateAxis = Vec3(1, 0, 0)
        var Plane = Vec3(0, 1, 1)
        when (solveAxis) {
            SolveAxis.X -> {
                RotateAxisIndex = 0
                RotateAxis = Vec3(1, 0, 0)
                Plane = Vec3(0, 1, 1)
            }
            SolveAxis.Y -> {
                RotateAxisIndex = 1
                RotateAxis = Vec3(0, 1, 0)
                Plane = Vec3(1, 0, 1)
            }
            SolveAxis.Z -> {
                RotateAxisIndex = 2
                RotateAxis = Vec3(0, 0, 1)
                Plane = Vec3(1, 1, 0)
            }
        }

        val chain = m_chains[chainIdx]
        val ikPos = Vec3(m_ikNode!!.m_global[3])

        val targetPos = Vec3(m_ikTarget!!.m_global[3])

        val invChain = glm.inverse(chain.m_node!!.m_global)

        val chainIkPos = Vec3(invChain * Vec4(ikPos, 1))
        val chainTargetPos = Vec3(invChain * Vec4(targetPos, 1))

        val chainIkVec = glm.normalize(chainIkPos)
        val chainTargetVec = glm.normalize(chainTargetPos)

        val dot = glm.clamp(glm.dot(chainTargetVec, chainIkVec), -1f, 1f)

        var angle = acos(dot)
        val angleDeg = glm.degrees(angle)

        angle = glm.clamp(angle, -m_limitAngle, m_limitAngle)

        val rot1 = glm.rotate(Quat(1, 0, 0, 0), angle, RotateAxis)
        val targetVec1 = rot1 * chainTargetVec
        val dot1 = glm.dot(targetVec1, chainIkVec)

        val rot2 = glm.rotate(Quat(1, 0, 0, 0), -angle, RotateAxis)
        val targetVec2 = rot2 * chainTargetVec
        val dot2 = glm.dot(targetVec2, chainIkVec)

        var newAngle = chain.m_planeModeAngle
        if (dot1 > dot2) newAngle += angle
        else newAngle -= angle

        if (iteration == 0u) {
            if (newAngle < chain.m_limitMin[RotateAxisIndex] || newAngle > chain.m_limitMax[RotateAxisIndex]) {
                if (-newAngle > chain.m_limitMin[RotateAxisIndex] && -newAngle < chain.m_limitMax[RotateAxisIndex]) newAngle *= -1
                else {
                    val halfRad = (chain.m_limitMin[RotateAxisIndex] + chain.m_limitMax[RotateAxisIndex]) * 0.5f;
                    if (glm.abs(halfRad - newAngle) > glm.abs(halfRad + newAngle)) newAngle *= -1
                }
            }
        }

        newAngle = glm.clamp(newAngle, chain.m_limitMin[RotateAxisIndex], chain.m_limitMax[RotateAxisIndex])
        chain.m_planeModeAngle = newAngle

        val ikRotM = glm.rotate(Quat(1, 0, 0, 0), newAngle, RotateAxis) * glm.inverse(chain.m_node!!.m_animRotate)
        chain.m_node!!.m_ikRotate = ikRotM

        chain.m_node!!.UpdateLocalTransform()
        chain.m_node!!.UpdateGlobalTransform()
    }
    fun AddIKChain(node: MMDNode?, isKnee: Boolean = false) {
        AddIKChain(IKChain(
            m_node = node,
            m_enableAxisLimit = isKnee,
            m_saveIKRot = Quat(1, 0, 0, 0)
        ).apply {
            if (isKnee) {
                m_limitMin = Vec3(glm.radians(0.5f), 0, 0)
                m_limitMax = Vec3(glm.radians(180f), 0, 0)
            }
        })
    }
    fun AddIKChain(node: MMDNode?, axisLimit: Boolean, limitMin: Vec3, limitMax: Vec3) {
        AddIKChain(IKChain(
            m_node = node,
            m_enableAxisLimit = axisLimit,
            m_limitMin = limitMin,
            m_limitMax = limitMax,
            m_saveIKRot = Quat(1, 0, 0, 0)
        ))
    }
    fun Solve() {
        if (!m_enable || m_ikNode == null || m_ikTarget == null) return
        m_chains.forEach {
            it.m_prevAngle = Vec3(0)
            it.m_node?.m_ikRotate = Quat(1, 0, 0, 0)
            it.m_planeModeAngle = 0f

            it.m_node?.UpdateLocalTransform()
            it.m_node?.UpdateGlobalTransform()
        }
        var maxDist = Float.MAX_VALUE
        for (i in 0u ..< maxDist.toUInt()) {
            SolveCore(i)

            val targetPos = Vec3(m_ikTarget!!.m_global[3])
            val ikPos = Vec3(m_ikNode!!.m_global[3])
            val dist = glm.length(targetPos - ikPos)
            if (dist < maxDist) {
                maxDist = dist
                m_chains.forEach { it.m_saveIKRot = it.m_node?.m_ikRotate!! }
            }
            else {
                m_chains.forEach {
                    it.m_node?.m_ikRotate = it.m_saveIKRot
                    it.m_node?.UpdateLocalTransform()
                    it.m_node?.UpdateGlobalTransform()
                }
            }
        }
    }
    fun SaveBaseAnimation() { m_baseAnimEnable = m_enable }
    fun LoadBaseAnimation() { m_enable = m_baseAnimEnable }
    fun ClearBaseAnimation() { m_baseAnimEnable = true }
}

fun NormalizeAngle(angle: Float): Float {
    var ret = angle
    while (ret >= glm.PI2f) ret -= glm.PI2f
    while (ret < 0) ret += glm.PI2f
    return ret
}

fun DiffAngle(a: Float, b: Float): Float {
    var diff = NormalizeAngle(a) - NormalizeAngle(b)
    if (diff > glm.PIf) return diff - glm.PI2f
    else if (diff < -glm.PIf) return diff + glm.PI2f
    return diff
}

fun ClampAngle(angle: Float, minAngle: Float, maxAngle: Float): Float {
    if (minAngle == maxAngle) return minAngle
    var ret = angle
    while (ret < minAngle) ret += glm.PI2f
    if (ret < maxAngle) return ret

    while (ret > maxAngle) ret -= glm.PI2f
    if (ret > minAngle) return ret

    return if (abs(DiffAngle(minAngle, ret)) < abs(DiffAngle(maxAngle, ret))) minAngle else maxAngle
}

fun Decompose(m: Mat3, before: Vec3): Vec3 {
    var r = Vec3()
    val sy = -m[0][2]
    val e = 1.0e-6f
    if ((1f - abs(sy)) < e) {
        r.y = asin(sy)
        val sx = sin(before.x)
        val sz = sin(before.z)
        if (abs(sx) < abs(sz)) {
            if (cos(before.x) > 0) {
                r.x = 0f
                r.z = asin(-m[1][0])
            }
            else {
                r.x = glm.PIf
                r.z = asin(m[1][0])
            }
        }
        else {
            if (cos(before.z) > 0) {
                r.z = 0f
                r.x = asin(-m[2][1])
            }
            else {
                r.z = glm.PIf
                r.x = asin(m[2][1])
            }
        }
    }
    else {
        r.x = atan2(m[1][2], m[2][2])
        r.y = asin(-m[0][2])
        r.z = atan2(m[0][1], m[0][0])
    }

    val pi = glm.PIf
    val tests = arrayOf<Vec3>(
        Vec3(r.x + pi, pi - r.y, r.z + pi),
        Vec3(r.x + pi, pi - r.y, r.z - pi),
        Vec3(r.x + pi, -pi - r.y, r.z + pi),
        Vec3(r.x + pi, -pi - r.y, r.z - pi),
        Vec3(r.x - pi, pi - r.y, r.z + pi),
        Vec3(r.x - pi, pi - r.y, r.z - pi),
        Vec3(r.x - pi, -pi - r.y, r.z + pi),
        Vec3(r.x - pi, -pi - r.y, r.z - pi),
    )
    val errX = abs(DiffAngle(r.x, before.x))
    val errY = abs(DiffAngle(r.y, before.y))
    val errZ = abs(DiffAngle(r.z, before.z))
    var minErr = errX + errY + errZ

    tests.forEach {
        val err = abs(DiffAngle(it.x, before.x)) +
                abs(DiffAngle(it.y, before.y)) +
                abs(DiffAngle(it.z, before.z))
        if (err < minErr) {
            minErr = err
            r = it
        }
    }
    return r
}

fun RotateFromTo(from: Vec3, to: Vec3): Quat {
    val nf = glm.normalize(from)
    val nt = glm.normalize(to)
    val localW = glm.cross(nf, nt)
    val dot = glm.dot(nf, nt)
    return if (abs(1f + dot) < 1.0e-7) {
        var v = glm.abs(from)
        v = if (v.x < v.y) {
            if (v.x < v.z) Vec3(1, 0, 0)
            else Vec3(0, 0, 1)
        } else {
            if (v.y < v.z) Vec3(0, 1, 0)
            else Vec3(0, 0, 1)
        }
        val axis = glm.normalize(glm.cross(from, v))
        Quat(0f, axis)
    }
    else glm.normalize(Quat(1f + dot, localW))
}