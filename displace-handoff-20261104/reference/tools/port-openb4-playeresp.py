"""Import the local OpenB4 PlayerESP backend; adapt only host APIs and settings."""
from pathlib import Path
import json
import re
import hashlib

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(r'C:\Users\ethan\Downloads\OpenB4-main\openb4\OpenB4\src\main')
JAVA = SOURCE / 'java/xyz/daikirai'
TARGET = ROOT / 'java/src/main/java/dev/rice'
PACKAGE = 'dev.rice.client.render.openb4'
OUT = TARGET / 'client/render/openb4'
OUT.mkdir(parents=True, exist_ok=True)

def methods(text, names):
    result = []
    pattern = r'    (?:public|private|protected) static [^\n]+? (\w+)\([^\n]*\) \{'
    for match in re.finditer(pattern, text):
        if match.group(1) not in names:
            continue
        start, index, depth = match.start(), match.end(), 1
        while depth:
            if text[index] == '{': depth += 1
            if text[index] == '}': depth -= 1
            index += 1
        result.append(text[start:index])
    return '\n\n'.join(result)

# These files retain the original shader programs and blur/outline passes.
for name in ('Shader', 'ShaderSources', 'GlowShader', 'EspShaders'):
    text = (JAVA / ('util/' + name + '.java')).read_text(encoding='utf-8')
    text = text.replace('package xyz.daikirai.util;', 'package ' + PACKAGE + ';')
    (OUT / (name + '.java')).write_text(text, encoding='utf-8')

render = (JAVA / 'util/Render3D.java').read_text(encoding='utf-8')
body = methods(render, {'updateFrustum', 'isVisible', 'setupCamera', 'setupOverlay', 'filledBox',
                       'entityBox', 'barHeight', 'healthBar', 'ring', 'circle'})
body = body.replace('Strings.healthColor(d4)', 'RenderUtils.healthColor(d4)')
header = '''package dev.rice.client.render.openb4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.*;
import net.minecraft.util.AxisAlignedBB;
import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Method;
import java.awt.Color;
import org.lwjgl.opengl.GL11;
/** Rendering primitives imported from OpenB4 Render3D. */
public final class Render3D {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final Frustum frustum = new Frustum();
    private static Method setupCameraTransform;
'''
(OUT / 'Render3D.java').write_text(header + body + '\n}\n', encoding='utf-8')

utils = (JAVA / 'util/RenderUtils.java').read_text(encoding='utf-8')
body = methods(utils, {'rainbow', 'blendInt', 'resetColor', 'rect', 'beginRect', 'rawRect', 'endRect',
                       'quad', 'screenQuad', 'gradientRect'})
strings = (JAVA / 'util/Strings.java').read_text(encoding='utf-8')
body += '\n' + methods(strings, {'healthColor'})
header = '''package dev.rice.client.render.openb4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
/** Only the OpenB4 drawing/color primitives used by PlayerESP. */
public final class RenderUtils {
    public static double scaledWidthD, scaledHeightD;
    public static int scaledWidth, scaledHeight;
    public static void updateResolution() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        scaledWidthD = sr.getScaledWidth_double(); scaledHeightD = sr.getScaledHeight_double();
        scaledWidth = sr.getScaledWidth(); scaledHeight = sr.getScaledHeight();
    }
'''
(OUT / 'RenderUtils.java').write_text(header + body + '\n}\n', encoding='utf-8')

