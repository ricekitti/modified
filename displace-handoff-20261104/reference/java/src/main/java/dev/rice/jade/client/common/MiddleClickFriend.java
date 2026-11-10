// Jade recovery: original class: jade.deps.eLz.oiLUHON
package dev.rice.jade.client.common;

import org.lwjgl.input.Mouse;

public class MiddleClickFriend {
   private static final MouseScrollState scrollState = new MouseScrollState();

   public static boolean isScrollKeyPressed(int var0) {
      return scrollState.matchesScrollDirection(var0, Mouse::getDWheel);
   }
}
