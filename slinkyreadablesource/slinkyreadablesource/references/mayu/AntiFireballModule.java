package myau;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MathHelper;

public class AntiFireballModule
extends Module {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    private final ArrayList<EntityFireball> candidateFireballs = new ArrayList();
    private final ArrayList<EntityFireball> ignoredCloseFireballs = new ArrayList();
    private EntityFireball targetFireball;
    public final FloatSetting range = new FloatSetting("range", Float.valueOf(5.0f), Float.valueOf(3.0f), Float.valueOf(8.0f));
    public final IntegerSetting fov = new IntegerSetting("fov", 360, 1, 360);
    public final BooleanSetting rotations = new BooleanSetting("rotations", true);
    public final BooleanSetting swing = new BooleanSetting("swing", true);
    public final ModeSetting moveFix = new ModeSetting("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
    public final ModeSetting showTarget = new ModeSetting("show-target", 0, new String[]{"NONE", "DEFAULT", "HUD"});

    public AntiFireballModule() {
        super("AntiFireball", false, 0);
    }

    private boolean isValidTarget(EntityFireball fireball) {
        if (fireball.getEntityBoundingBox().func_181656_b()) {
            return false;
        }
        if (RotationUtils.distanceToEntity((Entity)fireball) > (double)((Float)this.range.getValue()).floatValue() + 3.0) {
            return false;
        }
        return RotationUtils.entityAimError((Entity)fireball) <= (float)((Integer)this.fov.getValue()).intValue();
    }

    private void swing() {
        if (((Boolean)this.swing.getValue()).booleanValue()) {
            AntiFireballModule.minecraft.thePlayer.swingItem();
        } else {
            PacketUtils.send(new C0APacketAnimation());
        }
    }

    @EventHandler
    public void onPreTick(PreTickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        List<EntityFireball> loadedFireballs = (List<EntityFireball>)(List<?>)AntiFireballModule.minecraft.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityFireball).map(entity -> (EntityFireball)entity).collect(Collectors.toList());
        this.candidateFireballs.removeIf(fireball -> !loadedFireballs.contains(fireball));
        this.ignoredCloseFireballs.removeIf(fireball -> !loadedFireballs.contains(fireball));
        for (EntityFireball fireball2 : loadedFireballs) {
            if (this.candidateFireballs.contains(fireball2) || this.ignoredCloseFireballs.contains(fireball2)) continue;
            if (RotationUtils.distanceToEntity((Entity)fireball2) > 3.0) {
                this.candidateFireballs.add(fireball2);
                continue;
            }
            this.ignoredCloseFireballs.add(fireball2);
        }
        if (AntiFireballModule.minecraft.thePlayer.capabilities.allowFlying) {
            this.targetFireball = null;
            return;
        }
        this.targetFireball = this.candidateFireballs.stream().filter(this::isValidTarget).min(Comparator.comparingDouble(RotationUtils::distanceToEntity)).orElse(null);
    }

    @EventHandler(priority=-100)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (!this.isEnabled() || !EntityUtils.isLoaded((Entity)this.targetFireball)) {
            return;
        }
        if (((Boolean)this.rotations.getValue()).booleanValue() && !ItemUtils.isHoldingBlocks() && !ItemUtils.isHoldingBow() && !ItemUtils.isHoldingFireball()) {
            float previousYaw = event.getPreviousYaw();
            float previousPitch = event.getPreviousPitch();
            float[] targetRotation = RotationUtils.aimAtBounds(this.targetFireball.getEntityBoundingBox());
            float yaw = RotationUtils.snapToMouseSensitivity(RotationUtils.nearestEquivalentAngle(targetRotation[0], previousYaw), previousYaw);
            float pitch = RotationUtils.snapToMouseSensitivity(targetRotation[1], previousPitch);
            pitch = MathHelper.clamp_float((float)pitch, (float)-90.0f, (float)90.0f);
            event.setRotation(yaw, pitch, 0);
            if ((Integer)this.moveFix.getValue() != 0) {
                event.setMovementYaw(yaw, 0);
            }
        }
        if (MyauClient.packetActionTracker.sentUseEntity || MyauClient.packetActionTracker.sentDigging || MyauClient.packetActionTracker.sentBlockPlacement) {
            return;
        }
        this.swing();
        if (RotationUtils.distanceToEntity((Entity)this.targetFireball) <= (double)((Float)this.range.getValue()).floatValue()) {
            PacketUtils.send(new C02PacketUseEntity((Entity)this.targetFireball, C02PacketUseEntity.Action.ATTACK));
            PlayerUtils.attackLocally((Entity)this.targetFireball);
        }
    }

    @EventHandler
    public void onMovementInput(MovementInputEvent event) {
        if (!(this.isEnabled() && (Integer)this.moveFix.getValue() == 1 && RotationState.isActive() && RotationState.movementPriority() == 0.0f && MovementUtils.hasMovementInput())) {
            return;
        }
        MovementUtils.correctMovementForYaw(RotationState.movementYaw());
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.isEnabled() || (Integer)this.showTarget.getValue() == 0 || !EntityUtils.isLoaded((Entity)this.targetFireball)) {
            return;
        }
        Color color = Color.WHITE;
        switch ((Integer)this.showTarget.getValue()) {
            case 1: {
                double approachDotProduct = (this.targetFireball.posX - this.targetFireball.lastTickPosX) * (AntiFireballModule.minecraft.thePlayer.posX - this.targetFireball.posX) + (this.targetFireball.posY - this.targetFireball.lastTickPosY) * (AntiFireballModule.minecraft.thePlayer.posY + (double)AntiFireballModule.minecraft.thePlayer.getEyeHeight() - this.targetFireball.posY - (double)this.targetFireball.height / 2.0) + (this.targetFireball.posZ - this.targetFireball.lastTickPosZ) * (AntiFireballModule.minecraft.thePlayer.posZ - this.targetFireball.posZ);
                color = new Color(approachDotProduct < 0.0 ? 0xFF5555 : 0x55FF55);
                break;
            }
            case 2: {
                color = ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
                break;
            }
        }
        RenderUtils.beginOverlay();
        RenderUtils.fillEntityBounds((Entity)this.targetFireball, color.getRed(), color.getGreen(), color.getBlue(), 63, 0.1f);
        RenderUtils.endOverlay();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.candidateFireballs.clear();
        this.ignoredCloseFireballs.clear();
    }
}

