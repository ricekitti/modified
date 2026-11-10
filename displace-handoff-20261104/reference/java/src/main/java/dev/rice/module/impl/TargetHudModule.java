package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.client.combat.openb4.OpenB4KillAuraBackend;
import dev.rice.client.render.openb4.OpenB4TargetHudBackend;
import dev.rice.client.render.openb4.OpenB4TargetHudHost;
import dev.rice.client.render.openb4.RenderUtils;
import dev.rice.client.render.RenderPassScope;
import dev.rice.client.render.openb4.Render3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;

/**
 * Rice lifecycle/settings adapter for the generated OpenB4-u20 TargetHUD backend, with Rice AutoClicker/AimAssist target support.
 *
 * <p>OpenB4 timing as Rice hooks:
 * <ul>
 *   <li>RenderDispatcher.onRenderTick END runs KillAuraModule.onRenderTick, which draws the HUD. Here that is
 *       onRenderTick, calling the verbatim TargetHUD half, OpenB4KillAuraBackend.onRenderTickTargetHud().</li>
 *   <li>RenderWorldLastEvent runs onRenderWorld (Render ESP). Here that is event 6.</li>
 *   <li>The "Edit position" button opens the verbatim TargetHudEditor (OpenB4 guiButtonToggled).</li>
 * </ul>
 * OpenB4 keeps the HUD position (offsetX/offsetY) in its profile. Rice keeps it in two hidden settings, which are
 * saved with Rice profiles.
 */
public final class TargetHudModule extends Module {
   private final OpenB4TargetHudBackend backend;
   private final ModuleManager modules;
   private long assistedSince = -1L;
   private String assistedName = "";
   private float assistedHealth;
   private double assistedRatio;
   private OpenB4TargetHudBackend.Animation assistedFade;
   private final BooleanSetting editPosition;
   private final NumberSetting posX;
   private final NumberSetting posY;
   private double syncedX = Double.NaN;
   private double syncedY = Double.NaN;

   public TargetHudModule(ModuleManager modules) {
      super("targethud", "TargetHUD", "Renders the target of KillAura, AutoClicker or AimAssist.", ModuleCategory.RENDER);
      this.modules = modules;
      OpenB4TargetHudHost.bind("Mode", new OpenB4TargetHudHost.SliderSetting(this.add(new ChoiceSetting("mode", "Mode", 1, "Disabled", "Modern", "Legacy"))));
      OpenB4TargetHudHost.bind("Theme", new OpenB4TargetHudHost.SliderSetting(this.add(new ChoiceSetting("theme", "Theme", 0, "Default", "Rainbow", "Aurora",
         "Cherry", "Cotton candy", "Flare", "Flower", "Forest", "Frost", "Gold", "Grayscale", "Inferno", "Royal", "Sandstorm", "Sky", "Vine"))));
      this.editPosition = this.button("edit_position", "Edit position", false);
      this.button("render_esp", "Render ESP", false);
      this.button("show_difference", "Show difference", false);
      this.button("show_health", "Show health", true);
      this.button("show_win_loss", "Show win/loss", true);
      this.button("traditional_health_color", "Traditional health color", false);
      this.posX = this.add(new NumberSetting("pos_x", "Position X", 70.0, -4000.0, 4000.0, 1.0));
      this.posY = this.add(new NumberSetting("pos_y", "Position Y", 40.0, -4000.0, 4000.0, 1.0));
      this.backend = new OpenB4TargetHudBackend(0);
      this.backend.link();
      this.backend.guiUpdate();
      OpenB4TargetHudHost.onModified(this::storePosition);
      OpenB4TargetHudHost.guiKey(() -> {
         Module gui = modules.find("gui");
         return gui == null ? 0 : gui.getBinding();
      });
      this.syncPosition();
   }

   private BooleanSetting button(String key, String openB4Name, boolean defaultValue) {
      BooleanSetting setting = this.add(new BooleanSetting(key, openB4Name, defaultValue));
      OpenB4TargetHudHost.bind(openB4Name, new OpenB4TargetHudHost.ButtonSetting(setting));
      return setting;
   }

   @Override
   public int metadataCategory() {
      return 2;
   }

   @Override
   public boolean listens(int type) {
      return type == 6;
   }

   @Override
   public boolean settingVisible(String key) {
      return !key.equals("pos_x") && !key.equals("pos_y");
   }

