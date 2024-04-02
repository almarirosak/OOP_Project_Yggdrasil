package com.github.fhaustt.yggdrasil.system

import com.github.fhaustt.yggdrasil.component.ImageComponent
import com.github.fhaustt.yggdrasil.component.MovementComponent
import com.github.fhaustt.yggdrasil.component.PhysicsComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.component1
import ktx.math.component2

@AllOf([MovementComponent::class, PhysicsComponent::class])
class MoveSystem(
    private val moveCmps: ComponentMapper<MovementComponent>,
    private val physicsCmps: ComponentMapper<PhysicsComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,

    ): IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val moveCmp = moveCmps[entity]
        val physicsCmp = physicsCmps[entity]
        val mass = physicsCmp.body.mass
        val (velX, velY) = physicsCmp.body.linearVelocity

        if((moveCmp.cos == 0f && moveCmp.sin == 0f) || moveCmp.root){
            // no direction or rooted so stop immediately
            physicsCmp.impulse.set(
                mass * (0f - velX),
                mass * (0f - velY),
            )
            return
        }

        physicsCmp.impulse.set(
            mass * (moveCmp.speed * moveCmp.cos - velX),
            mass * (moveCmp.speed * moveCmp.sin - velY)
        )

        imageCmps.getOrNull(entity)?.let { imageCmp ->
            if (moveCmp.cos != 0f){
                imageCmp.image.flipX = moveCmp.cos < 0
            }
        }
    }
}