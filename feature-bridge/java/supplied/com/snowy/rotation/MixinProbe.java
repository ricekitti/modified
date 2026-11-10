package com.snowy.rotation;
/** This host installs native call-site hooks, not Snowy's mixin schema. */
public final class MixinProbe {
    public static boolean injectedIntoPlayer(){return false;}
}
