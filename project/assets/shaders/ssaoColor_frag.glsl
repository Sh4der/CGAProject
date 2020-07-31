#version 330 core
out float FragColor;

in vec2 ioTexCoords;

const int sampleCount = 64;
const float radius = 0.5f;
const float bias = 0.025f;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D texNoise;

uniform vec3 samples[sampleCount];
uniform mat4 projection_matrix;
uniform vec2 screenSize;

void main()
{
    // tile noise texture over screen, based on screen dimensions divided by noise size
    vec2 noiseScale = vec2(screenSize.x/4.0, screenSize.y/4.0);

    vec3 pos = texture(gPosition, ioTexCoords).xyz;
    vec3 normal = texture(gNormal, ioTexCoords).rgb;
    vec3 randomVec = normalize(texture(texNoise, ioTexCoords * noiseScale).xyz);

    //create TBN matrix to transform coords from tangentspace to cameracpace
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);

    float occlusion = 0.0f;
    for(int i = 0; i < sampleCount; ++i)
    {
        vec3 oneSample = TBN * samples[i];
        oneSample = pos + oneSample * radius;

        vec4 offset = vec4(oneSample, 1.0);
        offset = projection_matrix * offset;    // from view to clip-space
        offset.xyz /= offset.w;               // perspective divide
        offset.xyz = offset.xyz * 0.5 + 0.5; // transform to range 0.0 - 1.0

        float sampleDepth = texture(gPosition, offset.xy).z;

        float rangeCheck = smoothstep(0.0, 1.0, radius / abs(pos.z - sampleDepth));
        occlusion += (sampleDepth >= oneSample.z + bias ? 1.0 : 0.0) * rangeCheck;

    }
    occlusion = 1.0 - (occlusion / sampleCount);
    FragColor =  occlusion;
}