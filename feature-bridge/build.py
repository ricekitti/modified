"""Build supplied Java modules and isolated dependencies for the native adapter."""
from pathlib import Path
import hashlib, json, shutil, subprocess, zipfile, os, re
os.environ.pop('JAVA_TOOL_OPTIONS',None)
os.environ.pop('_JAVA_OPTIONS',None)

ROOT=Path(__file__).resolve().parent
OTHER=Path(r'C:/Users/ethan/Documents/ricerakak slionky/Slinky-cross-platform-source/Slinky-source')
JDK=Path(r'C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin')
SOURCE=ROOT.parent/'slinkyreadablesource/slinkyreadablesource/references/displace-bow-20261031/java/src/main/java'
OUT=ROOT/'build'
OUT.mkdir(exist_ok=True)
vendor=ROOT/'vendor';vendor.mkdir(exist_ok=True)
base=vendor/'rice-framework.jar'
if not base.exists():shutil.copy2(OTHER/'build/empty-hand-displace-crash-v214-r2/artifacts/payload-101.jar',base)
named=OTHER/'build/minecraft-1.8.9-named.jar'
libs=list((OTHER/'deps/libraries').glob('*.jar'))
classpath=';'.join(map(str,[base,named,*libs]))
classes=OUT/'classes'
# Build in a clean directory so removed Rise Scaffold classes cannot leak into the payload.
if classes.exists():shutil.rmtree(classes)
classes.mkdir(exist_ok=True)
adapted=OUT/'adapted';adapted.mkdir(exist_ok=True)

def replace_body(text,signature,body):
    start=text.index(signature);left=text.index('{',start);depth=1;right=left+1
    while depth:
        if text[right]=='{':depth+=1
        elif text[right]=='}':depth-=1
        right+=1
    return text[:left]+'{\n'+body+'\n   }'+text[right:]

rotation=(OTHER/'java/src/main/java/dev/rice/client/rotation/RotationService.java').read_text()
bridge='dev.rice.module.NativeFeatureBridge.rotation'
for sig,body in [
 ('boolean claim(int requested)',f'return {bridge}(0,requested,0)!=0;'),
 ('float pendingYaw()',f'return (float){bridge}(4,0,0);'),
 ('float pendingPitch()',f'return (float){bridge}(5,0,0);'),
 ('void add(float yaw, float pitch)',f'{bridge}(2,yaw,pitch);'),
 ('void applyPending(ClientContext context, boolean alsoArm)',f'{bridge}(3,alsoArm?1:0,0);')]:
    rotation=replace_body(rotation,sig,body)
# Keep the framework's camera bookkeeping, and retain through the existing
# native controller instead of installing a second rotation/camera hook set.
rotation=rotation.replace('      remap = false;\n   }','      remap = false;\n      '+bridge+'(1,yaw,pitch);\n   }',1)
(adapted/'RotationService.java').write_text(rotation)
relations=(OTHER/'java/src/main/java/dev/rice/client/RelationshipService.java').read_text()
relations=replace_body(relations,'int flags(Entity entity)','return dev.rice.module.NativeFeatureBridge.flags(entity);')
(adapted/'RelationshipService.java').write_text(relations)
timer=(OTHER/'java/src/main/java/dev/rice/module/impl/TimerRangeModule.java').read_text()
timer=replace_body(timer,'boolean frozenOrAdvancing()','return dev.rice.module.NativeFeatureBridge.timerBlocked();')
(adapted/'TimerRangeModule.java').write_text(timer)
# Compile the supplied Displace sources without behavioral modifications.
HANDOFF=ROOT.parent/'displace-handoff-20261104'
HANDOFF_JAVA=HANDOFF/'reference/java/src/main/java'
sources=[HANDOFF/"displace/KnockbackDisplaceModule.java", HANDOFF/"displace/RavenBsVoidScanner.java",
         HANDOFF_JAVA/'myau/RotationState.java',SOURCE/'dev/rice/module/impl/BowAimbotModule.java',SOURCE/'dev/rice/module/impl/WeaponsModule.java',
         *(SOURCE/'dev/rice/module/impl/bowaimbot').glob('*.java'),
         ROOT/'java/NativeFeatureBridge.java',adapted/'RotationService.java',adapted/'RelationshipService.java',adapted/'TimerRangeModule.java']
