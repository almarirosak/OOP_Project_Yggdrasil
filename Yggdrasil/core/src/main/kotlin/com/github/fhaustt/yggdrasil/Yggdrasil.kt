package com.github.fhaustt.yggdrasil


import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.github.fhaustt.yggdrasil.screen.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen




/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */

class Yggdrasil : KtxGame<KtxScreen>(){

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        addScreen(GameScreen())
        setScreen<GameScreen>()
    }

    companion object{
        const val UNIT_SCALE = 1/16f
    }


}
