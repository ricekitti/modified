"""Build all current sources into a new standalone EXE; verify all naming variants."""
import hashlib
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
options = argparse.ArgumentParser(description=__doc__)
options.add_argument('--build-name', default='zesttap-v41-build2')
options.add_argument('--output-name', default='rice-slinky-v41-ZESTTAP-FIX.exe')
args = options.parse_args()
if Path(args.build_name).name != args.build_name or Path(args.output_name).name != args.output_name:
    raise ValueError('Build/output names must be simple filenames')
BUILD = ROOT / 'build' / args.build_name
JDK = Path(r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot')
CMAKE = Path(r'C:\Program Files\Microsoft Visual Studio\18\Professional\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe')
OUTPUT = WORKSPACE / args.output_name

def run(*args):
    environment = os.environ.copy()
    environment.pop('JAVA_TOOL_OPTIONS', None)
    subprocess.run(list(map(str, args)), cwd=ROOT, check=True, env=environment)

def quote(path):
    return '"' + str(path).replace('\\', '/') + '"'

def hashes():
    paths = list((ROOT / 'java/src/main').rglob('*'))
    paths += list((ROOT / 'native').glob('*'))
    paths += list((WORKSPACE / 'rice-launcher/src').rglob('*'))
    return {str(p.relative_to(WORKSPACE)): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in paths if p.is_file() and 'bundled' not in p.parts}

def main():
    if OUTPUT.exists() or BUILD.exists():
        raise RuntimeError('Refusing to overwrite an existing build')
    BUILD.mkdir(parents=True)
    before = hashes()
    (BUILD / 'source-hashes.json').write_text(json.dumps(before, indent=2))
    classes = BUILD / 'classes'
    classes.mkdir()
    named = ROOT / 'build/minecraft-1.8.9-named.jar'
    libs = sorted((ROOT / 'deps/libraries').glob('*.jar'))
    classpath = os.pathsep.join(map(str, [named] + libs))
    sources = sorted((ROOT / 'java/src/main/java').rglob('*.java'))
    arguments = ['--release', '8', '-encoding', 'UTF-8', '-proc:none', '-classpath', quote(classpath),
                 '-d', quote(classes)] + [quote(p) for p in sources]
    argfile = BUILD / 'javac.args'
    argfile.write_text('\n'.join(arguments) + '\n', encoding='utf-8')
    run(JDK / 'bin/javac.exe', '@' + str(argfile))
    tools = BUILD / 'tools'
    tools.mkdir()
    run(JDK / 'bin/javac.exe', '--release', '8', '-cp', ROOT / 'deps/tooling/*',
        '-d', tools, ROOT / 'tools/RemapJar.java')
    tooling_cp = str(tools) + os.pathsep + str(ROOT / 'deps/tooling/*')
    run(JDK / 'bin/jar.exe', 'cfm', BUILD / 'payload-101.jar', ROOT / 'build/MANIFEST.MF',
        '-C', classes, '.', '-C', ROOT / 'java/src/main/resources', '.')
    for identifier, mapping in ((102, 'srg'), (103, 'official')):
        payload = BUILD / ('payload-%d.jar' % identifier)
        run(JDK / 'bin/java.exe', '-cp', tooling_cp, 'RemapJar', ROOT / ('mappings/named-to-' + mapping + '.txt'),
            'forward', BUILD / 'payload-101.jar', payload, named)
        mapping_root = BUILD / ('mapping-%d' % identifier)
        destination = mapping_root / 'dev/rice/inject/mappings.txt'
        destination.parent.mkdir(parents=True)
        shutil.copyfile(ROOT / ('java/mappings/' + mapping + '.txt'), destination)
        run(JDK / 'bin/jar.exe', 'ufm', payload, ROOT / 'build/MANIFEST.MF',
            '-C', mapping_root, 'dev/rice/inject/mappings.txt')
    test_cp = os.pathsep.join(map(str, [classes, ROOT / 'java/src/main/resources', named] + libs))
    run(JDK / 'bin/javac.exe', '--release', '8', '-cp', test_cp, '-d', tools, ROOT / 'tools/ZestTapHookCheck.java')
    for mapping, minecraft in [('identity', named), ('official', ROOT / 'deps/minecraft-1.8.9-official.jar'), ('srg', BUILD / 'minecraft-srg.jar')]:
        if mapping == 'srg':
            run(JDK / 'bin/java.exe', '-cp', tooling_cp, 'RemapJar', ROOT / 'mappings/named-to-srg.txt', 'forward', named, minecraft, named)
        run(JDK / 'bin/java.exe', '-cp', str(tools) + os.pathsep + test_cp,
            'dev.rice.inject.transform.ZestTapHookCheck', minecraft,
            'identity' if mapping == 'identity' else ROOT / ('java/mappings/' + mapping + '.txt'))
    run(CMAKE, '-S', ROOT / 'native', '-B', BUILD / 'native', '-G', 'Visual Studio 18 2026', '-A', 'x64',
        '-DPAYLOAD_DIR=' + str(BUILD), '-DJAVA_HOME=' + str(JDK))
    run(CMAKE, '--build', BUILD / 'native', '--config', 'Release', '--target', 'RiceNative', '--parallel', '2')
    native = BUILD / 'native/Release/RiceNative.dll'
    shutil.copyfile(native, WORKSPACE / 'rice-launcher/src/bundled/RiceNative.dll')
    run(CMAKE, '-S', WORKSPACE / 'rice-launcher', '-B', BUILD / 'launcher', '-G', 'Visual Studio 18 2026', '-A', 'x64', '-DBUILD_TESTING=OFF')
    run(CMAKE, '--build', BUILD / 'launcher', '--config', 'Release', '--target', 'rice_slinky', '--parallel', '2')
    after = hashes()
    if before != after:
        raise RuntimeError('Sources changed during build; rebuild required')
    shutil.copyfile(BUILD / 'launcher/Release/rice-slinky.exe', OUTPUT)
    print('Built from %d current source/resource files: %s' % (len(before), OUTPUT), flush=True)

if __name__ == '__main__':
    main()
