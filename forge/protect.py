"""Use the supplied ZKM tool without modifying it. Keep logs/mappings private."""
from pathlib import Path
import hashlib
import json
import re
import zipfile


def string_constants(data):
    """Read CONSTANT_String values, excluding class/member metadata names."""
    index, count, values, strings = 10, int.from_bytes(data[8:10], 'big'), {}, []
    slot = 1
    while slot < count:
        tag = data[index]; index += 1
        if tag == 1:
            size = int.from_bytes(data[index:index+2], 'big'); index += 2
            values[slot] = data[index:index+size].decode('utf-8', errors='replace'); index += size
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18): index += 4
        elif tag in (5, 6): index += 8; slot += 1
        elif tag == 8:
            strings.append(int.from_bytes(data[index:index+2], 'big')); index += 2
        elif tag in (7, 16, 19, 20): index += 2
        elif tag == 15: index += 3
        else: raise RuntimeError('Unsupported class constant-pool tag: ' + str(tag))
        slot += 1
    # Continue into the class member table so only actual method names can
    # become method exclusions (configuration words like "false" are not names).
    index += 6
    interfaces = int.from_bytes(data[index:index+2], 'big'); index += 2 + interfaces * 2
    def members(position):
        count = int.from_bytes(data[position:position+2], 'big'); position += 2
        names = set()
        for _ in range(count):
            names.add(values[int.from_bytes(data[position+2:position+4], 'big')])
            attributes = int.from_bytes(data[position+6:position+8], 'big'); position += 8
            for _ in range(attributes):
                size = int.from_bytes(data[position+2:position+6], 'big'); position += 6 + size
        return position, names
    index, _ = members(index)
    _, methods = members(index)
    return [values[number] for number in strings], methods


def zquote(path):
    return '"' + str(path).replace('\\', '/').replace('"', '""') + '"'


def rewrite_utf8_constants(data, replacements, string_only=False):
    """Rewrite class-file UTF-8 constants while preserving every non-string byte."""
    if data[:4] != b'\xca\xfe\xba\xbe':
        return data
    count = int.from_bytes(data[8:10], 'big')
    allowed = None
    if string_only:
        allowed, cursor, scan_slot = set(), 10, 1
        while scan_slot < count:
            tag = data[cursor]; cursor += 1
            if tag == 1:
                size = int.from_bytes(data[cursor:cursor + 2], 'big'); cursor += 2 + size
            else:
                sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
                         11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
                if tag == 8:
                    allowed.add(int.from_bytes(data[cursor:cursor + 2], 'big'))
                cursor += sizes[tag]
                if tag in (5, 6): scan_slot += 1
            scan_slot += 1
    source_index, slot = 10, 1
    output = bytearray(data[:10])
    while slot < count:
        tag = data[source_index]
        source_index += 1
        output.append(tag)
        if tag == 1:
            size = int.from_bytes(data[source_index:source_index + 2], 'big')
            source_index += 2
            value = data[source_index:source_index + size]
            source_index += size
            if allowed is None or slot in allowed:
                for old, new in replacements:
                    value = value.replace(old.encode('ascii'), new.encode('ascii'))
            if len(value) > 65535:
                raise RuntimeError('Rewritten class constant is too long')
            output += len(value).to_bytes(2, 'big') + value
        else:
            sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
                     11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
            if tag not in sizes:
                raise RuntimeError('Unsupported class constant-pool tag: ' + str(tag))
            size = sizes[tag]
            output += data[source_index:source_index + size]
            source_index += size
            if tag in (5, 6):
                slot += 1
        slot += 1
    output += data[source_index:]
    return bytes(output)


