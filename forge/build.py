"""Build and verify the standalone Minecraft 1.8.9 Forge mod (Java 8 bytecode).

Uses existing local Minecraft/JDK/Forge dependencies; never distributes Minecraft.
python build.py --game-source PATH_TO_EXISTING_SLINKY_SOURCE
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import uuid
import zipfile

ROOT = Path(__file__).resolve().parent
BUILD = ROOT / 'build'
DIST = ROOT / 'dist'
MANIFEST = ('Manifest-Version: 1.0\n'
            'FMLCorePlugin: dev.rice.forge.RiceLoadingPlugin\n'
            'FMLCorePluginContainsFMLMod: true\n'
            'ForceLoadAsMod: true\n'
            'FMLAT: rice_at.cfg\n'
            'Implementation-Title: Rice Modified Forge\n'
            'Implementation-Version: 0.2.2-forge\n\n')


def run(command, label):
    command = list(map(str, command))
    print(label, flush=True)
    env = os.environ.copy()
    for key in ('JAVA_TOOL_OPTIONS', '_JAVA_OPTIONS', 'JDK_JAVA_OPTIONS'):
        env.pop(key, None)
    result = subprocess.run(command, cwd=ROOT, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding='utf-8', errors='replace')
    (BUILD / (label + '.log')).write_text(result.stdout, encoding='utf-8')
    if result.returncode:
        print(result.stdout)
        raise RuntimeError(f'{label} failed; see build/{label}.log')
    print(result.stdout[-1400:], end='', flush=True)


def quote(value):
    return '"' + str(value).replace('\\', '/').replace('"', '\\"') + '"'


def archive(path, rows, manifest=None):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as jar:
        if manifest:
            jar.writestr('META-INF/MANIFEST.MF', manifest.replace('\n', '\r\n'))
        for name, data in sorted(rows.items()):
            jar.writestr(name, data)


def find_one(pattern, description):
    matches = sorted(Path.home().glob(pattern))
    if not matches:
        raise RuntimeError('Missing ' + description)
    return matches[-1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--game-source', type=Path, required=True, help='Existing Java framework project with build/minecraft-1.8.9-named.jar and deps/')
    parser.add_argument('--jdk', type=Path, help='JDK 11+ home; output remains Java 8 compatible')
    parser.add_argument('--forge-jar', type=Path, help='Named Forge 1.8.9 compile API (forgeSrc or equivalent)')
    parser.add_argument('--forge-srg-game', type=Path, help='Forge-patched SRG Minecraft JAR for verification')
    parser.add_argument('--launchwrapper', type=Path)
    parser.add_argument('--zkm', type=Path, help='Path to the ZKM JAR to use as supplied')
    parser.add_argument('--recipient-tag', default='unassigned', help='Private distribution label; never included in release files')
    args = parser.parse_args()
    BUILD.mkdir(exist_ok=True)
    DIST.mkdir(exist_ok=True)
    suffix = '.exe' if os.name == 'nt' else ''
    java_home = args.jdk or (Path(os.environ['JAVA_HOME']) if os.environ.get('JAVA_HOME') else None)
    if java_home is None and os.name == 'nt':
        candidates = sorted(Path('C:/Program Files/Eclipse Adoptium').glob('jdk-21*'))
        java_home = candidates[-1] if candidates else None
    javac = java_home / ('bin/javac' + suffix) if java_home else Path(shutil.which('javac') or '')
    if not javac.is_file():
        parser.error('Install a JDK 11+ and set JAVA_HOME or pass --jdk.')
    java = javac.parent / ('java' + suffix)
    named = args.game_source / 'build/minecraft-1.8.9-named.jar'
    libs = sorted((args.game_source / 'deps/libraries').glob('*.jar'))
    asm = sorted((args.game_source / 'deps/tooling').glob('asm*.jar'))
    # Analyzer is test-only; it is never included in the mod.
    analysis = find_one('.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-analysis/9.*/**/asm-analysis-*.jar', 'ASM analysis test dependency')
    asm.append(analysis)
    forge = args.forge_jar or find_one('.gradle/caches/minecraft/net/minecraftforge/forge/1.8.9-*/stable/22/forgeSrc-*.jar', 'named Forge 1.8.9 API')
    launch = args.launchwrapper or find_one('.gradle/caches/modules-2/files-2.1/net.minecraft/launchwrapper/1.12/**/*.jar', 'LaunchWrapper 1.12')
    srg_game = args.forge_srg_game or find_one('.gradle/caches/essential-loom/1.8.9/de.oceanlabs.mcp.*/minecraft-srg.jar', 'Forge-patched SRG Minecraft test JAR')
    # Local compilation API has a separate named Forge/MC jar, used for patched-game verification.
    named_forge_game = forge
    framework = ROOT / 'vendor/rice-framework-named.jar'
    eddsa = ROOT / 'vendor/eddsa-0.3.0.jar'
    crypto_info = json.loads((ROOT / 'vendor/eddsa-provenance.json').read_text())
    if hashlib.sha256(eddsa.read_bytes()).hexdigest() != crypto_info['sha256']:
        raise RuntimeError('EdDSA dependency checksum mismatch.')
    expected = json.loads((ROOT / 'vendor/provenance.json').read_text())['framework_sha256']
    if hashlib.sha256(framework.read_bytes()).hexdigest() != expected:
        raise RuntimeError('Framework snapshot changed; review and update provenance before building.')
    tools = BUILD / 'tools'
    classes = BUILD / 'classes'
    for directory in (tools, classes):
        if directory.exists():
            resolved = directory.resolve()
            if BUILD.resolve() not in resolved.parents:
                raise RuntimeError('Unexpected build output path')
            shutil.rmtree(resolved)
        directory.mkdir()
    tool_cp = os.pathsep.join(map(str, asm))
    run([javac, '--release', '8', '-proc:none', '-cp', tool_cp, '-d', tools, ROOT / 'tools/RemapJar.java', ROOT / 'tools/BuildSupport.java'], 'compile-tools')
    tool_cp = os.pathsep.join(map(str, [tools, *asm]))
    release_id = str(uuid.uuid4())
    private = BUILD / 'private' / release_id
    private.mkdir(parents=True)
    (private / 'distribution.json').write_text(json.dumps({'build_id': release_id, 'recipient_tag': args.recipient_tag}, indent=2), encoding='utf-8')
    generated = BUILD / 'generated/dev/rice/security/ReleaseIdentity.java'
    generated.parent.mkdir(parents=True, exist_ok=True)
    generated.write_text('package dev.rice.security; public final class ReleaseIdentity { private ReleaseIdentity() {} public static String build() { return "' + release_id + '"; }}', encoding='utf-8')
    cp = os.pathsep.join(map(str, [framework, named, forge, launch, eddsa, *libs]))
    sources = sorted((ROOT / 'src/main/java').rglob('*.java'))
    sources.append(generated)
    options = ['--release', '8', '-g:none', '-proc:none', '-encoding', 'UTF-8', '-cp', cp, '-d', classes, *sources]
    argfile = BUILD / 'javac.args'
    argfile.write_text('\n'.join(map(quote, options)), encoding='utf-8')
    run([javac, '@' + str(argfile)], 'compile-mod')
    excluded = ('dev/rice/inject/Bootstrap', 'dev/rice/inject/transform/TransformerEngine')
    with zipfile.ZipFile(framework) as base:
        rows = {name: base.read(name) for name in base.namelist()
                if not name.startswith(('META-INF/', *excluded)) and not name.endswith('/')}
    with zipfile.ZipFile(eddsa) as crypto:
        rows.update({name: crypto.read(name) for name in crypto.namelist() if name.endswith('.class')})
    for directory in (classes, ROOT / 'src/main/resources'):
        rows.update({p.relative_to(directory).as_posix(): p.read_bytes() for p in directory.rglob('*') if p.is_file()})
    # The reference exposes Edit HUD as a footer action, not as a module setting.
    # Keep the coordinate carrier internal by moving the legacy action key under
    # the already-hidden hud_ namespace (same-length constant-pool replacement).
    gui_class = 'dev/rice/module/impl/GuiModule.class'
    legacy_key, hidden_key = b'edit_hud', b'hud_edit'
    if rows[gui_class].count(legacy_key) != 1 or len(legacy_key) != len(hidden_key):
        raise RuntimeError('Unexpected GuiModule Edit HUD constant layout')
    rows[gui_class] = rows[gui_class].replace(legacy_key, hidden_key)
    rows['dev/rice/forge/named-to-srg.txt'] = (ROOT / 'vendor/named-to-srg.txt').read_bytes()
    rows['dev/rice/security/release.properties'] = ('build_id=' + release_id + '\nformat=1\n').encode('ascii')
    rows['META-INF/LICENSE-eddsa.txt'] = (ROOT / 'vendor/LICENSE-eddsa.txt').read_bytes()
    named_mod = BUILD / 'rice-modified-named.jar'
    archive(named_mod, rows)
    srg_mod = BUILD / 'rice-modified-srg.jar'
    run([java, '-cp', tool_cp, 'RemapJar', ROOT / 'vendor/named-to-srg.txt', 'forward', named_mod, srg_mod, named, forge, launch, *libs], 'remap-forge')
    hierarchy = BUILD / 'hierarchy.tsv'
    access = BUILD / 'rice_at.cfg'
    # SRG game first preserves production access flags and Forge-added members.
    run([java, '-cp', tool_cp, 'BuildSupport', hierarchy, access, srg_mod, srg_game, launch, *libs], 'audit-game-links')
    with zipfile.ZipFile(srg_mod) as jar:
        rows = {name: jar.read(name) for name in jar.namelist()}
    rows['dev/rice/forge/hierarchy.tsv'] = hierarchy.read_bytes()
    rows['META-INF/rice_at.cfg'] = access.read_bytes()
    final = DIST / 'rice-modified-forge-1.8.9.jar'
    candidate = BUILD / final.name
    archive(candidate, rows, MANIFEST.rstrip() + '\nRice-Build-ID: ' + release_id + '\n\n')
    verify_cp = os.pathsep.join(map(str, [candidate, srg_game, forge, launch, *libs, *asm]))
    run([javac, '--release', '8', '-proc:none', '-cp', verify_cp, '-d', tools, ROOT / 'tools/VerifyForge.java', ROOT / 'tools/VerifyModules.java', ROOT / 'tools/VerifyWorldEntry.java', ROOT / 'tools/SecurityTest.java', ROOT / 'tools/LicenseProbe.java', ROOT / 'tools/VerifyLunarCompatibility.java'], 'compile-verifier')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, candidate, srg_game, forge, launch, *libs, *asm])), 'dev.rice.security.SecurityTest'], 'verify-license-security')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, candidate, srg_game, forge, launch, *libs, *asm])), 'VerifyForge', 'false', srg_game, candidate], 'verify-production-hooks')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, candidate, srg_game, forge, launch, *libs, *asm])), 'VerifyLunarCompatibility', 'false', srg_game], 'verify-lunar-compatibility')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, candidate, srg_game, forge, launch, *libs, *asm])), 'VerifyWorldEntry', 'false'], 'verify-world-entry-locked')
    # Named development artifact uses named member references, the same production hierarchy index.
    with zipfile.ZipFile(named_mod) as jar:
        dev_rows = {name: jar.read(name) for name in jar.namelist()}
    dev_rows['dev/rice/forge/hierarchy.tsv'] = hierarchy.read_bytes()
    dev_mod = BUILD / 'rice-modified-dev.jar'
    archive(dev_mod, dev_rows, MANIFEST)
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, dev_mod, named_forge_game, launch, *libs, *asm])), 'VerifyForge', 'true', named_forge_game, dev_mod], 'verify-development-hooks')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, dev_mod, named_forge_game, launch, *libs, *asm])), 'VerifyLunarCompatibility', 'true', named_forge_game], 'verify-development-lunar-compatibility')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, dev_mod, named_forge_game, launch, *libs, *asm])), 'VerifyModules', 'true', BUILD], 'verify-module-lifecycle')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, dev_mod, named_forge_game, launch, *libs, *asm])), 'VerifyWorldEntry', 'true'], 'verify-development-world-entry-locked')
    run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, [tools, candidate, srg_game, forge, launch, *libs, *asm])), 'VerifyModules', 'false', BUILD], 'verify-production-module-lifecycle')
    from protect import obfuscate, relocate_verifiers
    zkm = args.zkm or (Path(os.environ['ZKM_JAR']) if os.environ.get('ZKM_JAR') else
        Path.home() / 'Downloads/ZelixKlassMaster-27-main/ZelixKlassMaster-27-main/build/libs/zkm27.jar')
    forge_runtime = find_one('.gradle/caches/essential-loom/1.8.9/de.oceanlabs.mcp.*/forge/forge-srg.jar', 'Forge SRG API')
    mixin = find_one('.gradle/caches/modules-2/files-2.1/org.spongepowered/mixin/0.8.5/**/*.jar', 'Mixin annotation dependency')
    protected, protection, layout = obfuscate(zkm, java, candidate, [srg_game, forge_runtime, launch, mixin, *libs], private, run)
    relocated_tools, protected_security_test = relocate_verifiers(tools, layout, private)
    protected_cp = os.pathsep.join(map(str, [relocated_tools, protected, srg_game, forge_runtime, forge, launch, *libs, *asm]))
    run([java, '-Xverify:all', '-cp', protected_cp, 'VerifyForge', 'false', srg_game, protected], 'verify-protected-hooks')
    run([java, '-Xverify:all', '-cp', protected_cp, 'VerifyLunarCompatibility', 'false', srg_game], 'verify-protected-lunar-compatibility')
    run([java, '-Xverify:all', '-cp', protected_cp, protected_security_test], 'verify-protected-license-security')
    run([java, '-Xverify:all', '-cp', protected_cp, 'VerifyModules', 'false', BUILD], 'verify-protected-modules')
    run([java, '-Xverify:all', '-cp', protected_cp, 'VerifyWorldEntry', 'false'], 'verify-protected-world-entry-locked')
    shutil.copy2(protected, final)
    mac_alias = DIST / 'rice-mac-1.8.9.jar'
    shutil.copy2(final, mac_alias)
    report = {'artifact': final.name, 'sha256': hashlib.sha256(final.read_bytes()).hexdigest(),
              'target': 'Forge 1.8.9 / Java 8 bytecode', 'native_dependencies': False,
              'checks': ['compile', 'SRG remap', 'game member linkage', 'production and development hook transformation',
                         'bytecode data flow', 'generated hook linkage', 'duplicate transformation', 'no JNI methods', '72-module initialization and shutdown in both namespaces', 'Scaffold absent from registry', 'GUI text branding'],
              'live_lunar_tested': False, 'live_macos_tested': False, 'feature_parity_with_native_build': 'not established',
              'build_id': release_id, 'licensing': 'KeyAuth signed responses; expiry and periodic session validation; no saved keys',
              'obfuscation': protection, 'private_keys_in_client': False}
    report['checks'] += ['signed license rejection/tamper/replay tests', 'post-obfuscation hooks and module lifecycle', 'watermark consistency']
    report['checks'] += ['Lunar camera-setter compatibility and both-axis validation', 'GuiScreen mixin targets preserved byte-for-byte', 'locked/uninitialized world-entry hooks and vanilla redirect fallbacks', 'exact 72-module modified-reference registry', 'modified-reference two-widget HUD editor']
    report['version'] = '0.2.2-forge'
    report['mac_artifact'] = {'file': mac_alias.name, 'sha256': hashlib.sha256(mac_alias.read_bytes()).hexdigest(),
                              'same_verified_jar': True, 'live_macos_tested': False}
    (DIST / 'build-report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    shutil.copy2(BUILD / 'module-inventory.tsv', DIST / 'module-inventory.tsv')
    run([sys.executable, ROOT / 'package.py'], 'package-release')
    print('Built and verified: ' + str(final))


if __name__ == '__main__':
    main()
