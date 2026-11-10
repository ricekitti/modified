// Jade recovery: original class: jade.deps.eLz.FOQzp4QRM
package dev.rice.jade.client.common;

import dev.rice.jade.client.Jade;
import net.minecraft.network.Packet;

public final class PacketUtils implements IMinecraft {
   private static final PacketExclusions packetExclusions = new PacketExclusions();

   public static boolean consumeSilentlySent(Packet<?> var0) {
      return packetExclusions.consumeOutboundPacket(var0);
   }

   public static boolean NQazztK(Packet<?> var0) {
      return packetExclusions.consumeInboundPacket(var0);
   }

   public static void sendSilently(Packet var0) {
      if (isOutboundPacket(var0)) {
         packetExclusions.excludeOutboundPacket(var0);
         Jade.mc.thePlayer.sendQueue.addToSendQueue(var0);
      }
   }

   private static boolean isOutboundPacket(Packet var0) {
      return var0 != null && !var0.getClass().getSimpleName().startsWith("S");
   }
}
