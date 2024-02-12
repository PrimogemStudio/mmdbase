package com.primogemstudio.mmdbase.io

import com.primogemstudio.mmdbase.model.*
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import java.io.DataInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class ModelDataInputStream(flow: InputStream) : DataInputStream(flow) {
    private fun readLEInt(): Int {
        return readNLEInts(1)[0]
    }

    private fun readLEFloat(): Float {
        return readNLEFloats(1)[0]
    }

    private fun readLEShort(): Short {
        return readNLEShorts(1)[0]
    }

    private fun readText(isUtf16: Boolean): String {
        val length = readLEInt()
        return String(readNBytes(length), if (isUtf16) StandardCharsets.UTF_16LE else StandardCharsets.UTF_8)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun readNLEFloats(n: Int): FloatArray {
        val buf = ByteBuffer.wrap(readNBytes(n * 4)).order(ByteOrder.LITTLE_ENDIAN)
        val arr = FloatArray(n)
        for (i in 0..<n) arr[i] = buf.getFloat(i * 4)
        return arr
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun readNLEShorts(n: Int): ShortArray {
        val buf = ByteBuffer.wrap(readNBytes(n * 2)).order(ByteOrder.LITTLE_ENDIAN)
        val arr = ShortArray(n)
        for (i in 0..<n) arr[i] = buf.getShort(i * 2)
        return arr
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun readNLEInts(n: Int): IntArray {
        val buf = ByteBuffer.wrap(readNBytes(n * 4)).order(ByteOrder.LITTLE_ENDIAN)
        val arr = IntArray(n)
        for (i in 0..<n) arr[i] = buf.getInt(i * 4)
        return arr
    }

    private fun readVec2(): Vec2 {
        return Vec2(readNLEFloats(2))
    }

    private fun readVec3(): Vec3 {
        return Vec3(readNLEFloats(3))
    }

    private fun readVec4(): Vec4 {
        return Vec4(readNLEFloats(4))
    }

    private fun readQuat(): Quat {
        return Quat(readVec4())
    }

    private fun readPMXHeader(header: PMXHeader) {
        header.m_magic = String(readNBytes(4))
        header.m_version = readLEFloat()
        header.m_dataSize = readByte()
        header.m_encode = readByte()
        header.m_addUVNum = readByte()
        header.m_vertexIndexSize = readByte()
        header.m_textureIndexSize = readByte()
        header.m_materialIndexSize = readByte()
        header.m_boneIndexSize = readByte()
        header.m_morphIndexSize = readByte()
        header.m_rigidbodyIndexSize = readByte()
    }

    private fun readPMXInfo(info: PMXInfo, isU16: Boolean) {
        info.m_modelName = readText(isU16)
        info.m_englishModelName = readText(isU16)
        info.m_comment = readText(isU16)
        info.m_englishComment = readText(isU16)
    }

    private fun readIndex(size: Int): Int {
        val arr = Array(1) { 0 }
        readIndex(arr, 0, size)
        return arr[0]
    }

    private fun readIndex(index: Array<Int>, offset: Int, size: Int) {
        when (size) {
            1 -> {
                val idx = readByte()
                if (idx != 0xFF.toByte()) {
                    index[offset] = idx.toInt()
                } else {
                    index[offset] = -1
                }
            }

            2 -> {
                val idx = readLEShort()
                if (idx != 0xFFFF.toShort()) {
                    index[offset] = idx.toInt()
                } else {
                    index[offset] = -1
                }
            }

            4 -> index[offset] = readLEInt()
        }
    }

    private fun readPMXVertices(vertices: Array<PMXVertex>, header: PMXHeader) {
        for (vertex in vertices) {
            vertex.m_position = readVec3()
            vertex.m_normal = readVec3()
            vertex.m_uv = readVec2()
            for (i in 0..<header.m_addUVNum) {
                vertex.m_addUV[i] = readVec4()
            }
            vertex.m_weightType = PMXVertex.PMXVertexWeight.entries[readByte().toInt()]
            when (vertex.m_weightType) {
                PMXVertex.PMXVertexWeight.BDEF1 -> readIndex(vertex.m_boneIndices, 0, header.m_boneIndexSize.toInt())
                PMXVertex.PMXVertexWeight.BDEF2 -> {
                    readIndex(vertex.m_boneIndices, 0, header.m_boneIndexSize.toInt())
                    readIndex(vertex.m_boneIndices, 1, header.m_boneIndexSize.toInt())
                    vertex.m_boneWeights.x = readLEFloat()
                }

                PMXVertex.PMXVertexWeight.BDEF4, PMXVertex.PMXVertexWeight.QDEF -> {
                    readIndex(vertex.m_boneIndices, 0, header.m_boneIndexSize.toInt())
                    readIndex(vertex.m_boneIndices, 1, header.m_boneIndexSize.toInt())
                    readIndex(vertex.m_boneIndices, 2, header.m_boneIndexSize.toInt())
                    readIndex(vertex.m_boneIndices, 3, header.m_boneIndexSize.toInt())
                    vertex.m_boneWeights = readVec4()
                }

                PMXVertex.PMXVertexWeight.SDEF -> {
                    readIndex(vertex.m_boneIndices, 0, header.m_boneIndexSize.toInt())
                    readIndex(vertex.m_boneIndices, 1, header.m_boneIndexSize.toInt())
                    vertex.m_boneWeights.x = readLEFloat()
                    vertex.m_sdefC = readVec3()
                    vertex.m_sdefR0 = readVec3()
                    vertex.m_sdefR1 = readVec3()
                }
            }
            vertex.m_edgeMag = readLEFloat()
        }
    }

    private fun readPMXFaces(face: Array<PMXFace>, header: PMXHeader) {
        val arr = when (header.m_vertexIndexSize) {
            1.toByte() -> readNBytes(face.size * 3)
            2.toByte() -> readNLEShorts(face.size * 3)
            4.toByte() -> readNLEInts(face.size * 3)
            else -> TODO("Unknown data size: ${header.m_vertexIndexSize}")
        }
        for (faceIdx in face.indices) {
            for (i in 0..<3) face[faceIdx].m_vertices[i] = arr.fetchInt(faceIdx * 3 + i)
        }
    }

    private fun readPMXTextures(textures: Array<String>, header: PMXHeader) {
        for (i in textures.indices) textures[i] = readText(header.m_encode == 0.toByte())
    }

    private fun readPMXMaterials(materials: Array<PMXMaterial>, header: PMXHeader) {
        for (i in materials.indices) {
            materials[i].m_name = readText(header.m_encode == 0.toByte())
            materials[i].m_englishName = readText(header.m_encode == 0.toByte())
            materials[i].m_diffuse = readVec4()
            materials[i].m_specular = readVec3()
            materials[i].m_specularPower = readLEFloat()
            materials[i].m_ambient = readVec3()
            materials[i].m_drawMode = PMXMaterial.PMXDrawModeFlags.findMode(readByte())
            materials[i].m_edgeColor = readVec4()
            materials[i].m_edgeSize = readLEFloat()
            materials[i].m_textureIndex = readIndex(header.m_textureIndexSize.toInt())
            materials[i].m_sphereTextureIndex = readIndex(header.m_textureIndexSize.toInt())
            materials[i].m_sphereMode = PMXMaterial.PMXSphereMode.entries[readByte().toInt()]
            materials[i].m_toonMode = PMXMaterial.PMXToonMode.entries[readByte().toInt()]
            materials[i].m_toonTextureIndex = when (materials[i].m_toonMode) {
                PMXMaterial.PMXToonMode.Separate -> readIndex(header.m_textureIndexSize.toInt())
                PMXMaterial.PMXToonMode.Common -> readByte().toInt()
            }

            materials[i].m_memo = readText(header.m_encode == 0.toByte())
            materials[i].m_numFaceVertices = readLEInt()
        }
    }

    private fun readPMXBones(bones: Array<PMXBone>, header: PMXHeader) {
        for (i in bones.indices) {
            bones[i].m_name = readText(header.m_encode == 0.toByte())
            bones[i].m_englishName = readText(header.m_encode == 0.toByte())
            bones[i].m_position = readVec3()
            bones[i].m_parentBoneIndex = readIndex(header.m_boneIndexSize.toInt())
            bones[i].m_deformDepth = readLEInt()
            bones[i].m_boneFlag = readLEShort().toInt()

            val flag = bones[i].m_boneFlag

            if (flag and PMXBoneFlags.TargetShowMode.flag == 0) bones[i].m_positionOffset = readVec3()
            else bones[i].m_linkBoneIndex = readIndex(header.m_boneIndexSize.toInt())

            if ((flag and PMXBoneFlags.AppendRotate.flag) != 0 || (flag and PMXBoneFlags.AppendTranslate.flag) != 0) {
                bones[i].m_appendBoneIndex = readIndex(header.m_boneIndexSize.toInt())
                bones[i].m_appendWeight = readLEFloat()
            }

            if ((flag and PMXBoneFlags.FixedAxis.flag) != 0) bones[i].m_fixedAxis = readVec3()
            if ((flag and PMXBoneFlags.LocalAxis.flag) != 0) {
                bones[i].m_localXAxis = readVec3()
                bones[i].m_localZAxis = readVec3()
            }
            if ((flag and PMXBoneFlags.DeformOuterParent.flag) != 0) bones[i].m_keyValue = readLEInt()
            if ((flag and PMXBoneFlags.IK.flag) != 0) {
                bones[i].m_ikTargetBoneIndex = readIndex(header.m_boneIndexSize.toInt())
                bones[i].m_ikIterationCount = readLEInt()
                bones[i].m_ikLimit = readLEFloat()

                val size = readLEInt()
                bones[i].m_ikLinks = Array(size) { PMXBone.PMXIKLink() }
                for (j in bones[i].m_ikLinks.indices) {
                    bones[i].m_ikLinks[j].m_ikBoneIndex = readIndex(header.m_boneIndexSize.toInt())
                    bones[i].m_ikLinks[j].m_enableLimit = readByte()
                    if (bones[i].m_ikLinks[j].m_enableLimit.toUByte() != 0.toUByte()) {
                        bones[i].m_ikLinks[j].m_limitMin = readVec3()
                        bones[i].m_ikLinks[j].m_limitMax = readVec3()
                    }
                }
            }
        }
    }

    private fun readPMXMorphs(morphs: Array<PMXMorph>, header: PMXHeader) {
        for (i in morphs.indices) {
            morphs[i].m_name = readText(header.m_encode == 0.toByte())
            morphs[i].m_englishName = readText(header.m_encode == 0.toByte())
            morphs[i].m_controlPanel = readByte()
            morphs[i].m_morphType = PMXMorph.PMXMorphType.entries[readByte().toInt()]
            val size = readLEInt()
            val type = morphs[i].m_morphType
            when {
                type == PMXMorph.PMXMorphType.Position -> {
                    morphs[i].m_positionMorph = Array(size) { PMXMorph.PositionMorph() }
                    for (j in morphs[i].m_positionMorph.indices) {
                        morphs[i].m_positionMorph[j].m_vertexIndex = readIndex(header.m_vertexIndexSize.toInt())
                        morphs[i].m_positionMorph[j].m_position = readVec3()
                    }
                }

                setOf(
                    PMXMorph.PMXMorphType.UV,
                    PMXMorph.PMXMorphType.AddUV1,
                    PMXMorph.PMXMorphType.AddUV2,
                    PMXMorph.PMXMorphType.AddUV3,
                    PMXMorph.PMXMorphType.AddUV4
                ).contains(type) -> {
                    morphs[i].m_uvMorph = Array(size) { PMXMorph.UVMorph() }
                    for (j in morphs[i].m_uvMorph.indices) {
                        morphs[i].m_uvMorph[j].m_vertexIndex = readIndex(header.m_vertexIndexSize.toInt())
                        morphs[i].m_uvMorph[j].m_uv = readVec4()
                    }
                }

                type == PMXMorph.PMXMorphType.Bone -> {
                    morphs[i].m_boneMorph = Array(size) { PMXMorph.BoneMorph() }
                    for (j in morphs[i].m_boneMorph.indices) {
                        morphs[i].m_boneMorph[j].m_boneIndex = readIndex(header.m_boneIndexSize.toInt())
                        morphs[i].m_boneMorph[j].m_position = readVec3()
                        morphs[i].m_boneMorph[j].m_quaternion = readQuat()
                    }
                }

                type == PMXMorph.PMXMorphType.Material -> {
                    morphs[i].m_materialMorph = Array(size) { PMXMorph.MaterialMorph() }
                    for (j in morphs[i].m_materialMorph.indices) {
                        morphs[i].m_materialMorph[j].m_materialIndex = readIndex(header.m_materialIndexSize.toInt())
                        morphs[i].m_materialMorph[j].m_opType = PMXMorph.MaterialMorph.OpType.entries[readByte().toInt()]
                        morphs[i].m_materialMorph[j].m_diffuse = readVec4()
                        morphs[i].m_materialMorph[j].m_specular = readVec3()
                        morphs[i].m_materialMorph[j].m_specularPower = readLEFloat()
                        morphs[i].m_materialMorph[j].m_ambient = readVec3()
                        morphs[i].m_materialMorph[j].m_edgeColor = readVec4()
                        morphs[i].m_materialMorph[j].m_edgeSize = readLEFloat()
                        morphs[i].m_materialMorph[j].m_textureFactor = readVec4()
                        morphs[i].m_materialMorph[j].m_sphereTextureFactor = readVec4()
                        morphs[i].m_materialMorph[j].m_toonTextureFactor = readVec4()
                    }
                }

                type == PMXMorph.PMXMorphType.Group -> {
                    morphs[i].m_groupMorph = Array(size) { PMXMorph.GroupMorph() }
                    for (j in morphs[i].m_groupMorph.indices) {
                        morphs[i].m_groupMorph[j].m_morphIndex = readIndex(header.m_morphIndexSize.toInt())
                        morphs[i].m_groupMorph[j].m_weight = readLEFloat()
                    }
                }

                type == PMXMorph.PMXMorphType.Flip -> {
                    morphs[i].m_flipMorph = Array(size) { PMXMorph.FlipMorph() }
                    for (j in morphs[i].m_flipMorph.indices) {
                        morphs[i].m_flipMorph[j].m_morphIndex = readIndex(header.m_morphIndexSize.toInt())
                        morphs[i].m_flipMorph[j].m_weight = readLEFloat()
                    }
                }

                type == PMXMorph.PMXMorphType.Impluse -> {
                    morphs[i].m_impulseMorph = Array(size) { PMXMorph.ImpulseMorph() }
                    for (j in morphs[i].m_impulseMorph.indices) {
                        morphs[i].m_impulseMorph[j].m_rigidbodyIndex = readIndex(header.m_rigidbodyIndexSize.toInt())
                        morphs[i].m_impulseMorph[j].m_localFlag = readByte()
                        morphs[i].m_impulseMorph[j].m_translateVelocity = readVec3()
                        morphs[i].m_impulseMorph[j].m_rotateTorque = readVec3()
                    }
                }

                else -> TODO("Unknown morph type")
            }
        }
    }

    private fun readPMXDisplayFrames(frames: Array<PMXDisplayFrame>, header: PMXHeader) {
        for (i in frames.indices) {
            frames[i].m_name = readText(header.m_encode == 0.toByte())
            frames[i].m_englishName = readText(header.m_encode == 0.toByte())
            frames[i].m_flag = PMXDisplayFrame.FrameType.entries[readByte().toInt()]
            val siz = readLEInt()
            frames[i].m_targets = Array(siz) { PMXDisplayFrame.Target() }
            for (j in frames[i].m_targets.indices) {
                frames[i].m_targets[j].m_type = PMXDisplayFrame.Target.TargetType.entries[readByte().toInt()]
                frames[i].m_targets[j].m_index = readIndex(
                    when (frames[i].m_targets[j].m_type) {
                        PMXDisplayFrame.Target.TargetType.BoneIndex -> header.m_boneIndexSize
                        PMXDisplayFrame.Target.TargetType.MorphIndex -> header.m_morphIndexSize
                    }.toInt()
                )
            }
        }
    }

    private fun readPMXRigidBodies(rigidbodies: Array<PMXRigidBody>, header: PMXHeader) {
        for (i in rigidbodies.indices) {
            rigidbodies[i].m_name = readText(header.m_encode == 0.toByte())
            rigidbodies[i].m_englishName = readText(header.m_encode == 0.toByte())
            rigidbodies[i].m_boneIndex = readIndex(header.m_boneIndexSize.toInt())
            rigidbodies[i].m_group = readByte()
            rigidbodies[i].m_collisionGroup = readLEShort()
            rigidbodies[i].m_shape = PMXRigidBody.Shape.entries[readByte().toInt()]
            rigidbodies[i].m_shapeSize = readVec3()
            rigidbodies[i].m_translate = readVec3()
            rigidbodies[i].m_rotate = readVec3()

            rigidbodies[i].m_mass = readLEFloat()
            rigidbodies[i].m_translateDimmer = readLEFloat()
            rigidbodies[i].m_rotateDimmer = readLEFloat()
            rigidbodies[i].m_repulsion = readLEFloat()
            rigidbodies[i].m_friction = readLEFloat()
            rigidbodies[i].m_op = PMXRigidBody.Operation.entries[readByte().toInt()]
        }
    }

    private fun readPMXJoints(joints: Array<PMXJoint>, header: PMXHeader) {
        for (i in joints.indices) {
            joints[i].m_name = readText(header.m_encode == 0.toByte())
            joints[i].m_englishName = readText(header.m_encode == 0.toByte())
            joints[i].m_type = PMXJoint.JointType.entries[readByte().toInt()]
            joints[i].m_rigidbodyAIndex = readIndex(header.m_rigidbodyIndexSize.toInt())
            joints[i].m_rigidbodyBIndex = readIndex(header.m_rigidbodyIndexSize.toInt())

            joints[i].m_translate = readVec3()
            joints[i].m_rotate = readVec3()
            joints[i].m_translateLowerLimit = readVec3()
            joints[i].m_translateUpperLimit = readVec3()
            joints[i].m_rotateLowerLimit = readVec3()
            joints[i].m_rotateUpperLimit = readVec3()
            joints[i].m_springTranslateFactor = readVec3()
            joints[i].m_springRotateFactor = readVec3()
        }
    }

    private fun readPMXSoftBodies(bodies: Array<PMXSoftBody>, header: PMXHeader) {
        for (i in bodies.indices) {
            bodies[i].m_name = readText(header.m_encode == 0.toByte())
            bodies[i].m_englishName = readText(header.m_encode == 0.toByte())
            bodies[i].m_type = PMXSoftBody.SoftbodyType.entries[readByte().toInt()]
            bodies[i].m_materialIndex = readIndex(header.m_materialIndexSize.toInt())
            bodies[i].m_group = readByte()
            bodies[i].m_collisionGroup = readLEShort()
            bodies[i].m_flag = PMXSoftBody.SoftbodyMask.getMask(readByte())
            bodies[i].m_BLinkLength = readLEInt()
            bodies[i].m_numClusters = readLEInt()
            bodies[i].m_totalMass = readLEFloat()
            bodies[i].m_collisionMargin = readLEFloat()
            bodies[i].m_aeroModel = PMXSoftBody.AeroModel.entries[readLEInt()]

            bodies[i].m_VCF = readLEFloat()
            bodies[i].m_DP = readLEFloat()
            bodies[i].m_DG = readLEFloat()
            bodies[i].m_LF = readLEFloat()
            bodies[i].m_PR = readLEFloat()
            bodies[i].m_VC = readLEFloat()
            bodies[i].m_DF = readLEFloat()
            bodies[i].m_MT = readLEFloat()
            bodies[i].m_CHR = readLEFloat()
            bodies[i].m_KHR = readLEFloat()
            bodies[i].m_SHR = readLEFloat()
            bodies[i].m_AHR = readLEFloat()

            bodies[i].m_SRHR_CL = readLEFloat()
            bodies[i].m_SKHR_CL = readLEFloat()
            bodies[i].m_SSHR_CL = readLEFloat()
            bodies[i].m_SR_SPLT_CL = readLEFloat()
            bodies[i].m_SK_SPLT_CL = readLEFloat()
            bodies[i].m_SS_SPLT_CL = readLEFloat()

            bodies[i].m_V_IT = readLEInt()
            bodies[i].m_P_IT = readLEInt()
            bodies[i].m_D_IT = readLEInt()
            bodies[i].m_C_IT = readLEInt()

            bodies[i].m_LST = readLEFloat()
            bodies[i].m_AST = readLEFloat()
            bodies[i].m_VST = readLEFloat()

            val arcnt = readLEInt()
            bodies[i].m_anchorRigidBodies = Array(arcnt) { PMXSoftBody.AnchorRigidbody() }
            for (j in bodies[i].m_anchorRigidBodies.indices) {
                bodies[i].m_anchorRigidBodies[j].m_rigidBodyIndex = readIndex(header.m_rigidbodyIndexSize.toInt())
                bodies[i].m_anchorRigidBodies[j].m_vertexIndex = readIndex(header.m_vertexIndexSize.toInt())
                bodies[i].m_anchorRigidBodies[j].m_nearMode = readByte()
            }

            val pvcnt = readLEInt()
            bodies[i].m_pinVertexIndices = Array(pvcnt) { 0 }
            for (n in bodies[i].m_pinVertexIndices.indices) {
                bodies[i].m_pinVertexIndices[n] = readIndex(header.m_vertexIndexSize.toInt())
            }
        }
    }

    fun readPMXFile(): PMXFile {
        val file = PMXFile()
        readPMXHeader(file.m_header)
        readPMXInfo(file.m_info, file.m_header.m_encode == 0.toByte())
        file.m_vertices = Array(readLEInt()) { PMXVertex() }
        readPMXVertices(file.m_vertices, file.m_header)
        file.m_faces = Array(readLEInt() / 3) { PMXFace() }
        readPMXFaces(file.m_faces, file.m_header)
        file.m_textures = Array(readLEInt()) { "" }
        readPMXTextures(file.m_textures, file.m_header)
        file.m_materials = Array(readLEInt()) { PMXMaterial() }
        readPMXMaterials(file.m_materials, file.m_header)
        file.m_bones = Array(readLEInt()) { PMXBone() }
        readPMXBones(file.m_bones, file.m_header)
        file.m_morphs = Array(readLEInt()) { PMXMorph() }
        readPMXMorphs(file.m_morphs, file.m_header)
        file.m_displayFrames = Array(readLEInt()) { PMXDisplayFrame() }
        readPMXDisplayFrames(file.m_displayFrames, file.m_header)
        file.m_rigidbodies = Array(readLEInt()) { PMXRigidBody() }
        readPMXRigidBodies(file.m_rigidbodies, file.m_header)
        file.m_joints = Array(readLEInt()) { PMXJoint() }
        readPMXJoints(file.m_joints, file.m_header)
        if (available() > 0) {
            file.m_softbodies = Array(readLEInt()) { PMXSoftBody() }
            readPMXSoftBodies(file.m_softbodies, file.m_header)
        }

        return file
    }

    private fun readSpecString(l: Int): String {
        return java.lang.String(readNBytes(l), "Shift-jis").toString()
    }

    private fun readVMDHeader(file: VMDFile) {
        val header = readSpecString(30)
        val h = VMDHeader()
        h.m_header = header
        if (header.startsWith("Vocaloid Motion Data file")) h.m_modelName = readSpecString(10)
        else if (header.startsWith("Vocaloid Motion Data 0002")) h.m_modelName = readSpecString(20)
        file.m_header = h
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun readVMDMotion(motions: Array<VMDMotion>) {
        for (i in motions.indices) {
            motions[i].m_boneName = readSpecString(15)
            motions[i].m_frame = readLEInt().toUInt()
            motions[i].m_translate = readVec3()
            motions[i].m_quaternion = readQuat()
            motions[i].m_interpolation = readNBytes(64).toUByteArray()
        }
    }
    private fun readVMDMorphs(morphs: Array<VMDMorph>) {
        for (i in morphs.indices) {
            morphs[i].m_blendShapeName = readSpecString(15)
            morphs[i].m_frame = readLEInt().toUInt()
            morphs[i].m_weight = readLEFloat()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun readVMDCamera(cameras: Array<VMDCamera>) {
        for (i in cameras.indices) {
            cameras[i].m_frame = readLEInt().toUInt()
            cameras[i].m_distance = readLEFloat()
            cameras[i].m_interest = readVec3()
            cameras[i].m_rotate = readVec3()
            cameras[i].m_interpolation = readNBytes(24).toUByteArray()
            cameras[i].m_viewAngle = readLEInt().toUInt()
            cameras[i].m_isPerspective = readByte().toUByte()
        }
    }

    private fun readVMDLights(lights: Array<VMDLight>) {
        for (i in lights.indices) {
            lights[i].m_frame = readLEInt().toUInt()
            lights[i].m_color = readVec3()
            lights[i].m_position = readVec3()
        }
    }

    private fun readVMDShadows(shadows: Array<VMDShadow>) {
        for (i in shadows.indices) {
            shadows[i].m_frame = readLEInt().toUInt()
            shadows[i].m_shadowType = readByte().toUByte()
            shadows[i].m_distance = readFloat()
        }
    }

    private fun readVMDIks(iks: Array<VMDIk>) {
        for (i in iks.indices) {
            iks[i].m_frame = readLEInt().toUInt()
            iks[i].m_show = readByte().toUByte()
            iks[i].m_ikInfos = Array(readLEInt()) { VMDIkInfo() }
            for (j in iks[i].m_ikInfos.indices) {
                iks[i].m_ikInfos[j].m_name = readSpecString(20)
                iks[i].m_ikInfos[j].m_enable = readByte().toUByte()
            }
        }
    }

    fun readVMDFile(): VMDFile {
        val file = VMDFile()
        readVMDHeader(file)
        file.m_motions = Array(readLEInt()) { VMDMotion() }
        readVMDMotion(file.m_motions)
        val func = { available() > 0 }
        if (func()) {
            file.m_morphs = Array(readLEInt()) { VMDMorph() }
            readVMDMorphs(file.m_morphs)
        }
        if (func()) {
            file.m_cameras = Array(readLEInt()) { VMDCamera() }
            readVMDCamera(file.m_cameras)
        }
        if (func()) {
            file.m_lights = Array(readLEInt()) { VMDLight() }
            readVMDLights(file.m_lights)
        }
        if (func()) {
            file.m_shadows = Array(readLEInt()) { VMDShadow() }
            readVMDShadows(file.m_shadows)
        }
        if (func()) {
            file.m_iks = Array(readLEInt()) { VMDIk() }
            readVMDIks(file.m_iks)
        }
        return file
    }
}

private fun Any.fetchInt(i: Int): Int {
    if (this is ByteArray) return this[i].toInt()
    if (this is ShortArray) return this[i].toInt()
    if (this is IntArray) return this[i]
    if (this is LongArray) return this[i].toInt()
    if (this is FloatArray) return this[i].toInt()
    if (this is DoubleArray) return this[i].toInt()
    return 0
}
