package dev.rice.client.input;

import dev.rice.inject.NativeBridge;

/** Cosmetic local event notification; the companion owns all settings and audio. */
public final class InputSoundBridge {
   private InputSoundBridge() { }

   public static void mouse(boolean left, boolean down) {
      emit(1, left ? 0 : 1, down);
   }

   public static void key(int lwjglCode, boolean down) {
      if (lwjglCode > 0 && lwjglCode < 256) emit(2, lwjglCode, down);
   }

   private static void emit(int kind, int code, boolean down) {
      try {
         NativeBridge.inputSoundEvent(kind, code, down);
      } catch (UnsatisfiedLinkError unavailable) {
         // Absent companion/JNI must never gate an input or alter module scheduling.
      }
   }
}
