"""Port the local OpenB4 KillAura, retaining its combat/packet state machine."""
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(r'C:\Users\ethan\Downloads\OpenB4-main\openb4\OpenB4\src\main')
RUN = ROOT / 'build/killaura-v71'
RUN.mkdir(exist_ok=True)
before = {str(p): hashlib.sha256(p.read_bytes()).hexdigest() for p in (ROOT/'java/src/main').rglob('*') if p.is_file()}
if not (RUN/'source-hashes-before.json').exists():
    (RUN/'source-hashes-before.json').write_text(json.dumps(before, indent=2))
src = (SOURCE/'java/xyz/daikirai/module/combat/KillAuraModule.java').read_text()
(RUN/'OpenB4-KillAuraModule.java').write_text(src)

def remove_block(text, marker):
    start = text.index(marker)
    brace = text.index('{', start)
    depth = 1
    end = brace + 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[:start] + text[end:]

src = re.sub(r'import xyz\.[^;]+;\n', '', src)
src = re.sub(r'import net\.minecraftforge\.[^;]+;\n', '', src)
src = src.replace('package xyz.daikirai.module.combat;', '''package dev.rice.client.combat.openb4;

import dev.rice.client.RelationshipService;
import dev.rice.setting.*;
import static dev.rice.client.combat.openb4.B4AuraHost.*;''')
src = src.replace('public class KillAuraModule extends Module', 'public final class OpenB4KillAuraBackend')
src = src.replace('KillAuraModule', 'OpenB4KillAuraBackend').replace('OpenB4OpenB4KillAuraBackendBackend', 'OpenB4KillAuraBackend')
src = src.replace('    @Override\n', '').replace('    @SubscribeEvent\n', '')
src = remove_block(src, '    public OpenB4KillAuraBackend(int key)')
src = remove_block(src, '    public void link()')
src = src.replace('    private static Minecraft mc()', '''    public boolean enabled;
    public OpenB4KillAuraBackend() { instance = this; }
    public boolean isEnabled() { return enabled; }

    private static Minecraft mc()''')
# HUD, IRC, and integrations with modules which only exist in OpenB4 are not
# imported into Rice. All target, attack, rotation and AutoBlock flows remain.
src = re.sub(r'    private static Animation hudFade;\n', '', src)
src = src.replace('        hudFade = null;\n', '')
src = remove_block(src, '        Module targetHud =')
src = src.replace('        Module targetHud =', '') if '        Module targetHud =' in src else src
# The previous removal starts at a declaration and consumes the following if.
src = src.replace('                    if (ModuleManager.enabled("Hit Select") != null) {\n                        HitSelectModule.onUseEntity((C02PacketUseEntity) packet);\n                    }', '')
src = src.replace('                && !ScaffoldModule.active && !AutoBlockinModule.active && AutoLadderModule.target == null && BedAuraModule.target == null', '')
src = src.replace('        return ScaffoldModule.pauseForKillAura() && BedAuraModule.allowsKillAura(attack);', '        return !B4AuraHost.placementBusy();')
src = remove_block(src, '        if (AutoBlockinModule.active || AutoLadderModule.target != null)')
src = remove_block(src, '        if (ScaffoldModule.active && ScaffoldModule.autoSwap.isToggled()')
src = src.replace(' && !ScaffoldModule.active', ' && !B4AuraHost.placementBusy()')
src = src.replace('BedAuraModule.allowKillAura.isToggled()', 'true')
src = src.replace('        if (DisplaceModule.queued) {', '        if (DisplaceModule.queued) {')
while '        if (DisplaceModule.queued)' in src:
    src = remove_block(src, '        if (DisplaceModule.queued)')
for marker in [' else if (AntiKnockbackModule.disableAttackWhileDelay', ' else if (!forced && HitSelectModule.', ' else if (ModuleManager.enabled("Displace")']:
    while marker in src:
        src = remove_block(src, marker)
