package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.fhaustt.yggdrasil.component.TiledComponent
import com.github.fhaustt.yggdrasil.event.CollissionDespawnEvent
import com.github.fhaustt.yggdrasil.event.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([TiledComponent::class])
class CollisionDespawnSystem(
    private val tiledCmps:ComponentMapper<TiledComponent>,
    private val stage:Stage,

) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(tiledCmps[entity]){
            if (nearbyEntities.isEmpty()){
                stage.fire(CollissionDespawnEvent(cell))
                world.remove(entity)
            }
        }
    }
}