package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.github.fhaustt.yggdrasil.component.*
import com.github.fhaustt.yggdrasil.system.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import com.github.quillraven.fleks.*
import ktx.box2d.query
import ktx.math.component1
import ktx.math.component2

@AllOf([AttackComponent:: class, PhysicsComponent::class, ImageComponent::class])
class AttackSystem (
    private val attackCmps: ComponentMapper<AttackComponent>,
    private val physicCmps: ComponentMapper<PhysicsComponent>,
    private val imgCmps: ComponentMapper<ImageComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val lootCmps: ComponentMapper<LootComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    private val phWorld: com.badlogic.gdx.physics.box2d.World
    ): IteratingSystem(){
    override fun onTickEntity(entity: Entity) {
        val attackCmp = attackCmps[entity]

        if(attackCmp.isReady && !attackCmp.doAttack){
            // entity does not want to attack, so do nothing
            return
        }

        if(attackCmp.isPrepared && attackCmp.doAttack){
            // attack intention and ready to attack, start attack
            attackCmp.doAttack = false
            attackCmp.state = AttackState.ATTACKING
            attackCmp.delay = attackCmp.maxDelay
            return
        }

        attackCmp.delay -= deltaTime
        if(attackCmp.delay <= 0f && attackCmp.isAttacking){
            // deal damage to enemy
            attackCmp.state = AttackState.DEAL_DAMAGE

            val image = imgCmps[entity].image
            val physicsCmp = physicCmps[entity]
            val attackLeft = image.flipX
            val (x,y) = physicsCmp.body.position
            val (offX, offY) = physicsCmp.offset
            val (w, h) = physicsCmp.size
            val halfW = w * 0.5f
            val halfH = h * 0.5f

            if(attackLeft){
                AABB_RECT.set(
                    x + offX - halfW - attackCmp.extraRange,
                    y + offY - halfH,
                    x + offX + halfW,
                    y + offY + halfH
                )
            }else {
                AABB_RECT.set(
                    x + offX - halfW,
                    y + offY - halfH,
                    x + offX + halfW + attackCmp.extraRange, // add extra range to top right hand corner
                    y + offY + halfH
                )
            }

            phWorld.query(AABB_RECT.x, AABB_RECT.y, AABB_RECT.width, AABB_RECT.height) // only react when detecting another hitbox (something to attack)
            {fixture -> if(fixture.userData != HIT_BOX_SENSOR){
                    return@query true
                }

                val fixtureEntity = fixture.entity
                if(fixtureEntity == entity){
                    // prevent attacking ourselves
                    return@query true
                }

                configureEntity(fixtureEntity){
                    lifeCmps.getOrNull(it)?.let{ // detect whether the target have a health bar, if yes, store damage dealt in take damage to process later, in case take damage from multiple source, add a ran function as crit
                        lifeCmp -> lifeCmp.takeDamage += attackCmp.damage * MathUtils.random(0.9f, 1.2f)
                    }

                    if(entity in playerCmps) {
                        lootCmps.getOrNull(it)?.let { lootCmp ->
                            lootCmp.interactEntity = entity
                        }
                    }
                }
                return@query true
            }
/*
            val isDone = animationCmps.getOrNull(entity)?.isAnimationDone?: true
            if(isDone){
                attackCmp.state  = AttackState.READY
            }

 */
        }

        val isDone = animationCmps.getOrNull(entity)?.isAnimationDone?: true
        if(isDone){
            attackCmp.state  = AttackState.READY
        }
    }

    companion object{
        val AABB_RECT = Rectangle()
    }


}