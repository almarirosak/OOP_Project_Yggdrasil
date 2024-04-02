@file:JvmName("Lwjgl3Launcher")

package com.github.fhaustt.yggdrasil.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.github.fhaustt.yggdrasil.Yggdrasil

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(Yggdrasil(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Yggdrasil")
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
