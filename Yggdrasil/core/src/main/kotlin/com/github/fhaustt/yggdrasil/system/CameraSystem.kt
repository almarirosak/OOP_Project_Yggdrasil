package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.fhaustt.yggdrasil.component.ImageComponent
import com.github.fhaustt.yggdrasil.component.PlayerComponent
import com.github.fhaustt.yggdrasil.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.tiled.height
import ktx.tiled.width

@AllOf([PlayerComponent::class, ImageComponent::class])
class CameraSystem(
    private val imageCmps: ComponentMapper<ImageComponent>,
    stage: Stage,
): EventListener, IteratingSystem() {

    private var maxW = 0f
    private var maxH = 0f
    private val camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        val viewW = camera.viewportWidth * 0.416f
        val viewH = camera.viewportHeight * 0.416f
        with(imageCmps[entity]){
            camera.position.set(
                image.x.coerceIn(viewW, maxW - viewW),
                image.y.coerceIn(viewH, maxH - viewH),
                camera.position.z
            )
        }

    }

    override fun handle(event: Event): Boolean {
        if (event is MapChangeEvent){
            maxW = event.map.width.toFloat()
            maxH = event.map.height.toFloat()
            return true
        }
        return false
    }
}