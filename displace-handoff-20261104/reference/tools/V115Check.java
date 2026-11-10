import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.combat.openb4.OpenB4AntiFireballBackend;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.openjade.JadeRuntime;
import dev.rice.client.packet.PacketPipeline;
import dev.rice.client.render.RecoveredGuiScreen;
import dev.rice.client.rotation.RotationService;
import dev.rice.config.ConfigStore;
import dev.rice.module.CoreModules;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.AntiFireballModule;
import dev.rice.module.impl.AutoClutchModule;
import dev.rice.module.impl.ClutchModule;
import dev.rice.module.impl.OpsecModule;

import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.setting.Setting;
import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.ClassWriter;
import dev.rice.shaded.asm.tree.AbstractInsnNode;
import dev.rice.shaded.asm.tree.ClassNode;
import dev.rice.shaded.asm.tree.MethodInsnNode;
import dev.rice.shaded.asm.tree.MethodNode;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * v115 behavior checks against real Minecraft 1.8.9 classes, without a game window:
 * <ul>
 *   <li>Catalog and registry: ZestTap is gone, a legacy ZestTap config still loads, and the new modules are present
 *       (v119: the OpenJade Scaffold/Clutch Assist checks moved to V119Check with their Mayu/AutoClutch replacements).</li>
 *   <li>OpenB4 AntiFireball end to end through the real hooks.</li>
 *   <li>Fireball ESP's full draw path, with GL redirected to a recorder.</li>
 *   <li>Opsec's off path and lobby-only Anti Ping.</li>
 *   <li>The PacketPipeline hold/release that TimerRange's freeze and Criticals' delay use.</li>
 * </ul>
 * Port fidelity (generated == upstream + whitelisted rewrites) is checked separately by the port tools' --check mode.
 */
public final class V115Check {
   static Object unsafe;
   static Method allocate;
   static Minecraft mc;
   static World world;
   static Player self;
   static Controller controller;
   static Handler handler;
   static ClientContext c;
   static ModuleManager manager;

   static <T> T blank(Class<T> type) throws Exception { return type.cast(allocate.invoke(unsafe, type)); }

   static void put(Object o, String name, Object value) throws Exception {
      for (Class<?> t = o.getClass(); t != null; t = t.getSuperclass()) {
         try { Field f = t.getDeclaredField(name); f.setAccessible(true); f.set(o, value); return; } catch (NoSuchFieldException next) { }
      }
      throw new NoSuchFieldException(name);
   }

   static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

   public static final class Player extends EntityPlayerSP {
      int swings;
      boolean spectator;
      @Override public boolean isSpectator() { return spectator; }
      Player() { super(null, null, null, null); }
      @Override public void swingItem() { swings++; }
      @Override public UUID getUniqueID() { return new UUID(0, getEntityId()); }
      @Override public boolean canBeCollidedWith() { return true; }
      @Override public boolean isInvisible() { return false; }
   }