   /** OpenB4 guiButtonToggled(editPosition) opens the position editor; Rice's button is momentary. */
   @Override
   public boolean afterSettingControl(String key, ClientContext context) {
      boolean handled = super.afterSettingControl(key, context);
      if ("edit_position".equals(key) && this.editPosition.isEnabled()) {
         this.editPosition.set(false);
         if (!context.ready()) {
            return true;
         }
         this.syncPosition();
         this.backend.guiUpdate();
         OpenB4TargetHudBackend.RenderUtils.updateResolution();
         this.backend.guiButtonToggled(OpenB4TargetHudBackend.editPosition);
         return true;
      }

      this.backend.guiUpdate();
      return handled;
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.syncPosition();
      this.backend.guiUpdate();
      OpenB4TargetHudHost.enable(this.backend);
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.clearAssisted();
      OpenB4TargetHudHost.disable();
      this.backend.onDisable();
   }

   /** Same target/text/theme renderer as gameplay; use the player when there is no combat target. */
   private String editorText() {
      this.syncPosition(); this.backend.guiUpdate();
      EntityLivingBase e = OpenB4KillAuraBackend.hasTarget() && OpenB4KillAuraBackend.target.living
            ? (EntityLivingBase)OpenB4KillAuraBackend.target.entity : this.modules.context().player();
      if (e == null) return OpenB4TargetHudBackend.text("Player", 20F, 1D);
      return OpenB4TargetHudBackend.text(e.getDisplayName().getUnformattedText(), e.getHealth(), Math.min(1D,e.getHealth()/Math.max(1F,e.getMaxHealth())));
   }

   public int[] editorBounds(int sw,int sh,net.minecraft.client.gui.FontRenderer font) {
      int w=font.getStringWidth(editorText())+8;
      return new int[]{sw/2-w/2+this.posX.intValue()-8,sh/2+this.posY.intValue()-8,w+8,font.FONT_HEIGHT+28};
   }

   public void renderEditor() {
      String text=editorText();
      EntityLivingBase player=this.modules.context().player();
      double ratio=player==null?1D:Math.min(1D,player.getHealth()/Math.max(1F,player.getMaxHealth()));
      if(OpenB4KillAuraBackend.hasTarget()&&OpenB4KillAuraBackend.target.living){EntityLivingBase e=(EntityLivingBase)OpenB4KillAuraBackend.target.entity;ratio=Math.min(1D,e.getHealth()/Math.max(1F,e.getMaxHealth()));}
      java.lang.reflect.Field[] fields=new java.lang.reflect.Field[4];Object[] previous=new Object[4];int saved=0;
      try {
         String[] names={"shown","target","from","changedAt"};
         for(int i=0;i<4;i++){fields[i]=dev.rice.inject.ReflectionHelper.findField(OpenB4TargetHudBackend.class,names[i]);previous[i]=fields[i].get(null);saved++;}
         fields[0].setDouble(null,ratio);fields[1].setDouble(null,ratio);fields[2].setDouble(null,ratio);fields[3].setLong(null,-1L);
         OpenB4TargetHudBackend.RenderUtils.updateResolution();
         try(RenderPassScope pass=RenderPassScope.hud(OpenB4TargetHudBackend.RenderUtils.scaledWidth,OpenB4TargetHudBackend.RenderUtils.scaledHeight)){
            OpenB4TargetHudBackend.mode.render(text,ratio);
         }
      }catch(IllegalAccessException error){throw new IllegalStateException(error);}
      finally{try{for(int i=0;i<saved;i++)fields[i].set(null,previous[i]);}catch(IllegalAccessException error){throw new IllegalStateException(error);}}
   }

   /** OpenB4 RenderDispatcher.onRenderTick END: KillAuraModule.onRenderTick() draws the HUD while TargetHUD is enabled. */
   @Override
   public void onRenderTick(ClientContext context) {
      if (dev.rice.client.render.HudLayout.editing()) return;
      if (!context.ready()) {
         this.clearAssisted();
         return;
      }

      this.syncPosition();
      this.backend.guiUpdate();
      OpenB4TargetHudBackend.RenderUtils.updateResolution();
      RenderUtils.updateResolution();
      try (RenderPassScope pass = RenderPassScope.hud(OpenB4TargetHudBackend.RenderUtils.scaledWidth, OpenB4TargetHudBackend.RenderUtils.scaledHeight)) {
         if (OpenB4KillAuraBackend.hasTarget()) {
            this.clearAssisted();
            OpenB4KillAuraBackend.onRenderTickTargetHud();
         } else if (!this.renderAssisted(context)) {
            OpenB4KillAuraBackend.onRenderTickTargetHud();
         }
      }
   }

