package com.primogemstudio.mmdbase

import com.bulletphysics.collision.broadphase.DbvtBroadphase
import com.bulletphysics.collision.dispatch.CollisionDispatcher
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration
import com.bulletphysics.collision.shapes.BoxShape
import com.bulletphysics.dynamics.DiscreteDynamicsWorld
import com.bulletphysics.dynamics.RigidBody
import com.bulletphysics.dynamics.RigidBodyConstructionInfo
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver
import com.bulletphysics.linearmath.DefaultMotionState
import com.bulletphysics.linearmath.Transform
import javax.vecmath.Quat4f
import javax.vecmath.Vector3f

fun main(args: Array<String>) {
    val collisionConfiguration = DefaultCollisionConfiguration()
    val dispatcher = CollisionDispatcher(collisionConfiguration)

    val operlappingPairCache = DbvtBroadphase()

    val solver = SequentialImpulseConstraintSolver()

    val dynamicsWorld = DiscreteDynamicsWorld(dispatcher, operlappingPairCache, solver, collisionConfiguration)
    dynamicsWorld.setGravity(Vector3f(0f, -98f, 0f))

    val groundShape = BoxShape(Vector3f(50f, 50f, 50f))
    val groundTransform = Transform()
    groundTransform.setIdentity()
    groundTransform.origin.set(0f, -50f, 0f)
    val motionState = DefaultMotionState(groundTransform)
    val body = RigidBody(RigidBodyConstructionInfo(1f, motionState, groundShape, Vector3f(0f, 0f, 0f)))

    dynamicsWorld.addRigidBody(body)

    for (i in 0 ..< 1000) {
        dynamicsWorld.stepSimulation(1f / 60f, 10)
        println("World transform: ${body.motionState.getWorldTransform(Transform()).origin}")
    }
}