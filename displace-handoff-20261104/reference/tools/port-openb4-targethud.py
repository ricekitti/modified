"""Generate (or verify) Rice's TargetHUD backend from the local OpenB4-u20 sources.

Input: OpenB4-u20 `src/main/java` (read-only; nothing from it is executed).
Output: java/src/main/java/dev/rice/client/render/openb4/OpenB4TargetHudBackend.java
        tools/openb4-targethud-port-manifest.json

The output is TargetHudModule.java plus every OpenB4 member it reaches, copied verbatim:
- Whole classes: Animation, Theme, Blur, HudEditor, TargetHudEditor.
- Members of RenderUtils, Strings, Overlay, Chat and InfoHudEditor.
Each one goes into a nested class named after its OpenB4 owner, so call sites keep their upstream spelling. The backend
lives next to Rice's earlier verbatim OpenB4 imports (GlowShader, EspShaders, Shader, ShaderSources, Render3D) and
uses them directly. --check re-verifies that those files still equal OpenB4-u20.

OpenB4 framework types the code names are small stand-ins in the hand-written OpenB4TargetHudHost.java: Module,
ModuleManager, settings, Settings/Gui module values, Watermark, Profiles, Backtrack's ghost, Forge's GuiButtonExt and
its events. The stand-in SliderSetting carries OpenB4's formatNumber verbatim, and --check verifies that copy too.

OpenB4 KillAura drives the HUD from KillAuraModule.onRenderTick. Rice's KillAura backend (an earlier port) carries
those upstream lines as onRenderTickTargetHud(). --check verifies them against OpenB4-u20 with the KA_REWRITES
applied.

Only the edits listed in REWRITES / KA_REWRITES are applied. Each one must match exactly the stated number of times,
or generation fails.

usage: python -I tools/port-openb4-targethud.py <OpenB4 src/main/java> [--check]
"""
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'java' / 'src' / 'main' / 'java' / 'dev' / 'rice'
OUT = JAVA / 'client' / 'render' / 'openb4' / 'OpenB4TargetHudBackend.java'
HOST = JAVA / 'client' / 'render' / 'openb4' / 'OpenB4TargetHudHost.java'
KILLAURA = JAVA / 'client' / 'combat' / 'openb4' / 'OpenB4KillAuraBackend.java'
IMPORTED = JAVA / 'client' / 'render' / 'openb4'
MANIFEST = ROOT / 'tools' / 'openb4-targethud-port-manifest.json'
MODULE = 'xyz/daikirai/module/render/TargetHudModule.java'
CLASS_LINE = 'public class TargetHudModule extends Module {\n'
MC = '    private static final Minecraft mc = Minecraft.getMinecraft();'

# Whole upstream classes, nested verbatim: (nested name, upstream file, class header line).
CLASSES = [
    ('Animation', 'xyz/daikirai/util/Animation.java', 'public class Animation {'),
    ('Theme', 'xyz/daikirai/util/Theme.java', 'public enum Theme {'),
    ('Blur', 'xyz/daikirai/util/Blur.java', 'public class Blur {'),
    ('HudEditor', 'xyz/daikirai/gui/HudEditor.java', 'public class HudEditor extends GuiScreen {'),
    ('TargetHudEditor', 'xyz/daikirai/gui/TargetHudEditor.java', 'public class TargetHudEditor extends HudEditor {'),
]

