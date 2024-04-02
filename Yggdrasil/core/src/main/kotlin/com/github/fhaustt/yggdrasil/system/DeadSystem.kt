package com.github.fhaustt.yggdrasil.system

import com.github.fhaustt.yggdrasil.component.DeadComponent
import com.github.fhaustt.yggdrasil.component.LifeComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DeadComponent::class])
class DeadSystem (
    // take in life component to know wheter is time to revive (revive time = 0)
    // take in dead to know who died
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
) :IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val deadCmp = deadCmps[entity]
        // if entity no revive time (npc), remove them after dead
        if(deadCmp.reviveTime == 0f){
            world.remove(entity)
            return
    }
        deadCmp.reviveTime -= deltaTime
        // if revive time smaller than 0, means (player), since only player supplied with revive time
        // revive player (health = max), and remove them from death
        if(deadCmp.reviveTime <= 0f){
            with(lifeCmps[entity]){life = max}
            configureEntity(entity){deadCmps.remove(entity)}

        }
}
}