src = remove_block(src, '                        if (AntiKnockbackModule.disableAttackWhileDelay')
src = remove_block(src, ' else if (HitSelectModule.holdForKillAura')
src = src.replace(' else if (!hitThroughBlocks.isToggled() && (aimPoint', ' if (!hitThroughBlocks.isToggled() && (aimPoint')
src = src.replace('                        Irc.onKillAuraHit();', '').replace('                                Irc.onKillAuraHit();', '')
src = src.replace('xyz.daikirai.module.movement.NoSlowModule.slowdown();', 'B4AuraHost.slowdown();')
src = remove_block(src, '    public void onJoin(EntityJoinWorldEvent e)')
src = remove_block(src, '    public void onMouse(MouseEvent e)')
src = src.replace('    public void onClientTick(TickEvent.ClientTickEvent e)', '    public void onClientTick()')
src = src.replace('        return fov.getDisplay(false);', '        return "None";').replace('        return rotationSetting.getDisplay(false);', '        return rotation.toString();')
src = src.replace('            return target != null && Hypixel.skywars(2)', '            return target != null && Hypixel.skywars(2)')
# Server-specific inventory suppression is deliberately not applied to Rice.
src = remove_block(src, '    private static boolean dropSetSlot(')
src = remove_block(src, '    private static boolean dropWindowItems(')
src = remove_block(src, '    public boolean onReceivePacket(')
src = src.replace('            if (AntiBotModule.ignored(living)) continue;', '            if ((RelationshipService.flags(living) & 2) != 0) continue;')
src = src.replace('DamageTagsModule.isMob(living)', 'B4AuraHost.isMob(living)')
src = src.replace('Hypixel.sameTeam(', 'B4AuraHost.sameTeam(')
src = src.replace('Strings.health((EntityLivingBase) this.entity)', '((EntityLivingBase) this.entity).getHealth()')
src = src.replace('return Minecraft.getMinecraft();', 'return B4AuraHost.context.minecraft();')
src = src.replace('Minecraft.getMinecraft().theWorld', 'B4AuraHost.context.world()')
src = src.replace('    private static final Item[] FREE_SKIP', '''    public static void resetWorld() {
        held.clear(); target = previousTarget = null;
        rotations = interactRotations = null; aimPoint = null;
        aimPoints.clear(); switchTimes.clear();
        blockState = -1; serverBlocking = fakeBlocking = false;
        attackQueued = lagging = returning = resyncSlot = false;
        forcedAttacks = badSwapTicks = noAttackTicks = dualSwaps = lagAccumulator = 0;
        dualAccumulator = 0; currentAps = 0; attackDelay = damageBlockAt = 0;
    }

    private static final Item[] FREE_SKIP''')
# Packet tracking and mouse input are scoped to the aura host, never global.
src = src.replace('mc.objectMouseOver.entityHit == null', '(mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)')
out = ROOT/'java/src/main/java/dev/rice/client/combat/openb4'
out.mkdir(exist_ok=True)
(out/'OpenB4KillAuraBackend.java').write_text(src)

modules = json.loads((SOURCE/'resources/assets/clickgui/modules.json').read_text())
if isinstance(modules, dict): modules = modules['modules']
settings = next(m['settings'] for m in modules if m['name']=='KillAura' and any(s['name']=='APS' for s in m['settings']))
names = dict(zip([s['name'] for s in settings], ['aps','attackOnDamage','autoBlockSetting','autoBlockAps','autoBlockLag','fov','attackRangeSetting','swingRangeSetting','blockRangeSetting','rotationSetting','rotationSmoothing','sortSetting','switchDelay','targets','aimInvis','attackFireballs','attackMobs','attackPlayers','disableInInventory','disableWhileBlocking','disableWhileMining','disableSprintMidair','hitThroughBlocks','ignoreTeammates','manualBlock','prioritizeEnemies','requireMouseDown','silentSwingWhileBlocking','weaponOnly']))
lines=[]
for s in settings:
    name=names[s['name']]
    key=re.sub(r'[^a-z0-9]+','_',s['name'].lower()).strip('_')
    # Keep existing compatible config keys for identity/ranges/modes.
    key={'aps':'target_cps','range_attack':'attack_range','range_swing':'swing_range','aim_invis':'target_invis','hit_through_blocks':'hit_through_walls'}.get(key,key)
    label=json.dumps(s['name'])
    if s['t']=='toggle':
        expr=f'new BooleanSetting("{key}", {label}, {str(s["value"]).lower()})'
        lines.append(f'      OpenB4KillAuraBackend.{name} = new B4AuraHost.ButtonSetting(add({expr}));')
    else:
        if s['t']=='mode':
            expr=f'new ChoiceSetting("{key}", {label}, {int(s["value"])}, '+', '.join(json.dumps(v) for v in s['options'])+')'
        else:
            expr=f'new NumberSetting("{key}", {label}, {s["value"]}, {s["min"]}, {s["max"]}, {s["step"]})'
        lines.append(f'      OpenB4KillAuraBackend.{name} = new B4AuraHost.SliderSetting(add({expr}));')
