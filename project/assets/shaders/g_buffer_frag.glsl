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

void main()
{
    // store the fragment position vector in the first gbuffer texture
    outPosition = vertexdata.fragPos;
    //outPosition = vec3(1.0f, 0.0f, 0.0f);
    // also store the per-fragment normals into the gbuffer
    outNormal = normalize(vertexdata.normal);
    // and the diffuse per-fragment color
    outAlbedo.rgb = vec3(0.95);
}