def randomize_resources(source, private):
    """Flatten private assets into one per-build namespace and patch all lookups."""
    token = private.name.replace('-', '')
    asset_root = 'a' + token[:16]
    class_root = 'c' + token[:16]
    replacements = []
    renamed = {}
    internal_resources = {}
    with zipfile.ZipFile(source) as jar:
        rows = {name: jar.read(name) for name in jar.namelist() if not name.endswith('/')}
    for name in rows:
        if not name.endswith('.class') and name.startswith('dev/rice/'):
            digest = hashlib.sha256((private.name + ':resource:' + name).encode()).hexdigest()[:20]
            target = class_root + '/R' + digest + '.bin'
            internal_resources[name] = target
            replacements += [('/' + name, '/' + target), (name, target),
                             (name.rsplit('/', 1)[-1], target.rsplit('/', 1)[-1])]
        elif name.startswith(('assets/ricesemantic/', 'assets/riceopenjade/')):
            digest = hashlib.sha256((private.name + ':' + name).encode()).hexdigest()[:16]
            target = 'assets/' + asset_root + '/r' + digest + '.bin'
            replacements.append(('/' + name, '/' + target))
            # Resource loaders assemble paths from a private directory plus a
            # font/shader filename. Rewrite those fragments as well as full paths.
            relative = name.split('/', 3)[3] if name.count('/') >= 3 else name.rsplit('/', 1)[-1]
            replacements.append((relative, target.rsplit('/', 1)[-1]))
            replacements.append((name.rsplit('/', 1)[-1], target.rsplit('/', 1)[-1]))
        else:
            target = name
        renamed[name] = target
    replacements += [
        ('/assets/ricesemantic/fonts/', '/assets/' + asset_root + '/'),
        ('/assets/ricesemantic/', '/assets/' + asset_root + '/'),
        ('/assets/riceopenjade/fonts/', '/assets/' + asset_root + '/'),
        ('/assets/riceopenjade/jade/', '/assets/' + asset_root + '/'),
    ]
    replacements.sort(key=lambda item: len(item[0]), reverse=True)
    stable_exact = {
        'dev/rice/RiceMod', 'dev/rice/forge/RiceLoadingPlugin', 'dev/rice/forge/RiceTransformer',
        'dev/rice/forge/ForgeEnvironment', 'dev/rice/forge/Hierarchy', 'dev/rice/inject/Mappings',
        'dev/rice/client/hook/GameHooks', 'dev/rice/client/hook/MayuHooks',
        'dev/rice/client/hook/RenderHooks', 'dev/rice/client/hook/HookLifecycle',
        'dev/rice/client/ClientEventBridge', 'dev/rice/client/SemanticEvent',
        'dev/rice/client/anticheat/SnowifyAntiCheatHost', 'dev/rice/client/blockin/SuppliedBlockInHost',
        'dev/rice/module/Module', 'dev/rice/module/ModuleManager', 'dev/rice/setting/Setting',
        'dev/rice/client/render/GuiText', 'dev/rice/client/render/HudLayout',
        'dev/rice/client/render/HudEditorScreen'}
    stable_prefixes = ('dev/rice/security/', 'net/i2p/crypto/eddsa/',
                       'dev/rice/inject/transform/RicePatches', 'dev/rice/client/render/DrawPlan')
    stable_names = [name[:-6] for name in rows if name.endswith('.class') and
                    (name[:-6] in stable_exact or name[:-6].startswith(stable_prefixes))]
    stable_replacements = []
    for old in stable_names:
        new = class_root + '/C' + hashlib.sha256(
            (private.name + ':class:' + old).encode()).hexdigest()[:20]
        stable_replacements += [(old, new), (old.replace('/', '.'), new.replace('/', '.'))]
    stable_replacements.sort(key=lambda item: len(item[0]), reverse=True)
    staged = private / 'randomized-resources.jar'
    with zipfile.ZipFile(staged, 'w', zipfile.ZIP_DEFLATED) as output:
        for old, data in sorted(rows.items()):
            if old.endswith('.class'):
                data = rewrite_utf8_constants(data, replacements)
                data = rewrite_utf8_constants(data, stable_replacements, string_only=True)
            output.writestr(renamed[old], data)
    changed = sum(old != new for old, new in renamed.items())
    return staged, {'renamed_resources': changed,
                    'asset_namespaces': [asset_root]}, internal_resources


