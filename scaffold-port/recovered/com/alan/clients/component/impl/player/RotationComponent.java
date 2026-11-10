/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.component.Component
 *  com.alan.clients.component.impl.player.rotationcomponent.MovementFix
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.input.MoveInputEvent
 *  com.alan.clients.newevent.impl.motion.JumpEvent
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.newevent.impl.motion.PreUpdateEvent
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.newevent.impl.render.LookEvent
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.util.rotation.RotationUtil
 *  com.alan.clients.util.vector.Vector2f
 *  lombok.Generated
 *  net.minecraft.util.MathHelper
 */
package com.alan.clients.component.impl.player;

import com.alan.clients.component.Component;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.JumpEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.render.LookEvent;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import java.util.function.Function;
import lombok.Generated;
import net.minecraft.util.MathHelper;

public final class RotationComponent
extends Component {
    private static boolean dj;
    private static boolean fj;
    public static Vector2f fk;
    public static Vector2f fl;
    public static Vector2f fm;
    public static Vector2f fn;
    private static double rotationSpeed;
    private static MovementFix correctMovement;
    private static Function<Vector2f, Boolean> fq;
    private static boolean fr;
    private static boolean fs;
    private static float ft;
    private static final Vector2f fu;
    @EventLink(value=0)
    public final Listener<PreUpdateEvent> onPreUpdate = var0 -> {
        if (!dj || fk == null || fl == null || fm == null || fn == null) {
            fm = fn = new Vector2f(RotationComponent.aEg.thePlayer.pl, RotationComponent.aEg.thePlayer.rotationPitch);
            fl = fn;
            fk = fn;
        }
        if (dj) {
            RotationComponent.bJ();
        }
        if (correctMovement == MovementFix.BACKWARDS_SPRINT && dj && Math.abs((double)(RotationComponent.fk.x % 360.0f) - Math.toDegrees(MoveUtil.direction()) % 360.0) > 45.0) {
            RotationComponent.aEg.gameSettings.cgG.setPressed(false);
            RotationComponent.aEg.thePlayer.setSprinting(false);
        }
    };
    @EventLink(value=1)
    public final Listener<MoveInputEvent> onMove = var0 -> {
        if (dj && correctMovement == MovementFix.NORMAL && fk != null) {
            float f = RotationComponent.fk.x;
            MoveUtil.fixMovement((MoveInputEvent)var0, (float)f);
        }
    };
    @EventLink(value=0)
    public final Listener<LookEvent> onLook = var0 -> {
        if (dj && fk != null && fs) {
            var0.setRotation(fk);
        }
    };
    @EventLink(value=0)
    public final Listener<StrafeEvent> onStrafe = var0 -> {
        if (dj && (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) && fk != null) {
            var0.setYaw(RotationComponent.fk.x);
        }
    };
    @EventLink(value=0)
    public final Listener<JumpEvent> onJump = var0 -> {
        if (dj && (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL || correctMovement == MovementFix.BACKWARDS_SPRINT) && fk != null) {
            var0.setYaw(RotationComponent.fk.x);
        }
    };
    @EventLink(value=0)
    public final Listener<PreMotionEvent> onPreMotionEvent = var1 -> {
        if (dj && fk != null) {
            boolean flag;
            float f = RotationComponent.fk.x;
            float f1 = RotationComponent.fk.y;
            var1.setYaw(f);
            var1.setPitch(f1);
            RotationComponent.aEg.thePlayer.rotationYawHead = f;
            RotationComponent.aEg.thePlayer.po = f1;
            if (!fr) {
                RotationComponent.aEg.thePlayer.pl = f;
                RotationComponent.aEg.thePlayer.rotationPitch = f1;
            }
            fn = new Vector2f(f, f1);
            boolean bl = flag = Math.abs((RotationComponent.fk.x - RotationComponent.aEg.thePlayer.pl) % 360.0f) < 1.0f && Math.abs(RotationComponent.fk.y - RotationComponent.aEg.thePlayer.rotationPitch) < 1.0f;
            if (flag) {
                dj = false;
                if (fr) {
                    this.bI();
                }
            }
            fl = fk;
        } else {
            fl = new Vector2f(RotationComponent.aEg.thePlayer.pl, RotationComponent.aEg.thePlayer.rotationPitch);
        }
        fm = new Vector2f(RotationComponent.aEg.thePlayer.pl, RotationComponent.aEg.thePlayer.rotationPitch);
        fj = false;
    };

    public static void setRotations(Vector2f vec2, double var1, MovementFix movementFix) {
        RotationComponent.a(vec2, var1, movementFix, null, true);
    }

    public static void a(Vector2f vec2, double var1, MovementFix movementFix, Function<Vector2f, Boolean> function) {
        RotationComponent.a(vec2, var1, movementFix, function, true);
    }

    public static void a(Vector2f vec2, double var1, MovementFix movementFix, Function<Vector2f, Boolean> function, boolean var5) {
        RotationComponent.a(vec2, var1, movementFix, function, var5, true);
    }

    public static void a(Vector2f vec2, double var1, MovementFix movementFix, Function<Vector2f, Boolean> function, boolean var5, boolean var6) {
        fm = vec2;
        rotationSpeed = var1 * 36.0;
        correctMovement = movementFix;
        fq = function;
        fr = var5;
        fs = var6;
        dj = true;
        RotationComponent.bJ();
    }

    public static Vector2f bH() {
        return dj && fl != null ? fl : new Vector2f(RotationComponent.aEg.thePlayer.pl, RotationComponent.aEg.thePlayer.rotationPitch);
    }

    private void bI() {
        Vector2f vector2f = RotationUtil.o((Vector2f)RotationUtil.applySensitivityPatch((Vector2f)new Vector2f(RotationComponent.aEg.thePlayer.pl, RotationComponent.aEg.thePlayer.rotationPitch), (Vector2f)fl));
        RotationComponent.aEg.thePlayer.pl = vector2f.x;
        RotationComponent.aEg.thePlayer.rotationPitch = vector2f.y;
    }

    public static void bJ() {
        if (!fj) {
            float f = RotationComponent.fm.x;
            float f1 = RotationComponent.fm.y;
            if (fq != null && (Math.abs(f - RotationComponent.fk.x) > 5.0f || Math.abs(f1 - RotationComponent.fk.y) > 5.0f)) {
                Vector2f vector2f = new Vector2f(fm.getX(), fm.getY());
                double d0 = Math.random() * Math.random() * Math.random() * 20.0;
                fu.setX((float)((double)fu.getX() + (double)(-MathHelper.sin((float)((float)Math.toRadians(ft += (float)((20.0 + (Math.random() - 0.5) * (Math.random() * Math.random() * Math.random() * 360.0)) * (double)(RotationComponent.aEg.thePlayer.ticksExisted / 10 % 2 == 0 ? -1 : 1)))))) * d0));
                fu.setY((float)((double)fu.getY() + (double)MathHelper.cos((float)((float)Math.toRadians(ft))) * d0));
                float f2 = f + fu.getX();
                float f3 = f1 + fu.getY();
                if (!fq.apply(new Vector2f(f2, f3)).booleanValue()) {
                    ft = (float)Math.toDegrees(Math.atan2(vector2f.getX() - f2, f3 - vector2f.getY())) - 180.0f;
                    float f4 = f;
                    float f5 = f1;
                    fu.setX((float)((double)fu.getX() + (double)(-MathHelper.sin((float)((float)Math.toRadians(ft)))) * d0));
                    fu.setY((float)((double)fu.getY() + (double)MathHelper.cos((float)((float)Math.toRadians(ft))) * d0));
                    f2 = f4 + fu.getX();
                    f3 = f5 + fu.getY();
                }
                if (!fq.apply(new Vector2f(f2, f3)).booleanValue()) {
                    fu.setX(0.0f);
                    fu.setY(0.0f);
                    f2 = (float)((double)RotationComponent.fm.x + Math.random() * 2.0);
                    f3 = (float)((double)RotationComponent.fm.y + Math.random() * 2.0);
                }
                f = f2;
                f1 = f3;
            }
            fk = RotationUtil.e((Vector2f)new Vector2f(f, f1), (double)(rotationSpeed + Math.random()));
            if (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) {
                RotationComponent.aEg.thePlayer.pp = RotationComponent.fk.x;
            }
            RotationComponent.aEg.thePlayer.pq = RotationComponent.fk.x;
        }
        fj = true;
        if (fs) {
            RotationComponent.aEg.entityRenderer.getMouseOver(1.0f);
        }
    }

    @Generated
    public static void c(boolean var0) {
        dj = var0;
    }

    @Generated
    public static void d(boolean var0) {
        fj = var0;
    }

    @Generated
    public static boolean bd() {
        return dj;
    }

    @Generated
    public static boolean bK() {
        return fj;
    }

    static {
        fl = new Vector2f(0.0f, 0.0f);
        fr = true;
        fs = true;
        fu = new Vector2f(0.0f, 0.0f);
    }
}
