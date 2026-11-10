"""Generate (or verify) the byte-faithful OpenJade port used by Scaffold, Clutch and Opsec.

Upstream: https://github.com/Tewxx/OpenJade at commit af7f296a9b3e8a69fe562a072afec8d2d5870224
(JadeClient/src/main/java). The upstream tree is read-only input; nothing from it is executed.

Every generated file is the upstream source with ONLY these mechanical rewrites:
  1. package relocation  jade.* / net.jade.dev.agent.transformer.* -> dev.rice.jade.* / dev.rice.jade.agent.*
  2. Jade's shaded gson   jade.deps.gson.* -> com.google.gson.*
  3. mixin accessor casts (IAccessorX)expr -> IAccessorX.of(expr)   (mixin interfaces cannot be cast to
     outside Mixin; the host IAccessorX interfaces expose the same methods)
  4. unused imports dropped
  5. member-level classes keep only the listed members (verbatim text) under their original declaration,
     or under a declared host shell for the three classes whose remaining members need one
  6. "stubs": a member whose every call path is unreachable under the Rice host (each stub records why)
     is replaced by the given body, so code that can never run (e.g. Badlion-only IRC chat routing) is
     not shipped
  7. org.jetbrains @NotNull/@Nullable annotations dropped (no runtime effect; not on Rice's classpath)
  8. "rewrites": exact, counted substring edits, each with a recorded reason, only for upstream source that
     cannot compile as-is (decompiler artefacts, Gson 2.2.4 vs Jade's newer shaded Gson); semantics unchanged
Classes Jade owns globally (Jade, ModuleManager, Gui, ...) are hand-written host shims under
java/src/main/java/dev/rice/jade (listed in the config as "shims"); they are NOT generated.

usage: python tools/port-openjade.py <upstream JadeClient/src/main/java> [--check]
"""
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / 'tools' / 'openjade-port-config.json'
OUT = ROOT / 'java' / 'src' / 'main' / 'java'
MANIFEST = ROOT / 'tools' / 'openjade-port-manifest.json'
UPSTREAM_COMMIT = 'af7f296a9b3e8a69fe562a072afec8d2d5870224'


def strip_comments_strings(text):
    out = list(text)
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        if text.startswith('//', i):
            j = text.find('\n', i)
            j = n if j < 0 else j
            for k in range(i, j):
                out[k] = ' '
            i = j
        elif text.startswith('/*', i):
            j = text.find('*/', i + 2)
            j = n if j < 0 else j + 2
            for k in range(i, j):
                if out[k] != '\n':
                    out[k] = ' '
            i = j
        elif c in '"\'':
            j = i + 1
            while j < n and text[j] != c:
                j += 2 if text[j] == '\\' else 1
            for k in range(i + 1, min(j, n)):
                out[k] = ' '
            i = j + 1
        else:
            i += 1
    return ''.join(out)


def split_members(text):
    """Split a top-level type into (prefix up to and incl. the body '{', [member texts], suffix from the closing '}')."""
    clean = strip_comments_strings(text)
    m = re.search(r'^(public |final |abstract |strictfp )*(class|interface|enum|@interface) [\w$]+[^{]*\{', clean, re.M)
    open_i = m.end() - 1
    depth, i, close_i = 0, open_i, None
    while i < len(clean):
        if clean[i] == '{':
            depth += 1
        elif clean[i] == '}':
            depth -= 1
            if depth == 0:
                close_i = i
                break
        i += 1
    members, seg_start, depth, paren = [], open_i + 1, 0, 0
    is_enum = m.group(2) == 'enum'
    if is_enum:
        # the enum constant list is the first member: everything up to the first top-level ';'
        # (or the whole body when the enum declares nothing else)
        d = p = 0
        end = None
        for i in range(open_i + 1, close_i):
            c = clean[i]
            if c == '(':
                p += 1
            elif c == ')':
                p -= 1
            elif c == '{':
                d += 1
            elif c == '}':
                d -= 1
            elif c == ';' and d == 0 and p == 0:
                end = i + 1
                break
        if end is None:
            return text[:open_i + 1], [text[open_i + 1:close_i]], text[close_i:], m
        members.append((seg_start, end))
        seg_start = end
    for i in range(seg_start, close_i):
        c = clean[i]
        if c == '(':
            paren += 1
        elif c == ')':
            paren -= 1
        elif c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0 and paren == 0:
                head = clean[seg_start:i]
                first = head.find('{')
                # a field whose initializer contains braces (array literal / anonymous class) ends at ';'
                if first >= 0 and '=' in head[:first] and '(' not in head[:head.find('=')]:
                    continue
                members.append((seg_start, i + 1))
                seg_start = i + 1
        elif c == ';' and depth == 0 and paren == 0:
            members.append((seg_start, i + 1))
            seg_start = i + 1
    return text[:open_i + 1], [text[a:b] for a, b in members if text[a:b].strip()], text[close_i:], m


