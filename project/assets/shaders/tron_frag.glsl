#version 330 core
const int maxPointlights = 64;
const int maxSpotlights = 64;


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
in mat4 inverseTransposeViewMatrixToFragment;


//input from vertex shader
in struct VertexData
{
    vec3 position;
    vec2 textureCoord;
    vec3 normal;
    vec4 testPos;
} vertexData;

struct Pointlight {
    vec3 Position;
    vec3 Color;

    float ConstantAttenuation;
    float LinearAttenuation;
    float QuadraticAttenuation;
};
uniform int numPointlights;
uniform Pointlight pointlight[maxPointlights];

struct Spotlight {
    vec3 Position;
    vec3 Color;
    vec3 Direction;
    float InnerCone;
    float OuterCone;

    float ConstantAttenuation;
    float LinearAttenuation;
    float QuadraticAttenuation;
};
uniform int numSpotlights;
uniform Spotlight spotlight[maxSpotlights];


void main(){


    vec3 normalizedToCamera = normalize (toCamera);
    vec3 normalizedNormal = normalize (vertexData.normal);

    vec3 finalDiff = vec3(0f);
    vec3 finalSpec = vec3(0f);
    vec3 finalPointAmbient = vec3(0f);
    vec3 finalSpotDiff = vec3(0f);
    vec3 finalSpotSpec = vec3(0f);
    vec3 finalSpotAmbient = vec3(0f);

    for(int i = 0; i < numPointlights; i++)
    {
        vec3 toLight = (inverseTransposeViewMatrixToFragment * vec4(pointlight[i].Position, 1.0f)).xyz - (inverseTransposeViewMatrixToFragment * vec4(vertexData.position, 1f)).xyz;
        vec3 normalizedToLight = normalize (toLight);
        float distanceToLight = length(toLight);
        float pointAttenuation = 1.0f / (pointlight[i].ConstantAttenuation + pointlight[i].LinearAttenuation * distanceToLight + pointlight[i].QuadraticAttenuation * (distanceToLight*distanceToLight));

        finalPointAmbient += 0.1f * pointlight[i].Color * pointAttenuation;


        float brightnessDiff = max(0.0f, dot(normalizedNormal, normalizedToLight));
        finalDiff +=  (pointAttenuation * pointlight[i].Color * brightnessDiff * texture(diffTex, vertexData.textureCoord).rgb);

        vec3 reflectedToLight = reflect(-normalizedToLight, normalizedNormal);
        float brightnessSpecular = max(0.0f, dot(reflectedToLight, normalizedToCamera));

        finalSpec += pointAttenuation * pow(brightnessSpecular, shininess) * texture(specTex, vertexData.textureCoord).rgb * pointlight[i].Color;
    }

    for(int i = 0; i < numSpotlights; i++)
    {
        vec3 toSpotLight = (inverseTransposeViewMatrixToFragment * vec4(spotlight[i].Position, 1.0f)).xyz - (inverseTransposeViewMatrixToFragment * vec4(vertexData.position, 1f)).xyz;
        //Spotlight
        vec3 normalizedToSpotLight = normalize (toSpotLight);
        vec3 normalizedSpotLightDirection = normalize(viewSpotLightDirection);
        float distanceToSpot = length(toSpotLight);
        float spotAttenuation = 1.0f / (spotlight[i].ConstantAttenuation + spotlight[i].LinearAttenuation * distanceToSpot + spotlight[i].QuadraticAttenuation * (distanceToSpot*distanceToSpot));
        finalSpotAmbient += 0.1f * spotlight[i].Color * spotAttenuation;


        float theta = dot(normalizedToSpotLight, normalize(-normalizedSpotLightDirection));
        float epsilon = spotlight[i].InnerCone - spotlight[i].InnerCone;
        float intensity = clamp((theta-spotlight[i].OuterCone)/epsilon, 0.0f, 1.0f);
        float brightnessSpotDiff = max(dot(normalizedNormal, normalizedToSpotLight), 0.0f);
        finalSpotDiff += spotAttenuation * brightnessSpotDiff * spotlight[i].Color * intensity * texture(diffTex, vertexData.textureCoord).rgb;

        vec3 reflectedToSpotLight = reflect(-normalizedToSpotLight, normalizedNormal);
        float brightnessSpotSpecular = max(0.0f, dot(reflectedToSpotLight, normalizedToCamera));

        finalSpotSpec += spotAttenuation * pow(brightnessSpotSpecular, shininess) * texture(specTex, vertexData.textureCoord).rgb * spotlight[i].Color;
    }

    //Output
    //vec3 result = emitColor * texture(emitTex, vertexData.textureCoord).rgb + (finalDiff + finalSpec + finalSpotDiff );
    vec3 result = emitColor * texture(emitTex, vertexData.textureCoord).rgb + finalPointAmbient + finalDiff + finalSpec + finalSpotDiff + finalSpotAmbient;

    //vec3 result = finalDiff * texture(emitTex, vertexData.textureCoord).rgb + finalSpec;
    //vec3 result = (ambient + finalDiff + finalSpec) + texture(emitTex, vertexData.textureCoord).rgb;

    color = vec4(result.rgb, 1.0);
    //color = vec4(vertexData.testPos.xy / vertexData.testPos.w, 1.0, 1.0);
    //color = vec4(0,1,0,1.0);

}