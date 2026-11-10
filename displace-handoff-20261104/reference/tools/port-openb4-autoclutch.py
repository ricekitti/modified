"""Generate (or verify) Rice's AutoClutch from Sam's encrypted OpenB4 script, on OpenB4-u20's own script runtime.

Inputs (vendored, read-only; nothing from them is executed):
  tools/fixtures/upstream/openb4-scripts/AutoClutch-decrypted.java   decrypted payload: script source + 32-hex trailer
  tools/fixtures/upstream/openb4/xyz/daikirai/...                     OpenB4-u20 sources (script runtime + helpers)
Output: java/src/main/java/dev/rice/client/script/openb4/*.java (generated files listed in OUTPUTS)
        tools/openb4-autoclutch-port-manifest.json

* The script is compiled the way OpenB4's ScriptManager/Script compile it: the payload is split like
  ScriptCrypto.source/trailer, its lines re-joined like ScriptManager.load, then ScriptManager.IMPORTS (one per line),
  the class line, the source and "\\n}". Five declared v122 reliability patches adapt collision prediction and
  airborne release gates (recorded in the manifest). The other differences are the
  package (placed on the first import line so script line numbers stay OpenB4's: source line N is file line N+20) and
  the class name (OpenB4 uses a random 200-character name; the build gives the class that 200-character SourceFile).
* The runtime is OpenB4-u20's: every member the script's dispatch can reach is copied verbatim into a file named after
  its OpenB4 class (ScriptDefaults with its nested API classes, ScriptProtected, ScriptBridge, ScriptInvoker, ScriptEvent,
  ScriptDelegated, ScriptPackets, ScriptEvents, ScriptManager, Script, ScriptCrypto, ScriptUtil, ScriptText, Chat,
  BlockUtils, Defaults). Nothing is rewritten: OpenB4 framework types those members name (Module, ModuleManager,
  Category, settings, Forge event types) are same-package host stand-ins (OpenB4ScriptHost.java and friends), and the
  two Forge-only Item/Block.getRegistryName() calls are bridged after compiling (tools/mayu-adapters.txt), like Mayu's.
  javac proves the closure complete: a reachable member that was not copied does not compile.

usage: python -I tools/port-openb4-autoclutch.py [<OpenB4 src/main/java> <AutoClutch-decrypted.java> to (re)vendor] [--check]
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = ROOT / 'tools/fixtures/upstream/openb4'
SCRIPT = ROOT / 'tools/fixtures/upstream/openb4-scripts/AutoClutch-decrypted.java'
OUT = ROOT / 'java/src/main/java/dev/rice/client/script/openb4'
MANIFEST = ROOT / 'tools/openb4-autoclutch-port-manifest.json'
PACKAGE = 'dev.rice.client.script.openb4'
SCRIPT_CLASS = 'AutoClutchScript'
BS = chr(92)

# Same-package host types standing in for OpenB4 framework classes the copied members name.
STANDINS = {'Module', 'ModuleManager', 'Category', 'Setting', 'ButtonSetting', 'SliderSetting', 'DescriptionSetting',
            'MinecraftForge', 'RenderWorldLastEvent', 'SubscribeEvent', 'PlayerUtils'}

HASHED = '''m351d12ebfcdc1f m51e8a7d3c9bf42 me0ef0d2d6d8947 m273b7e3f39ca1e m8246c06a696131 ma1da5bf03ec1d5 mb6cb1cd3241227
mec665253088a75 m51699116dc0d32 m21eba45aea8060 me3259e23ea00b4 maa00019f32541b m3cb75614ee4976 mcddb23d9d93af2 med53d00cb85603
mef74bbf389a75c me055b71d99fb47 m561fb4b556a781 mc1569e33fa2277 m854902085fd2c2 m3fd9a8cdd0b860 m453a86ec2d6a16 m7e79b326fc6e30
mc15bbb9c75c1ca m184663ac3accc6 ma03258600c2d43 mecc0542efd6108 mf58ad24838894d m847f3ea4155e20 m6eb36c7aa4e10c m6de92a3398625f
m1bbc8a0ce32204 mc9e9651588b2cd mfc369f2f7806b9 mbc99457df9804f me8fce7cd6becd0 m8df4a6c012f52c mb7aa1d5d8e0d32 me0a1c755ccff88
me383a8a14f149b mdc3ab0a92aa4a4 m5eba1266aa2c40'''.split()

WRAPPERS = 'CPacket C08 C0A C0B C0D C0E C0F C01 C02 C03 C07 C09 C10 C13 C16'.split()

# file -> (upstream path, {nested class or '' (top level): 'whole' | [member names] }). Members are selected by name
# (constructors by class name, static blocks by 'static'); every field and static initializer of an included class is
# always copied (they run when the class initialises), so 'skeleton' members need no listing.
OUTPUTS = {
    'ScriptDefaults': ('xyz/daikirai/script/ScriptDefaults.java', {
        '': HASHED + ['getScriptName', 'protected_setupCache', 'protected_resetCache'],
        'modules': ['getSlider', 'getSliderData', 'isEnabled', 'disable', 'enable', 'getButton', 'registerSlider', 'registerButton',
                    'registerDescription'],
        'client': ['getPlayer', 'isMoving', 'isFlying', 'getMotion', 'getStrafe', 'getForward'],
        'keybinds': ['isMouseDown', 'isPressed', 'isKeyDown', 'getKeycode', 'setPressed', 'access$000'],
        'inventory': ['getStackInSlot', 'setSlot'],
        'world': ['getBlockAt'],
        'render': ['getPosition'],
        'gl': ['push', 'pop', 'disableItemLighting', 'lighting', 'texture2d', 'cull', 'blend', 'alpha', 'depth', 'depthMask',
               'lineSmooth', 'resetColor', 'begin', 'end', 'vertex3', 'lineWidth', 'color', 'enable', 'disable'],
        'Entity': ['Entity', 'of', 'protected_of', 'access$100', 'access$300', 'getHurtTime', 'getHeldItem', 'onGround',
                   'getFallDistance', 'getPosition', 'getYaw', 'isSprinting'],
        'ItemStack': 'whole', 'Block': 'whole', 'Vec3': 'whole', **{w: 'whole' for w in WRAPPERS},
    }),
    'ScriptProtected': ('xyz/daikirai/script/ScriptProtected.java', {
        '': ['getSlider', 'isEnabled', 'enable', 'disable', 'getButton', 'registerSlider', 'registerButton', 'registerDescription',
             'getScriptName', 'canInteract', 'invokeBoolean'],
    }),
    'ScriptBridge': ('xyz/daikirai/script/ScriptBridge.java', {
        '': ['ScriptBridge', 'getSlider', 'isEnabled', 'enable', 'disable', 'getButton', 'registerSlider', 'registerButton',
             'registerDescription', 'getScriptName', 'canInteract'],
    }),
    'ScriptEvent': ('xyz/daikirai/script/ScriptEvent.java', {'': 'whole'}),
    'ScriptInvoker': ('xyz/daikirai/script/ScriptInvoker.java', {'': 'whole'}),
    'ScriptDelegated': ('xyz/daikirai/script/ScriptDelegated.java', {'': ['m7a3f19c8e42bd6', 'mf2946c1e83ab75']}),
    'ScriptPackets': ('xyz/daikirai/script/ScriptPackets.java', {'': ['wrapSent']}),
    'ScriptEvents': ('xyz/daikirai/script/ScriptEvents.java', {'': ['preUpdate', 'packetSent', 'onRenderWorld']}),
    'ScriptManager': ('xyz/daikirai/script/ScriptManager.java', {
        '': ['ScriptManager', 'onLoaded', 'onEnable', 'onDisable'],
        'fields': ['BASE_CLASS', 'baseClass', 'scripts', 'classNames', 'pendingSettings', 'events'],
    }),
    'Script': ('xyz/daikirai/script/Script.java', {'': ['Script', 'methodName', 'printErrors', 'reportError', 'deriveNames']}),
    'ScriptCrypto': ('xyz/daikirai/script/ScriptCrypto.java', {
        '': ['ScriptCrypto', 'md5', 'logKey', 'trailer', 'source'],
        'fields': ['LOG_KEY_HEAD'],
    }),
    'ScriptUtil': ('xyz/daikirai/script/ScriptUtil.java', {'': ['ScriptUtil', 'stripNamespace', 'enumByName', 'inGame'], 'fields': []}),
    'ScriptText': ('xyz/daikirai/script/ScriptText.java', {'': ['randomClassName'], 'fields': ['RANDOM']}),
    'Chat': ('xyz/daikirai/client/Chat.java', {'': ['Chat', 'inWorld', 'color', 'strip', 'print']}),
    'BlockUtils': ('xyz/daikirai/util/BlockUtils.java', {'': ['isContainer', 'isInteractable'], 'fields': []}),
    'Defaults': ('xyz/daikirai/script/Defaults.java', {'': 'whole'}),
    'ScriptReflection': ('xyz/daikirai/script/ScriptReflection.java', {'': 'whole'}),
}
VENDORED = sorted({p for p, _ in OUTPUTS.values()} | {'xyz/daikirai/script/ScriptManager.java'})


def skip_literal(text, i):
    quote = text[i]
    i += 1
    while text[i] != quote:
        i += 2 if text[i] == BS else 1
    return i + 1


def skip_noise(text, i):
    """Index past a string/char literal or comment starting at i, or i itself."""
    if text[i] in '"\'':
        return skip_literal(text, i)
    if text.startswith('//', i):
        return text.index('\n', i)
    if text.startswith('/*', i):
        return text.index('*/', i) + 2
    return i


def body_span(text, start):
    """(index of '{', index just past the matching '}') for the first '{' at/after start (literal/comment aware)."""
    i = start
    while text[i] != '{':
        j = skip_noise(text, i)
        i = j if j != i else i + 1
    open_brace = i
    depth = 0
    while True:
        j = skip_noise(text, i)
        if j != i:
            i = j
            continue
        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
            if depth == 0:
                return open_brace, i + 1
        i += 1


def members(text, open_brace, close):
    """Top-level members of the class body text[open_brace+1:close-1] as (kind, name, text-with-leading-lines)."""
    out, i, end = [], open_brace + 1, close - 1
    pending_start = None
    while i < end:
        if text[i] in ' \t\r\n':
            i += 1
            continue
        line_start = text.rfind('\n', 0, i) + 1
        start = pending_start if pending_start is not None else line_start
        if text[i] == '@':
            # annotation (possibly with arguments) belongs to the next member
            pending_start = start
            j = i + 1
            while j < end and (text[j].isalnum() or text[j] in '_.'):
                j += 1
            if text[j] == '(':
                depth = 0
                while True:
                    k = skip_noise(text, j)
                    if k != j:
                        j = k
                        continue
                    depth += (text[j] == '(') - (text[j] == ')')
                    j += 1
                    if depth == 0:
                        break
            i = j
            continue
        # scan to ';' or '{' at this level
        j, paren = i, 0
        while True:
            k = skip_noise(text, j)
            if k != j:
                j = k
                continue
            c = text[j]
            if c == '(':
                paren += 1
            elif c == ')':
                paren -= 1
            elif c == ';' and paren == 0:
                stop = j + 1
                kind = 'field'
                break
            elif c == '{' and paren == 0:
                head = text[i:j]
                if '=' in head:  # array/brace initializer of a field: ends at the following ';'
                    _, after = body_span(text, j)
                    stop = text.index(';', after) + 1
                    kind = 'field'
                else:
                    _, stop = body_span(text, j)
                    kind = 'static' if head.strip() == 'static' else ('class' if re.search(r'\b(class|interface|enum)\b', head) else 'method')
                break
            j += 1
        head = text[i:stop].split('{', 1)[0].split('=', 1)[0]
        if kind == 'field':
            name = re.findall(r'(\w+)\s*(?:=|;|$)', head.strip().rstrip(';'))[-1] if '=' not in text[i:stop] else re.findall(r'(\w+)\s*$', head)[0]
        elif kind == 'class':
            name = re.search(r'\b(?:class|interface|enum)\s+(\w+)', head).group(1)
        elif kind == 'static':
            name = 'static'
        else:
            name = re.search(r'([\w$]+)\s*\(', head).group(1)
        out.append((kind, name, text[start:stop]))
        pending_start = None
        i = stop
    return out


def class_span(text, simple):
    match = re.search(r'^(?:public |final |abstract )*(?:class|interface|enum) ' + simple + r'\b', text, re.M)
    return match.start(), body_span(text, match.start())


def render_class(text, start, span, selection, fields_only=None):
    """Declaration header verbatim + selected members (fields/static blocks always unless fields_only given)."""
    open_brace, close = span
    header = text[start:open_brace + 1]
    indent = re.match(r'[ ]*', text[text.rfind('\n', 0, start) + 1:]).group(0)
    parts, picked = [], []
    for kind, name, member in members(text, open_brace, close):
        keep = selection == 'whole'
        if not keep:
            if kind in ('field', 'static'):
                keep = fields_only is None or name in fields_only
            elif kind == 'method':
                keep = name in selection
            elif kind == 'class':
                keep = False
        if keep:
            parts.append(member)
            picked.append((kind, name))
    body = ''.join('\n' + p.rstrip('\n') if not p.startswith('\n') else p.rstrip('\n') for p in parts)
    return header + body + '\n' + indent + '}', picked


def generate_file(name):
    rel, spec = OUTPUTS[name]
    text = (UPSTREAM / rel).read_text(encoding='utf-8')
    start, span = class_span(text, name)
    picked = {}
    if spec.get('') == 'whole':
        cls = text[start:span[1]]
        picked[''] = 'whole'
    else:
        open_brace, close = span
        header = text[start:open_brace + 1]
        parts = []
        top = spec.get('', [])
        fields_only = spec.get('fields')
        nested_selected = []
        for kind, mname, member in members(text, open_brace, close):
            if kind == 'class' and mname in spec:
                inner_start = start + 0  # recompute inside the member text
                mtext = member
                lead = mtext[:len(mtext) - len(mtext.lstrip('\n'))]
                mt = mtext.lstrip('\n')
                ms = re.search(r'(?:class|interface|enum) ' + mname + r'\b', mt).start()
                ms = mt.rfind('\n', 0, ms) + 1
                sp = body_span(mt, ms)
                rendered, inner = render_class(mt, ms, sp, spec[mname])
                parts.append(lead + mt[:ms] + rendered)
                nested_selected.append(mname)
                picked[mname] = inner if spec[mname] != 'whole' else 'whole'
            elif kind in ('field', 'static'):
                if fields_only is None or mname in fields_only:
                    parts.append(member)
                    picked.setdefault('', []).append((kind, mname))
            elif kind == 'method' and mname in top:
                parts.append(member)
                picked.setdefault('', []).append((kind, mname))
        missing = [n for n in spec if n not in ('', 'fields') and n not in nested_selected]
        missing += [m for m in top if m not in {n for _, n in picked.get('', [])}]
        missing += [f for f in (fields_only or []) if f not in {n for _, n in picked.get('', [])}]
        if missing:
            raise RuntimeError('%s: members not found upstream: %s' % (name, missing))
        cls = header + ''.join('\n' + p.strip('\n') if True else p for p in parts) + '\n}'
    return cls, picked, text


def imports_for(text, body, own):
    keep = []
    used = set(re.findall(r'\b([A-Z][A-Za-z0-9_]*)\b', body))
    for line in re.findall(r'^import ([\w.]+(?:\.\*)?);$', text, re.M):
        simple = line.rsplit('.', 1)[1]
        if line.startswith('xyz.daikirai.') or simple in STANDINS or simple in own:
            continue
        if line.startswith('net.minecraftforge.'):
            continue
        if simple == '*' or simple in used:
            keep.append(line)
    return keep


def script_source():
    """ScriptCrypto.source/trailer on the payload, then ScriptManager.load's line join."""
    payload = SCRIPT.read_text(encoding='utf-8')
    trailer, source = payload[-32:], payload[:-32]
    if not re.fullmatch(r'[0-9a-f]{32}', trailer):
        raise RuntimeError('payload does not end with a 32-hex trailer')
    lines = source.split('\n')
    while lines and lines[-1] == '':
        lines.pop()  # String.split drops trailing empty strings
    return ''.join(line + '\n' for line in lines), trailer


