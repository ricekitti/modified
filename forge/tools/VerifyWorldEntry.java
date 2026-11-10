import dev.rice.client.hook.HookLifecycle;
import dev.rice.client.hook.MayuHooks;
import dev.rice.forge.ForgeEnvironment;
import dev.rice.inject.Mappings;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;

/** Regression for callbacks reached when a world renders before Rice is unlocked. */
public final class VerifyWorldEntry {
    static Object unsafe;
    static Method allocate;
    static <T> T empty(Class<T> type) throws Exception { return type.cast(allocate.invoke(unsafe, type)); }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void field(Class<?> owner, Object target, String named, Object value) throws Exception {
        Field field = owner.getDeclaredField(Mappings.mapField(owner.getName().replace('.', '/'), named));
        field.setAccessible(true);
        field.set(target, value);
    }
    static double number(Class<?> owner, Object target, String named) throws Exception {
        Field field = owner.getDeclaredField(Mappings.mapField(owner.getName().replace('.', '/'), named));
        field.setAccessible(true);
        return field.getDouble(target);
    }

    public static void main(String[] args) throws Exception {
        ForgeEnvironment.setDevelopment(Boolean.parseBoolean(args[0]));
        ForgeEnvironment.initialize();
        Class<?> type = Class.forName("sun.misc.Unsafe");
        Field instance = type.getDeclaredField("theUnsafe");
        instance.setAccessible(true);
        unsafe = instance.get(null);
        allocate = type.getMethod("allocateInstance", Class.class);

        dev.rice.security.LicenseRuntime.revoke();
        check(!HookLifecycle.active(), "Hooks active before licensed initialization");
        check(!HookLifecycle.hostInitialized(), "World test unexpectedly initialized hook host");

        EntityPlayerSP player = empty(EntityPlayerSP.class);
        EntityRenderer renderer = empty(EntityRenderer.class);
        RenderManager renderManager = empty(RenderManager.class);
        Minecraft minecraft = empty(Minecraft.class);

        // These are the paired callbacks reached by the first rendered world frame.
        MayuHooks.beforeUpdateCameraAndRender(0.5F, 1L, renderer);
        MayuHooks.afterUpdateCameraAndRender(0.5F, 1L, renderer);
        MayuHooks.beforeUpdateRenderer(renderer);
        MayuHooks.afterUpdateRenderer(renderer);
        MayuHooks.beforePlayerUpdate(player);
        MayuHooks.afterPlayerUpdate(player);
        MayuHooks.beforeRenderEntityStatic(player, 0.5F, false, renderManager);
        MayuHooks.afterRenderEntityStatic(player, 0.5F, false, renderManager);

        // Every unpaired callback must also be passive while locked/unloaded.
        MayuHooks.beforeWalkingPlayerUpdate();
        MayuHooks.beforeLivingUpdate();
        MayuHooks.afterMovementInputUpdate();
        MayuHooks.afterRunGameLoop();
        MayuHooks.beforeRunTick(minecraft);
        MayuHooks.afterRunTick(minecraft);
        MayuHooks.beforeLoadWorld(null, "test");
        MayuHooks.afterRenderTitle(0.5F);
        MayuHooks.afterRenderWorld(null, 0.5F);
        check(!MayuHooks.beforeLeftClick(minecraft), "Locked left click was cancelled");
        check(!MayuHooks.beforeRightClick(), "Locked right click was cancelled");
        check(!MayuHooks.beforeBlockClick(minecraft), "Locked block click was cancelled");
        check(!MayuHooks.beforeSetVelocity(1, 2, 3, player), "Locked velocity was cancelled");
        check(!MayuHooks.onChannelRead(null, null), "Locked inbound packet was cancelled");
        check(!MayuHooks.onSendPacket(null), "Locked outbound packet was cancelled");
        check(!MayuHooks.onSendPacketWithListeners(null, null, null), "Locked listener packet was cancelled");

        // Redirect hooks must execute the original vanilla operation, not just no-op.
        check(!MayuHooks.applySpoofedRotationBeforeRidingCheck(player, player), "Riding fallback changed");
        check(MayuHooks.modifyJumpYawRadians(1.25F, player) == 1.25F, "Jump yaw fallback changed");
        check(MayuHooks.modifySafeWalkFlag(true, player), "Safe-walk fallback changed");
        KeyBinding key = new KeyBinding("test", 1, "test");
        check(MayuHooks.onIsPressed(true, key), "Key fallback changed");
        InventoryPlayer inventory = new InventoryPlayer(player);
        check(MayuHooks.redirectCurrentHotbarItem(inventory) == null, "Hotbar item fallback changed");
        MayuHooks.redirectHotbarScroll(inventory, 1);
        field(net.minecraft.entity.Entity.class, player, "rotationYaw", 0.0F);
        MayuHooks.redirectMoveFlying(player, 1.0F, 0.0F, 1.0F, player);
        check(number(net.minecraft.entity.Entity.class, player, "motionX") != 0.0D ||
              number(net.minecraft.entity.Entity.class, player, "motionZ") != 0.0D,
              "Movement fallback skipped vanilla operation");

        check(!HookLifecycle.hostInitialized(), "Locked hooks initialized hook host");
        System.out.println("PASS: locked/uninitialized world-entry hooks are passive and redirects preserve vanilla behavior.");
    }
}
