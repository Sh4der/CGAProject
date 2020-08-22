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

//Light
uniform vec3 pointLightPosition;

uniform vec3 spotLightPosition;
uniform vec3 spotLightDirection;


out struct VertexData
{
    vec3 position;
    vec2 textureCoord;
    vec3 normal;
    vec4 testPos;
} vertexData;


out vec3 toLight;
out vec3 toSpotLight;
out vec3 toCamera;
out mat4 passInverseTransposeViewMatrix;
out vec3 viewSpotLightDirection;
out mat4 inverseTransposeViewMatrixToFragment;

void main(){
    vec4 pos = vec4(position, 1.0f);
    vec4 worldPosition = model_matrix * pos;
    vec4 positionInCameraSpace = view_matrix * worldPosition;
    mat4 inverseTransposeViewMatrix = inverse(transpose(view_matrix));
    inverseTransposeViewMatrixToFragment = inverseTransposeViewMatrix;

    gl_Position = projection_matrix * positionInCameraSpace;

    vertexData.testPos = projection_matrix * positionInCameraSpace;

    vertexData.position = worldPosition.xyz;
    vertexData.textureCoord = tcMultiplier * textureCoord;

    vertexData.normal = (inverseTransposeViewMatrix * model_matrix * vec4(normal, 0.0f)).xyz;


    toCamera = - positionInCameraSpace.xyz;
    viewSpotLightDirection = (inverseTransposeViewMatrix * vec4(spotLightDirection, 0.0f)).xyz;
}