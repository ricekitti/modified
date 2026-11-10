"""Generate (or verify) Rice's TimerRange and Criticals modules, and the PacketPipeline they run through, from OpenSlinky.

Upstream: https://github.com/Skid-Technology/OpenSlinky at commit 9c1430df6e0f32582b5762b358a58546c5f94390
(SlinkyClient/src/main/java). Read-only input; nothing from it is executed.

Only these mechanical rewrites are applied (OpenSlinky and Rice are the same client under different package
and helper-class names; the backends both modules use are already identical):
  package/imports  dev.slinky.* -> the matching dev.rice.* class
  CatalogModule -> EvidenceModule, NumericMath -> RecoveredMath, RotationRandom -> RecoveredRandom

usage: python tools/port-openslinky.py <upstream SlinkyClient/src/main/java> [--check]
"""
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'java' / 'src' / 'main' / 'java' / 'dev' / 'rice' / 'module' / 'impl'
MANIFEST = ROOT / 'tools' / 'openslinky-port-manifest.json'
UPSTREAM_COMMIT = '9c1430df6e0f32582b5762b358a58546c5f94390'
FILES = ['TimerRangeModule.java', 'CriticalsModule.java']
IMPORTS = {
    'dev.slinky.client.ClientContext': 'dev.rice.client.ClientContext',
    'dev.slinky.client.MinecraftFields': 'dev.rice.client.MinecraftFields',
    'dev.slinky.client.event.SemanticEvent': 'dev.rice.client.SemanticEvent',
    'dev.slinky.client.hook.GameHooks': 'dev.rice.client.hook.GameHooks',
    'dev.slinky.client.input.AttackInput': 'dev.rice.client.AttackInput',
    'dev.slinky.client.input.KeyState': 'dev.rice.client.KeyState',
    'dev.slinky.client.numeric.NumericMath': 'dev.rice.client.RecoveredMath',
    'dev.slinky.client.packet.PredictionGeometry': 'dev.rice.client.packet.PredictionGeometry',
    'dev.slinky.client.packet.TimingAccess': 'dev.rice.client.packet.TimingAccess',
    'dev.slinky.client.rotation.RotationMath': 'dev.rice.client.rotation.RotationMath',
    'dev.slinky.client.rotation.RotationRandom': 'dev.rice.client.rotation.RecoveredRandom',
    'dev.slinky.module.CatalogModule': 'dev.rice.module.EvidenceModule',
    'dev.slinky.module.ModuleCategory': 'dev.rice.module.ModuleCategory',
    'dev.slinky.module.ModuleManager': 'dev.rice.module.ModuleManager',
    'dev.slinky.module.catalog.StatusText': 'dev.rice.client.StatusText',
}
IDENTIFIERS = [('CatalogModule', 'EvidenceModule'), ('NumericMath', 'RecoveredMath'), ('RotationRandom', 'RecoveredRandom')]


def port(text):
    text = text.replace('package dev.slinky.module.combat;', 'package dev.rice.module.impl;')

    def imp(m):
        name = m.group(1)
        if name.startswith('dev.slinky.'):
            if name not in IMPORTS:
                raise SystemExit('unmapped import ' + name)
            return 'import ' + IMPORTS[name] + ';'
        return m.group(0)

    text = re.sub(r'^import ([\w.]+);', imp, text, flags=re.M)
    for old, new in IDENTIFIERS:
        text = re.sub(r'(?<![\w$])' + old + r'(?![\w$])', new, text)
    if 'dev.slinky' in text:
        raise SystemExit('unmapped dev.slinky reference left')
    return text


# The packet pipeline TimerRange's freeze and Criticals' delay both run through. It is checked against upstream
# too; only the package/imports and the Netty handler name differ.
PIPELINE = 'dev/slinky/client/packet/PacketPipeline.java'
PIPELINE_OUT = ROOT / 'java' / 'src' / 'main' / 'java' / 'dev' / 'rice' / 'client' / 'packet' / 'PacketPipeline.java'
PIPELINE_NAME = ('public static final String NAME = "slinky_semantic_packets";', 'public static final String NAME = "rice_semantic_packets";')


def port_pipeline(text):
    text = text.replace('package dev.slinky.client.packet;', 'package dev.rice.client.packet;')
    text = re.sub(r'^import ([\w.]+);', lambda m: 'import ' + IMPORTS.get(m.group(1), m.group(1)) + ';', text, flags=re.M)
    if text.count(PIPELINE_NAME[0]) != 1:
        raise SystemExit('PacketPipeline NAME line not found exactly once')
    text = text.replace(*PIPELINE_NAME)
    if 'dev.slinky' in text:
        raise SystemExit('unmapped dev.slinky reference left in PacketPipeline')
    return text


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    src = Path(sys.argv[1]) / 'dev' / 'slinky' / 'module' / 'combat'
    check = '--check' in sys.argv
    manifest = {'upstream': 'https://github.com/Skid-Technology/OpenSlinky', 'commit': UPSTREAM_COMMIT, 'files': {}}
    outputs = {}
    for name in FILES:
        raw = (src / name).read_bytes()
        outputs[OUT / name] = port(raw.decode('utf-8'))
        manifest['files'][name] = {'upstream_sha256': hashlib.sha256(raw).hexdigest(),
                                   'renames': IDENTIFIERS and [list(x) for x in IDENTIFIERS]}
    raw = (Path(sys.argv[1]) / PIPELINE).read_bytes()
    outputs[PIPELINE_OUT] = port_pipeline(raw.decode('utf-8'))
    manifest['pipeline'] = {'upstream': PIPELINE, 'upstream_sha256': hashlib.sha256(raw).hexdigest(),
                            'rewrites': ['package/imports dev.slinky -> dev.rice', list(PIPELINE_NAME)]}
    if check:
        bad = [str(p.relative_to(ROOT)) for p, t in outputs.items() if not p.exists() or p.read_text(encoding='utf-8') != t]
        if json.loads(MANIFEST.read_text(encoding='utf-8')) != manifest:
            bad.append('tools/openslinky-port-manifest.json')
        if bad:
            raise SystemExit('OpenSlinky port drifted from upstream:\n  ' + '\n  '.join(bad))
        print('PASS: TimerRange/Criticals/PacketPipeline equal OpenSlinky %s + whitelisted renames' % UPSTREAM_COMMIT[:8])
        return
    for p, t in outputs.items():
        if p == PIPELINE_OUT and p.read_text(encoding='utf-8') == t:
            continue
        p.write_text(t, encoding='utf-8')
    MANIFEST.write_text(json.dumps(manifest, indent=1, sort_keys=True), encoding='utf-8')
    print('generated', ', '.join(FILES))


if __name__ == '__main__':
    main()
