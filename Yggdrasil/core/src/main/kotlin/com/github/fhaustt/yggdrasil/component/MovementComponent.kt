package com.github.fhaustt.yggdrasil.component

data class MovementComponent(
    var speed:Float= 0f,
    var cos:Float = 0f,
    var sin:Float = 0f,
    var root:Boolean = false,
)