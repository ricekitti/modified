"""Snapshot and build Rice Slinky with explicit source/JAR scope and regression checks.

This is a change/build guard, not a security sandbox or a guarantee of game behavior.
Run --help; see workspace GLM-OPENCODE-HANDOFF.md for the complete workflow.
"""
import argparse
from contextlib import contextmanager
import fnmatch
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import struct
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
SNAPSHOTS = ROOT / 'build/agent-snapshots'
JDK = Path(r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot')
CMAKE = Path(r'C:\Program Files\Microsoft Visual Studio\18\Professional\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe')
GENERATOR = 'Visual Studio 18 2026'
ENV = os.environ.copy()
ENV.pop('JAVA_TOOL_OPTIONS', None)

def payload_codec():
    spec = importlib.util.spec_from_file_location('rice_native_codec', ROOT/'tools/native-payload-codec.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

def resource_helpers():
    # Isolated Python (-I) deliberately excludes the script directory from sys.path.
    # Load the trusted sibling by its resolved filename instead of relying on ambient imports.
    spec = importlib.util.spec_from_file_location('rice_pe_resources', ROOT/'tools/pe_resources.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.pe_resources, module.find_resource

def require(condition, message):
    if not condition:
        raise RuntimeError(message)

def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()

def key(path):
    return Path(path).resolve().relative_to(WORKSPACE.resolve()).as_posix()

def inside(path, parent):
    path = Path(path).resolve()
    require(path.is_relative_to(Path(parent).resolve()), 'Path outside intended directory: ' + str(path))
    return path

def simple_name(value):
    require(bool(re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,100}', value)), 'Use a simple task/build filename: ' + value)
    require(value.upper().split('.')[0] not in {'CON','PRN','AUX','NUL', *(f'COM{i}' for i in range(1,10)), *(f'LPT{i}' for i in range(1,10))}, 'Reserved filename')
    return value

def inventory():
    paths = []
    for directory in [ROOT/'java/src/main', ROOT/'java/mappings', ROOT/'mappings', ROOT/'native', ROOT/'tools', WORKSPACE/'rice-launcher/src', WORKSPACE/'rice-launcher/tests']:
        for path in directory.rglob('*'):
            if path.is_file() and '__pycache__' not in path.parts and 'bundled' not in path.parts:
                paths.append(inside(path, WORKSPACE))
    for path in [WORKSPACE/'rice-launcher/CMakeLists.txt', WORKSPACE/'AGENTS.md', WORKSPACE/'GLM-OPENCODE-HANDOFF.md']:
        if path.is_file(): paths.append(path)
    return {key(p): sha(p) for p in sorted(set(paths))}

def inputs():
    paths = list((ROOT/'deps').rglob('*.jar')) + [ROOT/'build/minecraft-1.8.9-named.jar']
    return {key(p): sha(p) for p in sorted(paths)}

def save(path, value):
    Path(path).write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')

def read(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def validate_jar(path, identifier):
    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        require(jar.testzip() is None and len(names) == len(set(names)), 'Corrupt/duplicate archive entries: ' + str(path))
        require('META-INF/MANIFEST.MF' in names, 'Missing manifest')
        require('Implementation-Version: 0.5.0-settings' in jar.read('META-INF/MANIFEST.MF').decode(), 'Unexpected payload identity')
        mapping = 'dev/rice/inject/mappings.txt'
        require((mapping in names) == (identifier != 101), 'Wrong mappings.txt presence')
        for name in names:
            if name.endswith('.class'):
                data = jar.read(name)
                require(data[:4] == b'\xca\xfe\xba\xbe' and struct.unpack_from('>H',data,6)[0] == 52, 'Expected Java 8 class: ' + name)

def snapshot(args):
    destination = inside(SNAPSHOTS/simple_name(args.name), SNAPSHOTS)
    require(not destination.exists(), 'Snapshot already exists; choose a new name')
    baseline = inside(ROOT/args.baseline_build, ROOT/'build')
    report = read(baseline/'verification.json')
    current = inventory()
    coverage = report.get('editable_hashes') or report.get('source_hashes')
    require(coverage, 'Baseline has no source fingerprints; establish a verified baseline first')
    for name, expected in coverage.items():
        path = inside(Path(name) if Path(name).is_absolute() else WORKSPACE/name, WORKSPACE)
        require(path.is_file() and sha(path) == expected, 'Current file differs from baseline; preserve existing work and establish its build first: ' + str(path))
    java_keys = {key(p) for p in (ROOT/'java/src/main').rglob('*') if p.is_file()}
    covered_keys = {key(Path(n) if Path(n).is_absolute() else WORKSPACE/n) for n in coverage}
    require(java_keys <= covered_keys, 'Unbuilt/new Java production files exist outside baseline coverage')
    executable = inside(report['output'], WORKSPACE)
    require(executable.is_file() and sha(executable) == report['sha256'], 'Baseline EXE missing or changed')
    pe_resources, find_resource = resource_helpers()
    native = (baseline/'native/Release/RiceNative.dll').read_bytes()
    require(payload_codec().unpack(find_resource(pe_resources(executable.read_bytes()),10,205)) == native, 'Baseline EXE/DLL mismatch')
    for identifier in (101,102,103):
        path = baseline/f'artifacts/payload-{identifier}.jar'
        validate_jar(path, identifier)
        require(find_resource(pe_resources(native),10,identifier) == path.read_bytes(), 'Baseline embedded JAR mismatch')
    destination.mkdir(parents=True)
    for name in current:
        target = destination/'backup'/name
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(WORKSPACE/name, target)
    for identifier in (101,102,103):
        shutil.copyfile(baseline/f'artifacts/payload-{identifier}.jar', destination/f'payload-{identifier}.jar')
    snapshot_hashes = {f'payload-{i}.jar':sha(destination/f'payload-{i}.jar') for i in (101,102,103)}
    save(destination/'snapshot.json', {'name':args.name,'workspace':str(WORKSPACE),'baseline_build':str(baseline),'baseline_exe':str(executable),'baseline_exe_sha256':report['sha256'],'editable_hashes':current,'input_hashes':inputs(),'payload_hashes':snapshot_hashes,'baseline_coverage_note':'Verified against baseline report coverage; initial v72 report covers Java production sources. Other editable files are captured at snapshot time, not retroactively proven against v72.'})
    print('Snapshot saved: ' + str(destination))
    print('Editable files backed up: ' + str(len(current)))

def patterns(values, label):
    require(isinstance(values,list) and all(isinstance(v,str) and v for v in values), label + ' must be a string array')
    for value in values:
        require('\\' not in value and not value.startswith('/') and '..' not in value.split('/'), label + ': use relative / paths')
        require(value not in {'*','**','**/*'}, 'Application-wide wildcard rejected: ' + value)
    return values

def difference(before, after):
    return { 'changed': sorted(n for n in before.keys() & after.keys() if before[n] != after[n]),
             'added': sorted(after.keys() - before.keys()), 'removed': sorted(before.keys() - after.keys()) }

def enforce(diff, allowed, label):
    unexpected = [name for rows in diff.values() for name in rows if not any(fnmatch.fnmatchcase(name, rule) for rule in allowed)]
    require(not unexpected, label + ' outside declared task scope:\n' + '\n'.join(unexpected))

@contextmanager
def build_lock():
    path = ROOT/'build/.safe-agent-build.lock'
    try:
        descriptor = os.open(path, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
    except FileExistsError:
        raise RuntimeError('Another guarded build, or an interrupted build lock, exists: ' + str(path) + '. Inspect it and active processes before removing it.')
    with os.fdopen(descriptor,'w') as stream:
        stream.write(json.dumps({'pid':os.getpid(),'script':str(Path(__file__).resolve())}))
    try: yield
    finally: path.unlink()

def build(args):
    snapdir = inside(SNAPSHOTS/simple_name(args.snapshot), SNAPSHOTS)
    state = read(snapdir/'snapshot.json')
    require(state['workspace'] == str(WORKSPACE), 'Snapshot belongs to another workspace')
    scope_path = inside(ROOT/args.scope, WORKSPACE)
    scope = read(scope_path)
    require(isinstance(scope.get('task'),str) and scope['task'].strip(), 'scope.task must describe the user request')
    allowed_sources = patterns(scope.get('allowed_sources'), 'allowed_sources')
    allowed_entries = patterns(scope.get('allowed_entries'), 'allowed_entries')
    current = inventory()
    source_diff = difference(state['editable_hashes'], current)
    enforce(source_diff, allowed_sources, 'Source change')
    require(inputs() == state['input_hashes'], 'Dependency JAR inputs changed after snapshot; review deliberately and establish a new baseline')
    for name, expected in state['payload_hashes'].items(): require(sha(snapdir/name) == expected, 'Baseline JAR changed: ' + name)
    directory = inside(ROOT/'build'/simple_name(args.build_name), ROOT/'build')
    output_name = simple_name(args.output_name)
    require(output_name.lower().endswith('.exe'), 'Output must be a new .exe filename')
    output = inside(WORKSPACE/output_name, WORKSPACE)
    require(not directory.exists() and not output.exists(), 'Build directory/output exists; choose new names')
    require(JDK.is_dir() and CMAKE.is_file(), 'Installed JDK/CMake path missing; inspect toolchain before changing it')
    directory.mkdir(parents=True)
    artifacts = directory/'artifacts';artifacts.mkdir()
    classes = artifacts/'classes';classes.mkdir()
    tools = directory/'checks';tools.mkdir()
    manifest = zipfile.ZipFile(snapdir/'payload-101.jar').read('META-INF/MANIFEST.MF')
    java = JDK/'bin/java.exe';javac = JDK/'bin/javac.exe'
    named = ROOT/'build/minecraft-1.8.9-named.jar'
    libs = sorted((ROOT/'deps/libraries').glob('*.jar'))
    asm = sorted((ROOT/'deps/tooling').glob('asm*.jar'))
    def execute(command, label):
        print(label, flush=True)
        with (directory/(label+'.log')).open('w',encoding='utf-8') as log:
            result = subprocess.run(list(map(str,command)),cwd=ROOT,env=ENV,stdout=log,stderr=subprocess.STDOUT)
        require(result.returncode == 0, 'Failed '+label+'; see '+str(directory/(label+'.log')))
    def classpath(paths): return os.pathsep.join(map(str,paths))
    def argquote(value): return '"' + str(value).replace('\\','/').replace('"','\\"') + '"'
    jdk8 = Path(r'C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot')
    require((jdk8/'bin/javac.exe').is_file(), 'Mayu instruction fidelity requires the installed JDK 8 javac')
    compile_stubs = directory/'compile-only-stubs'; compile_stubs.mkdir()
    execute([javac,'--release','8','-nowarn','-proc:none','-cp',classpath([named]+libs),'-d',compile_stubs,
             *sorted((ROOT/'tools/mayu-compile-stubs').rglob('*.java'))], 'compile-mayu-stubs')
    execute([javac,'--release','8','-cp',classpath(asm),'-d',tools,ROOT/'tools/MayuBytecode.java'], 'compile-mayu-tool')
    mayucp = classpath([tools]+asm)
    view = directory/'minecraft-compile-view.jar'
    execute([java,'-cp',mayucp,'MayuBytecode','compile-view',named,view], 'mayu-compile-view')
    cp = classpath([view,compile_stubs,named]+libs)
    argsfile = directory/'javac.args'
    compile_sources = sorted((ROOT/'java/src/main/java').rglob('*.java'))
    arguments = ['--release' ,'8','-encoding','UTF-8','-nowarn','-proc:none','-classpath',cp,'-d',classes] + compile_sources
    argsfile.write_text('\n'.join(argquote(v) for v in arguments)+'\n',encoding='utf-8')
    execute([javac,'@'+str(argsfile)],'compile-java')
    # JDK 8 reproduces the upstream compiler instructions, including synthetic enum methods.
    # Compile-only Forge/Mixin types and the modified Minecraft view never enter the payload.
    mayu_manifest = read(ROOT/'tools/mayu-port-manifest.json')
    mayu_sources = [ROOT/'java/src/main/java/myau'/rel for rel, info in mayu_manifest['files'].items() if info['byte_exact']]
    cp8 = classpath([view,compile_stubs,classes,named]+libs)
    execute([jdk8/'bin/javac.exe','-g','-nowarn','-proc:none','-encoding','UTF-8','-cp',cp8,'-d',classes,*mayu_sources], 'compile-mayu-jdk8')
    # AutoClutch is now a native module (dev.rice.client.clutch), not the OpenB4 script runtime, so the old
    # compile-autoclutch-jdk8 and autoclutch-source-file (200-char SourceFile identity) steps are gone.
    execute([java,'-cp',mayucp,'MayuBytecode','adapt',classes,ROOT/'tools/mayu-adapters.txt',
             ROOT/'tools/fixtures/upstream/meow/classes',named,directory/'mayu-adapters-report.txt'], 'mayu-adapt-and-link')
    execute([javac,'--release','8','-cp',classpath(asm),'-d',tools,ROOT/'tools/RemapJar.java'],'compile-remapper')
    remapcp = classpath([tools]+asm)
    def makejar(path, rows):
        with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED) as archive:
            archive.writestr('META-INF/MANIFEST.MF',manifest)
            for name,data in rows: archive.writestr(name,data)
    rows = []
    for base in (classes,ROOT/'java/src/main/resources'):
        rows.extend((p.relative_to(base).as_posix(),p.read_bytes()) for p in sorted(base.rglob('*')) if p.is_file())
    makejar(artifacts/'payload-101.jar',rows)
    for identifier,mapping in ((102,'srg'),(103,'official')):
        temporary = artifacts/f'remapped-{identifier}.jar'
        execute([java,'-cp',remapcp,'RemapJar',ROOT/f'java/mappings/{mapping}.txt','forward',artifacts/'payload-101.jar',temporary,named,*libs],f'remap-{identifier}')
        with zipfile.ZipFile(temporary) as archive:
            rows = [(n,archive.read(n)) for n in archive.namelist() if n not in {'META-INF/MANIFEST.MF','dev/rice/inject/mappings.txt'}]
        rows.append(('dev/rice/inject/mappings.txt',(ROOT/f'java/mappings/{mapping}.txt').read_bytes()))
        makejar(artifacts/f'payload-{identifier}.jar',rows)
    payload_diffs = {}
    for identifier in (101,102,103):
        fresh = artifacts/f'payload-{identifier}.jar';validate_jar(fresh,identifier)
        with zipfile.ZipFile(snapdir/f'payload-{identifier}.jar') as a,zipfile.ZipFile(fresh) as b:
            before = {n:hashlib.sha256(a.read(n)).hexdigest() for n in a.namelist()}
            after = {n:hashlib.sha256(b.read(n)).hexdigest() for n in b.namelist()}
        diff = difference(before,after);enforce(diff,allowed_entries,f'Payload {identifier}')
        payload_diffs[str(identifier)] = diff
    save(directory/'change-review.json',{'scope':scope,'sources':source_diff,'payloads':payload_diffs})
    testcp = classpath([classes,ROOT/'java/src/main/resources',named]+libs)
    # Port fidelity: every generated port equals its vendored upstream (tools/fixtures/upstream) plus whitelisted rewrites.
    upstream = ROOT/'tools/fixtures/upstream'
    for label,tool,source in [('openjade','port-openjade.py',upstream/'openjade'),('openslinky','port-openslinky.py',upstream/'openslinky'),
                              ('supplied-blockin','port-supplied-blockin.py',None),('openb4-antifireball','port-openb4-antifireball.py',upstream/'openb4'),('raven-fireballesp','port-raven-fireballesp.py',None),
                              ('snowify-anticheat','port-snowify-anticheat.py',None),('openb4-targethud','port-openb4-targethud.py',upstream/'openb4')]:
        execute([sys.executable,'-I',ROOT/'tools'/tool,*([source] if source else []),'--check'],'port-check-'+label)
    execute([sys.executable,'-I',ROOT/'tools/port-mayu.py','--check'],'port-check-mayu')
    execute([javac,'--release','8','-cp',testcp,'-d',tools,*[ROOT/'tools'/name for name in ['HudEditorCheck.java','SnowifyAntiCheatCheck.java','RemovedScaffoldCheck.java','BedDefenderStabilityCheck.java','SlinkyFastMineCheck.java','SlinkyDisplaceCheck.java','OpenB4KillAuraCheck.java','RavenBsCombatCheck.java','TeamsAimAssistCheck.java','ShopInteractionCheck.java','JadeVelocityCheck.java','HookPlacementCheck.java','BlockInCheck.java','V115GlStub.java','V115Input.java','V115InputStubs.java','V115Check.java','V116GlStub.java','V116Check.java','V119Check.java','V120Check.java','V122Check.java']]],'compile-checks')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'OpenB4KillAuraCheck',classes,ROOT],'openb4-killaura-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'RavenBsCombatCheck',classes,ROOT],'ravenbs-combat-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'SlinkyDisplaceCheck',classes,ROOT],'slinky-displace-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'SlinkyFastMineCheck',ROOT],'slinky-fastmine-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'ShopInteractionCheck',classes],'shop-interaction-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'BlockInCheck',classes],'supplied-blockin-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'SnowifyAntiCheatCheck',classes,ROOT],'snowify-anticheat-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'HudEditorCheck',classes,ROOT],'hud-editor-check')
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'JadeVelocityCheck',classes,snapdir/'payload-101.jar'],'jade-velocity-check')
    stubs = directory/'lwjgl-input-stubs'
    execute([java,'-cp',str(tools)+os.pathsep+testcp,'V115InputStubs',next(p for p in libs if p.name.startswith('lwjgl-2')),stubs],'v115-input-stubs')
    execute([java,'-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'BedDefenderStabilityCheck',classes,ROOT],'beddefender-stability-check')
    execute([java,'-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'V115Check',classes,ROOT],'v115-check')
    execute([java,'-Xverify:all','-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'RemovedScaffoldCheck',classes,ROOT],'removed-scaffold-check')
    execute([java,'-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'V116Check',classes,ROOT],'v116-check')
    execute([java,'-Xverify:all','-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'V119Check',classes,ROOT],'v119-check')
    execute([java,'-Xverify:all','-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'V120Check',classes,ROOT],'v120-check')
    execute([java,'-Xverify:all','-cp',str(stubs)+os.pathsep+str(tools)+os.pathsep+testcp,'V122Check',classes,ROOT],'v122-check')
    srg = directory/'minecraft-srg.jar'
    execute([java,'-cp',remapcp,'RemapJar',ROOT/'java/mappings/srg.txt','forward',named,srg,named,*libs],'remap-test-minecraft')
    for label,minecraft,mapping in [('named',named,'identity'),('srg',srg,ROOT/'java/mappings/srg.txt'),('official',ROOT/'deps/minecraft-1.8.9-official.jar',ROOT/'java/mappings/official.txt')]:
        execute([java,'-cp',str(tools)+os.pathsep+testcp,'dev.rice.inject.transform.HookPlacementCheck',minecraft,mapping],'hook-check-'+label)
    execute([CMAKE,'-S',ROOT/'native','-B',directory/'native','-G',GENERATOR,'-A','x64','-DPAYLOAD_DIR='+str(artifacts),'-DJAVA_HOME='+str(JDK)],'configure-native')
    execute([CMAKE,'--build',directory/'native','--config','Release','--target','RiceNative','--parallel','2'],'build-native')
    bundle = WORKSPACE/'rice-launcher/src/bundled/RiceNative.dll'
    require(bundle.is_file(),'Shared bundle missing; do not guess a replacement')
    original = bundle.read_bytes();backup = directory/'RiceNative.before.dll';backup.write_bytes(original)
    print('Shared-bundle recovery backup: '+str(backup),flush=True)
    try:
        native_bytes = (directory/'native/Release/RiceNative.dll').read_bytes()
        packed_native = payload_codec().pack(native_bytes)
        packed_path = directory/'native-resource.lzms'
        packed_path.write_bytes(packed_native)
        require(payload_codec().unpack(packed_native) == native_bytes, 'Compressed native DLL roundtrip mismatch')
        bundle.write_bytes(packed_native)
        execute([CMAKE,'-S',WORKSPACE/'rice-launcher','-B',directory/'launcher','-G',GENERATOR,'-A','x64','-DBUILD_TESTING=OFF'],'configure-launcher')
        execute([CMAKE,'--build',directory/'launcher','--config','Release','--target','rice_slinky','--parallel','2'],'build-launcher')
        execute([CMAKE,'--build',directory/'launcher','--config','Release','--target','NativePayloadCheck','--parallel','2'],'build-native-payload-check')
        execute([directory/'launcher/Release/NativePayloadCheck.exe',directory/'native/Release/RiceNative.dll',packed_path],'native-payload-check')
    finally:
        bundle.write_bytes(original)
    require(bundle.read_bytes() == original,'Shared bundle restoration failed')
    require(inventory() == current and inputs() == state['input_hashes'],'Editable files or inputs changed during build; do not deliver')
    pe_resources, find_resource = resource_helpers()
    exe = directory/'launcher/Release/rice-slinky.exe'
    native = (directory/'native/Release/RiceNative.dll').read_bytes()
    embedded_native = find_resource(pe_resources(exe.read_bytes()),10,205)
    require(embedded_native == packed_native, 'EXE embeds the wrong compressed resource')
    require(payload_codec().unpack(embedded_native) == native,'EXE decompresses to the wrong DLL')
    for identifier in (101,102,103):require(find_resource(pe_resources(native),10,identifier) == (artifacts/f'payload-{identifier}.jar').read_bytes(),'DLL embeds the wrong JAR')
    # Exclusive publication preserves every previous output, including one
    # created by another process after our initial existence check.
    with output.open('xb') as stream,exe.open('rb') as source:shutil.copyfileobj(source,stream)
    java_sources = {str(p):sha(p) for p in (ROOT/'java/src/main').rglob('*') if p.is_file()}
    report = {'output':str(output),'sha256':sha(output),'snapshot':str(snapdir),'scope':scope,'source_changes':source_diff,'payload_changes':payload_diffs,'source_hashes':java_sources,'editable_hashes':current,'input_hashes':state['input_hashes'],'bundled_dll_restored':True,'embedded_payload_chain':'EXE RCDATA 205 -> Windows LZMS decode -> byte-exact DLL RCDATA 101/102/103 -> built JARs','tests':['native-payload-check','removed-scaffold-check','port-check-mayu','port-check-supplied-blockin','mayu-adapt-and-link','port-check-openjade','port-check-openslinky','port-check-openb4-antifireball','port-check-raven-fireballesp','port-check-snowify-anticheat','snowify-anticheat-check','hud-editor-check','port-check-openb4-targethud','slinky-displace-check','slinky-fastmine-check','beddefender-stability-check','openb4-killaura-check','ravenbs-combat-check','shop-interaction-check','supplied-blockin-check','jade-velocity-check','v115-check','v116-check','v119-check','v120-check','v122-check','hook-check-named','hook-check-srg','hook-check-official'],'limitations':'Scope/byte/integration checks are not proof of live in-game behavior or zero bugs. No launcher or in-game session was run.'}
    report['blockin_import']={'source':'User BlockIn-full-source.zip','manifest':'tools/supplied-blockin-manifest.json','core':read(ROOT/'tools/supplied-blockin-manifest.json')['adaptations'],'adapters':'Rice settings, tick/render/input phase, rotation arbitration and packet synchronization, held item, claims and bind services','limitations':'Shared-service host integration is adapted to Rice; no live game/injection test was run.'}
    report['ravenbs_import']={'reference':'Raven bS keystrokesmod combat sources',
        'fixtures':{p.name:sha(p) for p in sorted((ROOT/'tools/fixtures/ravenbs').glob('*.java.txt'))},
        'active_aura':'dev.rice.client.combat.ravenbs.RavenBsKillAuraBackend',
        'active_displace':'dev.rice.client.combat.ravenbs.RavenBsDisplaceBackend',
        'adapters':'Rice lifecycle/settings, tick mouse selection, pre-interaction key queue, controller attacks, packet holds, rotation arbitration and movement hooks',
        'teams':'Color-based Teams with consistent scoreboard/tab/styled-username matching and cached Aim Assist target revalidation',
        'host_differences':['Rice Teams/Friends/AntiBot and weapon classification supply shared services',
            'Optional bS BedWars/SkyWars module opt-ins remain disabled because those modules are absent in Rice',
            'Hostile mob targets are sampled from client entities instead of Forge LivingSetAttackTarget callbacks',
            'Raven DEBUG visualization is disabled; Displace arrow rendering remains available'],
        'limitations':'Adapted to Rice host services; not byte-identical to the complete Raven client. Offline checks do not establish live gameplay or ban avoidance.'}
    report['scaffold']={'registered':False,'backend':'Scaffold module removed in v199; independent OG Clutch preserved','compatibility':'Legacy profiles cannot re-enable removed Scaffold; legacy Mayu renderer view remains inert.'}
    report['native_compression']={'format':'RSLZ0001 + Windows Compression API LZMS','raw_dll_bytes':len(native),'resource_bytes':len(packed_native),'exe_bytes':output.stat().st_size,'roundtrip':'Python and production C++ decoder restore original DLL byte-for-byte'}
    report['legacy_bednuker_import'] = {'source': mayu_manifest['upstream'], 'byte_exact_source_count': len(mayu_sources), 'compiled_reference_class_count': len(mayu_manifest['reference_classes']), 'adapters': 'Existing BedNuker/shared Mayu event/render compatibility; see mayu-adapters-report.txt'}
    save(directory/'verification.json',report)
    print('SUCCESS: '+str(output),flush=True)
    print('Report: '+str(directory/'verification.json'),flush=True)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command',required=True)
    snap = commands.add_parser('snapshot',help='Back up editable files against a verified build BEFORE edits')
    snap.add_argument('--name',required=True);snap.add_argument('--baseline-build',required=True)
    compile_ = commands.add_parser('build',help='Build only explicitly scoped changes into a NEW EXE')
    compile_.add_argument('--snapshot',required=True);compile_.add_argument('--scope',required=True)
    compile_.add_argument('--build-name',required=True);compile_.add_argument('--output-name',required=True)
    args = parser.parse_args()
    if args.command == 'snapshot':snapshot(args)
    else:
        with build_lock():build(args)

if __name__ == '__main__':
    try:main()
    except (RuntimeError,OSError,ValueError,KeyError,zipfile.BadZipFile) as failure:
        print('STOP: '+str(failure),file=sys.stderr)
        sys.exit(1)
