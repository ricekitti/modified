import com.mojang.authlib.GameProfile;
import dev.rice.client.ClientContext;
import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.client.combat.openb4.B4AuraHost;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.render.openb4.OpenB4TargetHudBackend;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.CoreModules;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovementInput;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * v116 behavior checks against real Minecraft 1.8.9 classes, without a game window.
 * <ul>
 *   <li>OpenB4 TargetHUD.
 *     <ul>
 *       <li>text(), and the KillAura-driven hold/fade through the restored onRenderTickTargetHud.</li>
 *       <li>The Legacy draw path, recorded with GL redirected to V116GlStub.</li>
 *       <li>Disabled/off/no-target cases, Render ESP, and the position editor with its persistence.</li>
 *     </ul></li>
 * </ul>
 * Port fidelity (generated == upstream + whitelisted rewrites) is checked separately by the port tools' --check mode.
 */
public final class V116Check {
   static Object unsafe;
   static Method allocate;
   static Minecraft mc;
   static World world;
   static Self self;
   static Handler handler;
   static Sounds sounds;
   static Font font;
   static boolean requireUnlitFont;
   static ClientContext c;
   static ModuleManager manager;
   static final List<IChatComponent> chat = Collections.synchronizedList(new ArrayList<IChatComponent>());

   static <T> T blank(Class<T> type) throws Exception { return type.cast(allocate.invoke(unsafe, type)); }

   static void put(Object o, String name, Object value) throws Exception {
      for (Class<?> t = o.getClass(); t != null; t = t.getSuperclass()) {
         try { Field f = t.getDeclaredField(name); f.setAccessible(true); f.set(o, value); return; } catch (NoSuchFieldException next) { }
      }
      throw new NoSuchFieldException(name);
   }

   static Object get(Class<?> type, Object o, String name) throws Exception {
      Field f = type.getDeclaredField(name); f.setAccessible(true); return f.get(o);
   }

   static void set(Class<?> type, Object o, String name, Object value) throws Exception {
      Field f = type.getDeclaredField(name); f.setAccessible(true); f.set(o, value);
   }

   static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

   public static final class Self extends EntityPlayerSP {
      Self() { super(null, null, null, null); }
      @Override public void swingItem() { }
      @Override public UUID getUniqueID() { return new UUID(0, getEntityId()); }
      @Override public void addChatMessage(IChatComponent message) { chat.add(message); }
   }

   public static final class Other extends EntityOtherPlayerMP {
      Other() { super(null, null); }
      @Override public UUID getUniqueID() { return new UUID(0, getEntityId()); }
   }

   public static final class World extends WorldClient {
      Map<BlockPos, IBlockState> blocks;
      World() { super(null, null, 0, null, null); }
      @Override public Entity getEntityByID(int id) { synchronized (loadedEntityList) { for (Object e : loadedEntityList) if (((Entity)e).getEntityId() == id) return (Entity)e; } return null; }
      @Override public IBlockState getBlockState(BlockPos pos) { IBlockState s = blocks.get(pos); return s == null ? Blocks.air.getDefaultState() : s; }
      @Override public boolean isBlockLoaded(BlockPos pos) { return true; }
      @Override public IChunkProvider getChunkProvider() {
         return (IChunkProvider)Proxy.newProxyInstance(V116Check.class.getClassLoader(), new Class<?>[]{IChunkProvider.class},
            (proxy, method, args) -> method.getName().equals("chunkExists") ? Boolean.TRUE : null);
      }
   }

   /** NetHandlerPlayClient.addToSendQueue -> NetworkManager.sendPacket head hook, as the game runs it. */
   public static final class Handler extends NetHandlerPlayClient {
      List<Packet<?>> sent;
      Handler() { super(null, null, null, null); }
      @Override public void addToSendQueue(Packet p) { if (!GameHooks.beforeSend(null, p)) sent.add(p); }
   }

   public static final class Sounds extends SoundHandler {
      List<ISound> played;
      Sounds() { super(null, null); }
      @Override public void playSound(ISound sound) { played.add(sound); }
      @Override public void resumeSounds() { }
   }

   /** 6 px per character, 9 px high; every drawString is recorded. */
   public static final class Font extends FontRenderer {
      List<Object[]> drawn;
      Font() { super(null, null, null, false); }
      @Override public int getStringWidth(String text) { return text == null ? 0 : text.length() * 6; }
      @Override public int getCharWidth(char ch) { return 6; }
      @Override public int drawString(String text, float x, float y, int color, boolean shadow) { if(requireUnlitFont) check(V116GlStub.program==0 && !V116GlStub.lighting && !V116GlStub.fog && !V116GlStub.textures.get(33985)
         && Arrays.equals(V116GlStub.currentColor(),new float[]{1,1,1,1}),"name-tag text still inherits gray/tinted world lighting"); drawn.add(new Object[]{text, x, y, color, shadow}); return 0; }
      @Override public int drawStringWithShadow(String text, float x, float y, int color) { return drawString(text, x, y, color, true); }
   }

   static void living(EntityPlayer p, int id, String name) throws Exception {
      p.setEntityId(id); put(p, "worldObj", world); put(p, "rand", new Random(id)); put(p, "gameProfile", new GameProfile(new UUID(0, id), name));
      p.capabilities = new PlayerCapabilities(); p.inventory = new InventoryPlayer(p);
      DataWatcher data = new DataWatcher(p); data.addObject(0, (byte)0); data.addObject(6, 20F); data.addObject(17, 0F); put(p, "dataWatcher", data);
      put(p, "activePotionsMap", new HashMap<>()); p.width = .6F; p.height = 1.8F;
      Method attributes = EntityLivingBase.class.getDeclaredMethod("applyEntityAttributes"); attributes.setAccessible(true); attributes.invoke(p);
   }

   static Other other(int id, String name, double x, double y, double z) throws Exception {
      Other p = blank(Other.class);
      living(p, id, name);
      p.ticksExisted = 100;
      place(p, x, y, z);
      world.playerEntities.add(p); world.loadedEntityList.add(p);
      return p;
   }

   static void place(Entity p, double x, double y, double z) {
      p.posX = p.prevPosX = p.lastTickPosX = x; p.posY = p.prevPosY = p.lastTickPosY = y; p.posZ = p.prevPosZ = p.lastTickPosZ = z;
      box(p);
   }

