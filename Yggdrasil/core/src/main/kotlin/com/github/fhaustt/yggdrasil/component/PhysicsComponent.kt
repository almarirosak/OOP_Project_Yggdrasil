package com.github.fhaustt.yggdrasil.component

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.fhaustt.yggdrasil.Yggdrasil.Companion.UNIT_SCALE
import com.github.fhaustt.yggdrasil.system.CollisionSpawnSystem
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateCfg
import ktx.app.gdxError
import ktx.box2d.BodyDefinition
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.loop
import ktx.math.vec2

class PhysicsComponent {
    val prevPos = vec2()
    val impulse = vec2()
    val offset = vec2()
    val size = vec2()
    lateinit var body: Body

    companion object{
        fun EntityCreateCfg.physicCmpFromShape2D(
            world: World,
            x: Int,
            y: Int,
            shape: Shape2D
        ): PhysicsComponent{
            when(shape){
                is Rectangle -> {
                    val bodyX = x + shape.x * UNIT_SCALE
                    val bodyY = y + shape.y * UNIT_SCALE
                    val bodyH = shape.height * UNIT_SCALE
                    val bodyW = shape.width * UNIT_SCALE

                    return add {
                        body = world.body(StaticBody){
                            position.set(bodyX, bodyY)
                            fixedRotation = true
                            allowSleep = false
                            loop(
                                vec2(0f,0f),
                                vec2(bodyW,0f),
                                vec2(bodyW,bodyH),
                                vec2(0f,bodyH)
                            )
                            circle(CollisionSpawnSystem.SPAWN_AREA_SIZE +2f){ isSensor = true }
                        }
                    }

                }
                else -> gdxError("Shape $shape is not supported.")
            }
        }
        fun EntityCreateCfg.physicCmpFromImg(
            world: World,
            image: Image,
            bodyType: BodyType,
            fixtureAction: BodyDefinition.(PhysicsComponent, Float, Float) -> Unit
        ) : PhysicsComponent{
            val x = image.x
            val y = image.y
            val w = image.width
            val h = image.height

            return add {
                body = world.body(bodyType) {
                    position.set(x+w*0.5f, y+h*0.5f)
                    fixedRotation = true
                    allowSleep = false
                    this.fixtureAction(this@add, w, h)

                }
            }
        }

        class PhysicsComponentListener : ComponentListener<PhysicsComponent> {
            override fun onComponentAdded(entity: Entity, component: PhysicsComponent) {
                component.body.userData = entity
            }

            override fun onComponentRemoved(entity: Entity, component: PhysicsComponent) {
                val body = component.body
                body.world.destroyBody(body)
                body.userData = null
            }

            

        }
    }
}