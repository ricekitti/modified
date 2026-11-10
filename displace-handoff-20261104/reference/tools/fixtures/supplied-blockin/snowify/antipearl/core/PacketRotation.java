/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.snowify.antipearl.core;

import java.lang.reflect.Field;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PacketRotation {
    private static final Logger LOGGER = LogManager.getLogger((String)"AntiPearl");
    private static Field yawField;
    private static Field pitchField;
    private static boolean resolved;

    private PacketRotation() {
    }

    public static boolean apply(C03PacketPlayer packet, float yaw, float pitch) {
        if (!resolved) {
            PacketRotation.resolve();
        }
        if (yawField == null || pitchField == null) {
            return false;
        }
        try {
            yawField.setFloat(packet, yaw);
            pitchField.setFloat(packet, pitch);
            return true;
        }
        catch (Throwable failure) {
            return false;
        }
    }

    public static boolean isUsable() {
        if (!resolved) {
            PacketRotation.resolve();
        }
        return yawField != null && pitchField != null;
    }

    private static void resolve() {
        resolved = true;
        yawField = PacketRotation.find("yaw", "field_149476_e");
        pitchField = PacketRotation.find("pitch", "field_149473_f");
        if (yawField == null || pitchField == null) {
            LOGGER.warn("Could not reach the rotation fields of C03PacketPlayer, so the look will only be replaced where the movement packet is built. Riding anything will bypass the silent aim.");
        }
    }

    private static Field find(String ... names) {
        for (String name : names) {
            try {
                Field field = C03PacketPlayer.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            }
            catch (Throwable throwable) {
            }
        }
        return null;
    }
}