   static void box(Entity p) {
      double w = p.width / 2.0;
      p.setEntityBoundingBox(new AxisAlignedBB(p.posX - w, p.posY, p.posZ - w, p.posX + w, p.posY + p.height, p.posZ + w));
   }

   /** One server tick of another player's motion as the client sees it (lastTick = previous position). */
   static void step(EntityPlayer p, double dx, double dy, double dz, float dyaw, float dpitch) {
      p.lastTickPosX = p.prevPosX = p.posX; p.lastTickPosY = p.prevPosY = p.posY; p.lastTickPosZ = p.prevPosZ = p.posZ;
      p.prevRotationYaw = p.rotationYaw; p.prevRotationPitch = p.rotationPitch;
      p.posX += dx; p.posY += dy; p.posZ += dz; p.rotationYaw += dyaw; p.rotationPitch += dpitch;
      p.ticksExisted++;
      box(p);
   }

   static KeyBinding key(String name, int code) { return new KeyBinding("v116." + name, code, "v116"); }

   static void setup() throws Exception {
      Bootstrap.register();
      Class<?> u = Class.forName("sun.misc.Unsafe"); Field f = u.getDeclaredField("theUnsafe"); f.setAccessible(true); unsafe = f.get(null);
      allocate = u.getMethod("allocateInstance", Class.class);
      mc = blank(Minecraft.class);
      Field singleton = Minecraft.class.getDeclaredField("theMinecraft"); singleton.setAccessible(true); singleton.set(null, mc);
      world = blank(World.class); world.blocks = new HashMap<>(); put(world, "loadedEntityList", Collections.synchronizedList(new ArrayList<Entity>()));
      put(world, "playerEntities", Collections.synchronizedList(new ArrayList<EntityPlayer>()));
      put(world, "worldBorder", new net.minecraft.world.border.WorldBorder()); put(world, "worldScoreboard", new Scoreboard());
      put(world, "provider", new WorldProviderSurface());
      self = blank(Self.class); living(self, 1, "RiceTester"); place(self, 0, 65, 0); self.ticksExisted = 10;
      world.playerEntities.add(self); world.loadedEntityList.add(self);
      handler = blank(Handler.class); handler.sent = Collections.synchronizedList(new ArrayList<Packet<?>>()); put(handler, "playerInfoMap", new HashMap<>());
      put(self, "sendQueue", handler); put(self, "mc", mc);
      PlayerControllerMP controller = blank(PlayerControllerMP.class); put(controller, "mc", mc); put(controller, "netClientHandler", handler);
      put(controller, "currentGameType", net.minecraft.world.WorldSettings.GameType.SURVIVAL);
      GameSettings keys = blank(GameSettings.class); keys.mouseSensitivity = .5F;
      keys.keyBindForward = key("forward", 17); keys.keyBindBack = key("back", 31); keys.keyBindLeft = key("left", 30); keys.keyBindRight = key("right", 32);
      keys.keyBindJump = key("jump", 57); keys.keyBindSneak = key("sneak", 42); keys.keyBindSprint = key("sprint", 29); keys.keyBindAttack = key("attack", -100);
      keys.keyBindUseItem = key("use", -99); keys.keyBindsHotbar = new KeyBinding[9]; for (int i = 0; i < 9; i++) keys.keyBindsHotbar[i] = key("hotbar" + i, 2 + i);
      sounds = blank(Sounds.class); sounds.played = new ArrayList<>();
      font = blank(Font.class); font.drawn = new ArrayList<>(); put(font, "FONT_HEIGHT", 9);
      put(mc, "gameSettings", keys); put(mc, "thePlayer", self); put(mc, "theWorld", world); put(mc, "playerController", controller);
      put(mc, "inGameHasFocus", false); put(mc, "renderViewEntity", self); put(mc, "mcThread", Thread.currentThread());
      put(mc, "mcSoundHandler", sounds); put(mc, "fontRendererObj", font); put(mc, "mcLanguageManager", blank(LanguageManager.class));
      put(mc, "displayWidth", 854); put(mc, "displayHeight", 480);
      RenderManager renderManager = blank(RenderManager.class); put(mc, "renderManager", renderManager);
      self.movementInput = new MovementInput();
      c = new ClientContext(mc);
      manager = new ModuleManager(c);
      GameHooks.install(manager);
      CoreModules.register(manager);
      manager.applyPendingChanges();

   }

   public static void main(String[] args) throws Exception {
      Path classes = Paths.get(args[0]);
      setup();
      targetHudRegistry();
      targetHud(classes);
      assistedHud(classes);
      renderIsolation(classes);
      configBinds();
      System.out.println("PASS: v116 "
         + "OpenB4 TargetHUD registry/defaults; text() health/difference/W-L/padding; KillAura-driven hold/fade via the restored onRenderTickTargetHud; "
         + "Legacy draw path (border, bar animation, text); Disabled/off/screen cases; Render ESP box; position editor drag/reset/persistence");
   }

   // ---------------------------------------------------------------------------------------------------------------
   static void num(Module m, String key, double value, double min, double max, double step) {
      Setting<?> s = m.findSetting(key);
      check(s instanceof NumberSetting, m.getName() + " missing number " + key);
      NumberSetting n = (NumberSetting)s;
      check(n.doubleValue() == value && n.getMinimum() == min && n.getMaximum() == max && Math.abs(n.getStep() - step) < 1e-9, m.getName() + " " + key + " range/default wrong");
   }

   static void bool(Module m, String key, String label, boolean value) {
      Setting<?> s = m.findSetting(key);
      check(s instanceof BooleanSetting && ((BooleanSetting)s).isEnabled() == value && s.getLabel().equals(label), m.getName() + " " + key + " default/label wrong");
   }

   static boolean flag(Module m, String key, boolean value) { ((BooleanSetting)m.findSetting(key)).set(value); m.afterSettingControl(key, c); return value; }

   static void toggle(Module m) { manager.toggle(m); manager.applyPendingChanges(); }

