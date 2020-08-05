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
    vec2 screenSpaceUV;
    vec4 testPos;
} vertexData;

out vec4 color;

void main() {

    vec4 normalTex = texture(diffTex, vertexData.textureCoord);
    vec4 testPos = normalize(vertexData.testPos);

    vec2 ndc = testPos.xy / testPos.w;
    vec2 screenSpace = (ndc.xy * .5 + .5);

    vec4 tex = texture(diffTex, screenSpace);
    //vec4 tex = texture(diffTex, testPos.xy / testPos.w);
    //vec4 tex = texture(diffTex, vertexData.screenSpaceUV);

    //color = vec4(screenSpace, 0, 1);
    //color = vec4(testPos.xy / testPos.w, 0, 1.0);
    color = vec4(tex.xyz, 1.0);
    //color = vec4(vertexData.textureCoord, 0, 1.0);
    //color = vec4(normalTex.xyz, 1.0);

}


/*#version 330 core


uniform vec3 pointLightColor;
uniform vec3 spotLightColor;
uniform float spotLightInnerCone;
uniform float spotLightOuterCone;
uniform float pointLightConstantAttenuation;
uniform float pointLightLinearAttenuation;
uniform float pointLightQuadraticAttenuation;
uniform float spotLightConstantAttenuation;
uniform float spotLightLinearAttenuation;
uniform float spotLightQuadraticAttenuation;


uniform float shininess;
uniform sampler2D emitTex;
uniform vec3 emitColor;
uniform sampler2D diffTex;
uniform sampler2D specTex;


out vec4 color;
in vec3 toLight;
in vec3 toSpotLight;
in vec3 toCamera;
in vec3 viewSpotLightDirection;


//input from vertex shader
in struct VertexData
{
    vec3 position;
    vec2 textureCoord;
    vec3 normal;
    vec4 testPos;
} vertexData;


void main(){
    vec3 normalizedToLight = normalize (toLight);
    vec3 normalizedToSpotLight = normalize (toSpotLight);
    vec3 normalizedToCamera = normalize (toCamera);
    vec3 normalizedNormal = normalize (vertexData.normal);
    vec3 normalizedSpotLightDirection = normalize(viewSpotLightDirection);

    float distanceToLight = length(toLight);
    float pointAttenuation = 1.0f / (pointLightConstantAttenuation + pointLightLinearAttenuation * distanceToLight + pointLightQuadraticAttenuation * (distanceToLight*distanceToLight));

    float brightnessDiff = max(0.0f, dot(normalizedNormal, normalizedToLight));
    vec3 finalDiff =  pointAttenuation * pointLightColor * brightnessDiff * texture(diffTex, vertexData.textureCoord).rgb;

    vec3 reflectedToLight = reflect(-normalizedToLight, normalizedNormal);
    float brightnessSpecular = max(0.0f, dot(reflectedToLight, normalizedToCamera));

    vec3 finalSpec = pointAttenuation * pow(brightnessSpecular, shininess) * texture(specTex, vertexData.textureCoord).rgb * pointLightColor;


    //Output
    vec3 ambient = 0.1f * pointLightColor * pointAttenuation;
    vec3 result = emitColor * texture(emitTex, vertexData.textureCoord).rgb + (ambient * finalDiff + finalSpec);

    //vec3 result = finalDiff * texture(emitTex, vertexData.textureCoord).rgb + finalSpec;
    //vec3 result = (ambient + finalDiff + finalSpec) + texture(emitTex, vertexData.textureCoord).rgb;

    color = vec4(result.rgb, 1.0);
    //color = vec4(vertexData.testPos.xy / vertexData.testPos.w, 1.0, 1.0);
    //color = vec4(0,1,0,1.0);

}*/