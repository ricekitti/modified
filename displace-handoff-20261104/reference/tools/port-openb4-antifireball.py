"""Generate (or verify) Rice's AntiFireball backend from the local OpenB4-u20 sources.

Input: OpenB4-u20 `src/main/java` (read-only; nothing from it is executed).
Output: java/src/main/java/dev/rice/client/combat/openb4/OpenB4AntiFireballBackend.java
        tools/openb4-antifireball-port-manifest.json

The output is AntiFireballModule.java plus every OpenB4 utility member it reaches, copied verbatim. Each utility
member goes into a nested class named after its OpenB4 owner (PlayerUtils, PacketUtils, RotationUtils, ...), so
call sites keep their upstream spelling. Only the edits listed in REWRITES are applied. Each one must match
exactly the stated number of times, or generation fails. The live bindings (settings, camera, scaffold state,
KillAura fake-block state) are in the hand-written OpenB4AntiFireballHost.java.

usage: python -I tools/port-openb4-antifireball.py <OpenB4 src/main/java> [--check]
"""
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'java' / 'src' / 'main' / 'java' / 'dev' / 'rice' / 'client' / 'combat' / 'openb4' / 'OpenB4AntiFireballBackend.java'
MANIFEST = ROOT / 'tools' / 'openb4-antifireball-port-manifest.json'
MODULE = 'xyz/daikirai/module/player/AntiFireballModule.java'
CLASS_LINE = 'public class AntiFireballModule extends Module {\n'

# (nested class, upstream file, [exact field lines or method/enum header lines]) in output order.
MEMBERS = [
    ('PlayerUtils', 'xyz/daikirai/util/PlayerUtils.java', [
        '    private static final Minecraft mc = Minecraft.getMinecraft();',
        '    public static boolean attacking;',
        '    public static void attack(Entity entity, boolean packetSwing) {',
        '    public static void swing(boolean packetOnly, boolean resetEquip) {',
        '    public static boolean isBlocking() {',
        '    public static boolean holdingSword() {',
        '    public static boolean inFov(float fov, double x, double z) {',
        '    public static boolean inFov(float fov, Entity entity) {',
    ]),
    ('PacketUtils', 'xyz/daikirai/util/PacketUtils.java', [
        '    private static final Minecraft mc = Minecraft.getMinecraft();',
        '    public static Packet<?> sending;',
        '    public static boolean sentCloseWindow;',
        '    public static boolean sentOpenInventory;',
        '    public static boolean sentClickWindow;',
        '    public static boolean sentDigging;',
        '    public static boolean sentAnyDigging;',
        '    public static boolean sentBlockPlace;',
        '    public static boolean sentAnyPlacement;',
        '    public static boolean sentInteractAt;',
        '    public static boolean sentUseEntity;',
        '    public static boolean sentAttack;',
        '    public static boolean sentSwing;',
        '    public static int slotChanges;',
        '    public static int serverSlot;',
        '    public static int previousServerSlot;',
        '    public static int lastUse = -1;',
        '    public static void send(Packet<?> packet) {',
        '    public static void track(Packet<?> packet) {',
        '    public static boolean noAttackPlaceOrDig() {',
    ]),
    ('ClickUtils', 'xyz/daikirai/util/ClickUtils.java', [
        '    private static final List<Long> attackClicks = new ArrayList<Long>();',
        '    public static long lastAttackClick;',
        '    public static void onAttackClick() {',
    ]),
    ('ItemUtils', 'xyz/daikirai/util/ItemUtils.java', [
        '    public static boolean isSword(ItemStack itemStack) {',
    ]),
    ('MathUtils', 'xyz/daikirai/util/MathUtils.java', [
        '    public static final Random RANDOM = new Random();',
        '    public static int randomInt(int min, int max) {',
    ]),
    ('RotationManager', 'xyz/daikirai/util/RotationManager.java', [
        '    public static boolean rotating;',
        '    public static boolean wasRotating;',
        '    public static float serverYaw;',
        '    public static float serverPitch;',
        '    public static float lastYaw;',
        '    public static float lastPitch;',
    ]),
    ('RotationUtils', 'xyz/daikirai/util/RotationUtils.java', [
        '    private static final Minecraft mc = Minecraft.getMinecraft();',
        '    public static float lastYawStep, lastPitchStep;',
        '    public static float[] playerRotation() {',
        '    public static float[] currentRotation() {',
        '    public static float[] toEntityFixed(Entity entity, float lastYaw, float lastPitch) {',
        '    public static float[] toEntityBox(Entity entity) {',
        '    public static float[] toBox(Entity entity, AxisAlignedBB box) {',
        '    public static Vec3 aimPoint(Vec3 eyes, AxisAlignedBB box) {',
        '    public static float[] fromDelta(double dx, double dy, double dz) {',
        '    public static float[] fixSensitivity(float yaw, float pitch, float lastYaw, float lastPitch, boolean jitter) {',
        '    public static float[] fixSensitivity(float yaw, float pitch, float lastYaw, float lastPitch) {',
        '    public static float yawTo(double x, double z) {',
        '    public static float yawBetween(double fromX, double fromZ, double toX, double toZ) {',
        '    public static float clampPitch(float pitch) {',
    ]),
    ('Overlay', 'xyz/daikirai/client/Overlay.java', [
        '    private static final Minecraft mc = Minecraft.getMinecraft();',
        '    public static boolean screenOpen() {',
        '    public static boolean ownScreen() {',
    ]),
    ('Chat', 'xyz/daikirai/client/Chat.java', [
        "    public static final char SECTION = '§';",
        '    public static String strip(String s) {',
    ]),
    ('Hypixel', 'xyz/daikirai/client/Hypixel.java', [
        '    private static final Minecraft mc = Minecraft.getMinecraft();',
        '    public enum State {',
        '    public static State state = State.UNKNOWN;',
        '    private static final String HOST = "hypixel.net";',
        '    private static final String LOBBY_MARK = "  " + Chat.SECTION + "8L";',
        "    private static final char BED_MARK = '✓';",
        '    public static String tabHeader() {',
        '    public static boolean isHypixelServer() {',
        '    public static List<String> sidebar() {',
        '    public static State detect() {',
    ]),
]

