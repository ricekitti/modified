package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.SemanticEvent;
import dev.rice.client.StatusText;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

public final strictfp class TimerModule extends EvidenceModule {
   // Last speed computed on the game thread; the worker re-asserts it off-thread.
   private volatile float applied = 1.0F;
   private Timer worker;

   public TimerModule() {
      super("timer", "Timer", ModuleCategory.UTILITY);
   }

   @Override
   public strictfp boolean listens(int type) {
      return type == 1 || type == 18;
   }

   public static strictfp float select(
      float speed,
      boolean requireMouse,
      boolean pressed,
      boolean requireFalling,
      boolean onGround,
      double motionY,
      boolean requireDamage,
      boolean ready,
      int hurtTime
   ) {
      if (requireMouse && !pressed) {
         return 1.0F;
      } else if (!requireFalling || !onGround && !(motionY >= 0.0)) {
         return !requireDamage || ready && hurtTime != 0 ? speed : 1.0F;
      } else {
         return 1.0F;
      }
   }

   @Override
   public strictfp void onEvent(ClientContext context, SemanticEvent event) {
      if (!context.ready()) {
         this.reset(context);
         return;
      }
      boolean ready = context.ready();
      boolean pressed = !this.flag("require_mouse")
         || Mouse.isButtonDown(context.minecraft().gameSettings.keyBindAttack.getKeyCode() == -99 ? 1 : 0);
      float selected = select(
         this.number("speed"),
         this.flag("require_mouse"),
         pressed,
         this.flag("require_falling"),
         !ready || context.player().onGround,
         ready ? context.player().motionY : 0.0,
         this.flag("require_damage"),
         context.inputReady(),
         ready ? context.player().hurtTime : 0
      );
      this.applied = selected;
      MinecraftFields.timer(context.minecraft()).timerSpeed = selected;
   }

   // OpenB4-style off-thread re-assert: Lunar/OptiFine can reset timerSpeed inside their
   // own render loop, so a background thread that re-writes it every few ms wins the race
   // for the value the game reads in updateTimer. It only mirrors the game-thread value.
   @Override
   protected strictfp void onEnable(ClientContext context) {
      this.applied = 1.0F;
      this.stopWorker();
      this.worker = new Timer("rice-timer", true);
      this.worker.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            try {
               Minecraft mc = Minecraft.getMinecraft();
               if (mc != null && mc.theWorld != null && mc.thePlayer != null && TimerModule.this.isEnabled()) {
                  MinecraftFields.timer(mc).timerSpeed = TimerModule.this.applied;
               }
            } catch (Throwable ignored) {
            }
         }
      }, 0L, 10L);
   }

   @Override
   protected strictfp void onDisable(ClientContext context) {
      this.stopWorker();
      this.reset(context);
   }

   @Override
   public strictfp void onWorldUnload(ClientContext context) {
      this.reset(context);
   }

   private strictfp void stopWorker() {
      if (this.worker != null) {
         this.worker.cancel();
         this.worker.purge();
         this.worker = null;
      }
   }

   private strictfp void reset(ClientContext context) {
      this.applied = 1.0F;
      if (context.minecraft() != null) {
         MinecraftFields.timer(context.minecraft()).timerSpeed = 1.0F;
      }
   }

   @Override
   public strictfp String statusText() {
      return !this.findSetting("speed").isConfigured() ? null : StatusText.number(this.getName(), (double)this.number("speed"));
   }
}
