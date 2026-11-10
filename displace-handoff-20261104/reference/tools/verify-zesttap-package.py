"""Verify the EXE embeds the fresh DLL and all fresh payloads, preserving v40 content."""
import hashlib
import io
import json
from pathlib import Path
import zipfile
import os
import subprocess
import argparse
from pe_resources import pe_resources, find_resource

root = Path(__file__).resolve().parents[1]
workspace = root.parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--autoblock', action='store_true')
parser.add_argument('--combat', action='store_true')
parser.add_argument('--blockhit', action='store_true')
parser.add_argument('--clutch', action='store_true')
parser.add_argument('--gui-clutch', action='store_true')
args = parser.parse_args()
build = root / ('build/autoblock-v42' if args.autoblock else 'build/zesttap-v41-build2')
exe = workspace / ('rice-slinky-v42-MANUAL-BLOCK.exe' if args.autoblock else 'rice-slinky-v41-ZESTTAP-FIX.exe')
baseline = workspace / ('rice-slinky-v41-ZESTTAP-FIX.exe' if args.autoblock else 'rice-slinky-v40.exe')
if args.combat:
    build = root / 'build/autoblock-v44-final'
    exe = workspace / 'rice-slinky-v44-COMBAT-FIX.exe'
    baseline = workspace / 'rice-slinky-v43-OPENB4-ESP-FIX.exe'
if args.blockhit:
    build = root / 'build/autoblock-v45'
    exe = workspace / 'rice-slinky-v45-BLOCKHIT-FIX.exe'
    baseline = workspace / 'rice-slinky-v44-COMBAT-FIX.exe'
if args.clutch:
    build = root / 'build/clutch-v46'
    exe = workspace / 'rice-slinky-v46-CLUTCH-FIX.exe'
    baseline = workspace / 'rice-slinky-v45-BLOCKHIT-FIX.exe'
if args.gui_clutch:
    build = root / 'build/gui-clutch-v47'
    exe = workspace / 'rice-slinky-v47-CLUTCH-GUI-FIX.exe'
    baseline = workspace / 'rice-slinky-v46-CLUTCH-FIX.exe'
native = find_resource(pe_resources(exe.read_bytes()), 10, 205)
assert native == (build / 'native/Release/RiceNative.dll').read_bytes(), 'stale native DLL in EXE'
payloads = pe_resources(native)
for identifier in (101, 102, 103):
    assert find_resource(payloads, 10, identifier) == (build / ('payload-%d.jar' % identifier)).read_bytes(), 'stale payload in DLL'
