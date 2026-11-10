"""Keep supplied movement policy; replace Scaffold planning with the isolated Slinky port."""
from pathlib import Path
import hashlib,json,subprocess,zipfile
ROOT=Path(__file__).resolve().parent
REFERENCE=ROOT.parent/'scaffold-supplied-20261103'
OTHER=Path(r'C:/Users/ethan/Documents/ricerakak slionky/Slinky-cross-platform-source/Slinky-source')
JDK=Path(r'C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin')
HELPERS=['com/snowify/module/player/scaffold/Blocks.class',
         'com/snowify/module/player/scaffold/Blocks$1.class',
         'com/snowify/module/player/scaffold/BlockCounter.class','com/snowify/util/WorldDraw.class']

def prepare(classes):
    lock=json.loads((REFERENCE/'source-lock.json').read_text())
    sources=[]
    for name,digest in lock.items():
        p=REFERENCE/name
        if hashlib.sha256(p.read_bytes()).hexdigest()!=digest:raise ValueError('Supplied source changed: '+name)
        if name=='Scaffold.java':
            from prepare_slinky_scaffold import prepare as prepare_backend
            sources.extend(prepare_backend(p))
        else:sources.append(p)
    out=ROOT/'build/supplied';out.mkdir(parents=True,exist_ok=True)
    compiled=REFERENCE/'compiled';compiled.mkdir(exist_ok=True)
    helper_source=Path(r'C:/Users/ethan/Downloads/snowify-1.13.55')
    hashes={}
    for name in HELPERS:
        target=compiled/name;target.parent.mkdir(parents=True,exist_ok=True)
        if not target.exists():target.write_bytes((helper_source/name).read_bytes())
        hashes[name]=hashlib.sha256(target.read_bytes()).hexdigest()
    helper_lock=REFERENCE/'helper-lock.json'
    if helper_lock.exists():
        if hashes!=json.loads(helper_lock.read_text()):raise ValueError('Compiled helper changed')
    else:helper_lock.write_text(json.dumps(hashes,indent=2)+'\n')
    mappings=(OTHER/'java/mappings/srg.txt').read_text()
    for name in ['module/player/scaffold/Blocks','module/player/scaffold/Blocks$1',
                 'module/player/scaffold/BlockCounter','util/WorldDraw','gui/Theme','gui/HudStyle']:
        mappings+='\nCL\tcom/snowy/'+name+'\tcom/snowify/'+name
    mapping=out/'helper-mappings.txt';mapping.write_text(mappings+'\n')
    original=out/'helpers-srg.jar'
    with zipfile.ZipFile(original,'w') as jar:
        for name in HELPERS:jar.write(compiled/name,name)
    asm=list((OTHER/'deps/tooling').glob('asm*.jar'))
    subprocess.run([str(JDK/'javac.exe'),'--release','8','-cp',';'.join(map(str,asm)),
                    '-d',str(out),str(OTHER/'tools/RemapJar.java')],check=True)
    remapped=out/'helpers-named.jar'
    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[out,*asm])),'RemapJar',
                    str(mapping),'reverse',str(original),str(remapped),str(OTHER/'build/minecraft-1.8.9-named.jar')],check=True)
    with zipfile.ZipFile(remapped) as jar:
        for name in jar.namelist():
            target=classes/name;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(jar.read(name))
    (ROOT/'build/supplied-source-manifest.json').write_text(json.dumps({
        'sources_sha256':lock,'source_files_compiled_unchanged':3,'scaffold_backend':json.loads((ROOT/'build/slinky-scaffold-manifest.json').read_text()),'original_helper_sha256':hashes,
        'helper_adaptation':'Package relocation com.snowify -> com.snowy; Minecraft SRG -> named. No helper behavior edits.',
        'host_adapters':['settings and lifecycle','native event dispatch','Claims for connected modules',
                         'native HUD theme','placement observers (no foreign packet queue)','mixin liveness via native counters']
    },indent=2)+'\n')
    return sources