# (scope, old, new, expected count, reason). scope is 'module' or the nested class name.
REWRITES = [
    ('module', CLASS_LINE, 'public final class OpenB4AntiFireballBackend {\n', 1,
     'standalone backend; Rice lifecycle lives in module/impl/AntiFireballModule'),
    ('module', 'AntiFireballModule', 'OpenB4AntiFireballBackend', 2, 'class rename (instance field, constructor)'),
    ('module', '        super("AntiFireball", Category.player, key);\n', '', 1, 'no OpenB4 Module superclass'),
    ('module', 'public OpenB4AntiFireballBackend(int key) {', 'public OpenB4AntiFireballBackend() {', 1,
     'keybinds are Rice-managed'),
    ('module', '''    public void link() {
        fov = (SliderSetting) ModuleManager.setting(this, "FOV");
        range = (SliderSetting) ModuleManager.setting(this, "Range");
        disableWhileFlying = (ButtonSetting) ModuleManager.setting(this, "Disable while flying");
        disableWhileScaffold = (ButtonSetting) ModuleManager.setting(this, "Disable while scaffold");
        rotateWithBlocks = (ButtonSetting) ModuleManager.setting(this, "Rotate with blocks");
        rotateWithProjectiles = (ButtonSetting) ModuleManager.setting(this, "Rotate with projectiles");
        silentSwing = (ButtonSetting) ModuleManager.setting(this, "Silent swing");
    }

''', '', 1, 'settings are bound by the Rice adapter (same names and defaults as OpenB4 modules.json)'),
    ('module', '    @Override\n', '', 5, 'no superclass'),
    ('module', '    @SubscribeEvent\n', '', 1, 'onJoin is driven by the World.spawnEntityInWorld head hook'),
    ('PlayerUtils', '        Module killAura = ModuleManager.get("KillAura");\n',
     '        OpenB4KillAuraBackend killAura = OpenB4KillAuraBackend.instance;\n', 1,
     "OpenB4's KillAura is Rice's OpenB4KillAuraBackend (same isFakeBlocking)"),
    ('PlayerUtils', '(mc.thePlayer.rotationYaw - RotationUtils.yawTo(x, z))',
     '(OpenB4AntiFireballHost.cameraYaw() - RotationUtils.yawTo(x, z))', 1,
     'OpenB4 rotationYaw is always the camera; Rice keeps the silent yaw there while a rotation is held'),
    ('RotationUtils', '        return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};\n',
     '        return new float[]{OpenB4AntiFireballHost.cameraYaw(), OpenB4AntiFireballHost.cameraPitch()};\n', 2,
     'camera rotation (playerRotation, toBox inside-box case), see above'),
    ('PacketUtils', '            xyz.daikirai.module.dev.DebugAcModule.updateHeldItem(serverSlot);\n', '', 1,
     'OpenB4 developer anticheat module is not part of Rice'),
    ('Overlay', '        return mc.currentScreen instanceof ClickGui || mc.currentScreen instanceof HudEditor;\n',
     '        return OpenB4AntiFireballHost.ownScreen(mc.currentScreen);\n', 1, "Rice's own GUI screens"),
]

