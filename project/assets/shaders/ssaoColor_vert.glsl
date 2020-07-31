#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoord;

out vec2 ioTextureCoord;

void main()
{
    ioTextureCoord = textureCoord;
    gl_Position = vec4(position, 1.0f);
}