def flatten_class_layout(source, private, internal_resources):
    """Relocate every bundled class into one opaque package after ZKM."""
    token = private.name.replace('-', '')
    package = 'c' + token[:16]
    with zipfile.ZipFile(source) as jar:
        rows = {name: jar.read(name) for name in jar.namelist() if not name.endswith('/')}
    old_classes = sorted(name[:-6] for name in rows if name.endswith('.class'))
    class_map = {}
    for old in old_classes:
        digest = hashlib.sha256((private.name + ':class:' + old).encode()).hexdigest()[:20]
        class_map[old] = package + '/C' + digest
    if len(set(class_map.values())) != len(class_map):
        raise RuntimeError('Opaque class-name collision')
    replacements = []
    for old, new in class_map.items():
        replacements += [(old, new), (old.replace('/', '.'), new.replace('/', '.'))]
    # Forge reads this package prefix from an annotation rather than a class ref.
    replacements += [('dev.rice.', package.replace('/', '.') + '.'),
                     ('dev/rice/', package + '/')]
    resource_map = {}
    for old, new in internal_resources.items():
        replacements += [('/' + old, '/' + new), (old, new),
                         (old.rsplit('/', 1)[-1], new.rsplit('/', 1)[-1])]
    for old in rows:
        if not old.endswith('.class') and old.startswith('dev/rice/'):
            digest = hashlib.sha256((private.name + ':resource:' + old).encode()).hexdigest()[:20]
            new = package + '/R' + digest + '.bin'
            resource_map[old] = new
            replacements += [('/' + old, '/' + new), (old, new),
                             (old.rsplit('/', 1)[-1], new.rsplit('/', 1)[-1])]
    # The access transformer is discovered by its manifest value.
    if 'META-INF/rice_at.cfg' in rows:
        at_name = 'a' + token[28:32] + '.bin'
        resource_map['META-INF/rice_at.cfg'] = 'META-INF/' + at_name
        replacements.append(('rice_at.cfg', at_name))
    replacements.sort(key=lambda item: len(item[0]), reverse=True)
    target = private / 'opaque-layout.jar'
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as output:
        for old, data in sorted(rows.items()):
            if old.endswith('.class'):
                data = rewrite_utf8_constants(data, replacements)
                new = class_map[old[:-6]] + '.class'
            else:
                new = resource_map.get(old, old)
                if old == 'META-INF/MANIFEST.MF':
                    text = data.decode('utf-8')
                    for before, after in replacements:
                        text = text.replace(before, after)
                    data = text.encode('utf-8')
            output.writestr(new, data)
    return target, {'opaque_class_package': package,
                    'relocated_classes': len(class_map),
                    'class_map': class_map,
                    'replacements': replacements}


def relocate_verifiers(directory, layout, private):
    """Patch the verification harness to address the opaque release layout."""
    rows = {path.relative_to(directory).as_posix(): path.read_bytes()
            for path in directory.rglob('*.class')}
    package = layout['opaque_class_package']
    tool_map = {}
    for name in rows:
        internal = name[:-6]
        if internal.startswith('dev/rice/security/'):
            digest = hashlib.sha256((private.name + ':test:' + internal).encode()).hexdigest()[:20]
            tool_map[internal] = package + '/T' + digest
    replacements = list(layout['replacements'])
    for old, new in tool_map.items():
        replacements += [(old, new), (old.replace('/', '.'), new.replace('/', '.'))]
    replacements.sort(key=lambda item: len(item[0]), reverse=True)
    target = private / 'relocated-verifiers.jar'
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as output:
        for old, data in sorted(rows.items()):
            data = rewrite_utf8_constants(data, replacements)
            internal = old[:-6]
            output.writestr(tool_map.get(internal, internal) + '.class', data)
    security_test = tool_map['dev/rice/security/SecurityTest'].replace('/', '.')
    return target, security_test


