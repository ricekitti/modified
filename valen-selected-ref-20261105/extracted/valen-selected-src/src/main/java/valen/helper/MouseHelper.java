/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  org.lwjgl.input.Mouse
 */
package valen.helper;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Mouse;
import valen.Valen;
import valen.lunar.forge.client.event.MouseEvent;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.module.ModuleManager;
import valen.module.impl.world.AntiBot;
import valen.utility.Utils;

public class MouseHelper {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static List<Long> a = new ArrayList<Long>();
    private static List<Long> b = new ArrayList<Long>();
    public static long LL = 0L;
    public static long LR = 0L;
    private static int cachedWheelDelta = 0;
    private static boolean wheelDeltaCached = false;

    public static void updateWheelCache() {
        cachedWheelDelta = Mouse.getDWheel();
        wheelDeltaCached = true;
    }

    public static boolean isScrollDown(int n) {
        if (!wheelDeltaCached) {
            cachedWheelDelta = Mouse.getDWheel();
            wheelDeltaCached = true;
        }
        if (cachedWheelDelta != 0) {
            if (n == 1069) {
                return cachedWheelDelta > 0;
            }
            if (n == 1070) {
                return cachedWheelDelta < 0;
            }
        }
        return false;
    }

    public static void clearWheelCache() {
        wheelDeltaCached = false;
        cachedWheelDelta = 0;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent mouseEvent) {
        EntityLivingBase entityLivingBase;
        if (!mouseEvent.buttonstate) {
            return;
        }
        if (mouseEvent.button == 0) {
            MouseHelper.aL();
            if (Valen.DEBUG && MouseHelper.mc.objectMouseOver != null) {
                Entity entity = MouseHelper.mc.objectMouseOver.entityHit;
                if (entity == null || !(entity instanceof EntityLivingBase)) {
                    return;
                }
                Utils.printInfo((EntityLivingBase)entity);
            }
        } else if (mouseEvent.button == 1) {
            MouseHelper.aR();
        } else if (mouseEvent.button == 2 && ModuleManager.relationships != null && ModuleManager.relationships.isEnabled() && ModuleManager.relationships.middleClickFriends.isToggled() && (entityLivingBase = Utils.raytrace(200)) != null && !AntiBot.isBot((Entity)entityLivingBase)) {
            String string = entityLivingBase.getName();
            if (Utils.addFriend(string)) {
                Utils.sendMessage("&7Added &afriend&7: &b" + string);
            } else if (Utils.removeFriend(string)) {
                Utils.sendMessage("&7Removed &afriend&7: &b" + string);
            }
        }
    }

    public static void aL() {
        LL = System.currentTimeMillis();
        a.add(LL);
    }

    public static void aR() {
        LR = System.currentTimeMillis();
        b.add(LR);
    }

    public static int f() {
        a.removeIf(l -> l < System.currentTimeMillis() - 1000L);
        return a.size();
    }

    public static int i() {
        b.removeIf(l -> l < System.currentTimeMillis() - 1000L);
        return b.size();
    }
}

