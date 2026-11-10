"""Clone Slinky Clutch targeting into Scaffold without changing either source archive."""
from pathlib import Path
import hashlib,json,re,struct
ROOT=Path(__file__).resolve().parent
NATIVE=ROOT.parent/'slinkyreadablesource/slinkyreadablesource'

def replace_method(text,name,body):
    match=re.search(r'    private (?:static )?[^\n]+\b'+name+r'\([^\n]*\) \{',text)
    if not match:raise ValueError('Missing supplied Scaffold method '+name)
    left=text.index('{',match.start());end=left+1;depth=1
    while depth:
        if text[end]=='{':depth+=1
        elif text[end]=='}':depth-=1
        end+=1
    return text[:left]+'{\n'+body+'\n    }'+text[end:]

def method_source(text,name):
    match=re.search(r'^    (?:private|protected|public) (?:static )?[^\n]+\b'+name+r'\([^\n]*\) \{',text,re.M)
    if not match:raise ValueError('Missing policy method '+name)
    left=text.index('{',match.start());end=left+1;depth=1
    while depth:
        if text[end]=='{':depth+=1
        elif text[end]=='}':depth-=1
        end+=1
    return text[match.start():end]

def prepare(source):
    out=ROOT/'build/slinky-scaffold';out.mkdir(exist_ok=True)
    original=(NATIVE/'original/slinky-main/src/logic/block/clutch.cpp').read_bytes()
    # Use the actual archived candidate ordering, not a newly invented search cube.
    data=(NATIVE/'src/data/rdata.s').read_text().split('rdata_156290:',1)[1].split('.globl',1)[0]
    values=bytes(int(value,16) for value in re.findall(r'0x([0-9a-fA-F]{2})',data))
    offsets=struct.unpack('<'+str(len(values)//2)+'H',values)
    # clutch_candidates_within_distance_n[4] = 79 entries.
    offsets=offsets[:79]
    generated=out/'SlinkyClutchOffsets.java'
    generated.write_text('package com.snowy.module.player.scaffold;\nfinal class SlinkyClutchOffsets {static final int[] OFFSETS={'+','.join(map(str,offsets))+'};}\n')
    upstream=source.read_text()
    text=upstream
    text=text.replace('    private static List<Target> locate(', '    private List<Target> locate(')
    text=text.replace('    private final BlockCounter blocks',
        '    private final com.snowy.module.player.scaffold.SlinkyScaffoldBackend backend=new com.snowy.module.player.scaffold.SlinkyScaffoldBackend();\n\n    private final BlockCounter blocks')
    text=text.replace('    protected void onEnable() {','    protected void onEnable() {\n        backend.reset();')
    text=replace_method(text,'locate','''        List<Target> found=new ArrayList<Target>();
        RotationManager rotation=RotationManager.get();
        for(com.snowy.module.player.scaffold.SlinkyScaffoldBackend.Target candidate:backend.locate(mc,spot,feetX,feetY,feetZ,rotation.getSentYaw(),rotation.getSentPitch())) {
            Target target=new Target(candidate.support,candidate.face);
            target.hit=candidate.point;found.add(target);
        }
        return found;''')
    text=replace_method(text,'aimAt','''        Vec3 point=where.hit;
        if(point==null)return null;
        // Slinky supplies the face point; retain Mayu's turn easing and 45-degree movement policy.
        float[] look=turn(point.xCoord-eye.xCoord,point.yCoord-eye.yCoord,point.zCoord-eye.zCoord,fromYaw,fromPitch);
        if(!Float.isNaN(steps))look[0]=near(steps+Math.round(MathHelper.wrapAngleTo180_float(look[0]-steps)/45.0F)*45.0F,fromYaw);
        MovingObjectPosition hit=Blocks.trace(mc,eye,look[0],look[1],mc.playerController.getBlockReachDistance());
        if(!landsInside(hit,where,CLICK_MARGIN))return null;
        Target result=new Target(where.against,where.face);
        result.hit=hit.hitVec;result.yaw=look[0];result.pitch=look[1];return result;''')
    # Remove the superseded Java face selector, so there is only one active planner.
    text=text.replace(method_source(text,'bestFace'),'')
    retained=[]
    for name in re.findall(r'^    (?:private|protected|public) (?:static )?[^\n]+\b(\w+)\([^\n]*\) \{',upstream,re.M):
        if name in {'locate','aimAt','bestFace','onEnable','Target','Scaffold'}:continue
        if method_source(upstream,name)!=method_source(text,name):raise ValueError('Mayu policy changed: '+name)
        retained.append(name)
    target=out/'Scaffold.java';target.write_text(text)
    manifest={'backend':'Independent Java port of Slinky Clutch targeting',
        'clutch_source_sha256':hashlib.sha256(original).hexdigest(),
        'candidate_offsets':list(offsets),'candidate_range':4,
        'adaptations':['Scaffold-owned state; Clutch unchanged','Mayu supplies height/predicted feet and rotation policy',
            'deterministic multipoint face aim (randomisation=0)','host collision/placement validation and ray checks',
            'retained Mayu movement, tower, Keep Y, switching, lifecycle and HUD'],
        'adapted_scaffold_sha256':hashlib.sha256(target.read_bytes()).hexdigest(),
        'retained_policy_methods':retained,
        'backend_sha256':hashlib.sha256((ROOT/'java/SlinkyScaffoldBackend.java').read_bytes()).hexdigest()}
    (ROOT/'build/slinky-scaffold-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
    return [target,generated,ROOT/'java/SlinkyScaffoldBackend.java']
