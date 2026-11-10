import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.*;
import net.minecraft.init.*;
import net.minecraft.item.*;

/** Independent source-recovered presets/editor/matcher checks. No purchase loop or confirmation stub. */
public final class QuickBuyFixture {
    static int checks;
    static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
    static Object call(Object o,String name,Class<?>[] types,Object...args)throws Exception{return o.getClass().getMethod(name,types).invoke(o,args);}
    @SuppressWarnings({"unchecked","rawtypes"})
    public static void run(Class<?> bridge)throws Exception{
        checks=0;ClassLoader loader=bridge.getClassLoader();Class<?> item=loader.loadClass("dev.rice.quickbuy.BedWarsShopItem");
        Object wool=Enum.valueOf((Class)item,"WOOL"),sword=Enum.valueOf((Class)item,"IRON_ARMOR");
        Class<?> modelType=loader.loadClass("dev.rice.quickbuy.QuickBuyPresetsSetting");Object model=modelType.getConstructor().newInstance();
        check((Integer)call(model,"getActivePreset",new Class[0])==0,"default first preset");
        call(model,"setActivePreset",new Class[]{int.class},99);check((Integer)call(model,"getActivePreset",new Class[0])==4,"preset upper clamp");
        call(model,"setActivePreset",new Class[]{int.class},-1);check((Integer)call(model,"getActivePreset",new Class[0])==0,"preset lower clamp");
        for(int i=0;i<20;i++)call(model,"add",new Class[]{int.class,item},0,wool);
        check((Integer)call(model,"getCount",new Class[]{int.class,item},0,wool)==16,"repeatable count capped at sixteen");
        call(model,"add",new Class[]{int.class,item},0,sword);call(model,"add",new Class[]{int.class,item},0,sword);
        check((Integer)call(model,"getCount",new Class[]{int.class,item},0,sword)==1,"nonrepeatable upgrades stay one");
        List<?> snapshot=(List<?>)call(model,"snapshotActivePreset",new Class[0]);
        call(model,"decrement",new Class[]{int.class,int.class},0,0);
        check((Integer)call(snapshot.get(0),"getCount",new Class[0])==16,"queue snapshot owns copies");
        check((Integer)call(model,"getCount",new Class[]{int.class,item},0,wool)==15,"decrement updates live count");
        call(model,"move",new Class[]{int.class,int.class,int.class},0,0,1);
        List<?> live=(List<?>)call(model,"getPreset",new Class[]{int.class},0);
        check(call(live.get(0),"getItemId",new Class[0]).equals("iron_armor"),"purchase ordering retained");
        try{live.clear();throw new AssertionError("mutable preset view");}catch(UnsupportedOperationException expected){checks++;}
        JsonObject json=new JsonObject();call(model,"writeProfile",new Class[]{JsonObject.class},json);
        Object restored=modelType.getConstructor().newInstance();call(restored,"loadProfile",new Class[]{JsonObject.class},json);
        check((Integer)call(restored,"getCount",new Class[]{int.class,item},0,wool)==15,"profile JSON roundtrip");
        JsonArray entries=json.getAsJsonObject("Presets").getAsJsonArray("presets").get(0).getAsJsonArray();
        JsonObject bad=new JsonObject();bad.addProperty("item","unknown");bad.addProperty("count",999);entries.add(bad);entries.add(entries.get(0));
        call(restored,"loadProfile",new Class[]{JsonObject.class},json);
        check(((List<?>)call(restored,"getPreset",new Class[]{int.class},0)).size()==2,"unknown and duplicate entries rejected");
        ItemStack stack=new ItemStack(Blocks.wool,32).setStackDisplayName("Wool");
        check((Boolean)call(wool,"matches",new Class[]{ItemStack.class},stack),"shop wool matching");
        check((Integer)call(wool,"inventoryProgress",new Class[]{ItemStack[][].class},(Object)new ItemStack[][]{{stack}})>0,"inventory progress counts real items");
        Class<?> category=loader.loadClass("dev.rice.quickbuy.BedWarsShopItem$Category");Object blocks=Enum.valueOf((Class)category,"BLOCKS");
        Class<?> matcher=loader.loadClass("dev.rice.quickbuy.QuickBuyShopMatcher");
        InventoryBasic inventory=new InventoryBasic("Item Shop",true,54);ContainerChest container=new ContainerChest(FeatureFixture.self.inventory,inventory,FeatureFixture.self);
        inventory.setInventorySlotContents(26,stack);
        check((Integer)matcher.getMethod("findItemSlot",ContainerChest.class,item).invoke(null,container,wool)==-1,"reserved shop slot excluded");
        inventory.setInventorySlotContents(18,stack);check((Integer)matcher.getMethod("findItemSlot",ContainerChest.class,item).invoke(null,container,wool)==18,"purchase slot lookup");
        inventory.setInventorySlotContents(3,new ItemStack(Blocks.wool).setStackDisplayName("Blocks"));
        check((Integer)matcher.getMethod("findCategorySlot",ContainerChest.class,category).invoke(null,container,blocks)==3,"category title matching");
        GuiScreen previousScreen=FeatureFixture.minecraft.currentScreen;Container previousContainer=FeatureFixture.self.openContainer;
        try{
            FeatureFixture.minecraft.currentScreen=new GuiChest(FeatureFixture.self.inventory,inventory);FeatureFixture.self.openContainer=container;
            check(matcher.getMethod("currentItemShop").invoke(null)==container,"actual item-shop title accepted");
            FeatureFixture.self.openContainer=new ContainerChest(FeatureFixture.self.inventory,new InventoryBasic("Ender Chest",true,54),FeatureFixture.self);
            check(matcher.getMethod("currentItemShop").invoke(null)==null,"ordinary chest rejected");
        }finally{FeatureFixture.minecraft.currentScreen=previousScreen;FeatureFixture.self.openContainer=previousContainer;}
        Class<?> editorType=loader.loadClass("dev.rice.quickbuy.QuickBuyPresetScreen");GuiScreen editor=(GuiScreen)editorType.getConstructor(modelType,GuiScreen.class).newInstance(model,null);
        editor.setWorldAndResolution(FeatureFixture.minecraft,800,500);
        check(!editor.doesGuiPauseGame(),"editor does not pause game");
        Method cycle=editorType.getDeclaredMethod("cyclePreset",int.class);cycle.setAccessible(true);cycle.invoke(editor,1);
        check((Integer)call(model,"getActivePreset",new Class[0])==1,"editor cycles active preset");editor.onGuiClosed();
        check((Long)call(model,"getRevision",new Class[0])>0,"mutations expose dirty revision for host persistence");
        System.out.println("Quick Buy helpers: "+checks+" preset, editor, shop matching and serialization checks passed; automatic purchases remain blocked");
    }
}
