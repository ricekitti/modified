package com.snowy.module.combat.displace;
/** Placement-look observations; the host leaves vanilla movement-packet construction intact. */
public final class WireRotation {
    private static float yaw,pitch;
    private static long observations;
    public static void ensurePlaced(){/* No foreign Displace wire is installed in this host. */}
    public static void notePlacementLook(float y,float p){yaw=y;pitch=p;observations++;}
    public static long observations(){return observations;}
}