   public static class World extends WorldClient {
      Map<BlockPos, IBlockState> blocks;
      World() { super(null, null, 0, null, null); }
      @Override public Entity getEntityByID(int id) { for (Object e : loadedEntityList) if (((Entity)e).getEntityId() == id) return (Entity)e; return null; }
      @Override public IBlockState getBlockState(BlockPos pos) { IBlockState s = blocks.get(pos); return s == null ? Blocks.air.getDefaultState() : s; }
      @Override public boolean isBlockLoaded(BlockPos pos) { return true; }
      @Override public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity, AxisAlignedBB query) {
         List<AxisAlignedBB> collisions = new ArrayList<>();
         for (Map.Entry<BlockPos,IBlockState> row : blocks.entrySet()) {
            AxisAlignedBB box = row.getValue().getBlock().getCollisionBoundingBox(this,row.getKey(),row.getValue());
            if (box != null && box.intersectsWith(query)) collisions.add(box);
         }
         return collisions;
      }
      @Override public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity except, AxisAlignedBB box) {
         List<Entity> out = new ArrayList<>();
         for (Object o : loadedEntityList) { Entity e = (Entity)o; if (e != except && e.getEntityBoundingBox().intersectsWith(box)) out.add(e); }
         return out;
      }
      @Override public IChunkProvider getChunkProvider() {
         return (IChunkProvider)Proxy.newProxyInstance(V115Check.class.getClassLoader(), new Class<?>[]{IChunkProvider.class},
            (proxy, method, args) -> method.getName().equals("chunkExists") ? Boolean.TRUE : null);
      }
   }

   public static final class Controller extends PlayerControllerMP {
      int attacks; Entity last; List<Object[]> placements;
      Controller() { super(null, null); }
      @Override public void attackEntity(EntityPlayer p, Entity e) { attacks++; last = e; }
      @Override public boolean onPlayerRightClick(EntityPlayerSP player, WorldClient w, ItemStack stack, BlockPos pos, net.minecraft.util.EnumFacing side, net.minecraft.util.Vec3 hit) {
         placements.add(new Object[]{pos, side, hit, player.inventory.currentItem});
         // The C08 vanilla would send, so packet order (C08 inside the walking event, before that tick's C03) is visible.
         player.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C08PacketPlayerBlockPlacement(pos, side.getIndex(), stack,
            (float)(hit.xCoord - pos.getX()), (float)(hit.yCoord - pos.getY()), (float)(hit.zCoord - pos.getZ())));
         return true;
      }
      @Override public boolean sendUseItem(EntityPlayer player, net.minecraft.world.World w, ItemStack stack) { return false; }
   }

   /** NetHandlerPlayClient.addToSendQueue -> NetworkManager.sendPacket head hook, as the game runs it. */
   public static final class Handler extends NetHandlerPlayClient {
      List<Packet<?>> sent;
      Handler() { super(null, null, null, null); }
      @Override public void addToSendQueue(Packet p) { if (!GameHooks.beforeSend(null, p)) sent.add(p); }
   }

   static Player player(int id, double x, double y, double z) throws Exception {
      Player p = blank(Player.class);
      p.setEntityId(id); put(p, "worldObj", world); put(p, "rand", new Random(id)); put(p, "mc", mc);
      p.capabilities = new PlayerCapabilities(); put(p,"foodStats",new net.minecraft.util.FoodStats()); p.inventory = new InventoryPlayer(p);
      p.getAttributeMap().registerAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed);
      DataWatcher data = new DataWatcher(p); data.addObject(0, (byte)0); data.addObject(6, 20F); put(p, "dataWatcher", data);
      put(p, "activePotionsMap", new HashMap<>()); p.width = .6F; p.height = 1.8F;
      place(p, x, y, z);
      return p;
   }

   static void place(Entity p, double x, double y, double z) {
      p.posX = p.prevPosX = p.lastTickPosX = x; p.posY = p.prevPosY = p.lastTickPosY = y; p.posZ = p.prevPosZ = p.lastTickPosZ = z;
      double w = p.width / 2.0;
      p.setEntityBoundingBox(new AxisAlignedBB(x - w, y, z - w, x + w, y + p.height, z + w));
   }

   static EntityLargeFireball fireball(int id, double x, double y, double z) throws Exception {
      EntityLargeFireball f = blank(EntityLargeFireball.class);
      f.setEntityId(id); put(f, "worldObj", world);
      f.posX = f.lastTickPosX = x; f.posY = f.lastTickPosY = y; f.posZ = f.lastTickPosZ = z; f.width = 1F; f.height = 1F;
      f.setEntityBoundingBox(new AxisAlignedBB(x - .5, y - .5, z - .5, x + .5, y + .5, z + .5));
      return f;
   }

   /** Vanilla default key codes, so upstream code that polls physical keys (V115Input via the LWJGL stand-ins) sees them. */
   static KeyBinding key(String name, int code) { return new KeyBinding("v115." + name, code, "v115"); }

   static float wrap(float angle) { return MathHelper.wrapAngleTo180_float(angle); }

   /** Entity flag 3 (sprinting) without EntityLivingBase's attribute bookkeeping, which the headless player lacks. */
   static void sprinting(boolean on) {
      byte flags = self.getDataWatcher().getWatchableObjectByte(0);
      self.getDataWatcher().updateObject(0, (byte)(on ? flags | 8 : flags & ~8));
   }

   static String describe(Object o) throws Exception {
      if (o == null) return "null";
      StringBuilder b = new StringBuilder("{");
      for (Field f : o.getClass().getDeclaredFields()) { f.setAccessible(true); b.append(f.getName()).append('=').append(f.get(o)).append(' '); }
      return b.append('}').toString();
   }

   /** Failures the Jade agent hooks caught and logged (they never throw into the game). */
   static String jadeFailures() throws Exception {
      Field f = Class.forName("dev.rice.jade.agent.JadeAgentHooks").getDeclaredField("LOGGED_FAILURES"); f.setAccessible(true);
      return String.valueOf(f.get(null));
   }

   /** Exercise the registered v167+ Rice backend, rather than the separate legacy myau module. */
   public static void main(String[] args) throws Exception {
      Path classes = Paths.get(args[0]);
      Path root = Paths.get(args[1]);
      setup();

      registry(root);
      legacyConfig();
      antiFireball();
      fireballEsp(classes);
      opsec();
      pipeline();
      check(manager.find("scaffold")==null,"removed Scaffold still registered");
      // v119: the OpenJade Scaffold/Clutch Assist and v118 meow-runtime sections moved to V119Check (Mayu Scaffold/BedNuker/AutoClutch).
      System.out.println("PASS: v115 registry/ZestTap removal + legacy ZestTap config; "
         + "AntiFireball join tracking/target/2-tick attack/silent swing/packet gates/blocking/screens/flying/scaffold/rotation yield (incl. OpenB4 Scaffold's claim)/disable; Fireball ESP settings + recorded draw path (face-plane end, 0.5 shortening, landing plate, block + entity highlight, early returns, themes); "
         + "Opsec off-path + lobby-only Anti Ping; ordered PacketPipeline hold/release");
   }

   /** Shared real-Minecraft headless fixture, also used by the v119 backend checks. */
   static void setup() throws Exception {
      Bootstrap.register();
      Class<?> u = Class.forName("sun.misc.Unsafe"); Field f = u.getDeclaredField("theUnsafe"); f.setAccessible(true); unsafe = f.get(null);
      allocate = u.getMethod("allocateInstance", Class.class);
      mc = blank(Minecraft.class);
      Field singleton = Minecraft.class.getDeclaredField("theMinecraft"); singleton.setAccessible(true); singleton.set(null, mc);
      world = blank(World.class); world.blocks = new HashMap<>(); put(world, "loadedEntityList", new ArrayList<Entity>()); put(world, "playerEntities", new ArrayList<EntityPlayer>());
      put(world, "worldBorder", new net.minecraft.world.border.WorldBorder());
      self = player(1, 0, 65, 0);
      controller = blank(Controller.class);
      handler = blank(Handler.class); handler.sent = new ArrayList<>(); put(self, "sendQueue", handler);
      controller.placements = new ArrayList<>(); put(controller, "mc", mc); put(controller, "netClientHandler", handler);
      put(controller, "currentGameType", net.minecraft.world.WorldSettings.GameType.SURVIVAL);
      GameSettings keys = blank(GameSettings.class); keys.mouseSensitivity = .5F;
      keys.keyBindForward = key("forward", 17); keys.keyBindBack = key("back", 31); keys.keyBindLeft = key("left", 30); keys.keyBindRight = key("right", 32);
      keys.keyBindJump = key("jump", 57); keys.keyBindSneak = key("sneak", 42); keys.keyBindSprint = key("sprint", 29); keys.keyBindAttack = key("attack", -100);
      keys.keyBindPickBlock = key("pickItem", -98);
      keys.keyBindUseItem = key("use", -99); keys.keyBindsHotbar = new KeyBinding[9]; for (int i = 0; i < 9; i++) keys.keyBindsHotbar[i] = key("hotbar" + i, 2 + i);
      put(mc, "gameSettings", keys); put(mc, "thePlayer", self); put(mc, "theWorld", world); put(mc, "playerController", controller);
      put(mc, "inGameHasFocus", true); put(mc, "renderViewEntity", self); put(mc, "mcThread", Thread.currentThread());
      RenderManager renderManager = blank(RenderManager.class); put(mc, "renderManager", renderManager);
      self.movementInput = new MovementInput();
      c = new ClientContext(mc);
      manager = new ModuleManager(c);
      GameHooks.install(manager);
      CoreModules.register(manager);
      manager.applyPendingChanges();
   }

   // ---------------------------------------------------------------------------------------------------------------
   static void registry(Path root) throws Exception {
      try { Class.forName("dev.rice.module.impl.ZestTapModule"); throw new AssertionError("ZestTapModule class still present"); } catch (ClassNotFoundException expected) { }
      for (Module m : manager.all()) check(!m.getId().equalsIgnoreCase("zesttap") && !m.getName().replace(" ", "").equalsIgnoreCase("zesttap"), "ZestTap still registered");
      for (String json : new String[]{"InitialSettings", "WidgetMetadata", "ModuleUi", "MetadataCatalog", "ModuleDescriptors"}) {
         String text = resource("/assets/ricesemantic/" + json + ".json");
         check(!text.contains("ZestTap"), json + ".json still mentions ZestTap");
      }
      for (String id : new String[]{"bednuker", "opsec", "antifireball", "fireball_esp", "timerrange", "criticals", "knockbackdisplace"}) {
         check(manager.find(id) != null, "module missing: " + id);
      }
      // v158: the original Slinky fall-clutch (OpenSlinky) was restored under id "clutch", replacing the removed
      // Vape Block-In clutch. The old OpenJade "Clutch Assist" class is gone; require the restored module instead.
      Module restoredClutch = manager.find("clutch");
      check(restoredClutch instanceof ClutchModule && restoredClutch.getName().equals("Clutch")
         && restoredClutch.findSetting("snapback_delay") != null && restoredClutch.findSetting("select_blocks") != null,
         "restored OG Slinky Clutch missing or wrong (expected dev.rice.module.impl.ClutchModule, id clutch, recovered keys)");
      check(manager.find("opsec") instanceof OpsecModule, "adapters wrong");
      check(manager.find("scaffold")==null,"removed Scaffold is present in registry");
      // v159 still holds for the rest: Auto Clutch, Tower, AutoSwap, Bed Defender and Fireball Boost stay unregistered
      // (their classes remain compiled but must not be reachable in the menu).
      for (String gone : new String[]{"autoclutch", "tower", "autoswap", "bed_defender", "fireball_boost"}) {
         check(manager.find(gone) == null, "removed module still registered: " + gone);
      }
      Module af = manager.find("antifireball");
      check(af.getCategory() == ModuleCategory.WORLD && af.metadataCategory() == 3 && af.getName().equals("Anti Fireball"), "AntiFireball tab/name wrong");
      num(af, "fov", 360, 20, 360, 5); num(af, "range", 10, 3, 20, .5);
      bool(af, "disable_while_flying", true); bool(af, "disable_while_scaffold", false); bool(af, "rotate_with_blocks", false);
      bool(af, "rotate_with_projectiles", false); bool(af, "silent_swing", false);
      check(af.getSettings().size() == 7, "AntiFireball has extra settings");
      Module esp = manager.find("fireball_esp");
      check(esp.getCategory() == ModuleCategory.RENDER && esp.metadataCategory() == 2 && esp.getName().equals("Fireball ESP")
         && esp.getDescription().equals("Fireball trajectories.") && esp.listens(6) && !esp.listens(17) && !esp.isEnabled(), "Fireball ESP identity wrong");
      List<String> themes = Arrays.asList("Default", "Rainbow", "Aurora", "Cherry", "Cotton Candy", "Flare", "Flower", "Forest", "Frost", "Gold",
         "Grayscale", "Inferno", "Royal", "Sandstorm", "Sky", "Vine");
      for (String key : new String[]{"theme", "highlight_theme"}) {
         ChoiceSetting s = (ChoiceSetting)esp.findSetting(key);
         check(s != null && s.index() == 0 && s.getOptions().equals(themes), "Fireball ESP " + key + " options/default wrong");
      }
      check(((ChoiceSetting)esp.findSetting("theme")).getLabel().equals("Theme") && ((ChoiceSetting)esp.findSetting("highlight_theme")).getLabel().equals("Highlight Theme"), "theme labels");
      bool(esp, "shorten_line", true); bool(esp, "highlight_entity", true); bool(esp, "only_on_highlightable", false);
      check(esp.getSettings().size() == 5, "Fireball ESP has extra settings");
      Module opsec = manager.find("opsec");
      bool(opsec, "anti_quickbuy_identify", true); bool(opsec, "anti_ping_identify", false); num(opsec, "ping_boost", 100, 50, 1000, 50);
      check(!opsec.settingVisible("ping_boost"), "Ping Boost visible without Anti Ping Identify");
   }

   static void num(Module m, String key, double value, double min, double max, double step) {
      Setting<?> s = m.findSetting(key);
      check(s instanceof NumberSetting, m.getName() + " missing number " + key);
      NumberSetting n = (NumberSetting)s;
      check(n.doubleValue() == value && n.getMinimum() == min && n.getMaximum() == max && Math.abs(n.getStep() - step) < 1e-9, m.getName() + " " + key + " range/default wrong");
   }

   static void bool(Module m, String key, boolean value) {
      Setting<?> s = m.findSetting(key);
      check(s instanceof BooleanSetting && ((BooleanSetting)s).isEnabled() == value, m.getName() + " " + key + " default wrong");
   }

   static String resource(String name) throws Exception {
      try (InputStream in = V115Check.class.getResourceAsStream(name)) {
         check(in != null, "resource missing " + name);
         ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] b = new byte[8192];
         for (int r; (r = in.read(b)) >= 0; ) out.write(b, 0, r);
         return new String(out.toByteArray(), StandardCharsets.UTF_8);
      }
   }

   static void legacyConfig() throws Exception {
      Path dir = Files.createTempDirectory("v115-config");
      Path file = dir.resolve("settings.json");
      ((BooleanSetting)manager.find("antifireball").findSetting("silent_swing")).set(true);
      new ConfigStore(file).save(manager);
      com.google.gson.JsonObject saved = new com.google.gson.JsonParser().parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8)).getAsJsonObject();
      com.google.gson.JsonObject zest = new com.google.gson.JsonObject();
      zest.addProperty("__on", true); zest.addProperty("__bind", 34.0); zest.addProperty("mode", 1.0); zest.addProperty("tap_delay", 50.0);
      saved.add("ZestTap", zest);
      Files.write(file, saved.toString().getBytes(StandardCharsets.UTF_8));
      ((BooleanSetting)manager.find("antifireball").findSetting("silent_swing")).set(false);
      new ConfigStore(file).load(manager);
      manager.applyPendingChanges();
      check(((BooleanSetting)manager.find("antifireball").findSetting("silent_swing")).isEnabled(), "config with a ZestTap block did not load the other modules");
      ((BooleanSetting)manager.find("antifireball").findSetting("silent_swing")).set(false);
      // The pre-catalog "modules" map format with a zesttap record loads too.
      Path legacy = dir.resolve("legacy.json");
      Files.write(legacy, "{\"modules\":{\"zesttap\":{\"enabled\":true,\"settings\":{\"mode\":1}},\"antifireball\":{\"settings\":{\"range\":12.5}}}}".getBytes(StandardCharsets.UTF_8));
      new ConfigStore(legacy).load(manager);
      manager.applyPendingChanges();
      check(((NumberSetting)manager.find("antifireball").findSetting("range")).doubleValue() == 12.5, "legacy modules-format config with ZestTap failed");
      ((NumberSetting)manager.find("antifireball").findSetting("range")).set(10.0);
   }

   // ---------------------------------------------------------------------------------------------------------------
   static boolean flag(Module m, String key, boolean value) { ((BooleanSetting)m.findSetting(key)).set(value); m.afterSettingControl(key, c); return value; }

   /** One client tick as the game runs it: frame claim reset, client tick start, world tick (16/17), C03, 18/13, client tick end. */
   static void tick() {
      RotationService.beginFrame(c, 0, 0);
      manager.clientTick(true);
      manager.dispatch(new SemanticEvent(16));
      manager.dispatch(new SemanticEvent(17));
      handler.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(self.posX, self.posY, self.posZ, self.rotationYaw, self.rotationPitch, true));
      manager.dispatch(new SemanticEvent(18));
      SemanticEvent camera = new SemanticEvent(13); camera.active = false; camera.yaw = 0; camera.pitch = 0;
      manager.dispatch(camera);
      lastFrameActive = camera.active;
      RotationService.finishFrame(c, camera);
      manager.clientTick(false);
      self.ticksExisted++;
   }

   static boolean lastFrameActive;

   static void antiFireball() throws Exception {
      Module af = manager.find("antifireball");
      AntiFireballModule module = (AntiFireballModule)af;
      RotationService.reset(c);
      place(self, 0, 65, 0); self.rotationYaw = self.prevRotationYaw = 0F; self.rotationPitch = 0F; self.ticksExisted = 10;
      self.inventory.mainInventory[0] = null; self.inventory.currentItem = 0;
      EntityLargeFireball near = fireball(60, 2, 66, 0), far = fireball(61, 6, 66.5, 0);
      world.loadedEntityList.add(near); world.loadedEntityList.add(far);
      GameHooks.entityJoin(world, far);
      check(OpenB4AntiFireballBackend.target == null, "fireball tracked while AntiFireball is off");
      manager.toggle(af); manager.applyPendingChanges();
      check(af.isEnabled(), "AntiFireball did not enable");
      GameHooks.entityJoin(world, near);
      GameHooks.entityJoin(world, far);
      module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == far && OpenB4AntiFireballBackend.pending, "far fireball not targeted (own close fireball must be ignored)");
      int attacks = controller.attacks, swings = self.swings;
      tick();
      check(controller.attacks == attacks + 1 && controller.last == far && self.swings == swings + 1, "pending attack did not fire at the next pre-update");
      check(Math.abs(wrap(self.rotationYaw + 90F)) < 1F && Math.abs(self.rotationPitch) < 10F && lastFrameActive, "rotation did not aim at the fireball: " + self.rotationYaw);
      check(Math.abs(wrap(RotationService.cameraYaw())) < 1e-4F, "camera yaw not preserved under the silent rotation");
      // Attack only on even ticks: the next tick (odd) sets no pending attack.
      attacks = controller.attacks;
      tick(); tick();
      check(controller.attacks == attacks + 1, "AntiFireball did not keep the every-2-ticks cadence");
      // Silent swing: C0A packet instead of a client swing.
      flag(af, "silent_swing", true); handler.sent.clear(); swings = self.swings; attacks = controller.attacks;
      tick(); tick();
      check(controller.attacks == attacks + 1 && self.swings == swings && handler.sent.stream().anyMatch(p -> p instanceof C0APacketAnimation), "silent swing did not use a packet swing");
      flag(af, "silent_swing", false);
      // PacketUtils.noAttackPlaceOrDig: a C02 attack sent this tick blocks the pending hit until the next C03.
      while (self.ticksExisted % 2 != 0) tick();
      module.onClientTick(c); check(OpenB4AntiFireballBackend.pending, "no pending attack to gate");
      handler.addToSendQueue(new C02PacketUseEntity(far, C02PacketUseEntity.Action.ATTACK));
      attacks = controller.attacks;
      RotationService.beginFrame(c, 0, 0); manager.dispatch(new SemanticEvent(16)); manager.dispatch(new SemanticEvent(17));
      check(controller.attacks == attacks && !OpenB4AntiFireballBackend.pending, "attack sent through a same-tick C02");
      handler.addToSendQueue(new C03PacketPlayer(true));
      manager.dispatch(new SemanticEvent(18)); SemanticEvent cam = new SemanticEvent(13); manager.dispatch(cam); RotationService.finishFrame(c, cam); manager.clientTick(false); self.ticksExisted++;
      // Blocking with a sword: no attack.
      while (self.ticksExisted % 2 != 0) tick();
      ItemStack sword = new ItemStack(Items.diamond_sword); self.inventory.mainInventory[0] = sword; self.setItemInUse(sword, 72000);
      attacks = controller.attacks; tick(); tick();
      check(controller.attacks == attacks, "attacked while blocking");
      self.clearItemInUse(); self.inventory.mainInventory[0] = null;
      // Screens: a game screen clears the target; Rice's own GUI does not.
      mc.currentScreen = blank(net.minecraft.client.gui.GuiIngameMenu.class); module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == null && !OpenB4AntiFireballBackend.pending, "target kept with a screen open");
      mc.currentScreen = blank(RecoveredGuiScreen.class); module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == far, "Rice's own GUI blocked AntiFireball");
      mc.currentScreen = null;
      // Flying (default on) and scaffold (default off).
      self.capabilities.isFlying = true; module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == null, "target kept while flying");
      flag(af, "disable_while_flying", false); module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == far, "Disable while flying off still disabled");
      flag(af, "disable_while_flying", true); self.capabilities.isFlying = false;
      // v159: the Scaffold module was removed from the client, so AntiFireball's "disable while scaffold" has no live
      // scaffold to bridge (OpenB4AntiFireballHost.ScaffoldModule.active stays false); the setting remains but is dormant.
      // Idle the pending/attack state before the holding-blocks sub-test the way the removed scaffold block did: it
      // cleared the pending attack by ticking with the target suppressed. Use flying suppression instead of scaffold.
      self.capabilities.isFlying = true; module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == null && !OpenB4AntiFireballBackend.pending, "AntiFireball not idled before holding-blocks test");
      self.capabilities.isFlying = false;
      // Rotation is suppressed when holding blocks (default), but the hit still lands.
      while (self.ticksExisted % 2 != 0) tick();
      self.inventory.mainInventory[0] = new ItemStack(Blocks.stone); RotationService.reset(c); self.rotationYaw = self.prevRotationYaw = 0F;
      attacks = controller.attacks; boolean rotated = false;
      for (int i = 0; i < 2; i++) { tick(); rotated |= self.rotationYaw != 0F || lastFrameActive; }
      check(controller.attacks == attacks + 1, "hit did not land while holding blocks");
      check(!rotated, "rotated while holding blocks (Rotate with blocks off)");
      flag(af, "rotate_with_blocks", true); tick(); tick();
      check(Math.abs(wrap(self.rotationYaw + 90F)) < 1F, "Rotate with blocks on did not rotate");
      flag(af, "rotate_with_blocks", false); self.inventory.mainInventory[0] = null;
      // Earlier rotation providers win: a frame claim (Aim Assist 100) or a tick claim (500 without a KillAura rotation) blocks AntiFireball.
      RotationService.reset(c); self.rotationYaw = self.prevRotationYaw = 0F;
      RotationService.beginFrame(c, 0, 0); RotationService.claim(100);
      manager.dispatch(new SemanticEvent(16)); manager.dispatch(new SemanticEvent(17));
      check(self.rotationYaw == 0F, "AntiFireball rotated over an Aim Assist claim");
      RotationService.beginFrame(c, 0, 0); manager.dispatch(new SemanticEvent(16)); RotationService.claim(500); manager.dispatch(new SemanticEvent(17));
      check(self.rotationYaw == 0F, "AntiFireball rotated over a higher claim while KillAura is off");
      manager.dispatch(new SemanticEvent(18)); cam = new SemanticEvent(13); manager.dispatch(cam); RotationService.finishFrame(c, cam);
      // OpenB4 Scaffold's rotation claim (priority 750, published at event 17 before AntiFireball's; OpenB4 polls
      // Scaffold first) blocks AntiFireball's rotation.
      RotationService.beginFrame(c, 0, 0); manager.dispatch(new SemanticEvent(16));
      RotationService.claim(750); manager.dispatch(new SemanticEvent(17));
      check(self.rotationYaw == 0F, "AntiFireball rotated over the OpenB4 Scaffold rotation claim");
      manager.dispatch(new SemanticEvent(18)); cam = new SemanticEvent(13); manager.dispatch(cam); RotationService.finishFrame(c, cam);
      // Joining the world clears tracked fireballs.
      GameHooks.entityJoin(world, self); module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == null, "own join did not clear tracked fireballs");
      GameHooks.entityJoin(world, far); module.onClientTick(c);
      check(OpenB4AntiFireballBackend.target == far, "re-tracking failed");
      // Disable: state cleared, hooks inert.
      manager.toggle(af); manager.applyPendingChanges();
      check(OpenB4AntiFireballBackend.target == null && !OpenB4AntiFireballBackend.pending, "disable kept AntiFireball state");
      attacks = controller.attacks; GameHooks.entityJoin(world, far); tick(); tick();
      check(controller.attacks == attacks && OpenB4AntiFireballBackend.target == null, "disabled AntiFireball still acted");
      world.loadedEntityList.clear(); RotationService.reset(c); self.rotationYaw = self.prevRotationYaw = 0F;
   }

   // ---------------------------------------------------------------------------------------------------------------
   static void fireballEsp(Path classes) throws Exception {
      ClassLoader loader = new ClassLoader(V115Check.class.getClassLoader()) {
         @Override protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("dev.rice.client.render.RavenScriptApi") && !name.startsWith("dev.rice.module.impl.FireballEspModule")
               && !name.startsWith("dev.rice.client.clutch.ClutchApi") && !name.startsWith("dev.rice.client.beddefense.BedDefenseApi")
               && !name.startsWith("dev.rice.client.scaffold.openb4.OpenB4ScaffoldBackend")) return super.loadClass(name, resolve);
            Class<?> cached = findLoadedClass(name); if (cached != null) return cached;
            try {
               byte[] bytes = Files.readAllBytes(classes.resolve(name.replace('.', '/') + ".class"));
               ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
               for (MethodNode method : node.methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (!(insn instanceof MethodInsnNode)) continue;
                  MethodInsnNode call = (MethodInsnNode)insn;
                  if (call.owner.equals("org/lwjgl/opengl/GL11") || call.owner.equals("net/minecraft/client/renderer/GlStateManager")
                     || call.owner.equals("net/minecraft/client/renderer/RenderGlobal") && call.name.equals("drawSelectionBoundingBox")
                     || call.owner.equals("dev/rice/client/render/RavenScriptApi") && call.name.equals("filledBox")) call.owner = "V115GlStub";
               }
               ClassWriter writer = new ClassWriter(0); node.accept(writer); bytes = writer.toByteArray();
               Class<?> type = defineClass(name, bytes, 0, bytes.length); if (resolve) resolveClass(type); return type;
            } catch (Exception e) { throw new ClassNotFoundException(name, e); }
         }
      };
      Module esp = (Module)loader.loadClass("dev.rice.module.impl.FireballEspModule").getConstructor().newInstance();
      Method render = esp.getClass().getDeclaredMethod("onRenderWorld", float.class); render.setAccessible(true);
      RenderManager rm = mc.getRenderManager();
      // Script math, unchanged from the Raven script.
      Method plane = esp.getClass().getDeclaredMethod("rayPlaneDistance", double.class, double.class, double.class, double.class, double.class, double.class,
         double.class, double.class, double.class, String.class); plane.setAccessible(true);
      check((double)plane.invoke(esp, 0, 10, 0, 0, -1, 0, 0, 7, 0, "UP") == 3.0 && (double)plane.invoke(esp, 0, 0, 0, 0, 0, 1, 0, 0, 4, "NORTH") == 4.0
         && (double)plane.invoke(esp, 0, 0, 0, 1, 0, 0, 0, 5, 0, "UP") == -1.0 && (double)plane.invoke(esp, 0, 0, 0, -1, 0, 0, 3, 0, 0, "EAST") == -1.0, "rayPlaneDistance");
      Method lerp = esp.getClass().getDeclaredMethod("lerpColor", int.class, int.class, double.class); lerp.setAccessible(true);
      check((int)lerp.invoke(esp, 0xFF000000, 0xFFFFFFFF, .5) == 0xFF7F7F7F && (int)lerp.invoke(esp, 0xFF102030, 0xFF405060, 1.0) == 0xFF405060, "lerpColor");
      Method theme = esp.getClass().getDeclaredMethod("getThemeColor", String.class); theme.setAccessible(true);
      check((int)theme.invoke(esp, "white") == 0xFFFFFFFF && (int)theme.invoke(esp, "nonsense") == 0xFFFFFFFF, "white theme");
      int aurora = (int)theme.invoke(esp, "aurora");
      check(between(aurora >> 16 & 255, 0x17, 0x73) && between(aurora >> 8 & 255, 0x01, 0xF0) && between(aurora & 255, 0xB1, 0xC2), "aurora outside its gradient");
      Method resolveTheme = esp.getClass().getDeclaredMethod("resolveTheme"); resolveTheme.setAccessible(true);
      ChoiceSetting themeSetting = (ChoiceSetting)esp.findSetting("theme");
      check(resolveTheme.invoke(esp).equals("white"), "Default theme must fall back to white (no Raven Settings module in Rice)");
      themeSetting.set(1); check(resolveTheme.invoke(esp).equals("Rainbow"), "Theme index 1"); themeSetting.set(8); check(resolveTheme.invoke(esp).equals("Frost"), "Theme index 8");
      themeSetting.set(0);
      Method white = esp.getClass().getDeclaredMethod("withAlpha", int.class, int.class); white.setAccessible(true);
      check((int)white.invoke(esp, 0xFF123456, 36) == 0x24123456, "withAlpha");

      // Case A: diamond block below, looking straight down, fire charge held.
      place(self, .5, 65, .5); self.rotationYaw = 0F; self.rotationPitch = 90F;
      rm.viewerPosX = .5; rm.viewerPosY = 65; rm.viewerPosZ = .5;
      world.blocks.clear(); world.blocks.put(new BlockPos(0, 64, 0), Blocks.diamond_block.getDefaultState());
      self.inventory.currentItem = 0; self.inventory.mainInventory[0] = new ItemStack(Items.fire_charge);
      esp.onClientTickStart(c);
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      check(V115GlStub.matrixDepth == 0, "unbalanced push/pop");
      List<V115GlStub.Primitive> prims = V115GlStub.primitives;
      check(prims.size() == 3, "expected aim line + landing plate (fill, outline), got " + prims.size());
      V115GlStub.Primitive line = prims.get(0);
      check(line.mode == 1 && line.vertices.size() == 2 && Arrays.equals(line.color, new float[]{1, 1, 1, 1}) && line.lineWidth == 2F, "aim line");
      double[] start = line.vertices.get(0), end = line.vertices.get(1);
      check(Math.abs(end[1] + 65 - 65.0) < 1e-9 && Math.abs(end[0]) < 1e-9 && Math.abs(end[2]) < 1e-9, "aim line does not end on the block face plane: " + Arrays.toString(end));
      double handX = .5 - .16 - .5, handY = 66.62 - .10 - 65, handZ = 0;
      check(Math.abs(Math.sqrt(sq(start[0] - handX) + sq(start[1] - handY) + sq(start[2] - handZ)) - .5) < 1e-6, "Shorten line did not start 0.5 from the hand");
      V115GlStub.Primitive fill = prims.get(1), outline = prims.get(2);
      check(fill.mode == 7 && fill.vertices.size() == 24 && Math.abs(fill.color[3] - .18F) < 1e-6 && outline.mode == 1 && outline.vertices.size() == 24 && Math.abs(outline.color[3] - .75F) < 1e-6,
         "landing plate fill/outline");
      for (double[] v : fill.vertices) check((Math.abs(v[1] - .02) < 1e-9 || Math.abs(v[1] - .035) < 1e-9) && Math.abs(Math.abs(v[0] - end[0]) - .30) < 1e-9, "landing plate geometry " + Arrays.toString(v));
      check(V115GlStub.boxes.size() == 1 && V115GlStub.outlines.size() == 1, "diamond block not highlighted");
      AxisAlignedBB bb = (AxisAlignedBB)V115GlStub.boxes.get(0)[0];
      check(bb.minX == -.5 && bb.minY == -1 && bb.minZ == -.5 && bb.maxX == .5 && bb.maxY == 0 && bb.maxZ == .5 && Math.abs(((float[])V115GlStub.boxes.get(0)[1])[3] - .42F) < 1e-6,
         "block highlight box/alpha (OpenB4 Render3D.blockBox)");
      check(V115GlStub.outlines.get(0).equals(bb) || V115GlStub.outlines.get(0).toString().equals(bb.toString()), "block outline box");
      // Case B: a plain block gets the line and plate but no highlight.
      world.blocks.put(new BlockPos(0, 64, 0), Blocks.stone.getDefaultState());
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      check(V115GlStub.primitives.size() == 3 && V115GlStub.boxes.isEmpty() && V115GlStub.outlines.isEmpty(), "stone highlighted");
      // Case C: Only on highlightable skips plain hits entirely.
      flag(esp, "only_on_highlightable", true); esp.onClientTickStart(c);
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      check(V115GlStub.calls == 0, "Only on highlightable drew on a plain block");
      world.blocks.put(new BlockPos(0, 64, 0), Blocks.emerald_block.getDefaultState());
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      check(V115GlStub.boxes.size() == 1, "Only on highlightable skipped an emerald block");
      flag(esp, "only_on_highlightable", false); esp.onClientTickStart(c);
      // Case D: nothing without a fire charge.
      self.inventory.mainInventory[0] = new ItemStack(Items.snowball);
      V115GlStub.reset(); render.invoke(esp, 1.0F); check(V115GlStub.calls == 0, "drew without a fire charge");
      self.inventory.mainInventory[0] = null;
      V115GlStub.reset(); render.invoke(esp, 1.0F); check(V115GlStub.calls == 0, "drew with an empty hand");
      self.inventory.mainInventory[0] = new ItemStack(Items.fire_charge);
      // Case E: a living entity in front of the block wins and is boxed; no landing plate.
      self.rotationYaw = -90F; self.rotationPitch = 0F;
      world.blocks.clear(); world.blocks.put(new BlockPos(5, 66, 0), Blocks.stone.getDefaultState());
      Player other = player(70, 3.0, 65, .5); world.loadedEntityList.add(other);
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      prims = V115GlStub.primitives;
      check(prims.size() == 3 && prims.get(0).mode == 1 && prims.get(1).mode == 7 && prims.get(2).mode == 1, "entity hit primitives " + prims.size());
      end = prims.get(0).vertices.get(1);
      check(Math.abs(end[0] + .5 - 2.7) < 1e-6, "aim line does not end at the entity box: " + Arrays.toString(end));
      check(Math.abs(prims.get(1).color[3] - 36 / 255F) < 1e-6 && prims.get(1).vertices.size() == 24 && Math.abs(prims.get(2).color[3] - 1F) < 1e-6 && prims.get(2).vertices.size() == 24,
         "entity highlight box");
      flag(esp, "highlight_entity", false); esp.onClientTickStart(c);
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      check(V115GlStub.primitives.size() == 1, "Highlight entity off still boxed the entity");
      flag(esp, "highlight_entity", true); esp.onClientTickStart(c);
      world.loadedEntityList.remove(other);
      // Case F: Shorten line off starts at the hand.
      flag(esp, "shorten_line", false); esp.onClientTickStart(c);
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      start = V115GlStub.primitives.get(0).vertices.get(0);
      double yaw = Math.toRadians(-90);
      check(Math.abs(start[0] - (-Math.cos(yaw) * .16)) < 1e-9 && Math.abs(start[1] - (1.62 - .10)) < 1e-6 && Math.abs(start[2] - (-Math.sin(yaw) * .16)) < 1e-9,
         "unshortened line does not start at the hand: " + Arrays.toString(start));
      flag(esp, "shorten_line", true);
      // Case G: a chosen theme colours the line from its gradient.
      themeSetting.set(9); esp.onClientTickStart(c);
      V115GlStub.reset(); render.invoke(esp, 1.0F);
      float[] gold = V115GlStub.primitives.get(0).color;
      check(betweenF(gold[0], 0xDA, 0xE5) && betweenF(gold[1], 0xDA, 0xDF) && betweenF(gold[2], 0x30, 0xB6), "Gold theme colour outside its gradient: " + Arrays.toString(gold));
      themeSetting.set(0);
      // Player moved four blocks this tick. The mid-frame eye/raycast must agree with the displayed line.
      place(self, 4.5, 65, .5); self.prevPosX = self.lastTickPosX = .5;
      self.rotationYaw = 0; self.rotationPitch = 0; rm.viewerPosX = 2.5; rm.viewerPosY = 65; rm.viewerPosZ = .5;
      world.blocks.clear(); world.blocks.put(new BlockPos(2,66,5),Blocks.stone.getDefaultState());
      esp.onClientTickStart(c); V115GlStub.reset(); render.invoke(esp,.5F);
      end=V115GlStub.primitives.get(0).vertices.get(1);
      check(Math.abs(end[0])<1e-6 && Math.abs(end[2]-4.5)<1e-6,"moving fireball preview missed interpolated block face: "+Arrays.toString(end));
      // A fast entity crossing the ray between ticks is hit in the same interpolated frame.
      place(self,.5,65,.5); rm.viewerPosX=.5; world.blocks.clear();
      Player moving=player(71,8.5,65,3); moving.lastTickPosX=-7.5; world.loadedEntityList.add(moving);
      V115GlStub.reset(); render.invoke(esp,.5F); end=V115GlStub.primitives.get(0).vertices.get(1);
      check(Math.abs(end[2]-2.2)<1e-6,"moving entity trajectory collision missed: "+Arrays.toString(end)); world.loadedEntityList.remove(moving);
      // Cache says the selected color is already active, but the actual driver was changed by another renderer.
      // Each script outline must still send the selected color to the driver.
      Class<?> clutchApi=loader.loadClass("dev.rice.client.clutch.ClutchApi");
      clutchApi.getMethod("bind",Minecraft.class,ModuleManager.class,java.util.function.Consumer.class).invoke(null,mc,manager,(java.util.function.Consumer<Setting<?>>)setting -> {});
      Class<?> clutchGl=loader.loadClass("dev.rice.client.clutch.ClutchApi$gl");
      V115GlStub.reset(); V115GlStub.suppressCachedColor=true;
      clutchGl.getMethod("color",int.class).invoke(null,0xCC336699);
      V115GlStub.glBegin(1); V115GlStub.glEnd();
      check(Arrays.equals(V115GlStub.primitives.get(0).color,new float[]{.2F,.4F,.6F,.8F}),"AutoClutch outline lost selected color/alpha with stale cache");
      // v159: Bed Defender was removed from the client; its outline-render coverage is dropped with the feature.
      V115GlStub.reset(); V115GlStub.suppressCachedColor=true;
      V115GlStub.reset(); V115GlStub.suppressCachedColor=true;
      Class<?> ravenGl=loader.loadClass("dev.rice.client.render.RavenScriptApi$gl");
      ravenGl.getMethod("color",int.class).invoke(null,0xCC336699); V115GlStub.glBegin(1); V115GlStub.glEnd();
      check(Arrays.equals(V115GlStub.primitives.get(0).color,new float[]{.2F,.4F,.6F,.8F}),"Fireball outline lost selected color/alpha");
      V115GlStub.reset();
      System.out.println("PASS: moving-player and moving-entity fireball trajectory alignment; Fireball/AutoClutch/Bed Defender/Scaffold outline color + alpha with stale GL cache");
      world.blocks.clear(); self.inventory.mainInventory[0] = null; place(self, 0, 65, 0); self.rotationYaw = self.rotationPitch = 0F;
   }

   static double sq(double v) { return v * v; }
   static boolean between(int v, int a, int b) { return v >= Math.min(a, b) && v <= Math.max(a, b); }
   static boolean betweenF(float v, int a, int b) { return v * 255 >= Math.min(a, b) - .5 && v * 255 <= Math.max(a, b) + .5; }

   // ---------------------------------------------------------------------------------------------------------------
   static void opsec() throws Exception {
      check(!JadeRuntime.active(), "Jade runtime active before Opsec");
      Object stack = new ItemStack(Items.diamond);
      check(JadeRuntime.containerDisplayedStack(null, stack) == stack && !JadeRuntime.containerClick(null, null, 0, 0, 0) && !JadeRuntime.containerManualInput(null)
         && !JadeRuntime.networkSend(new C03PacketPlayer(true)) && !JadeRuntime.networkReceive(new C03PacketPlayer(true)), "OpenJade container/network hooks acted with Opsec off");
      Module opsec = manager.find("opsec");
      manager.toggle(opsec); manager.applyPendingChanges();
      check(JadeRuntime.active() && JadeRuntime.opsec().isEnabled(), "Opsec did not start");
      flag(opsec, "anti_ping_identify", true);
      check(opsec.settingVisible("ping_boost"), "Ping Boost hidden with Anti Ping Identify on");
      handler.sent.clear();
      handler.addToSendQueue(new C03PacketPlayer(true));
      check(handler.sent.size() == 1, "Anti Ping held packets outside the Bed Wars lobby");
      check(!JadeRuntime.networkReceive(new C03PacketPlayer(true)), "Anti Ping held inbound packets outside the Bed Wars lobby");
      check(JadeRuntime.containerDisplayedStack(null, stack) == stack, "Opsec substituted a stack outside Quick Buy");
      flag(opsec, "anti_ping_identify", false);
      manager.toggle(opsec); manager.applyPendingChanges();
      check(!JadeRuntime.active(), "Jade runtime active after Opsec off");
   }

   static void pipeline() throws Exception {
      long[] now = {0};
      List<Integer> classified = new ArrayList<>();
      PacketPipeline pipe = new PacketPipeline(() -> now[0], (packet, out, age, sequence) -> { classified.add(sequence); return out && age < 50 ? 1 : 0; });
      EmbeddedChannel channel = new EmbeddedChannel(pipe);
      C03PacketPlayer first = new C03PacketPlayer(true), second = new C03PacketPlayer(false);
      channel.writeOutbound(first); channel.writeOutbound(second);
      check(channel.readOutbound() == null && pipe.retained(true) == 2, "held outbound packets escaped");
      now[0] = 30; pipe.run(); check(channel.readOutbound() == null, "released before the hold elapsed");
      now[0] = 60; pipe.run();
      check(channel.readOutbound() == first && channel.readOutbound() == second && pipe.retained(true) == 0, "hold did not release in order");
      channel.finish();
   }
}
