package dev.rice.client;

import dev.rice.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

public final class SemanticEvent {
   /** Runs after vanilla refreshes local movement input, before it is consumed. */
   public static final int MOVEMENT_INPUT = 27;
   /** Once per tick before vanilla consumes queued interaction key presses. */
   public static final int PRE_PLAYER_INTERACT = 28;
   /** After vanilla (or a custom ray provider) has finished mouse-over selection. */
   public static final int POST_MOUSE_OVER = 29;
   /** bS server rotation update, once in runTick before mouse selection. */
   public static final int PRE_MOUSE_OVER = 30;
   /** Cancellable controller attack, including attacks issued outside clickMouse. */
   public static final int PRE_ENTITY_ATTACK = 31;
   public final int type;
   public boolean cancelled;
   public boolean active = true;
   public boolean enabled;
   public boolean jump;
   public boolean sneaking;
   public boolean overrideRotation;
   public boolean remapMovement;
   public boolean reducing;
   public boolean queryOverride;
   public float sideways;
   public float forward;
   public float factor;
   public float scale = 1.0F;
   public float yaw;
   public float pitch;
   public float timerRenderPartialTicks;
   public double elapsedSeconds;
   public double distance;
   public double expand;
   public Entity entity;
   public Packet<?> packet;
   public NetworkManager receiver;
   public int dispatchMode;
   public long packetAgeMillis;
   public int packetSequence;
   public Module module;
   public double cameraX;
   public double cameraY;
   public double cameraZ;
   public float renderWidth;
   public float renderHeight;

   public SemanticEvent(int type) {
      this.type = type;
   }
}
