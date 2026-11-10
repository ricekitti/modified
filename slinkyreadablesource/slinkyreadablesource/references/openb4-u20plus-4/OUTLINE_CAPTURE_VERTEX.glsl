#version 120
varying float v_glint;

void main() {
    mat4 m = gl_TextureMatrix[0];

    float delta =
        abs(m[0][0] - 1.0) + abs(m[0][1]) +
        abs(m[1][0]) + abs(m[1][1] - 1.0) +
        abs(m[3][0]) + abs(m[3][1]);

    v_glint = step(0.001, delta);
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
