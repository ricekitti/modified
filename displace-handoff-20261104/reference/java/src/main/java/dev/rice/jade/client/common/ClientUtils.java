// Jade recovery: original class: jade.deps.eLz.IWmaZDm
package dev.rice.jade.client.common;

import dev.rice.jade.client.Jade;
import dev.rice.jade.mixin.impl.accessor.IAccessorMinecraft;
import java.awt.Color;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ClientUtils implements IMinecraft {
   private static final Random RANDOM = new Random();
   private static final ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(ClientUtils::initialNestedUpdateDepth);
   public static HashSet<String> friends = new HashSet<>();

   public static boolean canRiderInteract(Entity var0) {
      return AttackHelper.invokeCanRiderInteract(var0);
   }

   public static boolean vAxywR() {
      return GameStateUtils.isInGameWithFocus(mc);
   }

   public static double randomDouble(double var0, double var2) {
      return TextUtils.randomDoubleInRange(RANDOM, var0, var2);
   }

   public static void sendColoredMessage(String var0) {
      ChatSender.HARd(mc, var0, true);
   }

   public static void sendJadeMessage(String var0, String var1) {
      ChatSender.daPdo(mc, var0, " " + var1);
   }

   public static void sendDebugMessage(String var0) {
      ChatSender.sendDebugMessage(mc, var0);
   }

   public static boolean xusXfhC(KeyBinding var0) {
      return Wlo914.GKjTw(var0.getKeyCode(), Keyboard::isKeyDown, Mouse::isButtonDown);
   }

   public static boolean isFriend(EntityPlayer var0) {
      return var0 != null && isFriendName(var0.getName());
   }

   public static boolean isFriendName(String var0) {
      return Jade.relationManager != null ? Jade.relationManager.isFriend(var0) : var0 != null && !friends.isEmpty() && friends.contains(var0.toLowerCase());
   }

   public static boolean isInWorld() {
      return GameStateUtils.isInWorld(mc);
   }

   public static boolean isOnHypixel() {
      return GameStateUtils.isOnHypixel(mc);
   }

   public static int YVVZ(int var0, int var1) {
      return TextUtils.withAlpha(var0, var1);
   }

   public static boolean isTeammate(Entity var0) {
      return TeamUtils.isTeammate(mc, var0);
   }

   public static void eqyXnoq() {
      (IAccessorMinecraft.of(mc)).getTimer().timerSpeed = 1.0F;
   }

   public static boolean isInNestedUpdate() {
      return threadLocal.get() > 0;
   }

   public static int getBedWarsBoardType() {
      return ScoreboardUtils.getBedWarsBoardType(mc);
   }

   public static String zaUnpz(String var0) {
      return ScoreboardUtils.IwugSbg(var0);
   }

   public static boolean isJumpKeyDown() {
      return GameStateUtils.yrvB(mc);
   }

   public static int AIowEv(long var0, long... var2) {
      long var3 = System.currentTimeMillis() + (var2.length > 0 ? var2[0] : 0L);
      return Color.getHSBColor((float)(var3 % (15000L / var0)) / (15000.0F / (float)var0), 1.0F, 1.0F).getRGB();
   }

   public static String AOAtn(String var0) {
      return var0.isEmpty() ? var0 : ScoreboardUtils.IwugSbg(var0);
   }

   public static boolean isPassableBlock(ItemBlock var0) {
      return PassableBlocks.isPassable(var0.getBlock());
   }

   public static String readStreamText(InputStream var0) {
      return AQKBeof5.readStreamText(var0);
   }

   public static boolean mdeQ() {
      return isOnHypixel() && ScoreboardUtils.ATIk(mc);
   }

   private static Integer initialNestedUpdateDepth() {
      return 0;
   }
}
