package dev.rice.inject.transform;

import dev.rice.inject.Mappings;
import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.ClassWriter;
import dev.rice.shaded.asm.Opcodes;
import dev.rice.shaded.asm.Type;
import dev.rice.shaded.asm.tree.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * Applies every RicePatches edit (vanilla and Forge variants) to real Minecraft 1.8.9 bytecode in one naming scheme.
 *
 * <p>For each edit it requires:
 * <ul>
 *   <li>the method is found;</li>
 *   <li>the injection count equals the declared count (or is at least 1 where the count is open);</li>
 *   <li>head hooks run before any original call;</li>
 *   <li>"before return" hooks sit directly before every return;</li>
 *   <li>the patched class still gets frames from ASM (COMPUTE_FRAMES, as TransformerEngine writes it).</li>
 * </ul>
 * In the named run, every injected call must also reach a real static hook method with exactly that descriptor.
 *
 * <p>Replaces ZestTapHookCheck (ZestTap was removed in v115). It keeps that check's movement-input placement
 * assertion, which KillAura and Stasis rely on. Since v119 the generated Mayu injector bridge (myau/host/MayuMixins)
 * is a hook owner too; its calls must sit exactly at the Mayu mixin injection points (see mayuPoints).
 */
public final class HookPlacementCheck {
   private static void blockInCompatibility() {
      RicePatches.MethodEdit walkingEdit=null;
      for(RicePatches.ClassEdits patch:RicePatches.create(false))for(RicePatches.MethodEdit edit:patch.edits)
         if(patch.target.equals(RicePatches.PLAYER)&&edit.label.startsWith("Snowify Block In"))walkingEdit=edit;
      require(walkingEdit!=null,"Block In walking edit absent");
      for(boolean partial:new boolean[]{false,true}){
         MethodNode method=new MethodNode(Opcodes.ACC_PUBLIC,"onUpdateWalkingPlayer","()V",null,null);
         if(partial){method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,Mappings.mapClass(RicePatches.PLAYER),Mappings.mapField(RicePatches.ENTITY,"rotationYaw"),"F"));method.instructions.add(new InsnNode(Opcodes.POP));}
         method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,Mappings.mapClass(RicePatches.PLAYER),"lunar$walkingDelegate","()V",false));method.instructions.add(new InsnNode(Opcodes.RETURN));
         require(walkingEdit.apply(method)>0,"delegated walking method not adapted");
         require(countHook(method,"dev/rice/client/blockin/SuppliedBlockInHost","beginWalking")==1,"delegated walking scope missing");
         require(countHook(method,"dev/rice/client/blockin/SuppliedBlockInHost","endWalking")==2&&!method.tryCatchBlocks.isEmpty(),"delegated walking return/throw cleanup missing");
      }
   }

   private static boolean isHook(String owner) {
      return owner.startsWith("dev/rice/") || owner.startsWith("myau/host/");
   }

   private static final Map<String, String> supers = new HashMap<>();
   private static final Map<String, Boolean> interfaces = new HashMap<>();
   private static final Map<String, String[]> hierarchy = new HashMap<>();

   public static void main(String[] args) throws Exception {
      boolean named = args[1].equals("identity");
      Mappings.load(named ? null : Files.newInputStream(Paths.get(args[1])));
      try (JarFile jar = new JarFile(args[0])) {
         Enumeration<JarEntry> entries = jar.entries();
         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.getName().endsWith(".class")) continue;
            try (InputStream stream = jar.getInputStream(entry)) {
               ClassReader reader = new ClassReader(stream);
               List<String> parents = new ArrayList<>(Arrays.asList(reader.getInterfaces()));
               if (reader.getSuperName() != null) parents.add(reader.getSuperName());
               hierarchy.put(reader.getClassName(), parents.toArray(new String[0]));
               supers.put(reader.getClassName(), reader.getSuperName());
               interfaces.put(reader.getClassName(), (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0);
            }
         }
      }
      Mappings.setHierarchyProvider(hierarchy::get);

      int edits = 0, heads = 0, tails = 0, hooks = 0;
      Set<String> labels = new HashSet<>();
      try (JarFile jar = new JarFile(args[0])) {
         for (boolean forge : new boolean[]{false, true}) {
            for (RicePatches.ClassEdits patch : RicePatches.create(forge)) {
               ClassNode node = read(jar, Mappings.mapClass(patch.target));
               require(node != null, "missing target class " + patch.target);
               for (RicePatches.MethodEdit edit : patch.edits) {
                  MethodNode method = patch.find(node, edit);
                  require(method != null, patch.target + ": method not found for " + edit.describe());
                  int points = edit.apply(method);
                  require(points > 0, patch.target + ": no injection point for " + edit.describe());
                  require(edit.expected < 0 || points == edit.expected,
                     patch.target + ": " + points + " injection points (expected " + edit.expected + ") for " + edit.describe());
                  edits++;
                  labels.add(edit.label);
               }
               if (patch.hasEveryMethodEdits()) {
                  int points = patch.applyEveryMethod(node);
                  require(points == patch.getEveryMethodExpected(), patch.target + ": " + points + " points for " + patch.getEveryMethodLabel());
               }
               for (RicePatches.MethodEdit edit : patch.edits) {
                  MethodNode method = patch.find(node, edit);
                  String label = edit.label.toLowerCase(Locale.ROOT);
                  if (label.contains("head")) {
                     MethodInsnNode first = firstCall(method);
                     require(first != null && isHook(first.owner), patch.target + ": head hook is not the first call in " + edit.describe());
                     heads++;
                  }
                  if (label.contains("before return")) {
                     for (AbstractInsnNode insn : method.instructions.toArray()) {
                        if (!RicePatches.isReturn(insn)) continue;
                        AbstractInsnNode previous = RicePatches.prevReal(insn);
                        require(previous instanceof MethodInsnNode && isHook(((MethodInsnNode)previous).owner),
                           patch.target + ": return without a directly preceding hook in " + edit.describe());
                        tails++;
                     }
                  }
               }
               for (MethodNode method : node.methods) {
                  for (AbstractInsnNode insn : method.instructions.toArray()) {
                     if (insn instanceof MethodInsnNode && isHook(((MethodInsnNode)insn).owner)) {
                        MethodInsnNode call = (MethodInsnNode)insn;
                        require(call.getOpcode() == Opcodes.INVOKESTATIC, "hook call is not static: " + call.owner + "." + call.name);
                        if (named) requireHook(call);
                        hooks++;
                     }
                  }
               }
               ClassWriter writer = new FrameWriter();
               node.accept(writer);
               require(writer.toByteArray().length > 0, "empty class " + patch.target);
            }
         }
         // AutoBlock was rolled back to v136. Its later render-only overrides must be absent.
         MethodNode itemRenderer = patched(jar, RicePatches.ITEM_RENDERER, false, "renderItemInFirstPerson", "(F)V");
         for (AbstractInsnNode insn : itemRenderer.instructions.toArray()) {
            if (insn instanceof MethodInsnNode) require(!((MethodInsnNode)insn).name.startsWith("autoBlockAnimation"),"post-v136 AutoBlock renderer hook survived rollback");
         }
         for (String name : new String[]{"clickMouse","rightClickMouse","sendClickBlockToController"}) {
            MethodNode click=patched(jar,"net/minecraft/client/Minecraft",false,name,name.equals("sendClickBlockToController") ? "(Z)V" : "()V");
            require(isHookCall(firstCall(click),"dev/rice/client/hook/GameHooks","guiBlocksClicks"),"GUI must cancel before gameplay calls: "+name);
         }
         MethodNode blockInWalking=patched(jar,RicePatches.PLAYER,false,"onUpdateWalkingPlayer","()V");
         int blockInYaw=0,blockInPitch=0;
         for(AbstractInsnNode insn:blockInWalking.instructions.toArray()){
            if(insn instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)insn;if(call.owner.equals("dev/rice/client/blockin/SuppliedBlockInHost")){if(call.name.equals("spoofYaw"))blockInYaw++;if(call.name.equals("spoofPitch"))blockInPitch++;}}
            if(insn instanceof FieldInsnNode&&insn.getOpcode()==Opcodes.GETFIELD){FieldInsnNode field=(FieldInsnNode)insn;require(!field.name.equals(Mappings.mapField(RicePatches.ENTITY,"rotationYaw"))&&!field.name.equals(Mappings.mapField(RicePatches.ENTITY,"rotationPitch")),"walking packet bypasses Snowify rotation construction");}
         }
         require(blockInYaw>0&&blockInPitch>0,"Snowify walking rotation accessors missing");
         MethodNode acTab=patched(jar,"net/minecraft/client/gui/GuiPlayerTabOverlay",false,"getPlayerName","(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;");
         MethodNode acName=patched(jar,"net/minecraft/entity/player/EntityPlayer",false,"getDisplayName","()Lnet/minecraft/util/IChatComponent;");
         for(MethodNode marker:new MethodNode[]{acTab,acName})for(AbstractInsnNode insn:marker.instructions.toArray())if(insn.getOpcode()==Opcodes.ARETURN)require(RicePatches.prevReal(insn) instanceof MethodInsnNode&&((MethodInsnNode)RicePatches.prevReal(insn)).owner.equals("dev/rice/client/anticheat/SnowifyAntiCheatHost"),"Snowify marker misses return");
         blockInCompatibility();
         movementInput(jar);
         moveFlying(jar);
         mayuPoints(jar);
         ravenBsPoints(jar);
      }
      for (String required : new String[]{
         "Raven bS mouse-over completion before returns", "Raven bS pre-player-interact before queued keys",
         "Raven bS rotation update before mouse selection", "Raven bS entity attack at head (cancellable)",
         "1.7 blockhit animation (swing while blocking)", "GUI click isolation at head",
         "entityJoin at head (OpenB4 AntiFireball EntityJoinWorldEvent)",
         "OpenB4 Hooks.preMotion at head; returns early when the OpenB4 packet pipeline sent the packets",
         "OpenJade RenderTickEvent START at game-loop head",
         "OpenB4/Mayu/OpenJade inbound packet hook at head (cancellable)",
         "OpenJade RenderTickEvent END (HUD pass) before returns", "OpenJade Opsec click redirect (cancellable head)",
         "OpenJade manual-input block (cancellable head)", "OpenJade Opsec slot-draw capture + displayed-stack substitution",
         "OpenJade Opsec setup text before returns", MOVEMENT}) {
         require(labels.contains(required), "edit missing: " + required);
      }
      // Removed in v119: the OpenJade Scaffold/Clutch Assist hooks (jump/moveFlying body replacements, walking-packet
      // rebuild, player-update head) and v118's hand-written meow hooks; also v115's ZestTap.
      for (String removed : new String[]{"scaffold", "Scaffold", "ZestTap", "zesttap", "meow", "OpenJade JumpEvent", "OpenJade PreUpdateEvent",
         "walking-packet rebuild"}) {
         for (String label : labels) require(!label.contains(removed), "removed scaffold/ZestTap edit still present: " + label);
      }
      System.out.println("PASS: " + args[1] + " " + edits + " edits applied with declared counts, " + heads + " head hooks first, "
         + tails + " returns hooked, " + hooks + " hook calls" + (named ? " resolved to real methods" : "") + ", frames computed");
   }

   private static ClassNode read(JarFile jar, String name) throws IOException {
      JarEntry entry = jar.getJarEntry(name + ".class");
      if (entry == null) return null;
      ClassNode node = new ClassNode();
      try (InputStream stream = jar.getInputStream(entry)) {
         new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
      }
      return node;
   }

   private static MethodInsnNode firstCall(MethodNode method) {
      for (AbstractInsnNode insn : method.instructions.toArray()) {
         if (insn instanceof MethodInsnNode) return (MethodInsnNode)insn;
      }
      return null;
   }

   /** The hook must be a static method of that exact (runtime) descriptor. */
   private static void requireHook(MethodInsnNode call) throws Exception {
      Class<?> owner = Class.forName(call.owner.replace('/', '.'), false, HookPlacementCheck.class.getClassLoader());
      for (Method method : owner.getDeclaredMethods()) {
         if (method.getName().equals(call.name) && Type.getMethodDescriptor(method).equals(call.desc)) {
            require(Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()), "hook not public static: " + call.owner + "." + call.name);
            return;
         }
      }
      throw new AssertionError("hook method missing: " + call.owner + "." + call.name + call.desc);
   }

   static final String MOVEMENT = "Mayu afterMovementInputUpdate, then Rice movement input, after vanilla refresh";
   static final String MAYU = "myau/host/MayuMixins";

   /** bS rotation and click phases must match their original mixin injection points. */
   private static void ravenBsPoints(JarFile jar) throws IOException {
      MethodNode tick = patched(jar, "net/minecraft/client/Minecraft", false, "runTick", "()V");
      int rotations = 0, interactions = 0;
      for (AbstractInsnNode insn : tick.instructions.toArray()) {
         if (!(insn instanceof MethodInsnNode)) continue;
         MethodInsnNode call = (MethodInsnNode)insn;
         if (isHookCall(call, "dev/rice/client/hook/GameHooks", "beforeTickMouseOver")) {
            AbstractInsnNode next = RicePatches.nextReal(insn);
            require(next instanceof MethodInsnNode && ((MethodInsnNode)next).owner.equals(Mappings.mapClass("net/minecraft/client/renderer/EntityRenderer"))
               && ((MethodInsnNode)next).name.equals(Mappings.mapMethod("net/minecraft/client/renderer/EntityRenderer", "getMouseOver", "(F)V")),
               "bS rotation update must precede tick mouse selection");
            rotations++;
         }
         if (isHookCall(call, "dev/rice/client/hook/GameHooks", "prePlayerInteract")) {
            AbstractInsnNode next = RicePatches.nextReal(insn);
            require(next instanceof FieldInsnNode && ((FieldInsnNode)next).getOpcode() == Opcodes.GETFIELD
               && ((FieldInsnNode)next).name.equals(Mappings.mapField("net/minecraft/client/settings/GameSettings", "chatVisibility")),
               "bS pre-interaction must precede chat visibility field");
            interactions++;
         }
      }
      require(rotations == 1 && interactions == 1, "bS tick phases must each be injected once");
      MethodNode ray = patched(jar, "net/minecraft/client/renderer/EntityRenderer", false, "getMouseOver", "(F)V");
      for (AbstractInsnNode insn : ray.instructions.toArray()) {
         if (RicePatches.isReturn(insn)) require(isHookCall((RicePatches.prevReal(insn) instanceof MethodInsnNode ? (MethodInsnNode)RicePatches.prevReal(insn) : null),
            "dev/rice/client/hook/GameHooks", "afterRay"), "bS ray cleanup must cover custom and vanilla returns");
      }
   }

   /**
    * The movement hooks run immediately after the vanilla input refresh: first Mayu's MovementInputEvent
    * (@At(INVOKE, shift = AFTER)), then Rice's movement input (KillAura, Stasis).
    */
   private static void movementInput(JarFile jar) throws IOException {
      String player = "net/minecraft/client/entity/EntityPlayerSP";
      MethodNode method = patched(jar, player, false, "onLivingUpdate", "()V");
      int checked = 0;
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
         if (!(instruction instanceof MethodInsnNode) || !((MethodInsnNode)instruction).name.equals("afterMovementInput")) continue;
         MethodInsnNode call = (MethodInsnNode)instruction;
         AbstractInsnNode mayu = RicePatches.prevReal(RicePatches.prevReal(call));
         require(isHookCall(mayu, MAYU, "afterMovementInputUpdate"), "Rice movement input must directly follow Mayu's MovementInputEvent");
         require(RicePatches.isCall(RicePatches.prevReal(mayu), "net/minecraft/util/MovementInput", "updatePlayerMoveState", "()V"),
            "hooks must run immediately after vanilla input refresh");
         require(call.desc.equals(Mappings.mapDescriptor("(L" + player + ";)V")), "hook descriptor must match runtime");
         checked++;
      }
      require(checked == 1, "movement hook missing or duplicated");
   }

   /** Entity.moveFlying keeps only Rice's argument rewrite; the OpenJade/meow body replacements are gone (Mayu redirects call sites). */
   private static void moveFlying(JarFile jar) throws IOException {
      MethodNode method = patched(jar, "net/minecraft/entity/Entity", false, "moveFlying", "(FFF)V");
      boolean before = false;
      for (AbstractInsnNode insn : method.instructions.toArray()) {
         if (insn instanceof MethodInsnNode) {
            String name = ((MethodInsnNode)insn).name;
            before |= name.equals("beforeMoveFlying");
            require(!name.equals("jadeMoveFlying") && !name.equals("moveFlying"), "moveFlying: body replacement still present: " + name);
         }
      }
      require(before, "moveFlying: Rice argument rewrite missing");
   }

   /** Every Mayu injector call sits exactly at its Mixin target point (vanilla patch set). */
   private static void mayuPoints(JarFile jar) throws IOException {
      // MixinRenderManager: visual pose is scoped to drawing the entity, including exceptional exits.
      MethodNode model = patched(jar, "net/minecraft/client/renderer/entity/RenderManager", false,
         "renderEntityStatic", "(Lnet/minecraft/entity/Entity;FZ)Z");
      require(isHookCall(firstCall(model), MAYU, "beforeRenderEntityStatic"), "renderEntityStatic: Mayu pose must run first");
      for (AbstractInsnNode insn : model.instructions.toArray()) {
         if (RicePatches.isReturn(insn) || insn.getOpcode() == Opcodes.ATHROW) {
            require(isHookCall(RicePatches.prevReal(insn), MAYU, "afterRenderEntityStatic"),
               "renderEntityStatic: pose not restored on return/throw");
         }
      }
      require(!model.tryCatchBlocks.isEmpty(), "renderEntityStatic: missing exception cleanup scope");
      String player = "net/minecraft/client/entity/EntityPlayerSP";
      // MixinEntityPlayerSP.onUpdate: HEAD, @Redirect isRiding, @At(INVOKE onUpdateWalkingPlayer), RETURN.
      MethodNode update = patched(jar, player, false, "onUpdate", "()V");
      require(isHookCall(firstCall(update), "dev/rice/client/hook/GameHooks", "mayuPlayerUpdateHead"), "onUpdate: Mayu HEAD is not first");
      int redirects = 0, walking = 0;
      for (AbstractInsnNode insn : update.instructions.toArray()) {
         require(!RicePatches.isCall(insn, player, "isRiding", "()Z"), "onUpdate: isRiding not redirected");
         if (isHookCall(insn, MAYU, "applySpoofedRotationBeforeRidingCheck")) {
            require(RicePatches.prevReal(insn).getOpcode() == Opcodes.DUP, "onUpdate: redirect must receive the riding receiver twice");
            redirects++;
         }
         if (RicePatches.isCall(insn, player, "onUpdateWalkingPlayer", "()V")) {
            require(isHookCall(RicePatches.prevReal(RicePatches.prevReal(insn)), MAYU, "beforeWalkingPlayerUpdate"), "onUpdate: PreWalkingUpdateEvent not before onUpdateWalkingPlayer");
            walking++;
         }
         if (RicePatches.isReturn(insn)) require(isHookCall(RicePatches.prevReal(insn), MAYU, "afterPlayerUpdate"), "onUpdate: RETURN not hooked");
      }
      require(redirects == 1 && walking == 1, "onUpdate: redirect/walking hook count");
      // MixinEntityPlayerSP.onLivingUpdate: LivingUpdateEvent before super.onLivingUpdate().
      MethodNode living = patched(jar, player, false, "onLivingUpdate", "()V");
      int supers = 0;
      for (AbstractInsnNode insn : living.instructions.toArray()) {
         if (RicePatches.isCall(insn, "net/minecraft/client/entity/AbstractClientPlayer", "onLivingUpdate", "()V")) {
            AbstractInsnNode inject=RicePatches.prevReal(RicePatches.prevReal(insn));

            require(isHookCall(inject,MAYU,"beforeLivingUpdate"),"Mayu BedNuker living hook lost ordering");
            supers++;
         }
      }
      require(supers == 1, "onLivingUpdate super call count");
      // MixinEntityLivingBase: @ModifyVariable jump STORE (first float local), @Redirect moveFlying in moveEntityWithHeading.
      MethodNode jump = patched(jar, "net/minecraft/entity/EntityLivingBase", false, "jump", "()V");
      AbstractInsnNode fstore = first(jump, Opcodes.FSTORE);
      require(fstore != null && isHookCall(RicePatches.prevReal(fstore), MAYU, "modifyJumpYawRadians"), "jump: yaw STORE not modified");
      MethodNode heading = patched(jar, "net/minecraft/entity/EntityLivingBase", false, "moveEntityWithHeading", "(FF)V");
      require(countHook(heading, MAYU, "redirectMoveFlying") == 3, "moveEntityWithHeading: three moveFlying redirects expected");
      for (AbstractInsnNode insn : heading.instructions.toArray()) {
         require(!RicePatches.isCall(insn, "net/minecraft/entity/EntityLivingBase", "moveFlying", "(FFF)V"), "moveEntityWithHeading: moveFlying not redirected");
      }
      // MixinEntity: @ModifyVariable moveEntity flag STORE, @Inject setVelocity HEAD.
      MethodNode move = patched(jar, "net/minecraft/entity/Entity", false, "moveEntity", "(DDD)V");
      AbstractInsnNode istore = first(move, Opcodes.ISTORE);
      require(istore != null && isHookCall(RicePatches.prevReal(istore), MAYU, "modifySafeWalkFlag"), "moveEntity: flag STORE not modified");
      require(isHookCall(firstCall(patched(jar, "net/minecraft/entity/Entity", false, "setVelocity", "(DDD)V")), MAYU, "beforeSetVelocity"), "setVelocity: HEAD");
      // MixinKeyBinding.isPressed RETURN.
      MethodNode pressed = patched(jar, "net/minecraft/client/settings/KeyBinding", false, "isPressed", "()Z");
      for (AbstractInsnNode insn : pressed.instructions.toArray()) {
         if (RicePatches.isReturn(insn)) require(isHookCall(RicePatches.prevReal(insn), MAYU, "onIsPressed"), "isPressed: RETURN not hooked");
      }
      // MixinMinecraft: runTick HEAD/RETURN, wheel redirect.
      MethodNode tick = patched(jar, "net/minecraft/client/Minecraft", false, "runTick", "()V");
      MethodInsnNode tickHead = firstCall(tick);
      require(isHookCall(tickHead, "dev/rice/client/hook/GameHooks", "mayuTickHead")
         && isHookCall(RicePatches.nextReal(RicePatches.nextReal(tickHead)), MAYU, "beforeRunTick"), "runTick: rotation claim, then PreTickEvent, must be first");
      for (AbstractInsnNode insn : tick.instructions.toArray()) {
         if (RicePatches.isReturn(insn)) require(isHookCall(RicePatches.prevReal(insn), MAYU, "afterRunTick"), "runTick: PostTickEvent not before return");
      }
      ClassNode minecraft = patchedClass(jar, "net/minecraft/client/Minecraft", false);
      int wheel = 0;
      for (MethodNode method : minecraft.methods) {
         wheel += countHook(method, MAYU, "redirectHotbarScroll");
         for (AbstractInsnNode insn : method.instructions.toArray()) {
            require(!RicePatches.isCall(insn, "net/minecraft/entity/player/InventoryPlayer", "changeCurrentItem", "(I)V"), "changeCurrentItem not redirected");
         }
      }
      require(wheel == 1, "one wheel redirect expected");
      // MixinGuiIngameForge renderTitle point: directly before the scoreboard statement.
      MethodNode overlay = patched(jar, "net/minecraft/client/gui/GuiIngame", false, "renderGameOverlay", "(F)V");
      int titles = 0;
      for (AbstractInsnNode insn : overlay.instructions.toArray()) {
         if (isHookCall(insn, "dev/rice/client/hook/GameHooks", "hudRenderTitle")) {
            AbstractInsnNode self = RicePatches.nextReal(insn);
            AbstractInsnNode scoreboard = RicePatches.nextReal(RicePatches.nextReal(RicePatches.nextReal(self)));
            require(self.getOpcode() == Opcodes.ALOAD && scoreboard instanceof MethodInsnNode
               && ((MethodInsnNode)scoreboard).name.equals(Mappings.mapMethod("net/minecraft/world/World", "getScoreboard", "()Lnet/minecraft/scoreboard/Scoreboard;")),
               "renderGameOverlay: RenderOverlayEvent must precede the scoreboard statement");
            titles++;
         }
      }
      require(titles == 1, "one RenderOverlayEvent point expected");
   }

   private static ClassNode patchedClass(JarFile jar, String target, boolean forge) throws IOException {
      for (RicePatches.ClassEdits patch : RicePatches.create(forge)) {
         if (!patch.target.equals(target)) continue;
         ClassNode node = read(jar, Mappings.mapClass(target));
         for (RicePatches.MethodEdit edit : patch.edits) edit.apply(patch.find(node, edit));
         if (patch.hasEveryMethodEdits()) patch.applyEveryMethod(node);
         return node;
      }
      throw new AssertionError("no patches for " + target);
   }

   private static MethodNode patched(JarFile jar, String target, boolean forge, String name, String desc) throws IOException {
      ClassNode node = patchedClass(jar, target, forge);
      String runtime = Mappings.mapMethod(target, name, desc);
      String runtimeDesc = Mappings.mapDescriptor(desc);
      for (MethodNode method : node.methods) if (method.name.equals(runtime) && method.desc.equals(runtimeDesc)) return method;
      throw new AssertionError("missing " + target + "." + name + desc);
   }

   private static boolean isHookCall(AbstractInsnNode insn, String owner, String name) {
      return insn instanceof MethodInsnNode && ((MethodInsnNode)insn).owner.equals(owner) && ((MethodInsnNode)insn).name.equals(name);
   }

   private static int countHook(MethodNode method, String owner, String name) {
      int count = 0;
      for (AbstractInsnNode insn : method.instructions.toArray()) if (isHookCall(insn, owner, name)) count++;
      return count;
   }

   private static AbstractInsnNode first(MethodNode method, int opcode) {
      for (AbstractInsnNode insn : method.instructions.toArray()) if (insn.getOpcode() == opcode) return insn;
      return null;
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }

   private static String superOf(String type) {
      if (supers.containsKey(type)) return supers.get(type);
      try {
         Class<?> c = Class.forName(type.replace('/', '.'), false, HookPlacementCheck.class.getClassLoader());
         return c.getSuperclass() == null ? null : c.getSuperclass().getName().replace('.', '/');
      } catch (Throwable missing) {
         return "java/lang/Object";
      }
   }

   private static boolean isInterface(String type) {
      if (interfaces.containsKey(type)) return interfaces.get(type);
      try {
         return Class.forName(type.replace('/', '.'), false, HookPlacementCheck.class.getClassLoader()).isInterface();
      } catch (Throwable missing) {
         return false;
      }
   }

   /** COMPUTE_FRAMES writer resolving supertypes from the Minecraft jar under test, like TransformerEngine's game-loader writer. */
   private static final class FrameWriter extends ClassWriter {
      FrameWriter() {
         super(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
      }

      @Override
      protected String getCommonSuperClass(String first, String second) {
         if (first.equals(second)) return first;
         if (isInterface(first) || isInterface(second)) return "java/lang/Object";
         Set<String> chain = new HashSet<>();
         for (String t = first; t != null; t = superOf(t)) chain.add(t);
         for (String t = second; t != null; t = superOf(t)) if (chain.contains(t)) return t;
         return "java/lang/Object";
      }
   }
}