# (nested class, upstream file, [exact field lines or method/block header lines]) in output order.
MEMBERS = [
    ('RenderUtils', 'xyz/daikirai/util/RenderUtils.java', [
        MC,
        '    public static int scaledWidth, scaledWidthD, scaledHeight, scaledHeightD, scaleFactor = 1;',
        '    public static final double[] SIN_0_90 = new double[31], COS_0_90 = new double[31];',
        '    public static final double[] SIN_90_180 = new double[31], COS_90_180 = new double[31];',
        '    static {',
        '    public static void updateResolution() {',
        '    public static int rainbow(long speed, long... offset) {',
        '    public static int blend(Color color, Color color2, double d) {',
        '    public static int withAlpha(int rgb, int alpha) {',
        '    public static int clampAlpha(int n) {',
        '    public static void resetColor() {',
        '    public static void setColor(int n) {',
        '    public static void roundedRect(float x, float y, float x2, float y2, float r, int color) {',
        '    public static void quad(double x, double y, double w, double h) {',
        '    public static void screenQuad() {',
        '    public static int blendInt(int n, int n2, double d) {',
        '    public static void roundedRectBorder(float f, float f2, float f3, float f4, float f5, int n, int n2, int n3) {',
        '    public static void roundedRectGradient(float f, float f2, float f3, float f4, float f5, int n, int n2, int n3, int n4) {',
    ]),
    ('Strings', 'xyz/daikirai/util/Strings.java', [
        MC,
        "    public static final char SECTION = '§';",
        "    public static final char HEART = '❤';",
        '    public static final String RED = "§c", GOLD = "§6", YELLOW = "§e", GREEN = "§a", DARK_GREEN = "§2";',
        '    public static float health(EntityLivingBase e) {',
        '    public static double round(double d, int n) {',
        '    public static void heart(StringBuilder sb) {',
        '    public static String health(double d, double d2) {',
        '    public static String healthDifference(float f, boolean heart) {',
        '    public static int healthColor(double d) {',
    ]),
    ('Overlay', 'xyz/daikirai/client/Overlay.java', [
        MC,
        '    public static boolean canRender() {',
    ]),
    ('Chat', 'xyz/daikirai/client/Chat.java', [
        MC,
        '    public static boolean inWorld() {',
    ]),
    ('InfoHudEditor', 'xyz/daikirai/gui/InfoHudEditor.java', [
        '    public static void rainbowText(String s, char newline, int x, int y, long speed, long lineOffset, boolean shadow, FontRenderer fr) {',
    ]),
]

KA = 'xyz.daikirai.module.combat.KillAuraModule.'

# (scope, old, new, expected count, reason). scope is 'module' or the nested class name.
REWRITES = [
    ('module', CLASS_LINE, 'public final class OpenB4TargetHudBackend extends Module {\n', 1,
     'standalone backend over the OpenB4 Module stand-in; Rice lifecycle lives in module/impl/TargetHudModule'),
    ('module', 'public TargetHudModule(int key) {', 'public OpenB4TargetHudBackend(int key) {', 1, 'class rename (constructor)'),
    ('module', 'xyz.daikirai.util.Animation', 'Animation', 4, 'fully-qualified OpenB4 Animation -> the verbatim nested Animation'),
    ('module', '@net.minecraftforge.fml.common.eventhandler.SubscribeEvent', '@SubscribeEvent', 1,
     'Forge annotation -> host stand-in (Rice dispatches the render-world event itself)'),
    ('module', 'net.minecraftforge.client.event.RenderWorldLastEvent', 'RenderWorldLastEvent', 1, 'Forge event -> host stand-in'),
    ('module', 'xyz.daikirai.module.network.BacktrackModule.ghost', 'BacktrackModule.ghost', 1,
     "OpenB4 Backtrack's ghost entity -> host stand-in (Rice has no OpenB4 Backtrack ghost)"),
    ('module', KA, 'OpenB4KillAuraBackend.', 2, "OpenB4 KillAura is Rice's OpenB4KillAuraBackend (same static target)"),
    ('module', 'xyz.daikirai.util.Render3D.entityBox', 'Render3D.entityBox', 1,
     "fully-qualified OpenB4 Render3D -> Rice's verbatim import in this package"),
    ('Animation', 'public class Animation {', 'public static class Animation {', 1, 'nested'),
    ('Blur', 'public class Blur {', 'public static class Blur {', 1, 'nested'),
    ('HudEditor', 'public class HudEditor extends GuiScreen {', 'public static class HudEditor extends GuiScreen {', 1, 'nested'),
    ('HudEditor', 'xyz.daikirai.client.Profiles.markModified();', 'Profiles.markModified();', 1,
     "OpenB4 profile store -> host stand-in that writes the position into Rice's hidden settings"),
    ('HudEditor', '            this.handleInput();\n', '            OpenB4TargetHudHost.handleInput(this);\n', 1,
     "Rice's deobfuscated GuiScreen.handleInput() lacks the 'throws IOException' declaration, so the verbatim "
     "catch (IOException) would not compile; the host method calls it and declares the exception"),
    ('TargetHudEditor', 'public class TargetHudEditor extends HudEditor {', 'public static class TargetHudEditor extends HudEditor {', 1, 'nested'),
    ('TargetHudEditor', 'TargetHudModule.', 'OpenB4TargetHudBackend.', 14, 'class rename'),
    ('TargetHudEditor', KA, 'OpenB4KillAuraBackend.', 3, "OpenB4 KillAura is Rice's OpenB4KillAuraBackend"),
]