   /** OpenB4 RenderWorldLastEvent -> onRenderWorld (Render ESP box around the KillAura target). */
   @Override
   public void onEvent(ClientContext context, SemanticEvent event) {
      if (event.type == 6 && context.ready()) {
         this.backend.onRenderWorld(new OpenB4TargetHudHost.RenderWorldLastEvent(event.timerRenderPartialTicks));
         EntityLivingBase target = this.assistedTarget(context);
         if (!OpenB4KillAuraBackend.hasTarget() && target != null && OpenB4TargetHudBackend.renderEsp.isToggled()
            && OpenB4TargetHudHost.BacktrackModule.ghost == null) {
            Render3D.entityBox(target, 2, OpenB4TargetHudBackend.theme.getRGB(0.0), event.timerRenderPartialTicks);
         }
      }
   }

   /** Read the modules' actual selections without taking ownership of combat/rotation state. */
   private EntityLivingBase assistedTarget(ClientContext context) {
      if (!context.ready() || context.minecraft().currentScreen != null || !context.minecraft().inGameHasFocus) {
         return null;
      }
      Module clicker = this.modules.find("autoclicker");
      MovingObjectPosition hit = context.minecraft().objectMouseOver;
      if (clicker != null && clicker.isEnabled() && hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
         && this.validTarget(context, hit.entityHit)) {
         return (EntityLivingBase) hit.entityHit;
      }
      Module aim = this.modules.find("aimassist");
      if (aim instanceof AimAssistModule && aim.isEnabled() && ((AimAssistModule) aim).targetActive()
         && this.validTarget(context, ((AimAssistModule) aim).currentTarget())) {
         return ((AimAssistModule) aim).currentTarget();
      }
      return null;
   }

   private boolean validTarget(ClientContext context, Entity entity) {
      return entity instanceof EntityLivingBase && entity != context.player() && !entity.isDead
         && ((EntityLivingBase) entity).getHealth() > 0.0F && context.world().loadedEntityList.contains(entity)
         && (!(entity instanceof EntityPlayer) || RelationshipService.hostile((EntityPlayer) entity));
   }

   /** Same 250 ms hold / 400 ms fade as KillAura, without writing KillAura's target. */
   private boolean renderAssisted(ClientContext context) {
      EntityLivingBase target = this.assistedTarget(context);
      long now = System.currentTimeMillis();
      if (target != null) {
         this.assistedSince = now;
         this.assistedFade = null;
         this.assistedName = target.getDisplayName().getUnformattedText();
         this.assistedHealth = target.getHealth();
         this.assistedRatio = Math.min(this.assistedHealth / Math.max(target.getMaxHealth(), 1.0F), 1.0F);
      }
      if (this.assistedSince == -1L) {
         return false;
      }
      long age = now - this.assistedSince;
      if (age >= 650L) {
         this.clearAssisted();
         return false;
      }
      if (age >= 250L && this.assistedFade == null) {
         this.assistedFade = new OpenB4TargetHudBackend.Animation(400L);
         this.assistedFade.start();
      }
      if (OpenB4TargetHudBackend.mode != OpenB4TargetHudBackend.Mode.Disabled && OpenB4TargetHudBackend.Overlay.canRender()) {
         OpenB4TargetHudBackend.render(this.assistedFade, this.assistedName, this.assistedHealth, this.assistedRatio);
      }
      return true;
   }

   private void clearAssisted() {
      this.assistedSince = -1L;
      this.assistedFade = null;
      this.assistedName = "";
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      this.clearAssisted();
   }

   /** Settings -> OpenB4 offsets, when the stored position changed (profile load); the editor's live drag is kept. */
   private void syncPosition() {
      if (this.posX.doubleValue() != this.syncedX || this.posY.doubleValue() != this.syncedY) {
         this.syncedX = this.posX.doubleValue();
         this.syncedY = this.posY.doubleValue();
         OpenB4TargetHudBackend.offsetX = (int) this.syncedX;
         OpenB4TargetHudBackend.offsetY = (int) this.syncedY;
      }
   }

   /** OpenB4 Profiles.markModified() from the editor: the dragged position becomes the stored one. */
   private void storePosition() {
      this.syncedX = OpenB4TargetHudBackend.offsetX;
      this.syncedY = OpenB4TargetHudBackend.offsetY;
      this.posX.set(this.syncedX);
      this.posY.set(this.syncedY);
      this.modules.persist();
   }
}