def member_name(seg):
    cs = strip_comments_strings(seg)
    hdr = cs.split('{', 1)[0]
    if re.match(r'\s*(static\s*)?\{', cs) and not re.search(r'\w\s*\(', hdr):
        return '<clinit>'
    t = re.search(r'\b(class|interface|enum)\s+([\w$]+)', hdr)
    if t and '(' not in hdr.split('=')[0]:
        return t.group(2)
    if '=' in hdr.split('(')[0] or (';' in cs and '(' not in hdr):
        names = re.findall(r'([\w$]+)\s*(?==|;|,)', cs.split('=')[0] + ';')
        return names[-1] if names else '?'
    mm = re.search(r'([\w$]+)\s*\(', hdr)
    return mm.group(1) if mm else '?'


RELOCATE = [
    (re.compile(r'(?<![\w.])jade\.deps\.gson\.'), 'com.google.gson.'),
    (re.compile(r'(?<![\w.])net\.jade\.dev\.agent\.transformer\b'), 'dev.rice.jade.agent'),
    (re.compile(r'(?<![\w.])jade\.(client|deps|mixin|inject)\b'), r'dev.rice.jade.\1'),
]
CAST = re.compile(r'\((IAccessor(?:EntityPlayerSP|Minecraft|PlayerControllerMP))\)\s*((?:this\.)?[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)')


def relocate(text):
    out = []
    for line in text.split('\n'):
        if not line.startswith('// Jade recovery:'):  # provenance comments keep naming the upstream class
            for pattern, repl in RELOCATE:
                line = pattern.sub(repl, line)
        out.append(line)
    return '\n'.join(out)


ANNOTATIONS = re.compile(r'@(?:org\.jetbrains\.annotations\.)?(?:NotNull|Nullable)\b\s*')


def strip_annotations(text):
    text = re.sub(r'^import org\.jetbrains\.annotations\.[\w$]+;\n', '', text, flags=re.M)
    return ANNOTATIONS.sub('', text)


def apply_rewrites(fq, raw, cfg):
    applied = []
    for rw in cfg.get('rewrites', []):
        if rw['class'] != fq:
            continue
        count = raw.count(rw['old'])
        if count != rw.get('count', 1):
            raise SystemExit('rewrite in %s expected %d match(es), found %d: %r' % (fq, rw.get('count', 1), count, rw['old']))
        raw = raw.replace(rw['old'], rw['new'])
        applied.append(rw['reason'])
    return raw, applied


def prune_imports(text):
    body = re.sub(r'^import [^;]+;\n', '', text, flags=re.M)
    body_clean = strip_comments_strings(body)
    keep = []
    for line in text.split('\n'):
        mm = re.match(r'import (static )?([\w.$]+)\.([\w$*]+);$', line)
        if not mm:
            keep.append(line)
            continue
        simple = mm.group(3)
        if simple == '*' or re.search(r'(?<![\w$])' + re.escape(simple) + r'(?![\w$])', body_clean):
            keep.append(line)
    return '\n'.join(keep)


def upstream_file(src, fq):
    if fq.startswith('net.jade.'):
        return src / (fq.replace('.', '/') + '.java')
    return src / (fq.replace('.', '/') + '.java')


def target_file(fq):
    rel = relocate(fq)
    return OUT / (rel.replace('.', '/') + '.java')


