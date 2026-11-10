package dev.rice.jade.mixin.impl.accessor;

import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Rice host replacement for Jade's mixin accessor. Upstream casts EntityPlayerSP to this mixin interface;
 * the port calls {@link #of(Object)} instead (the only rewrite at those sites) and gets the same getters,
 * backed by Rice's mapped reflection.
 */
public interface IAccessorEntityPlayerSP {
   double getLastReportedPosX();

   double getLastReportedPosY();

   double getLastReportedPosZ();

   float getLastReportedYaw();

   float getLastReportedPitch();

   static IAccessorEntityPlayerSP of(Object player) {
      final EntityPlayerSP target = (EntityPlayerSP)player;
      return new IAccessorEntityPlayerSP() {
         @Override
         public double getLastReportedPosX() {
            return Fields.number(Fields.X, target).doubleValue();
         }

         @Override
         public double getLastReportedPosY() {
            return Fields.number(Fields.Y, target).doubleValue();
         }

         @Override
         public double getLastReportedPosZ() {
            return Fields.number(Fields.Z, target).doubleValue();
         }

         @Override
         public float getLastReportedYaw() {
            return Fields.number(Fields.YAW, target).floatValue();
         }

         @Override
         public float getLastReportedPitch() {
            return Fields.number(Fields.PITCH, target).floatValue();
         }
      };
   }

   final class Fields {
      static final Field X = ReflectionHelper.findField(EntityPlayerSP.class, "lastReportedPosX", "field_175172_bI");
      static final Field Y = ReflectionHelper.findField(EntityPlayerSP.class, "lastReportedPosY", "field_175166_bJ");
      static final Field Z = ReflectionHelper.findField(EntityPlayerSP.class, "lastReportedPosZ", "field_175167_bK");
      static final Field YAW = ReflectionHelper.findField(EntityPlayerSP.class, "lastReportedYaw", "field_175164_bL");
      static final Field PITCH = ReflectionHelper.findField(EntityPlayerSP.class, "lastReportedPitch", "field_175165_bM");

      private Fields() {
      }

      static Number number(Field field, Object owner) {
         try {
            return (Number)field.get(owner);
         } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
         }
      }
   }
}
