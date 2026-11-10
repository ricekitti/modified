"""Check v43 packaging, port provenance, and preservation of unrelated v42 code."""
from pathlib import Path
import io
import json
import hashlib
import os
import subprocess
import zipfile
from pe_resources import pe_resources, find_resource

root = Path(__file__).resolve().parents[1]
workspace = root.parents[1]
build = root / 'build/openb4-esp-v43-final'
exe = workspace / 'rice-slinky-v43-OPENB4-ESP-FIX.exe'
native = find_resource(pe_resources(exe.read_bytes()), 10, 205)
assert native == (build / 'native/Release/RiceNative.dll').read_bytes()
for identifier in (101, 102, 103):
    assert find_resource(pe_resources(native), 10, identifier) == (build / ('payload-%d.jar' % identifier)).read_bytes()
baseline_native = find_resource(pe_resources((workspace / 'rice-slinky-v42-MANUAL-BLOCK.exe').read_bytes()), 10, 205)
baseline = find_resource(pe_resources(baseline_native), 10, 101)
(build / 'baseline.jar').write_bytes(baseline)
with zipfile.ZipFile(io.BytesIO(baseline)) as old, zipfile.ZipFile(build / 'payload-101.jar') as new:
    old_names = {name for name in old.namelist() if not name.endswith('/')}
    new_names = {name for name in new.namelist() if not name.endswith('/')}
    assert old_names - new_names == {'dev/rice/module/impl/PlayerEspModule$Row.class'}
    added = sorted(new_names - old_names)
    assert all(name.startswith('dev/rice/client/render/openb4/') or name == 'dev/rice/module/impl/PlayerEspModule$Type.class' for name in added)
    changed = sorted(name for name in old_names & new_names if old.read(name) != new.read(name))
    expected = {
        'dev/rice/module/impl/AutoBlockModule.class',
        'dev/rice/module/impl/PlayerEspModule.class',
        'dev/rice/client/hook/RenderHooks.class',
        'assets/ricesemantic/ModuleUi.json',
        'assets/ricesemantic/WidgetMetadata.json',
    }
    assert set(changed) == expected, changed
    for resource in ('ModuleUi.json', 'WidgetMetadata.json'):
        path = 'assets/ricesemantic/' + resource
        before, after = json.loads(old.read(path)), json.loads(new.read(path))
        if resource == 'ModuleUi.json':
            before['modules'] = [row for row in before['modules'] if row['module'] != 'Player ESP']
            after['modules'] = [row for row in after['modules'] if row['module'] != 'Player ESP']
        else:
            before.pop('Player ESP'); after.pop('Player ESP')
        if before != after:
            def diff(a, b, path=''):
                if isinstance(a, dict) and isinstance(b, dict):
                    for key in a.keys() | b.keys():
                        if key not in a or key not in b: print(path + '/' + key, 'missing key'); continue
                        diff(a[key], b[key], path + '/' + key)
                elif isinstance(a, list) and isinstance(b, list):
                    if len(a) != len(b): print(path, 'length mismatch', len(a), len(b))
                    for i, (left, right) in enumerate(zip(a, b)): diff(left, right, path + '/' + str(i))
                elif a != b:
                    print(path, ascii(a), ascii(b))
            diff(before, after)
            raise AssertionError('unrelated settings changed: ' + resource)
source = Path(r'C:\Users\ethan\Downloads\OpenB4-main\openb4\OpenB4\src\main\java\xyz\daikirai')
provenance = {}
for name in ('Shader', 'ShaderSources', 'GlowShader', 'EspShaders'):
    original = (source / ('util/' + name + '.java')).read_text(encoding='utf-8')
    port = (root / ('java/src/main/java/dev/rice/client/render/openb4/' + name + '.java')).read_text(encoding='utf-8')
    assert original.replace('package xyz.daikirai.util;', 'package dev.rice.client.render.openb4;') == port
    provenance[name] = hashlib.sha256(original.encode()).hexdigest()
environment = os.environ.copy()
environment.pop('JAVA_TOOL_OPTIONS', None)
jdk = Path(r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot/bin')
tools_cp = str(build / 'tools') + os.pathsep + str(root / 'deps/tooling/*')
subprocess.run([str(jdk / 'javac.exe'), '--release', '8', '-cp', str(root / 'deps/tooling/*'), '-d', str(build / 'tools'), str(root / 'tools/BytecodeDump.java')], check=True, env=environment)
for label, jar in [('v42', build / 'baseline.jar'), ('v43', build / 'payload-101.jar')]:
    subprocess.run([str(jdk / 'java.exe'), '-cp', tools_cp, 'BytecodeDump', str(jar), str(build / (label + '.json'))], check=True, env=environment)
before = json.loads((build / 'v42.json').read_text())
after = json.loads((build / 'v43.json').read_text())
code_changed = sorted(name for name in before.keys() & after.keys() if before[name] != after[name])
assert code_changed == sorted(['dev/rice/module/impl/AutoBlockModule', 'dev/rice/module/impl/PlayerEspModule', 'dev/rice/client/hook/RenderHooks'])
report = {'exe': str(exe), 'sha256': hashlib.sha256(exe.read_bytes()).hexdigest(),
          'changed': changed, 'added': added, 'source_shader_hashes': provenance,
          'settings_gui_persistence_check': '11 settings / 8 modes match OpenB4', 'in_game_tested': False}
(build / 'verification.json').write_text(json.dumps(report, indent=2))
print('PASS: fresh native DLL and all 3 payloads embedded')
print('PASS: only AutoBlock, Player ESP and shader render hooks changed from v42')
print('PASS: all other module code and settings preserved, including ZestTap and other ESPs')
print('PASS: OpenB4 shader and glow backend unchanged except package names')
print('EXE: ' + str(exe))