def class_name_200(trailer):
    """Deterministic stand-in for ScriptText.randomClassName(200): 200 of 'I'/'l' from the trailer's md5 chain."""
    out, seed = [], trailer
    while len(out) < 200:
        seed = hashlib.md5(seed.encode()).hexdigest()
        out.extend('Il'[int(c, 16) & 1] for c in seed)
    return ''.join(out[:200])


def generate():
    files, manifest = {}, {'upstream': 'OpenB4-u20 xyz.daikirai (local copy)', 'files': {}}
    own = set(OUTPUTS) | STANDINS | {SCRIPT_CLASS, 'OpenB4ScriptHost', 'OpenB4Forge'}
    for name in OUTPUTS:
        cls, picked, text = generate_file(name)
        imports = imports_for(text, cls, own)
        out = 'package ' + PACKAGE + ';\n\n' + ''.join('import ' + i + ';\n' for i in imports) + '\n' + \
              '// GENERATED by tools/port-openb4-autoclutch.py from OpenB4-u20 ' + OUTPUTS[name][0] + ': members copied verbatim.\n' + cls + '\n'
        files[name + '.java'] = out
        manifest['files'][name + '.java'] = {'upstream': OUTPUTS[name][0], 'upstream_sha256': hashlib.sha256(text.encode()).hexdigest(),
                                             'members': picked}
    source, trailer = script_source()
    imports_text = (UPSTREAM / 'xyz/daikirai/script/ScriptManager.java').read_text(encoding='utf-8')
    block = re.search(r'IMPORTS = Arrays\.asList\((.*?)\);', imports_text, re.S).group(1)
    imports = re.findall(r'"([\w.]+)"', block)
    if len(imports) != 19:
        raise RuntimeError('ScriptManager.IMPORTS drift')
    head = ''.join('import ' + i + ';\n' for i in imports)
    head = 'package ' + PACKAGE + '; ' + head  # package shares line 1 so script line numbers match OpenB4's
    # v122: explicit reliability patches; preserve the decrypted fixture and record every change.
    patches = []
    original_source = source
    for method, replacement in [
        ('fallDistance', '\n    return dev.rice.client.mayu.ClutchGeometry.fallDistance();\n'),
        ('hitboxBlocked', '\n    return dev.rice.client.mayu.ClutchGeometry.blocked(x, y, z, hitboxHalfWidth, hitboxHeight);\n'),
        ('surfaceBelowColumn', '\n    return dev.rice.client.mayu.ClutchGeometry.surface(x, y, z, hitboxHalfWidth);\n'),
    ]:
        match = re.search(r'\b(?:int|boolean|double) ' + method + r'\([^)]*\)\s*\{', source)
        if not match:
            raise RuntimeError('Clutch geometry method drift: ' + method)
        opening = source.index('{', match.start())
        _, end = body_span(source, opening)
        source = source[:opening+1] + replacement + source[end-1:]
        patches.append(method + ': actual Minecraft collision shapes')
    old = '&& clutchOwned && simulateTrajectory(false, 2.0))'
    if source.count(old) != 1:
        raise RuntimeError('Predicted Safe gate drift')
    source = source.replace(old, '&& clutchOwned && player.onGround() && simulateTrajectory(false, 2.0))')
    patches.append('Predicted Safe: retain owned Scaffold until landing instead of releasing on an airborne prediction')
    old = 'falling && (player.onGround() || (dist != -1 && (maxFall <= 0 || dist <= maxFall)))'
    if source.count(old) != 1:
        raise RuntimeError('Clutch landing gate drift')
    source = source.replace(old, 'falling && player.onGround()')
    patches.append('Landing release: a newly predicted block below the player does not end the airborne clutch')
    script = head + 'public class ' + SCRIPT_CLASS + ' extends ' + PACKAGE + '.ScriptDefaults {\n' + source + '\n}'
    files[SCRIPT_CLASS + '.java'] = script
    # OpenB4 names a script after its file (ScriptManager.isValidName allows at most 14 characters): AutoClutch.java.
    name = 'AutoClutch'
    files['AutoClutchPayload.java'] = (
        'package ' + PACKAGE + ';\n\n'
        '/** GENERATED by tools/port-openb4-autoclutch.py: the values OpenB4 ScriptManager.load derives for this script. */\n'
        'final class AutoClutchPayload {\n'
        '   /** Script name (file name without .java). */\n'
        '   static final String NAME = "' + name + '";\n'
        '   /** ScriptCrypto.trailer(payload): seeds Script.deriveNames (encrypted event method names). */\n'
        '   static final String TRAILER = "' + trailer + '";\n'
        '   /** Stand-in for ScriptText.randomClassName(200); the build sets it as AutoClutchScript\'s SourceFile. */\n'
        '   static final String CLASS_NAME = "' + class_name_200(trailer) + '";\n'
        '   /** ScriptCrypto.md5(name + source). */\n'
        '   static final String HASH = "' + hashlib.md5((name + original_source).encode('utf-8')).hexdigest() + '";\n\n'
        '   private AutoClutchPayload() {\n   }\n}\n')
    payload = SCRIPT.read_bytes()
    manifest['script'] = {'payload_sha256': hashlib.sha256(payload).hexdigest(), 'trailer': trailer,
                          'reliability_patches': patches,
                          'upstream_source_sha256': hashlib.sha256(original_source.encode()).hexdigest(),
                          'source_sha256': hashlib.sha256(source.encode()).hexdigest(),
                          'class_name_200': class_name_200(trailer), 'source_lines': source.count('\n')}
    return files, manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', nargs='?', type=Path)
    parser.add_argument('script', nargs='?', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    if args.source:
        if args.check:
            raise RuntimeError('Check uses the vendored reference')
        for rel in VENDORED:
            target = UPSTREAM / rel
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes((args.source / rel).read_bytes())
        SCRIPT.parent.mkdir(parents=True, exist_ok=True)
        SCRIPT.write_bytes(args.script.read_bytes())
    files, manifest = generate()
    for name, data in files.items():
        manifest['files'].setdefault(name, {})['sha256'] = hashlib.sha256(data.encode()).hexdigest()
    if args.check:
        for name, data in files.items():
            if (OUT / name).read_text(encoding='utf-8') != data:
                raise RuntimeError('Port drift: ' + name)
        if json.loads(MANIFEST.read_text(encoding='utf-8')) != json.loads(json.dumps(manifest)):
            raise RuntimeError('Manifest drift')
    else:
        OUT.mkdir(parents=True, exist_ok=True)
        for name, data in files.items():
            (OUT / name).write_text(data, encoding='utf-8', newline='\n')
        MANIFEST.write_text(json.dumps(manifest, indent=1, sort_keys=True) + '\n', encoding='utf-8')
    print('PASS: AutoClutch script with five declared reliability patches (%d lines); %d OpenB4 runtime files of verbatim members'
          % (manifest['script']['source_lines'], len(OUTPUTS)))


if __name__ == '__main__':
    main()
