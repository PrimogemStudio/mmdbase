package com.primogemstudio.mmdbase.model

import com.primogemstudio.mmdbase.abstraction.ITextureManager
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class PMXBone {
    var m_name = ""
    var m_englishName = ""
    var m_position = Vec3()
    var m_parentBoneIndex = 0
    var m_deformDepth = 0
    var m_boneFlag = 0
    var m_positionOffset = Vec3()
    var m_linkBoneIndex = 0
    var m_appendBoneIndex = 0
    var m_appendWeight = 0f
    var m_fixedAxis = Vec3()
    var m_localXAxis = Vec3()
    var m_localZAxis = Vec3()
    var m_keyValue = 0
    var m_ikTargetBoneIndex = 0
    var m_ikIterationCount = 0
    var m_ikLimit = 0f
    var m_ikLinks = emptyArray<PMXIKLink>()

    class PMXIKLink {
        var m_ikBoneIndex = 0
        var m_enableLimit: Byte = 0

        var m_limitMin = Vec3()
        var m_limitMax = Vec3()
    }
}

enum class PMXBoneFlags(val flag: Int) {
    TargetShowMode(0x0001),
    AllowRotate(0x0002),
    AllowTranslate(0x0004),
    Visible(0x0008),
    AllowControl(0x0010),
    IK(0x0020),
    AppendLocal(0x0080),
    AppendRotate(0x0100),
    AppendTranslate(0x0200),
    FixedAxis(0x0400),
    LocalAxis(0x800),
    DeformAfterPhysics(0x1000),
    DeformOuterParent(0x2000);
}

class PMXDisplayFrame {
    var m_name = ""
    var m_englishName = ""
    var m_flag = FrameType.DefaultFrame
    var m_targets = emptyArray<Target>()

    enum class FrameType {
        DefaultFrame,
        SpecialFrame,
    }

    class Target {
        var m_type = TargetType.BoneIndex
        var m_index = 0
        enum class TargetType {
            BoneIndex,
            MorphIndex,
        }
    }
}

class PMXFace {
    var m_vertices = IntArray(3)
}

class PMXJoint {
    var m_name = ""
    var m_englishName = ""
    var m_type = JointType.SpringDOF6
    var m_rigidbodyAIndex = 0
    var m_rigidbodyBIndex = 0
    var m_translate = Vec3()
    var m_rotate = Vec3()
    var m_translateLowerLimit = Vec3()
    var m_translateUpperLimit = Vec3()
    var m_rotateLowerLimit = Vec3()
    var m_rotateUpperLimit = Vec3()
    var m_springTranslateFactor = Vec3()
    var m_springRotateFactor = Vec3()

    enum class JointType {
        SpringDOF6,
        DOF6,
        P2P,
        ConeTwist,
        Slider,
        Hinge,
    }
}

class PMXMaterial {
    var m_name = ""
    var m_englishName = ""
    var m_diffuse = Vec4()
    var m_specular = Vec3()
    var m_specularPower = 0f
    var m_ambient = Vec3()
    var m_drawMode = PMXDrawModeFlags.BothFace
    var m_edgeColor = Vec4()
    var m_edgeSize = 0f
    var m_textureIndex = 0
    var m_sphereTextureIndex = 0
    var m_sphereMode = PMXSphereMode.None
    var m_toonMode = PMXToonMode.Common
    var m_toonTextureIndex = 0
    var m_memo = ""
    var m_numFaceVertices = 0

    enum class PMXToonMode {
        Separate,
        Common,
    }

    enum class PMXSphereMode {
        None,
        Mul,
        Add,
        SubTexture,
    }

    enum class PMXDrawModeFlags {
        BothFace,
        GroundShadow,
        CastSelfShadow,
        RecieveSelfShadow,
        DrawEdge,
        VertexColor,
        DrawPoint,
        DrawLine;
        companion object {
            @JvmStatic
            fun findMode(a: Byte): PMXDrawModeFlags {
                return when (a.toInt()) {
                    0x01 -> BothFace
                    0x02 -> GroundShadow
                    0x04 -> CastSelfShadow
                    0x08 -> RecieveSelfShadow
                    0x10 -> DrawEdge
                    0x20 -> VertexColor
                    0x40 -> DrawPoint
                    0x80 -> DrawLine
                    else -> BothFace
                }
            }
        }
    }
}

class PMXMorph {
    var m_name = ""
    var m_englishName = ""
    var m_controlPanel: Byte = 0
    var m_morphType = PMXMorphType.Group
    var m_positionMorph = emptyArray<PositionMorph>()
    var m_uvMorph = emptyArray<UVMorph>()
    var m_boneMorph = emptyArray<BoneMorph>()
    var m_materialMorph = emptyArray<MaterialMorph>()
    var m_groupMorph = emptyArray<GroupMorph>()
    var m_flipMorph = emptyArray<FlipMorph>()
    var m_impulseMorph = emptyArray<ImpulseMorph>()

    enum class PMXMorphType {
        Group,
        Position,
        Bone,
        UV,
        AddUV1,
        AddUV2,
        AddUV3,
        AddUV4,
        Material,
        Flip,
        Impluse
    }

    class PositionMorph {
        var m_vertexIndex = 0
        var m_position = Vec3()
    }

    class UVMorph {
        var m_vertexIndex = 0
        var m_uv = Vec4()
    }

    class BoneMorph {
        var m_boneIndex = 0
        var m_position = Vec3()
        var m_quaternion = Quat()
    }

    class MaterialMorph {
        enum class OpType {
            Mul,
            Add,
        }

