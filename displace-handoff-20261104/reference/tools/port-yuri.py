"""Pinned Yuri v1.3.1 Scaffold port. --check verifies every generated production source."""
from pathlib import Path
import re,json,hashlib,argparse
ROOT=Path(__file__).resolve().parents[1]
FIX=ROOT/'tools/fixtures/yuri-v1.3.1'
OUT=ROOT/'java/src/main/java/dev/rice/client/scaffold/yuri'
HEADER='''package dev.rice.client.scaffold.yuri;
import dev.rice.client.scaffold.YuriScaffoldHost;
import dev.rice.client.scaffold.yuri.YuriEvents.*;
import net.minecraft.client.Minecraft;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.util.vector.Vector2f;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
'''
def body(s,start):
    end=s.index('{',start)+1;depth=1
    while depth:
        if s[end]=='{':depth+=1
        elif s[end]=='}':depth-=1
        end+=1
    return s[start:end]
def methods(s,names):
    return '\n'.join(body(s,m.start()) for m in re.finditer(r'    (?:public|private|protected) (?:static )?[^\n;={}]+? (\w+)\s*\([^;{}]*\)\s*\{',s) if m.group(1) in names)
def source(rel):return (FIX/rel).read_text(encoding='utf-8')
def rewrite(s):
    s=re.sub(r'\bmc\.thePlayer\.onGroundTicks\b','YuriScaffoldHost.groundTicks()',s)
    s=re.sub(r'\bmc\.thePlayer\.offGroundTicks\b','YuriScaffoldHost.airTicks()',s)
    s=s.replace('mc.thePlayer.movementYaw','YuriScaffoldHost.movementYaw()')
    s=s.replace('mc.thePlayer.getPreviousRotation()','RotationManager.lastRotations')
    s=s.replace('mc.thePlayer.getCustomPositionVector()','new Vector3d(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ)')
    s=s.replace('entity.getCustomPositionVector()','new Vector3d(entity.posX,entity.posY,entity.posZ)')
    s=s.replace('mc.entityRenderer.getMouseOver(1)','YuriScaffoldHost.refreshMouseOver()')
    s=re.sub(r'(mc\.gameSettings\.\w+)\.setPressed\(([^;]+)\);',r'YuriScaffoldHost.key(\1,\2);',s)
    s=re.sub(r'Keyboard\.isKeyDown\((mc\.gameSettings\.\w+)\.getKeyCode\(\)\)',r'YuriScaffoldHost.physical(\1)',s)
    s=s.replace('mc.playerController.onPlayerRightClick(', 'YuriScaffoldHost.place(mc.playerController, ')
    s=s.replace('mc.rightClickMouseSilentSwing()', 'YuriScaffoldHost.strictPlace()')
    s=s.replace('PacketUtils.sendPacket(', 'mc.thePlayer.sendQueue.addToSendQueue(')
    s=s.replace('this.offset.add(new Vec3i(', 'YuriScaffoldHost.add(this.offset, new Vec3i(')
    s=s.replace('mc.thePlayer.safeWalk = true','YuriScaffoldHost.setSafeWalk(true)').replace('mc.thePlayer.safeWalk = false','YuriScaffoldHost.setSafeWalk(false)')
    s=s.replace('mc.timer.timerSpeed = 1.0f','YuriScaffoldHost.resetTimer()')
    s=s.replace('mc.thePlayer.renderPitchHead = targetPitch','YuriScaffoldHost.setLarp(mc.thePlayer.rotationYaw + 125,targetPitch)')
    for cls,key in [('SpeedModule','speed'),('DerpModule','derp')]:
        s=s.replace('Yuri.INSTANCE.getModuleManager().getModule('+cls+'.class).isEnabled()', 'YuriScaffoldHost.moduleEnabled("'+key+'")')
    s=s.replace('Yuri.INSTANCE.getNotificationHandler().pop(', 'YuriScaffoldHost.notifyUser(')
    s=s.replace('ProgressBarManager.remove(barEntry)', 'YuriScaffoldHost.clearCounter()')
    # Stock 1.8.9 Vec3 has no Yuri floor/subtract helpers.
    s=s.replace('player.getPositionVector().floor().subtract(0, i, 0)', 'new Vec3(Math.floor(player.posX),Math.floor(player.posY)-i,Math.floor(player.posZ))')
    s=s.replace('mc.thePlayer.getPositionVector().subtract(0, 1, 0)','mc.thePlayer.getPositionVector().addVector(0,-1,0)')
    return s
