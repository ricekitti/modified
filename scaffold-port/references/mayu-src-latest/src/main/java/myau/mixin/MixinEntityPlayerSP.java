package myau.mixin;

import myau.AntiDebuffModule;
import myau.KillAuraModule;
import myau.LivingUpdateEvent;
import myau.MovementInputEvent;
import myau.MyauClient;
import myau.NoSlowModule;
import myau.PostPlayerUpdateEvent;
import myau.PreWalkingUpdateEvent;
import myau.RotationState;
import myau.RotationUpdateEvent;
import myau.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityPlayerSP.class})
public abstract class MixinEntityPlayerSP
extends MixinEntityPlayer {
    @Unique
    private float spoofedYaw = Float.NaN;
    @Unique
    private float spoofedPitch = Float.NaN;
    @Unique
    private float originalYaw;
    @Unique
    private float originalPitch;
    @Shadow
    private float field_175164_bL;
    @Shadow
    private float field_175165_bM;
    @Shadow
    public float field_71154_f;
    @Shadow
    public float field_71163_h;

    @Inject(method={"onUpdate"}, at={@At(value="HEAD")}, remap=false)
    private void beforePlayerUpdate(CallbackInfo callbackInfo) {
        EntityPlayerSP player = (EntityPlayerSP)((Object)this);
        if (player.worldObj.isBlockLoaded(new BlockPos(player.posX, 0.0, player.posZ)) && MyauClient.eventBus != null) {
            RotationUpdateEvent event = new RotationUpdateEvent(this.field_175164_bL, this.field_175165_bM, player.rotationYaw, player.rotationPitch);
            MyauClient.eventBus.post(event);
            RotationState.update(event.isModified(), event.getYaw(), event.getPitch(), event.getMovementYaw(), event.getMovementPriority());
            if (event.isModified()) {
                this.originalYaw = player.rotationYaw;
                this.originalPitch = player.rotationPitch;
                this.spoofedYaw = event.getYaw();
                this.spoofedPitch = event.getPitch();
            } else {
                this.originalYaw = Float.NaN;
                this.originalPitch = Float.NaN;
                this.spoofedYaw = Float.NaN;
                this.spoofedPitch = Float.NaN;
            }
        }
    }

    @Inject(method={"onUpdate"}, at={@At(value="RETURN")}, remap=false)
    private void afterPlayerUpdate(CallbackInfo callbackInfo) {
        EntityPlayerSP player = (EntityPlayerSP)((Object)this);
        if (player.worldObj.isBlockLoaded(new BlockPos(player.posX, 0.0, player.posZ))) {
            if (!Float.isNaN(this.originalYaw) && !Float.isNaN(this.originalPitch)) {
                this.field_175164_bL = player.rotationYaw;
                this.field_175165_bM = player.rotationPitch;
                player.rotationYaw = RotationUtils.nearestEquivalentAngle(this.originalYaw, player.rotationYaw);
                player.rotationPitch = this.originalPitch;
                player.prevRotationYaw = player.rotationYaw;
                player.prevRotationPitch = player.rotationPitch;
                this.field_71163_h = player.rotationYaw - (this.field_71154_f - this.field_71163_h) * 2.0f;
                this.field_71154_f = player.rotationYaw;
            }
            if (MyauClient.eventBus != null) {
                MyauClient.eventBus.post(new PostPlayerUpdateEvent());
            }
        }
    }

    @Redirect(method={"onUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isRiding()Z"), remap=false)
    private boolean applySpoofedRotationBeforeRidingCheck(EntityPlayerSP player) {
        if (!Float.isNaN(this.spoofedYaw) && !Float.isNaN(this.spoofedPitch)) {
            player.rotationYaw = this.spoofedYaw;
            player.rotationPitch = this.spoofedPitch;
        }
        return player.isRiding();
    }

    @Inject(method={"onUpdate"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V")}, remap=false)
    private void beforeWalkingPlayerUpdate(CallbackInfo callbackInfo) {
        if (MyauClient.eventBus != null) {
            MyauClient.eventBus.post(new PreWalkingUpdateEvent());
        }
    }

    @Inject(method={"onLivingUpdate"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/entity/AbstractClientPlayer;onLivingUpdate()V")}, remap=false)
    private void beforeLivingUpdate(CallbackInfo callbackInfo) {
        if (MyauClient.eventBus != null) {
            MyauClient.eventBus.post(new LivingUpdateEvent());
        }
    }

    @Inject(method={"onLivingUpdate"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V", shift=At.Shift.AFTER)}, remap=false)
    private void afterMovementInputUpdate(CallbackInfo callbackInfo) {
        if (MyauClient.eventBus != null) {
            MyauClient.eventBus.post(new MovementInputEvent());
        }
    }

    @Redirect(method={"onLivingUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"), remap=false)
    private boolean redirectUsingItemForNoSlow(EntityPlayerSP player) {
        KillAuraModule killAura = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
        if (killAura.isAutoBlockActive() && ((Boolean)killAura.autoBlockNoSlow.getValue()).booleanValue()) {
            return false;
        }
        NoSlowModule noSlow = (NoSlowModule)MyauClient.moduleManager.modules.get(NoSlowModule.class);
        return noSlow.isEnabled() && noSlow.shouldSuppressItemSlowdown() ? false : player.isUsingItem();
    }

    @Redirect(method={"onLivingUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"), remap=false)
    private boolean redirectNauseaCheck(EntityPlayerSP player, Potion potion) {
        AntiDebuffModule antiDebuff;
        if (potion == Potion.confusion && MyauClient.moduleManager != null && (antiDebuff = (AntiDebuffModule)MyauClient.moduleManager.modules.get(AntiDebuffModule.class)).isEnabled() && ((Boolean)antiDebuff.nausea.getValue()).booleanValue()) {
            return false;
        }
        return ((AccessorEntityLivingBase)player).getActivePotionsMap().containsKey(potion.id);
    }
}


