/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.module.impl.client.ChatCommands
 *  valen.module.impl.client.Relationships
 *  valen.module.impl.combat.Reduce
 *  valen.module.impl.combat.Velocity
 *  valen.module.impl.minigames.MurderMystery
 *  valen.module.impl.movement.BHop
 *  valen.module.impl.movement.Fly
 *  valen.module.impl.movement.KeepSprint
 *  valen.module.impl.movement.NoSlow
 *  valen.module.impl.other.NameHider
 *  valen.module.impl.player.SafeWalk
 *  valen.module.impl.render.AntiShuffle
 *  valen.module.impl.render.ExtendCamera
 *  valen.module.impl.render.NoCameraClip
 *  valen.module.impl.world.Weather
 */
package valen.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import valen.lunar.forge.common.MinecraftForge;
import valen.module.Module;
import valen.module.impl.ambiance.Antifullbright;
import valen.module.impl.ambiance.Timechanger;
import valen.module.impl.client.ChatCommands;
import valen.module.impl.client.CommandLine;
import valen.module.impl.client.Friends;
import valen.module.impl.client.Gui;
import valen.module.impl.client.Relationships;
import valen.module.impl.combat.AimAssist;
import valen.module.impl.combat.AntiKnockback;
import valen.module.impl.combat.AutoClicker;
import valen.module.impl.combat.AutoWeapon;
import valen.module.impl.combat.Autoblock;
import valen.module.impl.combat.Backtrack;
import valen.module.impl.combat.Criticals;
import valen.module.impl.combat.Displace;
import valen.module.impl.combat.HitSelect;
import valen.module.impl.combat.JumpReset;
import valen.module.impl.combat.KnockbackDelay;
import valen.module.impl.combat.LagRange;
import valen.module.impl.combat.RawInput;
import valen.module.impl.combat.Reach;
import valen.module.impl.combat.Reduce;
import valen.module.impl.combat.SprintReset;
import valen.module.impl.combat.TimerRange;
import valen.module.impl.combat.Velocity;
import valen.module.impl.combat.Weapons;
import valen.module.impl.minigames.AutoQuickBuy;
import valen.module.impl.minigames.Info;
import valen.module.impl.minigames.MurderMystery;
import valen.module.impl.movement.BHop;
import valen.module.impl.movement.FireballBoost;
import valen.module.impl.movement.Fly;
import valen.module.impl.movement.InstantStop;
import valen.module.impl.movement.InvMove;
import valen.module.impl.movement.KeepSprint;
import valen.module.impl.movement.LongJump;
import valen.module.impl.movement.MovementFix;
import valen.module.impl.movement.NoSlow;
import valen.module.impl.movement.NullMove;
import valen.module.impl.movement.Sprint;
import valen.module.impl.movement.Stasis;
import valen.module.impl.movement.Timer;
import valen.module.impl.other.NameHider;
import valen.module.impl.other.Teams;
import valen.module.impl.player.AntiFireball;
import valen.module.impl.player.AntiPumpkin;
import valen.module.impl.player.AutoPearl;
import valen.module.impl.player.AutoSwap;
import valen.module.impl.player.AutoTool;
import valen.module.impl.player.BedAssist;
import valen.module.impl.player.BedBoost;
import valen.module.impl.player.BedMiner;
import valen.module.impl.player.BedNuker;
import valen.module.impl.player.Blink;
import valen.module.impl.player.BlockIn;
import valen.module.impl.player.BridgeEgg;
import valen.module.impl.player.Clutch;
import valen.module.impl.player.DelayRemover;
import valen.module.impl.player.Eagle;
import valen.module.impl.player.FastMine;
import valen.module.impl.player.FastPlace;
import valen.module.impl.player.FireballPearlClutch;
import valen.module.impl.player.GhostHand;
import valen.module.impl.player.InvManager;
import valen.module.impl.player.NoRotate;
import valen.module.impl.player.PrePlace;
import valen.module.impl.player.SafeWalk;
import valen.module.impl.render.AntiDebuff;
import valen.module.impl.render.AntiShuffle;
import valen.module.impl.render.BedESP;
import valen.module.impl.render.BlockESP;
import valen.module.impl.render.BlockOverlay;
import valen.module.impl.render.BreakProgress;
import valen.module.impl.render.Chams;
import valen.module.impl.render.ChestESP;
import valen.module.impl.render.DamageTags;
import valen.module.impl.render.DamageTint;
import valen.module.impl.render.ExtendCamera;
import valen.module.impl.render.Freelook;
import valen.module.impl.render.HUD;
import valen.module.impl.render.Indicators;
import valen.module.impl.render.ItemESP;
import valen.module.impl.render.MobESP;
import valen.module.impl.render.Nametags;
import valen.module.impl.render.NoCameraClip;
import valen.module.impl.render.NoHurtCam;
import valen.module.impl.render.Notifications;
import valen.module.impl.render.PlayerESP;
import valen.module.impl.render.Pointers;
import valen.module.impl.render.Resources;
import valen.module.impl.render.TargetHUD;
import valen.module.impl.render.Trajectories;
import valen.module.impl.render.Tunnels;
import valen.module.impl.render.VoidBeam;
import valen.module.impl.world.AntiBot;
import valen.module.impl.world.Weather;
import valen.utility.font.ValenFontRenderer;

