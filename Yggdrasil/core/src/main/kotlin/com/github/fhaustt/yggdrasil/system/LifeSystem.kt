package com.github.fhaustt.yggdrasil.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.fhaustt.yggdrasil.component.*
import com.github.quillraven.fleks.*
import ktx.assets.disposeSafely

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem (
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val physicCmps: ComponentMapper<PhysicsComponent>
) : IteratingSystem(){
    private val damageFont = BitmapFont(Gdx.files.internal("damage.fnt"))
    private val floatingTextSyle = LabelStyle(damageFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        // regenerate health function with every tick, adjust at regeneration
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.max)

        // if health > 0(alive), take damage reduce health with (damage)
        if (lifeCmp.takeDamage > 0f){
            val physicCmp = physicCmps[entity]
            lifeCmp.life -= lifeCmp.takeDamage
            floatingText(lifeCmp.takeDamage.toInt().toString(), physicCmp.body.position, physicCmp.size)
            lifeCmp.takeDamage = 0f
        }

        // if player is dead (health <= 0), set a revive time
        if (lifeCmp.isDead){
            configureEntity(entity){
                deadCmps.add(it){
                    if(it in playerCmps){
                        reviveTime = 7f
                    }
                }

            }
        }
    }

    private fun floatingText(text: String, position: Vector2, size: Vector2) {
        world.entity{
            add<FloatingTextComponent>{
                txtLocation.set(position.x, position.y - size.y * 0.5f)
                lifeSpan = 1.5f
                label = Label(text, floatingTextSyle)
            }
        }
    }

    override fun onDispose() {
        damageFont.disposeSafely()
    }

}