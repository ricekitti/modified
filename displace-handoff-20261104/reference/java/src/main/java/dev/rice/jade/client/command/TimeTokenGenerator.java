// Jade recovery: original class: jade.deps.eLz.hFX0I60sD
package dev.rice.jade.client.command;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class TimeTokenGenerator {

   public static String HXFm(long var0) throws Exception {
      long var2 = var0 / 20000L + 29062381L;
      byte[] var4 = (var2 + "J{LlrPhHgj8zy:uB").getBytes(StandardCharsets.UTF_8);
      byte[] var5 = MessageDigest.getInstance("MD5").digest(var4);
      return String.format("%032x", new BigInteger(1, var5));
   }
}