original = (JAVA / 'module/render/PlayerESPModule.java').read_text(encoding='utf-8')
body = original[original.index('    public static boolean shouldRender'):]
body = body.replace('BotCheck.aboveSpawn(e) || BotCheck.isBot(e, true)', '!B4EspHost.allowed(e)')
body = body.replace('.isToggled()', '.isEnabled()')
body = body.replace('Theme.FLARE.first.getRGB()', 'new Color(241, 39, 17).getRGB()')
prefix = '''package dev.rice.module.impl;
import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.render.openb4.*;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
/** OpenB4 PlayerESP backend, with Rice lifecycle/settings adapters. */
public final class PlayerEspModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public enum Type {
        None(0), Outline(-1), Outline_Light_Glow(-2), Outline_Heavy_Glow(-3), Box(1), Shaded(2), _2D(3), Ring(6);
        public final int mode;
        Type(int mode) { this.mode = mode; }
    }
    public static ChoiceSetting typeSetting;
    public static NumberSetting red, green, blue;
    public static BooleanSetting disableInLobby, rainbow, healthBar, redOnDamage, renderSelf, teamColor, showInvis;
    public static Type type = Type.Outline;
    public static int color = 0xFF00FFFF;
    public static boolean shaderPass;
    private static Framebuffer framebuffer;

    public PlayerEspModule() {
        super("player_esp", "Player ESP", "OpenB4 PlayerESP rendering, shaders and options", ModuleCategory.RENDER);
        typeSetting = add(new ChoiceSetting("type", "Type", 1, "None", "Outline", "Outline (Light Glow)", "Outline (Heavy Glow)", "Box", "Shaded", "2D", "Ring"));
        red = add(new NumberSetting("red", "Red", 0, 0, 255, 1));
        green = add(new NumberSetting("green", "Green", 255, 0, 255, 1));
        blue = add(new NumberSetting("blue", "Blue", 255, 0, 255, 1));
        disableInLobby = add(new BooleanSetting("disable_in_lobby", "Disable in lobby", true));
        rainbow = add(new BooleanSetting("rainbow", "Rainbow", false));
        healthBar = add(new BooleanSetting("health_bar", "Health bar", true));
        redOnDamage = add(new BooleanSetting("red_on_damage", "Red on damage", false));
        renderSelf = add(new BooleanSetting("render_self", "Render self", false));
        teamColor = add(new BooleanSetting("team_color", "Team color", false));
        showInvis = add(new BooleanSetting("show_invis", "Show invis", true));
        for (Setting<?> setting : getSettings()) setting.parseAndSet(setting.format());
    }

    @Override public boolean listens(int event) { return event == 6; }

    @Override public String statusText() { return typeSetting.selectedLabel(); }

    @Override public void onEvent(ClientContext context, SemanticEvent event) {
        if (!context.ready() || shaderPass || (disableInLobby.isEnabled() && B4EspHost.inLobby())) return;
        type = Type.values()[typeSetting.index()];
        color = new Color(red.intValue(), green.intValue(), blue.intValue()).getRGB();
        RenderUtils.updateResolution();
        Render3D.updateFrustum(event.timerRenderPartialTicks);
        // Restore state that OpenB4's direct geometry calls change, including shader programs.
        int oldProgram = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM);
        dev.rice.client.hook.GlStateScope scope = dev.rice.client.hook.GlStateScope.open();
        try {
            if (isShader()) renderShader(event.timerRenderPartialTicks);
            for (EntityPlayer player : context.world().playerEntities) {
                if (player.deathTime != 0 || !shouldRender(player)) continue;
                if (player == context.player()) {
                    if (mc.gameSettings.thirdPersonView == 0 || player.isPlayerSleeping()) continue;
                } else if ((!showInvis.isEnabled() && player.isInvisible()) || !Render3D.isVisible(player)) continue;
                render(player, event.timerRenderPartialTicks);
            }
        } finally {
            org.lwjgl.opengl.GL20.glUseProgram(oldProgram);
            scope.close();
        }
    }

    @Override public void onWorldUnload(ClientContext context) {
        shaderPass = false;
        if (framebuffer != null && org.lwjgl.opengl.Display.isCreated()) {
            framebuffer.deleteFramebuffer(); framebuffer = null;
        }
    }
'''
(TARGET / 'module/impl/PlayerEspModule.java').write_text(prefix + body, encoding='utf-8')

# Replace Slinky's old ESP controls rather than leaving stale options in the GUI.
resources = ROOT / 'java/src/main/resources/assets/ricesemantic'
keys = ['type', 'red', 'green', 'blue', 'disable_in_lobby', 'rainbow', 'health_bar',
        'red_on_damage', 'render_self', 'team_color', 'show_invis']
p = resources / 'ModuleUi.json'
layout = json.loads(p.read_text(encoding='utf-8'))
for item in layout['modules']:
    if item['module'] == 'Player ESP':
        item['gates'] = []
        item['variants'] = [{'values': [], 'controls': [{'key': key} for key in keys]}]
p.write_text(json.dumps(layout, indent=1) + '\n', encoding='utf-8')
p = resources / 'WidgetMetadata.json'
metadata = json.loads(p.read_text(encoding='utf-8'))
metadata['Player ESP'] = {}
p.write_text(json.dumps(metadata, indent=1) + '\n', encoding='utf-8')
print('Imported OpenB4 PlayerESP, eight modes, eleven settings, shader and geometry backend.')
