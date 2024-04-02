package com.github.fhaustt.yggdrasil.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

enum class AnimationModel{
    PLAYER, SLIME, CHEST, EXIT, UNDEFINED; // Actors defined for current use

    val atlasKey: String = this.toString().lowercase()
}

enum class AnimationType{ // Animation types when doing a certain action
    IDLE, WALK, ATTACK, DEATH, OPEN;

    val atlasKey:String = this.toString().lowercase()
}

data class AnimationComponent(
    var model: AnimationModel = AnimationModel.UNDEFINED,
    var atlasKey:String = "",
    var stateTime: Float = 0f,
    var playMode: Animation.PlayMode = Animation.PlayMode.LOOP
) {
   lateinit var  animation: Animation<TextureRegionDrawable>
   var nextAnimation:String=""


    // Example of animation naming = player/idle.00

    val isAnimationDone : Boolean
        get() = animation.isAnimationFinished(stateTime)

   fun nextAnimation(model : AnimationModel, type: AnimationType){
       this.model = model
       nextAnimation ="${model.atlasKey}/${type.atlasKey}"
   }

    fun nextAnimation(type: AnimationType){
        nextAnimation ="${model.atlasKey}/${type.atlasKey}"
    }

    companion object{
        val NO_ANIMATION ="" // Default animation if none is set
    }
}