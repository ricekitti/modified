"""Reproducible source port of supplied InjectMyau Scaffold; no target execution."""
from pathlib import Path
import argparse, re, json, hashlib
ROOT=Path(__file__).resolve().parents[1]
FIXTURE=ROOT/'tools/fixtures/injectmyau'
OUT=ROOT/'java/src/main/java/dev/rice/client/scaffold/injectmyau'
PREFIX='dev.rice.client.scaffold.injectmyau'
SPEC={'copies': ['module/modules/Scaffold.java', 'management/RotationState.java', 'management/RotationManager.java', 'management/LateRotation.java', 'util/RiseRotation.java', 'util/RotationUtil.java', 'util/BlockUtil.java', 'util/RandomUtil.java', 'util/KeyBindUtil.java', 'property/Property.java', 'property/properties/BooleanProperty.java', 'property/properties/FloatProperty.java', 'property/properties/ModeProperty.java', 'property/properties/PercentProperty.java', 'event/EventTarget.java', 'event/types/EventType.java', 'event/types/Priority.java', 'event/events/Event.java', 'event/events/Cancellable.java', 'event/events/callables/EventCancellable.java', 'events/UpdateEvent.java', 'events/PlayerUpdateEvent.java', 'events/LivingUpdateEvent.java', 'events/MoveInputEvent.java', 'events/StrafeEvent.java', 'events/SafeWalkEvent.java', 'events/Render2DEvent.java', 'events/Render3DEvent.java', 'events/TickEvent.java', 'events/LeftClickMouseEvent.java', 'events/RightClickMouseEvent.java', 'events/HitBlockEvent.java', 'events/SwapItemEvent.java'], 'partial': {'util/MoveUtil.java': ['isForwardPressed', 'getForwardValue', 'getLeftValue', 'getMoveYaw', 'adjustYaw', 'getDirectionYaw', 'getSpeed', 'setSpeed', 'getSpeedLevel'], 'util/PlayerUtil.java': ['isUsingItem', 'canMove', 'isAirBelow', 'isAirAbove'], 'util/ItemUtil.java': ['isBlock', 'isContainerBlock', 'isHoldingBlock']}, 'hosts': ['Myau.java', 'module/Module.java', 'module/modules/BedNuker.java', 'module/modules/LongJump.java', 'module/modules/TargetStrafe.java', 'module/modules/HUD.java', 'access/AccessorEntity.java', 'inject/Log.java', 'util/PacketUtil.java']}

def body(s,start):
    brace=s.index('{',start);end=brace+1;depth=1
    while depth:
        c=s[end];end+=1
        if c=='{':depth+=1
        elif c=='}':depth-=1
    return s[start:end]

def generate():
    result={};manifest={}
    for rel in SPEC['copies']+list(SPEC['partial']):
        data=(FIXTURE/rel).read_bytes();s=data.decode('utf-8')
        if rel in SPEC['partial']:
            names=SPEC['partial'][rel];methods=[]
            for m in re.finditer(r'    public static [^\n]+? (\w+)\s*\(',s):
                if m.group(1) in names:methods.append(body(s,m.start()))
            cls=Path(rel).stem
            imports='import net.minecraft.client.Minecraft;import net.minecraft.util.*;import net.minecraft.item.*;import net.minecraft.block.Block;import net.minecraft.potion.Potion;import myau.Myau;import myau.module.modules.TargetStrafe;import myau.management.RotationState;'
            s='package myau.util;\n'+imports+'\npublic class '+cls+' {\nprivate static final Minecraft mc=Minecraft.getMinecraft();\n'+'\n'.join(methods)+'\n}\n'
        count=s.count('EnumFacing.VALUES');s=s.replace('EnumFacing.VALUES','EnumFacing.values()')
        s=re.sub(r'\bmyau(?=\.)',PREFIX,s)
        if rel=='module/modules/Scaffold.java':
            # Deliberate Rice behavior changes requested after the supplied-source port.
            start=s.index('    private void place(')
            original=body(s,start)
            adapted=original
            for block in re.findall(r'            if \(grim\) \{',original):
                begin=adapted.index(block);section=body(adapted,begin);adapted=adapted.replace(section,'\n'*section.count('\n'),1)
            adapted=adapted.replace('            boolean grim = this.rotationSubmit.getValue() == SUBMIT_GRIM;', '')
            s=s.replace(original,adapted)
            s=s.replace('mc.playerController.onPlayerRightClick(', 'dev.rice.client.scaffold.InjectMyauHost.place(mc.playerController, ')
            s=s.replace('                    this.grimYaw = targetYaw;',
                '                    float[] limited = dev.rice.client.scaffold.InjectMyauHost.aim(event, targetYaw, targetPitch); targetYaw = limited[0]; targetPitch = limited[1]; this.grimYaw = targetYaw;')
            s=s.replace('if (this.rotationSubmit.getValue() != SUBMIT_GRIM) {', 'if (true) {')
        result[rel]=s.encode('utf-8')
        manifest[rel]={'source_sha256':hashlib.sha256(data).hexdigest(),'ported_sha256':hashlib.sha256(result[rel]).hexdigest(),'namespace_relocation':True,'enum_values_adapter_count':count,'placement_pose_adapter':rel=='module/modules/Scaffold.java','rice_behavior_overrides':['place','onUpdate'] if rel=='module/modules/Scaffold.java' else [],'methods':SPEC['partial'].get(rel,'whole class')}
    return result,manifest

def main():
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');p.add_argument('--raw-output',type=Path);a=p.parse_args()
    if a.raw_output:
        for source in OUT.rglob('*.java'):
            dest=a.raw_output/source.relative_to(OUT);dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(source.read_bytes().replace(PREFIX.encode(),b'myau'))
        for rel in SPEC['copies']:
            dest=a.raw_output/rel;dest.write_bytes((FIXTURE/rel).read_bytes())
        return
    files,manifest=generate()
    for rel,data in files.items():
        dest=OUT/rel
        if a.check:
            if not dest.exists() or dest.read_bytes()!=data:raise RuntimeError('InjectMyau source drift: '+rel)
        else:dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(data)
    target=ROOT/'tools/injectmyau-manifest.json'
    if a.check:
        if json.loads(target.read_text())!=manifest:raise RuntimeError('InjectMyau manifest drift')
    else:target.write_text(json.dumps(manifest,indent=2)+'\n')
    print('PASS: InjectMyau Scaffold/rotation source and selected helper methods are reproducible from supplied originals with declared Rice bounded-rotation/ray-checked-placement overrides and namespace/EnumFacing adapters ('+str(len(files))+' files)')
if __name__=='__main__':main()
