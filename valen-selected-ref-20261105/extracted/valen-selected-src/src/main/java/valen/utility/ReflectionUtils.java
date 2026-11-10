/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiEnchantment
 *  net.minecraft.client.gui.inventory.GuiBrewingStand
 *  net.minecraft.client.gui.inventory.GuiDispenser
 *  net.minecraft.client.gui.inventory.GuiFurnace
 *  net.minecraft.client.settings.KeyBinding
 *  net.minecraft.inventory.ContainerHopper
 *  net.minecraft.inventory.ContainerHorseInventory
 *  net.minecraft.inventory.ContainerMerchant
 *  net.minecraft.inventory.ContainerWorkbench
 *  net.minecraft.inventory.IInventory
 *  org.lwjgl.input.Mouse
 */
package valen.utility;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import org.lwjgl.input.Mouse;
import valen.lunar.forge.client.event.MouseEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.fml.relauncher.ReflectionHelper;
import valen.mixin.interfaces.IMixinItemRenderer;

public class ReflectionUtils {
    public static Field button;
    public static Field buttonstate;
    public static Field buttons;
    public static HashMap<Class, Field> containerInventoryPlayer;
    private static List<Class> containerClasses;
    public static boolean ERROR;
    public static Map<String, KeyBinding> keybinds;

    public static void setupFields() {
        try {
            button = MouseEvent.class.getDeclaredField("button");
            buttonstate = MouseEvent.class.getDeclaredField("buttonstate");
            buttons = Mouse.class.getDeclaredField("buttons");
            for (Class clazz : containerClasses) {
                for (Field field : clazz.getDeclaredFields()) {
                    ReflectionUtils.addToMap(clazz, field);
                }
            }
        }
        catch (Exception exception) {
            System.out.println("There was an error, relaunch the game.");
            exception.printStackTrace();
            ERROR = true;
        }
    }

    public static void setKeyBindings() {
        for (KeyBinding keyBinding : Minecraft.getMinecraft().gameSettings.keyBindings) {
            String string = keyBinding.getKeyDescription().replaceFirst("key\\.", "");
            keybinds.put(string, keyBinding);
        }
    }

    public static void setButton(int n, boolean bl) {
        if (button != null && buttonstate != null && buttons != null) {
            MouseEvent mouseEvent = new MouseEvent();
            try {
                button.setAccessible(true);
                button.set(mouseEvent, n);
                buttonstate.setAccessible(true);
                buttonstate.set(mouseEvent, bl);
                MinecraftForge.EVENT_BUS.post(mouseEvent);
                buttons.setAccessible(true);
                ByteBuffer byteBuffer = (ByteBuffer)buttons.get(null);
                buttons.setAccessible(false);
                byteBuffer.put(n, (byte)(bl ? 1 : 0));
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
    }

    private static void addToMap(Class clazz, Field field) {
        if (field == null || field.getType() != IInventory.class) {
            return;
        }
        if ((field = ReflectionHelper.findField(clazz, field.getName())) == null) {
            return;
        }
        field.setAccessible(true);
        containerInventoryPlayer.put(clazz, field);
    }

    public static boolean setItemInUse(boolean bl) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.getItemRenderer() != null) {
            ((IMixinItemRenderer)minecraft.getItemRenderer()).setRenderItemInUse(bl);
        }
        return bl;
    }

    static {
        containerInventoryPlayer = new HashMap();
        containerClasses = Arrays.asList(GuiFurnace.class, GuiBrewingStand.class, GuiEnchantment.class, ContainerHopper.class, GuiDispenser.class, ContainerWorkbench.class, ContainerMerchant.class, ContainerHorseInventory.class);
        ERROR = false;
        keybinds = new HashMap<String, KeyBinding>();
    }
}

