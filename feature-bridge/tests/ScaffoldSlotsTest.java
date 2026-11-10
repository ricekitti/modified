import dev.rice.client.*;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.module.NativeFeatureBridge;
import java.lang.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

/** Tests selection/history and the actual vanilla controller's adapted call sites. */
public final class ScaffoldSlotsTest {
    public static final class PlacementBlock extends ItemBlock {
        boolean allowed=true,placed=true;int checks;
        PlacementBlock(Block block){super(block);}
        @Override public boolean canPlaceBlockOnSide(World world,BlockPos pos,EnumFacing facing,EntityPlayer player,ItemStack stack){checks++;return allowed;}
        @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,BlockPos pos,EnumFacing facing,float x,float y,float z){if(placed)--stack.stackSize;return placed;}
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);System.out.println("ok   "+message);}
    static Object call(Object target,String name,Class<?>[] types,Object... values)throws Exception{
        Method method=target.getClass().getDeclaredMethod(name,types);method.setAccessible(true);return method.invoke(target,values);
    }
    static int history(Object slots,ItemStack stack,String key)throws Exception{
        Field field=slots.getClass().getDeclaredField("history");field.setAccessible(true);
        Object state=((java.util.Map<?,?>)field.get(slots)).get(stack);
        Field value=state.getClass().getDeclaredField(key);value.setAccessible(true);return value.getInt(state);
    }
    public static void main(String[] args)throws Exception{
        Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule module=new ScaffoldModule();
        Field context=NativeFeatureBridge.class.getDeclaredField("context");context.setAccessible(true);context.set(null,c);
        Field modules=NativeFeatureBridge.class.getDeclaredField("modules");modules.setAccessible(true);modules.set(null,new dev.rice.module.Module[]{module,module,module,module,module});
        ScaffoldSlotFixtureBridge.use(NativeFeatureBridge.class);
        Method toggle=dev.rice.module.Module.class.getDeclaredMethod("setEnabledInternal",boolean.class,ClientContext.class);toggle.setAccessible(true);toggle.invoke(module,true,c);
        Field driverField=ScaffoldModule.class.getDeclaredField("driver");driverField.setAccessible(true);Object driver=driverField.get(module);
        Field slotField=driver.getClass().getDeclaredField("slots");slotField.setAccessible(true);Object slots=slotField.get(driver);
        PlacementBlock item=new PlacementBlock(Blocks.stone);Item.itemRegistry.register(30000,new ResourceLocation("fixture","placement_block"),item);ItemStack blocks=new ItemStack(item,64);
        ItemStack small=new ItemStack(Blocks.stone,10);
        FeatureFixture.self.inventory.mainInventory[1]=small;
        FeatureFixture.self.inventory.mainInventory[2]=new ItemStack(Blocks.torch,64);
        FeatureFixture.self.inventory.mainInventory[3]=blocks;
        Class<?>[] ctx={ClientContext.class};
        check(((Integer)call(slots,"find",ctx,c))==3,"Fresh stacks use current-count threshold and reject non-full torch blocks");
        call(slots,"select",ctx,c);
        check(FeatureFixture.self.inventory.currentItem==0&&NativeFeatureBridge.inventorySlot(FeatureFixture.self.inventory)==3,"Server slot selection leaves visible currentItem unchanged");
        check(FeatureFixture.self.inventory.getCurrentItem()==blocks&&FeatureFixture.self.getHeldItem()==blocks,"Held-item accessors see the server-selected stack without changing the hotbar index");
        check(FeatureFixture.lastSent instanceof C09PacketHeldItemChange&&((C09PacketHeldItemChange)FeatureFixture.lastSent).getSlotId()==3,"Actual controller sync sends selected-slot packet");
        InventoryPlayer foreign=new InventoryPlayer(FeatureFixture.self);foreign.currentItem=2;
        check(NativeFeatureBridge.inventorySlot(foreign)==2,"Slot claim does not affect a different inventory");
        call(slots,"release",ctx,c);
        check(((C09PacketHeldItemChange)FeatureFixture.lastSent).getSlotId()==0,"Releasing claim syncs the real visible slot");
        ++FeatureFixture.self.ticksExisted;NativeFeatureBridge.inventoryTick();
        check(((Integer)call(slots,"find",ctx,c))==1,"Aged smaller stack wins by source hotbar order rather than largest count");
        small.stackSize=4;
        check(((Integer)call(slots,"find",ctx,c))==1,"Previous-count snapshot is distinct from current count");
        for(int n=0;n<5;n++)NativeFeatureBridge.placementAttempt(FeatureFixture.self,small);
        check(((Integer)call(slots,"find",ctx,c))==3,"Eligible attempts decrement the source previous-count gate");
        FeatureFixture.self.inventory.mainInventory[1]=null;
        int packets=FeatureFixture.sentPackets.size();call(slots,"select",ctx,c);
        check((ItemStack)call(slots,"stack",ctx,c)==blocks,"Placement uses the shadow slot's actual stack");
        boolean result=mc.playerController.onPlayerRightClick(FeatureFixture.self,mc.theWorld,blocks,new BlockPos(5,64,5),EnumFacing.UP,new Vec3(5.5,65,5.5));
        check(result&&item.checks==1&&blocks.stackSize==63,"Vanilla placement runs the original can-place predicate once and consumes the selected stack");
        check(FeatureFixture.sentPackets.get(packets) instanceof C09PacketHeldItemChange&&FeatureFixture.sentPackets.get(packets+1) instanceof C08PacketPlayerBlockPlacement,"Slot synchronization precedes placement packet");
        check(history(slots,blocks,"age")==0&&history(slots,blocks,"previousCount")==63,"Controller can-place hook resets history and decrements snapshot before use");
        item.placed=false;result=mc.playerController.onPlayerRightClick(FeatureFixture.self,mc.theWorld,blocks,new BlockPos(5,64,5),EnumFacing.UP,new Vec3(5.5,65,5.5));
        check(!result&&blocks.stackSize==63&&history(slots,blocks,"previousCount")==62,"Eligible failed use still counts as a source placement attempt");
        NativeFeatureBridge.inventoryTick();check(history(slots,blocks,"age")==0,"History ticks once per player tick");
        blocks.stackSize=50;
        for(int n=0;n<19;n++){++FeatureFixture.self.ticksExisted;NativeFeatureBridge.inventoryTick();}
        check(history(slots,blocks,"previousCount")==62,"History snapshot stays unchanged for nineteen ticks after attempt");
        ++FeatureFixture.self.ticksExisted;NativeFeatureBridge.inventoryTick();
        check(history(slots,blocks,"previousCount")==50,"Twentieth tick refreshes the snapshot from current stack count");
        item.allowed=false;packets=FeatureFixture.sentPackets.size();
        result=mc.playerController.onPlayerRightClick(FeatureFixture.self,mc.theWorld,blocks,new BlockPos(5,64,5),EnumFacing.UP,new Vec3(5.5,65,5.5));
        check(!result&&FeatureFixture.sentPackets.size()==packets&&history(slots,blocks,"previousCount")==50,"Rejected can-place predicate does not send or consume history");
        FeatureFixture.self.inventory.currentItem=2;call(slots,"release",ctx,c);
        check(FeatureFixture.self.inventory.currentItem==2&&((C09PacketHeldItemChange)FeatureFixture.lastSent).getSlotId()==2,"Cleanup restores the user's latest visible selection");
        call(slots,"select",ctx,c);SemanticEvent changed=new SemanticEvent(3);changed.packet=new C09PacketHeldItemChange(6);module.onEvent(c,changed);
        packets=FeatureFixture.sentPackets.size();call(slots,"release",ctx,c);
        check(FeatureFixture.sentPackets.size()==packets,"Cleanup does not overwrite another module's newer server-slot change");
        FeatureFixture.self.inventory.mainInventory[3]=null;
        check(((Integer)call(slots,"find",ctx,c))==-1,"No eligible block stack returns no slot");
        FeatureFixture.self.inventory.mainInventory[4]=new ItemStack(Blocks.glass,64);
        check(((Integer)call(slots,"find",ctx,c))==4,"Source glass exception is eligible");
        call(slots,"select",ctx,c);mc.currentScreen=new net.minecraft.client.gui.GuiScreen(){};call(slots,"select",ctx,c);
        check(NativeFeatureBridge.inventorySlot(FeatureFixture.self.inventory)==2,"Opening a menu releases the shadow slot");mc.currentScreen=null;
        module.onWorldUnload(c);
        Field map=slots.getClass().getDeclaredField("history");map.setAccessible(true);
        check(((java.util.Map<?,?>)map.get(slots)).isEmpty(),"World unload clears stack identity history");
        System.out.println("ALL SCAFFOLD SLOT CHECKS PASSED");
    }
}