public class ModuleManager {
    public static List<Module> modules = new ArrayList<Module>();
    public static List<Module> organizedModules = Collections.synchronizedList(new ArrayList());
    private static final Map<String, Module> modulesByName = new HashMap<String, Module>();
    private static final Map<String, Module> modulesByNormalizedName = new HashMap<String, Module>();
    private static final Map<Class<?>, Module> modulesByClass = new HashMap();
    public static Timechanger timechanger;
    public static Antifullbright antifullbright;
    public static NameHider nameHider;
    public static FastPlace fastPlace;
    public static MurderMystery murderMystery;
    public static InvMove invmove;
    public static AntiFireball antiFireball;
    public static AntiPumpkin antiPumpkin;
    public static BedAssist bedAssist;
    public static BedBoost bedBoost;
    public static BedNuker bedNuker;
    public static FastMine fastMine;
    public static AntiShuffle antiShuffle;
    public static MovementFix movementFix;
    public static CommandLine commandLine;
    public static LongJump longJump;
    public static AntiBot antiBot;
    public static NoSlow noSlow;
    public static AutoClicker autoClicker;
    public static HitSelect hitSelect;
    public static KnockbackDelay knockbackDelay;
    public static Reach reach;
    public static Weapons weapons;
    public static NoRotate noRotate;
    public static BlockESP blockESP;
    public static BedESP bedESP;
    public static Blink blink;
    public static Chams chams;
    public static ChestESP chestESP;
    public static HUD hud;
    public static Fly fly;
    public static SprintReset sprintReset;
    public static Velocity velocity;
    public static AntiDebuff antiDebuff;
    public static TargetHUD targetHUD;
    public static PlayerESP playerESP;
    public static MobESP mobESP;
    public static Reduce reduce;
    public static SafeWalk safeWalk;
    public static KeepSprint keepSprint;
    public static GhostHand ghostHand;
    public static InvManager invManager;
    public static AntiKnockback antiKnockback;
    public static ExtendCamera extendCamera;
    public static Freelook freelook;
    public static NoCameraClip noCameraClip;
    public static BHop bHop;
    public static NoHurtCam noHurtCam;
    public static Notifications notifications;
    public static AutoTool autoTool;
    public static AutoPearl autoPearl;
    public static FireballPearlClutch fireballPearlClutch;
    public static BridgeEgg bridgeEgg;
    public static AutoWeapon autoWeapon;
    public static Clutch clutch;
    public static AutoSwap autoSwap;
    public static Sprint sprint;
    public static Weather weather;
    public static ChatCommands chatCommands;
    public static Relationships relationships;
    public static Friends friends;
    public static Teams teams;
    public static Displace displace;
    public static Tunnels tunnels;