wrapper='''package dev.rice.module.impl;

import dev.rice.client.*;
import dev.rice.client.combat.openb4.*;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

/** Rice lifecycle/settings adapter for the imported OpenB4 KillAura backend. */
public final class KillAuraModule extends Module {
   private final OpenB4KillAuraBackend backend = new OpenB4KillAuraBackend();
   private float[] published;
   private boolean initialized;
   public KillAuraModule(WeaponsModule weapons) {
      super("killaura", "Kill Aura", "OpenB4 KillAura targeting, rotations, attacks and AutoBlock", ModuleCategory.COMBAT);
      B4AuraHost.weapons = weapons;
SETTINGS
      backend.guiUpdate();
   }
   @Override public int metadataCategory() { return 0; }
   @Override public boolean listens(int type) { return type == 13 || type == 17 || type == 18 || type == 23 || type == 2 || type == SemanticEvent.MOVEMENT_INPUT; }
   @Override protected void onEnable(ClientContext c) {
      B4AuraHost.context = c;
      backend.enabled = true;
      if (c.ready()) { B4AuraHost.reset(); backend.onEnable(); initialized = true; }
   }
   @Override public void onRenderTickStart(ClientContext c) {
      if (c.inputReady()) { B4AuraHost.context = c; backend.guiUpdate(); OpenB4KillAuraBackend.onRenderTick(); }
   }
   @Override public void onEvent(ClientContext c, SemanticEvent e) {
      B4AuraHost.context = c;
      if (!c.ready()) { B4AuraHost.discard(); published = null; initialized = false; return; }
      if (!initialized) { B4AuraHost.reset(); backend.onEnable(); initialized = true; }
      if (e.type == 23) {
         if (!B4AuraHost.replaying() && !B4AuraHost.packet(e.packet)) e.cancelled = true;
      } else if (e.type == 2) {
         if (e.packet instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity)e.packet).getEntityID() == c.player().getEntityId()) {
            long now = System.currentTimeMillis(); backend.onKnockback((S12PacketEntityVelocity)e.packet, now); B4AuraHost.PacketUtils.lastVelocity = now;
         }
      } else if (e.type == SemanticEvent.MOVEMENT_INPUT) {
         backend.onPostPlayerInput();
         if (OpenB4KillAuraBackend.autoBlock.packet && OpenB4KillAuraBackend.serverBlocking) { e.forward *= 0.2F; e.sideways *= 0.2F; }
      } else if (e.type == 17) {
         backend.guiUpdate();
         B4AuraHost.beginTick();
         boolean hadRotation = published != null;
         published = null;
         if (!c.inputReady()) { OpenB4KillAuraBackend.setTarget(null); B4AuraHost.restore(); return; }
         B4AuraHost.RotationManager.lastYaw = hadRotation ? B4AuraHost.RotationManager.serverYaw : c.player().rotationYaw;
         B4AuraHost.RotationManager.lastPitch = hadRotation ? B4AuraHost.RotationManager.serverPitch : c.player().rotationPitch;
         backend.onClientTick(); backend.onPreUpdate();
         float[] r = backend.getRotations(c.player().rotationYaw, c.player().rotationPitch);
         if (r != null && RotationService.claim(500)) {
            published = r;
            if (OpenB4KillAuraBackend.rotation == OpenB4KillAuraBackend.Rotation.Silent) RotationService.retain(c, false, c.player().rotationYaw, c.player().rotationPitch);
            c.player().rotationYaw = r[0]; c.player().rotationPitch = r[1];
            B4AuraHost.RotationManager.serverYaw = r[0]; B4AuraHost.RotationManager.serverPitch = r[1];
         }
      } else if (e.type == 18) {
         backend.onPostMotion(); B4AuraHost.animate();
      } else if (e.type == 13 && published != null) {
         e.active = true; e.overrideRotation = false;
         if (OpenB4KillAuraBackend.rotation == OpenB4KillAuraBackend.Rotation.Lock_view) RotationService.restoreCamera(published[0], published[1]);
      }
   }
   @Override protected void onDisable(ClientContext c) {
      B4AuraHost.context = c;
      backend.enabled = false;
      if (c.ready()) { backend.onDisable(); backend.afterDisable(); B4AuraHost.restore(); }
      B4AuraHost.discard(); published = null; initialized = false;
   }
   @Override public void onWorldUnload(ClientContext c) { B4AuraHost.context = c; B4AuraHost.discard(); published = null; initialized = false; }
   @Override public String statusText() { return backend.getInfo(); }
}
'''.replace('SETTINGS','\n'.join(lines))
(ROOT/'java/src/main/java/dev/rice/module/impl/KillAuraModule.java').write_text(wrapper)
print('Ported OpenB4 KillAura and',len(settings),'settings')
