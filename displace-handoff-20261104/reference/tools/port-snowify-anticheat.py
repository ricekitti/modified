"""Pinned Snowify detectors; explicit namespace/compiler/host/lifecycle adaptations."""
from pathlib import Path
import hashlib,json,argparse
ROOT=Path(__file__).resolve().parents[1]
FIX=ROOT/'tools/fixtures/snowify-anticheat'
OUT=ROOT/'java/src/main/java/dev/rice/client/anticheat'
HOST='dev.rice.client.anticheat.SnowifyAntiCheatHost.'
def generate():
    outputs={};details={}
    for path in ['anticheat/Anticheat.java','anticheat/BreakCheck.java','util/Teams.java']:
        raw=(FIX/path).read_bytes();s=raw.decode('utf-8')
        s=s.replace('package com.snowify.anticheat;','package dev.rice.client.anticheat;').replace('package com.snowify.util;','package dev.rice.client.anticheat;')
        names={'com.snowify':['Snowify'],'com.snowify.config':['Configuration','Property'],'com.snowify.core.event':['EntityJoinWorldEvent','RenderGameOverlayEvent','SubscribeEvent','TickEvent'],'com.snowify.gui':['Draw','HudEditor','HudMovable','Theme'],'com.snowify.util':['Conditions','Gradient','InboundPackets'],'com.snowify.util.font':['SmoothFont']}
        for package,group in names.items():
            for name in group:s=s.replace('import '+package+'.'+name+';','import '+HOST+name+';')
        s=s.replace('import com.snowify.anticheat.BreakCheck;','import dev.rice.client.anticheat.BreakCheck;').replace('import com.snowify.util.Teams;','import dev.rice.client.anticheat.Teams;')
        s=s.replace('(T[])new double[0][]','new double[0][]').replace('new ArrayList(mc.theWorld.playerEntities)','new ArrayList<EntityPlayer>(mc.theWorld.playerEntities)')
        if path.endswith('Anticheat.java'):
            s=s.replace('return new File(new File(Minecraft.getMinecraft().mcDataDir, "snowify"), "anticheat_blacklist.json");','return SnowifyAntiCheatHost.blacklistFile();')
            s=s.replace('mc.addScheduledTask(new Runnable(){','SnowifyAntiCheatHost.schedule(mc, new Runnable(){')
            s=s.replace('    public Property enabledProp() {', '''    /** Rice lifecycle: discard transient histories, retaining the persistent blacklist. */
    public void resetSession() {
        this.players.clear();this.announced.clear();this.lag.clear();this.lag.suppressUntil=0;
        this.breaks.clear();this.placeEvents.clear();this.popupName=null;this.popupFlags=null;this.popupAt=0;this.lastPing=0;
    }

    public Property enabledProp() {''')
        if path.endswith('Teams.java'):
            s=s.replace('return Teams.sameTeam(Teams.ownTeam(mc), player.getTeam());','return SnowifyAntiCheatHost.isSlinkyTeammate(player) || Teams.sameTeam(Teams.ownTeam(mc), player.getTeam());')
        name=Path(path).name;outputs[name]=s.encode('utf-8');details[path]={'upstream_sha256':hashlib.sha256(raw).hexdigest(),'ported_sha256':hashlib.sha256(outputs[name]).hexdigest()}
    return outputs,{'repository':'https://github.com/ricekitti/snowify-open-src','commit':'1f7aca61589dac9bb1bd44f6ce8a436aaafd4f56','files':details,'adaptations':['Namespace/import relocation to Rice services','CFR generic-array/raw-iteration compiler repairs','Rice-specific blacklist directory; Snowify file untouched','Discard stale scheduled flags across sessions','Explicit transient reset for disable/unload; blacklist retained','Honor Slinky Teams relationship bit before Snowify scoreboard fallback'],'limitations':'Original detector heuristics; GUI font/theme/shared events adapted to Rice. No live-game equivalence claim.'}
def main():
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');a=p.parse_args();outputs,manifest=generate();target=ROOT/'tools/snowify-anticheat-manifest.json'
    if a.check:
        for name,data in outputs.items():
            if (OUT/name).read_bytes()!=data:raise RuntimeError('Snowify source drift: '+name)
        if json.loads(target.read_text())!=manifest:raise RuntimeError('Snowify manifest drift')
    else:
        OUT.mkdir(parents=True,exist_ok=True)
        for name,data in outputs.items():(OUT/name).write_bytes(data)
        target.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    print('PASS: Snowify Anticheat, BreakCheck and Teams match pinned source plus explicit host/compiler/lifecycle adaptations')
if __name__=='__main__':main()
