package dev.rice.client.velocity.jade;

/** OpenJade TimeoutCondition: a packet-release deadline with a minimum one-millisecond duration. */
public final class TimeoutCondition {
   private final long deadline;
   public TimeoutCondition(long now, long duration) { this.deadline = now + Math.max(1L, duration); }
   public boolean isOpen(long now) { return now >= this.deadline; }
}
