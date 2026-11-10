/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonObject
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockAir
 *  net.minecraft.block.BlockBanner
 *  net.minecraft.block.BlockBeacon
 *  net.minecraft.block.BlockButton
 *  net.minecraft.block.BlockCactus
 *  net.minecraft.block.BlockCarpet
 *  net.minecraft.block.BlockClay
 *  net.minecraft.block.BlockDaylightDetector
 *  net.minecraft.block.BlockDoublePlant
 *  net.minecraft.block.BlockEndPortal
 *  net.minecraft.block.BlockEndPortalFrame
 *  net.minecraft.block.BlockFence
 *  net.minecraft.block.BlockFlower
 *  net.minecraft.block.BlockFlowerPot
 *  net.minecraft.block.BlockGravel
 *  net.minecraft.block.BlockLadder
 *  net.minecraft.block.BlockLever
 *  net.minecraft.block.BlockLilyPad
 *  net.minecraft.block.BlockLiquid
 *  net.minecraft.block.BlockPane
 *  net.minecraft.block.BlockRailBase
 *  net.minecraft.block.BlockRedstoneTorch
 *  net.minecraft.block.BlockSand
 *  net.minecraft.block.BlockSapling
 *  net.minecraft.block.BlockSign
 *  net.minecraft.block.BlockSkull
 *  net.minecraft.block.BlockSlab
 *  net.minecraft.block.BlockSnow
 *  net.minecraft.block.BlockSoulSand
 *  net.minecraft.block.BlockStainedGlassPane
 *  net.minecraft.block.BlockStairs
 *  net.minecraft.block.BlockTallGrass
 *  net.minecraft.block.BlockTorch
 *  net.minecraft.block.BlockTripWire
 *  net.minecraft.block.BlockTripWireHook
 *  net.minecraft.block.BlockWeb
 *  net.minecraft.client.entity.EntityOtherPlayerMP
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.inventory.GuiInventory
 *  net.minecraft.client.network.NetHandlerPlayClient
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.client.renderer.ActiveRenderInfo
 *  net.minecraft.client.settings.KeyBinding
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.InventoryPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.inventory.ContainerPlayer
 *  net.minecraft.item.ItemArmor
 *  net.minecraft.item.ItemAxe
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.item.ItemBow
 *  net.minecraft.item.ItemFireball
 *  net.minecraft.item.ItemFood
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ItemSword
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer$C05PacketPlayerLook
 *  net.minecraft.network.play.client.C0APacketAnimation
 *  net.minecraft.potion.Potion
 *  net.minecraft.scoreboard.Score
 *  net.minecraft.scoreboard.ScoreObjective
 *  net.minecraft.scoreboard.ScorePlayerTeam
 *  net.minecraft.scoreboard.Scoreboard
 *  net.minecraft.scoreboard.Team
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.IChatComponent
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.StringUtils
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 *  valen.module.impl.minigames.DuelsStats
 *  valen.module.impl.player.Freecam
 *  valen.utility.ItemSortScoring
 *  valen.utility.color.ColorConstants
 */
package valen.utility;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import valen.Valen;
import valen.helper.MouseHelper;
import valen.lunar.VanillaHooks;
import valen.mixin.impl.accessor.IAccessorGuiIngame;
import valen.mixin.impl.accessor.IAccessorItemFood;
import valen.mixin.impl.accessor.IAccessorMinecraft;
import valen.mixin.impl.accessor.IAccessorPlayerControllerMP;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.module.impl.client.Settings;
import valen.module.impl.combat.Weapons;
import valen.module.impl.minigames.DuelsStats;
import valen.module.impl.player.Freecam;
import valen.module.impl.world.AntiBot;
import valen.module.setting.impl.SliderSetting;
import valen.utility.BlockUtils;
import valen.utility.IMinecraftInstance;
import valen.utility.ItemSortScoring;
import valen.utility.RotationUtils;
import valen.utility.color.ColorConstants;

