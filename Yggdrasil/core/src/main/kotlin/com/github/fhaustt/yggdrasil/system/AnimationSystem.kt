package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.fhaustt.yggdrasil.component.AnimationComponent
import com.github.fhaustt.yggdrasil.component.AnimationComponent.Companion.NO_ANIMATION
import com.github.fhaustt.yggdrasil.component.ImageComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.app.gdxError
import ktx.collections.map
import ktx.log.logger

@AllOf([AnimationComponent::class, ImageComponent::class])
class AnimationSystem(
    private val textureAtlas: TextureAtlas, // Maps all images
    private val animationCmps: ComponentMapper<AnimationComponent>, // Maps all animation cycles
    private val imageCmps: ComponentMapper<ImageComponent>, // Maps all image components
): IteratingSystem(){

    private val cachedAnimations = mutableMapOf<String, Animation<TextureRegionDrawable>>()


    override fun onTickEntity(entity: Entity) {
        val aniCmp = animationCmps[entity] // Animation Component Cycler

        if (aniCmp.nextAnimation == NO_ANIMATION){
            aniCmp.stateTime += deltaTime
        } else {
            aniCmp.animation = animation(aniCmp.nextAnimation)
            aniCmp.stateTime = 0f
            aniCmp.nextAnimation = NO_ANIMATION
        }

        aniCmp.animation.playMode = aniCmp.playMode
        imageCmps[entity].image.drawable = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }


    private fun animation(aniKeyPath:String):Animation<TextureRegionDrawable>{
        return cachedAnimations.getOrPut(aniKeyPath){
            log.debug { "New animation is created for  $aniKeyPath" }
            val regions = textureAtlas.findRegions(aniKeyPath)
            if (regions.isEmpty) {
                gdxError("There are no texture regions for $aniKeyPath")
            }
            Animation(DEFAULT_FRAME_DURATION, regions.map { TextureRegionDrawable(it)})
        }
    }

    companion object{ // Logger and frame timings
        private val log = logger<AnimationSystem>()
        private const val DEFAULT_FRAME_DURATION= 1/6f
    }
}