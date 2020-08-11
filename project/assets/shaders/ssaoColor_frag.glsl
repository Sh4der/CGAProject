#version 330 core

//in/outs
out float FragColor;
in vec2 ioTexCoords;

//constants to configure the ssao shading
const int sampleCount = 16; //64 vorher
const float radius = 1.5f; //0.5 vorher
const float bias = 0.025f;

//uniform Textures from the gBuffer FRamebuffer
uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D texNoise;

// other data for ssat shading
uniform vec3 samples[sampleCount];
uniform mat4 projection_matrix;
uniform vec2 screenSize;

void main()
{

    //scaling for the noise Texture to scale it over the whole screen
    vec2 noiseScale = vec2(screenSize.x/4.0, screenSize.y/4.0);

    // get fragment position/normal and random vector from textures
    vec3 pos = texture(gPosition, ioTexCoords).xyz;
    vec3 normal = texture(gNormal, ioTexCoords).rgb; //already normalized in the gBuffer fragment shader
    vec3 randomVec = normalize(texture(texNoise, ioTexCoords * noiseScale).xyz); //get random vec and scale it all over the screen unsing the noiseScale Variable

    //create a matrix to rotate the samples around the normal by the values in the random Vector
    vec3 bitangent = cross(normal, randomVec);
    if( length(bitangent) < 0.0001 )
        bitangent = cross( normal, vec3(0,0,1));
    bitangent = normalize(bitangent);
    vec3 tangent = cross(bitangent, normal);
    mat3 rotateAroundNormal = mat3(tangent, bitangent, normal);

    //occlusion is default 0. the higher this variable gets the darker the pixel is
    float occlusion = 0.0f;
    //go through every sample in the sample array and test if the sample is behind or infromt of the current pixel.
    //When the sample is behind the current pixel increment occulusion
    for(int i = 0; i < sampleCount; ++i)
    {
        //get current sample and rotate it around the normal
        vec3 oneSample = rotateAroundNormal * samples[i];
        //transpose the sample to current position and scale the sample
        oneSample = pos + oneSample * radius;

        //get the screen coords of the current sample
        vec4 offset = vec4(oneSample, 1.0);
        offset = projection_matrix * offset;    // from view to clip-space
        offset.xyz *= 1.0 / offset.w;               // perspective divide
        offset.xyz = offset.xyz * 0.5 + 0.5; // transform to range 0.0 - 1.0

        //get the depth of the pixel at xy position of the sample
        float surfaceDepth = texture(gPosition, offset.xy).z;

        //smoothly interpolate the shading between 0.1 and 1.0
        float rangeCheck = smoothstep(0.0, 1.0, radius / abs(pos.z - surfaceDepth));
        //add one to occulusion when surfaceDepth is larger than the samples depth plus bias
        occlusion += (surfaceDepth >= oneSample.z + bias ? 1.0 : 0.0) * rangeCheck;

    }
    //(occlusion /sampleCount) is the amount of dark samples in percent
    // we invert the value here
    occlusion = 1.0 - (occlusion / sampleCount);

    FragColor = occlusion;

}