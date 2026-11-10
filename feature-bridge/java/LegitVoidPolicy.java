package dev.rice.client.combat;

/** A candidate must stay inside the front hemisphere of the live aim. */
public final class LegitVoidPolicy {
    private LegitVoidPolicy() {}
    public static boolean allows(float referenceYaw,float candidateYaw) {
        if(!Float.isFinite(referenceYaw)||!Float.isFinite(candidateYaw))return false;
        double delta=((double)candidateYaw-referenceYaw)%360.0;
        if(delta>=180.0)delta-=360.0;
        if(delta< -180.0)delta+=360.0;
        return Math.abs(delta)<=90.0;
    }
}
