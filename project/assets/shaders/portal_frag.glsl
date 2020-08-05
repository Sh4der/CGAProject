#version 330 core

uniform sampler2D emitTex;
uniform vec3 emitColor;
uniform sampler2D diffTex;
uniform sampler2D specTex;
uniform float shininess;

//input from vertex shader
in struct VertexData
{
    vec3 position;
    vec2 textureCoord;
    vec3 normal;
    vec4 screenSpaceUV;
} vertexData;

out vec4 color;

void main() {

    vec4 normalTex = texture(diffTex, vertexData.textureCoord);
    vec4 screenSpaceUV = normalize(vertexData.screenSpaceUV);

    vec2 ndc = screenSpaceUV.xy / screenSpaceUV.w;
    vec2 screenSpace = (ndc.xy * .5 + .5);

    vec4 tex = texture(diffTex, screenSpace);

    //color = vec4(screenSpace, 0, 1);
    //color = vec4(vertexData.textureCoord, 0, 1.0);
    //color = vec4(normalTex.xyz, 1.0);
    color = vec4(tex.xyz, 1.0);

}