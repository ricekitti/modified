// Jade recovery: recovered class name: JadeAgentHooks
package dev.rice.jade.agent;

import dev.rice.jade.client.Jade;
import dev.rice.jade.client.common.ClientUtils;
import dev.rice.jade.client.common.EventBus;
import dev.rice.jade.client.common.EventPhase;
import dev.rice.jade.client.common.PacketUtils;
import dev.rice.jade.client.common.RotationHandler;
import dev.rice.jade.client.common.RotationUtils;
import dev.rice.jade.client.event.ChangeCurrentItemEvent;
import dev.rice.jade.client.event.ClickMouseEvent;
import dev.rice.jade.client.event.GameLoopEvent;
import dev.rice.jade.client.event.JumpEvent;
import dev.rice.jade.client.event.LeftClickEvent;
import dev.rice.jade.client.event.MouseEvent;
import dev.rice.jade.client.event.MoveFlyingEvent;
import dev.rice.jade.client.event.MoveInputEvent;
import dev.rice.jade.client.event.MoveStateUpdateEvent;
import dev.rice.jade.client.event.PacketReceiveEvent;
import dev.rice.jade.client.event.PacketSendEvent;
import dev.rice.jade.client.event.PreUpdateEvent;
import dev.rice.jade.client.event.RenderTickEvent;
import dev.rice.jade.client.event.RenderWorldLastEvent;
import dev.rice.jade.client.event.RightClickDelayEvent;
import dev.rice.jade.client.event.RightClickEvent;
import dev.rice.jade.client.event.SetCurrentItemEvent;
import dev.rice.jade.client.event.SilentPacketSendEvent;
import dev.rice.jade.client.event.TickEndEvent;
import dev.rice.jade.client.event.TickEvent;
import dev.rice.jade.client.event.TickStartEvent;
import dev.rice.jade.client.event.UpdateWalkingPlayerEvent;
import dev.rice.jade.client.event.PostWalkingUpdateEvent;
import dev.rice.jade.client.module.combat.AutoClicker;
import dev.rice.jade.client.module.minigames.BedwarsUtils;
import dev.rice.jade.client.module.minigames.Opsec;
import dev.rice.jade.client.module.player.DelayRemover;
import dev.rice.jade.client.module.player.Freecam;
import dev.rice.jade.client.module.player.InventoryManager;



import dev.rice.jade.deps.loader107.LocalCoreLoader;



