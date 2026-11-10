package com.snowy.core.event;
import net.minecraft.entity.player.EntityPlayer;
public class TickEvent {
    public enum Phase {START,END}
    public static final class PlayerTickEvent {
        public final Phase phase;public final EntityPlayer player;
        public PlayerTickEvent(Phase phase,EntityPlayer player){this.phase=phase;this.player=player;}
    }
}
