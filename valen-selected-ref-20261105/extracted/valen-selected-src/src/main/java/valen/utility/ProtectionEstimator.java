/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemArmor
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.potion.Potion
 *  net.minecraft.world.World
 *  valen.utility.MeleeDamage
 *  valen.utility.ProtectionEstimator$Estimate
 *  valen.utility.ProtectionEstimator$Observation
 */
package valen.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import valen.event.DispatchPacketEvent;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.utility.IMinecraftInstance;
import valen.utility.MeleeDamage;
import valen.utility.ProtectionEstimator;
import valen.utility.Utils;

public final class ProtectionEstimator
implements IMinecraftInstance {
    public static final int UNKNOWN = -1;
    private static final int ALL_LEVELS = 31;
    private static final Map<EntityPlayer, Estimate> estimates = new HashMap<EntityPlayer, Estimate>();
    private static Object world;

    public static int estimateProtectionLevel(double d, double d2, int n, int n2, int n3, boolean bl, double d3) {
        if (!Double.isFinite(d) || !Double.isFinite(d2) || d <= 0.0 || d2 <= 0.0 || n2 <= 0 || n2 > 4 || d3 < 0.0) {
            return 0;
        }
        int n4 = 0;
        block0: for (int i = 0; i <= 4; ++i) {
            int n5 = MeleeDamage.uniformEpf((int)i, (int)n2);
            for (int j = n5 + 1 >> 1; j <= n5; ++j) {
                double d4 = MeleeDamage.mitigated((double)d2, (int)n, (int)n3, (boolean)bl, (int)j);
                if (!(Math.abs(d4 - d) <= d3)) continue;
                n4 |= 1 << i;
                continue block0;
            }
        }
        return n4;
    }

    public static int uniqueLevel(int n) {
        return Integer.bitCount(n & 0x1F) == 1 ? Integer.numberOfTrailingZeros(n) : -1;
    }

    public static boolean cleanHitWindow(int n, int n2) {
        return n2 > 0 && n <= n2 / 2;
    }

    public static int getProtectionLevel(EntityPlayer entityPlayer) {
        Estimate estimate = ProtectionEstimator.current(entityPlayer, false);
        return estimate == null || estimate.samples < 2 ? -1 : ProtectionEstimator.uniqueLevel(estimate.candidates);
    }

    public static double[] getDamageRange(EntityPlayer entityPlayer, double d) {
        int n = entityPlayer.getTotalArmorValue();
        int n2 = ProtectionEstimator.effect(entityPlayer, Potion.resistance);
        Estimate estimate = ProtectionEstimator.current(entityPlayer, false);
        int n3 = estimate == null || estimate.samples < 2 ? 31 : estimate.candidates;
        double d2 = Double.POSITIVE_INFINITY;
        double d3 = 0.0;
        if (!ProtectionEstimator.uniformContext(entityPlayer)) {
            return new double[]{MeleeDamage.mitigated((double)d, (int)n, (int)n2, (boolean)entityPlayer.isBlocking(), (int)20), MeleeDamage.mitigated((double)d, (int)n, (int)n2, (boolean)entityPlayer.isBlocking(), (int)0)};
        }
        for (int i = 0; i <= 4; ++i) {
            if ((n3 & 1 << i) == 0) continue;
            double[] dArray = MeleeDamage.range((double)d, (int)n, (int)ProtectionEstimator.pieces(entityPlayer), (int)i, (int)n2, (boolean)entityPlayer.isBlocking());
            d2 = Math.min(d2, dArray[0]);
            d3 = Math.max(d3, dArray[1]);
        }
        return new double[]{Double.isFinite(d2) ? d2 : 0.0, d3};
    }

    public static boolean isLethal(EntityPlayer entityPlayer, double d) {
        if (entityPlayer == null || !Double.isFinite(d) || d <= 0.0) {
            return false;
        }
        return ProtectionEstimator.isLethal(entityPlayer.getHealth(), entityPlayer.getAbsorptionAmount(), ProtectionEstimator.getDamageRange(entityPlayer, d)[0]);
    }

    public static boolean isLethal(double d, double d2, double d3) {
        return Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && d >= 0.0 && d2 >= 0.0 && d3 >= d + d2;
    }

    private static boolean uniformContext(EntityPlayer entityPlayer) {
        if (!Utils.nullCheck() || ProtectionEstimator.pieces(entityPlayer) != 4) {
            return false;
        }
        int n = -1;
        for (ItemStack itemStack : entityPlayer.inventory.armorInventory) {
            if (!(itemStack.getItem() instanceof ItemArmor) || itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("AttributeModifiers")) {
                return false;
            }
            int n2 = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.protection.effectId, (ItemStack)itemStack);
            if (n2 > 4 || n >= 0 && n != n2) {
                return false;
            }
            n = n2;
        }
        return true;
    }

    private static int pieces(EntityPlayer entityPlayer) {
        int n = 0;
        for (ItemStack itemStack : entityPlayer.inventory.armorInventory) {
            if (itemStack == null || !(itemStack.getItem() instanceof ItemArmor)) continue;
            ++n;
        }
        return n;
    }

    private static int effect(EntityPlayer entityPlayer, Potion potion) {
        return entityPlayer.isPotionActive(potion) ? entityPlayer.getActivePotionEffect(potion).getAmplifier() + 1 : 0;
    }

    private static String armorKey(EntityPlayer entityPlayer) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ItemStack itemStack : entityPlayer.inventory.armorInventory) {
            stringBuilder.append(itemStack == null ? "empty" : Item.getIdFromItem((Item)itemStack.getItem()) + ":" + itemStack.getTagCompound()).append(';');
        }
        return stringBuilder.toString();
    }

    private static Estimate current(EntityPlayer entityPlayer, boolean bl) {
        if (world != ProtectionEstimator.mc.theWorld) {
            estimates.clear();
            world = ProtectionEstimator.mc.theWorld;
        }
        if (entityPlayer == null || entityPlayer.worldObj != world || entityPlayer.isDead || ProtectionEstimator.mc.theWorld == null || ProtectionEstimator.mc.theWorld.getEntityByID(entityPlayer.getEntityId()) != entityPlayer) {
            estimates.remove(entityPlayer);
            return null;
        }
        String string = ProtectionEstimator.armorKey(entityPlayer);
        Estimate estimate = estimates.get(entityPlayer);
        if (estimate != null && !estimate.armor.equals(string) && estimate.pending == null) {
            estimates.remove(entityPlayer);
            estimate = null;
        }
        if (estimate == null && bl) {
            estimate = new Estimate(string);
            estimates.put(entityPlayer, estimate);
        }
        return estimate;
    }

    @SubscribeEvent
    public void onAttackPacket(DispatchPacketEvent dispatchPacketEvent) {
        if (!(Utils.nullCheck() && mc.isCallingFromMinecraftThread() && dispatchPacketEvent.getPacket() instanceof C02PacketUseEntity)) {
            return;
        }
        C02PacketUseEntity c02PacketUseEntity = (C02PacketUseEntity)dispatchPacketEvent.getPacket();
        if (c02PacketUseEntity.getAction() != C02PacketUseEntity.Action.ATTACK) {
            return;
        }
        Entity entity = c02PacketUseEntity.getEntityFromWorld((World)ProtectionEstimator.mc.theWorld);
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer entityPlayer = (EntityPlayer)entity;
        Estimate estimate = ProtectionEstimator.current(entityPlayer, true);
        if (estimate == null) {
            return;
        }
        ItemStack itemStack = ProtectionEstimator.mc.thePlayer.getHeldItem();
        Observation observation = new Observation(entityPlayer, itemStack == null ? null : itemStack.copy());
        if (estimate.pending == null) {
            estimate.pending = observation;
        } else {
            estimate.pending.overlap(observation);
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent renderTickEvent) {
        if (world != ProtectionEstimator.mc.theWorld || !Utils.nullCheck()) {
            estimates.clear();
            world = ProtectionEstimator.mc.theWorld;
            return;
        }
        long l = System.nanoTime();
        Iterator<Map.Entry<EntityPlayer, Estimate>> iterator = estimates.entrySet().iterator();
        while (iterator.hasNext()) {
            int n;
            boolean bl;
            Map.Entry<EntityPlayer, Estimate> entry = iterator.next();
            EntityPlayer entityPlayer = entry.getKey();
            Estimate estimate = entry.getValue();
            if (entityPlayer.isDead || ProtectionEstimator.mc.theWorld.getEntityByID(entityPlayer.getEntityId()) != entityPlayer) {
                iterator.remove();
                continue;
            }
            Observation observation = estimate.pending;
            boolean bl2 = bl = !estimate.armor.equals(ProtectionEstimator.armorKey(entityPlayer));
            if (bl && observation == null) {
                iterator.remove();
                continue;
            }
            if (observation != null && l - observation.started > 1500000000L) {
                if (bl) {
                    iterator.remove();
                    continue;
                }
                estimate.pending = null;
                continue;
            }
            if (observation == null) continue;
            if (entityPlayer.hurtTime > observation.lastHurtTime) {
                observation.hurtConfirmed = true;
            }
            observation.lastHurtTime = entityPlayer.hurtTime;
            double d = entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount();
            if (Math.abs(d - observation.lastTotal) > 0.001) {
                observation.changed = l;
                observation.lastTotal = d;
                observation.minimumTotal = Math.min(observation.minimumTotal, d);
            }
            if (observation.changed == 0L || l - observation.changed < 100000000L) continue;
            double d2 = observation.health + observation.absorption - observation.minimumTotal;
            if (d2 <= 0.0) {
                observation.changed = 0L;
                continue;
            }
            int n2 = n = observation.reliable && observation.hurtConfirmed && observation.uniform ? ProtectionEstimator.estimateProtectionLevel(d2, observation.raw, observation.armor, observation.pieces, observation.resistance, observation.blocking, 0.06) : 0;
            if (n != 0) {
                int n3 = estimate.candidates & n;
                if (n3 == 0) {
                    estimate.candidates = 31;
                    estimate.samples = 0;
                } else {
                    estimate.candidates = n3;
                    ++estimate.samples;
                }
            }
            if (bl) {
                iterator.remove();
                continue;
            }
            estimate.pending = null;
        }
    }

    private static String formatEstimate(int n) {
        int n2 = n & 0x1F;
        if (n2 == 0) {
            return null;
        }
        int n3 = ProtectionEstimator.uniqueLevel(n2);
        if (n3 != -1) {
            return String.valueOf(n3);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <= 4; ++i) {
            if ((n2 & 1 << i) == 0) continue;
            if (stringBuilder.length() > 0) {
                stringBuilder.append('/');
            }
            stringBuilder.append(i);
        }
        return stringBuilder.append('?').toString();
    }

    static /* synthetic */ int access$000(EntityPlayer entityPlayer) {
        return ProtectionEstimator.pieces(entityPlayer);
    }

    static /* synthetic */ int access$100(EntityPlayer entityPlayer, Potion potion) {
        return ProtectionEstimator.effect(entityPlayer, potion);
    }

    static /* synthetic */ boolean access$200(EntityPlayer entityPlayer) {
        return ProtectionEstimator.uniformContext(entityPlayer);
    }
}

