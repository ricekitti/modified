package com.snowy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.BlockPos;

/**
 * Reflective reads and writes of game state that Snowy's mixin builds reach through mixed-in
 * accessor interfaces ({@code EntityAccess}, {@code MiningAccess}, and the rest). Those interfaces
 * only exist when Snowy is loaded as a mixin mod; an injected client cannot add them, because
 * {@code retransformClasses} may not change a loaded class's schema. Reading the same,
 * already-present fields by reflection works under every loader, so all of Snowy now goes through
 * here rather than casting to an added interface.
 *
 * <p>Each member is looked up under its MCP name (dev client, Lunar), its SRG name (Forge
 * production) and its obfuscated 1.8.9 name (vanilla / Badlion, where the injector weaves in against
 * the official names), the first that resolves is cached, so one accessor serves every build. The
 * extra names are harmless on the other name sets (no field or method answers to them there). A
 * lookup that fails leaves the module as a no-op rather than throwing into a game method.
 */
public final class GameAccess {

    private GameAccess() {
    }

    // --- Minecraft -----------------------------------------------------------

    private static Field timerField;
    private static Field partialField;
    private static Method rightClickMethod;
    private static Method leftClickMethod;

    /** {@code Minecraft.timer.renderPartialTicks}, the partial tick the renderer is drawing at. */
    public static float renderPartialTicks() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (timerField == null) {
                timerField = field(Minecraft.class, "timer", "field_71428_T", "Y");
            }
            Object timer = timerField.get(mc);
            if (partialField == null) {
                partialField = field(timer.getClass(), "renderPartialTicks", "field_74281_c", "c");
            }
            return partialField.getFloat(timer);
        } catch (Throwable t) {
            return 1.0F;
        }
    }

    private static Field leftClickCounterField;

    /**
     * Sets {@code Minecraft.leftClickCounter}: a left click that hits nothing sets it to 10, and while it is above 0
     * the game ignores every left click outright (no swing, no attack) - half a second of dead clicks.
     */
    public static void setLeftClickCounter(Minecraft mc, int ticks) {
        try {
            if (leftClickCounterField == null) {
                leftClickCounterField = field(Minecraft.class, "leftClickCounter", "field_71429_W", "ag");
            }
            leftClickCounterField.setInt(mc, ticks);
        } catch (Throwable ignored) {
            // left as the game has it
        }
    }

    /** {@code Minecraft.timer}, the game's tick clock (its timerSpeed is public); null if unreachable. */
    public static net.minecraft.util.Timer timer(Minecraft mc) {
        try {
            if (timerField == null) {
                timerField = field(Minecraft.class, "timer", "field_71428_T", "Y");
            }
            return (net.minecraft.util.Timer) timerField.get(mc);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Field rightClickDelayField;

    /** {@code Minecraft.rightClickDelayTimer}: ticks until a held right click uses again (4 after each). */
    public static int rightClickDelay(Minecraft mc) {
        try {
            if (rightClickDelayField == null) {
                rightClickDelayField = field(Minecraft.class, "rightClickDelayTimer", "field_71467_ac", "ap");
            }
            return rightClickDelayField.getInt(mc);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static void setRightClickDelay(Minecraft mc, int ticks) {
        try {
            if (rightClickDelayField == null) {
                rightClickDelayField = field(Minecraft.class, "rightClickDelayTimer", "field_71467_ac", "ap");
            }
            rightClickDelayField.setInt(mc, ticks);
        } catch (Throwable ignored) {
            // left as the game has it
        }
    }

    /** Minecraft's own {@code rightClickMouse()} - a throw Stasis held back, made now. */
    public static void rightClickMouse(Minecraft mc) {
        try {
            if (rightClickMethod == null) {
                rightClickMethod = method(Minecraft.class, "rightClickMouse", "func_147121_ag", "ax");
            }
            rightClickMethod.invoke(mc);
        } catch (Throwable ignored) {
            // nothing safe to do from here; the throw simply does not fire this tick
        }
    }

    /** Minecraft's own {@code clickMouse()} - a left click made now (the Auto Clicker's click). */
    public static void clickMouse(Minecraft mc) {
        try {
            if (leftClickMethod == null) {
                leftClickMethod = method(Minecraft.class, "clickMouse", "func_147116_af", "aw");
            }
            leftClickMethod.invoke(mc);
        } catch (Throwable ignored) {
            // the click simply does not fire
        }
    }

    // --- Entity --------------------------------------------------------------

    private static Field inWebField;

    /** The protected {@code isInWeb} flag. */
    public static boolean isInWeb(Entity entity) {
        try {
            if (inWebField == null) {
                inWebField = field(Entity.class, "isInWeb", "field_70134_J", "H");
            }
            return inWebField.getBoolean(entity);
        } catch (Throwable t) {
            return false;
        }
    }

    // --- EntityLivingBase ----------------------------------------------------

    private static Field jumpTicksField;
    private static Field potionsField;

    /** The private {@code jumpTicks}: ticks before a held jump can jump again (10 after each). */
    public static int jumpTicks(net.minecraft.entity.EntityLivingBase entity) {
        try {
            if (jumpTicksField == null) {
                jumpTicksField = field(net.minecraft.entity.EntityLivingBase.class, "jumpTicks", "field_70773_bE", "bn");
            }
            return jumpTicksField.getInt(entity);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static void setJumpTicks(net.minecraft.entity.EntityLivingBase entity, int ticks) {
        try {
            if (jumpTicksField == null) {
                jumpTicksField = field(net.minecraft.entity.EntityLivingBase.class, "jumpTicks", "field_70773_bE", "bn");
            }
            jumpTicksField.setInt(entity, ticks);
        } catch (Throwable ignored) {
            // left as the game has it
        }
    }

    /** The live map of effects by potion id ({@code activePotionsMap}); null if unreachable. */
    @SuppressWarnings("unchecked")
    public static java.util.Map<Integer, net.minecraft.potion.PotionEffect> potions(
            net.minecraft.entity.EntityLivingBase entity) {
        try {
            if (potionsField == null) {
                potionsField = field(net.minecraft.entity.EntityLivingBase.class, "activePotionsMap", "field_70713_bf", "g");
            }
            return (java.util.Map<Integer, net.minecraft.potion.PotionEffect>) potionsField.get(entity);
        } catch (Throwable t) {
            return null;
        }
    }

    // --- EntityPlayerSP ------------------------------------------------------

    private static Field serverSprintField;

    /** The sprint state the server has last been told of. */
    public static boolean serverSprinting(EntityPlayerSP player) {
        try {
            if (serverSprintField == null) {
                serverSprintField = field(EntityPlayerSP.class, "serverSprintState", "field_175171_bO", "bQ");
            }
            return serverSprintField.getBoolean(player);
        } catch (Throwable t) {
            return false;
        }
    }

    // --- PlayerControllerMP --------------------------------------------------

    private static Field currentItemField;
    private static Field hittingField;
    private static Field currentBlockField;
    private static Field damageField;
    private static Field hitDelayField;
    private static Method syncItemMethod;

    /** The hotbar slot the server was last told of. */
    public static int currentPlayerItem(PlayerControllerMP controller) {
        try {
            if (currentItemField == null) {
                currentItemField = field(PlayerControllerMP.class, "currentPlayerItem", "field_78777_l", "j");
            }
            return currentItemField.getInt(controller);
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Tell the server the current hotbar slot now ({@code syncCurrentPlayItem}). */
    public static void syncHeldItem(PlayerControllerMP controller) {
        try {
            if (syncItemMethod == null) {
                syncItemMethod = method(PlayerControllerMP.class, "syncCurrentPlayItem", "func_78750_j", "n");
            }
            syncItemMethod.invoke(controller);
        } catch (Throwable ignored) {
            // leave the held item unsynced rather than throw into the caller
        }
    }

    public static boolean hittingBlock(PlayerControllerMP controller) {
        try {
            if (hittingField == null) {
                hittingField = field(PlayerControllerMP.class, "isHittingBlock", "field_78778_j", "h");
            }
            return hittingField.getBoolean(controller);
        } catch (Throwable t) {
            return false;
        }
    }

    /** Sets the raw isHittingBlock flag without aborting the dig (Item Use Fix parks it around a use). */
    public static void setHittingBlock(PlayerControllerMP controller, boolean value) {
        try {
            if (hittingField == null) {
                hittingField = field(PlayerControllerMP.class, "isHittingBlock", "field_78778_j", "h");
            }
            hittingField.setBoolean(controller, value);
        } catch (Throwable ignored) {
            // field not resolvable on this client: Item Use Fix simply does nothing
        }
    }

    public static BlockPos currentBlock(PlayerControllerMP controller) {
        try {
            if (currentBlockField == null) {
                currentBlockField = field(PlayerControllerMP.class, "currentBlock", "field_178895_c", "c");
            }
            return (BlockPos) currentBlockField.get(controller);
        } catch (Throwable t) {
            return null;
        }
    }

    public static float blockDamage(PlayerControllerMP controller) {
        try {
            if (damageField == null) {
                damageField = field(PlayerControllerMP.class, "curBlockDamageMP", "field_78770_f", "e");
            }
            return damageField.getFloat(controller);
        } catch (Throwable t) {
            return 0.0F;
        }
    }

    public static void setBlockDamage(PlayerControllerMP controller, float damage) {
        try {
            if (damageField == null) {
                damageField = field(PlayerControllerMP.class, "curBlockDamageMP", "field_78770_f", "e");
            }
            damageField.setFloat(controller, damage);
        } catch (Throwable ignored) {
        }
    }

    public static int hitDelay(PlayerControllerMP controller) {
        try {
            if (hitDelayField == null) {
                hitDelayField = field(PlayerControllerMP.class, "blockHitDelay", "field_78781_i", "g");
            }
            return hitDelayField.getInt(controller);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static void setHitDelay(PlayerControllerMP controller, int ticks) {
        try {
            if (hitDelayField == null) {
                hitDelayField = field(PlayerControllerMP.class, "blockHitDelay", "field_78781_i", "g");
            }
            hitDelayField.setInt(controller, ticks);
        } catch (Throwable ignored) {
        }
    }

    // --- NetworkManager ------------------------------------------------------

    private static Field channelField;

    /** NetworkManager's Netty channel (private in vanilla; Forge adds a getter, but this reads the field). */
    public static Channel channel(NetworkManager manager) {
        try {
            if (channelField == null) {
                channelField = field(NetworkManager.class, "channel", "field_150746_k", "k");
            }
            return (Channel) channelField.get(manager);
        } catch (Throwable t) {
            return null;
        }
    }

    // --- NetworkManager: which side this connection is -----------------------

    private static Field packetListenerField;

    /** True when this NetworkManager's listener is the client play handler (not the integrated server's). */
    public static boolean isClientConnection(NetworkManager manager) {
        try {
            if (packetListenerField == null) {
                packetListenerField = field(NetworkManager.class, "packetListener", "field_150744_m", "m");
            }
            return packetListenerField.get(manager) instanceof NetHandlerPlayClient;
        } catch (Throwable t) {
            return false;
        }
    }

    // --- RendererLivingEntity ------------------------------------------------

    private static Field layersField;

    /** The mutable list of layers a living entity's renderer draws, for adding the cosmetics layer. */
    @SuppressWarnings("rawtypes")
    public static List layers(RendererLivingEntity renderer) {
        try {
            if (layersField == null) {
                layersField = field(RendererLivingEntity.class, "layerRenderers", "field_177097_h", "h");
            }
            return (List) layersField.get(renderer);
        } catch (Throwable t) {
            return null;
        }
    }

    // --- GuiContainer --------------------------------------------------------

    private static Field theSlotField;

    /** {@code GuiContainer.theSlot}, the slot the mouse is over (null when over none). */
    public static Slot hoveredSlot(GuiContainer screen) {
        try {
            if (theSlotField == null) {
                theSlotField = field(GuiContainer.class, "theSlot", "field_147006_u", "u");
            }
            return (Slot) theSlotField.get(screen);
        } catch (Throwable t) {
            return null;
        }
    }

    // --- GuiPlayerTabOverlay -------------------------------------------------

    private static Field footerField;

    /** {@code GuiPlayerTabOverlay.footer}, the line drawn under the tab list (null for none). */
    public static Object tabListFooter(Object overlay) {
        try {
            if (footerField == null) {
                footerField = field(net.minecraft.client.gui.GuiPlayerTabOverlay.class,
                        "footer", "field_175255_h", "h");
            }
            return footerField.get(overlay);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Sets {@code GuiPlayerTabOverlay.footer} (Generator Timers' line in for the frame, then the server's back). */
    public static void setTabListFooter(Object overlay, Object footer) {
        try {
            if (footerField == null) {
                footerField = field(net.minecraft.client.gui.GuiPlayerTabOverlay.class,
                        "footer", "field_175255_h", "h");
            }
            footerField.set(overlay, footer);
        } catch (Throwable ignored) {
            // left as the game has it
        }
    }

    // --- helpers -------------------------------------------------------------

    /** The first field present under any of {@code names}, made accessible. */
    private static Field field(Class<?> type, String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // try the next spelling
            }
        }
        throw new NoSuchFieldException(type.getName() + ": none of " + java.util.Arrays.toString(names));
    }

    /** The first no-argument method present under any of {@code names}, made accessible. */
    private static Method method(Class<?> type, String... names) throws NoSuchMethodException {
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                // try the next spelling
            }
        }
        throw new NoSuchMethodException(type.getName() + ": none of " + java.util.Arrays.toString(names));
    }
}
