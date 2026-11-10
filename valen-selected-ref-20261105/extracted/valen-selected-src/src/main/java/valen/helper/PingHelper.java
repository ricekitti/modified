/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.utility.CommandHandler
 */
package valen.helper;

import valen.lunar.forge.client.event.ClientChatReceivedEvent;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.utility.CommandHandler;
import valen.utility.Utils;

public class PingHelper {
    private static boolean sendChat = false;
    private static long sendTime = 0L;
    private static boolean sendChatCC = false;
    private static long sendTimeCC = 0L;

    @SubscribeEvent
    public void onChatMessageRecieved(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (sendChat ^ sendChatCC && Utils.nullCheck() && Utils.stripColor(clientChatReceivedEvent.message.getUnformattedText()).startsWith("Unknown")) {
            clientChatReceivedEvent.setCanceled(true);
            this.getPing(sendChatCC);
            sendChat = false;
            sendChatCC = false;
        }
    }

    public static void checkPing(boolean bl) {
        if (bl) {
            Utils.sendMessage("&7Checking ping...");
        } else {
            CommandHandler.print((String)"\u00a73Checking...", (int)1);
        }
        if (sendChat) {
            if (bl) {
                Utils.sendMessage("&7Please wait before checking again.");
            } else {
                CommandHandler.print((String)"\u00a7cPlease wait.", (int)0);
            }
            return;
        }
        Utils.mc.thePlayer.sendChatMessage("/...");
        if (bl) {
            sendChatCC = true;
            sendTimeCC = System.currentTimeMillis();
        } else {
            sendChat = true;
            sendTime = System.currentTimeMillis();
        }
    }

    private void getPing(boolean bl) {
        int n = (int)(System.currentTimeMillis() - (bl ? sendTimeCC : sendTime)) - 20;
        if (n < 0) {
            n = 0;
        }
        if (bl) {
            Utils.sendMessage("&7Your ping: &b" + n + "&7ms.");
        } else {
            CommandHandler.print((String)("Your ping: " + n + "ms"), (int)0);
        }
        PingHelper.reset(bl);
    }

    public static void reset(boolean bl) {
        if (bl) {
            sendChatCC = false;
            sendTimeCC = 0L;
        } else {
            sendChat = false;
            sendTime = 0L;
        }
    }
}

