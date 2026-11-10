/*
 * Decompiled with CFR 0.152.
 */
package valen.helper;

import valen.Valen;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.utility.IMinecraftInstance;
import valen.utility.RenderUtils;
import valen.utility.Utils;

public class DebugHelper
implements IMinecraftInstance {
    public static boolean MIXIN;
    public static boolean BACKGROUND;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent renderTickEvent) {
        if (!Valen.DEBUG || renderTickEvent.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        if (DebugHelper.mc.currentScreen == null) {
            RenderUtils.renderBPS(true, true);
        }
    }

    public static void debugMixin(Object object, String string) {
        if (!MIXIN) {
            return;
        }
        Utils.sendMessage("&d" + object.getClass().getSimpleName() + "&7: " + string);
    }
}

