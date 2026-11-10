package dev.rice.client.mayu;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

/** Clutch prediction uses collision shapes rather than render bounds or registry names. */
public final class ClutchGeometry {
   private static final double NONE = -1.0E9;
   private ClutchGeometry() {}

   public static boolean blocked(double x, double y, double z, double halfWidth, double height) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.theWorld == null || mc.thePlayer == null) return false;
      AxisAlignedBB body = new AxisAlignedBB(x-halfWidth+1.0E-6, y+0.001, z-halfWidth+1.0E-6,
         x+halfWidth-1.0E-6, y+height-0.001, z+halfWidth-1.0E-6);
      return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, body).isEmpty();
   }

   public static double surface(double x, double y, double z, double halfWidth) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.theWorld == null || mc.thePlayer == null) return NONE;
      AxisAlignedBB probe = new AxisAlignedBB(x-halfWidth+1.0E-6, y-5.0, z-halfWidth+1.0E-6,
         x+halfWidth-1.0E-6, y+0.001, z+halfWidth-1.0E-6);
      double highest = NONE;
      for (AxisAlignedBB box : mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, probe)) {
         if (box.maxY <= y+0.001 && box.maxY > highest) highest = box.maxY;
      }
      return highest;
   }

   public static int fallDistance() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.theWorld == null || mc.thePlayer == null) return -1;
      int x = (int)Math.floor(mc.thePlayer.posX), z = (int)Math.floor(mc.thePlayer.posZ);
      int top = (int)Math.floor(mc.thePlayer.posY)-1;
      for (int y=top; y>=0; --y) {
         BlockPos p = new BlockPos(x,y,z);
         AxisAlignedBB box = mc.theWorld.getBlockState(p).getBlock().getCollisionBoundingBox(mc.theWorld,p,mc.theWorld.getBlockState(p));
         if (box != null && box.maxY <= mc.thePlayer.posY+0.001) return top-y;
      }
      return -1;
   }
}
