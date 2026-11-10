package dev.rice.inject.transform;

import dev.rice.client.ClientContext;
import dev.rice.inject.Mappings;
import dev.rice.module.Module;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.DataWatcher;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Render activation regressions, real renderer patch placement, and backend bytecode invariance. */
public final class AutoBlockAnimationCheck {
   private static boolean right, left;
   private static Object unsafe;
   private static Method allocate;
   public static boolean button(int button) { return button == 1 ? right : left; }
   private static <T> T blank(Class<T> type) throws Exception { return type.cast(allocate.invoke(unsafe, type)); }
   private static void put(Object object, String name, Object value) throws Exception {
      for (Class<?> type = object.getClass(); type != null; type = type.getSuperclass()) {
         try { Field field = type.getDeclaredField(name); field.setAccessible(true); field.set(object, value); return; }
         catch (NoSuchFieldException next) { }
      }
      throw new NoSuchFieldException(name);
   }
   private static void require(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }
   public static final class Player extends EntityPlayerSP {
      boolean manual;
      Player() { super(null, null, null, null); }
      @Override public boolean isBlocking() { return manual; }
   }
   private static ClassNode read(byte[] bytes) {
      ClassNode node = new ClassNode();
      new ClassReader(bytes).accept(node, ClassReader.SKIP_DEBUG);
      return node;
   }
   private static byte[] methodBytes(MethodNode method) {
      ClassWriter writer = new ClassWriter(0);
      writer.visit(52, Opcodes.ACC_PUBLIC, "MethodCheck", null, "java/lang/Object", null);
      method.accept(writer);
      writer.visitEnd();
      return writer.toByteArray();
   }
   private static void backendUnchanged(byte[] baseline, byte[] current) {
      ClassNode old = read(baseline), fresh = read(current);
      int checked = 0;
      for (MethodNode method : old.methods) {
         if (method.name.equals("onRenderTickStart") || method.name.equals("setItemInUse")) continue;
         MethodNode match = null;
         for (MethodNode candidate : fresh.methods)
            if (candidate.name.equals(method.name) && candidate.desc.equals(method.desc)) match = candidate;
         require(match != null && Arrays.equals(methodBytes(method), methodBytes(match)),
            "Backend method changed: " + method.name + method.desc);
         checked++;
      }
      for (MethodNode method : fresh.methods) {
         if (!method.name.equals("animationUseCount") && !method.name.equals("onRenderTickStart")
            && !method.name.equals("setItemInUse")) continue;
         for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            require(!call.name.equals("useCount") && !call.name.equals("swingItem")
               && !call.name.equals("setKeyBindState") && !call.name.equals("sendPacket"),
               "Animation mutates gameplay: " + call.name);
         }
      }
      System.out.println("PASS: " + checked + " existing backend methods byte-identical; animation does not write item use, keys, swings or packets");
   }
   private static void renderer(String jarPath, String mapping) throws Exception {
      Mappings.load(mapping.equals("identity") ? null : Files.newInputStream(Paths.get(mapping)));
      Map<String, String[]> hierarchy = new HashMap<>();
      ClassNode renderer;
      try (JarFile jar = new JarFile(jarPath)) {
         Enumeration<JarEntry> entries = jar.entries();
         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.getName().endsWith(".class")) continue;
            try (InputStream stream = jar.getInputStream(entry)) {
               ClassReader reader = new ClassReader(stream);
               List<String> parents = new ArrayList<>(Arrays.asList(reader.getInterfaces()));
               if (reader.getSuperName() != null) parents.add(reader.getSuperName());
               hierarchy.put(reader.getClassName(), parents.toArray(new String[0]));
            }
         }
         Mappings.setHierarchyProvider(hierarchy::get);
         try (InputStream stream = jar.getInputStream(jar.getJarEntry(Mappings.mapClass(RicePatches.ITEM_RENDERER) + ".class"))) {
            renderer = new ClassNode(); new ClassReader(stream).accept(renderer, 0);
         }
      }
      int pose = 0, swing = 0;
      for (RicePatches.ClassEdits patch : RicePatches.create(false)) {
         if (!patch.target.equals(RicePatches.ITEM_RENDERER)) continue;
         for (RicePatches.MethodEdit edit : patch.edits) {
            MethodNode method = patch.find(renderer, edit);
            require(method != null, "renderer missing");
            int count = edit.apply(method);
            require(count == 1, "Expected one " + edit.label + ", got " + count);
            if (edit.label.equals("AutoBlock render-only block pose")) {
               for (AbstractInsnNode instruction : method.instructions.toArray()) {
                  if (!(instruction instanceof MethodInsnNode)) continue;
                  MethodInsnNode call = (MethodInsnNode) instruction;
                  if (!call.name.equals("autoBlockAnimationUseCount")) continue;
                  require(call.desc.equals(Mappings.mapDescriptor("(ILnet/minecraft/item/ItemStack;)I")), "pose descriptor");
                  require(RicePatches.prevReal(call) instanceof FieldInsnNode, "pose must receive rendered stack");
                  pose++;
               }
            } else {
               for (AbstractInsnNode instruction : method.instructions.toArray()) {
                  if (!RicePatches.isCall(instruction, RicePatches.ITEM_RENDERER, "doBlockTransformations", "()V")) continue;
                  AbstractInsnNode transform = RicePatches.prevReal(RicePatches.prevReal(instruction));
                  require(RicePatches.isCall(transform, RicePatches.ITEM_RENDERER, "transformFirstPersonItem", "(FF)V"), "block transform order");
                  AbstractInsnNode progress = RicePatches.prevReal(transform);
                  require(progress instanceof VarInsnNode && progress.getOpcode() == Opcodes.FLOAD
                     && ((VarInsnNode) progress).var == 4, "block must retain real swing progress");
                  swing++;
               }
            }
         }
      }
      require(pose == 1 && swing == 1, "pose/swing hooks missing");
      System.out.println("PASS: render-only pose and original block-hit swing on " + jarPath);
   }
   private static void activation(byte[] bytes) throws Exception {
      // Replace only physical mouse reads in the test copy. Production bytecode is untouched.
      ClassNode node = read(bytes);
      for (MethodNode method : node.methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
         if (!(instruction instanceof MethodInsnNode)) continue;
         MethodInsnNode call = (MethodInsnNode) instruction;
         if (call.owner.equals("org/lwjgl/input/Mouse") && call.name.equals("isButtonDown")) {
            call.owner = "dev/rice/inject/transform/AutoBlockAnimationCheck"; call.name = "button";
         }
      }
      ClassWriter writer = new ClassWriter(0); node.accept(writer);
      final byte[] testBytes = writer.toByteArray();
      Class<?> moduleType = new ClassLoader(AutoBlockAnimationCheck.class.getClassLoader()) {
         Class<?> define() { return defineClass("dev.rice.module.impl.AutoBlockModule", testBytes, 0, testBytes.length); }
      }.define();
      Bootstrap.register();
      Class<?> unsafeType = Class.forName("sun.misc.Unsafe"); Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true); unsafe = unsafeField.get(null); allocate = unsafeType.getMethod("allocateInstance", Class.class);
      Minecraft mc = blank(Minecraft.class); Player player = blank(Player.class), target = blank(Player.class);
      WorldClient world = blank(WorldClient.class);
      put(mc, "thePlayer", player); put(mc, "theWorld", world); put(mc, "gameSettings", blank(GameSettings.class));
      put(world, "playerEntities", new ArrayList<>(Arrays.asList(player, target)));
      player.inventory = new InventoryPlayer(player);
      DataWatcher targetData = new DataWatcher(target); targetData.addObject(6, Float.valueOf(20F));
      put(target, "dataWatcher", targetData);
      ItemStack sword = new ItemStack(Items.diamond_sword); player.inventory.mainInventory[0] = sword;
      put(player, "itemInUseCount", 17);
      player.swingProgress = .5F; player.prevSwingProgress = .25F;
      target.posX = 2;
      Module module = (Module)moduleType.getConstructors()[0].newInstance(new Object[]{null});
      put(module, "enabled", true);
      ((NumberSetting)module.findSetting("range")).set(3D);
      ((BooleanSetting)module.findSetting("require_right_mouse_pressed")).set(true);
      ((BooleanSetting)module.findSetting("require_left_mouse_pressed")).set(false);
      ((BooleanSetting)module.findSetting("fake_block_animation")).set(true);
      ((BooleanSetting)module.findSetting("fake_block_animation_require_target")).set(false);
      ClientContext context = new ClientContext(mc);
      Method render = moduleType.getMethod("animationUseCount", ClientContext.class, ItemStack.class, int.class);
      require((Integer)render.invoke(module, context, sword, 0) == 0, "force-block idle false activation");
      right = true; player.manual = true;
      require((Integer)render.invoke(module, context, sword, 40) == 40, "manual block missing");
      right = false;
      require((Integer)render.invoke(module, context, sword, 40) == 0, "manual release stuck");
      player.manual = false; right = true; put(module, "blocking", true);
      require((Integer)render.invoke(module, context, sword, 0) > 0, "automatic block missing");
      require((Integer)render.invoke(module, context, sword.copy(), 0) > 0,
         "renderer retains an equal sword stack after inventory synchronization; pose must not require object identity");
      ItemRenderer actualRenderer = blank(ItemRenderer.class);
      put(actualRenderer, "mc", mc); put(actualRenderer, "itemToRender", sword);
      put(actualRenderer, "equippedProgress", 1F);
      player.inventory.mainInventory[0] = sword.copy();
      actualRenderer.updateEquippedItem();
      Field itemToRender = ItemRenderer.class.getDeclaredField("itemToRender"); itemToRender.setAccessible(true);
      ItemStack retained = (ItemStack)itemToRender.get(actualRenderer);
      require(retained == sword && retained != player.getHeldItem(), "real renderer must reproduce retained stack case");
      require((Integer)render.invoke(module, context, retained, 0) > 0, "real updateEquippedItem suppressed automatic pose");
      player.inventory.mainInventory[0] = sword;
      require((Integer)render.invoke(module, context, new ItemStack(Items.iron_sword), 0) == 0,
         "a different sword during equip transition must not receive the forced pose");
      ((BooleanSetting)module.findSetting("fake_block_animation_require_target")).set(true);
      require((Integer)render.invoke(module, context, sword, 0) > 0, "in-range block missing");
      target.posX = 4;
      require((Integer)render.invoke(module, context, sword, 0) == 0, "range exit did not stop before next tick");
      player.manual = true;
      require((Integer)render.invoke(module, context, sword, 40) == 0, "manual pose ignored Only In Range");
      player.manual = false;
      target.posX = 2; target.isDead = true;
      require((Integer)render.invoke(module, context, sword, 0) == 0, "dead target activated pose");
      target.isDead = false; right = false;
      require((Integer)render.invoke(module, context, sword, 0) == 0, "stale backend flag ignores released right-click");
      ((BooleanSetting)module.findSetting("require_right_mouse_pressed")).set(false);
      require((Integer)render.invoke(module, context, sword, 0) > 0, "automatic block without RMB setting missing");
      put(module, "blocking", false); put(module, "lagActive", true);
      require((Integer)render.invoke(module, context, sword, 0) > 0, "Block Always lost active lag pose");
      ((BooleanSetting)module.findSetting("fake_block_animation")).set(false);
      require((Integer)render.invoke(module, context, sword, 0) == 0, "disabled force pose persisted");
      put(module, "lagActive", false);
      require((Integer)render.invoke(module, context, sword, 0) == 0, "release stuck");
      ItemStack bow = new ItemStack(Items.bow); player.inventory.mainInventory[0] = bow;
      require((Integer)render.invoke(module, context, bow, 50) == 50, "bow use altered");
      require((Integer)render.invoke(module, context, sword, 50) == 50, "stale equip stack altered");
      player.inventory.mainInventory[0] = sword; put(module, "enabled", false);
      require((Integer)render.invoke(module, context, sword, 0) == 0, "disable stuck");
      require((Integer)render.invoke(module, context, sword, 40) == 40, "disable must restore vanilla manual pose");
      module.onRenderTickStart(context);
      require(player.getItemInUseCount() == 17 && player.swingProgress == .5F && player.prevSwingProgress == .25F,
         "rendering changed real item-use or swing state");
      System.out.println("PASS: manual/automatic blocking, release, live range exit, dead target, force pose, lag transition, bow/equip handoff and disable");
   }
   public static void main(String[] args) throws Exception {
      byte[] baseline;
      try (JarFile jar = new JarFile(args[0]); InputStream stream = jar.getInputStream(jar.getJarEntry("dev/rice/module/impl/AutoBlockModule.class"))) {
         ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int n;
         while ((n = stream.read(buffer)) != -1) output.write(buffer, 0, n);
         baseline = output.toByteArray();
      }
      byte[] current = Files.readAllBytes(Paths.get(args[1]));
      backendUnchanged(baseline, current);
      try (JarFile jar = new JarFile(args[0])) {
         Path classes = Paths.get(args[1]).getParent().getParent().getParent().getParent().getParent();
         for (String inner : Arrays.asList("ClassEdits", "MethodEdit")) {
            String entry = "dev/rice/inject/transform/RicePatches$" + inner + ".class";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream stream = jar.getInputStream(jar.getJarEntry(entry))) {
               byte[] buffer = new byte[8192]; int n;
               while ((n = stream.read(buffer)) != -1) output.write(buffer, 0, n);
            }
            ClassWriter oldWriter = new ClassWriter(0), freshWriter = new ClassWriter(0);
            read(output.toByteArray()).accept(oldWriter);
            read(Files.readAllBytes(classes.resolve(entry))).accept(freshWriter);
            require(Arrays.equals(oldWriter.toByteArray(), freshWriter.toByteArray()), "non-debug change in " + inner);
         }
      }
      System.out.println("PASS: transformer nested classes changed only source debug information");
      renderer(args[2], args[3]);
      if (args.length > 4) renderer(args[4], args[5]);
      activation(current);
   }
}
