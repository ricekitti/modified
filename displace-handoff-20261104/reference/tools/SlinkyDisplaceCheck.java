import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/** Verifies separate Displaces plus u20+-4 Void geometry, rotation and lifecycle behavior. */
public final class SlinkyDisplaceCheck {
  private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
  private static String sha256(Path path) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (InputStream in = Files.newInputStream(path)) {
      byte[] buffer = new byte[8192];
      for (int n; (n = in.read(buffer)) >= 0;) digest.update(buffer, 0, n);
    }
    StringBuilder result = new StringBuilder();
    for (byte b : digest.digest()) result.append(String.format("%02x", b & 255));
    return result.toString();
  }
  public static void main(String[] args) throws Exception {
    require(args.length == 2, "usage: SlinkyDisplaceCheck <classes> <source-root>");
    Path classes = Paths.get(args[0]), root = Paths.get(args[1]);
    Path displace = root.resolve("java/src/main/java/dev/rice/module/impl/KnockbackDisplaceModule.java");
    behavior(classes, root);
    require(Files.exists(classes.resolve("dev/rice/module/impl/KnockbackDisplaceModule.class")), "Slinky Displace missing from payload");
    require(!Files.exists(classes.resolve("dev/rice/client/combat/openb4/B4DisplaceHost.class")), "OpenB4 Displace host remained in payload");
    require(!Files.exists(classes.resolve("dev/rice/client/combat/openb4/OpenB4DisplaceBackend.class")), "OpenB4 Displace backend remained in payload");
    String aura = new String(Files.readAllBytes(root.resolve("java/src/main/java/dev/rice/module/impl/KillAuraModule.java")), StandardCharsets.UTF_8);
    String backend = new String(Files.readAllBytes(root.resolve("java/src/main/java/dev/rice/client/combat/ravenbs/RavenBsKillAuraBackend.java")), StandardCharsets.UTF_8);
    String clicker = new String(Files.readAllBytes(root.resolve("java/src/main/java/dev/rice/module/impl/AutoClickerModule.java")), StandardCharsets.UTF_8);
    require(!aura.contains("B4DisplaceHost") && !aura.contains("coordinated"), "KillAura retained Raven Displace coupling");
    require(!backend.contains("OpenB4KillAuraBackend"), "active KillAura still uses OpenB4 backend");
    require(!clicker.contains("onSlinkyAutoClick"), "AutoClicker retained hybrid Displace interception");
    // Two separate displaces: v72 for manual/AutoClicker/AimAssist, OpenB4 KillAura Displace for KillAura hits.
    require(Files.exists(classes.resolve("dev/rice/module/impl/KillAuraDisplaceModule.class")), "KillAura Displace missing from payload");
    require(!Files.exists(classes.resolve("dev/rice/client/combat/openb4/AuraDisplaceAdapter.class")), "packet-rewriting KillAura/Displace adapter remained in payload");
    String kaDisplace = new String(Files.readAllBytes(root.resolve("java/src/main/java/dev/rice/module/impl/KillAuraDisplaceModule.java")), StandardCharsets.UTF_8);
    require(!kaDisplace.contains("KnockbackDisplaceModule"), "KillAura Displace is coupled to the v72 Knockback Displace");
    require(aura.contains("RavenBsKillAuraBackend") && kaDisplace.contains("RavenBsDisplaceBackend"), "Raven bS combat backends are not wired");
    require(!aura.contains("AuraDisplaceAdapter"), "KillAura module retained the packet-rewriting adapter");
    System.out.println("SlinkyDisplaceCheck: PASS (Raven bS full-circle Void, behind-attacker yaw, radius, deep support, walls, unloaded chunks, regular click/config wiring; bS aura Void and existing attack pairing; lifecycle)");
  }

  static final class Terrain extends net.minecraft.client.multiplayer.WorldClient {
    java.util.function.BiPredicate<Double,Double> solid;
    Terrain(){super(null,null,0,null,null);}
    @Override public net.minecraft.util.MovingObjectPosition rayTraceBlocks(net.minecraft.util.Vec3 a,net.minecraft.util.Vec3 b,boolean l,boolean n,boolean m) {
      require(a.yCoord-b.yCoord==6.0,"Void ray depth changed");
      return solid.test(a.xCoord,a.zCoord)?new net.minecraft.util.MovingObjectPosition(a,net.minecraft.util.EnumFacing.UP,net.minecraft.util.BlockPos.ORIGIN):null;
    }
  }
  static final class BsTerrain extends net.minecraft.client.multiplayer.WorldClient {
    java.util.function.BiPredicate<Integer,Integer> floor;
    java.util.function.Predicate<net.minecraft.util.BlockPos> wall;
    boolean loaded = true;
    BsTerrain(){super(null,null,0,null,null);}
    @Override public net.minecraft.entity.Entity getEntityByID(int id){if(loadedEntityList!=null)for(Object item:loadedEntityList){net.minecraft.entity.Entity e=(net.minecraft.entity.Entity)item;if(e.getEntityId()==id)return e;}return null;}
    @Override public java.util.List<net.minecraft.entity.Entity> getEntitiesWithinAABBExcludingEntity(net.minecraft.entity.Entity except,net.minecraft.util.AxisAlignedBB box){java.util.List<net.minecraft.entity.Entity> out=new java.util.ArrayList<>();if(loadedEntityList!=null)for(Object item:loadedEntityList){net.minecraft.entity.Entity e=(net.minecraft.entity.Entity)item;if(e!=except&&e.getEntityBoundingBox().intersectsWith(box))out.add(e);}return out;}
    @Override public boolean isBlockLoaded(net.minecraft.util.BlockPos pos,boolean allowEmpty){return loaded;}
    @Override public net.minecraft.block.state.IBlockState getBlockState(net.minecraft.util.BlockPos pos) {
      boolean solid = pos.getY()==64 && floor.test(pos.getX(),pos.getZ()) || wall!=null && wall.test(pos);
      return (solid?net.minecraft.init.Blocks.stone:net.minecraft.init.Blocks.air).getDefaultState();
    }
  }
  static Object field(Object o,String name)throws Exception {
    java.lang.reflect.Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);
  }
  static void behavior(Path classes,Path root)throws Exception {
    Class<?> u=Class.forName("sun.misc.Unsafe");java.lang.reflect.Field uf=u.getDeclaredField("theUnsafe");uf.setAccessible(true);
    OpenB4KillAuraCheck.unsafe=uf.get(null);OpenB4KillAuraCheck.allocate=u.getMethod("allocateInstance",Class.class);
    net.minecraft.init.Bootstrap.register();
    // Compile the actual linked release's dropSide method as an independent geometry oracle.
    String upstream=new String(Files.readAllBytes(root.resolve("tools/fixtures/openb4/WorldUtils-u20plus4.java.txt")),StandardCharsets.UTF_8);
    int start=upstream.indexOf('{',upstream.indexOf("public static int dropSide")),end=start+1,depth=1;
    while(depth>0){char ch=upstream.charAt(end++);if(ch=='{')depth++;else if(ch=='}')depth--;}
    String body=upstream.substring(start,end).replace("mc.thePlayer","player").replace("mc.theWorld","world");
    Path temp=Files.createTempDirectory("rice-raven-void-");
    Path source=temp.resolve("RavenVoidOracle.java");
    Files.write(source,("import net.minecraft.entity.Entity;import net.minecraft.world.World;import net.minecraft.util.Vec3;public class RavenVoidOracle {public static int dropSide(Entity player,World world,Entity entity,int previous)"+body+"}").getBytes(StandardCharsets.UTF_8));
    require(javax.tools.ToolProvider.getSystemJavaCompiler().run(null,null,null,"--release","8","-cp",System.getProperty("java.class.path"),"-d",temp.toString(),source.toString())==0,"release Void oracle compilation failed");
    try(java.net.URLClassLoader oracleLoader=new java.net.URLClassLoader(new java.net.URL[]{temp.toUri().toURL()})) {
      java.lang.reflect.Method oracle=oracleLoader.loadClass("RavenVoidOracle").getMethod("dropSide",net.minecraft.entity.Entity.class,net.minecraft.world.World.class,net.minecraft.entity.Entity.class,int.class);
      RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);
      Terrain terrain=OpenB4KillAuraCheck.blank(Terrain.class);f.mc.theWorld=terrain;
      f.self.posX=0;f.self.posZ=0;f.enemy.posX=3;f.enemy.posZ=0;
      terrain.solid=(x,z)->true;
      require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,-1)==-1,"solid platform chose Void");
      terrain.solid=(x,z)->z>=-0.5;
      require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,-1)==0,"left edge missed");
      terrain.solid=(x,z)->z<=0.5;
      require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,-1)==1,"right edge missed");
      terrain.solid=(x,z)->x<=3.5;
      require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,-1)==-1,"straight drop should keep natural knockback");
      terrain.solid=(x,z)->Math.abs(z)<=.5;
      require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,0)==0 && dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,1)==1,"equal side continuity lost");
      java.util.Random random=new java.util.Random(169);
      for(int i=0;i<400;i++) {
        double bearing=random.nextDouble()*Math.PI*2;f.enemy.posX=Math.cos(bearing)*3;f.enemy.posZ=Math.sin(bearing)*3;
        final long seed=random.nextLong();terrain.solid=(x,z)->new java.util.Random(seed ^ ((long)Math.floor(x)*73856093L) ^ ((long)Math.floor(z)*19349663L)).nextBoolean();
        int previous=i%3-1;
        int expected=(Integer)oracle.invoke(null,f.self,terrain,f.enemy,previous);
        require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,previous)==expected,"normal Void differs from linked release at map "+i);
      }
      f.enemy.posX=f.self.posX;f.enemy.posZ=f.self.posZ;
      require(dev.rice.client.combat.RavenCombatBackend.dropSide(f.c,f.enemy,1)==-1,"overlapping target chose Void");
      f.close();
    }
    ravenBsBehavior(classes,root);
    RavenBsCombatCheck.displaceBehavior(classes);
    emptyHandBehavior(classes);
    // Registered manual module: both directions use angle +/-5 with no pitch kick.
    {
      RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);
      f.self.getAttributeMap().registerAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed);
      f.self.setSprinting(false);f.mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
      dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);dev.rice.client.SemanticEvent click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
      require(click.cancelled&&(Boolean)field(f.v72,"active")&&f.self.isSprinting()&&(Boolean)field(f.v72,"sprintOwned"),"plain-item Displace did not prime a real sprint hit");
      f.v72.onEvent(f.c,new dev.rice.client.SemanticEvent(18));dev.rice.client.SemanticEvent after=new dev.rice.client.SemanticEvent(15);after.active=true;f.manager.dispatch(after);
      require(!(Boolean)field(f.v72,"sprintOwned")&&!f.self.isSprinting(),"plain-item sprint pulse leaked after the queued hit");f.close();
    }
    {
      RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);((dev.rice.setting.BooleanSetting)f.v72.findSetting("require_kb_enchant")).set(true);
      f.self.getAttributeMap().registerAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed);
      f.self.setSprinting(false);f.mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
      dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);dev.rice.client.SemanticEvent click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
      require(!click.cancelled&&!(Boolean)field(f.v72,"active")&&!f.self.isSprinting(),"required-enchantment gate accepted a plain item");
      f.self.inventory.mainInventory[0]=new net.minecraft.item.ItemStack(net.minecraft.init.Items.stick);f.self.inventory.mainInventory[0].addEnchantment(net.minecraft.enchantment.Enchantment.knockback,1);
      dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
      require(click.cancelled&&(Boolean)field(f.v72,"active")&&!f.self.isSprinting()&&!(Boolean)field(f.v72,"sprintOwned"),"current enchanted stick was not recognized immediately");f.close();
    }
    for(int direction=0;direction<2;direction++) {
      RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);
      ((dev.rice.setting.ChoiceSetting)f.v72.findSetting("direction")).set(direction);
      f.manager.requestEnabled(f.aura,false);f.manager.applyPendingChanges();
      f.mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
      dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);
      float yaw=f.self.rotationYaw,pitch=f.self.rotationPitch;
      dev.rice.client.SemanticEvent click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
      float offset=dev.rice.client.rotation.RotationMath.wrap(f.self.rotationYaw-yaw);
      require(click.cancelled && (Boolean)field(f.v72,"active"),"manual Displace did not arm");
      require(offset*(direction==0?-1:1)>=84.8 && offset*(direction==0?-1:1)<=95.2,"manual yaw outside Raven angle +/-5");
      require(f.self.rotationPitch==pitch,"manual Displace changed pitch");
      f.manager.requestEnabled(f.v72,false);f.manager.applyPendingChanges();
      require(!(Boolean)field(f.v72,"active") && !(Boolean)field(f.v72,"packetPending"),"disable retained a queued manual hit/hold");
      f.close();
    }
    RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);
    f.mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
    dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);
    dev.rice.client.SemanticEvent click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
    require((Boolean)field(f.v72,"active"),"focus test failed to arm");
    f.mc.inGameHasFocus=false;f.v72.onEvent(f.c,new dev.rice.client.SemanticEvent(18));
    require(!(Boolean)field(f.v72,"active") && !(Boolean)field(f.v72,"packetPending"),"focus loss retained manual hit/hold");f.close();
  }

  static void emptyHandBehavior(Path classes)throws Exception {
    require(dev.rice.module.impl.WeaponsModule.enchantmentGroups(null)==0,"empty hand has enchantment groups");
    for(boolean requireWeapon:new boolean[]{false,true})for(boolean allowFist:new boolean[]{false,true})for(boolean requireEnchant:new boolean[]{false,true}) {
      RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);
      try {
        f.self.inventory.mainInventory[0]=null;
        dev.rice.module.impl.WeaponsModule weapons=(dev.rice.module.impl.WeaponsModule)field(f.v72,"weapons");
        ((dev.rice.setting.BooleanSetting)weapons.findSetting("fist")).set(allowFist);
        ((dev.rice.setting.BooleanSetting)f.v72.findSetting("require_weapon")).set(requireWeapon);
        ((dev.rice.setting.BooleanSetting)f.v72.findSetting("require_kb_enchant")).set(requireEnchant);
        f.self.getAttributeMap().registerAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed);
        f.self.setSprinting(false);
        f.mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
        dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);
        dev.rice.client.SemanticEvent click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
        boolean expected=(!requireWeapon||allowFist)&&!requireEnchant;
        require(click.cancelled==expected&&(Boolean)field(f.v72,"active")==expected,"empty-hand attack ignored weapon/enchantment gates");
        require(f.self.isSprinting()==expected&&(Boolean)field(f.v72,"sprintOwned")==expected,"empty-hand sprint ownership incorrect");
        if(expected) {
          f.v72.onEvent(f.c,new dev.rice.client.SemanticEvent(18));
          dev.rice.client.SemanticEvent after=new dev.rice.client.SemanticEvent(15);after.active=true;f.manager.dispatch(after);
          require(!f.self.isSprinting()&&!(Boolean)field(f.v72,"sprintOwned"),"empty-hand sprint pulse leaked after hit");
        }
      } finally {f.close();}
    }
    System.out.println("Empty-hand Displace: PASS (attack dispatch, all weapon/fist/enchantment gates, sprint cleanup)");
  }

  static Float scan(RavenBsCombatCheck.Fixture f,double radius,boolean ignore) {
    return dev.rice.client.combat.RavenBsVoidScanner.findYaw(f.c,f.enemy,-90,90,radius,ignore);
  }
  static void ravenBsBehavior(Path classes,Path root)throws Exception {
    RavenBsCombatCheck.Fixture f=new RavenBsCombatCheck.Fixture(classes,true,false);
    BsTerrain terrain=OpenB4KillAuraCheck.blank(BsTerrain.class);terrain.loaded=true;f.mc.theWorld=terrain;
    f.self.posX=0;f.self.posZ=0;f.enemy.posX=3;f.enemy.posZ=0;
    f.enemy.setEntityBoundingBox(new net.minecraft.util.AxisAlignedBB(2.7,65,-.3,3.3,66.8,.3));
    terrain.floor=(x,z)->true;
    require(scan(f,6,false)==null,"bS Void found a drop on solid ground");
    terrain.floor=(x,z)->x>=-1;
    Float yaw=scan(f,6,false);
    require(yaw!=null && Math.abs(dev.rice.client.rotation.RotationMath.wrap(yaw-90))<35,"bS scan missed the drop behind the attacker: "+yaw);
    require(scan(f,2,false)==null,"bS scan ignored configured radius");
    terrain.loaded=false;require(scan(f,6,false)==null,"unloaded terrain counted as void");terrain.loaded=true;
    terrain.floor=(x,z)->x<=5;
    yaw=scan(f,6,false);
    require(yaw!=null && Math.abs(dev.rice.client.rotation.RotationMath.wrap(yaw+90))<35,"bS scan missed forward drop: "+yaw);
    terrain.floor=(x,z)->true;
    // A destination with support far below is not a true void column.
    terrain.floor=(x,z)->x>=-1;terrain.wall=pos->pos.getY()==5;
    require(scan(f,6,false)==null,"deep floor incorrectly counted as void");
    terrain.wall=pos->pos.getX()==1 && pos.getY()>=65 && pos.getY()<=68;
    require(scan(f,6,false)==null,"bS scan passed a tall wall");
    terrain.wall=pos->pos.getX()==1 && pos.getY()==65;
    require(scan(f,6,false)==null,"one-block wall was ignored with option disabled");
    require(scan(f,6,true)!=null,"one-block wall option did not allow the path");terrain.wall=null;
    // Real regular Displace attack hook must use the absolute chosen yaw, without angle jitter.
    f.manager.requestEnabled(f.aura,false);f.manager.applyPendingChanges();
    ((dev.rice.setting.ChoiceSetting)f.v72.findSetting("direction")).set(5);
    require(f.v72.visibleSettings().contains(f.v72.findSetting("void_scan_radius"))
      && f.v72.visibleSettings().contains(f.v72.findSetting("void_ignore_one_block_wall")),"Void controls missing from the rendered menu");
    f.self.rotationYaw=-90;f.self.rotationPitch=11;
    f.mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
    dev.rice.client.rotation.RotationService.beginFrame(f.c,0,0);
    Float expected=scan(f,6,false);
    dev.rice.client.SemanticEvent click=new dev.rice.client.SemanticEvent(14);click.active=true;f.manager.dispatch(click);
    require(click.cancelled && (Boolean)field(f.v72,"active"),"bS Void did not arm from regular attack");
    require(expected!=null && Math.abs(dev.rice.client.rotation.RotationMath.wrap(f.self.rotationYaw-expected))<.2,"regular Void failed to apply the bS yaw");
    require(f.self.rotationPitch==11,"bS Void changed pitch");
    com.google.gson.JsonObject config=dev.rice.config.RecoveredConfiguration.serialize(f.manager,dev.rice.config.RecoveredConfiguration.Scope.PROFILE,java.util.Collections.emptyList());
    com.google.gson.JsonObject regular=config.getAsJsonObject("Knockback Displace");
    require(regular.has("void_scan_radius") && regular.has("void_ignore_one_block_wall"),"bS controls omitted from saved profile");
    f.close();
  }

}