import dev.rice.jade.inject.InjectionAgent;
import dev.rice.jade.inject.RuntimeAccess;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public final class JadeAgentHooks {
   private static final Map<Object, Object> CONTAINER_DRAW_SLOT = new WeakHashMap<>();
   private static final Set<String> LOGGED_FAILURES = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static volatile boolean mixinEventBridgeActive;
   private static volatile boolean startupAttempted;
   private static volatile boolean prePlayerInteractPostedThisTick;

   private JadeAgentHooks() {
   }

      private static boolean handleLocalChatMessage(Minecraft mc, String message) {
      return false;
   }

   public static void onMoveFlying(Object entityObj, float strafe, float forward, float friction) {
      if (!mixinEventBridgeActive) {
         try {
            Entity entity = (Entity)entityObj;
            MoveFlyingEvent event = new MoveFlyingEvent(strafe, forward, friction, entity.rotationYaw);
            if (entity == minecraft().thePlayer) {
               EventBus.post(event);
            }

            strafe = event.getStrafe();
            forward = event.getForward();
            friction = event.getFriction();
            float yaw = event.osuUy7();
            float length = strafe * strafe + forward * forward;
            if (length >= 1.0E-4F) {
               length = MathHelper.sqrt_float(length);
               if (length < 1.0F) {
                  length = 1.0F;
               }

               length = friction / length;
               strafe *= length;
               forward *= length;
               float sin = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
               float cos = MathHelper.cos(yaw * (float) Math.PI / 180.0F);
               entity.motionX += strafe * cos - forward * sin;
               entity.motionZ += forward * cos + strafe * sin;
            }
         } catch (Throwable var10) {
            log("onMoveFlying", var10);
         }
      }
   }

   public static void onLivingJump(Object livingObj) {
      if (!mixinEventBridgeActive) {
         try {
            EntityLivingBase living = (EntityLivingBase)livingObj;
            Object base = invokeAny(living, new String[]{"getJumpUpwardsMotion", "func_175134_bD"});
            float motion = base instanceof Number ? ((Number)base).floatValue() : 0.42F;
            JumpEvent event = new JumpEvent(living, motion, living.rotationYaw, living.isSprinting());
            EventBus.post(event);
            if (event.isCanceled()) {
               return;
            }

            living.motionY = event.MCqU();
            if (living.isPotionActive(Potion.jump)) {
               living.motionY = living.motionY + (living.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
            }

            if (event.isSprinting()) {
               float radians = event.glMe() * (float) (Math.PI / 180.0);
               living.motionX = living.motionX - MathHelper.sin(radians) * 0.2F;
               living.motionZ = living.motionZ + MathHelper.cos(radians) * 0.2F;
            }

            living.isAirBorne = true;
         } catch (Throwable var6) {
            log("onLivingJump", var6);
         }
      }
   }

   public static void onContainerDrawSlotBegin(Object containerObj, Object slotObj) {
      if (!mixinEventBridgeActive) {
         synchronized (CONTAINER_DRAW_SLOT) {
            CONTAINER_DRAW_SLOT.put(containerObj, slotObj);
         }
      }
   }

   public static void onContainerDrawSlotEnd(Object containerObj) {
      synchronized (CONTAINER_DRAW_SLOT) {
         CONTAINER_DRAW_SLOT.remove(containerObj);
      }
   }

   public static Object onContainerDisplayedStack(Object containerObj, Object originalObj) {
      if (!mixinEventBridgeActive && Jade.getModuleManager().getModule(Opsec.class) != null) {
         try {
            Object slot;
            synchronized (CONTAINER_DRAW_SLOT) {
               slot = CONTAINER_DRAW_SLOT.get(containerObj);
            }

            return Jade.getModuleManager().getModule(Opsec.class).Bxn6((Slot)slot, (ItemStack)originalObj);
         } catch (Throwable var6) {
            log("onContainerDisplayedStack", var6);
            return originalObj;
         }
      } else {
         return originalObj;
      }
   }

   public static Object onContainerTooltipStack(Object containerObj, Object originalObj) {
      if (!mixinEventBridgeActive && Jade.getModuleManager().getModule(Opsec.class) != null) {
         try {
            Object rawSlot = getField(containerObj, new String[]{"theSlot", "field_147006_u"});
            if (rawSlot != null && !(rawSlot instanceof Slot)) {
               InjectionAgent.recordRuntimeSignal(
                  "container",
                  "tooltip_slot_type_mismatch"
               );
               return originalObj;
            } else if (originalObj != null && !(originalObj instanceof ItemStack)) {
               InjectionAgent.recordRuntimeSignal(
                  "container",
                  "tooltip_stack_type_mismatch"
               );
               return originalObj;
            } else {
               return Jade.getModuleManager().getModule(Opsec.class).Bxn6((Slot)rawSlot, (ItemStack)originalObj);
            }
         } catch (Throwable var3) {
            log("onContainerTooltipStack", var3);
            return originalObj;
         }
      } else {
         return originalObj;
      }
   }

   public static void onContainerDrawScreen(Object containerObj, int mouseX, int mouseY) {
      if (!mixinEventBridgeActive) {
         if (!(containerObj instanceof GuiContainer)) {
            InjectionAgent.recordRuntimeSignal(
               "container",
               "screen_type_mismatch"
            );
         } else {
            AutoClicker autoClicker = Jade.getModuleManager().getModule(AutoClicker.class);
            if (autoClicker != null && autoClicker.isEnabled() && autoClicker.whilstShiftingInInventory.isToggled()) {
               try {
                  autoClicker.setInventoryTarget((GuiContainer)containerObj, resolveHoveredContainerSlot((GuiContainer)containerObj, mouseX, mouseY));
               } catch (Throwable var6) {
                  log("onContainerDrawScreen.autoClicker", var6);
               }
            }

            if (Jade.getModuleManager().getModule(Opsec.class) != null) {
               try {
                  Jade.getModuleManager().getModule(Opsec.class).drawQuickBuySetupText();
               } catch (Throwable var5) {
                  log("onContainerDrawScreen.opsec", var5);
               }
            }
         }
      }
   }

   private static Slot resolveHoveredContainerSlot(GuiContainer container, int mouseX, int mouseY) {
      try {
         Method isMouseOverSlot = findMethod(container.getClass(), new String[]{"isMouseOverSlot", "func_146981_a"}, new Class[]{Slot.class, int.class, int.class});
         if (isMouseOverSlot != null) {
            if (container.inventorySlots != null && container.inventorySlots.inventorySlots != null) {
               for (Object candidate : container.inventorySlots.inventorySlots) {
                  if (candidate instanceof Slot) {
                     Object hovered = isMouseOverSlot.invoke(container, candidate, mouseX, mouseY);
                     if (Boolean.TRUE.equals(hovered)) {
                        return (Slot)candidate;
                     }
                  }
               }

               return null;
            }

            return null;
         }
      } catch (Throwable var7) {
         log("onContainerDrawScreen.hoveredSlot", var7);
      }

      try {
         Object rawSlot = getField(container, new String[]{"theSlot", "field_147006_u"});
         if (rawSlot == null || rawSlot instanceof Slot) {
            return (Slot)rawSlot;
         }

         InjectionAgent.recordRuntimeSignal(
            "container",
            "screen_slot_type_mismatch"
         );
      } catch (Throwable var8) {
         log("onContainerDrawScreen.slotFallback", var8);
      }

      return null;
   }

   public static boolean onContainerManualInput(Object ignored) {
      return mixinEventBridgeActive
         ? false
         : Jade.getModuleManager().getModule(InventoryManager.class) != null && Jade.getModuleManager().getModule(InventoryManager.class).isExecutingPlan()
            || Jade.getModuleManager().getModule(Opsec.class) != null && Jade.getModuleManager().getModule(Opsec.class).isQuickBuySetupActive();
   }

   public static boolean onContainerClick(Object containerObj, Object slotObj, int slotId, int clickedButton, int clickType) {
      if (mixinEventBridgeActive) {
         return false;
      } else {
         try {
            if (onContainerManualInput(containerObj)) {
               return true;
            } else {
               Slot slot = (Slot)slotObj;
               return Jade.getModuleManager().getModule(Opsec.class) != null
                     && Jade.getModuleManager().getModule(Opsec.class).axClaj(slot, slotId, clickedButton, clickType)
                  ? true
                  : Jade.getModuleManager().getModule(BedwarsUtils.class) != null && Jade.getModuleManager().getModule(BedwarsUtils.class).handleQuickShopSlotClick(slot, slotId, clickedButton, clickType);
            }
         } catch (Throwable var6) {
            log("onContainerClick", var6);
            return false;
         }
      }
   }

   public static void ensureJadeStarted() {
      if (!startupAttempted) {
         startupAttempted = true;

         try {
            InjectionAgent.reportDiagnostic(
               "client-thread Jade initialization started"
            );
            ClassLoader loader = JadeAgentHooks.class.getClassLoader();
            LocalCoreLoader.bootLocalCore(loader);
            InjectionAgent.markReady();
            InjectionAgent.reportDiagnostic(
               "client-thread Jade initialization completed"
            );
            log(
               "startup",
               "Core initialized"
            );
         } catch (Throwable var1) {
            InjectionAgent.markFailed(
               "Jade.init failed: " + InjectionAgent.describeFailure(var1)
            );
            log("onStartGame", var1);
         }
      }
   }

   public static void onRunTickStart() {
      if (!mixinEventBridgeActive) {
         try {
            prePlayerInteractPostedThisTick = false;
            ensureJadeStarted();
            EventBus.post(new TickStartEvent());
            EventBus.post(new TickEndEvent(EventPhase.START));
            RotationHandler.getInstance().updateTargetRotation();
            EventBus.post(new TickEvent());
            EventBus.post(new RightClickDelayEvent());
         } catch (Throwable var1) {
            log("onRunTickStart", var1);
         }
      }
   }

   public static void onRunTickEnd() {
      if (!mixinEventBridgeActive) {
         try {
            EventBus.post(new TickEndEvent(EventPhase.END));
            DelayRemover delayRemover = Jade.getModuleManager().getModule(DelayRemover.class);
            Minecraft mc = minecraft();
            if (delayRemover != null && delayRemover.isEnabled() && mc != null && mc.inGameHasFocus && minecraftPlayer(mc) != null && delayRemover.legacyHitreg.isToggled()) {
               setIntField(mc, 0, new String[]{"leftClickCounter", "field_71429_W"});
               InjectionAgent.recordRuntimeSignal(
                  "mutation",
                  "DelayRemover.leftClickCounter.write"
               );
               if (getIntField(mc, new String[]{"leftClickCounter", "field_71429_W"}) == 0) {
                  InjectionAgent.recordRuntimeSignal(
                     "mutation",
                     "DelayRemover.leftClickCounter.verified"
                  );
               }
            }
         } catch (Throwable var2) {
            log("onRunTickEnd", var2);
         }
      }
   }

   public static void onChangeCurrentItem(Object inventoryObj, int direction) {
      try {
         InventoryPlayer inventory = (InventoryPlayer)inventoryObj;
         if (!mixinEventBridgeActive) {
            ChangeCurrentItemEvent event = new ChangeCurrentItemEvent(direction, inventory.currentItem);
            EventBus.post(event);
            if (event.isCanceled()) {
               return;
            }
         }

         inventory.changeCurrentItem(direction);
      } catch (Throwable var4) {
         log("onChangeCurrentItem", var4);
      }
   }

   public static void onSetCurrentItem(Object inventoryObj, int slot) {
      try {
         InventoryPlayer inventory = (InventoryPlayer)inventoryObj;
         if (!mixinEventBridgeActive) {
            SetCurrentItemEvent event = new SetCurrentItemEvent(slot);
            EventBus.post(event);
            if (event.isCanceled()) {
               return;
            }
         }

         inventory.currentItem = slot;
      } catch (Throwable var4) {
         log("onSetCurrentItem", var4);
      }
   }

   public static void onRunGameLoopStart(Object minecraft) {
      if (!mixinEventBridgeActive) {
         try {
            ensureJadeStarted();
            Jade.refreshTimerModuleKeybind();
            Jade.openPendingScreen();
            EventBus.post(new GameLoopEvent());
            Object timer = getField(minecraft, new String[]{"timer", "field_71428_T"});
            float partialTicks = timer == null ? 0.0F : getFloatField(timer, new String[]{"renderPartialTicks", "field_74281_c"});
            EventBus.post(new RenderTickEvent(EventPhase.START, partialTicks));
         } catch (Throwable var3) {
            log("onRunGameLoopStart", var3);
         }
      }
   }

   public static boolean onClickMouse(Object minecraft) {
      if (mixinEventBridgeActive) {
         return false;
      } else {
         try {
            Minecraft mc = (Minecraft)minecraft;
            DelayRemover delayRemover = Jade.getModuleManager().getModule(DelayRemover.class);
            if (delayRemover != null && delayRemover.isEnabled() && delayRemover.legacyHitreg.isToggled()) {
               setIntField(mc, 0, new String[]{"leftClickCounter", "field_71429_W"});
               InjectionAgent.recordRuntimeSignal(
                  "mutation",
                  "DelayRemover.leftClickCounter.at_click"
               );
            }

            MovingObjectPosition mop = (MovingObjectPosition)getField(mc, new String[]{"objectMouseOver", "field_71476_x"});
            MouseEvent mouseEvent = new MouseEvent(0, true, Mouse.getX(), Mouse.getY(), 0, 0, 0);
            EventBus.post(mouseEvent);
            if (mouseEvent.isCanceled()) {
               return true;
            }

            ClickMouseEvent preAttack = new ClickMouseEvent(mop);
            EventBus.post(preAttack);
            if (preAttack.isCanceled()) {
               return true;
            }

            EventBus.post(new LeftClickEvent());
         } catch (Throwable var6) {
            log("onClickMouse", var6);
         }

         return false;
      }
   }

   public static boolean onRightClickMouse() {
      if (mixinEventBridgeActive) {
         return false;
      } else {
         try {
            if (Mouse.isButtonDown(1)) {
               MouseEvent mouseEvent = new MouseEvent(1, true, Mouse.getX(), Mouse.getY(), 0, 0, 0);
               EventBus.post(mouseEvent);
               if (mouseEvent.isCanceled()) {
                  return true;
               }
            }

            RightClickEvent event = new RightClickEvent();
            EventBus.post(event);
            return event.isCanceled();
         } catch (Throwable var1) {
            log("onRightClickMouse", var1);
            return false;
         }
      }
   }

   public static void onUpdatePlayerMoveState(Object inputObj) {
      if (!mixinEventBridgeActive && inputObj instanceof MovementInput) {
         try {
            MovementInput input = (MovementInput)inputObj;
            float forward = input.moveForward;
            float strafe = input.moveStrafe;
            if (input.sneak) {
               forward /= 0.3F;
               strafe /= 0.3F;
            }

            MoveInputEvent event = new MoveInputEvent(forward, strafe, input.jump, input.sneak, 0.3);
            EventBus.post(event);
            double sneakMultiplier = event.BFAN();
            input.moveForward = event.getMoveForward();
            input.moveStrafe = event.AfWugH();
            input.jump = event.isJumping();
            input.sneak = event.isSneaking();
            if (input.sneak) {
               input.moveStrafe = (float)(input.moveStrafe * sneakMultiplier);
               input.moveForward = (float)(input.moveForward * sneakMultiplier);
            }

            EventBus.post(new MoveStateUpdateEvent());
         } catch (Throwable var7) {
            log("onUpdatePlayerMoveState", var7);
         }
      }
   }

   public static boolean onNetworkSend(Object packetObj) {
      if (mixinEventBridgeActive) {
         return false;
      } else {
         try {
            Packet packet = (Packet)packetObj;
            if (InjectionAgent.isBadlionRuntime()
               && packet instanceof C01PacketChatMessage
               && handleLocalChatMessage(minecraft(), ((C01PacketChatMessage)packet).getMessage())) {
               InjectionAgent.recordRuntimeSignal(
                  "chat",
                  "outgoing.local_handled_at_packet"
               );
               return true;
            } else if (PacketUtils.consumeSilentlySent(packet)) {
               EventBus.post(new SilentPacketSendEvent(packet));
               return false;
            } else {
               PacketSendEvent event = new PacketSendEvent(packet);
               EventBus.post(event);
               return event.isCanceled();
            }
         } catch (Throwable var3) {
            log("onNetworkSend", var3);
            return false;
         }
      }
   }

   public static boolean onNetworkReceive(Object packetObj) {
      if (mixinEventBridgeActive) {
         return false;
      } else {
         try {
            Packet packet = (Packet)packetObj;
            if (PacketUtils.NQazztK(packet)) {
               return false;
            } else {
               PacketReceiveEvent event = new PacketReceiveEvent(packet);
               EventBus.post(event);
               return event.isCanceled();
            }
         } catch (Throwable var3) {
            log("onNetworkReceive", var3);
            return false;
         }
      }
   }

   public static void onPlayerUpdateHead(Object playerObj) {
      if (!mixinEventBridgeActive) {
         try {
            EntityPlayerSP player = (EntityPlayerSP)playerObj;
            if (ClientUtils.isInNestedUpdate()) {
               return;
            }

            if (player.worldObj.isBlockLoaded(new BlockPos(player.posX, 0.0, player.posZ))) {
               RotationUtils.previousPitch = RotationUtils.pN0;
               RotationUtils.IuZ = RotationUtils.outgoingYaw;
               EventBus.post(new PreUpdateEvent());
            }
         } catch (Throwable var2) {
            log("onPlayerUpdateHead", var2);
         }
      }
   }

   public static boolean onUpdateWalkingPlayer(Object playerObj) {
      if (mixinEventBridgeActive) {
         return false;
      } else {
         try {
            EntityPlayerSP player = (EntityPlayerSP)playerObj;
            UpdateWalkingPlayerEvent.QhtJiq = false;
            UpdateWalkingPlayerEvent.setYawOverrideRequested(false);
            RotationUtils.hasPendingRotationOverride = false;
            UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(
               player.posX,
               player.getEntityBoundingBox().minY,
               player.posZ,
               player.rotationYaw,
               player.rotationPitch,
               player.onGround,
               player.isSprinting(),
               player.isSneaking()
            );
            EventBus.post(event);
            RotationUtils.lastSentRotation = new float[]{event.getYaw(), event.getPitch()};
            boolean sprinting = event.isSprinting();
            boolean serverSprint = getBooleanField(player, new String[]{"serverSprintState", "field_175171_bO"});
            if (sprinting != serverSprint) {
               player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, sprinting ? Action.START_SPRINTING : Action.STOP_SPRINTING));
               setBooleanField(player, sprinting, new String[]{"serverSprintState", "field_175171_bO"});
            }

            boolean sneaking = event.isSneaking();
            boolean serverSneak = getBooleanField(player, new String[]{"serverSneakState", "field_175170_bN"});
            if (sneaking != serverSneak) {
               player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, sneaking ? Action.START_SNEAKING : Action.STOP_SNEAKING));
               setBooleanField(player, sneaking, new String[]{"serverSneakState", "field_175170_bN"});
            }

            if (invokeBoolean(player, new String[]{"isCurrentViewEntity", "func_175160_A"}) || Freecam.pqos()) {
               if (UpdateWalkingPlayerEvent.isYawOverrideRequested()) {
                  RotationUtils.applyRenderedYaw(event.getYaw());
               }

               RotationUtils.pN0 = event.getPitch();
               RotationUtils.outgoingYaw = event.getYaw();
               if (RotationUtils.hasPendingRotationOverride) {
                  RotationUtils.pN0 = RotationUtils.gib[1];
                  RotationUtils.outgoingYaw = RotationUtils.gib[0];
                  RotationUtils.applyRenderedYaw(RotationUtils.outgoingYaw);
               }

               RotationUtils.hasPendingRotationOverride = false;
               double lastX = getDoubleField(player, new String[]{"lastReportedPosX", "field_175172_bI"});
               double lastY = getDoubleField(player, new String[]{"lastReportedPosY", "field_175166_bJ"});
               double lastZ = getDoubleField(player, new String[]{"lastReportedPosZ", "field_175167_bK"});
               float lastYaw = getFloatField(player, new String[]{"lastReportedYaw", "field_175164_bL"});
               float lastPitch = getFloatField(player, new String[]{"lastReportedPitch", "field_175165_bM"});
               int updateTicks = getIntField(player, new String[]{"positionUpdateTicks", "field_175168_bP"});
               double dx = event.getX() - lastX;
               double dy = event.getY() - lastY;
               double dz = event.getZ() - lastZ;
               double yawDelta = event.getYaw() - lastYaw;
               double pitchDelta = event.getPitch() - lastPitch;
               boolean moved = dx * dx + dy * dy + dz * dz > 9.0E-4 || updateTicks >= 20;
               boolean rotated = yawDelta != 0.0 || pitchDelta != 0.0;
               if (player.ridingEntity == null) {
                  if (moved && rotated) {
                     player.sendQueue
                        .addToSendQueue(
                           new C06PacketPlayerPosLook(event.getX(), event.getY(), event.getZ(), event.getYaw(), event.getPitch(), event.isOnGround())
                        );
                  } else if (moved) {
                     player.sendQueue.addToSendQueue(new C04PacketPlayerPosition(event.getX(), event.getY(), event.getZ(), event.isOnGround()));
                  } else if (rotated) {
                     player.sendQueue.addToSendQueue(new C05PacketPlayerLook(event.getYaw(), event.getPitch(), event.isOnGround()));
                  } else {
                     player.sendQueue.addToSendQueue(new C03PacketPlayer(event.isOnGround()));
                  }
               } else {
                  player.sendQueue
                     .addToSendQueue(new C06PacketPlayerPosLook(player.motionX, -999.0, player.motionZ, event.getYaw(), event.getPitch(), event.isOnGround()));
                  moved = false;
               }

               setIntField(player, updateTicks + 1, new String[]{"positionUpdateTicks", "field_175168_bP"});
               if (moved) {
                  setDoubleField(player, event.getX(), new String[]{"lastReportedPosX", "field_175172_bI"});
                  setDoubleField(player, event.getY(), new String[]{"lastReportedPosY", "field_175166_bJ"});
                  setDoubleField(player, event.getZ(), new String[]{"lastReportedPosZ", "field_175167_bK"});
                  setIntField(player, 0, new String[]{"positionUpdateTicks", "field_175168_bP"});
               }

               if (rotated) {
                  setFloatField(player, event.getYaw(), new String[]{"lastReportedYaw", "field_175164_bL"});
                  setFloatField(player, event.getPitch(), new String[]{"lastReportedPitch", "field_175165_bM"});
               }
            }

            EventBus.post(new PostWalkingUpdateEvent());
            return true;
         } catch (Throwable var28) {
            log("onUpdateWalkingPlayer", var28);
            return false;
         }
      }
   }

   public static void onRenderWorldLast(float partialTicks) {
      if (!mixinEventBridgeActive && EventBus.hasListeners(RenderWorldLastEvent.class)) {
         try {
            EventBus.post(new RenderWorldLastEvent(partialTicks));
         } catch (Throwable var2) {
            log("onRenderWorldLast", var2);
         }
      }
   }

   public static void onRenderWorldLast(float partialTicks, boolean isolatedCamera) {
      if (EventBus.hasListeners(RenderWorldLastEvent.class)) {
         if (!isolatedCamera) {
            onRenderWorldLast(partialTicks);
         } else if (!mixinEventBridgeActive) {
            int previousMatrixMode = 5888;
            boolean projectionPushed = false;
            boolean modelViewPushed = false;

            try {
               Minecraft mc = minecraft();
               Object renderer = mc == null ? null : mc.entityRenderer;
               if (renderer != null && mc.displayWidth > 0 && mc.displayHeight > 0) {
                  previousMatrixMode = GL11.glGetInteger(2976);
                  GlStateManager.matrixMode(5889);
                  GL11.glPushMatrix();
                  projectionPushed = true;
                  GlStateManager.matrixMode(5888);
                  GL11.glPushMatrix();
                  modelViewPushed = true;
                  Method setupCamera = findMethod(renderer.getClass(), new String[]{"setupCameraTransform", "func_78479_a"}, new Class[]{float.class, int.class});
                  if (setupCamera == null) {
                     throw new NoSuchMethodException(
                        "EntityRenderer.setupCameraTransform(float,int)"
                     );
                  }

                  try {
                     setupCamera.invoke(renderer, partialTicks, 0);
                  } catch (Throwable var13) {
                     log("onRenderWorldLast camera", var13);
                  }

                  GlStateManager.enableDepth();
                  EventBus.post(new RenderWorldLastEvent(partialTicks));
                  return;
               }

               EventBus.post(new RenderWorldLastEvent(partialTicks));
            } catch (Throwable var14) {
               log("onRenderWorldLast", var14);
               return;
            } finally {
               if (modelViewPushed) {
                  GlStateManager.matrixMode(5888);
                  GL11.glPopMatrix();
               }

               if (projectionPushed) {
                  GlStateManager.matrixMode(5889);
                  GL11.glPopMatrix();
               }

               GlStateManager.matrixMode(previousMatrixMode);
            }
         }
      }
   }

   private static Minecraft minecraft() {
      try {
         Object mc = invokeStatic(Minecraft.class, new String[]{"getMinecraft", "func_71410_x"});
         return mc instanceof Minecraft ? (Minecraft)mc : null;
      } catch (Throwable var1) {
         return null;
      }
   }

   private static EntityPlayerSP minecraftPlayer(Minecraft mc) {
      if (mc == null) {
         return null;
      } else {
         try {
            Object player = getField(mc, new String[]{"thePlayer", "field_71439_g"});
            return player instanceof EntityPlayerSP ? (EntityPlayerSP)player : null;
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   private static Object getField(Object owner, String... names) throws Exception {
      Field field = findField(owner.getClass(), names);
      return field == null ? null : field.get(owner);
   }

   private static float getFloatField(Object owner, String... names) throws Exception {
      Object value = getField(owner, names);
      return value instanceof Number ? ((Number)value).floatValue() : 0.0F;
   }

   private static double getDoubleField(Object owner, String... names) throws Exception {
      Object value = getField(owner, names);
      return value instanceof Number ? ((Number)value).doubleValue() : 0.0;
   }

   private static int getIntField(Object owner, String... names) throws Exception {
      Object value = getField(owner, names);
      return value instanceof Number ? ((Number)value).intValue() : 0;
   }

   private static boolean getBooleanField(Object owner, String... names) throws Exception {
      Object value = getField(owner, names);
      return value instanceof Boolean && (Boolean)value;
   }

   private static void setFloatField(Object owner, float value, String... names) throws Exception {
      Field field = findField(owner.getClass(), names);
      if (field != null) {
         field.setFloat(owner, value);
      }
   }

   private static void setDoubleField(Object owner, double value, String... names) throws Exception {
      Field field = findField(owner.getClass(), names);
      if (field != null) {
         field.setDouble(owner, value);
      }
   }

   private static void setBooleanField(Object owner, boolean value, String... names) throws Exception {
      Field field = findField(owner.getClass(), names);
      if (field != null) {
         field.setBoolean(owner, value);
      }
   }

   private static void setIntField(Object owner, int value, String... names) throws Exception {
      Field field = findField(owner.getClass(), names);
      if (field != null) {
         field.setInt(owner, value);
      }
   }

   private static Field findField(Class<?> type, String... names) {
      return RuntimeAccess.resolveMappedField(type, names);
   }

   private static Object invokeAny(Object owner, String... names) throws Exception {
      return invokeMethod(owner, names, new Class[0]);
   }

   private static boolean invokeBoolean(Object owner, String... names) throws Exception {
      Object value = invokeMethod(owner, names, new Class[0]);
      return value instanceof Boolean && (Boolean)value;
   }

   private static Object invokeStatic(Class<?> owner, String... names) throws Exception {
      Method method = findMethod(owner, names, new Class[0]);
      return method == null ? null : method.invoke(null);
   }

   private static Object invokeMethod(Object owner, String[] names, Class<?>[] types, Object... args) throws Exception {
      Method method = findMethod(owner.getClass(), names, types);
      return method == null ? null : method.invoke(owner, args);
   }

   private static Method findMethod(Class<?> type, String[] names, Class<?>[] types) {
      return RuntimeAccess.resolveMappedMethod(type, types, names);
   }

   private static void log(String where, Throwable t) {
      try {
         String key = where
            + ":"
            + t.getClass().getName()
            + ":"
            + t.getMessage();
         if (!LOGGED_FAILURES.add(key)) {
            return;
         }

         InjectionAgent.recordRuntimeFailure("hook.internal", where, t);
         InjectionAgent.reportDiagnostic(where + " failed: " + t);
      } catch (Throwable var3) {
      }
   }

   private static void log(String where, String message) {
      try {
         InjectionAgent.reportDiagnostic(where + ": " + message);
      } catch (Throwable var3) {
      }
   }
}
