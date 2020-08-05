#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoord;
layout(location = 2) in vec3 normal;

//uniforms
// translation object to world
uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

uniform vec2 tcMultiplier;


out struct VertexData
{
    vec3 position;
    vec2 textureCoord;
    vec3 normal;
    vec4 screenSpaceUV;
} vertexData;


void main(){
    vec4 pos = vec4(position, 1.0f);
    vec4 worldPosition = model_matrix * pos;
    vec4 positionInCameraSpace = view_matrix * worldPosition;
    mat4 inverseTransposeViewMatrix = inverse(transpose(view_matrix));

    gl_Position = projection_matrix * positionInCameraSpace;
    //gl_Position = projection_matrix * view_matrix * model_matrix * vec4(pos.xy, pos.z, 1.0f);

    vertexData.position = pos.xyz;
    vertexData.normal = (inverse(transpose(view_matrix * model_matrix)) * vec4(normal, 0.0f)).xyz;

    vertexData.textureCoord = tcMultiplier * textureCoord;

    vertexData.screenSpaceUV = projection_matrix * view_matrix * model_matrix * vec4(pos.xyz, 1.0f);
}