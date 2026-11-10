"""Reproducible BlockIn import with explicit player-mined opening exclusions."""
from pathlib import Path
import json,hashlib,argparse
ROOT=Path(__file__).resolve().parents[1]
FIX=ROOT/'tools/fixtures/supplied-blockin'
OUT=ROOT/'java/src/main/java/dev/rice/client/blockin/SuppliedBlockIn.java'
def generate():
    source=(FIX/'BlockIn.java').read_bytes();s=source.decode('utf-8')
    reference=(FIX/'snowify/blockin/BlockIn.java').read_text(encoding='utf-8').replace('com.snowify','com.rice').replace('Snowify','Rice')
    if s != reference: raise RuntimeError('Core differs from pinned Snowify source beyond Rice namespace relocation')
    s=s.replace('package com.rice.blockin;','package dev.rice.client.blockin;')
    for name,package in [('Rice','com.rice'),('RotationManager','com.rice.antipearl.core'),('Configuration','com.rice.config'),('Property','com.rice.config'),('RenderGameOverlayEvent','com.rice.core.event'),('SubscribeEvent','com.rice.core.event'),('TickEvent','com.rice.core.event'),('WireRotation','com.rice.displace'),('ProgressBar','com.rice.gui'),('Claims','com.rice.util'),('GameAccess','com.rice.util'),('PlaceWatch','com.rice.util')]:
        s=s.replace('import '+package+'.'+name+';','import dev.rice.client.blockin.SuppliedBlockInHost.'+name+';')
    import re
    s=re.sub(r'\bBlockIn\b','SuppliedBlockIn',s)
    # Keep user-facing strings from the source. Only the class/type name is relocated.
    s=s.replace('"SuppliedBlockIn"','"BlockIn"')
    # v212: leave cells the player mined open. Keep each adaptation counted so
    # unrelated upstream planner changes still fail the reproducible import check.
    exclusions = [
        ('        for (BlockPos cell : cells) {',
         '        for (BlockPos cell : cells) {\n            if (GameAccess.keepMiningOpening(cell)) continue;'),
        ('if (!SuppliedBlockIn.open(mc, spot) || box.intersectsWith',
         'if (GameAccess.keepMiningOpening(spot) || !SuppliedBlockIn.open(mc, spot) || box.intersectsWith'),
        ('if (SuppliedBlockIn.open(mc, top)) {',
         'if (SuppliedBlockIn.open(mc, top) && !GameAccess.keepMiningOpening(top)) {'),
        ('if (ownColumn || !SuppliedBlockIn.open(mc, next) || box.intersectsWith',
         'if (ownColumn || GameAccess.keepMiningOpening(next) || !SuppliedBlockIn.open(mc, next) || box.intersectsWith'),
        ('    private float[] aim(Minecraft mc, BlockPos spot) {',
         '    private float[] aim(Minecraft mc, BlockPos spot) {\n        if (GameAccess.keepMiningOpening(spot)) return null;')
    ]
    for original, replacement in exclusions:
        if s.count(original) != 1: raise RuntimeError('Mining-opening adaptation anchor drift: ' + original)
        s = s.replace(original, replacement)
    return s.encode('utf-8'),{'repository':'https://github.com/ricekitti/snowify-open-src','commit':'1f7aca61589dac9bb1bd44f6ce8a436aaafd4f56','source_sha256':hashlib.sha256(source).hexdigest(),'ported_sha256':hashlib.sha256(s.encode()).hexdigest(),'adaptations':'Package/class/import relocation plus five counted v212 player-mined opening exclusions in progress, wall planning, roof planning, support search and final aim selection. Remaining upstream method bodies retained. Mining history and shared services are explicit Rice hosts.'}
def generate_wire():
    s=(FIX/'snowify/displace/WireRotation.java').read_text(encoding='utf-8')
    s=s.replace('package com.snowify.displace;','package dev.rice.client.blockin;').replace('import com.snowify.antipearl.core.PacketRotation;','import dev.rice.client.blockin.SuppliedBlockInHost.PacketRotation;').replace('import com.snowify.util.GameAccess;','import dev.rice.client.blockin.SuppliedBlockInHost.GameAccess;').replace('WireRotation','SnowifyWireRotation').replace('"snowify_wire_rotation"','"rice_blockin_wire_rotation"')
    s=s.replace('    private static boolean pendingForce;', '    private static volatile long session;\n    private static Channel attached;\n    private static boolean pendingForce;')
    s=s.replace('        channel.eventLoop().execute(new Runnable(){','        final long expectedSession = session;\n        channel.eventLoop().execute(new Runnable(){').replace('            public void run() {\n                forceYaw', '            public void run() {\n                if(expectedSession != session)return;\n                forceYaw')
    s=s.replace('            ChannelPipeline pipeline = channel.pipeline();','            attached = channel;\n            ChannelPipeline pipeline = channel.pipeline();')
    pos=s.index('    static {')
    cleanup="""    /** Discard pending force/restore state and remove only this module's wire handler. */
    public static void detach() {
        ++session;
        if(attached != null && attached.pipeline().context(HANDLER) != null)attached.pipeline().remove(HANDLER);
        attached=null;pendingForce=false;pendingRestore=false;placementPending=false;
        placementLookKnown=false;pendingKnown=false;pendingBlock=null;wireKnown=false;wirePosKnown=false;
        wireSneaking=false;wireSprinting=false;lastForcedYaw=null;
    }

"""
    return (s[:pos]+cleanup+s[pos:]).encode('utf-8')

def main():
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');a=p.parse_args();data,manifest=generate();target=ROOT/'tools/supplied-blockin-manifest.json'
    wire=generate_wire();wire_out=OUT.with_name('SnowifyWireRotation.java')
    manifest['wire_source_sha256']=hashlib.sha256((FIX/'snowify/displace/WireRotation.java').read_bytes()).hexdigest()
    manifest['wire_ported_sha256']=hashlib.sha256(wire).hexdigest()
    manifest['wire_adaptations']='Namespace and mapped host access; module-specific handler name; detach/session cleanup. Original force/restore, promotion and diagnostics retained.'
    if a.check:
        if wire_out.read_bytes()!=wire:raise RuntimeError('Snowify wire backend source drift')
        if OUT.read_bytes()!=data or json.loads(target.read_text())!=manifest:raise RuntimeError('Supplied BlockIn source drift')
    else:OUT.parent.mkdir(parents=True,exist_ok=True);OUT.write_bytes(data);wire_out.write_bytes(wire);target.write_text(json.dumps(manifest,indent=2)+'\n')
    print('PASS: supplied BlockIn source with five counted player-mined opening exclusions; remaining upstream method bodies retained')
if __name__=='__main__':main()
