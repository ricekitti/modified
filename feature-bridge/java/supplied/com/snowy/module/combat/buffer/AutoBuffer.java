package com.snowy.module.combat.buffer;
/** Placement observation only; the destination has no Snowy AutoBuffer packet queue. */
public final class AutoBuffer {
    private static long placements;
    public static void notePlacedBlock(){placements++;}
    public static long placements(){return placements;}
}
