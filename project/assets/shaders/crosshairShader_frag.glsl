#version 330 core
out vec4 FragColor;

in vec2 ioTexCoords;

uniform vec2 screenSize;

uniform sampler2D tex;

void main() {


    FragColor = texture(tex, ioTexCoords);

    if(gl_FragCoord.x >= screenSize.x/2f && gl_FragCoord.x <= screenSize.x/2f + 5f &&
    gl_FragCoord.y >= screenSize.y/2f - 5f && gl_FragCoord.y <= screenSize.y/2f + 5f)
    {
        FragColor = vec4(0.9f, 0.41f, 0.04f, 1f);
    }
    else if(gl_FragCoord.x >= screenSize.x/2f - 5f && gl_FragCoord.x <= screenSize.x/2f&&
    gl_FragCoord.y >= screenSize.y/2f - 5f && gl_FragCoord.y <= screenSize.y/2f + 5f)
    {
        FragColor = vec4(0.04f, 0.41f, 0.9f, 1f);
    }
}
