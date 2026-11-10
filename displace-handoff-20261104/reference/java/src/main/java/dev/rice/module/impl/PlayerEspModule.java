package dev.rice.module.impl;
import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.render.openb4.*;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
/** OpenB4 PlayerESP backend, with Rice lifecycle/settings adapters. */
public final class PlayerEspModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public enum Type {
        None(0), Outline(-1), Outline_Light_Glow(-2), Outline_Heavy_Glow(-3), Box(1), Shaded(2), _2D(3), Ring(6);
        public final int mode;
        Type(int mode) { this.mode = mode; }
    }
    public static ChoiceSetting typeSetting;
    public static NumberSetting red, green, blue;
    public static BooleanSetting disableInLobby, rainbow, healthBar, redOnDamage, renderSelf, teamColor, showInvis;
    public static Type type = Type.Outline;
    public static int color = 0xFF00FFFF;
    public static boolean shaderPass;
    private static Framebuffer framebuffer;

    public PlayerEspModule() {
        super("player_esp", "Player ESP", "PlayerESP rendering, shaders and options", ModuleCategory.RENDER);
        typeSetting = add(new ChoiceSetting("type", "Type", 1, "None", "Outline", "Outline (Light Glow)", "Outline (Heavy Glow)", "Box", "Shaded", "2D", "Ring"));
        red = add(new NumberSetting("red", "Red", 0, 0, 255, 1));
        green = add(new NumberSetting("green", "Green", 255, 0, 255, 1));
        blue = add(new NumberSetting("blue", "Blue", 255, 0, 255, 1));
        disableInLobby = add(new BooleanSetting("disable_in_lobby", "Disable in lobby", true));
        rainbow = add(new BooleanSetting("rainbow", "Rainbow", false));
        healthBar = add(new BooleanSetting("health_bar", "Health bar", true));
        redOnDamage = add(new BooleanSetting("red_on_damage", "Red on damage", false));
        renderSelf = add(new BooleanSetting("render_self", "Render self", false));
        teamColor = add(new BooleanSetting("team_color", "Team color", false));
        showInvis = add(new BooleanSetting("show_invis", "Show invis", true));
        for (Setting<?> setting : getSettings()) setting.parseAndSet(setting.format());
    }

    @Override public boolean listens(int event) { return event == 6; }

    @Override public String statusText() { return typeSetting.selectedLabel(); }

    @Override public void onEvent(ClientContext context, SemanticEvent event) {
        if (!context.ready() || shaderPass || (disableInLobby.isEnabled() && B4EspHost.inLobby())) return;
        type = Type.values()[typeSetting.index()];
        color = new Color(red.intValue(), green.intValue(), blue.intValue()).getRGB();
        RenderUtils.updateResolution();
        Render3D.updateFrustum(event.timerRenderPartialTicks);
        // Restore state that OpenB4's direct geometry calls change, including shader programs.
        int oldProgram = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM);
        dev.rice.client.hook.GlStateScope scope = dev.rice.client.hook.GlStateScope.open();
        try {
            if (isShader()) renderShader(event.timerRenderPartialTicks);
            for (EntityPlayer player : context.world().playerEntities) {
                if (player.deathTime != 0 || !shouldRender(player)) continue;
                if (player == context.player()) {
                    if (mc.gameSettings.thirdPersonView == 0 || player.isPlayerSleeping()) continue;
                } else if ((!showInvis.isEnabled() && player.isInvisible()) || !Render3D.isVisible(player)) continue;
                render(player, event.timerRenderPartialTicks);
            }
        } finally {
            org.lwjgl.opengl.GL20.glUseProgram(oldProgram);
            scope.close();
        }
    }

    @Override public void onWorldUnload(ClientContext context) {
        shaderPass = false;
        if (framebuffer != null && org.lwjgl.opengl.Display.isCreated()) {
            framebuffer.deleteFramebuffer(); framebuffer = null;
        }
    }
    public static boolean shouldRender(Entity e) {
        return !(!renderSelf.isEnabled() && e == mc.thePlayer || !B4EspHost.allowed(e));
    }

    public static int color(EntityLivingBase e, float pt) {
        int n = teamColor.isEnabled() && e instanceof EntityPlayer ? teamColor(e) : (rainbow.isEnabled() ? 0 : color);
        if (redOnDamage.isEnabled() && e.hurtTime > 1) {
            if (n == 0) {
                n = RenderUtils.rainbow(2L, 0L);
            }
            float f2 = (float) e.hurtTime - pt;
            double d = Math.max(0.0, Math.min(1.0, (double) (f2 - 1.0f) / 7.0));
            n = RenderUtils.blendInt(new Color(241, 39, 17).getRGB(), n, d);
        }
        return n;
    }

    public static int teamColor(EntityLivingBase e) {
        String s;
        ScorePlayerTeam team;
        int n = 0xFFFFFF;
        if (e instanceof EntityPlayer && (team = (ScorePlayerTeam) e.getTeam()) != null && (s = FontRenderer.getFormatFromString(team.getColorPrefix())).length() >= 2) {
            n = mc.fontRendererObj.getColorCode(s.charAt(1));
        }
        return 0xFF000000 | n & 0xFFFFFF;
    }

    public static boolean isShader() {
        return type.mode < 0;
    }

    public static void render(EntityLivingBase e, float pt) {
        int n = color(e, pt);
        if (healthBar.isEnabled()) {
            Render3D.entityBox(e, 4, n, pt);
        }
        if (type != Type.None && !isShader()) {
            Render3D.entityBox(e, type.mode, n, pt);
        }
    }

    private static Framebuffer framebuffer() {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
            framebuffer.setFramebufferFilter(9728);
        }
        return framebuffer;
    }

    public static void renderShader(float pt) {
        if (!EspShaders.valid(EspShaders.outline()) || !EspShaders.valid(EspShaders.fill())) {
            return;
        }
        Framebuffer fb = framebuffer();
        RenderManager rm = mc.getRenderManager();
        boolean shadow = rm.isRenderShadow();
        boolean drew = false;
        int mode = GL11.glGetInteger(2976);
        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        try {
            fb.framebufferClear();
            fb.bindFramebuffer(false);
            Render3D.setupCamera(pt);
            rm.setRenderShadow(false);
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.001f);
            GlStateManager.depthMask(false);
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            shaderPass = true;
            for (EntityPlayer p : mc.theWorld.playerEntities) {
                if (p.deathTime != 0 || p == mc.thePlayer && (!renderSelf.isEnabled() || mc.gameSettings.thirdPersonView == 0 || p.isPlayerSleeping())
                        || !showInvis.isEnabled() && p.isInvisible() || !shouldRender(p)) continue;
                int n = color(p, pt);
                if (n == 0) {
                    n = RenderUtils.rainbow(2L, 0L);
                }
                EspShaders.useFill(n);
                rm.renderEntityStatic(p, pt, true);
                EspShaders.fill().stop();
                drew = true;
            }
            shaderPass = false;
            mc.entityRenderer.disableLightmap();
            mc.getFramebuffer().bindFramebuffer(false);
            Render3D.setupOverlay();
            if (drew) {
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                GlStateManager.disableLighting();
                GlStateManager.disableCull();
                GlStateManager.enableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                if (type != Type.Outline) {
                    GlowShader.get().render(fb, 2, type == Type.Outline_Light_Glow ? 1.5f : 6.5f);
                }
                EspShaders.outline(fb, 2.0f, 0.1f);
            }
        } finally {
            EspShaders.fill().stop();
            shaderPass = false;
            rm.setRenderShadow(shadow);
            mc.getFramebuffer().bindFramebuffer(false);
            GL11.glMatrixMode(5888);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5889);
            GL11.glPopMatrix();
            GL11.glMatrixMode(mode);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1f);
            GlStateManager.disableBlend();
        }
    }
}
