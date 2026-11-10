#version 120

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
