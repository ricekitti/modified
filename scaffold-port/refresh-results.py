"""Record the verified candidate only after all suites and resource checks pass."""
from datetime import datetime, timezone
from pathlib import Path
import hashlib
import json
import re

root = Path(__file__).resolve().parent
modified = root.parent
java_log = (root / 'validation-build.log').read_text(errors='replace')
native_log = (root / 'validation-native.log').read_text(errors='replace')
markers = {
    'behavior': 'ALL SCAFFOLD BEHAVIOR CHECKS PASSED',
    'bridge': 'ALL PHASE-1 CHECKS PASSED',
    'slots': 'ALL SCAFFOLD SLOT CHECKS PASSED',
    'timing': 'ALL SCAFFOLD TIMING CHECKS PASSED',
    'telly': 'ALL SCAFFOLD TELLY CHECKS PASSED',
    'hud': 'ALL SCAFFOLD HUD CHECKS PASSED',
    'safe_walk': 'ALL SCAFFOLD SAFE WALK CHECKS PASSED',
    'watchdog': 'ALL SCAFFOLD WATCHDOG CHECKS PASSED',
    'parent': 'ALL SCAFFOLD PARENT CHECKS PASSED',
}
counts = {name: java_log.count(marker) for name, marker in markers.items()}
assert all(value == 3 for value in counts.values()), counts
assert not re.search(r'^Exception|error:|AssertionError', java_log, re.M)
assert '100% tests passed, 0 tests failed out of 20' in native_log

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

dll = modified / 'slinkyreadablesource/slinkyreadablesource/build/slinky.dll'
embedded = modified / 'provided-injector/rice-launcher/src/bundled/RiceNative.dll'
candidate_dll = modified / 'slinky-scaffold-partial.dll'
launcher = modified / 'provided-injector/rice-launcher/build/rice-loader.exe'
candidate_launcher = modified / 'rice-loader-scaffold-partial.exe'
assert digest(dll) == digest(embedded) == digest(candidate_dll)
assert digest(launcher) == digest(candidate_launcher)
artifacts = [candidate_launcher, candidate_dll, *[
    modified / f'feature-bridge/build/features-{name}.jar' for name in ('named', 'srg', 'official')
]]
sources = [*sorted((modified / 'feature-bridge/java').glob('Scaffold*.java')),
           modified / 'feature-bridge/java/NativeFeatureBridge.java',
           modified / 'feature-bridge/build.py',
           *sorted((modified / 'feature-bridge/tests').glob('Scaffold*.java')),
           modified / 'feature-bridge/tests/FeatureFixture.java',
           modified / 'feature-bridge/tests/FogGlStubs.java',
           modified / 'feature-bridge/tests/FogHookCheck.java',
           modified / 'provided-injector/rice-launcher/tests/SourceFeaturesJvmTest.cpp']
for name in ('logic/combat/source_features.cpp', 'engine/base/java_natives_world.cpp',
             'engine/base/java_natives_frame.cpp', 'engine/jni/hook_installer.cpp',
             'engine/jni/hook_tables.inc', 'engine/jni/classfile_hooks.cpp', 'data/rdata.s'):
    sources.append(modified / 'slinkyreadablesource/slinkyreadablesource/src' / name)
result = {
    'status': 'PARTIAL - full port unfinished; no live gameplay validation',
    'recorded_utc': datetime.now(timezone.utc).isoformat(),
    'repository': 'ricekitti/rise-open',
    'reference_commit': 'c08acf7223ce9a6cde453cf86f97d77d92c742ba',
    'dump_provenance': 'unverified',
    'bootstrap': 'SlinkyHooks17', 'native_declarations': 34,
    'parent_settings': 39, 'implemented_child_settings': 2, 'native_rows': 49,
    'unavailable_selections': [],
    'unresolved_source': ['Vk/Wa empty branches and original cadence', 'Place Extra blocks missing placement output', 'Rise cross-module dependencies', 'Failed parent methods and dump version provenance'],
    'artifacts': [{'path': str(path), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in artifacts],
    'sources': {str(path.relative_to(modified)): digest(path) for path in sources},
    'reference_helpers': {str(path.relative_to(root)): digest(path) for path in sorted((root / 'rise-source/helpers').glob('*.java'))},
    'java_validation': {'suite_passes_by_type': counts, 'total_suite_executions_passed': sum(counts.values()),
                        'target': 'Java 8', 'mappings': ['named', 'srg', 'official'],
                        'log': str(root / 'validation-build.log')},
    'native_validation': {'passed': 20, 'failed': 0, 'log': str(root / 'validation-native.log')},
    'live_validation_performed': False,
}
(root / 'BUILD-RESULTS.json').write_text(json.dumps(result, indent=2) + '\n')
print('Recorded 27 passing Java suite executions, 20 native tests and matching candidate artifacts.')
