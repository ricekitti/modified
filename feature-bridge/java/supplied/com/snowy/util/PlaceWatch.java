package com.snowy.util;
import net.minecraft.util.BlockPos;
/** Successful-placement observation for modules connected to this host. */
public final class PlaceWatch {
    private static BlockPos last;private static long count;
    public static void placed(String module,BlockPos position){last=position;count++;}
    public static long count(){return count;}
}
