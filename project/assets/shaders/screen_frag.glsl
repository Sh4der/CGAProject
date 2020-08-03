#version 330 core
out vec4 FragColor;

in vec2 ioTexCoords;
in vec2 ioPos;


uniform sampler2D tex;

void main()
{
    FragColor = texture(tex, ioTexCoords);
    //FragColor = vec4(0.0f, 1.0f, 0.0f, 1.0f);
    //FragColor = vec4(outTexCoords.x, outTexCoords.y, 0.0f, 1.0f);
    //FragColor = vec4(outPos, 0.0f, 1.0f);

    //FragColor = color;
}