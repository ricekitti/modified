/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.util.player.PlayerUtil
 *  com.alan.clients.value.Mode
 *  net.minecraft.item.ItemStack
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.Mode;
import net.minecraft.item.ItemStack;

public class VulcanTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = var0 -> {
        if (VulcanTower.aEg.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.ay((int)2) && VulcanTower.aEg.thePlayer.tR > 3) {
            ItemStack itemstack = VulcanTower.aEg.thePlayer.inventory.mainInventory[VulcanTower.aEg.thePlayer.inventory.currentItem];
            if (itemstack != null) {
                // empty if block
            }
            if (!MoveUtil.isMoving()) {
                var0.setOnGround(true);
                VulcanTower.aEg.thePlayer.onGround = true;
                VulcanTower.aEg.thePlayer.motionY = 0.5;
                if (VulcanTower.aEg.thePlayer.ticksExisted % 2 == 0) {
                    var0.setPosX(var0.getPosX() + 0.1);
                }
            }
        }
    };

    public VulcanTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}
