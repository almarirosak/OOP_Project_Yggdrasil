package com.github.fhaustt.yggdrasil.component


import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.fhaustt.yggdrasil.actor.FlipImage
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity

// Entity Component System to handle entity defining and rendering
class ImageComponent: Comparable<ImageComponent>{ // Added comparable image component (sorted rendering)
    lateinit var  image: FlipImage // Holds the component to the image actor (entities)

    override fun compareTo(other: ImageComponent): Int { //Layer comparison on x and y axes
        val yDiff = other.image.y.compareTo(image.y)
        return if (yDiff != 0){
            yDiff
        } else {
            other.image.x.compareTo(image.x)
        }
    }

    companion object {
        class ImageComponentListener( //Setting stage actors
            private val stage: Stage
        ) : ComponentListener<ImageComponent>{
            override fun onComponentAdded(entity: Entity, component: ImageComponent) {
                stage.addActor(component.image) //Add actor to stage
            }

            override fun onComponentRemoved(entity: Entity, component: ImageComponent) {
                stage.root.removeActor(component.image) // Remove actor from stage
            }
        }
    }

}