# The TargetHUD block of OpenB4 KillAuraModule.onRenderTick, as Rice's KillAura backend carries it.
KA_FILE = 'xyz/daikirai/module/combat/KillAuraModule.java'
KA_BLOCK_START = '        Module targetHud = ModuleManager.enabled("TargetHUD");\n'
KA_REWRITES = [
    (KA_BLOCK_START, '        OpenB4TargetHudHost.Module targetHud = OpenB4TargetHudHost.ModuleManager.enabled("TargetHUD");\n', 1,
     'OpenB4 module lookup -> TargetHUD host stand-in (non-null while the Rice TargetHUD module is enabled)'),
    ('TargetHudModule.', 'OpenB4TargetHudBackend.', 3, 'class rename'),
    ('Overlay.canRender()', 'OpenB4TargetHudBackend.Overlay.canRender()', 1,
     "the verbatim OpenB4 Overlay.canRender (Rice's KillAura host Overlay has no canRender)"),
]
KA_FIELD = '    private static Animation hudFade;\n'
KA_ENABLE = '        hudFade = null;\n        hudSince = -1L;\n'
KA_SWING = '                hudFade = null;\n                hudSince = System.currentTimeMillis();\n'

# Rice's earlier verbatim imports this backend uses: file -> upstream file (whole file, package line swapped).
IMPORTED_FILES = ['Shader', 'ShaderSources', 'GlowShader', 'EspShaders']
# Members of Rice's earlier partial imports (Render3D, RenderUtils) reached from here.
IMPORTED_MEMBERS = [
    ('Render3D', 'xyz/daikirai/util/Render3D.java', [
        '    public static void entityBox(Entity entity, int n, int n2, float f) {',
        '    public static void filledBox(AxisAlignedBB a, float r, float g, float b, float alpha) {',
    ]),
    ('RenderUtils', 'xyz/daikirai/util/RenderUtils.java', [
        '    public static int rainbow(long speed, long... offset) {',
        '    public static void resetColor() {',
        '    public static void quad(double x, double y, double w, double h) {',
        '    public static void screenQuad() {',
    ]),
]
HOST_MEMBERS = [('xyz/daikirai/module/setting/SliderSetting.java', '    public static String formatNumber(double d) {')]

HEADER = '''package dev.rice.client.render.openb4;

import dev.rice.client.combat.openb4.OpenB4KillAuraBackend;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.BacktrackModule;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.ButtonSetting;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.Category;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.GuiButtonExt;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.GuiModule;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.Module;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.ModuleManager;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.Profiles;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.RenderWorldLastEvent;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.SettingsModule;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.SliderSetting;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.SubscribeEvent;
import dev.rice.client.render.openb4.OpenB4TargetHudHost.Watermark;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * GENERATED by tools/port-openb4-targethud.py from OpenB4-u20 TargetHudModule; do not edit by hand.
 * Every nested class below is copied verbatim from the OpenB4 class it is named after; the whitelisted rewrites and
 * upstream hashes are recorded in tools/openb4-targethud-port-manifest.json.
 */
'''


def skip_literal(text, i):
    """i points at a quote; return the index just past the literal."""
    quote = text[i]
    i += 1
    while text[i] != quote:
        i += 2 if text[i] == '\\' else 1
    return i + 1