        var m_materialIndex = 0
        var m_opType = OpType.Mul
        var m_diffuse = Vec4()
        var m_specular = Vec3()
        var m_specularPower = 0f
        var m_ambient = Vec3()
        var m_edgeColor = Vec4()
        var m_edgeSize = 0f
        var m_textureFactor = Vec4()
        var m_sphereTextureFactor = Vec4()
        var m_toonTextureFactor = Vec4()
    }

    class GroupMorph {
        var	m_morphIndex = 0
        var m_weight = 0f
    }

    class FlipMorph {
        var	m_morphIndex = 0
        var m_weight = 0f
    }

    class ImpulseMorph {
        var m_rigidbodyIndex = 0
        var m_localFlag: Byte = 0	//0:OFF 1:ON
        var m_translateVelocity = Vec3()
        var m_rotateTorque = Vec3()
    }
}

class PMXRigidBody {
    var m_name = ""
    var m_englishName = ""
    var m_boneIndex = 0
    var m_group: Byte = 0
    var m_collisionGroup: Short = 0
    var m_shape = Shape.Sphere
    var m_shapeSize = Vec3()
    var m_translate = Vec3()
    var m_rotate = Vec3()
    var m_mass = 0f
    var m_translateDimmer = 0f
    var m_rotateDimmer = 0f
    var m_repulsion = 0f
    var m_friction = 0f
    var m_op = Operation.Static

    enum class Operation {
        Static,
        Dynamic,
        DynamicAndBoneMerge
    }

    enum class Shape {
        Sphere,
        Box,
        Capsule,
    }
}

class PMXSoftBody {
    var m_name = ""
    var m_englishName = ""
    var m_type = SoftbodyType.TriMesh
    var m_materialIndex = 0
    var m_group: Byte = 0
    var m_collisionGroup: Short = 0
    var m_flag = SoftbodyMask.BLink
    var m_BLinkLength = 0
    var m_numClusters = 0
    var m_totalMass = 0f
    var m_collisionMargin = 0F
    var m_aeroModel = AeroModel.kAeroModelF_OneSided
    var m_VCF = 0f
    var m_DP = 0f
    var m_DG = 0f
    var m_LF = 0f
    var m_PR = 0f
    var m_VC = 0f
    var m_DF = 0f
    var m_MT = 0f
    var m_CHR = 0f
    var m_KHR = 0f
    var m_SHR = 0f
    var m_AHR = 0f

    var m_SRHR_CL = 0f
    var m_SKHR_CL = 0f
    var m_SSHR_CL = 0f
    var m_SR_SPLT_CL = 0f
    var m_SK_SPLT_CL = 0f
    var m_SS_SPLT_CL = 0f

    var m_V_IT = 0
    var m_P_IT = 0
    var m_D_IT = 0
    var m_C_IT = 0

    var m_LST = 0f
    var m_AST = 0f
    var m_VST = 0f
    var m_anchorRigidBodies = emptyArray<AnchorRigidbody>()
    var m_pinVertexIndices = emptyArray<Int>()

    enum class SoftbodyType {
        TriMesh,
        Rope,
    }

    enum class SoftbodyMask {
        BLink,
        Cluster,
        HybridLink;
        companion object {
            @JvmStatic
            fun getMask(b: Byte): SoftbodyMask {
                return when (b.toInt()) {
                    0x01 -> BLink
                    0x02 -> Cluster
                    0x04 -> HybridLink
                    else -> BLink
                }
            }
        }
    }

    enum class AeroModel {
        kAeroModelV_TwoSided,
        kAeroModelV_OneSided,
        kAeroModelF_TwoSided,
        kAeroModelF_OneSided,
    }

    class AnchorRigidbody {
        var m_rigidBodyIndex = 0
        var m_vertexIndex = 0
        var	m_nearMode: Byte = 0 // 0:OFF 1:ON
    }
}

class PMXVertex {
    var m_position = Vec3()
    var m_normal = Vec3()
    var m_uv = Vec2()
    val m_addUV = arrayOf(Vec4(), Vec4(), Vec4(), Vec4())
    var m_weightType = PMXVertexWeight.BDEF1
    val m_boneIndices = arrayOf(0, 0, 0, 0)
    var m_boneWeights = Vec4()
    var m_sdefC = Vec3()
    var m_sdefR0 = Vec3()
    var m_sdefR1 = Vec3()
    var m_edgeMag = 0f

    enum class PMXVertexWeight {
        BDEF1, BDEF2, BDEF4, SDEF, QDEF,
    }
}

class PMXHeader {
    var m_magic = ""
    var m_version = 0f
    var m_dataSize: Byte = 0
    var m_encode: Byte = 0
    var m_addUVNum: Byte = 0
    var m_vertexIndexSize: Byte = 0
    var m_textureIndexSize: Byte = 0
    var m_materialIndexSize: Byte = 0
    var m_boneIndexSize: Byte = 0
    var m_morphIndexSize: Byte = 0
    var m_rigidbodyIndexSize: Byte = 0
}

class PMXInfo {
    var m_modelName = ""
    var m_englishModelName = ""
    var m_comment = ""
    var m_englishComment = ""
}

class PMXFile {
    var m_header = PMXHeader()
    var m_info = PMXInfo()
    var m_vertices = emptyArray<PMXVertex>()
    var m_faces = emptyArray<PMXFace>()
    var m_textures = emptyArray<String>()
    var m_materials = emptyArray<PMXMaterial>()
    var m_bones = emptyArray<PMXBone>()
    var m_morphs = emptyArray<PMXMorph>()
    var m_displayFrames = emptyArray<PMXDisplayFrame>()
    var m_rigidbodies = emptyArray<PMXRigidBody>()
    var m_joints = emptyArray<PMXJoint>()
    var m_softbodies = emptyArray<PMXSoftBody>()
    var textureManager: ITextureManager? = null
}