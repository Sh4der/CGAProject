#version 330 core
out vec4 FragColor;

in vec2 ioTexCoords;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform sampler2D ssao;

uniform vec3 lightPosition;
uniform mat4 view_matrix;


struct Pointlight {
    vec3 Position;
    vec3 Color;

    float ConstantAttenuation;
    float LinearAttenuation;
    float QuadraticAttenuation;
};
uniform Pointlight pointlight;

struct Spotlight {
    vec3 Position;
    vec3 Color;

    float ConstantAttenuation;
    float LinearAttenuation;
    float QuadraticAttenuation;
};
uniform Spotlight spotlight;


void main()
{
    vec3 FragPos = texture(gPosition, ioTexCoords).rgb;
    vec3 Normal = texture(gNormal, ioTexCoords).rgb;
    vec3 Diffuse = texture(gAlbedo, ioTexCoords).rgb;
    float AmbientOcclusion = texture(ssao, ioTexCoords).r;

    vec3 ambient = vec3(0.3 * Diffuse) * AmbientOcclusion;

    vec3 lighting  = ambient;
    vec3 viewDir  = normalize(-FragPos); // viewpos is (0.0.0)

    // diffuse
    vec3 lightDir = normalize((view_matrix * vec4(pointlight.Position, 1.0f)).xyz - FragPos);
    vec3 diffuse = max(dot(Normal, lightDir), 0.0f) * Diffuse * pointlight.Color;

    // specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(Normal, halfwayDir), 0.0), 8.0);
    vec3 specular = pointlight.Color * spec;

    // attenuation
    float distance = length(pointlight.Position - FragPos);
    float attenuation = 1.0 / (1.0 + pointlight.LinearAttenuation * distance + pointlight.QuadraticAttenuation * distance * distance);
    diffuse *= attenuation;
    specular *= attenuation;
    lighting += diffuse + specular;


    vec3 viewToLight = (inverse(transpose(view_matrix)) * vec4(pointlight.Position, 1.0f)).xyz;
    vec3 toLight = normalize(viewToLight - FragPos);
    vec3 result = max(dot(Normal, toLight), 0.0) * vec3(0.95f) * pointlight.Color * attenuation;

    FragColor = vec4(vec3(AmbientOcclusion), 1.0f);
    //FragColor = vec4(lighting, 1.0f);
    //FragColor = vec4(0.1, 0.2, 0.3, 1.0f);

}