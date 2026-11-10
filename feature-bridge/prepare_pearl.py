"""Adapt recovered rescue source without changing the quarantined reference files."""
from pathlib import Path
import re, hashlib, json

def body(text, signature, replacement):
    start=text.index(signature); left=text.index('{',start); depth=1; right=left+1
    while depth:
        depth += (text[right]=='{')-(text[right]=='}'); right+=1
    return text[:left]+'{\n'+replacement+'\n    }'+text[right:]

def prepare(root, adapted):
    ref=root.parent/'valen-selected-ref-20261105/extracted/valen-selected-src/src/main/java/valen'
    out=adapted/'pearl'; out.mkdir(exist_ok=True)
    sources=[]; hashes={}
    def read(name):
        p=ref/name; hashes[name]=hashlib.sha256(p.read_bytes()).hexdigest(); return p.read_text()
    def write(name,text):
        p=out/name; p.write_text(text,encoding='utf-8'); sources.append(p)

    text=read('module/impl/player/PearlRescueModule.java')
    text=text[text.index('package valen.'):]
    text=re.sub(r'^import valen\..*;\n','',text,flags=re.M)
    text=text.replace('package valen.module.impl.player;', '''package dev.rice.module.impl.pearl;
import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.MinecraftFields;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.module.impl.fireball.TimerController;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.Minecraft;''')
    text=re.sub(r'    @SubscribeEvent[^\n]*\n','',text)
    text=text.replace('    private final boolean fireballMode;', '    private static final Minecraft mc = Minecraft.getMinecraft();\n    private final boolean fireballMode;')
    text=text.replace('super(string, Module.category.player);','super(bl ? "fireball_pearl_clutch" : "auto_pearl", string, "Recovered Valen pearl rescue", ModuleCategory.UTILITY);')
    text=text.replace('SliderSetting','NumberSetting').replace('ButtonSetting','BooleanSetting')
    # Every reference setting gets an explicit stable key; group/unit metadata is framework-specific.
    text=text.replace('(GroupSetting)null, ', '').replace(', "In void for");', ');')
    def setting(m):
        kind,label,tail=m.groups(); key=label.lower().replace(' ','_')
        tail=re.sub(r'^, "[^\"]*"', '',tail) if kind=='NumberSetting' else tail
        return 'new '+kind+'("'+key+'", "'+label+'"'+tail+')'
    text=re.sub(r'new (NumberSetting|BooleanSetting)\("([^\"]+)"([^;]*?)\)',setting,text)
    # User's Valen screenshot overrides the recovered source's 0.1x pre-pearl default.
    text=text.replace('new NumberSetting("pre_pearl_timer", "Pre pearl timer", 0.1,', 'new NumberSetting("pre_pearl_timer", "Pre pearl timer", 0.02,')
    text=text.replace('this.registerSetting(', 'this.add(')
    text=re.sub(r'^\s*this\.[A-Za-z]+\.visible = [^;]+;\n','\n',text,flags=re.M)
    start=text.index('    @Override\n    public void guiUpdate()'); end=text.index('    @Override',start+15)
    text=text[:start]+'''    @Override public boolean settingVisible(String key) {
        if (key.equals("pre_pearl_timer") || key.equals("swap_back")) return !fireballMode;
        if (key.equals("in_void_for")) return requireAboveVoid.isEnabled() && !predictSafeLanding.isEnabled();
        if (key.equals("time")) return requireAboveVoid.isEnabled() && !predictSafeLanding.isEnabled() && inVoidFor.isEnabled();
        return true;
    }
    public boolean listens(int type) { return type==1 || type==2 || type==13; }
    public void onEvent(ClientContext context, SemanticEvent e) {
        if (typePaused()) { abortRescue(); return; }
        if (e.type==1) onRenderTick();
        else if(e.type==2) onReceivePacket(e);
        else if(e.type==13) onClientRotation(e);
    }
    private boolean typePaused() { return NativeFeatureBridge.guiOpen() || NativeFeatureBridge.timerBlocked(); }
    public void postMotion() { if(isEnabled() && !typePaused()) onPostMotion(); }
    public float[] motionRotation() {
        if(!isEnabled()||typePaused()||!RescueServices.ready()||activePlayer!=mc.thePlayer||activeWorld!=mc.theWorld||mc.currentScreen!=null||mc.thePlayer.isDead)return null;
        if(fireballMode&&(comboPhase==1||comboPhase==2))return conditionsMet()&&earlierRescuesYielded()?new float[]{aimYaw,aimPitch}:null;
        return !fireballMode&&!timering&&hasSolution&&conditionsMet()?new float[]{aimYaw,aimPitch}:null;
    }
    public void onWorldUnload(ClientContext context) { abortRescue(); resetSession(); }
    private void disable() { NativeFeatureBridge.disableFeature(this); }
''' +text[end:]
    text=text.replace('public void onEnable()', 'protected void onEnable(ClientContext context)').replace('public void onDisable()', 'protected void onDisable(ClientContext context)')
    text=text.replace('public void onRenderTick(TickEvent.RenderTickEvent renderTickEvent)', 'public void onRenderTick()')
    text=text.replace('''        if (renderTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        if (PearlRescueModule.mc.currentScreen == null) {
            this.onKeyBind();
        }
''','')
    text=text.replace('ReceivePacketEvent receivePacketEvent','SemanticEvent receivePacketEvent').replace('receivePacketEvent.getPacket()','receivePacketEvent.packet')
    text=text.replace('ClientRotationEvent clientRotationEvent','SemanticEvent clientRotationEvent')
    text=text.replace('PostMotionEvent postMotionEvent','')
    text=text.replace('RotationHelper.get().forceMovementFix = true;', 'clientRotationEvent.remapMovement = true; clientRotationEvent.overrideRotation = true;')
    text=re.sub(r'clientRotationEvent.setYaw\(Float.valueOf\(([^)]+)\)\);',r'clientRotationEvent.yaw = \1;',text)
    text=re.sub(r'clientRotationEvent.setPitch\(Float.valueOf\(([^)]+)\)\);',r'clientRotationEvent.pitch = \1;',text)
    text=text.replace('.isToggled()', '.isEnabled()').replace('.getInput()', '.doubleValue()')
    text=text.replace('Valen.timerController','RescueServices.timer()')
    text=text.replace('Utils.nullCheck()', 'RescueServices.ready()').replace('ModuleManager.isBedMining()', 'RescueServices.bedMining()')
    text=text.replace('ModuleManager.autoPearl != null && ModuleManager.autoPearl.isRescueActive()', 'RescueServices.pearlActive(false)')
    text=text.replace('ModuleManager.fireballPearlClutch != null && ModuleManager.fireballPearlClutch.isRescueActive()', 'RescueServices.pearlActive(true)')
    text=text.replace('ModuleManager.clutch != null && (ModuleManager.clutch.hasSafeLanding() || ModuleManager.clutch.canSave())','RescueServices.clutchCanSave()')
    text=text.replace('ModuleManager.clutch != null && ModuleManager.clutch.canSave()', 'RescueServices.clutchCanSave()')
    text=text.replace('ModuleManager.bridgeEgg == null || !ModuleManager.bridgeEgg.canSave()', '!RescueServices.bridgeEggCanSave()')
    text=text.replace('ModuleManager.bridgeEgg != null && ModuleManager.bridgeEgg.canSave()', 'RescueServices.bridgeEggCanSave()')
    text=text.replace('Utils.isFriended(entityPlayer) || Utils.isTeammate((Entity)entityPlayer)', 'RescueServices.friendly(entityPlayer)')
    text=text.replace('ModuleManager.antiBot != null && ModuleManager.antiBot.isEnabled() && AntiBot.isBot((Entity)entityPlayer)', 'RescueServices.bot(entityPlayer)')
    text=text.replace('RotationUtils.serverRotations','RescueServices.serverRotations')
    text=text.replace('''            if (ModuleManager.antiFireball != null) {
                ModuleManager.antiFireball.recordRescueLaunch(this.aimYaw, this.aimPitch);
            }''', '            RescueServices.recordLaunch(this.aimYaw, this.aimPitch);')
    text=body(text,'private boolean manualTimerActive()', '''        if(NativeFeatureBridge.timerBlocked()) return true;
        if(RescueServices.timer().isRequested(TimerController.Owner.TIMER)) return true;
        if(RescueServices.timer().hasRequests() || !RescueServices.ready()) return false;
        return Math.abs(MinecraftFields.timer(mc).timerSpeed - 1f) > .001f;''')
    text=body(text,'private void stopManualTimer()', '''        RescueServices.timer().release(TimerController.Owner.TIMER);''')
    text=text.replace('public String getInfo()', 'public String statusText()')
    # Query the fireball trigger without consulting Bridge Egg, which itself uses this
    # query to yield. Share the real trigger evaluation so duration/damage gates match.
    trigger_start=text.index('        this.solidCache.clear();', text.index('public void onClientRotation'))
    trigger_end=text.index('        if (this.pearlSlot == -1',trigger_start)
    trigger=text[trigger_start:trigger_end]
    text=text[:trigger_start]+'        this.refreshTriggerConditions();\n'+text[trigger_end:]
    text=text.replace('    private boolean conditionsMet() {','    private boolean conditionsMet() { return conditionsMet(true); }\n    private boolean conditionsMet(boolean checkBridgeEgg) {')
    text=text.replace('return !this.waitForBridgeEgg.isEnabled() || !RescueServices.bridgeEggCanSave();','return !checkBridgeEgg || !this.waitForBridgeEgg.isEnabled() || !RescueServices.bridgeEggCanSave();')
    text=text.rsplit('}',1)[0]+'''    private void refreshTriggerConditions() {
'''+trigger+'''    }
    public boolean shouldPrecedeBridgeEgg() {
        if(!fireballMode||!isEnabled()||!RescueServices.ready())return false;
        if(activeWorld!=mc.theWorld||activePlayer!=mc.thePlayer){resetSession();activeWorld=mc.theWorld;activePlayer=mc.thePlayer;}
        if(mc.thePlayer.onGround&&!isRescueActive())comboAttempted=false;
        // This is a sequence gate, not a prediction of whether Fireball Pearl can
        // trigger this frame. Missing items, delayed conditions and a reachable
        // normal-pearl target must not silently let Egg go first.
        return isRescueActive()||!comboAttempted;
    }
}
'''
    # A timeout means an unconfirmed attempt, not a confirmed teleport. In particular
    # an upward combo pearl can be airborne beyond the old 3.5 second cutoff.
    text=text.replace('boolean bl = this.comboPhase == 3;', 'boolean bl = this.comboPhase == 3 && (this.landed || PearlRescueModule.mc.thePlayer.onGround);')
    text=text.replace('''            this.stopTimering();
            if (this.disableAfterwards.isEnabled())''','''            boolean completed = this.landed || PearlRescueModule.mc.thePlayer.onGround;
            this.stopTimering();
            if (completed && this.disableAfterwards.isEnabled())''')
    text=text.replace('System.currentTimeMillis() + (long)this.simFlightTicks * 50L + this.pingMs() + 100L','System.currentTimeMillis() + Math.max(10000L, (long)this.simFlightTicks * 50L + this.pingMs() + 1000L)')
    text=text.replace('System.currentTimeMillis() + 3500L','System.currentTimeMillis() + 12000L + this.pingMs()')
    text=text.replace('this.comboPhase > 0 && (this.landed || PearlRescueModule.mc.thePlayer.onGround || System.currentTimeMillis() >= this.comboDeadline)', 'this.comboPhase > 0 && (this.landed || this.comboPhase < 3 && PearlRescueModule.mc.thePlayer.onGround || System.currentTimeMillis() >= this.comboDeadline)')
    text=text.replace('this.comboPhase == 3 && (this.landed || PearlRescueModule.mc.thePlayer.onGround)', 'this.comboPhase == 3 && this.landed')
    text=text.replace('this.timering && (this.landed || PearlRescueModule.mc.thePlayer.onGround || System.currentTimeMillis() >= this.timerDeadlineMs)', 'this.timering && (this.landed || System.currentTimeMillis() >= this.timerDeadlineMs)')
    text=text.replace('boolean completed = this.landed || PearlRescueModule.mc.thePlayer.onGround;', 'boolean completed = this.landed;')
    # Validate the selected landing tile itself, with room for vanilla launch spread;
    # the recovered check accepted any of the eight neighboring tiles at this height.
    text=text.replace('return Math.abs(blockPos2.getX() - blockPos.getX()) <= 1 && Math.abs(blockPos2.getZ() - blockPos.getZ()) <= 1;', '''if(!blockPos2.equals(blockPos)||movingObjectPosition.hitVec==null)return false;
        double x=movingObjectPosition.hitVec.xCoord-blockPos.getX(),z=movingObjectPosition.hitVec.zCoord-blockPos.getZ();
        return x>=0.1&&x<=0.9&&z>=0.1&&z<=0.9;''')
    text=text.replace('f4 += 0.05f','f4 += 0.01f')
    write('PearlRescueModule.java',text)
    for name in ['PearlFireballSequence','RescueFall','SimulatedPlayer']:
        text=read('utility/'+name+'.java');text=text[text.index('package valen.'):]
        text=re.sub(r'^import valen\..*;\n','',text,flags=re.M).replace('package valen.utility;','package dev.rice.module.impl.pearl;')
        if name=='RescueFall':
            text=text.replace('            Object object2;','').replace('List list =','List<AxisAlignedBB> list =').replace('for (Object object2 : list)', 'for (AxisAlignedBB object2 : list)')
            text=text.replace('object2 = list.iterator();','java.util.Iterator<AxisAlignedBB> object2 = list.iterator();')
        if name=='SimulatedPlayer':
            for accessor,field in [('((IAccessorEntityLivingBase)entityPlayerSP).valen$lunar$getJumpTicks()','jumpTicks'),('((IAccessorEntity)entityPlayerSP).valen$lunar$getFire()','fire'),('((IAccessorEntity)entityPlayerSP).valen$lunar$getNextStepDistance()','nextStepDistance')]:
                text=text.replace(accessor, 'RescueServices.intField(entityPlayerSP, "'+field+'")')
            text=text.replace('((IAccessorEntity)entityPlayerSP).valen$lunar$getIsInWeb()', 'RescueServices.web(entityPlayerSP)')
            text=text.replace('ModuleManager.noSlow == null || !ModuleManager.noSlow.isEnabled() || NoSlow.slowed.getInput() == 80.0','RescueServices.itemSlowdown(this.player) == 0.2f')
            text=text.replace('NoSlow.getSlowed()', 'RescueServices.itemSlowdown(this.player)')
            text=text.replace('List list = this.worldObj.getCollidingBoundingBoxes', 'List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes').replace('List list2 = this.worldObj.getCollidingBoundingBoxes', 'List<AxisAlignedBB> list2 = this.worldObj.getCollidingBoundingBoxes').replace('for (Object object : list)', 'for (AxisAlignedBB object : list)')
            text=text.replace('Pair.of((Object)d3, (Object)d4)', 'Pair.of(d3, d4)')
        write(name+'.java',text)
    (out/'source-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
    return sources
