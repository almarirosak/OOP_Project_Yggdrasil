package com.github.fhaustt.yggdrasil.ai

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g3d.model.Animation
import com.github.fhaustt.yggdrasil.component.AnimationType

enum class DefaultState : EntityState{
    IDLE{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.IDLE)
        }

        override fun update(entity: AiEntity) {
            when{
                entity.wantsToAttack -> entity.state(ATTACK)
                entity.wantsToWalk -> entity.state(WALK)
            }
        }
    },
    WALK{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.WALK)
        }

        override fun update(entity: AiEntity) {
            when{
                entity.wantsToAttack -> entity.state(ATTACK)
                !entity.wantsToWalk -> entity.state(IDLE)
            }

        }
        },
    ATTACK{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.ATTACK, PlayMode.NORMAL)
            entity.root(true)
            entity.startAttack()

            }

        override fun exit(entity: AiEntity) {
            entity.root(false)
        }

        override fun update(entity: AiEntity) {
            val attackCmp = entity.attackCmp
            if(attackCmp.isReady && !attackCmp.doAttack){
                entity.changeToPreviousState()
            } else if (attackCmp.isReady){
                //start another attack
                entity.animation(AnimationType.ATTACK, PlayMode.NORMAL, true)
                entity.startAttack()
            }
        }
        },
    DEATH{
        override fun enter(entity: AiEntity) {
            entity.enableGlobalState(true)
            entity.root(true)
        } },

    RESURRECT{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.DEATH, PlayMode.REVERSED, true)
        }

        override fun update(entity: AiEntity) {
            if(entity.isAnimationDone){
                entity.state(IDLE)
                entity.root(false)
            }
        }
    }

}

enum class DefaultGlobalState: EntityState {
    CHECK_ALIVE{
        override fun update(entity: AiEntity) {
            if (entity.isDead){
                entity.enableGlobalState(false)
                entity.state(DefaultState.DEATH, true)
            }
        }
    }
}