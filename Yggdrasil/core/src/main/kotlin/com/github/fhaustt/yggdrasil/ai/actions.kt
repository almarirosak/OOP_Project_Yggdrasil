package com.github.fhaustt.yggdrasil.ai

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.badlogic.gdx.ai.utils.random.FloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.github.fhaustt.yggdrasil.component.AnimationType
import ktx.math.vec2

abstract class Action : LeafTask<AiEntity>() {
    val entity: AiEntity
        get() = `object`

    override fun copyTo(task: Task<AiEntity>) = task
}

class IdleTask(
    @JvmField
    @TaskAttribute(required = true)
    var duration: FloatDistribution? = null
): Action(){
    private var currentDuration = 0f

    override fun execute():Status {
        if (status != Status.RUNNING){
            entity.animation(AnimationType.IDLE)
            currentDuration = duration?.nextFloat() ?: 1f
            return Status.RUNNING
        }

        currentDuration -= GdxAI.getTimepiece().deltaTime

        if (currentDuration <= 0f){
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }

    override fun copyTo(task: Task<AiEntity>): Task<AiEntity> {
        (task as IdleTask).duration = duration
        return task
    }
}

class WanderTask():Action(){

    private val startPos = vec2()
    private val targetPos = vec2()

    override fun execute(): Status {
        if(status != Status.RUNNING){
            entity.animation(AnimationType.WALK)
            if(startPos.isZero){
                startPos.set(entity.position)
            }
            targetPos.set(startPos)
            targetPos.x += MathUtils.random(-3f, 3f)
            targetPos.y += MathUtils.random(-3f, 3f)
            entity.moveTo(targetPos)
            return Status.RUNNING
        }

        if(entity.inRange(1f,targetPos)){
            entity.stopMovement()
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }

}

class AttackTask : Action(){
    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, resetAnimation = true)
            entity.doAndStartAttack()
            return Status.RUNNING
        }

        if(entity.isAnimationDone){
            entity.animation(AnimationType.IDLE)
            entity.stopMovement()
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }

}