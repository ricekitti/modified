/**
 * Physical keyboard/mouse state for headless checks. The test-only org.lwjgl.input.Keyboard/Mouse produced by
 * V115InputStubs read their key and button state from here instead of a native LWJGL device.
 */
public final class V115Input {
   public static final boolean[] keys = new boolean[512];
   public static final boolean[] buttons = new boolean[16];

   private V115Input() {
   }

   public static boolean key(int code) {
      return code > 0 && code < keys.length && keys[code];
   }

   public static boolean button(int button) {
      return button >= 0 && button < buttons.length && buttons[button];
   }

   public static void clear() {
      java.util.Arrays.fill(keys, false);
      java.util.Arrays.fill(buttons, false);
   }
}
