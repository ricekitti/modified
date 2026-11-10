package dev.rice.client.mayu;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import dev.rice.inject.ReflectionHelper;
import dev.rice.client.combat.openb4.OpenB4KillAuraBackend;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

/** Keeps vanilla's predicted block/inventory consistent when Rice rejects Mayu's placement packet. */
public final class MayuPlacement {
   private static final ThreadLocal<Attempt> CURRENT = new ThreadLocal<>();
   private static final Field AURA_HELD = ReflectionHelper.findField(OpenB4KillAuraBackend.class, "held");

   private MayuPlacement() {}

   public static boolean place(PlayerControllerMP controller, EntityPlayerSP player, WorldClient world,
         ItemStack stack, BlockPos support, EnumFacing face, Vec3 hit) {
      Attempt previous = CURRENT.get();
      Attempt attempt = new Attempt(support, face);
      BlockPos target = support.offset(face);
      IBlockState before = world.getBlockState(target);
      ItemStack saved = stack == null ? null : stack.copy();
      int slot = player.inventory.currentItem;
      CURRENT.set(attempt);
      try {
         boolean placed = controller.onPlayerRightClick(player, world, stack, support, face, hit);
         if (attempt.cancelled) {
            // Undo only the held block's prediction in this world; packet queues are handled separately below.
            if (player.worldObj == world && stack != null && stack.getItem() instanceof ItemBlock
                  && world.getBlockState(target) != before
                  && world.getBlockState(target).getBlock() == ((ItemBlock)stack.getItem()).getBlock()) {
               world.setBlockState(target, before, 3);
            }
            if (saved != null && player.inventory.currentItem == slot
                  && player.inventory.getStackInSlot(slot) == stack) {
               stack.stackSize = saved.stackSize;
            }
            return false;
         }
         return placed;
      } finally {
         if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
      }
   }

   /** Called only for a packet that the synchronous send hook actually rejects; holds remain ordered normally. */
   public static void rejected(Packet<?> packet) {
      Attempt attempt = CURRENT.get();
      if (attempt != null && packet instanceof C08PacketPlayerBlockPlacement) {
         // Aura's synchronous cancellation also represents its ordered AutoBlock queue. That is a deferral,
         // not a dropped placement: retain vanilla prediction and let its normal replay deliver the packet.
         try {
            if (((List<?>)AURA_HELD.get(null)).contains(packet)) return;
         } catch (IllegalAccessException error) {
            throw new IllegalStateException(error);
         }
         C08PacketPlayerBlockPlacement place = (C08PacketPlayerBlockPlacement)packet;
         if (place.getPosition().equals(attempt.support) && place.getPlacedBlockDirection() == attempt.face.getIndex()) {
            attempt.cancelled = true;
         }
      }
   }

   private static final class Attempt {
      final BlockPos support;
      final EnumFacing face;
      boolean cancelled;
      Attempt(BlockPos support, EnumFacing face) {
         this.support = support;
         this.face = face;
      }
   }
}
