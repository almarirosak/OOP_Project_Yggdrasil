package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.fhaustt.yggdrasil.Yggdrasil.Companion.UNIT_SCALE
import com.github.fhaustt.yggdrasil.component.ImageComponent
import com.github.fhaustt.yggdrasil.event.MapChangeEvent
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.compareEntity
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.tiled.forEachLayer


// Interval System for day/night cycle to add later

//@AllOf() = Entity must have all the components specified
//@NoneOf() = Entity must have none of the components specified
//@AnyOf() = Entity must have none of the components specified


// System that renders the game
@AllOf([ImageComponent::class])
class RenderSystem(
    private val gameStage:Stage, // World Stage literally
    @Qualifier("uiStage") val uiStage: Stage, // ui stage
    private val imageCmps: ComponentMapper<ImageComponent> // Taking care of components using mappers from FLEKS also doesnt need to be defined into a dependency
):  EventListener, IteratingSystem(
    comparator = compareEntity { e1, e2 ->  imageCmps[e1].compareTo(imageCmps[e2])} // Compare components quickly for entities
){

    private val bgLayers = mutableListOf<TiledMapTileLayer>() //Sorting into foreground layer
    private val fgLayers = mutableListOf<TiledMapTileLayer>() // Sorting into background layer
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, gameStage.batch)
    private val orthoCam = gameStage.camera as OrthographicCamera

    override fun onTick() {
        super.onTick()

        with(gameStage){ // Rendering the stage
            viewport.apply()

            AnimatedTiledMapTile.updateAnimationBaseTime()
            mapRenderer.setView(orthoCam)


            if (bgLayers.isNotEmpty()){
                gameStage.batch.use(orthoCam.combined){
                    bgLayers.forEach { mapRenderer.renderTileLayer(it)}
                }
            }

            act(deltaTime)
            draw()

             if (fgLayers.isNotEmpty()){
                 gameStage.batch.use(orthoCam.combined){
                     fgLayers.forEach{ mapRenderer.renderTileLayer(it)}
                 }
             }
        }


        // render UI
        with(uiStage){
            viewport.apply() // different viewport from main(gameStage), so apply again
            act(deltaTime)
            draw() // draw the stage
        }
    }

    override fun onTickEntity(entity: Entity) { //Scene2D maps components in order they are rendered
        imageCmps[entity].image.toFront() // Puts entities at front of rendering
    }

    override fun handle(event: Event?): Boolean {
        when (event){
            is MapChangeEvent -> {
                bgLayers.clear()
                fgLayers.clear()

                event.map.forEachLayer<TiledMapTileLayer> {layer ->
                    if (layer.name.startsWith("fg_")){
                        fgLayers.add(layer)
                    } else {
                        bgLayers.add(layer)
                    }
                }


                return true
            }

        }
        return true
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
    }
}