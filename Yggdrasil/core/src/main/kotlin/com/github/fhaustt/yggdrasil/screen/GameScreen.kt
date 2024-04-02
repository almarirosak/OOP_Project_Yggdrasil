package com.github.fhaustt.yggdrasil.screen

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.fhaustt.yggdrasil.component.*
import com.github.fhaustt.yggdrasil.component.ImageComponent.Companion.ImageComponentListener
import com.github.fhaustt.yggdrasil.component.PhysicsComponent.Companion.PhysicsComponentListener
import com.github.fhaustt.yggdrasil.component.FloatingTextComponent.Companion.FloatingTextComponentListener
import com.github.fhaustt.yggdrasil.event.MapChangeEvent
import com.github.fhaustt.yggdrasil.event.fire
import com.github.fhaustt.yggdrasil.input.PlayerKeyboardInputProcessor
import com.github.fhaustt.yggdrasil.system.*
import com.github.quillraven.fleks.World
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.math.vec2
import com.badlogic.gdx.physics.box2d.ContactListener as Box2dContactListener

class GameScreen : KtxScreen{
    private val gameStage : Stage = Stage(ExtendViewport(16f,9f)) // Declaring our game screen
    private val uiStage : Stage = Stage(ExtendViewport(1280f,720f)) // Declare to output floating text (damage value etc)
    private val textureAtlas = TextureAtlas("graphics/gameObject.atlas") // Optimised texture rendering for animation handling
    private var currentMap : TiledMap? = null // Declaring current map variable
    private val phWorld = createWorld(gravity = vec2()).apply {
        autoClearForces = false
    } // Declaring game physics variable

    private val eWorld:World = World{ // Creating a game world
        inject(gameStage) // Injecting a stage where we can put actors/entities into the game
        inject("uiStage", uiStage) // Supply a string value to the inject function to ensure the above stage is called as default, and call this stage only when needed
        inject(textureAtlas)
        inject(phWorld)

        componentListener<ImageComponentListener>() // Introduced an Image component listener
        componentListener<PhysicsComponentListener>()
        componentListener<FloatingTextComponentListener>()
        componentListener<StateComponent.Companion.StateComponentListener>()
        componentListener<AiComponent.Companion.AiComponentListener>()

        // Declaring what systems we are using in the game
        system<EntitySpawnSystem>()
        system<CollisionSpawnSystem>()
        system<MoveSystem>()
        system<AttackSystem>()
        system<LootSystem>()
        system<DeadSystem>()
        system<LifeSystem>()
        system<PhysicsSystem>()
        system<StateSystem>()
        system<AiSystem>()
        system<AnimationSystem>() // Update drawable of actor images first
        system<CameraSystem>()
        system<FloatingTextSystem>()
        system<RenderSystem>()
        system<DebugSystem>()

    }

    override fun show() {
        log.debug { "GameScreen is shown" }

        eWorld.systems.forEach{ system ->
            if (system is EventListener){
                gameStage.addListener(system)
            }
        }
        currentMap = TmxMapLoader().load("graphics/world.tmx") //  Creating a var to hold current map "graphics/home.tmx"
        gameStage.fire(MapChangeEvent(currentMap!!)) // Creating a map change event on the world stage

        PlayerKeyboardInputProcessor(eWorld)
    }
    override fun render(delta: Float) {
        eWorld.update(delta.coerceAtMost(0.25f)) // Updating the world based on delta time (set by default)
    }

    override fun resize(width: Int, height: Int) {
        gameStage.viewport.update(width,height, true)
        uiStage.viewport.update(width,height, true)
    }

    override fun dispose() {
        gameStage.disposeSafely() // Removing the world stage
        uiStage.disposeSafely() // Remove ui floating text
        textureAtlas.disposeSafely() //Remove texture atlas
        eWorld.dispose() // Removing all systems implemented by the world (entities and world textures)
        currentMap?.disposeSafely()
        phWorld.disposeSafely()
    }

    fun getStage(): Stage {
        return gameStage
    }

    companion object{
        private val log = logger<GameScreen>() // output log if game works
    }



}

