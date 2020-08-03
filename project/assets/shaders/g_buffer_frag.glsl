#version 330 core
layout (location = 0) out vec3 outPosition;
layout (location = 1) out vec3 outNormal;
layout (location = 2) out vec3 outAlbedo;

in struct VertexData
{
    vec3 fragPos;
    vec2 texCoords;
    vec3 normal;
} vertexdata;

uniform sampler2D emitTex;


void main()
{
    // store the fragment position vector in the first gbuffer texture
    outPosition = vertexdata.fragPos;
    // also store the per-fragment normals into the gbuffer
    outNormal = normalize(vertexdata.normal);
    // and the diffuse per-fragment color
    outAlbedo.rgb = vec3(0.95f);
    //outAlbedo.rgb = texture(emitTex, vertexdata.texCoords).rgb;
}