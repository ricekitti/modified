"""Static CFR port of the supplied Snowify detector; only host/event/config/render adapters differ."""
from pathlib import Path
import re,json,hashlib
ROOT=Path(__file__).resolve().parent
REF=ROOT.parent/'snowify-anticheat-port/reference/decompiled/com/snowify/module/utility/anticheat'
OTHER=Path('C:/Users/ethan/Documents/ricerakak slionky/Slinky-cross-platform-source/Slinky-source')

def replace_body(s,signature,body):
    start=s.index(signature);left=s.index('{',start);end=left+1;depth=1
    while depth:
        if s[end]=='{':depth+=1
        elif s[end]=='}':depth-=1
        end+=1
    return s[:left]+'{\n'+body+'\n    }'+s[end:]

def prepare(adapted):
    lock=json.loads((ROOT.parent/"snowify-anticheat-port/reference/source-lock.json").read_text())
    for name,digest in lock.items():
        if hashlib.sha256((REF/name).read_bytes()).hexdigest()!=digest:raise ValueError("Anticheat reference changed: "+name)
    members={}
    for line in (OTHER/'java/mappings/srg.txt').read_text().splitlines():
        row=line.split()
        if row and row[0] in ('MD','FD'):members[row[-1]]=row[2]
    output=[]
    for name in ('Anticheat','BreakCheck','CommandBlacklist'):
        s=(REF/(name+'.java')).read_text()
        s=re.sub(r'^import com\.snowify\..*;\s*$', '',s,flags=re.M)
        s=s.replace('package com.snowify.module.utility.anticheat;','package dev.rice.anticheat;')
        s=re.sub(r'\b(?:func_\w+|field_\w+)\b',lambda m:members.get(m[0],m[0]),s)
        s=s.replace('@SubscribeEvent','').replace('@Override','')
        s=s.replace('\"\\u26a0 \\u00a7lCHEATER DETECTED\"','\"RiceSlinky \\u00a7lCHEATER DETECTED\"')
        s=s.replace('(T[])new double[0][]','new double[0][]')
        s=s.replace('"snowify-manual:', '"riceslinky-manual:').replace('"snowify"','"riceslinky"')
        s=s.replace('Conditions.message','Host.message').replace('Teams.isTeammate','Host.isTeammate')
        if name=='Anticheat':
            s=s.replace('implements HudMovable','')
            # Constructor watcher becomes the explicit packet event entry point.
            start=s.index('        InboundPackets.watch(');end=s.index('\n    }',start)
            watcher=s[start:end];body=watcher[watcher.index('public void seen(Packet<?> packet) {')+len('public void seen(Packet<?> packet) {'):]
            body=body[:body.rfind('\n            }')].replace('Anticheat.this.','this.')
            s=s[:start]+s[end:]
            insert=s.index('    public Property enabledProp()')
            s=s[:insert]+'    public void onPacket(Packet<?> packet){'+body+'\n    }\n\n'+s[insert:]
            s=s.replace('onWorldJoin(EntityJoinWorldEvent event)','onWorldJoin(net.minecraft.entity.Entity entity)').replace('event.entity','entity')
            s=s.replace('onClientTick(TickEvent.ClientTickEvent event)','onClientTick()')
            s=s.replace('        if (event.phase != TickEvent.Phase.END) {\n            return;\n        }\n','')
            s=s.replace('onRender(RenderGameOverlayEvent.Post event)','onRender(int width,int height)')
            s=s.replace('event.type != RenderGameOverlayEvent.ElementType.ALL || ','').replace('mc.currentScreen instanceof HudEditor','Host.menuOpen()')
            s=s.replace('event.resolution.getScaledWidth(), event.resolution.getScaledHeight()','width,height')
            s=replace_body(s,'private static FontRenderer font()','        return Minecraft.getMinecraft().fontRendererObj;')
            s=replace_body(s,'private static void drawPopup(','        Host.drawPopup(fr,x,y,w,h,name,flags,alpha);')
            insertion='''    private int[] placeBox(int width,int height,int w,int h){return new int[]{Math.max(0,Math.min(width-w,(int)Math.round(hudX()*width-w/2.0))),Math.max(0,Math.min(height-h,(int)Math.round(hudY()*height)))};}
    public void clearWorld(){players.clear();announced.clear();breaks.clear();placeEvents.clear();popupName=null;}
'''
            s=s.replace('    public void samplePopup()',insertion+'\n    public void samplePopup()',1)
            # CFR loses parameterized ArrayList iteration and one JsonObject entry type.
            s=s.replace('new ArrayList(mc.theWorld.playerEntities)','new ArrayList<EntityPlayer>(mc.theWorld.playerEntities)')
        target=adapted/(name+'.java');target.write_text(s);output.append(target)
    return output
