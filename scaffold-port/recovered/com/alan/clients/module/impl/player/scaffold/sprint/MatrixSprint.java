/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.component.impl.player.RotationComponent
 *  com.alan.clients.module.impl.movement.Speed
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.value.Mode
 *  net.minecraft.util.MathHelper
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.value.Mode;
import net.minecraft.util.MathHelper;

public class MatrixSprint
extends Mode<Scaffold> {
    private int time;
    private boolean ignore;
    @EventLink
    public final Listener<StrafeEvent> onStrafe = var1x -> {
        boolean flag;
        float f = MathHelper.wrapAngleTo180_float((float)MatrixSprint.aEg.thePlayer.pl);
        RotationComponent.d((boolean)false);
        ((Speed)this.e(Speed.class)).setEnabled(false);
        boolean bl = flag = Math.abs(f % 80.0f) <= 15.0f || Math.abs(f % 90.0f) >= 75.0f;
        if (!flag) {
            MatrixSprint.aEg.thePlayer.motionX *= 0.95;
            MatrixSprint.aEg.thePlayer.motionZ *= 0.95;
        }
    };

    public MatrixSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}