sources.append(ROOT.parent/'slinkyreadablesource/slinkyreadablesource/references/fog-20261101/FogModule.java')
from prepare_supplied import prepare as prepare_supplied
sources.extend(prepare_supplied(classes))
sources.extend((ROOT/'java/supplied').rglob('*.java'))
sources.append(ROOT/'java/ScaffoldModule.java')
sources.extend((ROOT/'java/bedplates').glob('*.java'))
from prepare_rice_anticheat import prepare as prepare_rice_anticheat
sources.extend(prepare_rice_anticheat(adapted))
sources.extend((ROOT/'java/riceanticheat').glob('*.java'))
sources.append(ROOT/'java/FireballBoostFeatureModule.java')
sources.extend((ROOT/'java/fireball').glob('*.java'))
from prepare_pearl import prepare as prepare_pearl
sources.extend(prepare_pearl(ROOT,adapted))
from prepare_bridge_egg import prepare as prepare_bridge_egg
sources.extend(prepare_bridge_egg(ROOT,adapted))
from prepare_quickbuy import prepare as prepare_quickbuy
sources.extend(prepare_quickbuy(ROOT,adapted))
sources.extend((ROOT/'java/quickbuy').glob('*.java'))
sources.extend((ROOT/'java/pearl').glob('*.java'))
sources.append(ROOT/'java/AutoPearlFeatureModule.java')
sources.append(ROOT/'java/FireballPearlClutchFeatureModule.java')
shaderReference=ROOT.parent/'slinkyreadablesource/slinkyreadablesource/references/shaders-20261101/ShaderModule.java'
shader=shaderReference.read_text().replace('"scan_pixel"','"scan_pincushion"').replace('"Scan Pixel"','"Scan Pincushion"')
(adapted/'ShaderModule.java').write_text(shader)
sources.append(adapted/'ShaderModule.java')
args=['--release','8','-g','-proc:none','-encoding','UTF-8','-cp',str(classes)+';'+classpath,'-d',str(classes),*map(str,sources)]
argfile=OUT/'javac.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JDK/'javac.exe'),'@'+str(argfile)],check=True)
subprocess.run([str(JDK/'javac.exe'),'--release','8','-g:none','-d',str(classes),str(ROOT/'java/RiceFeatureLoader2.java')],check=True)
# Match the native hook declarations exactly, including the new post-setupFog wrapper.
table=(ROOT.parent/'slinkyreadablesource/slinkyreadablesource/src/engine/jni/hook_tables.inc').read_text().split('kNatives[',1)[1].split('};',1)[0]
hooks=re.findall(r'\{"([^"]+)",\s*"([^"]+)"',table)
def java_type(descriptor,position):
    kind=descriptor[position]
    if kind=='L':return 'Object',descriptor.index(';',position)+1
    return {'V':'void','Z':'boolean','I':'int','J':'long','F':'float','D':'double'}[kind],position+1
declarations=[]
for name,descriptor in hooks:
    args=[];position=1
    while descriptor[position]!=')':
        kind,position=java_type(descriptor,position);args.append(kind+' p'+str(len(args)))
    result,_=java_type(descriptor,position+1)
    declarations.append('public static native '+result+' '+name+'('+','.join(args)+');')
