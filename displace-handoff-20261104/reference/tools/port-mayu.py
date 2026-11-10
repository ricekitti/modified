"""Import/verify Mayu (Downloads/meow, package myau) Scaffold + BedNuker with their full backend. Never executes upstream code.

* COPIES: every upstream class Scaffold and BedNuker can reach (computed from the sources; LongJump, TargetStrafe and
  KillAura are the only Mayu types replaced, by always-disabled host stand-ins) is copied byte for byte except for the
  explicit BedNuker NUKER PATCH source adaptation recorded in the manifest.
* Vanilla Minecraft lacks what Forge/Mixin give the upstream build (EnumFacing.VALUES access, Item.getDigSpeed/
  getToolClasses/getMetadata(ItemStack), ForgeHooks, Mixin accessor interfaces). The sources stay unmodified; the
  build compiles them against tools/MayuBytecode's compile view and then rewrites exactly those call instructions
  (tools/mayu-adapters.json lists every one with its expected count).
* myau/host/MayuMixins.java is GENERATED from the Mayu mixin injectors that drive these modules: each injector body is
  copied verbatim with only the counted rewrites listed in REWRITES below (this./callback plumbing -> static form).

usage: python -I tools/port-mayu.py [<meow src/main/java/myau> to (re)vendor] [--check]
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FIXTURE = ROOT / 'tools/fixtures/upstream/meow/myau'
OUTPUT = ROOT / 'java/src/main/java/myau'
MANIFEST = ROOT / 'tools/mayu-port-manifest.json'
INDEX = FIXTURE.parent / 'myau-index.txt'  # every class file name of the upstream tree (vendored listing)
REFERENCE = FIXTURE.parent / 'classes/myau'  # Mayu's own compiled classes (meow/build/classes/main/myau) of COPIES

COPIES = '''AccessorC14PacketTabComplete AccessorEntity AccessorEntityPlayer AccessorEntityRenderer AccessorGuiChat AccessorMinecraft
AccessorPlayerControllerMP AccessorRenderManager BedESPModule BedNukerModule BeneficialPotionIds BlockClickEvent BlockPlacementTarget
BlockUtils BooleanSetting CancellableEvent ChatColor ChatUtils ColorSetting ColorUtils Command CommandManager DelayedPacket ElapsedTimer
EnchantmentLabel EnchantmentLabels EnchantmentText Event EventBus EventHandler EventListener FloatSetting HUDModule HotbarChangeEvent
IncomingBufferOwner IncomingPacketBuffer InputUtils IntegerSetting ItemUtils KeepSprintModule LeftClickEvent LivingUpdateEvent MathUtils
ModeSetting Module ModuleManager MoveFlyingEvent MovementInputEvent MovementUtils MutableValue NoHitDelayModule OutgoingBufferOwner
OutgoingPacketBuffer PacketActionTracker PacketDelayManager PacketReceiveEvent PacketSendEvent PacketUtils PercentSetting PlayerUtils
PostPlayerUpdateEvent PostTickEvent PreTickEvent PreWalkingUpdateEvent RandomUtils RawPacketReceiveEvent RenderOverlayEvent RenderUtils RenderWorldEvent
RightClickEvent RotationState RotationUpdateEvent RotationUtils SafeWalkEvent Setting SoundUtils VelocityEvent
WorldLoadEvent'''.split()
MIXINS = '''MixinEntity MixinEntityLivingBase MixinEntityPlayerSP MixinMinecraft MixinKeyBinding MixinGuiIngame MixinGuiIngameForge
MixinEntityRenderer MixinNetworkManager MixinForgeHooksClient MixinRenderManager'''.split()
HOST = {'MyauClient', 'LongJumpModule', 'TargetStrafeModule', 'KillAuraModule', 'ScaffoldModule', 'InjectScaffoldView'}  # hand-written in java/src/main/java/myau
UPSTREAM_ONLY_STOP = HOST  # closure computation stops at host types

# (mixin, injector method, kind, self type or None, state class or None)
#   inject: void hook; cancellable: returns true when the callback was cancelled; returnable: returns the value;
#   redirect / modify: same return type as upstream.
INJECTORS = [
    ('MixinEntityPlayerSP', 'beforePlayerUpdate', 'inject', 'EntityPlayerSP', 'MixinEntityPlayerSP'),
    ('MixinEntityPlayerSP', 'afterPlayerUpdate', 'inject', 'EntityPlayerSP', 'MixinEntityPlayerSP'),
    ('MixinEntityPlayerSP', 'applySpoofedRotationBeforeRidingCheck', 'redirect', 'EntityPlayerSP', 'MixinEntityPlayerSP'),
    ('MixinEntityPlayerSP', 'beforeWalkingPlayerUpdate', 'inject', None, None),
    ('MixinEntityPlayerSP', 'beforeLivingUpdate', 'inject', None, None),
    ('MixinEntityPlayerSP', 'afterMovementInputUpdate', 'inject', None, None),
    ('MixinEntityLivingBase', 'modifyJumpYawRadians', 'modify', 'EntityLivingBase', None),
    ('MixinEntityLivingBase', 'redirectMoveFlying', 'redirect', 'EntityLivingBase', None),
    ('MixinEntity', 'beforeSetVelocity', 'cancellable', 'Entity', None),
    ('MixinEntity', 'modifySafeWalkFlag', 'modify', 'Entity', None),
    ('MixinMinecraft', 'afterRunGameLoop', 'inject', None, None),
    ('MixinMinecraft', 'beforeRunTick', 'inject', 'Minecraft', None),
    ('MixinMinecraft', 'afterRunTick', 'inject', 'Minecraft', None),
    ('MixinMinecraft', 'beforeLoadWorld', 'inject', None, None),
    ('MixinMinecraft', 'beforeLeftClick', 'cancellable', 'Minecraft', None),
    ('MixinMinecraft', 'beforeRightClick', 'cancellable', None, None),
    ('MixinMinecraft', 'beforeBlockClick', 'cancellable', 'Minecraft', None),
    ('MixinMinecraft', 'redirectHotbarScroll', 'redirect', None, None),
    ('MixinKeyBinding', 'onIsPressed', 'returnable', 'KeyBinding', None),
    ('MixinGuiIngame', 'redirectCurrentHotbarItem', 'redirect', None, None),
    ('MixinEntityRenderer', 'beforeUpdateCameraAndRender', 'inject', 'EntityRenderer', 'MixinEntityRenderer'),
    ('MixinEntityRenderer', 'afterUpdateCameraAndRender', 'inject', 'EntityRenderer', 'MixinEntityRenderer'),
    ('MixinEntityRenderer', 'beforeUpdateRenderer', 'inject', 'EntityRenderer', 'MixinEntityRenderer'),
    ('MixinEntityRenderer', 'afterUpdateRenderer', 'inject', 'EntityRenderer', 'MixinEntityRenderer'),
    ('MixinGuiIngameForge', 'afterRenderTitle', 'inject', None, None),
    ('MixinNetworkManager', 'onChannelRead', 'cancellable', None, None),
    ('MixinNetworkManager', 'onSendPacket', 'cancellable', None, None),
    ('MixinNetworkManager', 'onSendPacketWithListeners', 'cancellable', None, None),
    ('MixinForgeHooksClient', 'afterRenderWorld', 'inject', None, None),
    ('MixinRenderManager', 'beforeRenderEntityStatic', 'inject', 'RenderManager', 'MixinRenderManager'),
    ('MixinRenderManager', 'afterRenderEntityStatic', 'inject', 'RenderManager', 'MixinRenderManager'),
]
# @Shadow private fields of the target class: read/written through host reflection (Rice's mapping-aware lookup).
PRIVATE_SHADOWS = {
    'MixinEntityPlayerSP': {'lastReportedYaw', 'lastReportedPitch'},
    'MixinMinecraft': {'leftClickCounter'},
    'MixinKeyBinding': {'keyDescription'},
    'MixinEntityRenderer': {'mc'},
}
CALLBACK = re.compile(r',?\s*(?:CallbackInfo|CallbackInfoReturnable<Boolean>) callbackInfo')


def strip_code(text):
    """Comments and string/char literals blanked (same length), for structural scanning."""
    out, i, n = list(text), 0, len(text)
    while i < n:
        if text.startswith('//', i):
            j = text.find('\n', i); j = n if j < 0 else j
            for k in range(i, j): out[k] = ' '
            i = j
        elif text.startswith('/*', i):
            j = text.find('*/', i + 2) + 2
            for k in range(i, j): out[k] = ' ' if text[k] != '\n' else '\n'
            i = j
        elif text[i] in '"\'':
            q, j = text[i], i + 1
            while text[j] != q:
                j += 2 if text[j] == '\\' else 1
            for k in range(i + 1, j): out[k] = ' '
            i = j + 1
        else:
            i += 1
    return ''.join(out)


def closure(roots, seed_text=''):
    """Upstream classes reachable by name from the roots and from seed_text (text level); host types stop the walk.

    Names are resolved against INDEX (every class of the upstream tree), so a reference to a class that was never
    vendored is reported instead of silently ignored."""
    index = INDEX.read_text(encoding='utf-8').split()
    names = {n for n in index if '/' not in n} | {n.split('/')[1] for n in index if n.startswith('mixin/Accessor')}
    seen, todo = set(), list(roots) + [r for r in set(re.findall(r'\b([A-Z][A-Za-z0-9]*)\b', strip_code(seed_text))) if r in names]
    while todo:
        name = todo.pop()
        if name in seen or name in UPSTREAM_ONLY_STOP:
            continue
        seen.add(name)
        path = FIXTURE / (name + '.java') if not name.startswith('Accessor') else FIXTURE / 'mixin' / (name + '.java')
        if not path.exists():
            raise RuntimeError('Reachable upstream class not vendored: ' + name)
        text = path.read_text(encoding='utf-8')
        for ref in set(re.findall(r'\b([A-Z][A-Za-z0-9]*)\b', strip_code(text))):
            if ref != name and ref in names and (ref.startswith('Accessor') is False or 'import myau.mixin.' + ref + ';' in text):
                todo.append(ref)
    return seen


def method_text(source, name):
    """Full declaration (signature + body) of an injector method, without its Mixin annotation."""
    code = strip_code(source)
    match = re.search(r'^   (?:private|public) (?:static )?[\w<>\[\], ?]+ ' + name + r'\(', code, re.M)
    if not match:
        raise RuntimeError('Missing injector ' + name)
    open_brace = code.index('{', match.end())
    depth, i = 1, open_brace + 1
    while depth:
        depth += (code[i] == '{') - (code[i] == '}')
        i += 1
    return source[match.start():i]


def unique_fields(source):
    """@Unique field declarations (verbatim, annotation dropped)."""
    return re.findall(r'^   @Unique\n(   private [^\n]+;)$', source, re.M)


def rewrite_body(mixin, name, kind, self_type, state, decl):
    counts = {}

    def bump(key, n=1):
        counts[key] = counts.get(key, 0) + n

    header, body = decl.split('{', 1)
    body = body[:body.rindex('}')]
    sig = re.match(r'\s*(?:private|public) (static )?([\w<>\[\], ?]+?) ' + name + r'\((.*)\)\s*$', header, re.S)
    ret, params = sig.group(2).strip(), sig.group(3)
    params, removed = CALLBACK.subn('', params)
    bump('callback parameter dropped', removed)
    params = params.strip().strip(',').strip()
    shadows = PRIVATE_SHADOWS.get(mixin, set())
    uniques = set()
    if state:
        uniques = {re.search(r'(\w+)(?: = [^;]+)?;$', d).group(1) for d in unique_fields((FIXTURE / 'mixin' / (mixin + '.java')).read_text(encoding='utf-8'))}

    # private shadow writes: this.f = expr;  ->  MayuShadows.set_f(self, expr);
    def shadow_write(m):
        bump('private @Shadow write -> MayuShadows.set_' + m.group(2))
        return m.group(1) + 'MayuShadows.set_' + m.group(2) + '(self, ' + m.group(3) + ');'
    for field in sorted(shadows):
        body = re.sub(r'^(\s*)this\.(' + field + r') = ([^;]+);', shadow_write, body, flags=re.M)

    def this_ref(m):
        field = m.group(1)
        if field in uniques:
            bump('@Unique field -> per-instance state'); return 'state.' + field
        if field in shadows:
            bump('private @Shadow read -> MayuShadows.get_' + field); return 'MayuShadows.get_' + field + '(self)'
        bump('this. -> self.'); return 'self.' + field
    body = re.sub(r'\bthis\.(\w+)', this_ref, body)
    n = len(re.findall(r'\(Object\)this\b', body)); body = re.sub(r'\(Object\)this\b', '(Object)self', body); bump('(Object)this -> (Object)self', n)
    if re.search(r'\bthis\b', body):
        raise RuntimeError('Unhandled this in ' + mixin + '.' + name)

    if kind == 'cancellable':
        body, c = re.subn(r'callbackInfo\.cancel\(\);', 'cancelled = true;', body); bump('callbackInfo.cancel() -> cancelled = true', c)
        body, r = re.subn(r'\breturn;', 'return cancelled;', body); bump('return; -> return cancelled;', r)
        ret = 'boolean'
        body = '\n      boolean cancelled = false;' + body.rstrip() + '\n\n      return cancelled;\n   '
        bump('cancel flag declared and returned')
    elif kind == 'returnable':
        body, g = re.subn(r'callbackInfo\.getReturnValue\(\)', 'returnValue', body); bump('callbackInfo.getReturnValue() -> returnValue', g)
        body, s = re.subn(r'callbackInfo\.setReturnValue\((\w+)\);', r'returnValue = \1;', body); bump('callbackInfo.setReturnValue(v) -> returnValue = v', s)
        ret = 'boolean'
        params = ('boolean returnValue, ' + params).strip().strip(',').strip()
        body = body.rstrip() + '\n\n      return returnValue;\n   '
        bump('return value threaded through')
    if 'callbackInfo' in body:
        raise RuntimeError('Unhandled callbackInfo in ' + mixin + '.' + name)
    if self_type:
        params = (params + ', ' if params else '') + self_type + ' self'
        bump('target instance parameter added')
    if state and 'state.' in body:
        body = '\n      ' + state + 'State state = ' + state + 'State.of(self);' + body
        bump('per-instance state lookup')
    out = '   public static ' + ret + ' ' + name + '(' + params + ') {' + body + '}'
    return out, counts


def generate_bridge():
    sections, states, manifest = [], [], {}
    available = {}
    for mixin in MIXINS:
        text = (FIXTURE / 'mixin' / (mixin + '.java')).read_text(encoding='utf-8')
        for line in re.findall(r'^import ([\w.]+);$', text, re.M):
            if not line.startswith(('org.spongepowered', 'myau.')):
                available[line.rsplit('.', 1)[1]] = line
    for mixin, name, kind, self_type, state in INJECTORS:
        text = (FIXTURE / 'mixin' / (mixin + '.java')).read_text(encoding='utf-8')
        method = method_text(text, name)
        out, counts = rewrite_body(mixin, name, kind, self_type, state, method)
        sections.append('   // ' + mixin + '.' + name + ' (' + kind + ')\n' + out)
        manifest[mixin + '.' + name] = {'kind': kind, 'upstream_sha256': hashlib.sha256(method.encode()).hexdigest(), 'rewrites': counts}
        if (mixin, name) == ('MixinEntityPlayerSP', 'beforePlayerUpdate'):
            # Rice rotation arbitration (GameHooks.mayuPlayerUpdateHead): when a higher-priority Rice rotation owner holds
            # the frame, the same body runs without posting RotationUpdateEvent, i.e. as upstream with no module rotating.
            post = '         MyauClient.eventBus.post(event);\n'
            if out.count(post) != 1:
                raise RuntimeError('beforePlayerUpdate post line drift')
            variant = out.replace(post, '').replace(' beforePlayerUpdate(', ' beforePlayerUpdateArbitrated(')
            sections.append('   // ' + mixin + '.' + name + ' (inject), RotationUpdateEvent not posted: Rice rotation arbitration\n' + variant)
            manifest[mixin + '.' + name + ' arbitrated'] = {'kind': 'inject', 'derived_from': mixin + '.' + name,
                                                           'rewrites': dict(counts, **{'RotationUpdateEvent post removed': 1})}
    for mixin in sorted({s for _, _, _, _, s in INJECTORS if s}):
        text = (FIXTURE / 'mixin' / (mixin + '.java')).read_text(encoding='utf-8')
        self_type = next(t for m, _, _, t, s in INJECTORS if s == mixin and t)
        fields = unique_fields(text)
        states.append(
            '   /** {@code @Unique} fields of ' + mixin + ', one set per target instance as Mixin keeps them. */\n'
            '   static final class ' + mixin + 'State {\n'
            '      private static final Map<' + self_type + ', ' + mixin + 'State> INSTANCES = new WeakHashMap<>();\n'
            + ''.join('   ' + f + '\n' for f in fields) +
            '\n      static ' + mixin + 'State of(' + self_type + ' self) {\n'
            '         return INSTANCES.computeIfAbsent(self, ignored -> new ' + mixin + 'State());\n'
            '      }\n   }\n')
        manifest[mixin + ' @Unique'] = fields
    used = set(re.findall(r'\b([A-Z][A-Za-z0-9]*)\b', strip_code('\n'.join(sections + states))))
    imports = {available[n] for n in used if n in available}
    imports |= {'java.util.Map', 'java.util.WeakHashMap', 'myau.*', 'myau.mixin.*'}
    head = ('package myau.host;\n\n' + ''.join('import ' + i + ';\n' for i in sorted(imports)) +
            '\n/**\n * GENERATED by tools/port-mayu.py from the Mayu mixin injectors (Downloads/meow myau/mixin). Each method body is the\n'
            ' * upstream injector body with only the rewrites counted in tools/mayu-port-manifest.json. RicePatches calls them at\n'
            ' * the injectors\' own target points. Do not edit; regenerate.\n */\n'
            '@SuppressWarnings({"unchecked", "rawtypes"})\npublic final class MayuMixins {\n   private MayuMixins() {\n   }\n\n')
    return head + '\n\n'.join(sections) + '\n\n' + '\n'.join(states) + '}\n', manifest


def adapt_bed_nuker(data):
    """Apply Rice's explicit third-mode extension without altering the upstream fixture."""
    text = data.decode('utf-8')

    def once(old, new, label):
        nonlocal text
        if text.count(old) != 1:
            raise RuntimeError('BedNuker adaptation drift (%s): expected one match, got %d' % (label, text.count(old)))
        text = text.replace(old, new)

    once('import java.util.concurrent.TimeUnit;\n',
         'import java.util.concurrent.TimeUnit;\nimport dev.rice.client.bednuker.NukerPatchPlanner;\n', 'planner import')
    once('   private boolean diggingInProgress = false;\n   private boolean awaitingSpawnTeleport = false;\n'
         '   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"LEGIT", "SWAP"});',
         '   private boolean diggingInProgress = false;\n   private boolean awaitingSpawnTeleport = false;\n'
         '   private int confirmationTicks = 0;\n   private Block confirmationBlock = null;\n'
         '   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"LEGIT", "SWAP", "NUKER PATCH"});',
         'mode and confirmation state')
    once('      this.interactionLocked = false;\n      this.diggingInProgress = false;\n',
         '      this.interactionLocked = false;\n      this.diggingInProgress = false;\n'
         '      this.confirmationTicks = 0;\n      this.confirmationBlock = null;\n', 'reset confirmation')
    once('   private BlockPos findSurroundingBlock(BlockPos bedPosition) {', '''   private BlockPos findNukerPatchTarget(BlockPos bedPosition) {
      IBlockState bedState = minecraft.theWorld.getBlockState(bedPosition);
      if (!(bedState.getBlock() instanceof BlockBed)) {
         return null;
      }
      EnumPartType part = (EnumPartType)bedState.getValue(BlockBed.PART);
      EnumFacing facing = (EnumFacing)bedState.getValue(BlockBed.FACING);
      BlockPos otherHalf = bedPosition.offset(part == EnumPartType.HEAD ? facing.getOpposite() : facing);
      return NukerPatchPlanner.next(minecraft.theWorld, bedPosition, otherHalf, new NukerPatchPlanner.Model() {
         public double breakTicks(BlockPos position) {
            float speedPerTick = BedNukerModule.this.getBestBreakSpeed(position);
            float threshold = 1.0F - 0.3F * ((float)BedNukerModule.this.speed.getValue().intValue() / 100.0F);
            return speedPerTick <= 0.0F ? Double.POSITIVE_INFINITY : Math.ceil((double)(threshold / speedPerTick));
         }

         public boolean canBreak(BlockPos position) {
            IBlockState state = minecraft.theWorld.getBlockState(position);
            return state.getBlock().getBlockHardness(minecraft.theWorld, position) >= 0.0F
               && (!BedNukerModule.this.toolCheck.getValue() || BedNukerModule.this.hasRequiredTool(state.getBlock()));
         }

         public boolean canTarget(BlockPos position) {
            return PlayerUtils.isBlockWithinReach(position, BedNukerModule.this.range.getValue().doubleValue());
         }
      });
   }

   private BlockPos findSurroundingBlock(BlockPos bedPosition) {''', 'planner bridge')
    once('            BlockPos surroundingBlock = this.findSurroundingBlock(bed);',
         '            if (this.mode.getValue() == 2) {\n'
         '               BlockPos plannedBlock = this.findNukerPatchTarget(bed);\n'
         '               if (plannedBlock != null) {\n                  return plannedBlock;\n               }\n'
         '               continue;\n            }\n\n'
         '            BlockPos surroundingBlock = this.findSurroundingBlock(bed);', 'planner selection')
    once('                  IBlockState currentState = minecraft.theWorld.getBlockState(this.targetBlock);\n'
         '                  Block currentBlock = currentState.getBlock();\n'
         '                  if (currentBlock.getMaterial() != Material.air) {\n'
         '                     minecraft.theWorld.playAuxSFX(2001, this.targetBlock, Block.getStateId(currentState));\n'
         '                     minecraft.theWorld.setBlockToAir(this.targetBlock);\n'
         '                  }\n\n'
         '                  if (currentBlock instanceof BlockBed) {\n                     this.bedBreakTimer.reset();\n                  }\n\n'
         '                  this.breakPhase = 2;\n                  break;\n'
         '               case 2:\n                  this.restoreHotbarSlot();\n                  this.resetBreakState();\n                  break;',
         '''                  IBlockState currentState = minecraft.theWorld.getBlockState(this.targetBlock);
                  Block currentBlock = currentState.getBlock();
                  if (this.mode.getValue() == 2) {
                     this.confirmationBlock = currentBlock;
                     this.confirmationTicks = 0;
                     this.restoreHotbarSlot();
                     this.interactionLocked = false;
                     this.breakPhase = 3;
                     break;
                  }
                  if (currentBlock.getMaterial() != Material.air) {
                     minecraft.theWorld.playAuxSFX(2001, this.targetBlock, Block.getStateId(currentState));
                     minecraft.theWorld.setBlockToAir(this.targetBlock);
                  }

                  if (currentBlock instanceof BlockBed) {
                     this.bedBreakTimer.reset();
                  }

                  this.breakPhase = 2;
                  break;
               case 2:
                  this.restoreHotbarSlot();
                  this.resetBreakState();
                  break;
               case 3:
                  Block confirmedBlock = minecraft.theWorld.getBlockState(this.targetBlock).getBlock();
                  if (confirmedBlock != this.confirmationBlock || ++this.confirmationTicks >= 15) {
                     if (this.confirmationBlock instanceof BlockBed && confirmedBlock != this.confirmationBlock) {
                        this.bedBreakTimer.reset();
                     }
                     this.restoreHotbarSlot();
                     this.resetBreakState();
                  }
                  break;''', 'server confirmation')
    return text.encode('utf-8')