def generate(src, cfg):
    outputs, manifest = {}, {'upstream': 'https://github.com/Tewxx/OpenJade', 'commit': UPSTREAM_COMMIT, 'files': {}}
    for fq in cfg['whole']:
        p = upstream_file(src, fq)
        raw = p.read_text(encoding='utf-8')
        raw, rewrites = apply_rewrites(fq, raw, cfg)
        text = CAST.sub(lambda m: m.group(1) + '.of(' + m.group(2) + ')', raw)
        casts = len(CAST.findall(raw))
        text = prune_imports(relocate(strip_annotations(text)))
        outputs[target_file(fq)] = text
        manifest['files'][fq] = {'mode': 'whole', 'sha256': hashlib.sha256(p.read_bytes()).hexdigest(), 'accessor_casts': casts, 'rewrites': rewrites}
    for fq, names in cfg['members'].items():
        p = upstream_file(src, fq)
        raw = p.read_text(encoding='utf-8')
        raw, rewrites = apply_rewrites(fq, raw, cfg)
        prefix, segs, suffix, decl = split_members(raw)
        wanted = set(names)
        if any(member_name(s) == '<clinit>' for s in segs):
            wanted.add('<clinit>')  # static initialisers always travel with their class's static state
        stubs = cfg.get('stubs', {}).get(fq, {})
        picked = [s for s in segs if member_name(s) in wanted]
        if decl.group(2) == 'enum' and segs and segs[0] not in picked:
            picked.insert(0, segs[0])  # enum constants always travel with the enum
        for stub_name, stub in stubs.items():
            idx = [i for i, s in enumerate(picked) if member_name(s) == stub_name]
            if len(idx) != 1:
                raise SystemExit('stub target %s.%s must match exactly one member' % (fq, stub_name))
            lead = re.match(r'\s*', picked[idx[0]]).group(0)
            picked[idx[0]] = lead + stub['body'].strip('\n')
        found = {member_name(s) for s in picked}
        missing = wanted - found
        if missing:
            raise SystemExit('missing members in %s: %s' % (fq, sorted(missing)))
        shell = cfg.get('shells', {}).get(fq)
        if shell:
            prefix = prefix[:decl.start()] + shell['decl'] + ' {'
            extra = '\n' + shell['extra'].rstrip() + '\n'
        else:
            # keep the original declaration; only "extends Module" is dropped (its constructor is not extracted),
            # interfaces such as IMinecraft (which supplies the mc constant) are kept
            d = prefix[decl.start():]
            d2 = re.sub(r'\s+extends\s+Module\b', '', d.rstrip())
            prefix = prefix[:decl.start()] + d2
            extra = ''
        body = ''.join(picked)
        text = prefix + extra + body + '\n' + suffix
        casts = len(CAST.findall(text))
        text = CAST.sub(lambda m: m.group(1) + '.of(' + m.group(2) + ')', text)
        text = prune_imports(relocate(strip_annotations(text)))
        outputs[target_file(fq)] = text
        manifest['files'][fq] = {'mode': 'members', 'sha256': hashlib.sha256(p.read_bytes()).hexdigest(), 'rewrites': rewrites,
                                 'members': sorted(found), 'member_sha256': {member_name(s): hashlib.sha256(s.encode('utf-8')).hexdigest() for s in picked},
                                 'accessor_casts': casts, 'shell': bool(shell),
                                 'stubs': {k: v['reason'] for k, v in stubs.items()}}
    manifest['shims'] = cfg['shims']
    return outputs, manifest


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    src = Path(sys.argv[1])
    check = '--check' in sys.argv
    head = src.parents[3] / '.git' / 'HEAD' if len(src.parents) > 3 else None
    cfg = json.loads(CONFIG.read_text(encoding='utf-8'))
    outputs, manifest = generate(src, cfg)
    generated = {str(p.relative_to(OUT)).replace('\\', '/') for p in outputs}
    manifest['generated'] = sorted(generated)
    if check:
        bad = []
        for p, text in outputs.items():
            if not p.exists() or p.read_text(encoding='utf-8') != text:
                bad.append(str(p.relative_to(ROOT)))
        old = json.loads(MANIFEST.read_text(encoding='utf-8'))
        if old != manifest:
            bad.append('tools/openjade-port-manifest.json')
        jade_root = OUT / 'dev' / 'rice' / 'jade'
        shims = set(cfg['shims'])
        for p in jade_root.rglob('*.java'):
            rel = str(p.relative_to(OUT)).replace('\\', '/')
            if rel not in generated and rel not in shims:
                bad.append('unlisted file ' + rel)
        if bad:
            raise SystemExit('OpenJade port drifted from upstream:\n  ' + '\n  '.join(bad))
        print('PASS: %d generated OpenJade files equal upstream %s + whitelisted rewrites; %d host shims listed' % (len(outputs), UPSTREAM_COMMIT[:8], len(shims)))
        return
    for p, text in outputs.items():
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(text, encoding='utf-8')
    MANIFEST.write_text(json.dumps(manifest, indent=1, sort_keys=True), encoding='utf-8')
    print('generated %d files' % len(outputs))


if __name__ == '__main__':
    main()
