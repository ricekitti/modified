import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.SemanticEvent;
import dev.rice.config.InitialSettings;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.FastMineModule;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;

/** Original Slinky source identity and real mapped controller field/event behavior. */
public final class SlinkyFastMineCheck {
    private static void check(boolean pass, String message) {
        if (!pass) throw new AssertionError(message);
    }

    private static void close(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.000001F, message + ": " + actual);
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        String expected = new String(Files.readAllBytes(root.resolve("tools/fixtures/slinky/FastMineModule.java.txt")), StandardCharsets.UTF_8)
            .replace("dev.slinky", "dev.rice").replace("\r\n", "\n");
        String actual = new String(Files.readAllBytes(root.resolve("java/src/main/java/dev/rice/module/impl/FastMineModule.java")), StandardCharsets.UTF_8)
            .replace("\r\n", "\n");
        check(expected.equals(actual), "Fast Mine differs from original Slinky beyond namespace adaptation");
        close(FastMineModule.acceleratedProgress(0.1F, 2F), 0.6F, "first damage acceleration");
        close(FastMineModule.acceleratedProgress(0.25F, 2F), 0.75F, "quarter-progress acceleration");
        close(FastMineModule.acceleratedProgress(0.2F, 1F), 0.2F, "speed one changed mining progress");

        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
        Minecraft mc = (Minecraft)allocate.invoke(unsafe, Minecraft.class);
        PlayerControllerMP controller = (PlayerControllerMP)allocate.invoke(unsafe, PlayerControllerMP.class);
        mc.playerController = controller;
        ClientContext context = new ClientContext(mc);
        ModuleManager manager = new ModuleManager(context);
        FastMineModule module = new FastMineModule();
        manager.register(module);
        InitialSettings.applyMissing(manager);
        ((NumberSetting)module.findSetting("speed")).set(2D);
        ((NumberSetting)module.findSetting("break_delay")).set(2D);
        ((BooleanSetting)module.findSetting("decr_block_hit_delay")).set(true);
        check(module.getId().equals("fastmine") && module.listens(0) && module.listens(18) && !module.listens(17), "registration or event contract changed");
        manager.requestEnabled(module, true);
        manager.applyPendingChanges();

        MinecraftFields.setBlockHitDelay(controller, 5);
        MinecraftFields.setBlockDamage(controller, 0F);
        manager.dispatch(new SemanticEvent(0));
        check(MinecraftFields.blockHitDelay(controller) == 2, "ray phase did not clamp break delay");
        MinecraftFields.setBlockDamage(controller, 0.1F);
        manager.dispatch(new SemanticEvent(18));
        close(MinecraftFields.blockDamage(controller), 0.6F, "post-update did not accelerate first progress");
        manager.dispatch(new SemanticEvent(0));
        check(MinecraftFields.blockHitDelay(controller) == 2, "delay decremented before repeated observation");
        MinecraftFields.setBlockDamage(controller, 0.7F);
        manager.dispatch(new SemanticEvent(18));
        close(MinecraftFields.blockDamage(controller), 0.7F, "progress repeatedly boosted within one block");
        manager.dispatch(new SemanticEvent(0));
        check(MinecraftFields.blockHitDelay(controller) == 1, "stuck delay was not decremented");

        MinecraftFields.setBlockDamage(controller, 0F);
        manager.dispatch(new SemanticEvent(0));
        manager.dispatch(new SemanticEvent(18));
        close(MinecraftFields.blockDamage(controller), 0F, "idle mining progress became nonzero");
        MinecraftFields.setBlockDamage(controller, 0.25F);
        manager.dispatch(new SemanticEvent(18));
        close(MinecraftFields.blockDamage(controller), 0.75F, "new block did not rearm acceleration");
        Field stateField = FastMineModule.class.getDeclaredField("state");
        stateField.setAccessible(true);
        FastMineModule.State state = (FastMineModule.State)stateField.get(module);
        manager.dispatch(new SemanticEvent(0));
        check(state.savedProgress > 0F, "progress observation missing");
        manager.requestEnabled(module, false);
        manager.applyPendingChanges();
        check(state.savedProgress == 0F && state.lastDelay == 0, "disable retained mining state");
        MinecraftFields.setBlockDamage(controller, 0.2F);
        manager.dispatch(new SemanticEvent(18));
        close(MinecraftFields.blockDamage(controller), 0.2F, "disabled Fast Mine changed progress");
        state.savedProgress = 0.5F;
        state.lastDelay = 3;
        module.onWorldUnload(context);
        check(state.savedProgress == 0F && state.lastDelay == 0, "world unload retained mining state");
        mc.playerController = null;
        module.onEvent(context, new SemanticEvent(0));
        module.onEvent(context, new SemanticEvent(18));
        check(state.lastDelay == 0, "missing controller did not reset delay tracking");
        System.out.println("PASS: original Slinky Fast Mine source parity; mapped ray/post-update hooks; delay clamp/decrement; first-progress acceleration; next-block rearm; idle/speed-one/off/null-controller behavior; disable/world cleanup");
    }
}
