package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.fhaustt.yggdrasil.component.FloatingTextComponent
import com.github.quillraven.fleks.*
import ktx.math.vec2

@AllOf([FloatingTextComponent::class]) // iterate over all entity with floating text component
class FloatingTextSystem(
    private val gameStage: Stage,
    @Qualifier("uiStage") private val uiStage: Stage,
    private val textCmps: ComponentMapper<FloatingTextComponent>
) :IteratingSystem() {
    private val uiLocation = vec2() // both use to temporary store information
    private val uiTarget = vec2()

    private fun Vector2.toUiCoordinates(from: Vector2){
        this.set(from)
        gameStage.viewport.project(this)
        uiStage.viewport.unproject(this)
    }

    override fun onTickEntity(entity: Entity) {
        with(textCmps[entity]){// get floating text component of entity
            if (time >= lifeSpan){
                world.remove(entity)
                return
            }

            time += deltaTime
            uiLocation.toUiCoordinates(txtLocation)
            uiTarget.toUiCoordinates(txtTarget)
            uiLocation.interpolate(uiTarget, (time/lifeSpan).coerceAtMost(1f), Interpolation.smooth2) // change how the damage text fade(pattern)
            label.setPosition(uiLocation.x, uiStage.viewport.worldHeight - uiLocation.y) // because of how y is past, we need to format it
        }
    }
}