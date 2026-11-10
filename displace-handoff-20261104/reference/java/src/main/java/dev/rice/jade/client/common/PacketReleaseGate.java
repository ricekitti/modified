// Jade recovery: original class: jade.deps.eLz.OAkSC2
package dev.rice.jade.client.common;

public abstract class PacketReleaseGate {
   private final OneWayFlag dGx = new OneWayFlag();

   protected abstract boolean shouldRelease();

   public final boolean isOpen() {
      return this.dGx.isSet() ? true : this.shouldRelease();
   }

   public final void forceOpen() {
      this.dGx.DzqT();
   }
}