hookSource=adapted/'SlinkyHooks18.java';hookSource.write_text('public final class SlinkyHooks18 {\n'+'\n'.join(declarations)+'\n}')
subprocess.run([str(JDK/'javac.exe'),'--release','8','-g:none','-d',str(classes),str(hookSource)],check=True)
payload=OUT/'features-named.jar'
with zipfile.ZipFile(base) as original,zipfile.ZipFile(payload,'w',zipfile.ZIP_DEFLATED) as target:
    overrides={p.relative_to(classes).as_posix():p for p in classes.rglob('*.class') if p.relative_to(classes).as_posix() not in {'rice/features/RiceFeatureLoader2.class','SlinkyHooks13.class','SlinkyHooks14.class','SlinkyHooks15.class','SlinkyHooks16.class','SlinkyHooks18.class'}}
    for row in original.infolist():
        if not re.match(r'dev/rice/(?:module/impl/KnockbackDisplaceModule|client/combat/RavenBsVoidScanner|client/combat/LegitVoidPolicy)(?:\$[^/]*)?\.class$',row.filename) and row.filename not in overrides and not re.search(r'(?:DisplaceBackend|KillAuraDisplaceModule|LegitVoidPolicy)(?:\$[^/]*)?\.class$',row.filename) and not row.filename.startswith('META-INF/') and not re.match(r'dev/rice/module/impl/Scaffold[^/]*\.class$',row.filename):
            data=original.read(row)
            if row.filename in {'assets/ricesemantic/MetadataCatalog.json','assets/ricesemantic/InitialSettings.json'}:
                current=json.loads(data)
                supplied=json.loads((HANDOFF/'reference/java/src/main/resources'/row.filename).read_text())
                if row.filename.endswith('MetadataCatalog.json'):
                    replacement=next(m for m in supplied['modules'] if m['module']=='Knockback Displace')
                    current['modules']=[replacement if m['module']=='Knockback Displace' else m for m in current['modules']]
                else:current['modules']['Knockback Displace']=supplied['modules']['Knockback Displace']
                data=json.dumps(current).encode()
            target.writestr(row,data)
    for name,path in overrides.items():target.writestr(name,path.read_bytes())
    target.writestr("assets/riceslinky/licenses/snowify-anticheat-MIT.txt",(ROOT.parent/"snowify-anticheat-port/reference/LICENSE.txt").read_bytes())
asm=list((OTHER/'deps/tooling').glob('asm*.jar'))
tools=OUT/'tools';tools.mkdir(exist_ok=True)
subprocess.run([str(JDK/'javac.exe'),'--release','8','-cp',';'.join(map(str,asm)),'-d',str(tools),str(OTHER/'tools/RemapJar.java'),str(ROOT/'tests/FogHookCheck.java'),str(ROOT/'tests/MayuHookCheck.java'),str(ROOT/'tests/ScaffoldMotionHookCheck.java'),str(ROOT/'tests/ScaffoldSlotHookCheck.java'),str(ROOT/'tests/ScaffoldInventoryHookCheck.java'),str(ROOT/'tests/ScaffoldMoveHookCheck.java'),str(ROOT/'tests/ScaffoldJumpHookCheck.java')],check=True)
subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'FogHookCheck',str(named),'net/minecraft/client/renderer/EntityRenderer','renderWorldPass','setupFog',str(classes/'SlinkyHooks18.class')],check=True)
subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'ScaffoldMoveHookCheck',str(named),'net/minecraft/entity/EntityLivingBase','moveEntityWithHeading','moveEntity',str(classes/'SlinkyHooks18.class')],check=True)
subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'ScaffoldJumpHookCheck',str(named),'net/minecraft/entity/EntityLivingBase','onLivingUpdate','jump',str(classes/'SlinkyHooks18.class')],check=True)
subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'MayuHookCheck',str(named),'-',str(classes/'SlinkyHooks18.class')],check=True)
for mapping in ['srg','official']:
    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'RemapJar',str(OTHER/f'java/mappings/{mapping}.txt'),'forward',str(payload),str(OUT/f'features-{mapping}.jar'),str(named),*map(str,libs)],check=True)
    with zipfile.ZipFile(OUT/f'features-{mapping}.jar','a',zipfile.ZIP_DEFLATED) as jar:
        jar.writestr('dev/rice/inject/mappings.txt',(OTHER/f'java/mappings/{mapping}.txt').read_bytes())
