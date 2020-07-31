#version 330 core
out vec4 FragColor;

in vec2 ioTextureCoord;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform sampler2D ssao;

struct Pointlight {
    vec3 Position;
    vec3 Color;

    float Constant;
    float Linear;
    float Quadratic;
};
uniform Pointlight pointlight;

struct Spotlight {
    vec3 Position;
    vec3 Color;

    float Constant;
    float Linear;
    float Quadratic;
};
uniform Spotlight spotlight;


void main()
{
    vec3 FragPos = texture(gPosition, ioTextureCoord).rgb;
    vec3 Normal = texture(gNormal, ioTextureCoord).rgb;
    vec3 Diffuse = texture(gAlbedo, ioTextureCoord).rgb;
    float AmbientOcclusion = texture(ssao, ioTextureCoord).r;

    vec3 ambient = vec3(0.3 * Diffuse);

    vec3 lighting  = ambient;
    vec3 viewDir  = normalize(-FragPos); // viewpos is (0.0.0)
    // diffuse
    vec3 lightDir = normalize(pointlight.Position - FragPos);
    vec3 diffuse = max(dot(Normal, lightDir), 0.0) * Diffuse * pointlight.Color;
    // specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(Normal, halfwayDir), 0.0), 8.0);
    vec3 specular = pointlight.Color * spec;
    // attenuation
    float distance = length(pointlight.Position - FragPos);
    float attenuation = 1.0 / (1.0 + pointlight.Linear * distance + pointlight.Quadratic * distance * distance);
    diffuse *= attenuation;
    specular *= attenuation;
    lighting += diffuse + specular;

    FragColor = vec4(lighting, 1.0);




}