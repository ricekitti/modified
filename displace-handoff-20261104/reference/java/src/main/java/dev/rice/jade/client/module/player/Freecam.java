package dev.rice.jade.client.module.player;

/** Rice host shim: Jade's Freecam is not part of Rice, so it is never active (upstream pqos() is "freecam active"). */
public final class Freecam {
   private Freecam() {
   }

   public static boolean pqos() {
      return false;
   }
}
