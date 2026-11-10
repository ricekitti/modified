package dev.rice.client.openjade;

import dev.rice.client.ClientContext;
import dev.rice.jade.client.setting.FontSetting;
import dev.rice.jade.client.setting.Setting;
import dev.rice.jade.client.setting.SliderSetting;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import java.util.ArrayList;
import java.util.List;

/**
 * Rice GUI/config face of a ported Jade module. Rice settings keep their existing keys; each is bound to the
 * upstream Jade setting of the same meaning. A change is pushed into the Jade setting and announced to the
 * Jade module exactly like Jade's ClickGui does ({@code guiButtonToggled} / {@code guiSliderChanged}, then
 * {@code guiUpdate}); visibility is read back from the Jade setting, which the module's own guiUpdate maintains.
 */
public abstract class JadeModuleAdapter extends Module {
   protected final ModuleManager modules;
   private final List<Binding> bindings = new ArrayList<>();

   protected JadeModuleAdapter(ModuleManager modules, String id, String name, String description, ModuleCategory category) {
      super(id, name, description, category);
      this.modules = modules;
   }

   protected abstract dev.rice.jade.client.module.Module jade();

   protected final BooleanSetting bind(BooleanSetting rice, String jadeName) {
      this.bindings.add(new Binding(rice, jadeName));
      return rice;
   }

   protected final ChoiceSetting bind(ChoiceSetting rice, String jadeName) {
      this.bindings.add(new Binding(rice, jadeName));
      return rice;
   }

   protected final NumberSetting bind(NumberSetting rice, String jadeName) {
      this.bindings.add(new Binding(rice, jadeName));
      return rice;
   }

   protected final void ensureRuntime() {
      JadeRuntime.init(this.modules);
   }

   /** Push changed Rice values into the Jade module, notifying it the way Jade's ClickGui does. */
   protected final void sync() {
      this.ensureRuntime();
      dev.rice.jade.client.module.Module jade = this.jade();
      boolean changed = false;
      for (Binding binding : this.bindings) {
         Setting target = binding.target(jade);
         if (target instanceof dev.rice.jade.client.setting.BooleanSetting) {
            dev.rice.jade.client.setting.BooleanSetting jadeBool = (dev.rice.jade.client.setting.BooleanSetting)target;
            boolean value = ((BooleanSetting)binding.rice).isEnabled();
            if (jadeBool.isToggled() != value) {
               jadeBool.setToggled(value);
               jade.guiButtonToggled(jadeBool);
               changed = true;
            }
         } else if (target instanceof FontSetting) {
            FontSetting font = (FontSetting)target;
            String wanted = ((ChoiceSetting)binding.rice).getOptions().get(((ChoiceSetting)binding.rice).index());
            String[] options = font.getOptions();
            for (int i = 0; i < options.length; i++) {
               if (options[i].equalsIgnoreCase(wanted) && (int)font.getInput() != i) {
                  font.setValueClamped(i);
                  jade.guiSliderChanged(font);
                  changed = true;
               }
            }
         } else if (target instanceof SliderSetting) {
            SliderSetting slider = (SliderSetting)target;
            double value = binding.rice instanceof ChoiceSetting ? ((ChoiceSetting)binding.rice).index() : ((NumberSetting)binding.rice).doubleValue();
            if (slider.getInput() != value) {
               slider.setValueClamped(value);
               jade.guiSliderChanged(slider);
               changed = true;
            }
         }
      }

      if (changed) {
         jade.guiUpdate();
      }
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.sync();
      JadeRuntime.setEnabled(this.jade(), true);
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.ensureRuntime();
      JadeRuntime.setEnabled(this.jade(), false);
   }

   @Override
   public void onClientTickStart(ClientContext context) {
      this.sync();
      if (this.isEnabled() && !this.jade().isEnabled()) {
         // the Jade module turned itself off (upstream Module#disable from inside the module)
         this.modules.requestEnabled(this, false);
      }
   }

   @Override
   public boolean afterSettingControl(String key, ClientContext context) {
      this.sync();
      return false;
   }

   @Override
   public boolean settingVisible(String key) {
      this.ensureRuntime();
      for (Binding binding : this.bindings) {
         if (binding.rice.getKey().equals(key)) {
            Setting target = binding.target(this.jade());
            return target == null || target.visible;
         }
      }

      return true;
   }

   @Override
   public String statusText() {
      this.ensureRuntime();
      String info = this.jade().getInfo();
      return info == null || info.isEmpty() ? null : info;
   }

   private static final class Binding {
      final dev.rice.setting.Setting<?> rice;
      final String jadeName;
      Setting cached;

      Binding(dev.rice.setting.Setting<?> rice, String jadeName) {
         this.rice = rice;
         this.jadeName = jadeName;
      }

      Setting target(dev.rice.jade.client.module.Module jade) {
         if (this.cached == null) {
            for (Setting setting : jade.getSettings()) {
               if (setting.getName().equals(this.jadeName)) {
                  this.cached = setting;
               }
            }

            if (this.cached == null) {
               throw new IllegalStateException("Jade setting missing: " + jade.getName() + "." + this.jadeName);
            }
         }

         return this.cached;
      }
   }
}
