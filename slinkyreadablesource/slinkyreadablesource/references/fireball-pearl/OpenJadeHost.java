package dev.rice.client.openjade;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;

public final class OpenJadeHost {
   private static ModuleManager modules;
   private static ClientContext context;

   private OpenJadeHost() {}
   public static void bind(ModuleManager manager, ClientContext current) { modules=manager; context=current; }
   public static RotationEvent rotation(int sourcePriority) {
      if (context == null || context.player() == null) return null;
      return new RotationEvent(context.player().rotationYaw, context.player().rotationPitch);
   }
   public static boolean publish(RotationEvent event, int sourcePriority) {
      if (event == null || event.Jxhy() == 0 || context == null || context.player() == null) return false;
      float cameraYaw=context.player().rotationYaw;
      float cameraPitch=context.player().rotationPitch;
      RotationService.retain(context,false,cameraYaw,cameraPitch);
      context.player().rotationYaw=event.MGzP2;
      context.player().rotationPitch=event.pitch;
      // Commit the agreed rotation to the shared "last sent" state THIS tick, before the
      // scaffold's LOWEST-priority placement handlers (ePgq3 / RotationHandler.getTargetYaw)
      // read it. Previously it was only updated when the C03 packet was observed on send
      // (next event), so placement ray-traced against the prior tick's rotation and the
      // server rejected the block (ghost placement -> fall-through). Mirrors OpenJade's
      // RotationHandler writing the agreed rotation before placement runs.
      RotationUtils.lastSentRotation[0]=event.MGzP2;
      RotationUtils.lastSentRotation[1]=event.pitch;
      return true;
   }
   public static void finalRotation(SemanticEvent event, boolean published) {
      if (published) { event.active=true;event.overrideRotation=false;event.remapMovement=true; }
   }
   public static void syncDisable(OpenJadeModule backend, Module owner) {
      if (backend.consumeDisableRequest() && modules != null) modules.setEnabled(owner,false,true);
   }
   public static void resetWorld() { OpenJadeRuntime.resetWorld(); }
   public static void observePacket(Packet<?> packet, boolean outbound) { OpenJadeRuntime.observePacket(packet,outbound); }
   public static void tickEnd() { OpenJadeRuntime.tickEnd(); }
}

final class OpenJadeRuntime {
   private static BlockPos spawn;
   private static boolean captureSpawn;
   private static boolean respawnPending;
   private static long deathTimeMillis;
   static int mapRotationPriority(int source){return source>=60?800:source>=50?700:600;}
   static boolean timerModuleEnabled(){Module m=find("timer");return m!=null&&m.isEnabled();}
   static boolean bridgeAssistHudEnabled(){Module m=find("bridge_assist");return m!=null&&m.isEnabled();}
   static BlockPos spawnBlockPos(){return spawn;}
   static void captureSpawn(){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer!=null)spawn=mc.thePlayer.getPosition();}
   static void observePacket(Packet<?> packet,boolean outbound){
      if(outbound&&packet instanceof C03PacketPlayer){C03PacketPlayer movement=(C03PacketPlayer)packet;if(movement.getRotating()){RotationUtils.lastSentRotation[0]=movement.getYaw();RotationUtils.lastSentRotation[1]=movement.getPitch();}}
      if(!outbound&&packet instanceof S02PacketChat){String value=StringUtils.stripControlCodes(((S02PacketChat)packet).getChatComponent().getUnformattedText());if(value.startsWith(" ")&&value.contains("Protect your bed and destroy the enemy beds.")){captureSpawn=true;respawnPending=false;}else if(value.equals("You will respawn because you still have a bed!")){respawnPending=true;deathTimeMillis=System.currentTimeMillis();}else if(value.equals("You have respawned!")&&respawnPending&&System.currentTimeMillis()-deathTimeMillis<=12000L){captureSpawn=true;respawnPending=false;}}
   }
   static void tickEnd(){Minecraft mc=Minecraft.getMinecraft();if(captureSpawn&&mc.thePlayer!=null&&bedWarsBoardType()==2){spawn=mc.thePlayer.getPosition();captureSpawn=false;}}
   static void resetWorld(){spawn=null;captureSpawn=false;respawnPending=false;deathTimeMillis=0L;}
   static int bedWarsBoardType(){
      Minecraft mc=Minecraft.getMinecraft();if(mc.theWorld==null)return 0;
      try {Scoreboard board=mc.theWorld.getScoreboard();ScoreObjective objective=board.getObjectiveInDisplaySlot(1);if(objective==null)return 0;StringBuilder text=new StringBuilder(objective.getDisplayName());for(Score score:board.getSortedScores(objective))text.append(' ').append(score.getPlayerName());String value=net.minecraft.util.StringUtils.stripControlCodes(text.toString()).toLowerCase();return value.contains("bed wars")||value.contains("bedwars")?2:0;} catch(Throwable ignored){return 0;}
   }
   private static Module find(String id){try{java.lang.reflect.Field f=OpenJadeHost.class.getDeclaredField("modules");f.setAccessible(true);ModuleManager m=(ModuleManager)f.get(null);return m==null?null:m.find(id);}catch(Throwable ignored){return null;}}
}
