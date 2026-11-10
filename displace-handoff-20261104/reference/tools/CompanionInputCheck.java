import dev.rice.client.ClientContext;
import dev.rice.client.KeyState;
import dev.rice.client.action.ActionAccess;
import dev.rice.client.input.InputSoundBridge;
import dev.rice.config.InitialSettings;
import dev.rice.module.Module;
import dev.rice.module.RecoveredUiLayout;
import dev.rice.module.impl.AutoClickerModule;
import dev.rice.module.impl.BridgeAssistModule;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;

/** Verifies actual wrappers and unchanged click/sneak algorithms without loading a native DLL. */
public final class CompanionInputCheck {
   static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
   static int calls,kind,code;static boolean down;
   public static void capture(int type,int key,boolean pressed){calls++;kind=type;code=key;down=pressed;}
   static final class Loader extends ClassLoader {
      Loader(){super(CompanionInputCheck.class.getClassLoader());}
      Class<?> define(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}
   }
   static void bridgeProbe(Path classes)throws Exception{
      final String original="dev/rice/client/input/InputSoundBridge",probe="InputBridgeProbe";
      ClassReader r=new ClassReader(Files.readAllBytes(classes.resolve(original+".class")));ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
      r.accept(new ClassVisitor(Opcodes.ASM9,w){
         @Override public void visit(int version,int access,String name,String sig,String parent,String[] interfaces){super.visit(version,access,probe,sig,parent,interfaces);}
         @Override public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] exceptions){return new MethodVisitor(Opcodes.ASM9,super.visitMethod(access,name,desc,sig,exceptions)){
            @Override public void visitMethodInsn(int opcode,String owner,String method,String desc,boolean itf){if(owner.equals(original))owner=probe;if(owner.equals("dev/rice/inject/NativeBridge")&&method.equals("inputSoundEvent")){owner="CompanionInputCheck";method="capture";}super.visitMethodInsn(opcode,owner,method,desc,itf);}
         };}
      },ClassReader.SKIP_FRAMES);
      Class<?> p=new Loader().define(w.toByteArray());Method mouse=p.getMethod("mouse",boolean.class,boolean.class),key=p.getMethod("key",int.class,boolean.class);
      for(boolean left:new boolean[]{true,false})for(boolean pressed:new boolean[]{true,false}){int before=calls;mouse.invoke(null,left,pressed);check(calls==before+1&&kind==1&&code==(left?0:1)&&down==pressed,"mouse protocol direction/edge");}
      key.invoke(null,42,true);check(kind==2&&code==42&&down,"key down");key.invoke(null,42,false);check(kind==2&&!down,"key up");int before=calls;key.invoke(null,0,true);key.invoke(null,256,true);key.invoke(null,-1,true);check(calls==before,"invalid keyboard binding notified");
      InputSoundBridge.mouse(true,true);InputSoundBridge.mouse(false,false);InputSoundBridge.key(42,true);InputSoundBridge.key(42,false);
   }
   static byte[] stripped(byte[] bytes){ClassWriter w=new ClassWriter(0);new ClassReader(bytes).accept(w,ClassReader.SKIP_DEBUG);return w.toByteArray();}
   static void unchangedAlgorithms(Path classes,Path baseline)throws Exception{
      try(JarFile jar=new JarFile(baseline.toFile())){
         for(String name:new String[]{"AutoClickerModule$State","AutoClickerModule$Settings","BridgeAssistModule$State","BridgeAssistModule$Settings"}){
            String entry="dev/rice/module/impl/"+name+".class";
            java.io.InputStream in=jar.getInputStream(jar.getJarEntry(entry));java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)b.write(buf,0,n);in.close();
            check(Arrays.equals(stripped(b.toByteArray()),stripped(Files.readAllBytes(classes.resolve(entry)))),"algorithm changed beyond debug metadata: "+name);
         }
      }
   }
   static void ordering(Path classes,String name)throws Exception{
      ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(classes.resolve("dev/rice/module/impl/"+name+"$1.class"))).accept(c,0);
      for(MethodNode m:c.methods)if(m.name.equals("emit")){
         int index=0,lastInput=-1,notify=-1;for(AbstractInsnNode i:m.instructions){if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;if(call.owner.equals("dev/rice/client/input/LwjglMouseEvents")||call.owner.endsWith("ActionAccess$MouseHost"))lastInput=index;if(call.owner.equals("dev/rice/client/input/InputSoundBridge")&&call.name.equals("mouse"))notify=index;}index++;}
         check(lastInput>=0&&notify>lastInput,"sound notification precedes or replaces click emission");return;
      }throw new AssertionError("emit closure missing");
   }
   public static void main(String[] args)throws Exception{
      Path classes=Paths.get(args[0]);V115Check.setup();InitialSettings.applyMissing(V115Check.manager);
      check(V115Check.manager.find("sounds")==null,"Rice sound module still registered");
      final List<String> emitted=new ArrayList<>();ActionAccess.MouseHost old=ActionAccess.bindMouse(new ActionAccess.MouseHost(){public boolean physical(int b){return true;}public boolean shift(){return false;}public void emit(ClientContext c,int button,boolean pressed){emitted.add(button+":"+pressed);}});
      try{for(String id:new String[]{"autoclicker","rightclicker"}){
         Module m=V115Check.manager.find(id);check(m!=null,"missing clicker");for(dev.rice.setting.Setting<?> s:m.getSettings())check(!s.getKey().startsWith("sound_"),"audio setting remains in Rice");for(RecoveredUiLayout.Control c:RecoveredUiLayout.controls(m))check(!"Sound".equals(c.section),"audio heading remains");
         ((dev.rice.setting.BooleanSetting)m.findSetting("lunar_clicks")).set(false);Method method=m.getClass().getDeclaredMethod("input",ClientContext.class);method.setAccessible(true);AutoClickerModule.Input input=(AutoClickerModule.Input)method.invoke(m,V115Check.c);int button=id.equals("autoclicker")?1:0;
         for(int i=0;i<25;i++){input.emit(button,true);input.emit(button,false);}check(emitted.size()==50,"absent JNI changed input count");for(int i=0;i<50;i++)check(emitted.get(i).equals(button+":"+(i%2==0)),"input order/button changed");emitted.clear();ordering(classes,m.getClass().getSimpleName());
      }}finally{ActionAccess.bindMouse(old);}
      BridgeAssistModule module=(BridgeAssistModule)V115Check.manager.find("bridgeassist");Method method=module.getClass().getDeclaredMethod("input",ClientContext.class);method.setAccessible(true);BridgeAssistModule.Input sneak=(BridgeAssistModule.Input)method.invoke(module,V115Check.c);
      V115Input.clear();KeyState.set(V115Check.mc.gameSettings.keyBindSneak,false);sneak.sneak(true);check(sneak.sneak(),"Bridge Assist press changed");sneak.sneak(true);check(sneak.sneak(),"repeat press changed");sneak.sneak(false);check(!sneak.sneak(),"release changed");sneak.sneak(true);sneak.restore();check(!sneak.sneak(),"restore physical-up changed");V115Input.keys[42]=true;sneak.restore();check(sneak.sneak(),"restore physical-down changed");V115Input.clear();sneak.restore();
      bridgeProbe(classes);unchangedAlgorithms(classes,Paths.get(args[1]));
      System.out.println("PASS: no Rice audio module/settings; real click edges retained with missing JNI; notification after emission; correct mouse/key protocol; Bridge Assist press/release/repeat/restoration; click and bridge algorithms identical to v127 after debug stripping");
   }
}
