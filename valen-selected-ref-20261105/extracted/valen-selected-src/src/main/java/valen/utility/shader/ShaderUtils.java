/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.util.ResourceLocation
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL20
 */
package valen.utility.shader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import valen.lunar.render.FullscreenQuad;
import valen.utility.Utils;

public class ShaderUtils {
    private Minecraft mc = Minecraft.getMinecraft();
    public final int programID;
    private final Map<String, Integer> uniformLocations = new HashMap<String, Integer>();
    private final String kawaseUpGlow = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform bool check;\nuniform float lastPass;\nuniform float exposure;\n\nvoid main() {\n    if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));\n}";
    private final String glowShader = "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    innerColor *= weights[0];\n    for (float r = 1.0; r <= radius; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n";
    private final String roundRectTexture = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) -size, 0.)) - radius;\n}\n\n\nvoid main() {\n    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}";
    private final String roundRectOutline = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat roundedSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n}\n\nvoid main() {\n    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}";
    private final String kawaseUpBloom = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec2 offset1 = vec2(-halfpixel.x, 0.0) * offset;\n    vec2 offset2 = vec2(-halfpixel.x, halfpixel.y) * offset;\n    vec2 offset3 = vec2(0.0, halfpixel.y * 2.0) * offset;\n    vec2 offset4 = vec2(halfpixel.x, halfpixel.y) * offset;\n    vec2 offset5 = vec2(halfpixel.x * 2.0, 0.0) * offset;\n    vec2 offset6 = vec2(halfpixel.x, -halfpixel.y) * offset;\n    vec2 offset7 = vec2(0.0, -halfpixel.y * 2.0) * offset;\n    vec2 offset8 = vec2(-halfpixel.x, -halfpixel.y) * offset;\n\n    vec4 sum = texture2D(inTexture, uv + offset1);\n    sum.rgb *= sum.a;\n    vec4 smpl1 = texture2D(inTexture, uv + offset2);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + offset3);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + offset4);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + offset5);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + offset6);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + offset7);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + offset8);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n\n    vec4 result = sum / 12.0;\n    float checkAlpha = texture2D(textureToCheck, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - checkAlpha), float(check)));\n}\n";
    private final String kawaseDownBloom = "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec4 sum = texture2D(inTexture, uv);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}\n";
    private final String kawaseUp = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;\n\n    vec4 average = sum / 12.0;\n    gl_FragColor = vec4(average.rgb, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}";
    private final String kawaseDown = "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec4 sum = texture2D(inTexture, uv) * 4.0;\n    sum += texture2D(inTexture, uv - halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n\n    gl_FragColor = vec4(sum.rgb * 0.125, 1.0);\n}";
    private final String gradientMask = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\n\n#define NOISE .5/255.0\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);\n}";
    private final String mask = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D u_texture, u_texture2;\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;\n    vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;\n    gl_FragColor = vec4(tex2Color, texColorAlpha);\n}";
    private final String gradient = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n#define NOISE .5/255.0\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    gl_FragColor = createGradient(coords, color1, color2, color3, color4);\n}";
    private final String roundedRectGradient = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b , 0.0)) - r;\n}\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}";
    private final String roundedRect = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0.0)) - r;\n}\n\n\nvoid main() {\n    vec2 rectHalf = rectSize * .5;\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\n    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);\n\n}";
    private final String guiShape = "#version 120\n\nuniform vec2 size, margin;\nuniform float radius, softness, stroke, cell, inset;\nuniform int mode;\nuniform vec4 color1, color2, color3, color4;\n\nvoid main() {\n    vec2 local = gl_TexCoord[0].st * (size + margin * 2.0) - margin;\n    vec2 extent = size * 0.5;\n    vec2 corner = abs(local - extent) - (extent - radius);\n    float dist = length(max(corner, 0.0)) + min(max(corner.x, corner.y), 0.0) - radius;\n    float coverage;\n    if (softness > 0.0) {\n        coverage = 1.0 - smoothstep(-softness, softness, dist);\n    } else {\n        float pixel = max(fwidth(dist), 0.0001);\n        coverage = clamp(0.5 - dist / pixel, 0.0, 1.0);\n        if (stroke > 0.0) {\n            coverage *= clamp(0.5 + (dist + stroke) / pixel, 0.0, 1.0);\n        }\n    }\n    vec2 uv = clamp((local - inset) / max(size - inset * 2.0, vec2(0.0001)), 0.0, 1.0);\n    vec4 color;\n    if (mode == 1) {\n        color = vec4(clamp(abs(mod(uv.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0), color1.a);\n    } else if (mode == 2) {\n        float parity = mod(floor(local.x / cell) + floor(local.y / cell), 2.0);\n        vec4 base = mix(color1, color2, parity);\n        vec4 over = mix(color3, color4, uv.x);\n        color = vec4(mix(base.rgb, over.rgb, over.a), base.a);\n    } else if (mode == 3) {\n        vec2 offset = local - extent;\n        float turn = fract(atan(offset.y, offset.x) / 6.2831853 + 0.25);\n        color = vec4(clamp(abs(mod(turn * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0), color1.a);\n    } else {\n        color = mix(mix(color1, color2, uv.x), mix(color3, color4, uv.x), uv.y);\n    }\n    gl_FragColor = vec4(color.rgb, color.a * coverage);\n}";
    private final String roundedRectRise = "#version 120\n\nuniform vec2 u_size;\nuniform float u_radius;\nuniform vec4 u_color;\nuniform vec4 u_edges;\n\nvoid main(void)\n{\n    vec2 tex_coord = gl_TexCoord[0].st;\n\n    if (tex_coord.x < 0.5 && tex_coord.y > 0.5 && u_edges.x == 0.0 ||\n        tex_coord.x > 0.5 && tex_coord.y > 0.5 && u_edges.y == 0.0 ||\n        tex_coord.x > 0.5 && tex_coord.y < 0.5 && u_edges.z == 0.0 ||\n        tex_coord.x < 0.5 && tex_coord.y < 0.5 && u_edges.w == 0.0) {\n        gl_FragColor = u_color;\n    } else {\n        gl_FragColor = vec4(u_color.rgb, u_color.a * smoothstep(1.0, 0.0, length(max((abs(tex_coord - 0.5) + 0.5) * u_size - u_size + u_radius, 0.0)) - u_radius + 0.5));\n    }\n}";

    public ShaderUtils(String string, String string2) {
        int n = GL20.glCreateProgram();
        int n2 = 0;
        int n3 = 0;
        boolean bl = false;
        try {
            switch (string) {
                case "kawaseUpGlow": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform bool check;\nuniform float lastPass;\nuniform float exposure;\n\nvoid main() {\n    if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));\n}".getBytes()), 35632);
                    break;
                }
                case "glow": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    innerColor *= weights[0];\n    for (float r = 1.0; r <= radius; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n".getBytes()), 35632);
                    break;
                }
                case "roundRectTexture": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) -size, 0.)) - radius;\n}\n\n\nvoid main() {\n    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}".getBytes()), 35632);
                    break;
                }
                case "roundRectOutline": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat roundedSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n}\n\nvoid main() {\n    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}".getBytes()), 35632);
                    break;
                }
                case "kawaseUpBloom": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec2 offset1 = vec2(-halfpixel.x, 0.0) * offset;\n    vec2 offset2 = vec2(-halfpixel.x, halfpixel.y) * offset;\n    vec2 offset3 = vec2(0.0, halfpixel.y * 2.0) * offset;\n    vec2 offset4 = vec2(halfpixel.x, halfpixel.y) * offset;\n    vec2 offset5 = vec2(halfpixel.x * 2.0, 0.0) * offset;\n    vec2 offset6 = vec2(halfpixel.x, -halfpixel.y) * offset;\n    vec2 offset7 = vec2(0.0, -halfpixel.y * 2.0) * offset;\n    vec2 offset8 = vec2(-halfpixel.x, -halfpixel.y) * offset;\n\n    vec4 sum = texture2D(inTexture, uv + offset1);\n    sum.rgb *= sum.a;\n    vec4 smpl1 = texture2D(inTexture, uv + offset2);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + offset3);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + offset4);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + offset5);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + offset6);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + offset7);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + offset8);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n\n    vec4 result = sum / 12.0;\n    float checkAlpha = texture2D(textureToCheck, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - checkAlpha), float(check)));\n}\n".getBytes()), 35632);
                    break;
                }
                case "kawaseDownBloom": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec4 sum = texture2D(inTexture, uv);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}\n".getBytes()), 35632);
                    break;
                }
                case "kawaseUp": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;\n\n    vec4 average = sum / 12.0;\n    gl_FragColor = vec4(average.rgb, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}".getBytes()), 35632);
                    break;
                }
                case "kawaseDown": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / iResolution;\n\n    vec4 sum = texture2D(inTexture, uv) * 4.0;\n    sum += texture2D(inTexture, uv - halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n\n    gl_FragColor = vec4(sum.rgb * 0.125, 1.0);\n}".getBytes()), 35632);
                    break;
                }
                case "gradientMask": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\n\n#define NOISE .5/255.0\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);\n}".getBytes()), 35632);
                    break;
                }
                case "mask": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D u_texture, u_texture2;\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;\n    vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;\n    gl_FragColor = vec4(tex2Color, texColorAlpha);\n}".getBytes()), 35632);
                    break;
                }
                case "gradient": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n#define NOISE .5/255.0\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    gl_FragColor = createGradient(coords, color1, color2, color3, color4);\n}".getBytes()), 35632);
                    break;
                }
                case "roundedRect": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0.0)) - r;\n}\n\n\nvoid main() {\n    vec2 rectHalf = rectSize * .5;\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\n    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);\n\n}".getBytes()), 35632);
                    break;
                }
                case "roundedRectGradient": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b , 0.0)) - r;\n}\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}".getBytes()), 35632);
                    break;
                }
                case "roundedRectRise": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 u_size;\nuniform float u_radius;\nuniform vec4 u_color;\nuniform vec4 u_edges;\n\nvoid main(void)\n{\n    vec2 tex_coord = gl_TexCoord[0].st;\n\n    if (tex_coord.x < 0.5 && tex_coord.y > 0.5 && u_edges.x == 0.0 ||\n        tex_coord.x > 0.5 && tex_coord.y > 0.5 && u_edges.y == 0.0 ||\n        tex_coord.x > 0.5 && tex_coord.y < 0.5 && u_edges.z == 0.0 ||\n        tex_coord.x < 0.5 && tex_coord.y < 0.5 && u_edges.w == 0.0) {\n        gl_FragColor = u_color;\n    } else {\n        gl_FragColor = vec4(u_color.rgb, u_color.a * smoothstep(1.0, 0.0, length(max((abs(tex_coord - 0.5) + 0.5) * u_size - u_size + u_radius, 0.0)) - u_radius + 0.5));\n    }\n}".getBytes()), 35632);
                    break;
                }
                case "guiShape": {
                    n2 = this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 size, margin;\nuniform float radius, softness, stroke, cell, inset;\nuniform int mode;\nuniform vec4 color1, color2, color3, color4;\n\nvoid main() {\n    vec2 local = gl_TexCoord[0].st * (size + margin * 2.0) - margin;\n    vec2 extent = size * 0.5;\n    vec2 corner = abs(local - extent) - (extent - radius);\n    float dist = length(max(corner, 0.0)) + min(max(corner.x, corner.y), 0.0) - radius;\n    float coverage;\n    if (softness > 0.0) {\n        coverage = 1.0 - smoothstep(-softness, softness, dist);\n    } else {\n        float pixel = max(fwidth(dist), 0.0001);\n        coverage = clamp(0.5 - dist / pixel, 0.0, 1.0);\n        if (stroke > 0.0) {\n            coverage *= clamp(0.5 + (dist + stroke) / pixel, 0.0, 1.0);\n        }\n    }\n    vec2 uv = clamp((local - inset) / max(size - inset * 2.0, vec2(0.0001)), 0.0, 1.0);\n    vec4 color;\n    if (mode == 1) {\n        color = vec4(clamp(abs(mod(uv.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0), color1.a);\n    } else if (mode == 2) {\n        float parity = mod(floor(local.x / cell) + floor(local.y / cell), 2.0);\n        vec4 base = mix(color1, color2, parity);\n        vec4 over = mix(color3, color4, uv.x);\n        color = vec4(mix(base.rgb, over.rgb, over.a), base.a);\n    } else if (mode == 3) {\n        vec2 offset = local - extent;\n        float turn = fract(atan(offset.y, offset.x) / 6.2831853 + 0.25);\n        color = vec4(clamp(abs(mod(turn * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0), color1.a);\n    } else {\n        color = mix(mix(color1, color2, uv.x), mix(color3, color4, uv.x), uv.y);\n    }\n    gl_FragColor = vec4(color.rgb, color.a * coverage);\n}".getBytes()), 35632);
                    break;
                }
                default: {
                    n2 = this.createShader(this.mc.getResourceManager().getResource(new ResourceLocation(string)).getInputStream(), 35632);
                }
            }
            GL20.glAttachShader((int)n, (int)n2);
            n3 = this.createShader(this.mc.getResourceManager().getResource(new ResourceLocation(string2)).getInputStream(), 35633);
            GL20.glAttachShader((int)n, (int)n3);
            GL20.glLinkProgram((int)n);
            if (GL20.glGetProgrami((int)n, (int)35714) == 0) {
                String string3 = GL20.glGetProgramInfoLog((int)n, (int)4096).trim();
                throw new IllegalStateException("Shader failed to link: " + (string3.isEmpty() ? "OpenGL returned no linker log" : string3));
            }
            this.programID = n;
            bl = true;
        }
        catch (IOException iOException) {
            throw new IllegalStateException("Shader resource failed to load", iOException);
        }
        finally {
            if (n2 != 0) {
                GL20.glDetachShader((int)n, (int)n2);
                GL20.glDeleteShader((int)n2);
            }
            if (n3 != 0) {
                GL20.glDetachShader((int)n, (int)n3);
                GL20.glDeleteShader((int)n3);
            }
            if (!bl) {
                GL20.glDeleteProgram((int)n);
            }
        }
    }

    public ShaderUtils(String string) {
        this(string, "minecraft:shaders/vertex.vsh");
    }

    public static void drawQuads() {
        FullscreenQuad.begin();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        float f = (float)scaledResolution.getScaledWidth_double();
        float f2 = (float)scaledResolution.getScaledHeight_double();
        GL11.glBegin((int)7);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex2f((float)0.0f, (float)0.0f);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex2f((float)0.0f, (float)f2);
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex2f((float)f, (float)f2);
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex2f((float)f, (float)0.0f);
        GL11.glEnd();
        FullscreenQuad.end();
    }

    public static void drawQuads(float f, float f2, float f3, float f4) {
        GL11.glBegin((int)7);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex2f((float)f, (float)f2);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex2f((float)f, (float)(f2 + f4));
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex2f((float)(f + f3), (float)(f2 + f4));
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex2f((float)(f + f3), (float)f2);
        GL11.glEnd();
    }

    public void init() {
        GL20.glUseProgram((int)this.programID);
    }

    public void unload() {
        GL20.glUseProgram((int)0);
    }

    public void setUniformf(String string, float ... fArray) {
        int n = this.getUniformLocation(string);
        if (n == -1) {
            return;
        }
        switch (fArray.length) {
            case 1: {
                GL20.glUniform1f((int)n, (float)fArray[0]);
                break;
            }
            case 2: {
                GL20.glUniform2f((int)n, (float)fArray[0], (float)fArray[1]);
                break;
            }
            case 3: {
                GL20.glUniform3f((int)n, (float)fArray[0], (float)fArray[1], (float)fArray[2]);
                break;
            }
            case 4: {
                GL20.glUniform4f((int)n, (float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
            }
        }
    }

    public void setUniformi(String string, int ... nArray) {
        int n = this.getUniformLocation(string);
        if (n == -1) {
            return;
        }
        if (nArray.length > 1) {
            GL20.glUniform2i((int)n, (int)nArray[0], (int)nArray[1]);
        } else {
            GL20.glUniform1i((int)n, (int)nArray[0]);
        }
    }

    private int getUniformLocation(String string) {
        Integer n = this.uniformLocations.get(string);
        if (n != null) {
            return n;
        }
        int n2 = GL20.glGetUniformLocation((int)this.programID, (CharSequence)string);
        this.uniformLocations.put(string, n2);
        return n2;
    }

    private int createShader(InputStream inputStream, int n) {
        int n2 = GL20.glCreateShader((int)n);
        GL20.glShaderSource((int)n2, (CharSequence)Utils.readInputStream(inputStream));
        GL20.glCompileShader((int)n2);
        if (GL20.glGetShaderi((int)n2, (int)35713) == 0) {
            String string = GL20.glGetShaderInfoLog((int)n2, (int)4096).trim();
            GL20.glDeleteShader((int)n2);
            throw new IllegalStateException(String.format("Shader (%s) failed to compile: %s", n, string.isEmpty() ? "OpenGL returned no compiler log" : string));
        }
        return n2;
    }
}

