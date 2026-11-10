import java.lang.reflect.*;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.util.*;

/** Test-only equivalent of the bootstrap-native-to-isolated-bridge handoff. */
public final class ScaffoldSlotFixtureBridge {
    private static Method slot,canPlace;
    public static void use(Class<?> bridge) throws Exception {
        slot=bridge.getMethod("inventorySlot",Object.class);
        canPlace=bridge.getMethod("canPlaceAndRecord",Object.class,Object.class,Object.class,Object.class,Object.class,Object.class);
    }
    private static Object call(Method method,Object... args){
        try{return method.invoke(null,args);}
        catch(InvocationTargetException failure){
            Throwable cause=failure.getCause();if(cause instanceof RuntimeException)throw (RuntimeException)cause;
            if(cause instanceof Error)throw (Error)cause;throw new IllegalStateException(cause);
        }catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
    }
    public static int inventorySlot(Object inventory){return slot==null?((InventoryPlayer)inventory).currentItem:((Integer)call(slot,inventory)).intValue();}
    public static boolean canPlace(Object item,Object world,Object pos,Object facing,Object player,Object stack){
        if(canPlace!=null)return ((Boolean)call(canPlace,item,world,pos,facing,player,stack)).booleanValue();
        return ((ItemBlock)item).canPlaceBlockOnSide((World)world,(BlockPos)pos,(EnumFacing)facing,(EntityPlayer)player,(ItemStack)stack);
    }
}