   /** One client tick as the game runs it, with the walking-packet head (OpenB4 pre-motion) before this tick's C03. */
   static void tick() {
      RotationService.beginFrame(c, 0, 0);
      manager.clientTick(true);
      manager.dispatch(new SemanticEvent(16));
      manager.dispatch(new SemanticEvent(17));
      GameHooks.walkingPlayerHead(self);
      handler.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(self.posX, self.posY, self.posZ, self.rotationYaw, self.rotationPitch, true));
      manager.dispatch(new SemanticEvent(18));
      SemanticEvent camera = new SemanticEvent(13); camera.active = false; manager.dispatch(camera);
      RotationService.finishFrame(c, camera);
      manager.clientTick(false);
      self.ticksExisted++;
   }

   static List<String> chatText() { synchronized (chat) { return chat.stream().map(IChatComponent::getUnformattedText).collect(Collectors.toList()); } }

   static long alerts(String check) { return chatText().stream().filter(s -> s.contains(" §7detected for §d" + check + " ")).count(); }

   static String alert(String name, String check, String distance) { return "§7[§dR§7]§r " + name + " §7detected for §d" + check + " " + distance; }

   /** Fresh scene: only the local player, no blocks, AntiCheat state cleared through the player's own join event. */
   static void reset() throws Exception {
      world.playerEntities.retainAll(Collections.singleton(self)); world.loadedEntityList.retainAll(Collections.singleton(self));
      world.blocks.clear(); chat.clear(); handler.sent.clear(); sounds.played.clear();
      GameHooks.entityJoin(world, self);
   }

   static void hypixel(boolean on) throws Exception {
      put(mc, "currentServerData", on ? new ServerData("Hypixel", "mc.hypixel.net", false) : null);
   }



   static S23PacketBlockChange blockChange(BlockPos pos, IBlockState state) throws Exception {
      S23PacketBlockChange p = blank(S23PacketBlockChange.class); put(p, "blockPosition", pos); put(p, "blockState", state); return p;
   }

   static List<Packet<?>> chatAndMovement() {
      synchronized (handler.sent) { return handler.sent.stream().filter(p -> p instanceof C01PacketChatMessage || p instanceof C03PacketPlayer).collect(Collectors.toList()); }
   }

   static void receive(Packet<?> packet) { check(!GameHooks.receivePacket(null, packet), "inbound packet cancelled"); }

   static void solid(BlockPos center) {
      for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++)
         world.blocks.put(center.add(dx, dy, dz), Blocks.stone.getDefaultState());
   }

   // ---------------------------------------------------------------------------------------------------------------




   // ---------------------------------------------------------------------------------------------------------------


   static void targetHudRegistry() {
      Module m = manager.find("targethud");
      check(m != null && m.getName().equals("TargetHUD") && m.getCategory() == ModuleCategory.RENDER && m.metadataCategory() == 2
         && m.getDescription().equals("Renders the target of KillAura, AutoClicker or AimAssist.") && !m.isEnabled() && m.listens(6), "TargetHUD identity wrong");
      ChoiceSetting mode = (ChoiceSetting)m.findSetting("mode"), theme = (ChoiceSetting)m.findSetting("theme");
      check(mode.index() == 1 && mode.getOptions().equals(Arrays.asList("Disabled", "Modern", "Legacy")), "TargetHUD mode default/options");
      check(theme.index() == 0 && theme.getOptions().equals(Arrays.asList("Default", "Rainbow", "Aurora", "Cherry", "Cotton candy", "Flare", "Flower", "Forest",
         "Frost", "Gold", "Grayscale", "Inferno", "Royal", "Sandstorm", "Sky", "Vine")), "TargetHUD theme default/options");
      bool(m, "edit_position", "Edit position", false); bool(m, "render_esp", "Render ESP", false); bool(m, "show_difference", "Show difference", false);
      bool(m, "show_health", "Show health", true); bool(m, "show_win_loss", "Show win/loss", true); bool(m, "traditional_health_color", "Traditional health color", false);
      num(m, "pos_x", 70, -4000, 4000, 1); num(m, "pos_y", 40, -4000, 4000, 1);
      check(!m.settingVisible("pos_x") && !m.settingVisible("pos_y") && m.settingVisible("mode"), "position settings must be hidden");
      check(m.getSettings().size() == 10, "TargetHUD has extra settings");
   }

   /** Loads the TargetHUD backend, Render3D/RenderUtils and the KillAura backend with GL redirected to V116GlStub. */
   static ClassLoader recorder(Path classes) {
      return new ClassLoader(V116Check.class.getClassLoader()) {
         @Override protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("dev.rice.client.render.openb4.OpenB4TargetHudBackend")
               && !name.startsWith("dev.rice.client.combat.openb4.OpenB4KillAuraBackend")
               && !name.startsWith("dev.rice.module.impl.TargetHudModule")
               && !name.equals("dev.rice.client.render.RenderPassScope") && !name.equals("dev.rice.client.render.RenderAccess")) return super.loadClass(name, resolve);
            Class<?> cached = findLoadedClass(name); if (cached != null) return cached;
            try {
               byte[] bytes = Files.readAllBytes(classes.resolve(name.replace('.', '/') + ".class"));
               ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
               for (MethodNode method : node.methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (!(insn instanceof MethodInsnNode)) continue;
                  MethodInsnNode call = (MethodInsnNode)insn;
                  if (call.owner.equals("org/lwjgl/opengl/GL11") || call.owner.equals("org/lwjgl/opengl/GL13") || call.owner.equals("org/lwjgl/opengl/GL20") ||
                     call.owner.equals("net/minecraft/client/renderer/OpenGlHelper") && call.name.equals("glBindFramebuffer") || call.owner.equals("net/minecraft/client/renderer/GlStateManager")
                     || call.owner.equals("dev/rice/client/render/openb4/Render3D") && call.name.equals("entityBox")
                     || call.owner.equals("net/minecraft/client/renderer/RenderGlobal") && call.name.equals("drawSelectionBoundingBox")) call.owner = "V116GlStub";
                  if(call.owner.equals("dev/rice/client/hook/GlStateScope") && call.name.equals("open")) {
                     call.owner="V116Check"; call.name="openRenderScope";
                  }
               }
               ClassWriter writer = new ClassWriter(0); node.accept(writer); bytes = writer.toByteArray();
               Class<?> type = defineClass(name, bytes, 0, bytes.length); if (resolve) resolveClass(type); return type;
            } catch (Exception e) { throw new ClassNotFoundException(name, e); }
         }
      };
   }

   static int withAlpha(int rgb, int alpha) { return rgb & 0xFFFFFF | alpha << 24; }

   static void close(double actual, double expected, String what) { check(Math.abs(actual - expected) < 1e-3, what + ": " + actual + " != " + expected); }

   static void targetHud(Path classes) throws Exception {
      Module hud = manager.find("targethud");
      // text(): upstream formula (name, health colour, difference, W/L, centring pad below 14 characters).
      check(OpenB4TargetHudBackend.text("Cheater", 10F, .5).equals("Cheater §e10 §aW"), "text() " + OpenB4TargetHudBackend.text("Cheater", 10F, .5));
      check(OpenB4TargetHudBackend.text("Bo", 4F, .2).equals("   Bo §c4 §aW   "), "text() padding/red " + OpenB4TargetHudBackend.text("Bo", 4F, .2));
      flag(hud, "show_difference", true);
      check(OpenB4TargetHudBackend.text("Cheater", 10F, .5).equals("Cheater §e10 §2+10 §aW"), "text() difference " + OpenB4TargetHudBackend.text("Cheater", 10F, .5));
      flag(hud, "show_difference", false); flag(hud, "show_win_loss", false); flag(hud, "show_health", false);
      check(OpenB4TargetHudBackend.text("Teammate1", 10F, .5).equals("    Teammate1    "), "text() name only " + OpenB4TargetHudBackend.text("Teammate1", 10F, .5));
      flag(hud, "show_win_loss", true); flag(hud, "show_health", true);

      // The KillAura-driven path, with GL recorded: the restored onRenderTickTargetHud draws through TargetHudModule.render.
      ClassLoader loader = recorder(classes);
      Class<?> backend = loader.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudBackend");
      Class<?> aura = loader.loadClass("dev.rice.client.combat.openb4.OpenB4KillAuraBackend");
      Class<?> targetType = loader.loadClass("dev.rice.client.combat.openb4.OpenB4KillAuraBackend$Target");
      Object recorded = backend.getConstructor(int.class).newInstance(0);
      backend.getMethod("link").invoke(recorded);
      Method guiUpdate = backend.getMethod("guiUpdate");
      Method resolution = loader.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudBackend$RenderUtils").getMethod("updateResolution");
      Method hudTick = aura.getMethod("onRenderTickTargetHud");
      ((ChoiceSetting)hud.findSetting("mode")).set(2); guiUpdate.invoke(recorded); resolution.invoke(null);
      B4AuraHost.context = c;
      Other enemy = other(20, "Cheater", 3, 65, 0); enemy.setHealth(10F);
      Constructor<?> make = targetType.getDeclaredConstructor(Entity.class); make.setAccessible(true);
      set(aura, null, "target", make.newInstance(enemy));
      set(aura, null, "hudSince", System.currentTimeMillis());
      // TargetHUD off: nothing.
      V116GlStub.reset(); font.drawn.clear(); hudTick.invoke(null);
      check(V116GlStub.calls == 0 && font.drawn.isEmpty(), "drew with TargetHUD off");
      toggle(hud); check(hud.isEnabled(), "TargetHUD did not enable");
      String text = "Cheater §e10 §aW";
      int w = text.length() * 6 + 8, x = 427 / 2 - w / 2 + 70, y = 240 / 2 + 40, x1 = x - 8, y1 = y - 8, x2 = x + w, y2 = y + 9 - 1 + 8, bx1 = x1 + 6, bx2 = x2 - 6;
      // First frame: upstream shows the bar from 'shown' (0) while it latches the target ratio.
      V116GlStub.reset(); font.drawn.clear(); hudTick.invoke(null);
      check(font.drawn.size() == 1 && font.drawn.get(0)[0].equals(text) && (float)font.drawn.get(0)[1] == x && (float)font.drawn.get(0)[2] == y
         && (boolean)font.drawn.get(0)[4] && (int)font.drawn.get(0)[3] == withAlpha(new java.awt.Color(220, 220, 220).getRGB(), 255), "HUD text/position: " + Arrays.deepToString(font.drawn.toArray()));
      check(V116GlStub.matrixDepth == 0 && V116GlStub.attribDepth == 0, "unbalanced GL stacks");
      List<V116GlStub.Primitive> prims = V116GlStub.primitives;
      check(prims.size() == 4 && prims.get(0).mode == 9 && prims.get(1).mode == 2 && prims.get(2).mode == 9 && prims.get(3).mode == 9, "Legacy primitives " + prims.size());
      close(prims.get(0).minX(), 2 * x1, "border left"); close(prims.get(0).maxX(), 2 * x2, "border right");
      close(prims.get(0).minY(), 2 * y1, "border top"); close(prims.get(0).maxY(), 2 * (y2 + 13), "border bottom");
      close(prims.get(2).minX(), 2 * bx1, "bar background left"); close(prims.get(2).maxX(), 2 * bx2, "bar background right"); close(prims.get(2).minY(), 2 * y2, "bar top");
      close(prims.get(3).maxX(), 2 * bx1, "first-frame bar width (upstream shown = 0)");
      // Second frame: the bar shows the ratio (10/20) in the Default (Frost) theme colours.
      V116GlStub.reset(); font.drawn.clear(); hudTick.invoke(null);
      prims = V116GlStub.primitives;
      close(prims.get(3).maxX(), 2 * (bx2 + (bx1 - bx2) * .5), "bar width at 50% health");
      float[] c1 = prims.get(3).colors.get(0);
      int frost = new java.awt.Color(253, 253, 253).getRGB();
      check(Math.abs(c1[0] - (frost >> 16 & 255) / 255F) < 1e-6 && Math.abs(c1[3] - 210 / 255F) < 1e-6, "bar colour (Frost, alpha 210): " + Arrays.toString(c1));
      // Health drops to 25%: the text is re-centred ("§c5" is shorter) and the bar animates over 125 ms from 50%.
      enemy.setHealth(5F);
      int w2 = "Cheater §c5 §aW".length() * 6 + 8, xx = 427 / 2 - w2 / 2 + 70, bx1b = xx - 8 + 6, bx2b = xx + w2 - 6;
      V116GlStub.reset(); font.drawn.clear(); hudTick.invoke(null);
      check(font.drawn.get(0)[0].equals("Cheater §c5 §aW") && (float)font.drawn.get(0)[1] == xx, "re-centred text");
      close(V116GlStub.primitives.get(3).maxX(), 2 * (bx2b + (bx1b - bx2b) * .5), "bar right after the drop");
      Thread.sleep(150L);
      V116GlStub.reset(); hudTick.invoke(null); V116GlStub.reset(); hudTick.invoke(null);
      close(V116GlStub.primitives.get(3).maxX(), 2 * (bx2b + (bx1b - bx2b) * (1 - .25)), "bar after the 125 ms animation");
      // Hold then fade: past 250 ms the fade starts; at full fade nothing draws; past 650 ms the HUD ends.
      set(aura, null, "hudSince", System.currentTimeMillis() - 300L);
      V116GlStub.reset(); hudTick.invoke(null);
      Object fade = get(aura, null, "hudFade");
      check(fade != null && V116GlStub.calls > 0, "fade did not start after the 250 ms hold");
      Class<?> animation = loader.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudBackend$Animation");
      animation.getField("start").set(fade, System.currentTimeMillis() - 1000L);
      V116GlStub.reset(); font.drawn.clear(); hudTick.invoke(null);
      check(V116GlStub.calls == 0 && font.drawn.isEmpty(), "drew at full fade");
      set(aura, null, "hudSince", System.currentTimeMillis() - 700L);
      hudTick.invoke(null);
      check((long)get(aura, null, "hudSince") == -1L && get(aura, null, "hudFade") == null, "HUD did not end after hold + fade");
      // Not drawn: Mode Disabled, a screen open, no hit since KillAura last swung.
      set(aura, null, "hudSince", System.currentTimeMillis());
      ((ChoiceSetting)hud.findSetting("mode")).set(0); guiUpdate.invoke(recorded);
      V116GlStub.reset(); hudTick.invoke(null); check(V116GlStub.calls == 0, "drew in Mode Disabled");
      ((ChoiceSetting)hud.findSetting("mode")).set(2); guiUpdate.invoke(recorded);
      mc.currentScreen = blank(net.minecraft.client.gui.GuiIngameMenu.class);
      V116GlStub.reset(); hudTick.invoke(null); check(V116GlStub.calls == 0, "drew with a screen open");
      mc.currentScreen = null;
      // Target gone: the last name and ratio stay on screen until the fade ends.
      set(aura, null, "target", null);
      V116GlStub.reset(); font.drawn.clear(); hudTick.invoke(null);
      check(font.drawn.size() == 1 && ((String)font.drawn.get(0)[0]).startsWith("Cheater §c5"), "lost target did not keep the last HUD: " + Arrays.deepToString(font.drawn.toArray()));
      set(aura, null, "hudSince", -1L);
      V116GlStub.reset(); hudTick.invoke(null); check(V116GlStub.calls == 0, "drew with no KillAura hit");

      // Render ESP: OpenB4 Render3D.entityBox(target, 2 = filled box, theme colour at 0, partial ticks); off by default.
      Method renderWorld = backend.getMethod("onRenderWorld", loader.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudHost$RenderWorldLastEvent"));
      Object event = Class.forName("dev.rice.client.render.openb4.OpenB4TargetHudHost$RenderWorldLastEvent").getConstructor(float.class).newInstance(1F);
      set(aura, null, "target", make.newInstance(enemy));
      V116GlStub.reset(); renderWorld.invoke(recorded, event); check(V116GlStub.boxes.isEmpty(), "Render ESP drew while off");
      flag(hud, "render_esp", true);
      V116GlStub.reset(); renderWorld.invoke(recorded, event);
      check(V116GlStub.boxes.size() == 1, "Render ESP box missing");
      Object[] esp = V116GlStub.boxes.get(0);
      int espColor = (int)esp[2];
      check(esp[0] == enemy && (int)esp[1] == 2 && (float)esp[3] == 1F && espColor >>> 24 == 255 && (espColor >> 16 & 255) >= 209 && (espColor >> 16 & 255) <= 253,
         "Render ESP call: entity " + ((Entity)esp[0]).getEntityId() + " mode " + esp[1] + " colour " + Integer.toHexString(espColor) + " pt " + esp[3]);
      flag(hud, "render_esp", false); set(aura, null, "target", null);

      // Position editor (real classes): opens from the momentary button, drags, resets, and persists on close.
      OpenB4TargetHudBackend.RenderUtils.updateResolution();
      flag(hud, "edit_position", true);
      check(!((BooleanSetting)hud.findSetting("edit_position")).isEnabled(), "Edit position did not reset itself");
      check(mc.currentScreen instanceof OpenB4TargetHudBackend.TargetHudEditor, "Edit position did not open the editor: " + mc.currentScreen);
      OpenB4TargetHudBackend.TargetHudEditor editor = (OpenB4TargetHudBackend.TargetHudEditor)mc.currentScreen;
      editor.updateScreen();
      String selfText = OpenB4TargetHudBackend.text("RiceTester", 20F, 1.0);
      int ew = selfText.length() * 6 + 8, ex = 427 / 2 - ew / 2 + 70, ey = 240 / 2 + 40;
      Method drag = OpenB4TargetHudBackend.TargetHudEditor.class.getDeclaredMethod("mouseClickMove", int.class, int.class, int.class, long.class); drag.setAccessible(true);
      Method release = OpenB4TargetHudBackend.TargetHudEditor.class.getDeclaredMethod("mouseReleased", int.class, int.class, int.class); release.setAccessible(true);
      drag.invoke(editor, ex, ey, 0, 0L); drag.invoke(editor, ex + 10, ey + 5, 0, 0L); release.invoke(editor, ex + 10, ey + 5, 0);
      check(OpenB4TargetHudBackend.offsetX == 80 && OpenB4TargetHudBackend.offsetY == 45, "drag did not move the HUD: " + OpenB4TargetHudBackend.offsetX + "," + OpenB4TargetHudBackend.offsetY);
      editor.onGuiClosed();
      check(((NumberSetting)hud.findSetting("pos_x")).doubleValue() == 80 && ((NumberSetting)hud.findSetting("pos_y")).doubleValue() == 45, "closing the editor did not store the position");
      Method action = OpenB4TargetHudBackend.TargetHudEditor.class.getDeclaredMethod("actionPerformed", GuiButton.class); action.setAccessible(true);
      action.invoke(editor, get(OpenB4TargetHudBackend.TargetHudEditor.class, editor, "resetButton"));
      check(OpenB4TargetHudBackend.offsetX == 70 && OpenB4TargetHudBackend.offsetY == 40, "Reset position");
      mc.currentScreen = null;
      // A stored position (profile load) reaches the HUD on the next render tick.
      ((NumberSetting)hud.findSetting("pos_x")).set(100.0); ((NumberSetting)hud.findSetting("pos_y")).set(-20.0);
      Method syncPosition=hud.getClass().getDeclaredMethod("syncPosition"); syncPosition.setAccessible(true); syncPosition.invoke(hud);
      check(OpenB4TargetHudBackend.offsetX == 100 && OpenB4TargetHudBackend.offsetY == -20, "stored position not applied");
      toggle(hud); check(!hud.isEnabled(), "TargetHUD did not disable");
      world.playerEntities.remove(enemy); world.loadedEntityList.remove(enemy);
   }
   /** Exercise the real GlStateScope cleanup with headless native attribute operations. */
   public static dev.rice.client.hook.GlStateScope openRenderScope() {
      try {
         final dev.rice.client.hook.GlStateCacheSnapshot cache=dev.rice.client.hook.GlStateCacheSnapshot.capture();
         final Object[][] state={null};
         Class<?> attrs=Class.forName("dev.rice.client.hook.GlStateScope$Attributes");
         Object adapter=Proxy.newProxyInstance(attrs.getClassLoader(),new Class<?>[]{attrs},(proxy,method,args)->{
            if(method.getName().equals("pushServer")) {state[0]=V116GlStub.saveState(); V116GlStub.attribDepth++;}
            if(method.getName().equals("popServer")) {V116GlStub.restoreState(state[0]); V116GlStub.attribDepth--;}
            return null;
         });
         Constructor<?> make=dev.rice.client.hook.GlStateScope.class.getDeclaredConstructor(attrs,Runnable.class); make.setAccessible(true);
         return (dev.rice.client.hook.GlStateScope)make.newInstance(adapter,(Runnable)cache::restore);
      } catch(ReflectiveOperationException failure) {throw new IllegalStateException(failure);}
   }

   static void renderIsolation(Path classes)throws Exception {
      ClassLoader loader=recorder(classes);
      Class<?> scope=loader.loadClass("dev.rice.client.render.RenderPassScope");
      Field tex=net.minecraft.client.renderer.OpenGlHelper.class.getDeclaredField("defaultTexUnit"); tex.setAccessible(true); tex.setInt(null,33984);
      tex=net.minecraft.client.renderer.OpenGlHelper.class.getDeclaredField("lightmapTexUnit"); tex.setAccessible(true); tex.setInt(null,33985);
      V116GlStub.reset(); V116GlStub.lighting=true; V116GlStub.fog=true; V116GlStub.textures.put(33985,true);
      V116GlStub.program=7; V116GlStub.framebuffer=11; V116GlStub.matrixMode=5890;
      AutoCloseable hud=(AutoCloseable)scope.getMethod("hud",int.class,int.class).invoke(null,427,240);
      check(!V116GlStub.lighting && !V116GlStub.fog && !V116GlStub.textures.get(33985) && V116GlStub.activeTexture==33984
         && V116GlStub.program==0 && V116GlStub.orthoCalls==1,"HUD inherited world/shader/lightmap state");
      V116GlStub.framebuffer=99; hud.close();
      check(V116GlStub.program==7 && V116GlStub.framebuffer==11 && V116GlStub.matrixMode==5890 && V116GlStub.matrixDepth==0
         && V116GlStub.attribDepth==0 && V116GlStub.lighting && V116GlStub.fog && V116GlStub.textures.get(33985),"HUD state not restored");
      try(AutoCloseable world=(AutoCloseable)scope.getMethod("world").invoke(null)) {
         V116GlStub.program=123; V116GlStub.framebuffer=321; V116GlStub.lighting=false;
         throw new java.io.IOException("scripted renderer failure");
      } catch(java.io.IOException expected) {}
      check(V116GlStub.program==7 && V116GlStub.framebuffer==11 && V116GlStub.lighting && V116GlStub.matrixDepth==0,"failed renderer leaked state");
      // Run the actual text executor with colored team/rank formatting under the poisoned world state.
      net.minecraft.client.renderer.entity.RenderItem item=blank(net.minecraft.client.renderer.entity.RenderItem.class);
      for(Field field:Minecraft.class.getDeclaredFields()) if(field.getType()==net.minecraft.client.renderer.entity.RenderItem.class) {field.setAccessible(true);field.set(mc,item);}
      Class<?> access=loader.loadClass("dev.rice.client.render.RenderAccess"); Method draw=access.getDeclaredMethod("execute",dev.rice.client.render.DrawPlan.class); draw.setAccessible(true);
      font.drawn.clear(); requireUnlitFont=true;
      try {draw.invoke(null,new dev.rice.client.render.DrawPlan().text("\u00a7cRed \u00a7aGreen",0,0,-1,true,9));} finally {requireUnlitFont=false;}
      check(font.drawn.size()==1 && font.drawn.get(0)[0].equals("\u00a7cRed \u00a7aGreen") && (int)font.drawn.get(0)[3]==-1,"formatted name-tag color changed");
      check(V116GlStub.lighting && V116GlStub.fog && V116GlStub.textures.get(33985) && V116GlStub.matrixDepth==0,"name-tag text leaked render state");
      // Real Legacy HUD driver, with a live AC target, from a shader/world matrix rather than an assumed HUD projection.
      Module module=(Module)loader.loadClass("dev.rice.module.impl.TargetHudModule").getConstructor(ModuleManager.class).newInstance(manager);
      ((ChoiceSetting)module.findSetting("mode")).set(2);
      Module ac=manager.find("autoclicker"); mark(ac,true); Other target=other(89,"LegacyTarget",2,65,0); mc.currentScreen=null; mc.inGameHasFocus=true;
      mc.objectMouseOver=new net.minecraft.util.MovingObjectPosition(target); font.drawn.clear(); module.onRenderTick(c);
      check(font.drawn.size()==1 && ((String)font.drawn.get(0)[0]).startsWith("LegacyTarget") && V116GlStub.program==7
         && V116GlStub.framebuffer==11 && V116GlStub.matrixMode==5890 && V116GlStub.matrixDepth==0 && V116GlStub.attribDepth==0,"Legacy HUD did not draw/restore shader and matrices");
      mark(ac,false); mc.objectMouseOver=null; world.playerEntities.remove(target); world.loadedEntityList.remove(target);
      System.out.println("PASS: unlit formatted name-tag text; Legacy HUD explicit projection; shader/framebuffer/matrix/lighting restoration including exceptions");
   }

   public static final class BindProbe extends Module {
      final NumberSetting value=add(new NumberSetting("value","Value",1,0,20,1));
      BindProbe(){super("bind_probe","Bind Probe","Config bind regression",ModuleCategory.UTILITY);}
   }

   public static final class QuietMouse extends net.minecraft.util.MouseHelper {
      @Override public void grabMouseCursor() {}
      @Override public void ungrabMouseCursor() {}
   }

   static void configBinds()throws Exception {
      java.nio.file.Path directory=Files.createTempDirectory("rice-config-bind-check-");
      ModuleManager test=new ModuleManager(c); BindProbe probe=new BindProbe();
      dev.rice.module.impl.GuiModule gui=new dev.rice.module.impl.GuiModule(test);
      test.register(probe); test.register(gui); dev.rice.client.render.GuiAppearance.configureMissing(gui);
      probe.setBinding(37);
      dev.rice.config.ConfigStore global=new dev.rice.config.ConfigStore(directory.resolve("global.json"));
      dev.rice.client.render.ProfileServices.FilesHost host=new dev.rice.client.render.ProfileServices.FilesHost(test,directory.resolve("profiles"),global);
      int[] loads={0};
      dev.rice.client.render.ProfileServices.Host monitored=(dev.rice.client.render.ProfileServices.Host)Proxy.newProxyInstance(
         V116Check.class.getClassLoader(),new Class<?>[]{dev.rice.client.render.ProfileServices.Host.class},(proxy,method,args)->{
            if(method.getName().equals("load")) loads[0]++;
            try{return method.invoke(host,args);}catch(java.lang.reflect.InvocationTargetException error){throw error.getCause();}
         });
      test.input().profiles(global,monitored);
      probe.value.set(2D); host.save("A",false); probe.value.set(9D); host.save("B",false);
      global.profileBindings(Arrays.asList(new dev.rice.config.RecoveredConfiguration.ProfileBinding("A",37),new dev.rice.config.RecoveredConfiguration.ProfileBinding("B",1)));
      global.save(test);
      dev.rice.config.ConfigStore reloaded=new dev.rice.config.ConfigStore(directory.resolve("global.json")); reloaded.load(test); test.applyPendingChanges();
      check(reloaded.profileBindings().size()==2,"profile bindings did not persist");
      host.load("B");
      dev.rice.client.ClientEventBridge bridge=new dev.rice.client.ClientEventBridge(test,null,null);
      Method poll=dev.rice.client.ClientEventBridge.class.getDeclaredMethod("pollBinds");poll.setAccessible(true);
      Field mapping=dev.rice.client.NativeKeyMap.class.getDeclaredField("CODES");mapping.setAccessible(true);int[] codes=(int[])mapping.get(null);int previous=codes[37];codes[37]=38;
      put(mc,"mouseHelper",blank(QuietMouse.class));
      try {
         V115Input.clear(); mc.currentScreen=null; mc.inGameHasFocus=true; poll.invoke(bridge);
         V115Input.keys[38]=true; poll.invoke(bridge);
         check(host.current().equals("A") && probe.value.doubleValue()==2D && loads[0]==1,"keyboard config bind missed without native input events");
         check(!probe.isEnabled(),"shared config/module key toggled module after profile load");
         poll.invoke(bridge); poll.invoke(bridge); check(loads[0]==1,"held key repeatedly loaded config");
         test.input().input(new dev.rice.client.InputQuery.Request(2,37,true)); poll.invoke(bridge);
         check(loads[0]==1,"native input event duplicated polled config load");
         V115Input.keys[38]=false; poll.invoke(bridge); V115Input.keys[38]=true; poll.invoke(bridge);
         check(loads[0]==2,"second press did not reload config"); V115Input.keys[38]=false; poll.invoke(bridge);
         mc.currentScreen=blank(net.minecraft.client.gui.GuiIngameMenu.class); V115Input.keys[38]=true; poll.invoke(bridge);
         mc.currentScreen=null; poll.invoke(bridge); check(loads[0]==2,"key held through menu close loaded profile");
         V115Input.keys[38]=false; poll.invoke(bridge); mc.inGameHasFocus=false; V115Input.keys[38]=true; poll.invoke(bridge);
         mc.inGameHasFocus=true; poll.invoke(bridge); check(loads[0]==2,"unfocused keypress loaded profile on return");
         V115Input.keys[38]=false; poll.invoke(bridge); V115Input.buttons[0]=true; poll.invoke(bridge);
         check(host.current().equals("B") && probe.value.doubleValue()==9D && loads[0]==3,"mouse config bind missed");
         poll.invoke(bridge); check(loads[0]==3,"held mouse repeatedly loaded config"); V115Input.buttons[0]=false; poll.invoke(bridge);
         // Bound configs replace dirty edits immediately without opening the GUI.
         probe.value.set(7D); V115Input.keys[38]=true; poll.invoke(bridge);
         check(!gui.isEnabled() && gui.runtime().modal==0 && mc.currentScreen==null && host.current().equals("A")
            && probe.value.doubleValue()==2D && loads[0]==4 && !host.pending(),"dirty keyboard config bind did not load immediately");
         poll.invoke(bridge); check(loads[0]==4,"held dirty config key repeated load");
         V115Input.keys[38]=false; poll.invoke(bridge); probe.value.set(6D);
         V115Input.buttons[0]=true; poll.invoke(bridge);
         check(!gui.isEnabled() && gui.runtime().modal==0 && host.current().equals("B")
            && probe.value.doubleValue()==9D && loads[0]==5 && !host.pending(),"dirty mouse config bind did not load immediately");
         check(Files.readAllBytes(directory.resolve("profiles/A")).length>0,"profile A disappeared");
         mc.currentScreen=null; mark(gui,false);
         System.out.println("PASS: persisted config keyboard/mouse binds without native events; one load per press; no duplicate event load; shared module key; menu/focus/held-key suppression; immediate replacement of dirty edits without confirmation");
      } finally {codes[37]=previous; V115Input.clear(); mc.currentScreen=null; mark(gui,false);}
   }

   static void mark(Module module, boolean enabled) throws Exception {
      Method mark = Module.class.getDeclaredMethod("markEnabled", boolean.class); mark.setAccessible(true); mark.invoke(module, enabled);
   }

   static void assistedHud(Path classes) throws Exception {
      ClassLoader loader = recorder(classes);
      Class<?> type = loader.loadClass("dev.rice.module.impl.TargetHudModule");
      Module hud = (Module)type.getConstructor(ModuleManager.class).newInstance(manager);
      Class<?> backend = loader.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudBackend");
      ((ChoiceSetting)hud.findSetting("mode")).set(2);
      Object engine = get(type, hud, "backend"); backend.getMethod("guiUpdate").invoke(engine);
      loader.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudBackend$RenderUtils").getMethod("updateResolution").invoke(null);
      Method select = type.getDeclaredMethod("assistedTarget", ClientContext.class); select.setAccessible(true);
      Method render = type.getDeclaredMethod("renderAssisted", ClientContext.class); render.setAccessible(true);
      Module clicker = manager.find("autoclicker"), aim = manager.find("aimassist");
      Other enemy = other(81, "ClickTarget", 2, 65, 0), aimed = other(82, "AimTarget", 3, 65, 0);
      mc.currentScreen = null; mc.inGameHasFocus = true;
      mc.objectMouseOver = new net.minecraft.util.MovingObjectPosition(enemy);
      check(select.invoke(hud, c) == null, "disabled AC/AA selected a target");
      mark(clicker, true);
      check(select.invoke(hud, c) == enemy, "AC crosshair target missing");
      V116GlStub.reset(); font.drawn.clear(); render.invoke(hud, c);
      check(font.drawn.size() == 1 && ((String)font.drawn.get(0)[0]).startsWith("ClickTarget"), "AC HUD did not draw");
      enemy.setHealth(5F); font.drawn.clear(); render.invoke(hud, c);
      check(((String)font.drawn.get(0)[0]).contains("\u00a7c5"), "AC HUD health not updated");
      mark(aim, true); put(aim, "target", aimed); put(aim, "targetActive", true);
      check(select.invoke(hud, c) == enemy, "AC crosshair priority");
      mc.objectMouseOver = null;
      check(select.invoke(hud, c) == aimed, "active AA target missing");
      font.drawn.clear(); render.invoke(hud, c);
      check(((String)font.drawn.get(0)[0]).startsWith("AimTarget"), "AA HUD did not draw");
      put(aim, "targetActive", false); check(select.invoke(hud, c) == null, "inactive AA target selected");
      put(aim, "targetActive", true); aimed.isDead = true; check(select.invoke(hud, c) == null, "dead target selected"); aimed.isDead = false;
      world.loadedEntityList.remove(aimed); check(select.invoke(hud, c) == null, "unloaded target selected"); world.loadedEntityList.add(aimed);
      mc.inGameHasFocus = false; check(select.invoke(hud, c) == null, "unfocused target selected"); mc.inGameHasFocus = true;
      mc.currentScreen = blank(net.minecraft.client.gui.GuiIngameMenu.class);
      V116GlStub.reset(); render.invoke(hud, c); check(V116GlStub.calls == 0, "assisted HUD drew over menu"); mc.currentScreen = null;
      mark(aim, false); mark(clicker, false);
      set(type, hud, "assistedSince", System.currentTimeMillis() - 300L);
      render.invoke(hud, c); check(get(type, hud, "assistedFade") != null, "assisted HUD fade missing");
      set(type, hud, "assistedSince", System.currentTimeMillis() - 700L);
      V116GlStub.reset(); check(!(Boolean)render.invoke(hud, c) && V116GlStub.calls == 0, "assisted HUD did not expire");
      set(type, hud, "assistedSince", System.currentTimeMillis()); hud.onWorldUnload(c);
      check((long)get(type, hud, "assistedSince") == -1L, "assisted HUD survived world unload");
      put(aim, "target", null); put(aim, "targetActive", false);

      // Exercise the real menu-close -> queued GUI disable, which used to dismiss the new editor.
      dev.rice.module.impl.GuiModule gui = (dev.rice.module.impl.GuiModule)manager.find("gui");
      mark(gui, true); mc.inGameHasFocus = false;
      mc.displayGuiScreen(new dev.rice.client.render.RecoveredGuiScreen(manager, gui, null));
      check(!mc.currentScreen.allowUserInput,"GUI allowed vanilla mouse input");
      flag(gui,"allow_input",true);
      Method sync=dev.rice.client.render.RecoveredGuiScreen.class.getDeclaredMethod("syncMovementKeys"); sync.setAccessible(true); sync.invoke(mc.currentScreen);
      check(!mc.currentScreen.allowUserInput,"Allow input re-enabled vanilla mouse clicks");
      check(GameHooks.guiBlocksClicks() && GameHooks.beforeInteraction(false) && GameHooks.beforeInteraction(true),"GUI leaked attacks/item use");
      for(Module module:manager.all()) {
         String description=dev.rice.client.render.GuiAppearance.description(module);
         check(!java.util.regex.Pattern.compile("(?i)\\b(openb4|openjade|raven|myau|mayu|meow|liquidbounce|vape)\\b").matcher(description).find(),"other-client branding in GUI: "+description);
      }
      flag(hud, "edit_position", true);
      Object editor = mc.currentScreen;
      check(editor != null && editor.getClass().getName().endsWith("$TargetHudEditor"), "position editor not opened");
      manager.applyPendingChanges();
      check(!gui.isEnabled() && mc.currentScreen == editor && !mc.inGameHasFocus, "queued menu close dismissed position editor");
      // Persist even while HUD is disabled; resolution was refreshed by the edit action.
      int[] saves = {0}; manager.setPersistence(() -> saves[0]++);
      backend.getField("offsetX").setInt(null, 91); backend.getField("offsetY").setInt(null, 52);
      ((net.minecraft.client.gui.GuiScreen)editor).onGuiClosed();
      check(((NumberSetting)hud.findSetting("pos_x")).doubleValue() == 91 && saves[0] == 1, "editor did not persist position");
      manager.setPersistence(() -> {}); mc.currentScreen = null;
      check(!GameHooks.guiBlocksClicks(),"GUI click isolation survived menu close");
      world.playerEntities.remove(enemy); world.loadedEntityList.remove(enemy); world.playerEntities.remove(aimed); world.loadedEntityList.remove(aimed);
      System.out.println("PASS: AC/AA HUD draw, health, target priority/validity, focus/menu, fade/unload; real GUI handoff keeps editor open; disabled HUD position save");
   }

}
