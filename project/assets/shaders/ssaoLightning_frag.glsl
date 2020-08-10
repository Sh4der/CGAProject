#version 330 core
const int maxPointlights = 64;
const int maxSpotlights = 64;
const int cellShadingLevels = 4;

out vec4 FragColor;

in vec2 ioTexCoords;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gDiff;
uniform sampler2D gEmit;
uniform sampler2D gSpec;
uniform sampler2D gShininess;
uniform sampler2D gEmitColor;
uniform sampler2D gIsPortal;

uniform sampler2D ssao;

uniform vec3 lightPosition;
uniform mat4 view_matrix;

uniform int cellShading = 0;


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

vec3 calcPointlight(Pointlight curPointlight, vec3 FragPos, vec3 Normal, vec3 Diffuse, vec3 Spec, float Shininess, float AmbientOcclusion) {
    vec3 viewDir  = normalize(-FragPos); // viewpos is (0.0.0)

    // diffuse pointlight
    vec3 lightPositionInViewspace = (view_matrix * vec4(curPointlight.Position, 1.0f)).xyz;
    vec3 toLight = normalize(lightPositionInViewspace - FragPos);
    float brightness = max(dot(Normal, toLight), 0.0f);
    if(cellShading == 1)
    {
        brightness = floor(brightness * cellShadingLevels) / cellShadingLevels;
    }
    vec3 diffuse = brightness * Diffuse * curPointlight.Color;

    // specular pointlight
    vec3 reflectedToLight = reflect(-normalize(toLight), Normal);
    float brightnessSpecular = max(0.0f, dot(reflectedToLight, viewDir));
    if(cellShading == 1)
    {
        brightnessSpecular = floor(brightnessSpecular * cellShadingLevels) / cellShadingLevels;
    }
    vec3 specular = pow(brightnessSpecular, Shininess) * curPointlight.Color * Spec;


    // attenuation pointlight
    float distance = length(lightPositionInViewspace - FragPos);
    float attenuation = 1.0 / (1.0 + curPointlight.LinearAttenuation * distance + curPointlight.QuadraticAttenuation * distance * distance);

    diffuse *= attenuation;
    specular *= attenuation;

    vec3 ambient = 0.1 * curPointlight.Color * attenuation * Diffuse * AmbientOcclusion;

    return diffuse + specular + ambient;
}

vec3 calcSpotlight(Spotlight curSpotlight, vec3 FragPos, vec3 Normal, vec3 Diffuse, vec3 Spec, float Shininess, float AmbientOcclusion)
{
    vec3 viewDir  = normalize(-FragPos); // viewpos is (0.0.0)
    vec3 spotDir = normalize((view_matrix * vec4(curSpotlight.Direction, 0.0f))).xyz;

    vec3 spotlightPositionInViewspace = (view_matrix * vec4(curSpotlight.Position, 1.0f)).xyz;

    vec3 toSpotlight = spotlightPositionInViewspace - FragPos;

    float theta = dot(normalize(toSpotlight), normalize(-spotDir));
    float epsilon = curSpotlight.InnerCone - curSpotlight.OuterCone;
    float intensity = clamp((theta - curSpotlight.OuterCone) / epsilon, 0.0f, 1.0f);
    float brightnessSpotDiff = max(dot(Normal, normalize(toSpotlight)), 0.0f);
    if(cellShading == 1)
    {

        intensity = floor(intensity * cellShadingLevels) / cellShadingLevels;
        brightnessSpotDiff = floor(brightnessSpotDiff * cellShadingLevels) / (cellShadingLevels)+ 0.1;
    }
    vec3 diffuse = intensity * brightnessSpotDiff * curSpotlight.Color * Diffuse;

    vec3 reflectedToSpotLight = reflect(-normalize(toSpotlight), Normal);
    float brightnessSpotSpecular = max(0.0f, dot(reflectedToSpotLight, viewDir));
    if(cellShading == 1)
    {
        brightnessSpotSpecular = floor(brightnessSpotSpecular * cellShadingLevels) / cellShadingLevels;
    }
    vec3 specular = pow(brightnessSpotSpecular, Shininess) * curSpotlight.Color * Spec * intensity;

    float distance = length(toSpotlight);
    float attenuation = 1.0f / (curSpotlight.ConstantAttenuation + curSpotlight.LinearAttenuation * distance + curSpotlight.QuadraticAttenuation * (distance * distance));

    diffuse *= attenuation;
    specular *= attenuation;


    vec3 ambient = 0.1 * curSpotlight.Color * attenuation * Diffuse * AmbientOcclusion;



    return diffuse + ambient;
}

void main()
{
    vec3 FragPos = texture(gPosition, ioTexCoords).rgb;
    vec3 Normal = texture(gNormal, ioTexCoords).rgb;
    vec3 Diffuse = texture(gDiff, ioTexCoords).rgb;
    vec3 Emit = texture(gEmit, ioTexCoords).rgb;
    vec3 Spec = texture(gSpec, ioTexCoords).rgb;
    float Shininess = texture(gShininess, ioTexCoords).x;
    vec3 EmitColor = texture(gEmitColor, ioTexCoords).rgb;
    float IsPortal = texture(gIsPortal, ioTexCoords).r;


    float AmbientOcclusion = texture(ssao, ioTexCoords).r;


    vec3 lighting  = vec3(0f);
    lighting += Emit * EmitColor;
    if(IsPortal <= 0.01)
    {
        for(int i = 0; i < numPointlights; i++)
        {
            lighting += calcPointlight(pointlight[i], FragPos, Normal, Diffuse, Spec, Shininess, AmbientOcclusion);
        }
        for(int i = 0; i < numSpotlights; i++)
        {
            lighting += calcSpotlight(spotlight[i], FragPos, Normal, Diffuse, Spec, Shininess, AmbientOcclusion);
        }
    }
    else
    {
        lighting += Diffuse;
    }



    //FragColor = vec4(vec3(AmbientOcclusion), 1.0f);
    FragColor = vec4(lighting, 1.0f);
}