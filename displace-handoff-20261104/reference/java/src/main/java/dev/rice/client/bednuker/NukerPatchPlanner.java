package dev.rice.client.bednuker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/** Plans one server-visible tunnel through a layered bed defense instead of greedily mining every shell. */
public final class NukerPatchPlanner {
   private static final int MARGIN = 5;
   private static final double EPSILON = 0.000001D;

   public interface Model {
      double breakTicks(BlockPos position);

      boolean canBreak(BlockPos position);

      boolean canTarget(BlockPos position);
   }

   private static final class Step {
      final BlockPos position;
      final double cost;
      final int hops;

      Step(BlockPos position, double cost, int hops) {
         this.position = position;
         this.cost = cost;
         this.hops = hops;
      }
   }

   private NukerPatchPlanner() {
   }

   /** Returns only the next exposed block on the cheapest complete route from outside to either bed half. */
   public static BlockPos next(WorldClient world, BlockPos firstBedHalf, BlockPos secondBedHalf, Model model) {
      int minX = Math.min(firstBedHalf.getX(), secondBedHalf.getX()) - MARGIN;
      int maxX = Math.max(firstBedHalf.getX(), secondBedHalf.getX()) + MARGIN;
      int minY = Math.min(firstBedHalf.getY(), secondBedHalf.getY());
      int maxY = Math.max(firstBedHalf.getY(), secondBedHalf.getY()) + MARGIN;
      int minZ = Math.min(firstBedHalf.getZ(), secondBedHalf.getZ()) - MARGIN;
      int maxZ = Math.max(firstBedHalf.getZ(), secondBedHalf.getZ()) + MARGIN;

      Comparator<Step> ordering = new Comparator<Step>() {
         @Override
         public int compare(Step first, Step second) {
            int cost = Double.compare(first.cost, second.cost);
            if (cost != 0) {
               return cost;
            }
            int hops = Integer.compare(first.hops, second.hops);
            if (hops != 0) {
               return hops;
            }
            int x = Integer.compare(first.position.getX(), second.position.getX());
            if (x != 0) {
               return x;
            }
            int y = Integer.compare(first.position.getY(), second.position.getY());
            return y != 0 ? y : Integer.compare(first.position.getZ(), second.position.getZ());
         }
      };
      PriorityQueue<Step> pending = new PriorityQueue<>(ordering);
      Map<BlockPos, Double> costs = new HashMap<>();
      Map<BlockPos, Integer> hops = new HashMap<>();
      Map<BlockPos, BlockPos> parent = new HashMap<>();
      Set<BlockPos> bedHalves = new HashSet<>();
      bedHalves.add(firstBedHalf);
      bedHalves.add(secondBedHalf);
      seed(firstBedHalf, pending, costs, hops);
      seed(secondBedHalf, pending, costs, hops);

      BlockPos exit = null;
      while (!pending.isEmpty()) {
         Step current = pending.poll();
         Double bestCost = costs.get(current.position);
         Integer bestHops = hops.get(current.position);
         if (bestCost == null || current.cost > bestCost + EPSILON || current.hops != bestHops) {
            continue;
         }
         if (!bedHalves.contains(current.position)
            && isBoundary(current.position, minX, maxX, minY, maxY, minZ, maxZ)
            && isReplaceable(world, current.position)) {
            exit = current.position;
            break;
         }

         for (EnumFacing facing : EnumFacing.values()) {
            BlockPos next = current.position.offset(facing);
            if (!inside(next, minX, maxX, minY, maxY, minZ, maxZ)) {
               continue;
            }
            boolean replaceable = isReplaceable(world, next);
            double addedCost = 0.0D;
            if (!replaceable && !bedHalves.contains(next)) {
               if (!model.canBreak(next)) {
                  continue;
               }
               addedCost = model.breakTicks(next);
               if (!(addedCost >= 0.0D) || Double.isInfinite(addedCost) || Double.isNaN(addedCost)) {
                  continue;
               }
            }
            double candidate = current.cost + addedCost;
            int candidateHops = current.hops + 1;
            Double oldCost = costs.get(next);
            Integer oldHops = hops.get(next);
            if (oldCost == null || candidate < oldCost - EPSILON
               || Math.abs(candidate - oldCost) <= EPSILON && candidateHops < oldHops) {
               costs.put(next, candidate);
               hops.put(next, candidateHops);
               parent.put(next, current.position);
               pending.add(new Step(next, candidate, candidateHops));
            }
         }
      }

      if (exit == null) {
         return null;
      }
      List<BlockPos> route = new ArrayList<>();
      for (BlockPos cursor = exit; cursor != null; cursor = parent.get(cursor)) {
         route.add(cursor);
         if (bedHalves.contains(cursor)) {
            break;
         }
      }
      Collections.reverse(route);
      for (int i = route.size() - 1; i >= 0; i--) {
         BlockPos position = route.get(i);
         if (!isReplaceable(world, position)) {
            return model.canTarget(position) ? position : null;
         }
      }
      return null;
   }

   private static void seed(BlockPos position, PriorityQueue<Step> pending, Map<BlockPos, Double> costs, Map<BlockPos, Integer> hops) {
      costs.put(position, 0.0D);
      hops.put(position, 0);
      pending.add(new Step(position, 0.0D, 0));
   }

   private static boolean isReplaceable(WorldClient world, BlockPos position) {
      Block block = world.getBlockState(position).getBlock();
      return block.isReplaceable(world, position);
   }

   private static boolean inside(BlockPos position, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
      return position.getX() >= minX && position.getX() <= maxX
         && position.getY() >= minY && position.getY() <= maxY
         && position.getZ() >= minZ && position.getZ() <= maxZ;
   }

   private static boolean isBoundary(BlockPos position, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
      return position.getX() == minX || position.getX() == maxX || position.getY() == maxY
         || position.getZ() == minZ || position.getZ() == maxZ;
   }
}
