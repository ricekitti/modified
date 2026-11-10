// Jade recovery: original class: jade.deps.eLz.xqHsE7Caxu
package dev.rice.jade.client.common;

import java.util.Random;

public final class TextUtils {

   public static double randomDoubleInRange(Random var0, double var1, double var3) {
      return var1 + var0.nextDouble() * (var3 - var1);
   }

   public static String QJdK(String var0) {
      return var0 == null ? "" : var0.replace('&', '§');
   }

   public static int withAlpha(int var0, int var1) {
      return var0 & 16777215 | var1 << 24;
   }
}
