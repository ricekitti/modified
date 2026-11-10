package myau.host;

import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;

/**
 * The private {@code @Shadow} fields the generated {@link MayuMixins} bodies read and write. Mixin merges the injector
 * into the target class, so upstream touches these fields directly; the host reaches the same fields through Rice's
 * mapping-aware reflection (named, SRG or official names, matching the payload's mapping).
 */
public final class MayuShadows {
   private static Field lastReportedYaw;
   private static Field lastReportedPitch;
   private static Field leftClickCounter;
   private static Field keyDescription;
   private static Field rendererMinecraft;

   private MayuShadows() {
   }

   private static Field field(Class<?> owner, String named, String srg) {
      return ReflectionHelper.findField(owner, named, srg);
   }

   public static float get_lastReportedYaw(EntityPlayerSP self) {
      try {
         if (lastReportedYaw == null) lastReportedYaw = field(EntityPlayerSP.class, "lastReportedYaw", "field_175164_bL");
         return lastReportedYaw.getFloat(self);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   public static void set_lastReportedYaw(EntityPlayerSP self, float value) {
      try {
         if (lastReportedYaw == null) lastReportedYaw = field(EntityPlayerSP.class, "lastReportedYaw", "field_175164_bL");
         lastReportedYaw.setFloat(self, value);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   public static float get_lastReportedPitch(EntityPlayerSP self) {
      try {
         if (lastReportedPitch == null) lastReportedPitch = field(EntityPlayerSP.class, "lastReportedPitch", "field_175165_bM");
         return lastReportedPitch.getFloat(self);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   public static void set_lastReportedPitch(EntityPlayerSP self, float value) {
      try {
         if (lastReportedPitch == null) lastReportedPitch = field(EntityPlayerSP.class, "lastReportedPitch", "field_175165_bM");
         lastReportedPitch.setFloat(self, value);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   public static void set_leftClickCounter(Minecraft self, int value) {
      try {
         if (leftClickCounter == null) leftClickCounter = field(Minecraft.class, "leftClickCounter", "field_71429_W");
         leftClickCounter.setInt(self, value);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   public static String get_keyDescription(KeyBinding self) {
      try {
         if (keyDescription == null) keyDescription = field(KeyBinding.class, "keyDescription", "field_74515_c");
         return (String)keyDescription.get(self);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   public static Minecraft get_mc(EntityRenderer self) {
      try {
         if (rendererMinecraft == null) rendererMinecraft = field(EntityRenderer.class, "mc", "field_78531_r");
         return (Minecraft)rendererMinecraft.get(self);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }
}
