#version 120
uniform sampler2D u_texture;
uniform vec3 u_color;
uniform float u_alpha_threshold;
varying float v_glint;

void main() {
    if (v_glint > 0.5) discard;

    float a = texture2D(u_texture, gl_TexCoord[0].st).a;
    if (a <= u_alpha_threshold) discard;

    gl_FragColor = vec4(u_color, 1.0);
}