# The launcher compresses the whole native DLL with LZMS. Leave these inner
# archives uncompressed so the outer dictionary can share code across classes
# and namespaces. Verify every entry survives the storage-only conversion.
for jar in [payload, OUT/'features-srg.jar', OUT/'features-official.jar']:
    stored = jar.with_suffix('.stored.jar')
    with zipfile.ZipFile(jar) as source, zipfile.ZipFile(stored, 'w', zipfile.ZIP_STORED) as target:
        for row in source.infolist():
            data = source.read(row.filename)
            row.compress_type = zipfile.ZIP_STORED
            target.writestr(row, data)
    with zipfile.ZipFile(jar) as source, zipfile.ZipFile(stored) as target:
        assert source.namelist() == target.namelist()
        for name in source.namelist():
            assert source.read(name) == target.read(name), 'Payload entry changed: ' + name
    stored.replace(jar)
blobs={'loader':classes/'rice/features/RiceFeatureLoader2.class','hooks':classes/'SlinkyHooks18.class','named':payload,'srg':OUT/'features-srg.jar','official':OUT/'features-official.jar'}
assembly=['.section .rdata,"dr"']
for name,path in blobs.items():
    data=path.read_bytes();assembly.extend([f'.globl feature_{name}',f'feature_{name}:',f'.incbin "{path.as_posix()}"','.balign 4',f'.globl feature_{name}_size',f'feature_{name}_size:',f'.long {len(data)}'])
(OUT/'payloads.s').write_text('\n'.join(assembly)+'\n')
manifest={'archive_sources':{str(p.relative_to(SOURCE)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources if p.is_relative_to(SOURCE)},
          'displace_backend':'Supplied 2026-11-04 manual KnockbackDisplaceModule and RavenBsVoidScanner (verbatim Java sources)',
          'displace_sources':{str(p.relative_to(HANDOFF)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources if p.is_relative_to(HANDOFF)},
          'fog_source_sha256':hashlib.sha256((ROOT.parent/'slinkyreadablesource/slinkyreadablesource/references/fog-20261101/FogModule.java').read_bytes()).hexdigest(),
          'shaders_source_sha256':hashlib.sha256((ROOT.parent/'slinkyreadablesource/slinkyreadablesource/references/shaders-20261101/ShaderModule.java').read_bytes()).hexdigest(),
          'shaders_asset_fix_sha256':hashlib.sha256((adapted/'ShaderModule.java').read_bytes()).hexdigest(),
          'framework_sha256':hashlib.sha256(base.read_bytes()).hexdigest(),
          'payloads':{k:hashlib.sha256(p.read_bytes()).hexdigest() for k,p in blobs.items()},
          'supplied_scaffold':json.loads((OUT/'supplied-source-manifest.json').read_text()),'registered':['Knockback Displace','Bow Aimbot','Fog','Shaders','Scaffold','Fireball Boost','Auto Pearl','Fireball Pearl Clutch','Bridge Egg Clutch'],'inactive_reference':'KillAura Displace (matches other client CoreModules)'}
(OUT/'manifest.json').write_text(json.dumps(manifest,indent=2))
print('Built original supplied module classes for named, SRG, and official mappings.')
fixture=OUT/'fixture';fixture.mkdir(exist_ok=True)


fixtureBase=adapted/'OpenB4KillAuraCheck.java'
fixtureBase.write_text((OTHER/'tools/OpenB4KillAuraCheck.java').read_text().replace('RavenBsCombatCheck.legacyHelpers(Paths.get(args[0]));','').replace('t.getDeclaredField(name)','t.getDeclaredField(FeatureFixture.mappedName(t,name))'))
subprocess.run([str(JDK/'javac.exe'),'--release','8','-cp',str(classes)+';'+classpath,'-d',str(fixture),str(fixtureBase),str(ROOT/'tests/FeatureFixture.java'),str(ROOT/'tests/PearlFixture.java'),str(ROOT/'tests/QuickBuyFixture.java'),str(ROOT/'tests/SuppliedScaffoldTest.java'),str(ROOT/'tests/SlinkyScaffoldBackendTest.java'),str(ROOT/'tests/BedplatesTest.java'),str(ROOT/'tests/RiceAnticheatTest.java'),str(ROOT/'tests/FogGlStubs.java'),str(OTHER/'tools/V115Input.java'),str(OTHER/'tools/V115InputStubs.java')],check=True)
subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[fixture,base])),'V115InputStubs',str(next(p for p in libs if p.name.startswith('lwjgl-'))),str(fixture)],check=True)
subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[fixture,base])),'FogGlStubs',str(next(p for p in libs if p.name.startswith('lwjgl-'))),str(fixture)],check=True)
lwjgl=next(p for p in libs if p.name.startswith('lwjgl-'));testLwjgl=OUT/'lwjgl-test.jar'
with zipfile.ZipFile(lwjgl) as original,zipfile.ZipFile(testLwjgl,'w',zipfile.ZIP_DEFLATED) as target:
    for row in original.infolist():
        if not row.filename.startswith('META-INF/'):target.writestr(row,original.read(row))
