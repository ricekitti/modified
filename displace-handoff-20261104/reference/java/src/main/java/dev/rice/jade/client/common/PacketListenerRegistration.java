// Jade recovery: original class: jade.deps.eLz.oKaHYZy4
package dev.rice.jade.client.common;

import java.util.Set;

public final class PacketListenerRegistration {
   private final PacketHandlerBinding registration;

   public PacketListenerRegistration(Set<PacketDirection> var1, PacketReleaseGate var2) {
      this.registration = new PacketHandlerBinding(var1, var2);
   }

   public Set<PacketDirection> lFjiOy() {
      return this.registration.getDirections();
   }

   public PacketReleaseGate getPacketHandler() {
      return this.registration.PdeC();
   }
}
