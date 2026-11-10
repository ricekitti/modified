import dev.rice.client.*;
import dev.rice.client.hook.GlStateScope;
import dev.rice.module.impl.*;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import java.lang.reflect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Source HUD selection/layout/animation plus the actual painter with GL calls recorded. */
public final class ScaffoldHudTest {
   static int items,labels,counts;static ItemStack painted;static boolean failItem;
   static final RuntimeException PAINT_FAILURE=new IllegalStateException("simulated item renderer failure");
   public static final class Font extends FontRenderer {
      Font(){super(null,null,null,false);}
      @Override public int getStringWidth(String text){return text.length()*6;}
      @Override public int drawString(String text,float x,float y,int color,boolean shadow){if(text.equals("Amount:"))labels++;else counts++;return getStringWidth(text);}
   }
   public static final class ItemPainter extends RenderItem {
      ItemPainter(){super(null,null);}
      @Override public void renderItemAndEffectIntoGUI(ItemStack stack,int x,int y){items++;painted=stack;if(failItem)throw PAINT_FAILURE;}
   }
   static void check(boolean value,String message){if(!value)throw new AssertionError(message);System.out.println("ok   "+message);}
   static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
   static Object call(Object object,String name,Class<?>[] types,Object...args)throws Exception{Method method=object.getClass().getDeclaredMethod(name,types);method.setAccessible(true);return method.invoke(object,args);}
   public static void main(String[] args)throws Exception{
      Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule module=new ScaffoldModule();
      Method toggle=dev.rice.module.Module.class.getDeclaredMethod("setEnabledInternal",boolean.class,ClientContext.class);toggle.setAccessible(true);toggle.invoke(module,true,c);
      Object driver=field(module,"driver"),slots=field(driver,"slots");
      FeatureFixture.self.inventory.mainInventory[1]=new ItemStack(Blocks.stone,64);
      FeatureFixture.self.inventory.mainInventory[2]=new ItemStack(Blocks.torch,16);
      FeatureFixture.self.inventory.mainInventory[8]=new ItemStack(Blocks.glass,32);
      FeatureFixture.self.inventory.mainInventory[9]=new ItemStack(Blocks.stone,64);
      call(slots,"select",new Class<?>[]{ClientContext.class},c);
      ScaffoldHud.Selection selected=module.hudSelection(c);
      check(selected!=null&&selected.stack==FeatureFixture.self.inventory.mainInventory[1]&&selected.block&&selected.count==112,
            "HUD uses claimed server item and totals every hotbar ItemBlock, including non-full blocks, excluding main inventory");
      check(FeatureFixture.self.inventory.currentItem==0,"HUD selection does not move the visible hotbar slot");
      ((BooleanSetting)module.findSetting("render")).set(false);check(module.hudSelection(c)==null,"Render setting suppresses the slot panel");
      ((BooleanSetting)module.findSetting("render")).set(true);
      mc.currentScreen=new net.minecraft.client.gui.GuiChat();check(module.hudSelection(c)==null,"An open screen hides the active HUD selection");mc.currentScreen=null;
      FeatureFixture.self.inventory.mainInventory[1]=new ItemStack(Items.diamond_sword);
      selected=module.hudSelection(c);check(selected!=null&&!selected.block&&selected.count==1,"A changed shadow item uses item-only display and its own count");
      FeatureFixture.self.inventory.currentItem=1;check(module.hudSelection(c)==null,"Source HUD hides a matching visible non-block item");
      FeatureFixture.self.inventory.currentItem=0;FeatureFixture.self.inventory.mainInventory[1]=new ItemStack(Blocks.stone,64);
      selected=module.hudSelection(c);ScaffoldHud model=new ScaffoldHud();
      ScaffoldHud.Frame first=model.frame(selected,0,640,360,42,18);
      check(first.alpha==0&&Math.abs(first.scale-1.1)<1e-12&&first.width==89&&first.x==275.5&&first.y==270,
            "Panel starts at source 1.1 scale with measured width, centered ninety pixels above the bottom");
      ScaffoldHud.Frame middle=model.frame(selected,.45,640,360,42,18);
      check(Math.abs(middle.alpha-.96875)<1e-10,"Nine-hundred-millisecond expo animation reaches the source half-time fade");
      ScaffoldHud.Frame settled=model.frame(selected,.91,640,360,42,18);
      check(settled.scale==1&&settled.alpha==1,"Visible panel settles at full opacity and unit scale");
      ScaffoldHud.Selection changed=new ScaffoldHud.Selection(selected.stack,111,true);
      ScaffoldHud.Frame countChange=model.frame(changed,.95,640,360,42,18);
      check(countChange.alpha==1&&countChange.count.equals("111"),"Changing block count updates the label without restarting the entrance animation");
      ScaffoldHud.Frame leaving=model.frame(null,1,640,360,0,0);
      check(leaving.selection==changed&&leaving.width==89&&leaving.alpha==1,"Fade-out retains the last item, count and measured geometry");
      check(model.frame(null,1.91,640,360,0,0)==null&&model.remembered()==null,"Completed fade-out releases the retained ItemStack");
      model.frame(selected,2,640,360,42,18);check(model.frame(selected,1,640,360,42,18).alpha==0,"A backwards clock restarts panel animation predictably");
      check(model.frame(selected,Double.NaN,640,360,42,18)==null&&model.remembered()==null,"Invalid render time clears retained display state");
      ScaffoldHud.Frame icon=model.frame(new ScaffoldHud.Selection(new ItemStack(Items.diamond_sword),1,false),3,640,360,42,6);
      check(icon.width==22,"Non-block source panel reserves only the item icon width");

      Font font=FeatureFixture.blank(Font.class);ItemPainter painter=FeatureFixture.blank(ItemPainter.class);painter.zLevel=17;
      FeatureFixture.put(mc,"fontRendererObj",font);FeatureFixture.put(mc,"renderItem",painter);
      Class<?> renderer=Class.forName("dev.rice.module.impl.ScaffoldHudRenderer");Method draw=renderer.getDeclaredMethod("draw",Minecraft.class,ScaffoldHud.Frame.class,int.class,int.class,int.class);draw.setAccessible(true);
      FeatureFixture.resetHudGl();draw.invoke(null,mc,settled,0x4080c0,640,360);
      check(items==1&&painted==selected.stack&&labels==1&&counts==1,"Actual HUD painter draws the claimed item and Amount/count text");
      check(painter.zLevel==17&&!GlStateScope.active()&&FeatureFixture.hudGlBalanced(),"Painter restores item depth, both matrices, GL attributes and the existing cache scope");
      FeatureFixture.recordHudGl(0,5889);draw.invoke(null,mc,settled,0x4080c0,640,360);
      check(FeatureFixture.queryHudGl(2976)==5889,"Painter preserves an original projection-matrix mode");
      FeatureFixture.recordHudGl(0,5888);check(FeatureFixture.hudGlBalanced(),"Projection-mode entry leaves both matrix stacks balanced");
      failItem=true;FeatureFixture.resetHudGl();
      try{draw.invoke(null,mc,settled,0x4080c0,640,360);throw new AssertionError("Missing simulated item failure");}
      catch(InvocationTargetException expected){check(expected.getCause()==PAINT_FAILURE,"Painter preserves the original item-render exception");}
      finally{failItem=false;}
      check(painter.zLevel==17&&!GlStateScope.active()&&FeatureFixture.hudGlBalanced(),"Failed item rendering still restores the entire render scope");
      ScaffoldHud active=(ScaffoldHud)field(driver,"hud");active.frame(selected,4,640,360,42,18);
      ((BooleanSetting)module.findSetting("render")).set(false);module.renderHud(c,0,5);
      check(active.remembered()==null,"Turning Render off releases the previous item immediately");
      ((BooleanSetting)module.findSetting("render")).set(true);active.frame(selected,6,640,360,42,18);module.onWorldUnload(c);
      check(active.remembered()==null&&module.hudSelection(c)==null,"World unload releases the panel and its shadow-slot ownership");
      toggle.invoke(module,false,c);((ChoiceSetting)module.findSetting("sprint")).set(ScaffoldModule.SPRINT_WATCHDOG_PREDICTION_VM);
      int sentBefore=FeatureFixture.sentCount;float yawBefore=FeatureFixture.self.rotationYaw;c.setGuiEnabled(()->true);toggle.invoke(module,true,c);
      check(FeatureFixture.sentCount==sentBefore&&FeatureFixture.self.rotationYaw==yawBefore,"Enabling Scaffold in the native menu defers sprint startup packets and camera claims");
      c.setGuiEnabled(()->false);toggle.invoke(module,false,c);
      System.out.println("ALL SCAFFOLD HUD CHECKS PASSED");
   }
}