testlibs=[testLwjgl if p==lwjgl else p for p in libs]
(OUT/'test-classpath.txt').write_text(';'.join(map(str,[fixture,named,payload,*testlibs])))
fixtureJar=OUT/'fixture-named.jar'
with zipfile.ZipFile(fixtureJar,'w',zipfile.ZIP_DEFLATED) as jar:
    for p in fixture.rglob('*.class'):jar.write(p,p.relative_to(fixture).as_posix())
for mapping,namespace in [('srg',2),('official',0)]:
    lines=[line.split('\t') for line in (OTHER/f'java/mappings/{mapping}.txt').read_text().splitlines() if line]
    types={r[1]:r[2] for r in lines if r[0]=='CL'}
    fields={(r[1],r[2]):r[3] for r in lines if r[0]=='FD'}
    output=OUT/f'fixture-{mapping}.jar'
    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'RemapJar',str(OTHER/f'java/mappings/{mapping}.txt'),'forward',str(fixtureJar),str(output),str(named),*map(str,libs)],check=True)
    properties='\n'.join(types.get(owner,owner).replace('/','.')+'/'+name+'='+mapped for (owner,name),mapped in fields.items())
    with zipfile.ZipFile(output,'a',zipfile.ZIP_DEFLATED) as jar:jar.writestr('test-fields.properties',properties)
    mc=OUT/'minecraft-official-test.jar'
    if mapping=='official' and not mc.exists():
        # Default-package test helpers cannot share a package with signed game
        # classes. Preserve game class bytes in an unsigned test-only JAR.
        with zipfile.ZipFile(OTHER/'deps/minecraft-1.8.9-official.jar') as original,zipfile.ZipFile(mc,'w',zipfile.ZIP_DEFLATED) as target:
            for row in original.infolist():
                if not row.filename.startswith('META-INF/'):target.writestr(row,original.read(row))
    if mapping=='srg':
        mc=OUT/'minecraft-srg.jar'
        if not mc.exists():subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'RemapJar',str(OTHER/'java/mappings/srg.txt'),'forward',str(named),str(mc),str(named),*map(str,libs)],check=True)
    (OUT/f'test-classpath-{mapping}.txt').write_text(';'.join(map(str,[output,mc,OUT/f'features-{mapping}.jar',*testlibs])))
    renderer='net/minecraft/client/renderer/EntityRenderer'
    methodNames={(r[1],r[2],r[3]):r[4] for r in lines if r[0]=='MD'}
    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'FogHookCheck',str(mc),types[renderer],methodNames[(renderer,'renderWorldPass','(IFJ)V')],methodNames[(renderer,'setupFog','(IF)V')],str(classes/'SlinkyHooks18.class')],check=True)

    controller='net/minecraft/client/multiplayer/PlayerControllerMP';inventory='net/minecraft/entity/player/InventoryPlayer';item='net/minecraft/item/ItemBlock'
    placeDesc='(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/Vec3;)Z'
    canPlaceDesc='(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z'
    def mappedDesc(desc):
        return re.sub(r'L([^;]+);',lambda m:'L'+types.get(m.group(1),m.group(1))+';',desc)
    minecraft='net/minecraft/client/Minecraft';entity='net/minecraft/entity/Entity'
    living='net/minecraft/entity/EntityLivingBase'
    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'ScaffoldMoveHookCheck',str(mc),types[living],methodNames[(living,'moveEntityWithHeading','(FF)V')],methodNames[(entity,'moveEntity','(DDD)V')],str(classes/'SlinkyHooks18.class')],check=True)
    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'ScaffoldJumpHookCheck',str(mc),types[living],methodNames[(living,'onLivingUpdate','()V')],methodNames[(living,'jump','()V')],str(classes/'SlinkyHooks18.class')],check=True)
    player='net/minecraft/client/entity/EntityPlayerSP';world='net/minecraft/client/multiplayer/WorldClient'
    config=[str(namespace),fields[(minecraft,'theMinecraft')],'L'+types[minecraft]+';',fields[(minecraft,'thePlayer')],'L'+types[player]+';',fields[(minecraft,'theWorld')],'L'+types[world]+';',types[entity],fields[(entity,'rotationYaw')],fields[(entity,'rotationPitch')],fields[(entity,'posX')],fields[(entity,'posZ')],fields[(entity,'onGround')],fields[('net/minecraft/util/AxisAlignedBB','minY')],types['net/minecraft/util/AxisAlignedBB'],types[inventory],fields[(inventory,'currentItem')]]
    (OUT/f'test-mappings-{mapping}.txt').write_text('\n'.join(config))


