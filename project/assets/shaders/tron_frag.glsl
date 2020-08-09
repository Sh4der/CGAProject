#version 330 core


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


    //Spotlight
    float distanceToSpot = length(toSpotLight);
    float spotAttenuation = 1.0f / (spotLightConstantAttenuation + spotLightLinearAttenuation * distanceToLight + spotLightQuadraticAttenuation * (distanceToLight*distanceToLight));

    float theta = dot(normalizedToSpotLight, normalize(-normalizedSpotLightDirection));
    float epsilon = spotLightInnerCone - spotLightOuterCone;
    float intensity = clamp((theta-spotLightOuterCone)/epsilon, 0.0f, 1.0f);
    float brightnessSpotDiff = max(dot(normalizedNormal, normalizedToSpotLight), 0.0f);
    vec3 finalSpotDiff = spotAttenuation * brightnessSpotDiff * spotLightColor * intensity * texture(diffTex, vertexData.textureCoord).rgb;

    vec3 reflectedToSpotLight = reflect(-normalizedToSpotLight, normalizedNormal);
    float brightnessSpotSpecular = max(0.0f, dot(reflectedToSpotLight, normalizedToCamera));

    vec3 finalSpotSpec = spotAttenuation * pow(brightnessSpotSpecular, shininess) * texture(specTex, vertexData.textureCoord).rgb * spotLightColor;


    //Output
    vec3 ambient = 0.1f * pointLightColor * pointAttenuation;
    vec3 result = emitColor * texture(emitTex, vertexData.textureCoord).rgb + (ambient * finalDiff + finalSpec + finalSpotDiff );

    //vec3 result = finalDiff * texture(emitTex, vertexData.textureCoord).rgb + finalSpec;
    //vec3 result = (ambient + finalDiff + finalSpec) + texture(emitTex, vertexData.textureCoord).rgb;

    color = vec4(result.rgb, 1.0);
    //color = vec4(vertexData.testPos.xy / vertexData.testPos.w, 1.0, 1.0);
    //color = vec4(0,1,0,1.0);

}