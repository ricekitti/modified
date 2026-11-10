#version 120
uniform sampler2D u_texture;
uniform vec2 u_texel_size;
uniform float u_radius;
uniform float u_alpha_threshold;

vec4 pick(vec2 uv, vec2 p) {
    vec4 c;
    c = texture2D(u_texture, uv + vec2( p.x, 0.0)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2(-p.x, 0.0)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2(0.0,  p.y)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2(0.0, -p.y)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2( p.x,  p.y)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2(-p.x,  p.y)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2( p.x, -p.y)); if (c.a > u_alpha_threshold) return c;
    c = texture2D(u_texture, uv + vec2(-p.x, -p.y)); if (c.a > u_alpha_threshold) return c;
    return vec4(0.0);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 center = texture2D(u_texture, uv);
    if (center.a > u_alpha_threshold || u_radius <= 0.0) discard;

    vec4 c = pick(uv, u_texel_size * min(u_radius, 1.0));
    if (c.a <= u_alpha_threshold && u_radius > 1.0) c = pick(uv, u_texel_size * u_radius);
    if (c.a <= u_alpha_threshold) discard;

    gl_FragColor = vec4(c.rgb, 1.0);
}
