#version 330 core
layout (location = 0) out vec3 outPosition;
layout (location = 1) out vec3 outNormal;
layout (location = 2) out vec3 outDiff;
layout (location = 3) out vec3 outEmit;
layout (location = 4) out vec3 outSpec;
layout (location = 5) out float outShininess;


in struct VertexData
{
    vec3 fragPos;
    vec2 texCoords;
    vec3 normal;
} vertexdata;

uniform sampler2D emitTex;
uniform sampler2D diffTex;
uniform sampler2D specTex;
uniform float shininess;


void main()
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
}