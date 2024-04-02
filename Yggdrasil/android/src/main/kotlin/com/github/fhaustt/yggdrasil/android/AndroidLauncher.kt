package com.github.fhaustt.yggdrasil.android


import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.github.fhaustt.yggdrasil.Yggdrasil

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(Yggdrasil(), AndroidApplicationConfiguration().apply {
            // Configure your application here.
        })
    }
}
