"""Prepare IntelliJ's Minecraft compile view and compile current Java sources.

Development classes only. Release payloads require safe-agent-build.py, which also
performs the JDK 8 fidelity pass, adapters, remapping, tests and EXE packaging.
"""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--prepare-only', action='store_true')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    spec = importlib.util.spec_from_file_location('safe_agent_build', root / 'tools/safe-agent-build.py')
    guard = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(guard)
    env = os.environ.copy()
    env.pop('JAVA_TOOL_OPTIONS', None)
    support = root / 'build/idea-support'
    support.mkdir(parents=True, exist_ok=True)
    build = Path(tempfile.mkdtemp(prefix='compile-', dir=support))
    javac = guard.JDK / 'bin/javac.exe'
    java = guard.JDK / 'bin/java.exe'
    named = root / 'build/minecraft-1.8.9-named.jar'
    libs = sorted((root / 'deps/libraries').glob('*.jar'))
    asm = sorted((root / 'deps/tooling').glob('asm*.jar'))
    for required in [javac, java, named, *libs, *asm]:
        if not required.is_file():
            raise FileNotFoundError(required)
    if not libs or not asm:
        raise RuntimeError('Missing bundled library/tooling JARs')

    def run(command):
        subprocess.run([str(item) for item in command], cwd=root, env=env, check=True)

    def cp(paths):
        return os.pathsep.join(str(path) for path in paths)

    tools = build / 'tools'
    tools.mkdir()
    run([javac, '--release', '8', '-cp', cp(asm), '-d', tools, root / 'tools/MayuBytecode.java'])
    view = build / 'minecraft-compile-view.jar'
    run([java, '-cp', cp([tools, *asm]), 'MayuBytecode', 'compile-view', named, view])
    # Stable library path for IDEA; the compiler's outputs always use a fresh folder.
    staged = support / 'minecraft-compile-view.jar.tmp'
    shutil.copyfile(view, staged)
    staged.replace(support / 'minecraft-compile-view.jar')
    if args.prepare_only:
        print('IntelliJ compile library prepared: ' + str(support), flush=True)
        return
    stubs = build / 'compile-only-stubs'
    stubs.mkdir()
    run([javac, '--release', '8', '-proc:none', '-cp', cp([named, *libs]), '-d', stubs,
         *sorted((root / 'tools/mayu-compile-stubs').rglob('*.java'))])
    classes = build / 'classes'
    classes.mkdir()
    sources = sorted((root / 'java/src/main/java').rglob('*.java'))
    arguments = ['--release', '8', '-encoding', 'UTF-8', '-proc:none', '-cp',
                 cp([view, stubs, named, *libs]), '-d', classes, *sources]
    argsfile = build / 'javac.args'
    argsfile.write_text('\n'.join('"' + str(value).replace('\\', '/').replace('"', '\\"') + '"'
                                  for value in arguments) + '\n', encoding='utf-8')
    run([javac, '@' + str(argsfile)])
    compiled = list(classes.rglob('*.class'))
    if not compiled or any(path.read_bytes()[:8] != b'\xca\xfe\xba\xbe\x00\x00\x00\x34' for path in compiled):
        raise RuntimeError('Expected Java 8 classes in fresh development output')
    with zipfile.ZipFile(view) as jar:
        if jar.testzip() is not None:
            raise RuntimeError('Invalid Minecraft compile view')
    report = {'sources': len(sources), 'classes': len(compiled), 'class_major': 52,
              'output': str(classes), 'purpose': 'IDE development compilation; not an injectable payload'}
    (build / 'verification.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report, indent=2), flush=True)


if __name__ == '__main__':
    main()
