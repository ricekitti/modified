import dev.rice.client.KeyState;
import dev.rice.client.SemanticEvent;
import dev.rice.client.beddefense.BedDefenseApi;
import dev.rice.client.beddefense.BedDefenseBackend;
import dev.rice.client.render.BlockMesh;
import dev.rice.config.InitialSettings;
import dev.rice.module.Module;
import dev.rice.module.impl.BedDefenderModule;
import dev.rice.module.impl.BlockEspModule;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ColorSetting;
import dev.rice.setting.NamedColorListSetting;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;

public final class BedDefenderStabilityCheck {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static Object get(Object object, String name) throws Exception {
        Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);
    }
    static void put(Object object, String name, Object value) throws Exception {
        Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.set(object,value);
    }
    static final class RenamedLookPacket extends C03PacketPlayer.C05PacketPlayerLook {
        RenamedLookPacket() { super(127F,22F,true); }
    }
    static void bedLifecycle() throws Exception {
        BedDefenderModule module=(BedDefenderModule)V115Check.manager.find("bed_defender");
        if (module == null) { System.out.println("SKIP: Bed Defender removed from the client (v159); lifecycle stability test not applicable"); return; }
        BedDefenseBackend backend=(BedDefenseBackend)get(module,"backend");
        ((BooleanSetting)module.findSetting("bedwars_only")).set(false);
        V115Check.self.inventory.mainInventory[0]=new ItemStack(Blocks.end_stone,64);
        V115Check.self.inventory.currentItem=0;
        V115Check.mc.thePlayer=null;V115Check.mc.theWorld=null;
        V115Check.manager.requestEnabled(module,true);V115Check.manager.applyPendingChanges();
        check(module.isEnabled()&&!(Boolean)get(module,"initialized"),"enabling without world must wait without crashing");
        module.onEvent(V115Check.c,new SemanticEvent(17));module.onEvent(V115Check.c,new SemanticEvent(6));
        V115Check.mc.thePlayer=V115Check.self;V115Check.mc.theWorld=V115Check.world;
        V115Check.world.blocks.put(new BlockPos(0,64,0),Blocks.bed.getDefaultState());
        module.onEvent(V115Check.c,new SemanticEvent(17));
        check((Boolean)get(module,"initialized"),"module did not initialize after world joined");
        backend.onPacketSent(BedDefenseApi.wrapOutbound(new RenamedLookPacket()));
        check((Float)get(backend,"serverYaw")==127F&&(Float)get(backend,"serverPitch")==22F,"rotation packet tracking depends on mapped class name");
        backend.onPacketSent(BedDefenseApi.wrapOutbound(new C03PacketPlayer(true)));
        check((Float)get(backend,"serverYaw")==127F,"position-only packet overwrote server rotation");
        put(backend,"pendingPlace",true);put(backend,"hitPos",new BedDefenseApi.Vec3(0,64,0));
        put(backend,"placePos",new BedDefenseApi.Vec3(.5,65,.5));put(backend,"face","UP");
        put(backend,"renderTarget",new BedDefenseApi.Vec3(0,65,0));
        put(backend,"sneaked",true);V115Input.keys[42]=true;KeyState.set(V115Check.mc.gameSettings.keyBindSneak,true);
        module.onWorldUnload(V115Check.c);
        check(!(Boolean)get(backend,"pendingPlace")&&get(backend,"renderTarget")==null&&get(backend,"origin")==null&&get(backend,"hitPos")==null,"world unload retained stale placement/preview");
        check(V115Check.mc.gameSettings.keyBindSneak.isKeyDown(),"cleanup released physically held sneak");
        put(backend,"sneaked",true);V115Input.keys[42]=false;module.onWorldUnload(V115Check.c);
        check(!V115Check.mc.gameSettings.keyBindSneak.isKeyDown(),"cleanup kept synthetic sneak pressed");
        int placed=V115Check.controller.placements.size();
        V115Check.mc.thePlayer=null;V115Check.mc.theWorld=null;
        backend.onEnable();check(backend.getRotations(0,0)==null,"backend did not tolerate missing player");
        module.onEvent(V115Check.c,new SemanticEvent(17));
        check(V115Check.controller.placements.size()==placed,"stale placement ran after unload");
        V115Check.mc.thePlayer=V115Check.self;V115Check.mc.theWorld=V115Check.world;
        module.onEvent(V115Check.c,new SemanticEvent(17));
        put(backend,"pendingPlace",true);V115Check.mc.inGameHasFocus=false;
        module.onEvent(V115Check.c,new SemanticEvent(17));
        check(!(Boolean)get(backend,"pendingPlace")&&!(Boolean)get(module,"initialized")&&!module.cancelMouse(0),"focus loss left queued actions or mouse capture");
        V115Check.mc.inGameHasFocus=true;
        V115Check.self.inventory.mainInventory[0]=null;
        check(!BedDefenseApi.client.placeBlock(new BedDefenseApi.Vec3(0,64,0),"UP",new BedDefenseApi.Vec3(.5,65,.5)),"empty-hand placement reached controller");
        V115Check.manager.requestEnabled(module,false);V115Check.manager.applyPendingChanges();
        check(!(Boolean)get(module,"initialized")&&!module.cancelMouse(1),"disabled module captures clicks");
        System.out.println("PASS: Bed Defender absent world/player, reconnect initialization, stale-placement/preview cleanup, physical sneak preservation, focus loss, empty hotbar, mapped packet rotation and disabled mouse routing");
    }
    static void concurrentEsp() throws Exception {
        BlockEspModule module=(BlockEspModule)V115Check.manager.find("block_esp");
        ((NamedColorListSetting)module.findSetting("allowed_blocks")).set(Collections.singletonList(new NamedColorListSetting.Entry("stone",new ColorSetting.Value(.2F,.4F,.6F))));
        V115Check.mc.theWorld=null;
        V115Check.manager.requestEnabled(module,true);V115Check.manager.applyPendingChanges();
        V115Check.mc.theWorld=V115Check.world;
        CountDownLatch start=new CountDownLatch(1);AtomicReference<Throwable> failure=new AtomicReference<>();
        Thread updates=new Thread(() -> {
            try { start.await();for(int n=0;n<1500;n++) {
                int x=(n%8)*16;module.regionChanged((net.minecraft.world.World)null,2,x,65,0,1);
                if((n&1)==0)module.regionChanged((net.minecraft.world.World)null,0,(n+3)%8,0,0,0);
                if((n&63)==0)module.onWorldUnload(V115Check.c);
            }} catch(Throwable problem) { failure.compareAndSet(null,problem); }
        },"block-esp-chunk-updates");
        Thread rendering=new Thread(() -> {
            try { start.await();SemanticEvent render=new SemanticEvent(6);for(int n=0;n<1500;n++) {
                module.plan(render);for(BlockMesh.Region ignored:module.regions()) { }
            }} catch(Throwable problem) { failure.compareAndSet(null,problem); }
        },"block-esp-rendering");
        updates.start();rendering.start();start.countDown();updates.join(20000);rendering.join(20000);
        check(!updates.isAlive()&&!rendering.isAlive(),"ESP update/render deadlock");
        if(failure.get()!=null)throw new AssertionError("concurrent ESP update/render failed",failure.get());
        module.regionChanged((net.minecraft.world.World)null,2,0,65,0,1);
        List<BlockMesh.Region> snapshot=module.regions();int size=snapshot.size();
        module.onWorldUnload(V115Check.c);
        check(snapshot.size()==size&&module.regions().isEmpty(),"regions list remained a live mutable view");
        V115Check.manager.requestEnabled(module,false);V115Check.manager.applyPendingChanges();
        System.out.println("PASS: concurrent Block ESP chunk changes, mesh rebuilds, render plans and unloads; stable region snapshots; no ConcurrentModificationException or torn mesh arrays");
    }
    public static void main(String[] args) throws Exception {
        V115Check.setup();InitialSettings.applyMissing(V115Check.manager);
        bedLifecycle();concurrentEsp();
    }
}
