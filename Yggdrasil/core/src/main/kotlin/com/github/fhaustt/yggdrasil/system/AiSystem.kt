package com.github.fhaustt.yggdrasil.system

import com.github.fhaustt.yggdrasil.component.AiComponent
import com.github.fhaustt.yggdrasil.component.DeadComponent
import com.github.quillraven.fleks.*


@AllOf([AiComponent::class])
@NoneOf([DeadComponent::class])
class AiSystem (
    private val aiCmps: ComponentMapper<AiComponent>
): IteratingSystem() {


    override fun onTickEntity(entity: Entity) {
        with(aiCmps[entity]){
            behaviorTree.step()
        }
    }
}