# Generic native call-site contracts are retained; supplied Scaffold behavior is checked below.

    subprocess.run([str(JDK/'java.exe'),'-cp',';'.join(map(str,[tools,*asm])),'MayuHookCheck',str(mc),str(OTHER/f'java/mappings/{mapping}.txt'),str(classes/'SlinkyHooks18.class')],check=True)


subprocess.run([str(JDK/'java.exe'),'-Xverify:all','-cp',';'.join(map(str,[fixture,classes,named,base,*testlibs])),'SuppliedScaffoldTest'],check=True)
for mapping in ['srg','official']:
    subprocess.run([str(JDK/'java.exe'),'-Xverify:all','-cp',(OUT/f'test-classpath-{mapping}.txt').read_text(),'SuppliedScaffoldTest'],check=True)

subprocess.run([str(JDK/'java.exe'),'-Xverify:all','-cp',';'.join(map(str,[fixture,classes,named,base,*testlibs])),'SlinkyScaffoldBackendTest'],check=True)
for mapping in ['srg','official']:
    subprocess.run([str(JDK/'java.exe'),'-Xverify:all','-cp',(OUT/f'test-classpath-{mapping}.txt').read_text(),'SlinkyScaffoldBackendTest'],check=True)

subprocess.run([str(JDK/"java.exe"),"-Xverify:all","-cp",";".join(map(str,[fixture,classes,named,base,*testlibs])),"BedplatesTest"],check=True)
for mapping in ["srg","official"]:
    subprocess.run([str(JDK/"java.exe"),"-Xverify:all","-cp",(OUT/f"test-classpath-{mapping}.txt").read_text(),"BedplatesTest"],check=True)

subprocess.run([str(JDK/"java.exe"),"-Xverify:all","-cp",";".join(map(str,[fixture,classes,named,base,*testlibs])),"RiceAnticheatTest"],check=True)
for mapping in ["srg","official"]:
    subprocess.run([str(JDK/"java.exe"),"-Xverify:all","-cp",(OUT/f"test-classpath-{mapping}.txt").read_text(),"RiceAnticheatTest"],check=True)