def block_end(text, start):
    """Index just past the brace block that opens at the first '{' at or after start (string/char/comment aware)."""
    i = text.index('{', start)
    depth = 0
    while True:
        c = text[i]
        if c in '"\'':
            i = skip_literal(text, i)
            continue
        if text.startswith('//', i):
            i = text.index('\n', i)
            continue
        if text.startswith('/*', i):
            i = text.index('*/', i) + 2
            continue
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1


def extract(text, line, where):
    hits = [k for k in range(len(text)) if text.startswith(line + '\n', k) and (k == 0 or text[k - 1] == '\n')]
    if len(hits) != 1:
        raise SystemExit('%s: expected exactly one "%s", found %d' % (where, line.strip(), len(hits)))
    k = hits[0]
    if not line.rstrip().endswith('{'):
        return line + '\n'
    end = block_end(text, k)
    if '=' in line.split('{', 1)[0]:
        semicolon = text.index(';', end)
        if text[end:semicolon].strip():
            raise SystemExit('%s: unexpected text after the initializer of "%s"' % (where, line.strip()))
        end = semicolon + 1
    return text[k:end] + '\n'


def apply(text, rules, label, applied=None):
    for old, new, count, reason in rules:
        found = text.count(old)
        if found != count:
            raise SystemExit('rewrite in %s expected %d match(es), found %d: %r' % (label, count, found, old[:80]))
        text = text.replace(old, new)
    if applied is not None:
        applied.append(label)
    return text


def scoped(scope):
    return [(o, n, c, r) for s, o, n, c, r in REWRITES if s == scope]


def indent(block):
    return ''.join(('    ' + l if l.strip() else l) for l in block.splitlines(True))


def normalized(text):
    return text.replace('\r\n', '\n')


def read(path):
    return normalized(path.read_bytes().decode('utf-8'))


