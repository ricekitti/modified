"""Build the Mayu Scaffold from pinned source, without any Rise implementation."""
from pathlib import Path
import re, hashlib, json

ROOT = Path(__file__).resolve().parent
REFERENCE = ROOT.parent / 'scaffold-port/references/mayu-src-latest/src/main/java/myau'
OUT = ROOT / 'build/mayu/dev/rice/mayu'

def method(source, name):
    match = re.search(r'^    (?:public|private|protected) (?:static )?[^\n]+\b' + re.escape(name) + r'\([^\n]*\) \{', source, re.M)
    if not match: raise ValueError('Missing upstream method ' + name)
    end = source.index('{', match.start()) + 1
    depth = 1
    while depth:
        if source[end] == '{': depth += 1
        if source[end] == '}': depth -= 1
        end += 1
    return source[match.start():end]

def prepare():
    OUT.mkdir(parents=True, exist_ok=True)
    lock=json.loads((ROOT/'mayu-source-lock.json').read_text())
    sources = {}
    def original(name):
        path = REFERENCE / (name + '.java')
        text=path.read_text(encoding='utf-8')
        data=text.encode('utf-8')
        blob=hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()
        if blob!=lock['blobs'][name]:raise ValueError('Pinned upstream source differs: '+name)
        sources[name] = hashlib.sha256(data).hexdigest()
        return text
    def write(name, text):
        (OUT / (name + '.java')).write_text(text.replace('package myau;', 'package dev.rice.mayu;'), encoding='utf-8')
    original('mixin/MixinGuiIngame')  # Pinned behavior for the host GUI item-label hook.
    for name in ['BlockUtils','RandomUtils','PacketUtils','BooleanSetting','PercentSetting','ModeSetting','Setting',
                 'RotationUpdateEvent','RotationState','MoveFlyingEvent','MovementInputEvent','LivingUpdateEvent',
                 'SafeWalkEvent','RenderOverlayEvent','LeftClickEvent','RightClickEvent','BlockClickEvent',
                 'HotbarChangeEvent','Event','CancellableEvent','BlockPlacementTarget']:
        write(name, original(name))
    # Retain only Scaffold's dependency closure. Method bodies come from Mayu;
    # unrelated combat/Forge/mixin helpers are deliberately not linked.
    selections = {
        'MovementUtils': ['hasMovementInput','forwardInput','strafeInput','movementYaw','movementYawForInput',
                          'motionYaw','horizontalSpeed','horizontalMagnitude','setSpeedAlongMotion',
                          'setHorizontalMotion','speedPotionLevel','correctMovementForYaw','setMovementInput'],
        'PlayerUtils': ['isUseItemKeyDown','isGameplayKeyDown','isOffsetCollisionFree','hasCollisionBelow','hasCollisionAbove'],
        'RotationUtils': ['nearestEquivalentAngle','clampAngleChange','snapToMouseSensitivity','rotationsFromDelta',
                          'traceBlocks','traceBlocksFrom'],
        'ItemUtils': ['isPlaceableBlockStack','isSuitablePlacementBlock','isHoldingPlaceableBlock'],
    }
    imports = '''import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
'''
    for name, methods in selections.items():
        text = original(name)
        bodies = '\n\n'.join(method(text, m) for m in methods)
        if name == 'MovementUtils':
            # User-requested steady movement: retain the exact input direction rather
            # than jumping between eight key combinations as placement yaw changes.
            bodies = bodies.replace(method(text, 'movementYawForInput'), '''    public static float movementYawForInput(float yaw, float forward, float strafe) {
        float strafeYaw = Compatibility.targetStrafeYaw();
        if (!Float.isNaN(strafeYaw)) return strafeYaw;
        if (forward == 0.0f && strafe == 0.0f) return MathHelper.wrapAngleTo180_float(yaw);
        return MathHelper.wrapAngleTo180_float(yaw - (float)Math.toDegrees(Math.atan2(strafe, forward)));
    }''')
            bodies = bodies.replace(method(text, 'correctMovementForYaw'), '''    public static void correctMovementForYaw(float yaw) {
        MovementInput input = minecraft.thePlayer.movementInput;
        float viewAngle = minecraft.thePlayer.rotationYaw * (float)Math.PI / 180.0f;
        float targetAngle = yaw * (float)Math.PI / 180.0f;
        double viewSin = MathHelper.sin(viewAngle), viewCos = MathHelper.cos(viewAngle);
        double targetSin = MathHelper.sin(targetAngle), targetCos = MathHelper.cos(targetAngle);
        double worldX = input.moveStrafe * viewCos - input.moveForward * viewSin;
        double worldZ = input.moveForward * viewCos + input.moveStrafe * viewSin;
        double magnitude = targetSin * targetSin + targetCos * targetCos;
        float forward = (float)((worldZ * targetCos - worldX * targetSin) / magnitude);
        float strafe = (float)((worldX * targetCos + worldZ * targetSin) / magnitude);
        setMovementInput(Math.abs(forward) < 1.0E-6f ? 0.0f : forward,
                         Math.abs(strafe) < 1.0E-6f ? 0.0f : strafe);
    }''')
        if name == 'PlayerUtils':
            bodies = bodies.replace('InputUtils.isKeyDown(keyCode)', 'Compatibility.isKeyDown(keyCode)')
        if name == 'RotationUtils':
            bodies = bodies.replace('((AccessorEntity)RotationUtils.minecraft.thePlayer).callGetVectorForRotation(pitch, yaw)',
                                    'Compatibility.vectorForRotation(pitch, yaw)')
        write(name, 'package myau;\n' + imports + '\npublic final class '+name+' {\n'
              '    private static final Minecraft minecraft = Minecraft.getMinecraft();\n' + bodies + '\n}\n')
    core = original('ScaffoldModule')
    core = re.sub(r'    @EventHandler(?:\([^\n]*\))?\n', '', core)
    core = core.replace('EnumFacing.VALUES','EnumFacing.values()')
    core = core.replace(method(core, 'canPlaceWithActiveModules'), '''    private boolean canPlaceWithActiveModules() {
        return Compatibility.canPlace();
    }''')
    core = core.replace('        HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);\n'
                        '        float hudScale = ((Float)hud.scale.getValue()).floatValue();',
                        '        float hudScale = Compatibility.hudScale();')
    core = core.replace('((Boolean)hud.shadow.getValue()).booleanValue()', 'Compatibility.hudShadow()')
    write('ScaffoldModule', core)
    upstream=(REFERENCE/'ScaffoldModule.java').read_text()
    unchanged=0
    for name in re.findall(r'^    (?:public|private|protected) (?:static )?[^\n]+\b(\w+)\([^\n]*\) \{',upstream,re.M):
        if name in {'canPlaceWithActiveModules','drawBlockCounter'}:continue
        before=method(upstream,name).replace('EnumFacing.VALUES','EnumFacing.values()')
        if before!=method(core,name):raise AssertionError('Scaffold method behavior changed: '+name)
        unchanged+=1
    (ROOT/'build/mayu-source-manifest.json').write_text(json.dumps({
        'repository':'https://github.com/ricekitti/Mayu-src-latest',
        'commit':'7026f1748845db7593c607c5f936c79f7dc27183',
        'source_sha256': sources,
        'unchanged_scaffold_methods':unchanged,
        'adaptations':['package relocation','event annotations dispatched by adapter',
                       'Scaffold-only helper method closure','native cross-module and HUD compatibility',
                       'Mixin vector accessor replaced with vanilla vector formula',
                       'private EnumFacing.VALUES replaced with public values()',
                       'continuous movement correction and analog movement yaw for requested sway reduction']}, indent=2))
    return list((ROOT/'build/mayu').rglob('*.java'))

if __name__ == '__main__': prepare()
