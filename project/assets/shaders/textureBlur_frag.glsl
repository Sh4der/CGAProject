#version 330 core
out vec4 FragColor;

in vec2 ioTexCoords;

uniform sampler2D ssaoInput;


/**
* Normal distribution
*
* @param x float position where to get the gaussian value
* @param sigma float sigma of gausssian function
* @return Normal distribution of x and sigma
**/
float normpdf(float x, float sigma)
{
    return 0.39894  / sigma * exp( -0.5 * x * x / (sigma * sigma) );
}


void main()
{


    //declare kernel parameters
    const int kernelDiameter = 11;
    float sigma = 2.0;
    const int kernelRadius = (kernelDiameter-1)/2;
    float kernel[kernelDiameter];
    vec3 resultColor = vec3(0.0);
    //get the texture size
    vec2 texSize = 1.0 / vec2(textureSize(ssaoInput, 0));

    //fill kernel array with gaussian values
    float Z = 0.0;
    for (int j = 0; j <= kernelRadius; ++j)
    {
        kernel[kernelRadius+j] = kernel[kernelRadius-j] = normpdf(float(j), sigma);
    }


    //go through each nearby pixel of the current pixel multiply them by the gaussian value and add them togeter to get the new pixel color
    for (int x=-kernelRadius; x <= kernelRadius; ++x)
    {
        for (int y=-kernelRadius; y <= kernelRadius; ++y)
        {
            vec2 offset = vec2(float(x), float(y)) * texSize;
            resultColor += kernel[kernelRadius+y] * kernel[kernelRadius+x] * texture(ssaoInput, (ioTexCoords.xy + offset)).rgb;
        }
    }

    FragColor = vec4(resultColor, 1.0);
}