def generate(src):
    manifest = {'source': 'OpenB4-u20 (local download, read-only)', 'files': {}, 'members': {}, 'rewrites': [],
                'killaura': {}, 'imported': {}, 'host': {}}
    applied = []

    def upstream(path):
        raw = (src / path).read_bytes()
        manifest['files'][path] = hashlib.sha256(raw).hexdigest()
        return raw.decode('utf-8')

    module = upstream(MODULE)
    body = module[module.index(CLASS_LINE):]
    body = apply(body, scoped('module'), 'module', applied).rstrip()
    if not body.endswith('}'):
        raise SystemExit('unexpected module tail')
    out = [HEADER, body[:-1].rstrip() + '\n']
    for nested, path, header in CLASSES:
        text = upstream(path)
        block = extract(text, header, path)
        manifest['members']['%s :: %s' % (path, header)] = hashlib.sha256(block.encode('utf-8')).hexdigest()
        block = apply(block, scoped(nested), nested, applied)
        out.append('\n    // ---- verbatim from %s ----\n%s' % (path, indent(block)))
    for nested, path, lines in MEMBERS:
        text = upstream(path)
        parts = []
        for line in lines:
            member = extract(text, line, path)
            manifest['members']['%s :: %s' % (path, line.strip())] = hashlib.sha256(member.encode('utf-8')).hexdigest()
            parts.append(member)
        block = apply('\n'.join(parts), scoped(nested), nested, applied)
        out.append('\n    // ---- verbatim from %s ----\n    public static final class %s {\n%s    }\n' % (path, nested, indent(block)))
    out.append('}\n')
    for s, old, new, count, reason in REWRITES:
        if s not in applied:
            raise SystemExit('rewrite scope never applied: ' + s)
        manifest['rewrites'].append({'scope': s, 'old': old, 'new': new, 'count': count, 'reason': reason})

    # KillAura's TargetHUD lines (in Rice's hand-maintained KillAura backend).
    ka = upstream(KA_FILE)
    start = ka.index(KA_BLOCK_START)
    end = block_end(ka, start + len(KA_BLOCK_START)) + 1
    block = apply(ka[start:end], [(o, n, c, r) for o, n, c, r in KA_REWRITES], 'killaura')
    method = '    public static void onRenderTickTargetHud() {\n' + block + '    }\n'
    for snippet in (KA_FIELD, KA_ENABLE, KA_SWING):
        if ka.count(snippet) != 1:
            raise SystemExit('KillAura upstream: expected exactly one %r' % snippet)
    manifest['killaura'] = {'onRenderTickTargetHud': hashlib.sha256(method.encode('utf-8')).hexdigest(),
                            'rewrites': [{'old': o, 'new': n, 'count': c, 'reason': r} for o, n, c, r in KA_REWRITES]}
    expected_ka = [method, KA_FIELD, KA_ENABLE, KA_SWING]

    # Rice's earlier verbatim OpenB4 imports that this backend uses.
    imported = []
    for name in IMPORTED_FILES:
        text = normalized(upstream('xyz/daikirai/util/%s.java' % name)).replace('package xyz.daikirai.util;', 'package dev.rice.client.render.openb4;', 1)
        manifest['imported'][name + '.java'] = 'whole file'
        imported.append((IMPORTED / (name + '.java'), text, 'equal'))
    for name, path, lines in IMPORTED_MEMBERS:
        text = normalized(upstream(path))
        for line in lines:
            manifest['imported']['%s.java :: %s' % (name, line.strip())] = 'member'
            imported.append((IMPORTED / (name + '.java'), extract(text, line, path), 'contains'))
    host = []
    for path, line in HOST_MEMBERS:
        member = extract(normalized(upstream(path)), line, path)
        manifest['host']['%s :: %s' % (path, line.strip())] = hashlib.sha256(member.encode('utf-8')).hexdigest()
        host.append(indent(member))
    return ''.join(out), manifest, expected_ka, imported, host


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    text, manifest, expected_ka, imported, host = generate(Path(sys.argv[1]))
    bad = []
    killaura = read(KILLAURA)
    bad += ['%s lacks upstream TargetHUD lines: %r' % (KILLAURA.relative_to(ROOT), s[:60]) for s in expected_ka if killaura.count(s) != 1]
    for path, expected, mode in imported:
        current = read(path)
        if (current != expected) if mode == 'equal' else (expected not in current):
            bad.append('%s differs from OpenB4-u20 (%s)' % (path.relative_to(ROOT), expected.splitlines()[0].strip() if mode == 'contains' else 'whole file'))
    host_text = read(HOST) if HOST.exists() else ''
    bad += ['%s lacks verbatim %r' % (HOST.relative_to(ROOT), m.splitlines()[0].strip()) for m in host if m not in host_text]
    if '--check' in sys.argv:
        if not OUT.exists() or OUT.read_text(encoding='utf-8') != text:
            bad.append(str(OUT.relative_to(ROOT)))
        if not MANIFEST.exists() or json.loads(MANIFEST.read_text(encoding='utf-8')) != manifest:
            bad.append(str(MANIFEST.relative_to(ROOT)))
        if bad:
            raise SystemExit('OpenB4 TargetHUD port drifted from upstream:\n  ' + '\n  '.join(bad))
        print('PASS: OpenB4TargetHudBackend equals OpenB4-u20 TargetHudModule + %d member(s) + %d whitelisted rewrite(s); '
              'KillAura TargetHUD lines, %d imported shader/render file(s)/member(s) and host formatNumber equal OpenB4-u20'
              % (len(manifest['members']), len(REWRITES), len(imported)))
        return
    OUT.write_text(text, encoding='utf-8', newline='\n')
    MANIFEST.write_text(json.dumps(manifest, indent=1, sort_keys=True, ensure_ascii=False) + '\n', encoding='utf-8', newline='\n')
    print('generated', OUT.relative_to(ROOT), 'with', len(manifest['members']), 'members')
    if bad:
        print('still to match (KillAura backend / imports / host):\n  ' + '\n  '.join(bad))
        print('expected KillAura method:\n' + expected_ka[0])


if __name__ == '__main__':
    main()