HEADER = '''package dev.rice.client.combat.openb4;

import dev.rice.client.combat.openb4.OpenB4AntiFireballHost.ButtonSetting;
import dev.rice.client.combat.openb4.OpenB4AntiFireballHost.EntityJoinWorldEvent;
import dev.rice.client.combat.openb4.OpenB4AntiFireballHost.ScaffoldModule;
import dev.rice.client.combat.openb4.OpenB4AntiFireballHost.SettingsModule;
import dev.rice.client.combat.openb4.OpenB4AntiFireballHost.SliderSetting;
import dev.rice.inject.ReflectionHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * GENERATED by tools/port-openb4-antifireball.py from OpenB4-u20 AntiFireballModule; do not edit by hand.
 * Every utility member below is copied verbatim from the OpenB4 class its nested class is named after; the
 * whitelisted rewrites and upstream hashes are recorded in tools/openb4-antifireball-port-manifest.json.
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
    if line.rstrip().endswith('{'):
        return text[k:block_end(text, k)] + '\n'
    return line + '\n'


def rewrite(text, scope, applied):
    for s, old, new, count, reason in REWRITES:
        if s != scope:
            continue
        found = text.count(old)
        if found != count:
            raise SystemExit('rewrite in %s expected %d match(es), found %d: %r' % (scope, count, found, old[:80]))
        text = text.replace(old, new)
        applied.append(s)
    return text


def indent(block):
    return ''.join(('    ' + l if l.strip() else l) for l in block.splitlines(True))


def generate(src):
    manifest = {'source': 'OpenB4-u20 (local download, read-only)', 'files': {}, 'members': {}, 'rewrites': []}
    applied = []
    raw = (src / MODULE).read_bytes()
    manifest['files'][MODULE] = hashlib.sha256(raw).hexdigest()
    module = raw.decode('utf-8')
    body = module[module.index(CLASS_LINE):]
    body = rewrite(body, 'module', applied)
    body = body.rstrip()
    if not body.endswith('}'):
        raise SystemExit('unexpected module tail')
    out = [HEADER, body[:-1].rstrip() + '\n']
    for nested, path, lines in MEMBERS:
        raw = (src / path).read_bytes()
        manifest['files'][path] = hashlib.sha256(raw).hexdigest()
        text = raw.decode('utf-8')
        parts = []
        for line in lines:
            member = extract(text, line, path)
            manifest['members']['%s :: %s' % (path, line.strip())] = hashlib.sha256(member.encode('utf-8')).hexdigest()
            parts.append(member)
        block = rewrite('\n'.join(parts), nested, applied)
        out.append('\n    // ---- verbatim from %s ----\n    public static final class %s {\n%s    }\n' % (path, nested, indent(block)))
    out.append('}\n')
    for s, old, new, count, reason in REWRITES:
        if s not in applied:
            raise SystemExit('rewrite scope never applied: ' + s)
        manifest['rewrites'].append({'scope': s, 'old': old, 'new': new, 'count': count, 'reason': reason})
    return ''.join(out), manifest


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    text, manifest = generate(Path(sys.argv[1]))
    if '--check' in sys.argv:
        bad = []
        if not OUT.exists() or OUT.read_text(encoding='utf-8') != text:
            bad.append(str(OUT.relative_to(ROOT)))
        if not MANIFEST.exists() or json.loads(MANIFEST.read_text(encoding='utf-8')) != manifest:
            bad.append(str(MANIFEST.relative_to(ROOT)))
        if bad:
            raise SystemExit('OpenB4 AntiFireball port drifted from upstream:\n  ' + '\n  '.join(bad))
        print('PASS: OpenB4AntiFireballBackend equals OpenB4-u20 AntiFireballModule + %d member(s) + %d whitelisted rewrite(s)'
              % (len(manifest['members']), len(REWRITES)))
        return
    OUT.write_text(text, encoding='utf-8', newline='\n')
    MANIFEST.write_text(json.dumps(manifest, indent=1, sort_keys=True, ensure_ascii=False) + '\n', encoding='utf-8', newline='\n')
    print('generated', OUT.relative_to(ROOT), 'with', len(manifest['members']), 'members')


if __name__ == '__main__':
    main()
