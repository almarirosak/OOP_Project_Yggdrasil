package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Entity
import com.github.fhaustt.yggdrasil.Yggdrasil.Companion.UNIT_SCALE
import com.github.fhaustt.yggdrasil.actor.FlipImage
import com.github.fhaustt.yggdrasil.component.*
import com.github.fhaustt.yggdrasil.component.PhysicsComponent.Companion.physicCmpFromImg
import com.github.fhaustt.yggdrasil.event.*
import com.github.fhaustt.yggdrasil.screen.GameScreen
import com.github.fhaustt.yggdrasil.system.PhysicsSystem.*
import ktx.app.gdxError
import ktx.box2d.box
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.layer
import ktx.tiled.type
import ktx.tiled.*
import ktx.tiled.propertyOrNull
import ktx.tiled.x
import ktx.tiled.y
import kotlin.math.roundToInt
import com.github.fhaustt.yggdrasil.event.*
import ktx.actors.stage
import ktx.box2d.circle


@AnyOf([SpawnComponent::class])
class EntitySpawnSystem(
    private val phWorld: World,
    private val spawnCmps:ComponentMapper<SpawnComponent>,
) : EventListener, IteratingSystem(){
    private val cachedCfgs = mutableMapOf<String, SpawnCfg>()

    override fun onTickEntity(entity: Entity) {
        with(spawnCmps[entity]){
            val cfg = spawnCfg(type)

            world.entity {
                val imageCmp = add<ImageComponent>{
                    image = FlipImage().apply {
                        setPosition(location.x, location.y)
                        setSize(1f, 1f)
                        setScaling(Scaling.fill)
                    }
                }
                add<AnimationComponent> {
                    if (type == "Exit"){
                        nextAnimation(cfg.model, AnimationType.WALK)
                    }
                    nextAnimation(cfg.model, AnimationType.WALK)
                }
                val physicCmp =physicCmpFromImg(phWorld, imageCmp.image, cfg.bodyType ){ phCmp, width, height ->
                    val w = width * cfg.physicScaling.x
                    val h = height * cfg.physicScaling.y
                    phCmp.offset.set(cfg.physicOffSet)
                    phCmp.size.set(w,h)

                    // hit box
                    box(w, h, cfg.physicOffSet){
                        isSensor = cfg.bodyType != StaticBody
                        userData = HIT_BOX_SENSOR

                    }

                    // collision box
                    if (cfg.bodyType != StaticBody){
                        val collH = h * 0.3f
                        val callOffSet = vec2().apply { set(cfg.physicOffSet) }
                        callOffSet.y -= h * 0.5f - collH * 0.5f
                       box(w, h * 0.4f, callOffSet)
                    }

                    // attempt at mapChange event
                    /*if (cfg.event != null){
                        val event: Event = cfg.event
                        val stage1: Stage = Stage(ExtendViewport(16f,9f))
                        stage1.fire(event)

                    }*/
                }

                if (cfg.speedScaling > 0f){
                    add<MovementComponent>{
                        speed = DEFAULT_SPEED * cfg.speedScaling
                    }
                }

                if(cfg.canAttack){
                    add<AttackComponent> {
                        maxDelay = cfg.attackDelay
                        damage = (DEFAULT_ATTACK_DAMAGE * cfg.attackScaling).roundToInt()
                        extraRange = cfg.attackExtraRange
                    }
                }

                if (cfg.lifeScaling > 0f){
                    add<LifeComponent> {
                        max = DEFAULT_LIFE * cfg.lifeScaling
                        life = max
                    }
                }

                if (type == "Player"){
                    add<PlayerComponent>()
                    add<StateComponent>()
                }

                if (type == "Slime"){
                    add<StateComponent>()
                }

                if (cfg.lootable){
                    add<LootComponent>()
                }

                if (cfg.bodyType != StaticBody) {
                    add<CollisionComponent>()
                }

                if (cfg.aiTreePath.isNotBlank()){
                    add<AiComponent>{
                        treePath = cfg.aiTreePath
                    }
                    physicCmp.body.circle(4f){
                        isSensor = true
                        userData = AI_SENSOR
                    }
                }
            }
        }
        world.remove(entity)
    }

    private fun spawnCfg(type:String):SpawnCfg = cachedCfgs.getOrPut(type){
        when (type) {
            "Player" -> SpawnCfg(
                AnimationModel.PLAYER,
                attackExtraRange = 0.6f,
                attackScaling = 1.25f,
                physicScaling = vec2(0.8f, 0.8f),
                physicOffSet = vec2(0f,-2f * UNIT_SCALE)
            )
            "Slime" -> SpawnCfg(
                AnimationModel.SLIME,
                lifeScaling = 0.75f,
                physicScaling = vec2(0.8f, 0.8f),
                physicOffSet = vec2(0f,-2f * UNIT_SCALE),
                aiTreePath = "ai/slime.tree",
            )
            "Chest" -> SpawnCfg(
                AnimationModel.CHEST,
                speedScaling = 0f,
                bodyType = StaticBody,
                canAttack = false,
                lifeScaling = 0f,
                lootable = true,

            )

            else -> gdxError("Type $type has no SpawnCfg setup.")
        }
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is MapChangeEvent -> {
                log.debug { "MapChange start" }
                val entityLayer = event.map.layer("entities")
                entityLayer.objects.forEach { mapObj ->
                    val type = mapObj.type ?: gdxError("MapObject $mapObj does not have a type!")
                    world.entity {
                        add<SpawnComponent>{
                            this.type = type
                            this.location.set(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                        }
                    }

                }
                return true
            }
        }
        return false
    }
    companion object{
        const val HIT_BOX_SENSOR = "Hitbox"
        const val AI_SENSOR ="AiSensor"
        private val log = logger<GameScreen>() // output log if game works
    }

}