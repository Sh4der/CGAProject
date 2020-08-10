#version 330 core
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoord;
layout(location = 2) in vec3 normal;


out struct VertexData
{
    vec3 fragPos;
    vec2 texCoords;
    vec3 normal;
    vec4 screenSpaceUV;
} vertexdata;

uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

uniform vec2 tcMultiplier;

void main()
{
    vec4 viewPos = view_matrix * model_matrix * vec4(position, 1.0f);
    vertexdata.fragPos = viewPos.xyz;
    vertexdata.texCoords = textureCoord * tcMultiplier;

    mat3 normalMatrix = transpose(inverse(mat3(view_matrix * model_matrix)));
    vertexdata.normal = normalMatrix * normal;

    gl_Position = projection_matrix * viewPos;

    vertexdata.screenSpaceUV = projection_matrix * viewPos;

}