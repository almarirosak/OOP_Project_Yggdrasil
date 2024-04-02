package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.fhaustt.yggdrasil.component.PhysicsComponent
import com.github.fhaustt.yggdrasil.component.PhysicsComponent.Companion.physicCmpFromShape2D
import com.github.fhaustt.yggdrasil.component.TiledComponent
import com.github.fhaustt.yggdrasil.event.CollissionDespawnEvent
import com.github.fhaustt.yggdrasil.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.body
import ktx.box2d.loop
import ktx.collections.GdxArray
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.*
import kotlin.math.max

@AllOf([PhysicsComponent::class])
class CollisionSpawnSystem(
    private val phWorld: com.badlogic.gdx.physics.box2d.World,
    private val physicsCmps: ComponentMapper<PhysicsComponent>

) : EventListener, IteratingSystem() {

    private val tiledLayers = GdxArray<TiledMapTileLayer>()
    private val processedCells = mutableSetOf<TiledMapTileLayer.Cell>()

    private fun TiledMapTileLayer.forEachCell(
        startX:Int,
        startY:Int,
        size: Int,
        action: (TiledMapTileLayer.Cell, Int, Int) -> Unit
    ){
        for (x in startX - size .. startX + size){
            for (y in startY - size .. startY + size){
                this.getCell(x,y)?.let { action(it, x, y) }
            }
        }
    }



    override fun onTickEntity(entity: Entity) {
        val(entityX, entityY) = physicsCmps[entity].body.position

        tiledLayers.forEach() { layer ->
            layer.forEachCell(entityX.toInt(), entityY.toInt(), SPAWN_AREA_SIZE){ cell, x, y ->
                if(cell.tile.objects.isEmpty()){
                    // no collision
                    return@forEachCell
                }

                if (cell in processedCells){
                    return@forEachCell
                }

                processedCells.add(cell)
                cell.tile.objects.forEach { mapObject ->
                    world.entity {

                        physicCmpFromShape2D(phWorld, x, y, mapObject.shape)
                        add<TiledComponent> {
                            this.cell = cell
                            nearbyEntities.add(entity)

                        }
                    }



                }
            }
        }
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is MapChangeEvent ->{
                event.map.layers.getByType(TiledMapTileLayer::class.java, tiledLayers)

                // world/map boundary
                world.entity {
                    val w = event.map.width.toFloat()
                    val h = event.map.height.toFloat()

                    add<PhysicsComponent> {
                        body = phWorld.body(StaticBody) {
                            position.set(0f, 0f)
                            fixedRotation = true
                            allowSleep = false
                            loop(
                                vec2(0f,0f),
                                vec2(w,0f),
                                vec2(w,h),
                                vec2(0f,h),
                            )
                        }
                    }
                }

                return true
            }
            is CollissionDespawnEvent -> {
                processedCells.remove(event.cell)
                return true
            }
            else -> return false
        }
    }

    companion object{
        const val SPAWN_AREA_SIZE = 3
    }
}