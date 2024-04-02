package com.github.fhaustt.yggdrasil.component


import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.scenes.scene2d.Event
import ktx.math.vec2
import javax.swing.tree.TreePath

const val DEFAULT_SPEED = 4f
const val DEFAULT_ATTACK_DAMAGE = 10
const val DEFAULT_LIFE = 30f


data class SpawnCfg(
    val model: AnimationModel,
    val speedScaling: Float = 1f,
    val canAttack: Boolean = true,
    val attackScaling: Float = 1f,
    val attackDelay: Float = 0.2f,
    val attackExtraRange: Float = 0f,
    val lifeScaling: Float = 1f,
    val lootable:Boolean = false,
    val aiTreePath: String= "",
    val physicScaling: Vector2 = vec2(1f, 1f),
    val physicOffSet: Vector2 = vec2(0f, 0f),
    val bodyType: BodyType = DynamicBody,
    val event:Event? = null,
)


data class SpawnComponent (
    var type:String = " ",
    var location:Vector2 = vec2()
)