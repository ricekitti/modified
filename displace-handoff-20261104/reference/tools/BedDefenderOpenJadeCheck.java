import dev.rice.module.Module;
import dev.rice.module.impl.BedDefenderModule;
import dev.rice.client.openjade.OpenJadeBedDefenderBackend;
import dev.rice.setting.ChoiceSetting;
import dev.rice.config.SettingCodec;
import com.google.gson.JsonPrimitive;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;
import java.lang.reflect.*;

/** Restoration acceptance: original compiled OpenJade code, settings, and removal of the additions. */
public final class BedDefenderOpenJadeCheck {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static Map<String,byte[]> entries(Path jar)throws Exception {
  Map<String,byte[]> result=new TreeMap<>();
  String[] prefixes={"dev/rice/client/openjade/OpenJadeBedDefenderBackend","dev/rice/module/impl/BedDefenderModule","dev/rice/module/impl/GuiModule","dev/rice/client/GuiModuleSettings","dev/rice/client/render/GuiWidgets","dev/rice/client/render/InlineModuleSettings"};
  try(JarFile file=new JarFile(jar.toFile())){Enumeration<JarEntry> rows=file.entries();while(rows.hasMoreElements()){
   JarEntry row=rows.nextElement();boolean wanted=false;for(String prefix:prefixes)if(row.getName().startsWith(prefix)&&row.getName().endsWith(".class"))wanted=true;
   if(!wanted)continue;try(InputStream in=file.getInputStream(row)){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)bytes.write(b,0,n);result.put(row.getName(),bytes.toByteArray());}
  }}return result;
 }
 public static void main(String[] args)throws Exception {
  for(String mapping:new String[]{"101","102","103"}) {
   Map<String,byte[]> before=entries(Paths.get(args[0],"payload-"+mapping+".jar")),after=entries(Paths.get(args[1],"payload-"+mapping+".jar"));
   check(!before.isEmpty()&&before.keySet().equals(after.keySet()),"Original restoration class inventory "+mapping);
   for(String name:before.keySet())check(Arrays.equals(before.get(name),after.get(name)),"Original class bytes differ "+mapping+" "+name);
   try(JarFile jar=new JarFile(Paths.get(args[1],"payload-"+mapping+".jar").toFile())){
    Enumeration<JarEntry> rows=jar.entries();while(rows.hasMoreElements()){String name=rows.nextElement().getName();check(!name.contains("BedDefenderCustomizeScreen")&&!name.contains("DefenseLayouts")&&!name.contains("HotbarMaterialAnalyzer"),"Removed addon still packaged: "+name);}
   }
   System.out.println("PASS: original v130 OpenJade/backend/GUI class bytes restored; addon entries absent; mapping "+mapping);
  }
  V115Check.setup();
  Module module=V115Check.manager.find("bed_defender");check(module instanceof BedDefenderModule,"Original module still registered");
  check(module.getSettings().size()==6&&module.visibleSettings().size()==6,"Normal original settings panel restored");
  for(String key:new String[]{"only_whitelisted_bed","swap_speed","aim_speed","sneak_speed","fov","defense"})check(module.findSetting(key)!=null,"Original key preserved: "+key);
  ChoiceSetting defense=(ChoiceSetting)module.findSetting("defense");
  check(defense.getOptions().equals(Arrays.asList("End Long","End Wool Long","1 Layer Wool")),"Only original OpenJade defenses");
  for(int i=0;i<3;i++){SettingCodec.decode(defense,new JsonPrimitive(defense.getOptions().get(i)));check(defense.index()==i,"Original saved defense loads");}
  defense.set(0);SettingCodec.decode(defense,new JsonPrimitive("EndGlassWool"));check(defense.index()==0,"Removed imported choice safely ignored");
  for(String key:new String[]{"smart_select","customize","enable_0","enable_10"})check(module.findSetting(key)==null,"Removed control: "+key);
  for(String name:new String[]{"dev.rice.client.BedDefenderCustomizeScreen","dev.rice.client.openjade.DefenseLayouts","dev.rice.client.openjade.HotbarMaterialAnalyzer"}){
   try{Class.forName(name);throw new AssertionError("Removed class remains loadable: "+name);}catch(ClassNotFoundException expected){}
  }
  Field f=OpenJadeBedDefenderBackend.class.getDeclaredField("DEFENSE_PATTERNS");f.setAccessible(true);Object[][][] layouts=(Object[][][])f.get(null);
  check(layouts.length==3&&layouts[0].length==12&&layouts[1].length==35&&layouts[2].length==8,"Original three pattern sizes");
  check(!module.isEnabled()&&V115Check.controller.placements.isEmpty(),"Inspection does not activate or place");
  System.out.println("PASS: original registry/settings/profile choices/patterns; smart/custom controls and imported classes removed.");
 }
}