    public void register() {
        this.addModule(new Gui());
        this.addModule(new Resources());
        friends = new Friends();
        this.addModule(friends);
        teams = new Teams();
        this.addModule(teams);
        timechanger = new Timechanger();
        this.addModule(timechanger);
        antifullbright = new Antifullbright();
        this.addModule(antifullbright);
        this.addModule(new AimAssist());
        autoClicker = new AutoClicker();
        this.addModule(autoClicker);
        autoWeapon = new AutoWeapon();
        this.addModule(autoWeapon);
        this.addModule(new Autoblock());
        this.addModule(new Backtrack());
        this.addModule(new Criticals());
        displace = new Displace();
        this.addModule(displace);
        hitSelect = new HitSelect();
        this.addModule(hitSelect);
        this.addModule(new JumpReset());
        knockbackDelay = new KnockbackDelay();
        this.addModule(knockbackDelay);
        this.addModule(new LagRange());
        this.addModule(new RawInput());
        reach = new Reach();
        this.addModule(reach);
        sprintReset = new SprintReset();
        this.addModule(sprintReset);
        this.addModule(new TimerRange());
        weapons = new Weapons();
        this.addModule(weapons);
        movementFix = new MovementFix();
        this.addModule(movementFix);
        this.addModule(new FireballBoost());
        this.addModule(new InstantStop());
        invmove = new InvMove();
        this.addModule(invmove);
        this.addModule(new NullMove());
        sprint = new Sprint();
        this.addModule(sprint);
        this.addModule(new Stasis());
        this.addModule(new Timer());
        antiFireball = new AntiFireball();
        this.addModule(antiFireball);
        antiPumpkin = new AntiPumpkin();
        this.addModule(antiPumpkin);
        autoPearl = new AutoPearl();
        this.addModule(autoPearl);
        fireballPearlClutch = new FireballPearlClutch();
        this.addModule(fireballPearlClutch);
        autoSwap = new AutoSwap();
        this.addModule(autoSwap);
        antiBot = new AntiBot();
        this.addModule(antiBot);
        this.addModule(new AutoQuickBuy());
        this.addModule(new BlockIn());
        this.addModule(new Info());
        this.addModule(new Eagle());
        autoTool = new AutoTool();
        this.addModule(autoTool);
        bedAssist = new BedAssist();
        this.addModule(bedAssist);
        bedBoost = new BedBoost();
        this.addModule(bedBoost);
        bedNuker = new BedNuker();
        this.addModule(bedNuker);
        clutch = new Clutch();
        this.addModule(clutch);
        bridgeEgg = new BridgeEgg();
        this.addModule(bridgeEgg);
        blink = new Blink();
        this.addModule(blink);
        this.addModule(new DelayRemover());
        fastMine = new FastMine();
        this.addModule(fastMine);
        fastPlace = new FastPlace();
        this.addModule(fastPlace);
        ghostHand = new GhostHand();
        this.addModule(ghostHand);
        invManager = new InvManager();
        this.addModule(invManager);
        noRotate = new NoRotate();
        this.addModule(noRotate);
        this.addModule(new PrePlace());
        antiDebuff = new AntiDebuff();
        this.addModule(antiDebuff);
        bedESP = new BedESP();
        this.addModule(bedESP);
        blockESP = new BlockESP();
        this.addModule(blockESP);
        this.addModule(new BlockOverlay());
        this.addModule(new BreakProgress());
        chams = new Chams();
        this.addModule(chams);
        chestESP = new ChestESP();
        this.addModule(chestESP);
        this.addModule(new DamageTags());
        this.addModule(new DamageTint());
        freelook = new Freelook();
        this.addModule(freelook);
        hud = new HUD();
        this.addModule(hud);
        this.addModule(new Indicators());
        this.addModule(new ItemESP());
        this.addModule(new Nametags());
        noHurtCam = new NoHurtCam();
        this.addModule(noHurtCam);
        notifications = new Notifications();
        this.addModule(notifications);
        playerESP = new PlayerESP();
        this.addModule(playerESP);
        this.addModule(new Pointers());
        targetHUD = new TargetHUD();
        this.addModule(targetHUD);
        this.addModule(new Trajectories());
        tunnels = new Tunnels();
        this.addModule(tunnels);
        this.addModule(new VoidBeam());
        notifications.enable();
        teams.enable();
        if (movementFix != null) {
            movementFix.enable();
        }
        if (antiBot != null) {
            antiBot.enable();
        }
        for (Module module : modules) {
            if (!module.alwaysOn) continue;
            MinecraftForge.EVENT_BUS.register(module);
        }
        modules.sort(Comparator.comparing(Module::getName));
    }

    public void addModule(Module module) {
        modules.add(module);
        modulesByName.put(module.getName(), module);
        modulesByNormalizedName.put(ModuleManager.normalizeModuleName(module.getName()), module);
        modulesByClass.put(module.getClass(), module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> inCategory(Module.category category2) {
        ArrayList<Module> arrayList = new ArrayList<Module>();
        for (Module module : this.getModules()) {
            if (!module.moduleCategory().equals((Object)category2)) continue;
            arrayList.add(module);
        }
        return arrayList;
    }

    public static Module getModule(String string) {
        Module module = modulesByName.get(string);
        if (module != null) {
            return module;
        }
        return modulesByNormalizedName.get(ModuleManager.normalizeModuleName(string));
    }

    public static Module getModule(Class<?> clazz) {
        return modulesByClass.get(clazz);
    }

    public static BedMiner getActiveBedMiner() {
        if (bedAssist != null && bedAssist.isActivelyMining()) {
            return bedAssist;
        }
        if (bedNuker != null && bedNuker.isActivelyMining()) {
            return bedNuker;
        }
        return null;
    }

    public static boolean isBedMining() {
        return ModuleManager.getActiveBedMiner() != null;
    }

    public static void sort() {
        ValenFontRenderer valenFontRenderer = HUD.getHudFontRenderer();
        organizedModules.sort((module, module2) -> valenFontRenderer.getStringWidth(HUD.getHudRenderText(module2)) - valenFontRenderer.getStringWidth(HUD.getHudRenderText(module)));
    }

    private static String normalizeModuleName(String string) {
        if (string == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (!Character.isLetterOrDigit(c)) continue;
            stringBuilder.append(Character.toLowerCase(c));
        }
        return stringBuilder.toString();
    }
}

