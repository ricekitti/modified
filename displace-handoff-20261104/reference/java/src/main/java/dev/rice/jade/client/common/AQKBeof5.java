// Jade recovery: original class: jade.deps.eLz.AQKBeof5
package dev.rice.jade.client.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class AQKBeof5 {

   public static String readStreamText(InputStream var0) {
      StringBuilder var1 = new StringBuilder();

      try {
         BufferedReader var2 = new BufferedReader(new InputStreamReader(var0));

         for (String var3 = var2.readLine(); var3 != null; var3 = var2.readLine()) {
            var1.append(var3).append('\n');
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return var1.toString();
   }
}
