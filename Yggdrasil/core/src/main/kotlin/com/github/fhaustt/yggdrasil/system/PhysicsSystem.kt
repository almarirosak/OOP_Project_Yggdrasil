package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.github.fhaustt.yggdrasil.component.*
import com.github.fhaustt.yggdrasil.system.CollisionSpawnSystem
import com.github.fhaustt.yggdrasil.system.EntitySpawnSystem.Companion.AI_SENSOR
import com.github.quillraven.fleks.*
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

val Fixture.entity: Entity
    get() = this.body.userData as Entity

@AllOf([PhysicsComponent::class, ImageComponent::class])
class PhysicsSystem(
    private val phWorld: World,
    private val imageCmps : ComponentMapper<ImageComponent>,
    private val physicsCmp : ComponentMapper<PhysicsComponent>,
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val collisionCmps: ComponentMapper<CollisionComponent>,
    private val aiCmps: ComponentMapper<AiComponent>,
) : ContactListener, IteratingSystem(interval = Fixed(1 / 60f)) {

    init {
        phWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if(phWorld.autoClearForces){
            log.error { "AutoClearForces must be set to false." }
            phWorld.autoClearForces = false
        }
        super.onUpdate()
        phWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        phWorld.step(deltaTime, 6, 2)
    }

    override fun onTickEntity(entity: Entity) {
        val physicsCmp = physicsCmp[entity]

        physicsCmp.prevPos.set(physicsCmp.body.position)

        if (!physicsCmp.impulse.isZero){
            physicsCmp.body.applyLinearImpulse(physicsCmp.impulse, physicsCmp.body.worldCenter, true)
            physicsCmp.impulse.setZero()
        }


    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val physicsCmp = physicsCmp[entity]
        val imageCmps = imageCmps[entity]

        val (prevX, prevY) = physicsCmp.prevPos
        val (bodyX, bodyY) = physicsCmp.body.position
        imageCmps.image.run {
            setPosition(
                MathUtils.lerp(prevX, bodyX, alpha) - width * 0.5f,
                MathUtils.lerp(prevY, bodyY, alpha) - height * 0.5f
            )
        }
    }

    override fun beginContact(contact: Contact) {
        val entityA: Entity = contact.fixtureA.entity
        val entityB: Entity = contact.fixtureB.entity
        val isEntityATiledCollisionSensor = entityA in tiledCmps && contact.fixtureA.isSensor
        val isEntityBCollisionFixture = entityB in collisionCmps && !contact.fixtureB.isSensor
        val isEntityACollisionFixture = entityA in collisionCmps && !contact.fixtureA.isSensor
        val isEntityBTiledCollisionSensor = entityB in tiledCmps && contact.fixtureB.isSensor

        when{
            isEntityATiledCollisionSensor && isEntityBCollisionFixture -> {
                tiledCmps[entityA].nearbyEntities += entityB
            }
            isEntityBTiledCollisionSensor && isEntityACollisionFixture -> {
                tiledCmps[entityB].nearbyEntities += entityA
            }
        }

        val isEntityAAiSensor=entityA in aiCmps && contact.fixtureA.isSensor && contact.fixtureA.userData==AI_SENSOR
        val isEntityBAiSensor=entityB in aiCmps && contact.fixtureB.isSensor && contact.fixtureB.userData==AI_SENSOR

        when{
            isEntityAAiSensor && isEntityBCollisionFixture -> {
                aiCmps[entityA].nearbyEntities+=entityB
            }
            isEntityBAiSensor && isEntityACollisionFixture -> {
                aiCmps[entityB].nearbyEntities+=entityA
            }
        }
    }



    override fun endContact(contact: Contact) {
        val entityA: Entity = contact.fixtureA.entity
        val entityB: Entity = contact.fixtureB.entity
        val isEntityATiledCollisionSensor = entityA in tiledCmps && contact.fixtureA.isSensor
        val isEntityBTiledCollisionSensor = entityB in tiledCmps && contact.fixtureB.isSensor

        when{
            isEntityATiledCollisionSensor && !contact.fixtureB.isSensor -> {
                tiledCmps[entityA].nearbyEntities -= entityB
            }
            isEntityBTiledCollisionSensor && !contact.fixtureA.isSensor -> {
                tiledCmps[entityB].nearbyEntities -= entityA
            }
        }

        val isEntityAAiSensor=entityA in aiCmps && contact.fixtureA.isSensor && contact.fixtureA.userData==AI_SENSOR
        val isEntityBAiSensor=entityB in aiCmps && contact.fixtureB.isSensor && contact.fixtureB.userData==AI_SENSOR

        when{
            isEntityAAiSensor && !contact.fixtureB.isSensor -> {
                aiCmps[entityA].nearbyEntities-=entityB
            }
            isEntityBAiSensor && !contact.fixtureA.isSensor -> {
                aiCmps[entityB].nearbyEntities-=entityA
            }
        }

    }

    private fun Fixture.isStaticBody() = this.body.type == StaticBody
    private fun Fixture.isDynamicBody() = this.body.type == DynamicBody


    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        contact.isEnabled = (contact.fixtureA.isStaticBody() && contact.fixtureB.isDynamicBody()) || (contact.fixtureB.isStaticBody() && contact.fixtureA.isDynamicBody())
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit

    companion object{
        private val log = logger<PhysicsSystem>()

    }


}