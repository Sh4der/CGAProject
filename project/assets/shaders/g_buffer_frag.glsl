#version 330 core
layout (location = 0) out vec3 outPosition;
layout (location = 1) out vec3 outNormal;
layout (location = 2) out vec3 outDiff;
layout (location = 3) out vec3 outEmit;
layout (location = 4) out vec3 outSpec;
layout (location = 5) out float outShininess;
layout (location = 6) out vec3 outEmitColor;
layout (location = 7) out float outIsPortal;



in struct VertexData
{
    vec3 fragPos;
    vec2 texCoords;
    vec3 normal;
    vec4 screenSpaceUV;
} vertexdata;

uniform sampler2D emitTex;
uniform sampler2D diffTex;
uniform sampler2D specTex;
uniform float shininess;

uniform float isPortal;
uniform vec3 emitColor;

void calcPortal()
{
    vec4 screenSpaceUV = normalize(vertexdata.screenSpaceUV);

    vec2 ndc = screenSpaceUV.xy / screenSpaceUV.w;
    vec2 screenSpace = (ndc.xy * .5 + .5);

    vec4 tex = texture(diffTex, screenSpace);

    if (emitColor == vec3(1,1,1)) {
        outDiff = tex.xyz;
    }
    else {
        outDiff = emitColor;
    }
}

void calcDefault()
{
    // store the fragment position vector in the first gbuffer texture
    outPosition = vertexdata.fragPos;
    // also store the per-fragment normals into the gbuffer
    outNormal = normalize(vertexdata.normal);
    // and the diffuse per-fragment color
    outDiff.rgb = texture(diffTex, vertexdata.texCoords).rgb;
    outEmit.rgb = texture(emitTex, vertexdata.texCoords).rgb;
    outSpec.rgb = texture(specTex, vertexdata.texCoords).rgb;
    outShininess = shininess;
    outEmitColor = emitColor;
    outIsPortal = isPortal;
}

void main()
{

    calcDefault();
    if(isPortal == 1.0f){
        calcPortal();
    }

}

