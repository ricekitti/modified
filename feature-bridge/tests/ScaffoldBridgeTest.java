import dev.rice.module.impl.ScaffoldModule;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.module.Module;
import dev.rice.setting.*;
import java.lang.reflect.Field;
import java.util.List;

/** Headless Phase-1 verification: Scaffold setting surface + range-bridge round-trip. */
public class ScaffoldBridgeTest {
   static int fails = 0;
   static void check(boolean c, String m) { System.out.println((c ? "ok   " : "FAIL ") + m); if (!c) fails++; }
   static void checkChoice(ScaffoldModule s, String key, int n) {
      ChoiceSetting c = (ChoiceSetting) s.findSetting(key);
      check(c != null && c.getOptions().size() == n, key + " dropdown = " + n + " options" + (c != null ? " (got " + c.getOptions().size() + ")" : " MISSING"));
   }

   public static void main(String[] a) throws Exception {
      ScaffoldModule s = new ScaffoldModule();
      List<Setting<?>> settings = s.getSettings();

      // 1. mode dropdowns match Rise source option counts 6/2/3/10/12/3/4/3
      checkChoice(s, "mode", 6);
      checkChoice(s, "rotation_mode", 2);
      checkChoice(s, "ray_cast", 3);
      checkChoice(s, "sprint", 10);
      checkChoice(s, "tower", 12);
      checkChoice(s, "same_y", 3);
      checkChoice(s, "downwards", 4);
      checkChoice(s, "yaw_offset", 3);

      // 2. dual "Watchdog Jump" (Rise ViMode idx5, VkMode idx8) distinct by index
      ChoiceSetting sp = (ChoiceSetting) s.findSetting("sprint");
      check("Watchdog Jump".equals(sp.getOptions().get(5)) && "Watchdog Jump".equals(sp.getOptions().get(8))
         && ScaffoldModule.SPRINT_WATCHDOG_JUMP_VI == 5 && ScaffoldModule.SPRINT_WATCHDOG_JUMP_VK == 8,
         "sprint index 5 & 8 both 'Watchdog Jump' with distinct stable ids");

      // 3. setting type census vs Rise (8 mode + 19 bool + 3 number + 8 range)
      int bools = 0, nums = 0, ranges = 0, choices = 0;
      for (Setting<?> x : settings) {
         if (x instanceof BooleanSetting) bools++;
         else if (x instanceof RangeSetting) ranges++;
         else if (x instanceof NumberSetting) nums++;
         else if (x instanceof ChoiceSetting) choices++;
      }
      System.out.println("   census: choice=" + choices + " bool=" + bools + " number=" + nums + " range=" + ranges + " total=" + settings.size());
      // Bytecode-exact census (classdump com.alan.clients...Scaffold <init>): 8 mode / 20 bool / 3 number / 8 range.
      check(choices == 8, "8 mode dropdowns");
      check(ranges == 8, "8 range settings (BoundsNumberValue), both endpoints preserved");
      check(bools == 21, "20 parent booleans plus Watchdog Jump child setting");
      check(nums == 4, "3 parent number settings plus Vulcan Sprint child speed");
      check(settings.size() == 41, "39 parent settings plus two submode child settings");
      // spot-check bytecode-exact defaults
      check(((BooleanSetting) s.findSetting("safe_walk")).isEnabled(), "Safe Walk defaults ON");
      check(((BooleanSetting) s.findSetting("keep_y_bypass")).isEnabled(), "Keep-Y bypass defaults ON");
      check(!((BooleanSetting) s.findSetting("movement_correction")).isEnabled(), "Movement Correction defaults OFF");
      check(((NumberSetting) s.findSetting("timer")).doubleValue() == 1.0, "Timer defaults 1.0");
      RangeSetting rs = (RangeSetting) s.findSetting("rotation_speed");
      check(rs.get().lower == 5.0 && rs.get().upper == 10.0 && rs.minimum() == 0.0 && rs.maximum() == 10.0, "Rotation Speed range 5..10 in 0..10");
      check(s.findSetting("ignore_speed_effect") != null, "Ignore Speed Effect present (Rise field agt)");
      check(s.findSetting("watchdog_telly_jump_delay") instanceof RangeSetting, "Watchdog Telly Jump Delay is a range (Rise field age)");

      // 4. bridge range expansion + spec resolution (inject modules[4]=scaffold, no MC/natives)
      Field f = NativeFeatureBridge.class.getDeclaredField("modules"); f.setAccessible(true);
      Module[] mods = new Module[5]; mods[4] = s; f.set(null, mods);

      String[][] labels = NativeFeatureBridge.labels(4);
      int expandedRows = 0; for (Setting<?> x : settings) expandedRows += (x instanceof RangeSetting) ? 2 : 1;
      check(labels.length == expandedRows, "labels expands ranges: " + labels.length + " rows == " + expandedRows);

      boolean rangeRowsOk = true;
      for (Setting<?> x : settings) if (x instanceof RangeSetting) {
         boolean lo = false, hi = false;
         for (String[] row : labels) { if (row[0].equals(x.getKey() + "_lo")) lo = true; if (row[0].equals(x.getKey() + "_hi")) hi = true; }
         if (!lo || !hi) { rangeRowsOk = false; System.out.println("   missing lo/hi rows for " + x.getKey()); }
      }
      check(rangeRowsOk, "every RangeSetting emits _lo and _hi endpoint rows");

      boolean specOk = true;
      for (int i = 0; i < labels.length; i++) { double[] spec = NativeFeatureBridge.specification(4, i); if (spec.length != 5) { specOk = false; } }
      check(specOk, "specification() resolves all " + labels.length + " native rows");
      boolean visibilityOk=true, stepsOk=true;
      for(int i=0;i<labels.length;++i){
         String key=labels[i][0];
         if(key.endsWith("_lo")||key.endsWith("_hi")) {
            key=key.substring(0,key.length()-3);
            stepsOk &= NativeFeatureBridge.specification(4,i)[3]==1;
         }
         visibilityOk &= NativeFeatureBridge.visible(4,i)==s.settingVisible(key);
      }
      check(!s.settingVisible("vulcan_sprint_speed"),"Vulcan child speed hidden outside its submode");
      ((ChoiceSetting)s.findSetting("sprint")).set(ScaffoldModule.SPRINT_VULCAN_VJ);
      check(s.settingVisible("vulcan_sprint_speed"),"Vulcan child speed visible in its submode");
      ((ChoiceSetting)s.findSetting("sprint")).set(ScaffoldModule.SPRINT_NORMAL);
      check(!s.settingVisible("watchdog_jump_extra_blocks")&&!((BooleanSetting)s.findSetting("watchdog_jump_extra_blocks")).isEnabled(),"Watchdog Jump child defaults off and hidden");
      ((ChoiceSetting)s.findSetting("sprint")).set(ScaffoldModule.SPRINT_WATCHDOG_JUMP_VK);
      check(s.settingVisible("watchdog_jump_extra_blocks"),"Watchdog Jump child visible only in its own submode");
      ((ChoiceSetting)s.findSetting("sprint")).set(ScaffoldModule.SPRINT_NORMAL);
      check(visibilityOk,"visibility resolves every expanded range row, including last settings");
      check(stepsOk,"all Scaffold ranges use source integer steps");
      check(!s.settingVisible("up_side_down")&&!s.settingVisible("start_sneaking")&&!s.settingVisible("watchdog_telly_jump_delay"),"dependent controls hidden by default");
      ((BooleanSetting)s.findSetting("advanced")).set(true);
      ((BooleanSetting)s.findSetting("sneak")).set(true);
      ((BooleanSetting)s.findSetting("watchdog_telly")).set(true);
      check(s.settingVisible("up_side_down")&&s.settingVisible("start_sneaking")&&s.settingVisible("watchdog_telly_jump_delay")&&!s.settingVisible("rotation_block_rotation_speed"),"independent parent controls reveal their children");
      ((BooleanSetting)s.findSetting("rotation_block_boost")).set(true);
      check(s.settingVisible("rotation_block_rotation_speed"),"rotation boost nested condition");
      for(String key:new String[]{"advanced","sneak","watchdog_telly","rotation_block_boost"})((BooleanSetting)s.findSetting(key)).set(false);


      // 5. configure() round-trip (native sync layout: 1 per bool/number/choice, 2 per range)
      double[] vals = new double[expandedRows];
      int c = 0;
      for (Setting<?> x : settings) {
         if (x instanceof BooleanSetting) vals[c++] = ((BooleanSetting) x).isEnabled() ? 1 : 0;
         else if (x instanceof NumberSetting) vals[c++] = ((NumberSetting) x).doubleValue();
         else if (x instanceof ChoiceSetting) vals[c++] = ((ChoiceSetting) x).index();
         else if (x instanceof RangeSetting) { RangeSetting r = (RangeSetting) x; vals[c++] = r.get().lower; vals[c++] = r.get().upper; }
      }
      check(c == expandedRows, "sync value layout matches native row count (" + c + ")");
      NativeFeatureBridge.configure(4, vals);
      check(true, "configure() identity round-trip did not throw");

      // mutate place_delay range through its two lanes and confirm write-back
      RangeSetting pd = (RangeSetting) s.findSetting("place_delay");
      int lane = 0; for (Setting<?> x : settings) { if (x == pd) break; lane += (x instanceof RangeSetting) ? 2 : 1; }
      vals[lane] = 1; vals[lane + 1] = 3; // place_delay range is 0..5
      NativeFeatureBridge.configure(4, vals);
      check(pd.get().lower == 1.0 && pd.get().upper == 3.0, "range write-back: place_delay -> (" + pd.get().lower + "," + pd.get().upper + ")");

      // 6. native event-phase routing: listens() + onEvent dispatch every wired type without error
      int[] wired = {2, 3, 5, 13, 14, 16, 17, 18, 21, 22, 26, 32, 33, 34};
      boolean routeOk = true;
      for (int t : wired) {
         if (!s.listens(t)) { routeOk = false; System.out.println("   not listening to " + t); }
         try { s.onEvent(null, new dev.rice.client.SemanticEvent(t)); }
         catch (Throwable ex) { routeOk = false; System.out.println("   onEvent(" + t + ") threw " + ex); }
      }
      check(routeOk, "listens()+onEvent route all wired native phases 2/3/5/13/14/16/17/18/21/22/26");
      check(!s.listens(30) && !s.listens(99), "does not over-subscribe to unrelated event types");

      // 7. Product status makes the incomplete, unvalidated candidate explicit.
      String st = s.statusText();
      check(st != null && st.contains("experimental"),
         "statusText identifies the experimental candidate");

      System.out.println(fails == 0 ? "\nALL PHASE-1 CHECKS PASSED" : "\n" + fails + " CHECK(S) FAILED");
      System.exit(fails == 0 ? 0 : 1);
   }
}
