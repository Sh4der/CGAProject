package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram

class Lightpool : ArrayList<ILight>() {

    fun bind(shader : ShaderProgram)
    {
        var numSpotlight = 0
        var numPointlight = 0
        for (light in this)
        {
            if(light.name == "pointlight")
            {
                light.bind(shader, "${light.name}[$numPointlight].")
                numPointlight++;
            }else if(light.name == "spotlight")
            {
                light.bind(shader, "${light.name}[$numSpotlight].")
                numSpotlight++;
            }
            shader.setUniform("numPointlights", numPointlight)
            shader.setUniform("numSpotlights", numSpotlight)
        }
    }
}