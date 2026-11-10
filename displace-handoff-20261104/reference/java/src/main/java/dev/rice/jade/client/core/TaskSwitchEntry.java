// Jade recovery: original class: jade.deps.eLz.u5NqoQk
package dev.rice.jade.client.core;

import dev.rice.jade.client.common.PacketListenerRegistration;

public final class TaskSwitchEntry extends PacketQueueEntry {
   private final PacketListenerRegistration task;

   public TaskSwitchEntry(PacketListenerRegistration var1) {
      this.task = var1;
   }

   public PacketListenerRegistration getTask() {
      return this.task;
   }

   @Override
   public PacketListenerRegistration nok9(PacketListenerRegistration var1, HandledPacketTracker var2) {
      return this.task;
   }
}