old_native = find_resource(pe_resources(baseline.read_bytes()), 10, 205)
old_payload = find_resource(pe_resources(old_native), 10, 101)
(build / 'v40-payload-101.jar').write_bytes(old_payload)
with zipfile.ZipFile(io.BytesIO(old_payload)) as old, zipfile.ZipFile(build / 'payload-101.jar') as fresh:
    old_names = set(old.namelist())
    new_names = set(fresh.namelist())
    files = {name for name in old_names | new_names if not name.endswith('/')}
    old_files = {name for name in old_names if not name.endswith('/')}
    new_files = {name for name in new_names if not name.endswith('/')}
    expected_added = {'dev/rice/config/GuiPreferences.class'} if args.gui_clutch else set()
    assert new_files - old_files == expected_added and not old_files - new_files, 'unexpected payload file changes'
    changed = sorted(name for name in old_files & new_files if old.read(name) != fresh.read(name))
    allowed = {
        'dev/rice/client/SemanticEvent.class',
        'dev/rice/client/hook/GameHooks.class',
        'dev/rice/inject/transform/RicePatches.class',
        'dev/rice/module/impl/ZestTapModule.class',
    }
    if args.autoblock or args.combat:
        allowed = {'dev/rice/module/impl/AutoBlockModule.class'}
    if args.blockhit:
        allowed = {'dev/rice/module/impl/AutoBlockModule.class', 'dev/rice/client/ClientEventBridge.class'}
    if args.clutch:
        allowed = {'dev/rice/module/impl/ClutchModule.class', 'dev/rice/module/impl/ClutchModule$State.class'}
    if args.gui_clutch:
        allowed = {'dev/rice/RiceMod.class', 'dev/rice/config/ConfigStore.class', 'dev/rice/config/RecoveredConfiguration.class',
                   'dev/rice/module/impl/ClutchModule.class', 'dev/rice/module/impl/ClutchModule$State.class',
                   'dev/rice/client/placement/PlacementFrame.class', 'dev/rice/client/placement/PlacementFrame$Game.class'}
    allowed_binary = allowed | {'META-INF/MANIFEST.MF',
        'dev/rice/inject/transform/RicePatches$ClassEdits.class',
        'dev/rice/inject/transform/RicePatches$MethodEdit.class'}
    if args.blockhit:
        # Moving source lines changes debug tables in these anonymous classes;
        # the normalized bytecode comparison below must still prove equality.
        allowed_binary |= {'dev/rice/client/ClientEventBridge$1.class',
                           'dev/rice/client/ClientEventBridge$2.class'}
    if args.clutch:
        allowed_binary |= {'dev/rice/module/impl/ClutchModule$Face.class',
                           'dev/rice/module/impl/ClutchModule$Settings.class'}
    if args.gui_clutch:
        allowed_binary |= {'dev/rice/RiceMod$1.class', 'dev/rice/config/RecoveredConfiguration$ProfileBinding.class',
                           'dev/rice/config/RecoveredConfiguration$Scope.class',
                           'dev/rice/module/impl/ClutchModule$Face.class', 'dev/rice/module/impl/ClutchModule$Settings.class'}
    assert set(changed) <= allowed_binary, ('unexpected differences from latest v40', changed)
    def attributes(data):
        return dict(line.split(': ', 1) for line in data.decode().splitlines() if ': ' in line)
    old_manifest, new_manifest = attributes(old.read('META-INF/MANIFEST.MF')), attributes(fresh.read('META-INF/MANIFEST.MF'))
    for key in ('Manifest-Version', 'Implementation-Title', 'Implementation-Version'):
        assert old_manifest.get(key) == new_manifest.get(key), ('manifest identity changed', key)
environment = os.environ.copy()
environment.pop('JAVA_TOOL_OPTIONS', None)
jdk = Path(r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot/bin')
tooling = str(build / 'tools') + os.pathsep + str(root / 'deps/tooling/*')
subprocess.run([str(jdk / 'javac.exe'), '--release', '8', '-cp', str(root / 'deps/tooling/*'), '-d', str(build / 'tools'), str(root / 'tools/BytecodeDump.java')], check=True, env=environment)
for label, jar in [('v40', build / 'v40-payload-101.jar'), ('v41', build / 'payload-101.jar')]:
    subprocess.run([str(jdk / 'java.exe'), '-cp', tooling, 'BytecodeDump', str(jar), str(build / (label + '-bytecode.json'))], check=True, env=environment)
old_code = json.loads((build / 'v40-bytecode.json').read_text())
new_code = json.loads((build / 'v41-bytecode.json').read_text())
code_changed = sorted(name + '.class' for name in old_code if old_code[name] != new_code[name])
assert set(code_changed) == allowed, ('unexpected bytecode changes', code_changed)
print('PASS: baseline payload files preserved; only %d intended classes have code changes' % len(allowed))
report = {
    'exe': str(exe), 'sha256': hashlib.sha256(exe.read_bytes()).hexdigest(),
    'baseline': str(baseline),
    'embedded_native_matches': True, 'embedded_payloads_match': [101, 102, 103],
    'v40_changed_payload_files': changed,
    'v40_changed_code': code_changed,
    'hook_checks': ['identity', 'official', 'srg'],
    'in_game_tested': False,
}
(build / 'verification.json').write_text(json.dumps(report, indent=2))
print('PASS: EXE embeds the newly built DLL and all three newly built payloads')
print('SHA256: ' + report['sha256'])