def generate():
    result={}
    # Keep the complete bridge/rotation/sprint/tower/placement logic, adapting only host calls.
    s=source('modules/impl/player/ScaffoldModule.java');s=s[s.index('@ModuleInfo'):]
    s=re.sub(r'@ModuleInfo\([^\n]+\)\n','',s);s=re.sub(r'    @EventHook\n|    @Getter\n|    @Setter\n','',s)
    s=s.replace('class ScaffoldModule extends Module','class Scaffold extends YuriModule')
    s=s.replace('private ProgressBarEntry barEntry;','private Object barEntry;')
    old=body(s,s.index('    private void renderBlockCounter()'))
    s=s.replace(old,'    private void renderBlockCounter() { YuriScaffoldHost.renderCounter(blockCounter.getValue().ordinal(), ScaffoldUtils.countBlocks(), initialBlockCount); }')
    s=s.replace('mc.objectMouseOver.sideHit != enumFacing.getEnumFacing() || !mc.objectMouseOver.getBlockPos().equals(blockFace)', 'mc.objectMouseOver == null || mc.objectMouseOver.sideHit != enumFacing.getEnumFacing() || !blockFace.equals(mc.objectMouseOver.getBlockPos())')
    result['Scaffold.java']=HEADER+'import net.minecraft.client.gui.ScaledResolution;\nimport net.minecraft.entity.projectile.EntityFireball;\nimport net.minecraft.network.play.client.C0APacketAnimation;\nimport net.minecraft.network.play.server.S02PacketChat;\n'+rewrite(s)
    s=source('utils/player/ScaffoldUtils.java');s=s[s.index('public final class'):].replace('ScaffoldModule.','Scaffold.')
    old=body(s,s.index('    public static void renderBlockCounter('));s=s.replace(old,'')
    # Preserve player position even if the ray-cast itself throws.
    s=s.replace('            MovingObjectPosition mop = RayCastUtils.rayCast(\n                    new Vector2f((float) (mc.thePlayer.rotationYaw + (offset * 3)), 0), 4.5);\n            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + difference, mc.thePlayer.posZ);', '            MovingObjectPosition mop;\n            try { mop = RayCastUtils.rayCast(new Vector2f((float)(mc.thePlayer.rotationYaw + offset * 3),0),4.5); }\n            finally { mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + difference, mc.thePlayer.posZ); }')
    result['ScaffoldUtils.java']=HEADER+rewrite(s)
    partial={
      'PlayerUtils':['getPlacePossibility','getEnumFacing','checkAndAddFacing','checkBlock','isBlockUnder'],
      'RotationUtils':['move','applySensitivityPatch','resetRotation','smooth','calculate','getMovementYaw'],
      'MoveUtils':['direction','enoughMovementForSprinting','speed','isMoving','strafe','getBaseMoveSpeed','findGround','findGroundB','isMovingMotion','getSpeedPosBased'],
      'BlockUtils':['block','blockRelativeToPlayer','blockAheadOfPlayer'],
    }
    for cls,names in partial.items():
        s=source('utils/player/'+cls+'.java')
        if cls=='RotationUtils':
            # The adaptive entity-target overload is unrelated combat code.
            start=s.index('    public static Vector2f calculate(final Entity entity, final boolean adaptive');s=s.replace(body(s,start),'')
            s=s.replace('final double distributionYaw =','if(distance < 1.0E-8) return new Vector2f(0,0);\n            final double distributionYaw =')
        if cls=='PlayerUtils':s=s.replace('PlayerUtils.blockRelativeToPlayer','BlockUtils.blockRelativeToPlayer')
        extra=''
        if cls=='BlockUtils':extra=s[s.index('    public static final List<Block> blacklist'):s.index('    public static boolean inLiquid')]
        result[cls+'.java']=HEADER+'public final class '+cls+' {\nprivate static final Minecraft mc=Minecraft.getMinecraft();\n'+extra+rewrite(methods(s,names))+'\n}\n'
    for cls,rel in [('Representation','api/properties/impl/Representation.java'),('TimerUtils','utils/client/TimerUtils.java'),('ModeProperty','api/properties/impl/ModeProperty.java'),('NumberProperty','api/properties/impl/NumberProperty.java'),('ValueChangeListener','api/properties/ValueChangeListener.java'),('Property','api/properties/Property.java')]:
        s=source(rel);s=s[s.index('public '):].replace('Module module','YuriModule module')
        result[cls+'.java']=HEADER+'import java.util.function.Supplier;\n'+s
    s=source('utils/player/EnumFacingOffset.java');s=s[s.index('public class'):];s=s[:-2]+'public EnumFacing getEnumFacing(){return enumFacing;} public Vec3 getOffset(){return offset;}\n}\n';result['EnumFacingOffset.java']=HEADER+s
    s=source('managers/impl/SlotManager.java');s=s[s.index('public final class'):];s=s.replace('    @Getter\n','');s=s[:-2]+'public static int getOriginalSlot(){return originalSlot;} public static boolean isServerSwapActive(){return serverSwapActive;} public static void clear(){originalSlot=-1;originalStack=null;serverSwapActive=false;}\n}\n';result['SlotManager.java']=HEADER+s.replace('public final class SlotManager {','public final class SlotManager { private static final Minecraft mc=Minecraft.getMinecraft();')
    s=source('utils/client/MathUtils.java');result['MathUtils.java']=HEADER+'public final class MathUtils { public static final double TO_DEGREES=180/Math.PI;\n'+methods(s,['getRandom','wrappedDifference'])+'\n}'
    s=source('Vector3d.java');s=s[s.index('public class'):];result['Vector3d.java']=HEADER+s
    return result
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');a=parser.parse_args();manifest={}
    for name,s in generate().items():
        p=OUT/name;data=s.encode();manifest[name]=hashlib.sha256(data).hexdigest()
        if a.check:
            if not p.exists() or p.read_bytes()!=data:raise RuntimeError('Yuri port drift: '+name)
        else:p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
    record={'source':'https://github.com/unleg1t/Yuri/tree/v1.3.1','revision':'1cb86ad','generated':manifest,'reference':{p.relative_to(FIX).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in FIX.rglob('*') if p.is_file()},'adaptations':'Namespace flattening; Lombok getters explicit; stock mapped Minecraft equivalents for custom fields/methods; Rice events/properties/HUD/platform hosts; null mouse-over guard; zero-angle normalization; exception-safe temporary ray position. RotationManager/RayCastUtils are host adaptations of Yuri runtime logic.'}
    path=ROOT/'tools/yuri-manifest.json'
    if a.check:
        if json.loads(path.read_text())!=record:raise RuntimeError('Yuri manifest drift')
    else:path.write_text(json.dumps(record,indent=2)+'\n')
    print('PASS: pinned Yuri v1.3.1 Scaffold/planner/helpers and declared host adapters ('+str(len(manifest))+' generated source files)')
if __name__=='__main__':main()