def outputs():
    bridge, bridge_manifest = generate_bridge()
    # Roots: the two modules, the infrastructure MyauClient.initialize registers that they reach, the types of the
    # MyauClient fields upstream code reads (commandManager), and everything the generated injector bodies name.
    reach = closure(['BedNukerModule', 'EventBus', 'ModuleManager', 'IncomingPacketBuffer', 'OutgoingPacketBuffer',
                     'PacketDelayManager', 'PacketActionTracker', 'CommandManager'], bridge)
    if reach - set(COPIES):
        raise RuntimeError('Closure drift: missing %s extra %s' % (sorted(reach - set(COPIES)), sorted(set(COPIES) - reach)))
    result = {}
    for name in COPIES:
        rel = ('mixin/' if name.startswith('Accessor') else '') + name + '.java'
        data = (FIXTURE / rel).read_bytes()
        result[rel] = adapt_bed_nuker(data) if name == 'BedNukerModule' else data
    bridge = bridge.replace('ScaffoldModule', 'InjectScaffoldView')
    result['host/MayuMixins.java'] = bridge.encode('utf-8')
    return result, bridge_manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', nargs='?', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    if args.source:
        if args.check:
            raise RuntimeError('Check uses the vendored reference')
        (FIXTURE / 'mixin').mkdir(parents=True, exist_ok=True)
        INDEX.write_text('\n'.join(sorted([p.stem for p in args.source.glob('*.java')]
                                          + ['mixin/' + p.stem for p in (args.source / 'mixin').glob('*.java')])) + '\n', encoding='utf-8')
        for name in COPIES + MIXINS:
            rel = ('mixin/' if name.startswith(('Accessor', 'Mixin')) else '') + name + '.java'
            (FIXTURE / rel).write_bytes((args.source / rel).read_bytes())
        # Mayu's own compiled classes (meow/build/classes/main) for the build's instruction-level comparison.
        compiled = args.source.parents[3] / 'build/classes/main/myau'
        for old in REFERENCE.rglob('*.class'):
            old.unlink()
        for path in list(compiled.glob('*.class')) + list((compiled / 'mixin').glob('Accessor*.class')):
            if path.stem.split('$')[0] in COPIES:
                target = REFERENCE / path.relative_to(compiled)
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(path.read_bytes())
    expected, bridge_manifest = outputs()
    reference = sorted(p for p in REFERENCE.rglob('*.class') if p.stem.split('$')[0] in COPIES)
    if {p.stem.split('$')[0] for p in reference} != set(COPIES):
        raise RuntimeError('Reference classes do not cover the copied closure')
    manifest = {'upstream': 'Downloads/meow/meow/src/main/java/myau (Myau 1.0.0, MCP stable_22 sources)',
                'host_types': sorted(HOST),
                'source_adaptations': {'BedNukerModule.java': 'Rice NUKER PATCH route planner and server-confirmed break state'},
                'files': {rel: {'sha256': hashlib.sha256(data).hexdigest(),
                                'byte_exact': rel.startswith('host/') is False and data == (FIXTURE / rel).read_bytes()}
                          for rel, data in sorted(expected.items())},
                'reference_classes': {p.relative_to(REFERENCE).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest() for p in reference},
                'bridge': bridge_manifest}
    if args.check:
        for rel, data in expected.items():
            if (OUTPUT / rel).read_bytes() != data:
                raise RuntimeError('Port drift: ' + rel)
        stray = sorted(p.relative_to(OUTPUT).as_posix() for p in OUTPUT.rglob('*.java')
                       if p.relative_to(OUTPUT).as_posix() not in expected and p.stem not in HOST
                       and not p.relative_to(OUTPUT).as_posix().startswith('host/'))
        if stray:
            raise RuntimeError('Unlisted files in myau: ' + ', '.join(stray))
        if json.loads(MANIFEST.read_text(encoding='utf-8')) != manifest:
            raise RuntimeError('Manifest drift')
    else:
        for rel, data in expected.items():
            (OUTPUT / rel).parent.mkdir(parents=True, exist_ok=True)
            (OUTPUT / rel).write_bytes(data)
        MANIFEST.write_text(json.dumps(manifest, indent=1, sort_keys=True) + '\n', encoding='utf-8')
    exact = sum(1 for v in manifest['files'].values() if v['byte_exact'])
    print('PASS: %d Mayu files byte-exact with the vendored upstream; MayuMixins generated from %d injectors with counted rewrites'
          % (exact, len(INJECTORS)))


if __name__ == '__main__':
    main()
