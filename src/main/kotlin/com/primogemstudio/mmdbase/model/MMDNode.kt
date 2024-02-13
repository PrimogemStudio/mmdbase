package com.primogemstudio.mmdbase.model

import glm_.glm
import glm_.vec3.Vec3
import glm_.quat.Quat
import glm_.mat4x4.Mat4

class MMDNode {
    var m_index: UInt = 0u
    var m_name: String = ""
    var m_enableIK: Boolean = false

    var m_parent: MMDNode? = null
    var m_child: MMDNode? = null
    var m_next: MMDNode? = null
    var m_prev: MMDNode? = null

    var m_translate: Vec3 = Vec3()
    var m_rotate: Quat = Quat(1f, 0f, 0f, 0f)
    var m_scale: Vec3 = Vec3(1f, 1f, 1f)

    var m_animTranslate: Vec3 = Vec3()
    var m_animRotate: Quat = Quat(1f, 0f, 0f, 0f)

    var m_baseAnimTranslate: Vec3 = Vec3()
        private set
    var m_baseAnimRotate: Quat = Quat(1f, 0f, 0f, 0f)
        private set

    var m_ikRotate: Quat = Quat(1f, 0f, 0f, 0f)

    var m_local: Mat4 = Mat4(1f)
    var m_global: Mat4 = Mat4(1f)
    var m_inverseInit: Mat4 = Mat4(1f)
        private set

    var m_initTranslate: Vec3 = Vec3()
        private set
    var m_initRotate: Quat = Quat(1f, 0f, 0f, 0f)
        private set
    var m_initScale: Vec3 = Vec3(1f, 1f, 1f)
        private set

    fun AddChild(child: MMDNode?) {
        assert(child != null)
        assert(child?.m_parent == null)
        assert(child?.m_next == null)
        assert(child?.m_prev == null)
        child?.m_parent = this

        if (m_child == null) {
            m_child = child
            m_child?.m_next = null
            m_child?.m_prev = m_child
        }
        else {
            val lastNode = m_child?.m_prev
            lastNode?.m_prev = child
            child?.m_prev = lastNode

            m_child?.m_prev = child
        }
    }

    fun BeginUpdateTransform() {
        LoadInitialTRS()
        m_ikRotate = Quat(1f, 0f, 0f, 0f)
        OnBeginUpdateTransform()
    }

    fun EndUpdateTransform() {
        OnEndUpdateTransform()
    }

    fun UpdateLocalTransform() {
        OnUpdateLocalTransform()
    }

    fun UpdateGlobalTransform() {
        m_global = if (m_parent == null) m_local
        else m_parent!!.m_global * m_local
        UpdateChildTransform()
    }

    fun UpdateChildTransform() {
        var child = m_child
        while (child != null) {
            child.UpdateGlobalTransform()
            child = child.m_next
        }
    }

    fun CalculateInverseInitTransform() { m_inverseInit = glm.inverse(m_global) }

    fun OnBeginUpdateTransform() = null
    fun OnEndUpdateTransform() = null

    fun OnUpdateLocalTransform() {
        val s = glm.scale(Mat4(1f), m_scale)
        var r = glm.mat4_cast(AnimateRotate())
        val t = glm.translate(Mat4(1f), AnimateTranslate())
        if (m_enableIK) r = glm.mat4_cast(m_ikRotate) * r
        m_local = t * r * s
    }

    fun SetAnimationTranslate(t: Vec3) { m_animTranslate = t }

    fun SetAnimationRotate(q: Quat) { m_animRotate = q }

    fun AnimateTranslate(): Vec3 = m_animTranslate + m_translate
    fun AnimateRotate(): Quat = m_animRotate * m_rotate

    fun SaveInitialTRS() {
        m_initTranslate = m_translate
        m_initRotate = m_rotate
        m_initScale = m_scale
    }

    fun LoadInitialTRS() {
        m_translate = m_initTranslate
        m_rotate = m_initRotate
        m_scale = m_initScale
    }

    fun SaveBaseAnimation() {
        m_baseAnimTranslate = m_animTranslate
        m_baseAnimRotate = m_animRotate
    }

    fun LoadBaseAnimation() {
        m_animTranslate = m_baseAnimTranslate
        m_animRotate = m_baseAnimRotate
    }

    fun ClearBaseAnimation() {
        m_baseAnimTranslate = Vec3()
        m_baseAnimRotate = Quat(1f, 0f, 0f, 0f)
    }
}