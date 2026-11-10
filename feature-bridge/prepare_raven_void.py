"""Extract the production Void scanner from the user's pinned Raven bS source."""
from pathlib import Path
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parent
REFERENCE = ROOT.parent / 'raven-bs-void-20261103'
METHODS = ['findBestVoidYaw', 'selectBetterVoidYaw', 'scoreVoidPath',
           'getVoidNeighborhood', 'getVoidPathBlockedDistance', 'isOneBlockWall',
           'isVoidColumn', 'getColumnKey']
CLASSES = ['VoidYawCandidate', 'VoidPathScore', 'VoidNeighborhood']


def closing(text, opening):
    depth = 1
    i = opening + 1
    while depth:
        if text[i] == '{': depth += 1
        elif text[i] == '}': depth -= 1
        i += 1
    return i


def extract(text, pattern):
    match = re.search(pattern, text)
    if not match: raise ValueError('Missing Raven source: ' + pattern)
    opening = text.index('{', match.start())
    return text[match.start():closing(text, opening)]


def prepare(adapter_reference):
    source = REFERENCE / 'Displace.java'
    expected = json.loads((REFERENCE / 'source-lock.json').read_text())['Displace.java']
    if hashlib.sha256(source.read_bytes()).hexdigest() != expected:
        raise ValueError('Pinned Raven Displace source changed')
    original = source.read_text()
    chunks = [extract(original, r'    private [^\n]+\b' + name + r'\(') for name in METHODS]
    chunks += [extract(original, r'    private static final class ' + name + r'\b') for name in CLASSES]
    body = '\n\n'.join(chunks)
    # Debug snapshots/probes are observers only; the scanner keeps all production math.
    body = re.sub(r'        VoidDebugScan debugScan = Raven.DEBUG.*?: null;', '', body, flags=re.S)
    body = re.sub(r'        VoidDebugCandidate debugCandidate = debugScan == null.*?;', '', body, flags=re.S)
    while True:
        match = re.search(r'if\s*\(debug(?:Scan|Candidate)\s*(?:!=|==)\s*null\)\s*\{', body)
        if not match: break
        opening = body.index('{', match.start())
        body = body[:match.start()] + body[closing(body, opening):]
    body = re.sub(r'^\s*latestVoidDebugScan = debugScan;\s*$', '', body, flags=re.M)
    body = re.sub(r',\s*VoidDebug(?:Scan|Candidate) debug(?:Scan|Candidate)', '', body)
    body = re.sub(r',\s*debug(?:Scan|Candidate)(?=\))', '', body)
    body = re.sub(r'^\s*private final VoidDebugCandidate debugCandidate;\s*$', '', body, flags=re.M)
    body = re.sub(r'^\s*this\.debugCandidate = debugCandidate;\s*$', '', body, flags=re.M)
    body = body.replace('EntityPlayer target', 'Entity target')
    body = body.replace('yawOffset.getInput()', 'angle').replace('scanRadius.getInput()', 'radius')
    body = body.replace('ignoreOneBlockWall.isToggled()', 'ignoreOneBlockWall')
    body = body.replace('mc.theWorld.getCollisionBoxes(checkBox)', 'blockCollisions(checkBox)')
    if 'VoidDebug' in body or 'debugCandidate' in body or 'debugScan' in body:
        raise ValueError('Unadapted Raven debug dependency')
    header = adapter_reference.read_text().split('    private Float findBestVoidYaw', 1)[0]
    result = header + body + '\n}\n'
    manifest = {'reference_sha256': expected, 'methods': METHODS, 'classes': CLASSES,
                'adaptations': ['host constructor/API', '1.8.9 block collision API',
                                'debug observers omitted', 'optional existing Legit Void filter added by build.py']}
    (ROOT / 'build/raven-void-source-manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
    return result