def obfuscate(zkm, java, source, dependencies, private, run):
    if not zkm.is_file():
        raise RuntimeError('ZKM JAR not found. Supply --zkm; no unprotected release will be published.')
    source, resource_protection, internal_resources = randomize_resources(source, private)
    keep = {'dev.rice.RiceMod',
            'dev.rice.forge.RiceLoadingPlugin', 'dev.rice.forge.RiceTransformer', 'dev.rice.forge.ForgeEnvironment',
            'dev.rice.forge.Hierarchy', 'dev.rice.inject.Mappings', 'dev.rice.inject.transform.RicePatches*',
            'dev.rice.client.hook.GameHooks', 'dev.rice.client.hook.MayuHooks',
            'dev.rice.client.hook.RenderHooks', 'dev.rice.client.hook.HookLifecycle',
            'dev.rice.client.ClientEventBridge', 'dev.rice.client.SemanticEvent',
            'dev.rice.client.anticheat.SnowifyAntiCheatHost', 'dev.rice.client.blockin.SuppliedBlockInHost',
            'dev.rice.security.*', 'net.i2p.crypto.eddsa.*', 'net.i2p.crypto.eddsa.spec.*',
            'net.i2p.crypto.eddsa.math.*', 'net.i2p.crypto.eddsa.math.bigint.*',
            'net.i2p.crypto.eddsa.math.ed25519.*',
            'dev.rice.module.Module', 'dev.rice.module.ModuleManager',
            'dev.rice.setting.Setting',
            'dev.rice.client.render.GuiText', 'dev.rice.client.render.DrawPlan*',
            'dev.rice.client.render.HudLayout', 'dev.rice.client.render.HudEditorScreen'}
    # Keep names used through class-name strings, and all fields: several legacy
    # adapters intentionally read fields by name through generic reflection.
    reflection_methods = set()
    declared_methods = set()
    with zipfile.ZipFile(source) as jar:
        names = {n[:-6].replace('/', '.') for n in jar.namelist() if n.endswith('.class')}
        before = {n: jar.read(n) for n in jar.namelist() if n.endswith('.class')}
        for data in before.values():
            strings, methods = string_constants(data)
            declared_methods.update(methods)
            for text in strings:
                candidate = text.replace('/', '.')
                if candidate in names: keep.add(candidate)
                if re.fullmatch(r'[A-Za-z_$][A-Za-z0-9_$]{0,100}', text): reflection_methods.add(text)
    # Field names remain stable for legacy reflection adapters. Everything else
    # is renamed unless it belongs to the small Forge/hook ABI above.
    excluded = ['exclude *.* *;', 'exclude *.* public *(*) and *.* protected *(*);']
    keep_packages = set()
    for name in keep:
        base = name[:-2] if name.endswith('.*') else name
        if '.' in base:
            keep_packages.add(base.rsplit('.', 1)[0] if not name.endswith('.*') else base)
    excluded += ['exclude ' + name + '.;' for name in sorted(keep_packages)]
    for name in sorted(keep):
        excluded += ['exclude ' + name + ';', 'exclude ' + name + ' *;', 'exclude ' + name + ' *(*);']
    excluded += ['exclude *.* ' + name + '(*);' for name in sorted(reflection_methods & declared_methods)]
    out = private / 'obfuscated'; out.mkdir(exist_ok=True)
    change_log = private / 'names.txt'
    defaults = private / 'defaults.zkm'
    defaults.write_text('exclude *.* values() and *.* valueOf(java.lang.String) and\n'
                        ' *.* readObject(java.io.ObjectInputStream) and *.* writeObject(java.io.ObjectOutputStream) and\n'
                        ' *.* readResolve() and *.* writeReplace();\n', encoding='utf-8')
    script = private / 'release.zkm'
    flat_package = 'r' + private.name.replace('-', '')[:12]
    script.write_text('classpath ' + '\n '.join(zquote(p) for p in dependencies) + ';\n' +
        'open ' + zquote(source) + ';\n' + '\n'.join(excluded) + '\n' +
        'obfuscateFlowExclude dev.rice.shaded.asm..* *(*) and net.i2p.crypto.eddsa..* *(*);\n' +
        'stringEncryptionExclude net.i2p.crypto.eddsa..*;\n' +
        'obfuscate changeLogFileOut=' + zquote(change_log) + '\n' +
        ' obfuscateFlow=normal exceptionObfuscation=light encryptStringLiterals=enhanced\n' +
        ' encryptIntegerConstants=normal autoReflectionHandling=none\n' +
        ' methodParameterChanges=none lineNumbers=delete localVariables=delete\n' +
        ' collapsePackagesWithDefault="' + flat_package + '" uniqueClassNames=true\n' +
        ' keepInnerClassInfo=true mixedCaseClassNames=true randomize=true;\n' +
        'saveAll archiveCompression=all ' + zquote(out) + ';\n', encoding='utf-8')
    run([java, '-Xmx2g', '-jar', zkm, '-dd', private, '-de', defaults, '-l', private / 'tool.log', script], 'obfuscate-release')
    log = (private / 'tool.log').read_text(encoding='utf-8', errors='replace')
    if re.search(r'^ERROR:', log, re.MULTILINE):
        raise RuntimeError('Obfuscator reported errors; see private tool.log. Release not published.')
    protected = out / source.name
    if not protected.is_file(): raise RuntimeError('Obfuscator did not create the expected JAR.')
    with zipfile.ZipFile(protected) as jar:
        after = {name: jar.read(name) for name in jar.namelist() if name.endswith('.class')}
    # The supplied tool emits empty data resources. Preserve our original assets,
    # mappings and manifest verbatim; only its class files are release inputs.
    with zipfile.ZipFile(source) as original:
        resources = {name: original.read(name) for name in original.namelist() if not name.endswith('.class')}
    hierarchy_name = internal_resources['dev/rice/forge/hierarchy.tsv']
    identity_name = internal_resources['dev/rice/security/release.properties']
    hierarchy = resources[hierarchy_name].decode('utf-8').splitlines()
    resources[hierarchy_name] = ('\n'.join(line for line in hierarchy
        if not line.startswith(('dev/rice/', 'myau/', 'net/i2p/'))) + '\n').encode('utf-8')
    merged = private / 'protected-release.jar'
    with zipfile.ZipFile(merged, 'w', zipfile.ZIP_DEFLATED) as output:
        for name, data in sorted({**resources, **after}.items()): output.writestr(name, data)
    protected = merged
    with zipfile.ZipFile(protected) as jar:
        changed = sum(after.get(name) != data for name, data in before.items())
        if changed == 0: raise RuntimeError('Obfuscator produced no bytecode changes.')
        if any('SecurityTest' in name or name.endswith(('.zkm', '.map')) for name in jar.namelist()):
            raise RuntimeError('Private/test files leaked into the release.')
        identity = jar.read(identity_name)
        with zipfile.ZipFile(source) as original:
            if identity != original.read(identity_name):
                raise RuntimeError('Build watermark changed during obfuscation.')
    protected, layout = flatten_class_layout(protected, private, internal_resources)
    result = {'tool': 'supplied ZKM-compatible JAR', 'tool_sha256': hashlib.sha256(zkm.read_bytes()).hexdigest(),
              'changed_classes': changed, 'flow': 'normal', 'string_encryption': 'enhanced',
              'reflection_exclusions': 'conservative; runtime ABI names preserved', 'private_mappings_distributed': False}
    result['flow_coverage_limited_by_tool'] = 'only one or two methods' in log
    result['renamed_classes'] = layout['relocated_classes']
    before_packages = {name.rsplit('/', 1)[0] for name in before if '/' in name}
    after_packages = {name.rsplit('/', 1)[0] for name in after if '/' in name}
    result['package_count_before'] = len(before_packages)
    result['package_count_after'] = 1
    result['collapsed_package'] = flat_package
    result['opaque_class_package'] = layout['opaque_class_package']
    result.update(resource_protection)
    if len(after_packages) >= len(before_packages):
        raise RuntimeError('Package collapse did not reduce the JAR folder structure.')
    (private / 'protection.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
    return protected, result, layout
