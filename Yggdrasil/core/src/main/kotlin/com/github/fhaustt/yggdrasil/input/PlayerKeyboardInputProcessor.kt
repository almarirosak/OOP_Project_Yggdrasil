package com.github.fhaustt.yggdrasil.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.*
import com.github.fhaustt.yggdrasil.component.AttackComponent
import com.github.fhaustt.yggdrasil.component.MovementComponent
import com.github.fhaustt.yggdrasil.component.PlayerComponent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.app.KtxInputAdapter

class PlayerKeyboardInputProcessor(
    world: World,
    private val moveCmps:ComponentMapper<MovementComponent> = world.mapper(),
    private val attackCmps:ComponentMapper<AttackComponent> = world.mapper(),
) : KtxInputAdapter {

    private var playerSine = 0f
    private var playerCos = 0f
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    init {
        Gdx.input.inputProcessor = this
    }

    private fun Int.isMovementKey() : Boolean {
        return this == UP || this == DOWN || this == LEFT || this == RIGHT
    }

    private fun updatePlayerMovement(){
        playerEntities.forEach { player ->
            with(moveCmps[player]){
                sin = playerSine
                cos = playerCos
            }

        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode.isMovementKey()){
            when (keycode){
                UP -> playerSine = 1f
                DOWN -> playerSine = -1f
                RIGHT -> playerCos = 1f
                LEFT -> playerCos = -1f
            }
            updatePlayerMovement()
            return true
        }else if(keycode == SPACE){
            playerEntities.forEach {
                with(attackCmps[it]){
                    doAttack = true
                    //startAttack()

                }
            }
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode.isMovementKey()){
            when (keycode){
                UP -> playerSine = if (Gdx.input.isKeyPressed(DOWN)) -1f else 0f
                DOWN -> playerSine = if (Gdx.input.isKeyPressed(UP)) 1f else 0f
                RIGHT -> playerCos = if (Gdx.input.isKeyPressed(LEFT)) -1f else 0f
                LEFT -> playerCos = if (Gdx.input.isKeyPressed(RIGHT)) 1f else 0f
            }
            updatePlayerMovement()
            return true
        }
        return false
    }
}