public class Utils
implements IMinecraftInstance {
    private static final Random rand = new Random();
    private static final ThreadLocal<Integer> LOCAL_PLAYER_SUB_UPDATE_DEPTH = ThreadLocal.withInitial(() -> 0);
    public static HashSet<String> friends = new HashSet();
    public static HashSet<String> enemies = new HashSet();
    public static final Logger log = LogManager.getLogger();

    public static boolean addEnemy(String string) {
        if (Valen.playerRelationsManager != null) {
            return Valen.playerRelationsManager.addEnemy(string);
        }
        if (enemies.add(string.toLowerCase())) {
            Utils.sendMessage("&7Added enemy&7: &b" + string);
            return true;
        }
        return false;
    }

    public static boolean removeEnemy(String string) {
        if (Valen.playerRelationsManager != null) {
            return Valen.playerRelationsManager.removeEnemy(string);
        }
        if (enemies.remove(string.toLowerCase())) {
            Utils.sendMessage("&7Removed enemy&7: &b" + string);
            return true;
        }
        return false;
    }

    public static float getCameraYaw() {
        return (float)Math.toDegrees(Math.atan2(ActiveRenderInfo.getRotationZ(), ActiveRenderInfo.getRotationX()));
    }

    public static float getCameraPitch() {
        return (float)Math.toDegrees(Math.acos(ActiveRenderInfo.getRotationXZ()));
    }

    public static Vec3 getCameraPos(double d) {
        if (Utils.mc.gameSettings.thirdPersonView == 0) {
            Vec3 vec3 = new Vec3(Utils.mc.thePlayer.posX, Utils.mc.thePlayer.posY + (double)Utils.mc.thePlayer.getEyeHeight(), Utils.mc.thePlayer.posZ);
            return vec3;
        }
        float f = 4.0f;
        if (ModuleManager.extendCamera != null && ModuleManager.extendCamera.isEnabled()) {
            f = (float)ModuleManager.extendCamera.distance.getInput();
        }
        Entity entity = mc.getRenderViewEntity();
        float f2 = entity.getEyeHeight();
        double d2 = entity.prevPosX + (entity.posX - entity.prevPosX) * d;
        double d3 = entity.prevPosY + (entity.posY - entity.prevPosY) * d + (double)f2;
        double d4 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * d;
        double d5 = f;
        float f3 = Utils.getCameraYaw();
        float f4 = Utils.getCameraPitch();
        double d6 = (double)(-MathHelper.sin((float)(f3 / 180.0f * (float)Math.PI)) * MathHelper.cos((float)(f4 / 180.0f * (float)Math.PI))) * d5;
        double d7 = (double)(MathHelper.cos((float)(f3 / 180.0f * (float)Math.PI)) * MathHelper.cos((float)(f4 / 180.0f * (float)Math.PI))) * d5;
        double d8 = (double)(-MathHelper.sin((float)(f4 / 180.0f * (float)Math.PI))) * d5;
        if (ModuleManager.noCameraClip == null || !ModuleManager.noCameraClip.isEnabled()) {
            for (int i = 0; i < 8; ++i) {
                double d9;
                float f5 = (float)((i & 1) * 2 - 1) * 0.1f;
                float f6 = (float)((i >> 1 & 1) * 2 - 1) * 0.1f;
                float f7 = (float)((i >> 2 & 1) * 2 - 1) * 0.1f;
                MovingObjectPosition movingObjectPosition = Utils.mc.theWorld.rayTraceBlocks(new Vec3(d2 + (double)f5, d3 + (double)f6, d4 + (double)f7), new Vec3(d2 - d6 + (double)f5 + (double)f7, d3 - d8 + (double)f6, d4 - d7 + (double)f7));
                if (movingObjectPosition == null || !((d9 = movingObjectPosition.hitVec.distanceTo(new Vec3(d2, d3, d4))) < d5)) continue;
                d5 = d9;
            }
        }
        double d10 = d2 - d6 * (d5 / (double)f);
        double d11 = d3 - d8 * (d5 / (double)f);
        double d12 = d4 - d7 * (d5 / (double)f);
        return new Vec3(d10, d11, d12);
    }

    public static void printInfo(EntityLivingBase entityLivingBase) {
        boolean bl;
        if (entityLivingBase == null) {
            return;
        }
        Utils.sendMessage("&7&m-------------------------");
        Utils.sendMessage("&eattacking: &r" + entityLivingBase.getName());
        Utils.sendMessage("&7type: &b" + entityLivingBase.getClass().getSimpleName());
        Utils.sendMessage("&7bot: &r" + (ModuleManager.antiBot != null && ModuleManager.antiBot.isEnabled() ? Boolean.valueOf(AntiBot.isBot((Entity)entityLivingBase)) : "&cantibot disabled"));
        boolean bl2 = entityLivingBase instanceof EntityPlayer;
        Utils.sendMessage("&7player: &r" + bl2);
        Utils.sendMessage("&7dist eye: &d" + Utils.round(Utils.getDistanceToEye((Entity)entityLivingBase), 2));
        Utils.sendMessage("&7min dist: &d" + Utils.round(Math.sqrt(Utils.raycastDistanceSq((Entity)entityLivingBase, 12.0, false)), 2));
        IChatComponent iChatComponent = entityLivingBase.getDisplayName();
        boolean bl3 = bl = iChatComponent != null;
        if (bl2) {
            EntityPlayer entityPlayer = (EntityPlayer)entityLivingBase;
            UUID uUID = entityPlayer.getUniqueID();
            Utils.sendMessage("&7uuid: &d" + uUID.toString() + " &b" + uUID.variant() + " " + uUID.version());
            NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID());
            Utils.sendMessage("&7ping: &d" + (networkPlayerInfo == null ? "&cnot found" : Integer.valueOf(networkPlayerInfo.getResponseTime())));
            Utils.sendMessage("&7teammate: &r" + Utils.isTeammate((Entity)entityPlayer));
            Utils.sendMessage("&7tablist: &r" + Utils.isInTabList(entityPlayer));
            if (entityPlayer.getTeam() != null) {
                ScorePlayerTeam scorePlayerTeam = (ScorePlayerTeam)entityPlayer.getTeam();
                Utils.sendMessage("&7team name: &r" + scorePlayerTeam.getTeamName());
                Utils.sendMessage("&7team prefix: &r" + scorePlayerTeam.getColorPrefix());
                Utils.sendMessage("&7team suffix: &r" + scorePlayerTeam.getColorSuffix());
            }
        }
        Utils.sendMessage("&7display unformatted: &r" + (bl ? iChatComponent.getUnformattedText() : "&cnull"));
        Utils.sendMessage("&7insertion: &r" + (bl ? iChatComponent.getChatStyle().getInsertion() : "&cnull"));
        Utils.sendMessage("&7health: &r" + entityLivingBase.getHealth());
        Utils.sendMessage("&7ht: &d" + entityLivingBase.hurtTime + " &7mht: &d" + entityLivingBase.maxHurtTime);
        Utils.sendMessage("&7ticks existed: &r" + entityLivingBase.ticksExisted);
        Utils.sendMessage("&7invisible: &r" + entityLivingBase.isInvisible());
        Utils.sendMessage("&7dead: &r" + entityLivingBase.isDead);
    }

    public static double raycastDistanceSq(Entity entity, double d, boolean bl) {
        float f;
        float f2;
        Vec3 vec3 = Utils.mc.thePlayer.getPositionEyes(1.0f);
        if (bl) {
            float[] fArray = RotationUtils.getRotations(entity);
            f2 = fArray[0];
            f = fArray[1];
        } else {
            f2 = Utils.mc.thePlayer.rotationYaw;
            f = Utils.mc.thePlayer.rotationPitch;
        }
        float f3 = MathHelper.cos((float)(-f2 * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = MathHelper.sin((float)(-f2 * ((float)Math.PI / 180) - (float)Math.PI));
        float f5 = -MathHelper.cos((float)(-f * ((float)Math.PI / 180)));
        float f6 = MathHelper.sin((float)(-f * ((float)Math.PI / 180)));
        Vec3 vec32 = new Vec3((double)(f4 * f5), (double)f6, (double)(f3 * f5));
        double d2 = vec32.xCoord * d;
        double d3 = vec32.yCoord * d;
        double d4 = vec32.zCoord * d;
        Vec3 vec33 = vec3.addVector(d2, d3, d4);
        List list = Utils.mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(d2, d3, d4).expand(1.0, 1.0, 1.0));
        for (int i = 0; i < list.size(); ++i) {
            Entity entity2 = (Entity)list.get(i);
            if (entity2 != entity || !entity2.canBeCollidedWith()) continue;
            float f7 = entity2.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB = entity2.getEntityBoundingBox().expand((double)f7, (double)f7, (double)f7);
            MovingObjectPosition movingObjectPosition = axisAlignedBB.calculateIntercept(vec3, vec33);
            if (movingObjectPosition == null) continue;
            return vec3.squareDistanceTo(movingObjectPosition.hitVec);
        }
        return -1.0;
    }

    public static double getDistanceToEye(Entity entity) {
        return Utils.mc.thePlayer.getPositionEyes(1.0f).distanceTo(entity.getPositionEyes(1.0f));
    }

    public static boolean isInTabList(EntityPlayer entityPlayer) {
        for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
            if (!networkPlayerInfo.getGameProfile().equals((Object)entityPlayer.getGameProfile())) continue;
            return true;
        }
        return false;
    }

    public static String getServerName() {
        return DuelsStats.nick.isEmpty() ? Utils.mc.thePlayer.getName() : DuelsStats.nick;
    }

    public static boolean tabbedIn() {
        return Utils.mc.currentScreen == null && Utils.mc.inGameHasFocus;
    }

    public static boolean isConsuming(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        return ((EntityPlayer)entity).isUsingItem() && Utils.holdingFood((EntityLivingBase)((EntityPlayer)entity));
    }

    public static boolean holdingFood(EntityLivingBase entityLivingBase) {
        return entityLivingBase.getHeldItem() != null && entityLivingBase.getHeldItem().getItem() instanceof ItemFood;
    }

    public static int getColorFromEntity(Entity entity) {
        int n;
        String string;
        IChatComponent iChatComponent;
        if (entity == null) {
            return -1;
        }
        if (entity instanceof EntityPlayer && (iChatComponent = (ScorePlayerTeam)((EntityLivingBase)entity).getTeam()) != null && (string = FontRenderer.getFormatFromString((String)iChatComponent.getColorPrefix())).length() >= 2 && (n = Utils.getColorFromFormattingCode(string.charAt(1))) != -1) {
            return n;
        }
        iChatComponent = entity.getDisplayName();
        if (iChatComponent == null) {
            return -1;
        }
        string = Utils.removeFormatCodes(iChatComponent.getFormattedText());
        if (string.length() < 2 || !string.startsWith("\u00a7") || Character.toLowerCase(string.charAt(1)) == 'f') {
            return -1;
        }
        return Utils.getColorFromFormattingCode(string.charAt(1));
    }

    private static int getColorFromFormattingCode(char c) {
        switch (Character.toLowerCase(c)) {
            case '0': {
                return ColorConstants.BLACK;
            }
            case '1': {
                return ColorConstants.DARK_BLUE;
            }
            case '2': {
                return ColorConstants.DARK_GREEN;
            }
            case '3': {
                return ColorConstants.DARK_AQUA;
            }
            case '4': {
                return ColorConstants.DARK_RED;
            }
            case '5': {
                return ColorConstants.DARK_PURPLE;
            }
            case '6': {
                return ColorConstants.GOLD;
            }
            case '7': {
                return ColorConstants.GRAY;
            }
            case '8': {
                return ColorConstants.DARK_GRAY;
            }
            case '9': {
                return ColorConstants.BLUE;
            }
            case 'a': {
                return ColorConstants.GREEN;
            }
            case 'b': {
                return ColorConstants.AQUA;
            }
            case 'c': {
                return ColorConstants.RED;
            }
            case 'd': {
                return ColorConstants.LIGHT_PURPLE;
            }
            case 'e': {
                return ColorConstants.YELLOW;
            }
            case 'f': {
                return -1;
            }
        }
        return -1;
    }

    public static boolean overVoid(double d, double d2, double d3) {
        for (int i = (int)d2; i > -1; --i) {
            if (Utils.mc.theWorld.getBlockState(new BlockPos(d, (double)i, d3)).getBlock() instanceof BlockAir) continue;
            return false;
        }
        return true;
    }

    public static Block getBlockFromName(String string) {
        return (Block)Block.blockRegistry.getObject((Object)new ResourceLocation("minecraft:" + string));
    }

    public static boolean canPlayerBeSeen(EntityLivingBase entityLivingBase) {
        double d;
        double d2 = entityLivingBase.posX;
        double d3 = entityLivingBase.posY;
        double d4 = entityLivingBase.posZ;
        Vec3 vec3 = Utils.mc.thePlayer.getPositionEyes(1.0f);
        if (Utils.canSeeVec(vec3, new Vec3(d2 + 0.3, d = (double)entityLivingBase.getEyeHeight() - 0.2, d4))) {
            return true;
        }
        if (Utils.canSeeVec(vec3, new Vec3(d2 - 0.3, d, d4))) {
            return true;
        }
        if (Utils.canSeeVec(vec3, new Vec3(d2, d, d4 + 0.3))) {
            return true;
        }
        if (Utils.canSeeVec(vec3, new Vec3(d2, d, d4 - 0.3))) {
            return true;
        }
        for (double d5 = (double)entityLivingBase.getEyeHeight() + 0.2; d5 > 0.0; d5 -= 0.2) {
            Vec3 vec32 = new Vec3(d2, d3 + d5, d4);
            if (!Utils.canSeeVec(vec3, vec32)) continue;
            return true;
        }
        return false;
    }

    public static boolean holdingFireball() {
        if (Utils.mc.thePlayer.getHeldItem() == null) {
            return false;
        }
        return Utils.mc.thePlayer.getHeldItem().getItem() instanceof ItemFireball;
    }

    public static boolean canSeeVec(Vec3 vec3, Vec3 vec32) {
        MovingObjectPosition movingObjectPosition = Utils.mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, false);
        return movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
    }

    public static List<NetworkPlayerInfo> getTablist(boolean bl) {
        ArrayList<NetworkPlayerInfo> arrayList = new ArrayList<NetworkPlayerInfo>(mc.getNetHandler().getPlayerInfoMap());
        if (bl) {
            arrayList.remove(mc.getNetHandler().getPlayerInfo(Utils.mc.thePlayer.getUniqueID()));
        }
        return arrayList;
    }

    public static boolean removeFriend(String string) {
        if (Valen.playerRelationsManager != null) {
            return Valen.playerRelationsManager.removeFriend(string);
        }
        if (friends.remove(string.toLowerCase())) {
            Utils.sendMessage("&7Removed &afriend&7: &b" + string);
            return true;
        }
        return false;
    }

    public static boolean addFriend(String string) {
        if (Valen.playerRelationsManager != null) {
            return Valen.playerRelationsManager.addFriend(string);
        }
        if (friends.add(string.toLowerCase())) {
            Utils.sendMessage("&7Added &afriend&7: &b" + string);
            if (enemies.contains(string.toLowerCase())) {
                enemies.remove(string.toLowerCase());
            }
            return true;
        }
        return false;
    }

    public static boolean isWholeNumber(double d) {
        return d == Math.floor(d);
    }

    public static String asWholeNum(double d) {
        return Utils.isWholeNumber(d) ? (int)d + "" : String.valueOf(d);
    }

    public static int randomizeInt(int n, int n2) {
        return rand.nextInt(n2 - n + 1) + n;
    }

    public static double randomizeDouble(double d, double d2) {
        return d + (d2 - d) * rand.nextDouble();
    }

    public static boolean inFov(float f, BlockPos blockPos) {
        return Utils.inFov(f, blockPos.getX(), blockPos.getZ());
    }

    public static boolean inFov(float f, Entity entity) {
        return Utils.inFov(f, entity.posX, entity.posZ);
    }

    public static boolean inFov(float f, double d, double d2) {
        return Utils.inFov((Entity)Utils.mc.thePlayer, f, d, d2);
    }

    public static boolean inFov(Entity entity, float f, double d, double d2) {
        f = (float)((double)f * 0.5);
        double d3 = MathHelper.wrapAngleTo180_double((double)((entity.rotationYaw - RotationUtils.angle(d, d2)) % 360.0f));
        return d3 > 0.0 ? d3 < (double)f : d3 > (double)(-f);
    }

    public static boolean inFov(float f, float f2, float f3) {
        f2 *= 0.5f;
        double d = MathHelper.wrapAngleTo180_double((double)((f - f3) % 360.0f));
        if (d > 0.0) {
            return d < (double)f2;
        }
        return d > (double)(-f2);
    }

    public static Vec3 getLookVec(float f, float f2) {
        float f3 = MathHelper.cos((float)(-f * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = MathHelper.sin((float)(-f * ((float)Math.PI / 180) - (float)Math.PI));
        float f5 = -MathHelper.cos((float)(-f2 * ((float)Math.PI / 180)));
        float f6 = MathHelper.sin((float)(-f2 * ((float)Math.PI / 180)));
        return new Vec3((double)(f4 * f5), (double)f6, (double)(f3 * f5));
    }

    public static boolean holdingBow() {
        if (Utils.mc.thePlayer.getHeldItem() == null) {
            return false;
        }
        return Utils.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow;
    }

    public static boolean bowBackwards() {
        return Utils.holdingBow() && Utils.mc.thePlayer.moveStrafing == 0.0f && Utils.mc.thePlayer.moveForward <= 0.0f && Utils.isMoving();
    }

    public static boolean noSlowingBackWithBow() {
        return ModuleManager.noSlow != null && ModuleManager.noSlow.noSlowing && Utils.bowBackwards();
    }

    public static void sendMessage(String string) {
        if (Utils.nullCheck()) {
            String string2 = Utils.formatColor(string);
            Utils.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(string2));
        }
    }

    public static void sendMessageStr(String string) {
        if (Utils.nullCheck()) {
            String string2 = Utils.formatColor(string);
            Utils.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(string2));
        }
    }

    public static void sendMessage(Object object) {
        String string = String.valueOf(object);
        Utils.sendMessage(string);
    }

    public static void sendDebugMessage(String string) {
        if (Utils.nullCheck()) {
            Utils.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(Utils.formatColor(string)));
        }
    }

    public static void attackEntity(Entity entity, boolean bl, boolean bl2) {
        if (bl) {
            Utils.mc.thePlayer.swingItem();
        } else if (bl2 || !bl2 && !bl) {
            Utils.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0APacketAnimation());
        }
        Utils.mc.playerController.attackEntity((EntityPlayer)Utils.mc.thePlayer, entity);
    }

    public static void sendRawMessage(String string) {
        if (Utils.nullCheck()) {
            Utils.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(Utils.formatColor(string)));
        }
    }

    public static float getTotalHealth(EntityLivingBase entityLivingBase) {
        return entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
    }

    public static String getHealthStr(EntityLivingBase entityLivingBase, boolean bl) {
        float f = Utils.getTotalHealth(entityLivingBase);
        if (bl && entityLivingBase.isDead) {
            f = 0.0f;
        }
        return Utils.getColorForHealth(entityLivingBase.getHealth() / entityLivingBase.getMaxHealth(), f);
    }

    public static boolean isBindDown(KeyBinding keyBinding) {
        int n = keyBinding.getKeyCode();
        if (n < 0) {
            return Mouse.isButtonDown((int)(n + 100));
        }
        return Keyboard.isKeyDown((int)n);
    }

    public static int getTool(Block block) {
        double d = 1.0;
        int n = -1;
        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            double d2;
            ItemStack itemStack = Utils.mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack == null || !((d2 = ItemSortScoring.getBlockBreakingScore((ItemStack)itemStack, (Block)block)) > d)) continue;
            d = d2;
            n = i;
        }
        return n;
    }

    public static boolean onLadder(Entity entity) {
        int n;
        int n2;
        int n3 = MathHelper.floor_double((double)entity.posX);
        BlockPos blockPos = new BlockPos(n3, n2 = MathHelper.floor_double((double)(entity.posY - (double)0.2f)), n = MathHelper.floor_double((double)entity.posZ));
        Block block = Utils.mc.theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockLadder && !entity.onGround;
    }

    public static float getEfficiency(ItemStack itemStack, Block block) {
        int n;
        float f = itemStack.getStrVsBlock(block);
        if (f > 1.0f && (n = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.efficiency.effectId, (ItemStack)itemStack)) > 0) {
            f += (float)(n * n + 1);
        }
        return f;
    }

    public static boolean isEnemy(EntityPlayer entityPlayer) {
        return entityPlayer != null && Utils.isEnemy(entityPlayer.getName());
    }

    public static boolean isEnemy(String string) {
        if (ModuleManager.relationships == null || !ModuleManager.relationships.isEnabled()) {
            return false;
        }
        if (Valen.playerRelationsManager != null) {
            return Valen.playerRelationsManager.isEnemy(string);
        }
        return string != null && !enemies.isEmpty() && enemies.contains(string.toLowerCase());
    }

    public static String getColorForHealth(double d, double d2) {
        if (Settings.showHealthAsHearts.isToggled()) {
            d2 /= 2.0;
        }
        double d3 = Utils.round(d2, 1);
        String string = (d < 0.3 ? "\u00a7c" : (d < 0.5 ? "\u00a76" : (d < 0.7 ? "\u00a7e" : "\u00a7a"))) + Utils.asWholeNum(d3);
        if (Settings.showHeartSymbol.isToggled()) {
            string = string + "\u00a7c\u2764\u00a7r";
        }
        return string;
    }

    public static int getColorForHealth(double d) {
        return d < 0.3 ? -43691 : (d < 0.5 ? -22016 : (d < 0.7 ? -171 : -11141291));
    }

    public static String formatColor(String string) {
        return string.replaceAll("&", "\u00a7");
    }

    public static String getFirstColorCode(String string) {
        if (string == null || string.length() < 2) {
            return "";
        }
        for (int i = 0; i < string.length() - 1; ++i) {
            char c;
            if (string.charAt(i) != '\u00a7' || !((c = string.charAt(i + 1)) >= '0' && c <= '9' || c >= 'a' && c <= 'f') && (c < 'A' || c > 'F')) continue;
            return "\u00a7" + c;
        }
        return "";
    }

    public static int getBoldWidth(String string) {
        boolean bl = false;
        int n = 0;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\u00a7' && i + 1 < string.length()) {
                int n2 = "0123456789abcdefklmnor".indexOf(string.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                if (n2 == 17) {
                    bl = true;
                }
                ++i;
                continue;
            }
            if (!bl) continue;
            ++n;
        }
        return n;
    }

    public static void correctValue(SliderSetting sliderSetting, SliderSetting sliderSetting2) {
        if (sliderSetting.getInput() > sliderSetting2.getInput()) {
            double d = sliderSetting.getInput();
            sliderSetting.setValueWithEvent(sliderSetting2.getInput());
            sliderSetting2.setValueWithEvent(d);
        }
    }

    public static String generateRandomString(int n2) {
        char[] cArray = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        IntStream.range(0, n2).forEach(n -> stringBuilder.append(cArray[rand.nextInt(cArray.length)]));
        return stringBuilder.toString();
    }

    public static boolean isFriended(EntityPlayer entityPlayer) {
        return entityPlayer != null && Utils.isFriended(entityPlayer.getName());
    }

    public static boolean isFriended(String string) {
        if (Valen.playerRelationsManager != null) {
            return Valen.playerRelationsManager.isFriend(string);
        }
        return string != null && !friends.isEmpty() && friends.contains(string.toLowerCase());
    }

    public static double getRandomValue(SliderSetting sliderSetting, SliderSetting sliderSetting2, Random random) {
        return sliderSetting.getInput() == sliderSetting2.getInput() ? sliderSetting.getInput() : sliderSetting.getInput() + random.nextDouble() * (sliderSetting2.getInput() - sliderSetting.getInput());
    }

    public static boolean nullCheck() {
        return Utils.mc.thePlayer != null && Utils.mc.theWorld != null;
    }

    public static boolean isHypixel() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null && Utils.mc.getCurrentServerData().serverIP.contains("hypixel.net");
    }

    public static String getHitsToKillStr(EntityPlayer entityPlayer, ItemStack itemStack) {
        int n = (int)Math.ceil(Utils.getHitsToKill(entityPlayer, itemStack));
        return "\u00a7" + (n <= 1 ? "c" : (n <= 3 ? "6" : (n <= 5 ? "e" : "a"))) + n;
    }

    public static double getHitsToKill(EntityPlayer entityPlayer, ItemStack itemStack) {
        double d = 1.0;
        if (itemStack != null && (itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemAxe)) {
            d += Utils.getDamageLevel(itemStack);
        }
        double d2 = 0.0;
        double d3 = 0.0;
        for (int i = 0; i < 4; ++i) {
            ItemStack itemStack2 = entityPlayer.inventory.armorItemInSlot(i);
            if (itemStack2 == null || !(itemStack2.getItem() instanceof ItemArmor)) continue;
            d2 += (double)((ItemArmor)itemStack2.getItem()).damageReduceAmount * 0.04;
            int n = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.protection.effectId, (ItemStack)itemStack2);
            if (n == 0) continue;
            double d4 = Math.floor(0.75 * (double)(6 + n * n) / 3.0);
            d3 += d4;
        }
        d3 = 0.04 * Math.min(Math.ceil(Math.min(d3, 25.0) * 0.75), 20.0);
        double d5 = d2 + d3 * (1.0 - d2);
        double d6 = d * (1.0 - d5);
        double d7 = (double)Utils.getTotalHealth((EntityLivingBase)entityPlayer) / d6;
        return Utils.round(d7, 1);
    }

    public static float n() {
        return Utils.ae(Utils.mc.thePlayer.rotationYaw, Utils.mc.thePlayer.movementInput.moveForward, Utils.mc.thePlayer.movementInput.moveStrafe);
    }

    public static String extractFileName(String string) {
        int n = string.indexOf("_");
        int n2 = string.lastIndexOf("_");
        if (n != -1 && n2 != -1 && n2 > n) {
            return string.substring(n + 1, n2);
        }
        return string;
    }

    public static int mergeAlpha(int n, int n2) {
        return n & 0xFFFFFF | n2 << 24;
    }

    public static int clamp(int n) {
        if (n > 255) {
            return 255;
        }
        if (n < 4) {
            return 4;
        }
        return n;
    }

    public static boolean hasArrows(ItemStack itemStack) {
        boolean bl = Utils.mc.thePlayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel((int)Enchantment.infinity.effectId, (ItemStack)itemStack) > 0;
        return bl || Utils.mc.thePlayer.inventory.hasItem(Items.arrow);
    }

    public static int darkenColor(int n, double d) {
        int n2 = n >> 24 & 0xFF;
        int n3 = n >> 16 & 0xFF;
        int n4 = n >> 8 & 0xFF;
        int n5 = n & 0xFF;
        d = (100.0 - d) / 100.0;
        n3 = (int)((double)n3 * d);
        n4 = (int)((double)n4 * d);
        n5 = (int)((double)n5 * d);
        n3 = Utils.clamp(n3);
        n4 = Utils.clamp(n4);
        n5 = Utils.clamp(n5);
        int n6 = n2 << 24 | n3 << 16 | n4 << 8 | n5;
        return n6;
    }

    public static boolean isTeammate(Entity entity) {
        return ModuleManager.teams != null && ModuleManager.teams.isTeammate(entity);
    }

    public static String displayNameOf(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)entity;
            String string = ScorePlayerTeam.formatPlayerName((Team)entityPlayer.getTeam(), (String)entityPlayer.getName());
            return string == null ? "" : string;
        }
        IChatComponent iChatComponent = entity == null ? null : entity.getDisplayName();
        return iChatComponent == null ? "" : iChatComponent.getUnformattedText();
    }

    public static String getNetworkDisplayName() {
        if (Utils.mc.thePlayer == null) {
            return "";
        }
        NetHandlerPlayClient netHandlerPlayClient = mc.getNetHandler();
        if (netHandlerPlayClient == null) {
            return "";
        }
        NetworkPlayerInfo networkPlayerInfo = netHandlerPlayClient.getPlayerInfo(Utils.mc.thePlayer.getUniqueID());
        if (networkPlayerInfo == null || networkPlayerInfo.getGameProfile() == null) {
            return "";
        }
        String string = ScorePlayerTeam.formatPlayerName((Team)networkPlayerInfo.getPlayerTeam(), (String)networkPlayerInfo.getGameProfile().getName());
        return string == null ? "" : string;
    }

    public static void setSpeed(double d) {
        if (d == 0.0) {
            Utils.mc.thePlayer.motionZ = 0.0;
            Utils.mc.thePlayer.motionX = 0.0;
            return;
        }
        float f = Utils.n();
        Utils.mc.thePlayer.motionX = -Math.sin(f) * d;
        Utils.mc.thePlayer.motionZ = Math.cos(f) * d;
    }

    public static void resetTimer() {
        ((IAccessorMinecraft)Utils.mc).valen$lunar$getTimer().timerSpeed = 1.0f;
    }

    public static void beginLocalPlayerSubUpdate() {
        LOCAL_PLAYER_SUB_UPDATE_DEPTH.set(LOCAL_PLAYER_SUB_UPDATE_DEPTH.get() + 1);
    }

    public static void endLocalPlayerSubUpdate() {
        int n = LOCAL_PLAYER_SUB_UPDATE_DEPTH.get() - 1;
        LOCAL_PLAYER_SUB_UPDATE_DEPTH.set(Math.max(0, n));
    }

    public static boolean isLocalPlayerSubUpdate() {
        return LOCAL_PLAYER_SUB_UPDATE_DEPTH.get() > 0;
    }

    public static boolean inInventory() {
        if (!Utils.nullCheck()) {
            return false;
        }
        return Utils.mc.currentScreen != null && Utils.mc.thePlayer.inventoryContainer != null && Utils.mc.thePlayer.inventoryContainer instanceof ContainerPlayer && Utils.mc.currentScreen instanceof GuiInventory;
    }

    public static int getSkyWarsStatus() {
        List<String> list = Utils.getSidebarLines();
        if (list.isEmpty()) {
            return -1;
        }
        if (Utils.stripColor(list.get(0)).startsWith("SKYWARS")) {
            for (String string : list) {
                if ((string = Utils.stripColor(string)).equals("Waiting...") || string.startsWith("Starting in ")) {
                    return 1;
                }
                if (!string.startsWith("Players left: ")) continue;
                return 2;
            }
            return 0;
        }
        return -1;
    }

    public static String getString(JsonObject jsonObject, String string) {
        try {
            return jsonObject.get(string).getAsString();
        }
        catch (Exception exception) {
            return "";
        }
    }

    public static int getBedwarsStatus() {
        if (!Utils.nullCheck()) {
            return -1;
        }
        Scoreboard scoreboard = Utils.mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return -1;
        }
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreObjective == null || !Utils.stripString(scoreObjective.getDisplayName()).contains("BED WARS")) {
            return -1;
        }
        for (String string : Utils.getSidebarLines()) {
            String[] stringArray = (string = Utils.stripString(string)).split("  ");
            if (stringArray.length > 1) {
                if (!stringArray[1].startsWith("L")) continue;
                return 0;
            }
            if (string.equals("Waiting...") || string.startsWith("Starting in")) {
                return 1;
            }
            if (!string.startsWith("R Red:") && !string.startsWith("B Blue:")) continue;
            return 2;
        }
        return -1;
    }

    public static String stripString(String string) {
        char[] cArray = StringUtils.stripControlCodes((String)string).toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : cArray) {
            if (c >= '\u007f' || c <= '\u0014') continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static List<String> getSidebarLines() {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (Utils.mc.theWorld == null) {
            return arrayList;
        }
        Scoreboard scoreboard = Utils.mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return arrayList;
        }
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreObjective == null) {
            return arrayList;
        }
        ArrayList<Object> arrayList2 = scoreboard.getSortedScores(scoreObjective);
        ArrayList<Object> arrayList3 = new ArrayList<Object>();
        for (Object object : arrayList2) {
            if (object == null || object.getPlayerName() == null || object.getPlayerName().startsWith("#")) continue;
            arrayList3.add(object);
        }
        arrayList2 = arrayList3.size() > 15 ? new ArrayList(Lists.newArrayList((Iterable)Iterables.skip(arrayList3, (int)(arrayList3.size() - 15)))) : arrayList3;
        int n = 0;
        for (Score score : arrayList2) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
            arrayList.add(ScorePlayerTeam.formatPlayerName((Team)scorePlayerTeam, (String)score.getPlayerName()));
            if (++n != arrayList2.size()) continue;
            arrayList.add(scoreObjective.getDisplayName());
        }
        Collections.reverse(arrayList);
        return arrayList;
    }

    public static Random getRandom() {
        return rand;
    }

    public static boolean isMoving() {
        return Utils.mc.thePlayer.moveForward != 0.0f || Utils.mc.thePlayer.moveStrafing != 0.0f;
    }

    public static void aim(Entity entity, float f, boolean bl) {
        float[] fArray;
        if (entity != null && (fArray = Utils.getRotationsOld(entity)) != null) {
            float f2 = fArray[0];
            float f3 = fArray[1] + 4.0f + f;
            if (bl) {
                mc.getNetHandler().addToSendQueue((Packet)new C03PacketPlayer.C05PacketPlayerLook(f2, f3, Utils.mc.thePlayer.onGround));
            } else {
                Utils.mc.thePlayer.rotationYaw = f2;
                Utils.mc.thePlayer.rotationPitch = f3;
            }
        }
    }

    public static float[] getRotationsOld(Entity entity) {
        double d;
        if (entity == null) {
            return null;
        }
        double d2 = entity.posX - Utils.mc.thePlayer.posX;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
            d = entityLivingBase.posY + (double)entityLivingBase.getEyeHeight() * 0.9 - (Utils.mc.thePlayer.posY + (double)Utils.mc.thePlayer.getEyeHeight());
        } else {
            d = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (Utils.mc.thePlayer.posY + (double)Utils.mc.thePlayer.getEyeHeight());
        }
        double d3 = entity.posZ - Utils.mc.thePlayer.posZ;
        double d4 = MathHelper.sqrt_double((double)(d2 * d2 + d3 * d3));
        float f = (float)(Math.atan2(d3, d2) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float)(-(Math.atan2(d, d4) * 180.0 / Math.PI));
        return new float[]{Utils.mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(f - Utils.mc.thePlayer.rotationYaw)), Utils.mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(f2 - Utils.mc.thePlayer.rotationPitch))};
    }

    public static double aimDifference(Entity entity, boolean bl) {
        return ((double)((bl ? RotationUtils.serverRotations[0] : Utils.mc.thePlayer.rotationYaw) - Utils.getYaw(entity)) % 360.0 + 540.0) % 360.0 - 180.0;
    }

    public static double pitchDifference(Entity entity, boolean bl) {
        return ((double)((bl ? RotationUtils.serverRotations[1] : Utils.mc.thePlayer.rotationPitch) - Utils.getPitch(entity)) % 360.0 + 540.0) % 360.0 - 180.0;
    }

    public static float getYaw(Entity entity) {
        double d = entity.posX - Utils.mc.thePlayer.posX;
        double d2 = entity.posZ - Utils.mc.thePlayer.posZ;
        double d3 = Math.atan2(d, d2) * 57.29577951308232;
        return (float)(d3 * -1.0);
    }

    public static float getPitch(Entity entity) {
        double d = entity.posX - Utils.mc.thePlayer.posX;
        double d2 = entity.posZ - Utils.mc.thePlayer.posZ;
        double d3 = entity.posY + (double)(entity.getEyeHeight() / 2.0f) - (Utils.mc.thePlayer.posY + (double)Utils.mc.thePlayer.getEyeHeight());
        double d4 = Math.atan2(d3, Math.sqrt(d * d + d2 * d2)) * 57.29577951308232;
        return (float)(d4 * -1.0);
    }

    public static void switchSlot(int n, boolean bl) {
        Utils.mc.thePlayer.inventory.currentItem = n;
        if (bl) {
            ((IAccessorPlayerControllerMP)Utils.mc.playerController).valen$lunar$callSyncCurrentPlayItem();
        }
    }

    public static MovingObjectPosition getTarget(double d, float f, float f2) {
        Vec3 vec3 = Utils.mc.thePlayer.getPositionEyes(1.0f);
        float f3 = -f * ((float)Math.PI / 180);
        float f4 = -f2 * ((float)Math.PI / 180);
        float f5 = MathHelper.cos((float)(f3 - (float)Math.PI));
        float f6 = MathHelper.sin((float)(f3 - (float)Math.PI));
        float f7 = -MathHelper.cos((float)f4);
        float f8 = MathHelper.sin((float)f4);
        Vec3 vec32 = new Vec3((double)(f6 * f7), (double)f8, (double)(f5 * f7));
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        return Utils.mc.theWorld.rayTraceBlocks(vec3, vec33, false, false, false);
    }

    public static boolean isPossibleToReach(BlockPos blockPos, double d) {
        float[] fArray = RotationUtils.getRotations(blockPos);
        Vec3 vec3 = Utils.mc.thePlayer.getPositionEyes(1.0f);
        float f = -fArray[0] * ((float)Math.PI / 180);
        float f2 = -fArray[1] * ((float)Math.PI / 180);
        float f3 = MathHelper.cos((float)(f - (float)Math.PI));
        float f4 = MathHelper.sin((float)(f - (float)Math.PI));
        float f5 = -MathHelper.cos((float)f2);
        float f6 = MathHelper.sin((float)f2);
        Vec3 vec32 = new Vec3((double)(f4 * f5), (double)f6, (double)(f3 * f5));
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        AxisAlignedBB axisAlignedBB = BlockUtils.getBlock(blockPos).getCollisionBoundingBox((World)Utils.mc.theWorld, blockPos, BlockUtils.getBlockState(blockPos));
        if (axisAlignedBB == null) {
            return false;
        }
        MovingObjectPosition movingObjectPosition = axisAlignedBB.calculateIntercept(vec3, vec33);
        return movingObjectPosition != null;
    }

    public static void setSpeed(double d, boolean bl) {
        if (!bl || Utils.isMoving()) {
            Utils.mc.thePlayer.motionX = -Math.sin(Utils.gd()) * d;
            Utils.mc.thePlayer.motionZ = Math.cos(Utils.gd()) * d;
        }
    }

    public static boolean keysDown() {
        return Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindRight.getKeyCode());
    }

    public static boolean jumpDown() {
        return Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindJump.getKeyCode());
    }

    public static double distanceToGround(Entity entity) {
        if (entity.onGround) {
            return 0.0;
        }
        double d = -1.0;
        double d2 = entity.posY;
        if (entity.posY % 1.0 == 0.0) {
            d2 -= 1.0;
        }
        for (int i = (int)Math.floor(d2); i > -1; --i) {
            if (Utils.isPlaceable(new BlockPos(entity.posX, (double)i, entity.posZ))) continue;
            d = d2 - (double)i;
            break;
        }
        return d - 1.0;
    }

    public static float gd() {
        float f = Utils.mc.thePlayer.rotationYaw;
        if (Utils.mc.thePlayer.moveForward < 0.0f) {
            f += 180.0f;
        }
        float f2 = Utils.mc.thePlayer.moveForward < 0.0f ? -0.5f : (Utils.mc.thePlayer.moveForward > 0.0f ? 0.5f : 1.0f);
        if (Utils.mc.thePlayer.moveStrafing > 0.0f) {
            f -= 90.0f * f2;
        }
        if (Utils.mc.thePlayer.moveStrafing < 0.0f) {
            f += 90.0f * f2;
        }
        return f *= (float)Math.PI / 180;
    }

    public static float ae(float f, float f2, float f3) {
        float f4 = 1.0f;
        if (f2 < 0.0f) {
            f += 180.0f;
            f4 = -0.5f;
        } else if (f2 > 0.0f) {
            f4 = 0.5f;
        }
        if (f3 > 0.0f) {
            f -= 90.0f * f4;
        } else if (f3 < 0.0f) {
            f += 90.0f * f4;
        }
        return f * ((float)Math.PI / 180);
    }

    public static double getHorizontalSpeed() {
        return Utils.getHorizontalSpeed((Entity)Utils.mc.thePlayer);
    }

    public static double getHorizontalSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
    }

    public static List<String> getTopLevelLines(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String[] stringArray = string.split("\\r?\\n");
        int n = 0;
        boolean bl = false;
        String[] stringArray2 = stringArray;
        int n2 = stringArray2.length;
        for (int i = 0; i < n2; ++i) {
            String string2;
            String string3 = string2 = stringArray2[i];
            String string4 = string2.trim();
            if (bl) {
                if (!string4.contains("*/")) continue;
                bl = false;
                string4 = string4.substring(string4.indexOf("*/") + 2).trim();
            }
            if (string4.startsWith("//")) continue;
            if (string4.contains("/*")) {
                bl = true;
                if ((string4 = string4.substring(0, string4.indexOf("/*")).trim()).isEmpty()) continue;
            }
            if (string4.contains("//")) {
                string4 = string4.substring(0, string4.indexOf("//")).trim();
            }
            if (string4.contains("/*") && string4.contains("*/")) {
                string4 = string4.substring(0, string4.indexOf("/*")) + string4.substring(string4.indexOf("*/") + 2);
                string4 = string4.trim();
            }
            if (string4.isEmpty()) continue;
            String string5 = Utils.removeStringLiterals(string4);
            int n3 = 0;
            int n4 = 0;
            for (char c : string5.toCharArray()) {
                if (c == '{') {
                    ++n3;
                    continue;
                }
                if (c != '}') continue;
                ++n4;
            }
            if ((n += n3 - n4) != 0 || string4.contains("{") || string4.contains("}") || string4.startsWith("@")) continue;
            arrayList.add(string3.trim());
        }
        return arrayList;
    }

    public static boolean holdingEdible(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemFood && Utils.mc.thePlayer.getFoodStats().getFoodLevel() == 20) {
            ItemFood itemFood = (ItemFood)itemStack.getItem();
            return ((IAccessorItemFood)itemFood).valen$lunar$getAlwaysEdible();
        }
        return true;
    }

    private static String removeStringLiterals(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = false;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\"' && (i == 0 || string.charAt(i - 1) != '\\')) {
                bl = !bl;
                continue;
            }
            if (bl) continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static boolean blockAbove() {
        return !(BlockUtils.getBlock(new BlockPos(Utils.mc.thePlayer.posX, Utils.mc.thePlayer.posY + 2.0, Utils.mc.thePlayer.posZ)) instanceof BlockAir);
    }

    public static boolean onEdge() {
        return Utils.onEdge((Entity)Utils.mc.thePlayer);
    }

    public static boolean onEdge(Entity entity) {
        return Utils.mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(entity.motionX / 3.0, -1.0, entity.motionZ / 3.0)).isEmpty();
    }

    public static boolean lookingAtBlock() {
        return Utils.mc.objectMouseOver != null && Utils.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Utils.mc.objectMouseOver.getBlockPos() != null;
    }

    public static boolean isDiagonal(boolean bl) {
        boolean bl2;
        float f = (Utils.mc.thePlayer.rotationYaw % 360.0f + 360.0f) % 360.0f;
        f = f > 180.0f ? f - 360.0f : f;
        boolean bl3 = bl2 = Utils.inBetween(-170.0, 170.0, f) && !Utils.inBetween(-10.0, 10.0, f) && !Utils.inBetween(80.0, 100.0, f) && !Utils.inBetween(-100.0, -80.0, f);
        if (bl) {
            bl2 = Utils.inBetween(-178.5, 178.5, f) && !Utils.inBetween(-1.5, 1.5, f) && !Utils.inBetween(88.5, 91.5, f) && !Utils.inBetween(-91.5, -88.5, f);
        }
        boolean bl4 = Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown((int)Utils.mc.gameSettings.keyBindRight.getKeyCode());
        return bl2 || bl4;
    }

    public static double gbps(Entity entity, int n) {
        double d = entity.posX - entity.prevPosX;
        double d2 = entity.posZ - entity.prevPosZ;
        double d3 = Math.sqrt(d * d + d2 * d2) * 20.0;
        if (n == 0) {
            return d3;
        }
        return Utils.round(d3, n);
    }

    public static boolean inBetween(double d, double d2, double d3) {
        return d3 >= d && d3 <= d2;
    }

    public static String removeFormatCodes(String string) {
        return string.replace("\u00a7k", "").replace("\u00a7l", "").replace("\u00a7m", "").replace("\u00a7n", "").replace("\u00a7o", "").replace("\u00a7r", "");
    }

    public static boolean isClicking() {
        if (ModuleManager.autoClicker != null && ModuleManager.autoClicker.isEnabled()) {
            return Mouse.isButtonDown((int)0);
        }
        return MouseHelper.f() > 1 && System.currentTimeMillis() - MouseHelper.LL < 300L;
    }

    public static boolean isMining() {
        float f;
        float f2;
        boolean bl;
        int n = Utils.mc.gameSettings.keyBindAttack.getKeyCode();
        if (n == 0) {
            return false;
        }
        boolean bl2 = bl = n < 0 ? Mouse.isButtonDown((int)(n + 100)) : Keyboard.isKeyDown((int)n);
        if (!bl) {
            return false;
        }
        double d = Utils.mc.playerController.getBlockReachDistance();
        MovingObjectPosition movingObjectPosition = RotationUtils.rayTrace(d, 1.0f, new float[]{f2 = Utils.mc.thePlayer.rotationYaw, f = Utils.mc.thePlayer.rotationPitch}, null);
        if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            return false;
        }
        MovingObjectPosition movingObjectPosition2 = RotationUtils.rayCastBlock(d, f2, f);
        return movingObjectPosition2 != null && movingObjectPosition2.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && movingObjectPosition2.getBlockPos() != null;
    }

    public static boolean isEdgeOfBlock() {
        BlockPos blockPos = new BlockPos(Utils.mc.thePlayer.posX, Utils.mc.thePlayer.posY - (double)(Utils.mc.thePlayer.posY % 1.0 == 0.0 ? 1 : 0), Utils.mc.thePlayer.posZ);
        return Utils.mc.theWorld.isAirBlock(blockPos);
    }

    public static long timeBetween(long l, long l2) {
        return Math.abs(l2 - l);
    }

    public static void sendModuleMessage(Module module, String string) {
        Utils.sendRawMessage("&3" + module.getName() + "&7: &r" + string);
    }

    public static EntityLivingBase raytrace(int n) {
        Entity entity = null;
        EntityOtherPlayerMP entityOtherPlayerMP = Freecam.freeEntity == null ? Utils.mc.thePlayer : Freecam.freeEntity;
        MovingObjectPosition movingObjectPosition = entityOtherPlayerMP.rayTrace((double)n, 1.0f);
        Vec3 vec3 = entityOtherPlayerMP.getPositionEyes(1.0f);
        float f = entityOtherPlayerMP.rotationYaw;
        float f2 = entityOtherPlayerMP.rotationPitch;
        float f3 = MathHelper.cos((float)(-f * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = MathHelper.sin((float)(-f * ((float)Math.PI / 180) - (float)Math.PI));
        float f5 = -MathHelper.cos((float)(-f2 * ((float)Math.PI / 180)));
        Vec3 vec32 = new Vec3((double)(f4 * f5), (double)MathHelper.sin((float)(-f2 * ((float)Math.PI / 180))), (double)(f3 * f5));
        Vec3 vec33 = vec3.addVector(vec32.xCoord * (double)n, vec32.yCoord * (double)n, vec32.zCoord * (double)n);
        Vec3 vec34 = null;
        List list = Utils.mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(vec32.xCoord * (double)n, vec32.yCoord * (double)n, vec32.zCoord * (double)n).expand(1.0, 1.0, 1.0));
        double d = n;
        for (int i = 0; i < list.size(); ++i) {
            double d2;
            Entity entity2 = (Entity)list.get(i);
            if (!entity2.canBeCollidedWith()) continue;
            float f6 = entity2.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB = entity2.getEntityBoundingBox().expand((double)f6, (double)f6, (double)f6);
            MovingObjectPosition movingObjectPosition2 = axisAlignedBB.calculateIntercept(vec3, vec33);
            if (axisAlignedBB.isVecInside(vec3)) {
                if (!(0.0 < d) && d != 0.0) continue;
                entity = entity2;
                vec34 = movingObjectPosition2 == null ? vec3 : movingObjectPosition2.hitVec;
                d = 0.0;
                continue;
            }
            if (movingObjectPosition2 == null || !((d2 = vec3.distanceTo(movingObjectPosition2.hitVec)) < d) && d != 0.0) continue;
            if (entity2 == Utils.mc.getRenderViewEntity().ridingEntity && !VanillaHooks.canRiderInteract(entity2)) {
                if (d != 0.0) continue;
                entity = entity2;
                vec34 = movingObjectPosition2.hitVec;
                continue;
            }
            entity = entity2;
            vec34 = movingObjectPosition2.hitVec;
            d = d2;
        }
        if (entity != null && (d < (double)n || movingObjectPosition == null)) {
            movingObjectPosition = new MovingObjectPosition(entity, vec34);
        }
        if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && movingObjectPosition.entityHit instanceof EntityLivingBase) {
            return (EntityLivingBase)movingObjectPosition.entityHit;
        }
        return null;
    }

    public static int getChroma(long l, long ... lArray) {
        long l2 = System.currentTimeMillis() + (lArray.length > 0 ? lArray[0] : 0L);
        return Color.getHSBColor((float)(l2 % (15000L / l)) / (15000.0f / (float)l), 1.0f, 1.0f).getRGB();
    }

    public static double round(double d, int n) {
        if (n == 0) {
            return Math.round(d);
        }
        double d2 = Math.pow(10.0, n);
        return (double)Math.round(d * d2) / d2;
    }

    public static String stripColor(String string) {
        if (string.isEmpty()) {
            return string;
        }
        char[] cArray = StringUtils.stripControlCodes((String)string).toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : cArray) {
            if (c >= '\u007f' || c <= '\u0014') continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static List<String> getScoreBoardOld() {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (Utils.mc.theWorld == null) {
            return arrayList;
        }
        Scoreboard scoreboard = Utils.mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return arrayList;
        }
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreObjective == null) {
            return arrayList;
        }
        ArrayList arrayList2 = scoreboard.getSortedScores(scoreObjective);
        ArrayList arrayList3 = new ArrayList();
        for (Score score : arrayList2) {
            if (score == null || score.getPlayerName() == null || score.getPlayerName().startsWith("#")) continue;
            arrayList3.add(score);
        }
        arrayList2 = arrayList3.size() > 15 ? Lists.newArrayList((Iterable)Iterables.skip(arrayList3, (int)(arrayList2.size() - 15))) : arrayList3;
        for (Score score : arrayList2) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
            arrayList.add(ScorePlayerTeam.formatPlayerName((Team)scorePlayerTeam, (String)score.getPlayerName()));
        }
        return arrayList;
    }

    public static void setSwinging() {
        int n;
        int n2 = Utils.mc.thePlayer.isPotionActive(Potion.digSpeed) ? 6 - (1 + Utils.mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (n = Utils.mc.thePlayer.isPotionActive(Potion.digSlowdown) ? 6 + (1 + Utils.mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
        if (!Utils.mc.thePlayer.isSwingInProgress || Utils.mc.thePlayer.swingProgressInt >= n / 2 || Utils.mc.thePlayer.swingProgressInt < 0) {
            Utils.mc.thePlayer.swingProgressInt = -1;
            Utils.mc.thePlayer.isSwingInProgress = true;
        }
    }

    public static String uppercaseFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static boolean isPlaceable(BlockPos blockPos) {
        return BlockUtils.replaceable(blockPos) || BlockUtils.isFluid(BlockUtils.getBlock(blockPos));
    }

    public static boolean spectatorCheck() {
        return Utils.mc.thePlayer.inventory.getStackInSlot(8) != null && Utils.mc.thePlayer.inventory.getStackInSlot(8).getDisplayName().contains("Return") || Utils.stripString(((IAccessorGuiIngame)Utils.mc.ingameGUI).valen$lunar$getDisplayedTitle()).contains("YOU DIED");
    }

    public static boolean holdingWeapon() {
        return Utils.holdingWeapon((EntityLivingBase)(mc == null ? null : Utils.mc.thePlayer));
    }

    public static boolean holdingWeapon(EntityLivingBase entityLivingBase) {
        if (entityLivingBase == null) {
            return false;
        }
        ItemStack itemStack = entityLivingBase.getHeldItem();
        int n = mc != null && entityLivingBase == Utils.mc.thePlayer ? Utils.mc.thePlayer.inventory.currentItem : -1;
        return ModuleManager.weapons == null ? Weapons.isDefaultWeapon(itemStack) : ModuleManager.weapons.isWeapon(itemStack, n);
    }

    public static boolean holdingSword() {
        if (Utils.mc.thePlayer.getHeldItem() == null) {
            return false;
        }
        return Utils.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public static double getDamageLevel(ItemStack itemStack) {
        return ItemSortScoring.getMeleeDamage((ItemStack)itemStack);
    }

    public static float getDirection() {
        return Utils.getCustomDirection(Utils.mc.thePlayer.rotationYaw, Utils.mc.thePlayer.movementInput.moveForward, Utils.mc.thePlayer.movementInput.moveStrafe);
    }

    public static boolean isUserMoving() {
        return Utils.mc.thePlayer.movementInput.moveForward != 0.0f || Utils.mc.thePlayer.movementInput.moveStrafe != 0.0f;
    }

    public static float getCustomDirection(float f, float f2, float f3) {
        float f4 = 1.0f;
        if (f2 < 0.0f) {
            f += 180.0f;
            f4 = -0.5f;
        } else if (f2 > 0.0f) {
            f4 = 0.5f;
        }
        if (f3 > 0.0f) {
            f -= 90.0f * f4;
        } else if (f3 < 0.0f) {
            f += 90.0f * f4;
        }
        return f * ((float)Math.PI / 180);
    }

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if (block == null) {
            return false;
        }
        return !BlockUtils.isInteractable(block) && !(block instanceof BlockSnow) && !(block instanceof BlockWeb) && !(block instanceof BlockSapling) && !(block instanceof BlockDaylightDetector) && !(block instanceof BlockBeacon) && !(block instanceof BlockBanner) && !(block instanceof BlockEndPortalFrame) && !(block instanceof BlockEndPortal) && !(block instanceof BlockLever) && !(block instanceof BlockButton) && !(block instanceof BlockSkull) && !(block instanceof BlockLiquid) && !(block instanceof BlockCactus) && !(block instanceof BlockDoublePlant) && !(block instanceof BlockLilyPad) && !(block instanceof BlockCarpet) && !(block instanceof BlockTripWire) && !(block instanceof BlockTripWireHook) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFlower) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockSign) && !(block instanceof BlockLadder) && !(block instanceof BlockTorch) && !(block instanceof BlockRedstoneTorch) && !(block instanceof BlockStairs) && !(block instanceof BlockSlab) && !(block instanceof BlockFence) && !(block instanceof BlockPane) && !(block instanceof BlockStainedGlassPane) && !(block instanceof BlockGravel) && !(block instanceof BlockClay) && !(block instanceof BlockSand) && !(block instanceof BlockSoulSand) && !(block instanceof BlockRailBase);
    }

    public static <E extends Enum<E>> E getEnum(Class<E> clazz, String string) {
        for (Enum enum_ : (Enum[])clazz.getEnumConstants()) {
            if (!enum_.name().equals(string)) continue;
            return (E)enum_;
        }
        return null;
    }

    public static int getSpeedAmplifier() {
        if (Utils.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 1 + Utils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
        }
        return 0;
    }

    public static ItemStack getSpoofedItem(ItemStack itemStack) {
        if (ModuleManager.autoTool != null && ModuleManager.autoTool.isEnabled() && ModuleManager.autoTool.spoofItem.isToggled() && Utils.mc.thePlayer != null) {
            return Utils.mc.thePlayer.inventory.getStackInSlot(ModuleManager.autoTool.previousSlot == -1 ? Utils.mc.thePlayer.inventory.currentItem : ModuleManager.autoTool.previousSlot);
        }
        return itemStack;
    }

    public static boolean scaffoldDiagonal(boolean bl) {
        return false;
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String string;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((string = bufferedReader.readLine()) != null) {
                stringBuilder.append(string).append('\n');
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static boolean isLobby() {
        String[] stringArray;
        List<String> list;
        return Utils.isHypixel() && !(list = Utils.getSidebarLines()).isEmpty() && (stringArray = Utils.stripColor(list.get(1)).split("  ")).length > 1 && stringArray[1].charAt(0) == 'L';
    }

    public static boolean isBedwarsPracticeOrReplay() {
        if (Utils.isHypixel()) {
            if (!Utils.nullCheck()) {
                return false;
            }
            Scoreboard scoreboard = Utils.mc.theWorld.getScoreboard();
            if (scoreboard == null) {
                return false;
            }
            ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
            if (scoreObjective == null) {
                return false;
            }
            String string = Utils.stripString(scoreObjective.getDisplayName());
            return string.contains("BED WARS PRACTICE") || string.contains("REPLAY");
        }
        return false;
    }

    public static Vec3 getClosestPlayerPos(double d) {
        if (Utils.mc.theWorld == null || Utils.mc.thePlayer == null) {
            return null;
        }
        Vec3 vec3 = null;
        double d2 = d;
        for (EntityPlayer entityPlayer : Utils.mc.theWorld.playerEntities) {
            double d3;
            double d4;
            double d5;
            double d6;
            if (entityPlayer == Utils.mc.thePlayer || mc.getNetHandler() == null || mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID()) == null || !((d6 = (d5 = entityPlayer.posX - Utils.mc.thePlayer.posX) * d5 + (d4 = entityPlayer.posY - Utils.mc.thePlayer.posY) * d4 + (d3 = entityPlayer.posZ - Utils.mc.thePlayer.posZ) * d3) < d2)) continue;
            d2 = d6;
            vec3 = new Vec3(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);
        }
        return vec3;
    }
}

