package dev.rice.mayu;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Destination dependencies, separate from the upstream Scaffold algorithm. */
public final class Compatibility {
    private Compatibility(){}
    public static boolean isKeyDown(int key){return key<0?Mouse.isButtonDown(key+100):key>0&&key<256&&Keyboard.isKeyDown(key);}
    public static Vec3 vectorForRotation(float pitch,float yaw){
        float c=MathHelper.cos(-yaw*0.017453292F-(float)Math.PI),s=MathHelper.sin(-yaw*0.017453292F-(float)Math.PI);
        float p=-MathHelper.cos(-pitch*0.017453292F),q=MathHelper.sin(-pitch*0.017453292F);
        return new Vec3(s*p,q,c*p);
    }
    // The destination has no BedNuker, automatic LongJump, or TargetStrafe modules.
    // Mayu's conflict guards therefore have no active counterpart here.
    public static boolean canPlace(){return true;}
    public static float targetStrafeYaw(){return Float.NaN;}
    public static float hudScale(){return 1.0F;}
    public static boolean hudShadow(){return true;}
}
