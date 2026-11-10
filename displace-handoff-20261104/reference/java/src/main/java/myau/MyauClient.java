package myau;

import java.lang.reflect.Field;

/**
 * Rice host for Mayu's {@code myau.MyauClient}: the same public fields, initialised the way upstream
 * {@code MyauClient.initialize()} does for the ported Scaffold + BedNuker closure (tools/port-mayu.py).
 *
 * <ul>
 *   <li>Infrastructure: the outgoing/incoming packet buffers, the packet delay manager and the packet action tracker
 *       are the verbatim Mayu classes, registered on the bus in upstream order. RotationSmoother, GroundSpoofManager,
 *       HurtTimeTracker, the friend/enemy lists, SettingManager and CommandManager are not part of the port (nothing
 *       ported reaches them); {@link #commandManager} stays null, which upstream code null-checks.</li>
 *   <li>Modules are registered in upstream's order, so the bus keeps upstream's listener order for equal priorities.
 *       HUDModule is constructed (Scaffold/BedNuker read its scale, shadow and colour) but kept off the bus: its
 *       overlay handler draws Mayu's own HUD without an enabled check, and Rice draws its own HUD.</li>
 *   <li>KillAuraModule, LongJumpModule and TargetStrafeModule are always-disabled host stand-ins (see their files).</li>
 * </ul>
 */
public final class MyauClient {
   public static final String CHAT_PREFIX = "&7[&bMyau&7] &r";
   public static final String VERSION = "1.0.0";
   public static EventBus eventBus;
   public static OutgoingPacketBuffer outgoingPacketBuffer;
   public static IncomingPacketBuffer incomingPacketBuffer;
   public static PacketDelayManager packetDelayManager;
   public static PacketActionTracker packetActionTracker;
   public static ModuleManager moduleManager;
   public static CommandManager commandManager;
   private static boolean initialized;

   private MyauClient() {
   }

   public static synchronized void initialize() {
      if (!initialized) {
         eventBus = new EventBus();
         outgoingPacketBuffer = new OutgoingPacketBuffer();
         incomingPacketBuffer = new IncomingPacketBuffer();
         packetDelayManager = new PacketDelayManager();
         packetActionTracker = new PacketActionTracker();
         moduleManager = new ModuleManager();
         eventBus.register(outgoingPacketBuffer);
         eventBus.register(incomingPacketBuffer);
         eventBus.register(packetDelayManager);
         registerModule(new BedESPModule(), true);
         registerModule(new BedNukerModule(), true);
         registerModule(new HUDModule(), false);
         registerModule(new KeepSprintModule(), true);
         registerModule(new KillAuraModule(), true);
         registerModule(new LongJumpModule(), true);
         registerModule(new NoHitDelayModule(), true);
         registerModule(new InjectScaffoldView(), true);
         registerModule(new TargetStrafeModule(), true);
         initialized = true;
      }
   }

   public static boolean initialized() {
      return initialized;
   }

   /** Upstream registerModule: module map, bus registration, and every public Setting field bound to its module. */
   private static void registerModule(Module module, boolean bus) {
      moduleManager.modules.put(module.getClass(), module);
      if (bus) {
         eventBus.register(module);
      }

      for (Field field : module.getClass().getFields()) {
         if (Setting.class.isAssignableFrom(field.getType())) {
            try {
               Setting<?> setting = (Setting<?>)field.get(module);
               if (setting != null) {
                  setting.setModule(module);
               }
            } catch (IllegalAccessException error) {
               throw new IllegalStateException("Unable to register setting " + field.getName(), error);
            }
         }
      }
   }
}
