/*
 * Decompiled with CFR 0.152.
 */
package com.alan.clients.component.impl.player;

import com.alan.clients.component.Component;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.inventory.SyncCurrentItemEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.ui.theme.Themes;
import com.alan.clients.util.animation.Animation;
import com.alan.clients.util.animation.Easing;
import com.alan.clients.util.font.Font;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.font.FontWeight;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.shader.ShaderQueueType;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class SlotComponent
extends Component {
    private static final Animation animation = new Animation(Easing.EASE_OUT_EXPO, 900L);
    private static boolean fX = true;
    public static boolean fY = true;
    public static boolean dj;
    private static boolean fZ;
    private static ItemStack itemStack;
    private static boolean gb;
    private static String stackSize;
    private static double gd;
    private final Font ge = FontManager.MAIN.a(18, FontWeight.LIGHT);
    private final Font gf = FontManager.MAIN.a(18, FontWeight.MEDIUM);
    @EventLink(value=4)
    public final Listener<SyncCurrentItemEvent> onSyncItem = var0 -> {
        InventoryPlayer inventoryplayer = SlotComponent.aEg.thePlayer.inventory;
        var0.setSlot(inventoryplayer.cIU ? inventoryplayer.cIT : inventoryplayer.currentItem);
    };
    @EventLink(value=4)
    public final Listener<PreUpdateEvent> onPreUpdate = var0 -> {
        InventoryPlayer inventoryplayer = SlotComponent.aEg.thePlayer.inventory;
        inventoryplayer.cIU = false;
        inventoryplayer.cIT = inventoryplayer.currentItem;
    };
    @EventLink(value=4)
    public final Listener<Render2DEvent> onRender2D = var1 -> {
        if (!fY && SlotComponent.aEg.thePlayer != null && fX) {
            boolean flag1;
            boolean flag;
            ItemStack itemstack = SlotComponent.getItemStack();
            ItemStack itemstack1 = itemstack != null && SlotComponent.aEg.thePlayer.inventory.cIU ? itemstack : null;
            boolean bl = flag = itemstack1 != null && itemstack1.stackSize > 0 && SlotComponent.aEg.thePlayer.inventory.cIU && (SlotComponent.aEg.thePlayer.inventory.cIT != SlotComponent.aEg.thePlayer.inventory.currentItem || SlotComponent.getItemStack() == null || SlotComponent.getItemStack() != null && SlotComponent.getItemStack().getItem() instanceof ItemBlock) && SlotComponent.aEg.currentScreen == null;
            if (flag != fZ) {
                animation.setValue(flag ? 1.1 : 1.0);
            }
            fZ = flag;
            if (flag && itemstack1 != null) {
                itemStack = itemstack1;
                gb = itemstack1.getItem() instanceof ItemBlock;
                stackSize = gb ? String.valueOf(SlotComponent.bN()) : String.valueOf(itemstack1.stackSize);
                gd = this.a(gb, stackSize);
            }
            animation.setDuration(900L);
            animation.Q(flag ? 1.0 : 1.1);
            double d0 = animation.getValue();
            double d1 = 1.0 - 10.0 * Math.abs(1.0 - d0);
            double d2 = Math.max(0.0, Math.min(1.0, d1));
            boolean bl2 = flag1 = flag ? itemstack1.getItem() instanceof ItemBlock : gb;
            String s = flag ? (flag1 ? String.valueOf(SlotComponent.bN()) : String.valueOf(itemstack1.stackSize)) : stackSize;
            ItemStack itemstack2 = flag ? itemstack1 : itemStack;
            double d3 = flag ? this.a(flag1, s) : gd;
            ScaledResolution scaledresolution = var1.getScaledResolution();
            double d4 = (double)scaledresolution.getScaledWidth() / 2.0 - d3 / 2.0;
            double d5 = scaledresolution.getScaledHeight() - 90;
            double d6 = d5 + 11.0 - (double)this.ge.height() / 2.0 + 3.0;
            if (d2 > 0.01 && itemstack2 != null) {
                if (d2 > 0.15) {
                    this.b(ShaderQueueType.BLUR).c(() -> {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate((double)((d4 + d3 * 0.5) * (1.0 - d0)), (double)((d5 + 11.0) * (1.0 - d0)), (double)0.0);
                        GlStateManager.scale((double)d0, (double)d0, (double)d0);
                        RenderUtil.roundedRectangle((double)d4, (double)d5, (double)d3, (double)22.0, (double)this.rz().getRound(), (Color)Color.BLACK);
                        GlStateManager.popMatrix();
                    });
                }
                double d7 = d2;
                this.b(ShaderQueueType.REGULAR, 1).c(() -> {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((double)((d4 + d3 * 0.5) * (1.0 - d0)), (double)((d5 + 11.0) * (1.0 - d0)), (double)0.0);
                    GlStateManager.scale((double)d0, (double)d0, (double)d0);
                    double d9 = this.rz().getRound();
                    this.rz();
                    Color color = Themes.rK();
                    this.rz();
                    RenderUtil.roundedRectangle((double)d4, (double)d5, (double)d3, (double)22.0, (double)d9, (Color)ColorUtil.withAlpha((Color)color, (int)((int)((double)Themes.rK().getAlpha() * d7))));
                    if (flag1) {
                        float f = this.ge.getStringWidth("Amount:") + 2;
                        this.ge.b("Amount:", d4 + 6.0 + 16.0, d6, ColorUtil.withAlpha((Color)Color.WHITE, (int)((int)(255.0 * d7))).getRGB());
                        this.gf.b(s, d4 + 6.0 + 16.0 + (double)f, d6, ColorUtil.withAlpha((Color)this.rz().rA(), (int)((int)(255.0 * d7))).getRGB());
                    }
                    RenderUtil.a((double)(d4 + 3.0), (double)(d5 + 3.0), (int)ColorUtil.withAlpha((Color)Color.WHITE, (int)((int)(255.0 * d7))).getRGB(), (ItemStack)itemstack2);
                    GlStateManager.popMatrix();
                });
                double d8 = d2;
                this.b(ShaderQueueType.BLOOM).c(() -> {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((double)((d4 + d3 * 0.5) * (1.0 - d0)), (double)((d5 + 11.0) * (1.0 - d0)), (double)0.0);
                    GlStateManager.scale((double)d0, (double)d0, (double)0.0);
                    RenderUtil.roundedRectangle((double)(d4 + 0.5), (double)(d5 + 0.5), (double)(d3 - 1.0), (double)21.0, (double)(this.rz().getRound() + 1), (Color)ColorUtil.withAlpha((Color)this.rz().rE(), (int)((int)((double)this.rz().rE().getAlpha() * d8))));
                    GlStateManager.popMatrix();
                });
            }
            if (!flag && animation.isFinished() && d0 >= 1.099) {
                fY = true;
            }
        }
    };

    public static void setSlot(int slot) {
        SlotComponent.b(slot, true);
    }

    public static void b(int var0, boolean var1) {
        if (var0 >= 0 && var0 < 9) {
            dj = true;
            InventoryPlayer inventoryplayer = SlotComponent.aEg.thePlayer.inventory;
            inventoryplayer.cIT = var0;
            inventoryplayer.cIU = true;
            fX = var1;
            fY = false;
            animation.setValue(1.1);
            SlotComponent.aEg.playerController.syncCurrentPlayItem();
            dj = false;
        }
    }

    public static void c(int var0, boolean var1) {
        SlotComponent.a(var0, var1, true);
    }

    public static void a(int var0, boolean var1, boolean var2) {
        if (!(Math.random() * Math.random() > 0.25) && !var1) {
            SlotComponent.b(var0, var2);
        } else {
            SlotComponent.b(SlotComponent.aEg.playerController.bCP, var2);
        }
    }

    private double a(boolean var1, String var2) {
        float f = this.ge.getStringWidth("Amount:") + 2;
        return 22.0f + (var1 ? f + (float)this.gf.getStringWidth(var2) + 2.0f + 3.0f : 0.0f);
    }

    private static int bN() {
        if (SlotComponent.aEg.thePlayer == null) {
            return 0;
        }
        int i = 0;
        InventoryPlayer inventoryplayer = SlotComponent.aEg.thePlayer.inventory;
        for (int j = 0; j < 9; ++j) {
            ItemStack itemstack = inventoryplayer.mainInventory[j];
            if (itemstack == null || !(itemstack.getItem() instanceof ItemBlock)) continue;
            i += itemstack.stackSize;
        }
        return i;
    }

    public static ItemStack getItemStack() {
        return SlotComponent.aEg.thePlayer != null && SlotComponent.aEg.thePlayer.inventoryContainer != null ? SlotComponent.aEg.thePlayer.inventoryContainer.getSlot(SlotComponent.bQ() + 36).getStack() : null;
    }

    public static Item bP() {
        ItemStack itemstack = SlotComponent.getItemStack();
        return itemstack == null ? null : itemstack.getItem();
    }

    public static int bQ() {
        InventoryPlayer inventoryplayer = SlotComponent.aEg.thePlayer.inventory;
        return inventoryplayer.cIU ? inventoryplayer.cIT : inventoryplayer.currentItem;
    }

    static {
        fZ = false;
        itemStack = null;
        gb = false;
        stackSize = "0";
        gd = 38.0;
    }
}
