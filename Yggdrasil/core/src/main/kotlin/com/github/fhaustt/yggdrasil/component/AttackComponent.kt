package com.github.fhaustt.yggdrasil.component

enum class AttackState{
    READY, PREPARE, ATTACKING, DEAL_DAMAGE
}

class AttackComponent(
    var doAttack:Boolean = false, //boolean to control attacking(stop spamming)
    var state: AttackState = AttackState.READY,
    var damage:Int = 5,
    var delay:Float = 0f,
    var maxDelay:Float = 0f,
    var extraRange:Float = 0f, // since hitbox doesn't cover the hand movement of player, thus we need this variable to calculate.
    ){
    val isReady:Boolean
        get() = state == AttackState.READY

    val isPrepared:Boolean
        get() = state == AttackState.PREPARE

    val isAttacking:Boolean
        get() = state == AttackState.ATTACKING

    fun startAttack(){
        state = AttackState.PREPARE
    }

}