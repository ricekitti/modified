package dev.rice.client.input;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import org.lwjgl.input.Mouse;

/**
 * Injects synthetic LWJGL mouse events into {@code org.lwjgl.input.Mouse}'s event queue (LWJGL 2.9.4, bundled). This
 * is the layer Lunar Client's built-in CPS counter and Keystrokes mod read, and the one the game's own
 * {@code Minecraft.runTick()} drains — so an injected click is seen as a real click by all of them. The record is the
 * exact 22-byte Mouse event: {@code button(1) state(1) dx/x(4) dy/y(4) dwheel(4) nanos(8)}.
 *
 * <p>Everything here is best-effort and fully guarded: the LWJGL internals are version-specific and only verifiable
 * live, in the actual game, so any reflection/capacity/timing problem makes a call a no-op rather than disturbing the
 * player's real mouse input. Nothing is ever written unless there is spare capacity past the current read limit.
 */
public final class LwjglMouseEvents {
   private static final int EVENT_SIZE = 22;
   private static Field readBufferField;
   private static Field buttonsField;
   private static Object lock;
   private static boolean resolved;
   private static boolean available;

   private LwjglMouseEvents() {
   }

   private static synchronized void resolve() {
      if (resolved) {
         return;
      }
      resolved = true;
      try {
         readBufferField = Mouse.class.getDeclaredField("readBuffer");
         readBufferField.setAccessible(true);
         buttonsField = Mouse.class.getDeclaredField("buttons");
         buttonsField.setAccessible(true);
         Field lockField = Class.forName("org.lwjgl.input.OpenGLPackageAccess").getDeclaredField("global_lock");
         lockField.setAccessible(true);
         lock = lockField.get(null);
         available = lock != null;
      } catch (Throwable notAvailable) {
         available = false;
      }
   }

   /** Queue a full click (press then release) of an LWJGL mouse button (0 = left, 1 = right). */
   public static void click(int button) {
      press(button, true);
      press(button, false);
   }

   /** Queue a single button transition into the LWJGL event stream. */
   public static void press(int button, boolean down) {
      resolve();
      if (!available || !Mouse.isCreated()) {
         return;
      }
      try {
         synchronized (lock) {
            ByteBuffer readBuffer = (ByteBuffer) readBufferField.get(null);
            if (readBuffer == null || readBuffer.capacity() - readBuffer.limit() < EVENT_SIZE) {
               return; // no spare room past the current events; skip rather than risk corrupting the stream
            }
            int position = readBuffer.position();
            int limit = readBuffer.limit();
            boolean grabbed = Mouse.isGrabbed();
            int px = grabbed ? 0 : Mouse.getX();
            int py = grabbed ? 0 : Mouse.getY();
            readBuffer.position(limit);
            readBuffer.limit(limit + EVENT_SIZE);
            readBuffer.put((byte) button).put((byte) (down ? 1 : 0)).putInt(px).putInt(py).putInt(0).putLong(System.nanoTime());
            readBuffer.position(position); // restore read cursor; the extended limit now includes the new event
            setButtonState(button, down);
         }
      } catch (Throwable ignored) {
         // Never disturb real input if anything about the internals differs from expectations.
      }
   }

   /** Reflect the current pressed state so isButtonDown-based readers (Lunar keystroke boxes) light up too. */
   private static void setButtonState(int button, boolean down) {
      try {
         ByteBuffer buttons = (ByteBuffer) buttonsField.get(null);
         if (buttons != null && button >= 0 && button < buttons.capacity()) {
            buttons.put(button, (byte) (down ? 1 : 0));
         }
      } catch (Throwable ignored) {
      }
   }
}
