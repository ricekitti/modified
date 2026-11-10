// GLSL literals copied unchanged from OpenB4 u20+-4.
#pragma once
namespace openb4 {
inline constexpr char KAWASE_BLOOM_DOWN[] = R"OPENB4(#version 120

uniform sampler2D inTexture;
uniform vec2 offset, halfpixel, iResolution;

void main() {
    vec2 uv = gl_FragCoord.xy / iResolution;

    vec4 sum = texture2D(inTexture, uv);
    sum.rgb *= sum.a;
    sum *= 4.0;
    
    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);
    smp1.rgb *= smp1.a;
    sum += smp1;
    
    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);
    smp2.rgb *= smp2.a;
    sum += smp2;
    
    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
    smp3.rgb *= smp3.a;
    sum += smp3;
    
    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);
    smp4.rgb *= smp4.a;
    sum += smp4;
    
    vec4 result = sum / 8.0;
    gl_FragColor = vec4(result.rgb / result.a, result.a);
}
)OPENB4";
inline constexpr char KAWASE_BLOOM_UP[] = R"OPENB4(#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 halfpixel, offset, iResolution;
uniform int check;

void main() {
    vec2 uv = gl_FragCoord.xy / iResolution;

    vec2 offset1 = vec2(-halfpixel.x, 0.0) * offset;
    vec2 offset2 = vec2(-halfpixel.x, halfpixel.y) * offset;
    vec2 offset3 = vec2(0.0, halfpixel.y * 2.0) * offset;
    vec2 offset4 = vec2(halfpixel.x, halfpixel.y) * offset;
    vec2 offset5 = vec2(halfpixel.x * 2.0, 0.0) * offset;
    vec2 offset6 = vec2(halfpixel.x, -halfpixel.y) * offset;
    vec2 offset7 = vec2(0.0, -halfpixel.y * 2.0) * offset;
    vec2 offset8 = vec2(-halfpixel.x, -halfpixel.y) * offset;

    vec4 sum = texture2D(inTexture, uv + offset1);
    sum.rgb *= sum.a;
    vec4 smpl1 = texture2D(inTexture, uv + offset2);
    smpl1.rgb *= smpl1.a;
    sum += smpl1 * 2.0;
    vec4 smp2 = texture2D(inTexture, uv + offset3);
    smp2.rgb *= smp2.a;
    sum += smp2;
    vec4 smp3 = texture2D(inTexture, uv + offset4);
    smp3.rgb *= smp3.a;
    sum += smp3 * 2.0;
    vec4 smp4 = texture2D(inTexture, uv + offset5);
    smp4.rgb *= smp4.a;
    sum += smp4;
    vec4 smp5 = texture2D(inTexture, uv + offset6);
    smp5.rgb *= smp5.a;
    sum += smp5 * 2.0;
    vec4 smp6 = texture2D(inTexture, uv + offset7);
    smp6.rgb *= smp6.a;
    sum += smp6;
    vec4 smp7 = texture2D(inTexture, uv + offset8);
    smp7.rgb *= smp7.a;
    sum += smp7 * 2.0;

    vec4 result = sum / 12.0;
    float checkAlpha = texture2D(textureToCheck, gl_TexCoord[0].st).a;
    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - checkAlpha), float(check)));
}
)OPENB4";
inline constexpr char OUTLINE_MASK[] = R"OPENB4(#version 120
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
)OPENB4";
inline constexpr char OUTLINE_CAPTURE_VERTEX[] = R"OPENB4(#version 120
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
)OPENB4";
inline constexpr char OUTLINE[] = R"OPENB4(#version 120
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
)OPENB4";
inline constexpr char VERTEX[] = R"OPENB4(#version 120

void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
})OPENB4";
}
