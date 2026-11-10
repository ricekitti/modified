import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.client.rotation.RotationService;
import dev.rice.config.InitialSettings;
import dev.rice.module.Module;
import dev.rice.module.impl.TeamsModule;
import dev.rice.module.impl.WeaponsModule;
import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.ClassWriter;
import dev.rice.shaded.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.*;
import net.minecraft.util.*;

/** Actual Teams flags and Aim Assist acquisition/render checks; never launches the game. */
public final class TeamsAimAssistCheck extends OpenB4KillAuraCheck {
    public static boolean isButtonDown(int button) { return false; }

    static IChatComponent styled(String name, EnumChatFormatting color) {
        return new ChatComponentText("")
            .appendSibling(new ChatComponentText("[MVP] ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)))
            .appendSibling(new ChatComponentText(name).setChatStyle(new ChatStyle().setColor(color)));
    }

    static void refresh(TeamsModule teams, RavenBsCombatCheck.Fixture f) {
        teams.onEvent(f.c, new SemanticEvent(0));
    }

    static Module aim(final Path classes) throws Exception {
        ClassLoader loader = new ClassLoader(TeamsAimAssistCheck.class.getClassLoader()) {
            @Override protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.startsWith("dev.rice.module.impl.AimAssistModule")) return super.loadClass(name, resolve);
                Class<?> existing = findLoadedClass(name);
                if (existing != null) return existing;
                try {
                    ClassNode node = new ClassNode();
                    new ClassReader(Files.readAllBytes(classes.resolve(name.replace('.', '/') + ".class"))).accept(node, 0);
                    for (MethodNode method : node.methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
                        if (insn instanceof MethodInsnNode) {
                            MethodInsnNode call = (MethodInsnNode)insn;
                            if (call.owner.equals("org/lwjgl/input/Mouse")) call.owner = "TeamsAimAssistCheck";
                        }
                    }
                    ClassWriter writer = new ClassWriter(0);
                    node.accept(writer);
                    byte[] data = writer.toByteArray();
                    return defineClass(name, data, 0, data.length);
                } catch (Exception e) { throw new ClassNotFoundException(name, e); }
            }
        };
        return (Module)loader.loadClass("dev.rice.module.impl.AimAssistModule").getConstructor(WeaponsModule.class).newInstance(new WeaponsModule());
    }

    public static void main(String[] args) throws Exception { run(Paths.get(args[0])); }

    public static void run(Path classes) throws Exception {
        check(TeamsModule.nameColor("\u00a76[MVP] \u00a7B\u00a7lAlice\u00a7r", "Alice") == 'b', "rank/reset replaced the username colour");
        check(TeamsModule.nameColor("\u00a7bAl\u00a7bice\u00a7r", "Alice") == 'b', "component-split username lost its colour");
        check(TeamsModule.nameColor("Alice", "Alice") == 0 && TeamsModule.nameColor("\u00a7rAlice", "Alice") == 0, "missing/reset colour became a team");
        RavenBsCombatCheck.Fixture f = new RavenBsCombatCheck.Fixture(classes, false, false);
        f.manager.applyNow(f.aura, false);
        put(f.self, "gameProfile", new com.mojang.authlib.GameProfile(f.self.getUniqueID(), "Owner"));
        Scoreboard board = new Scoreboard();
        put(f.world, "worldScoreboard", board);
        Map<UUID, NetworkPlayerInfo> infos = new HashMap<>();
        put(f.handler, "playerInfoMap", infos);
        NetworkPlayerInfo own = new NetworkPlayerInfo(f.self.getGameProfile());
        NetworkPlayerInfo friend = new NetworkPlayerInfo(f.enemy.getGameProfile());
        RavenBsCombatCheck.Remote opponent = f.remote(103, 0, 2.0);
        f.world.loadedEntityList.add(opponent); f.world.playerEntities.add(opponent);
        NetworkPlayerInfo enemy = new NetworkPlayerInfo(opponent.getGameProfile());
        infos.put(f.self.getUniqueID(), own); infos.put(f.enemy.getUniqueID(), friend); infos.put(opponent.getUniqueID(), enemy);
        own.setDisplayName(styled(f.self.getName(), EnumChatFormatting.AQUA));
        friend.setDisplayName(styled(f.enemy.getName(), EnumChatFormatting.AQUA));
        enemy.setDisplayName(styled(opponent.getName(), EnumChatFormatting.RED));
        TeamsModule teams = new TeamsModule(); Module aim = aim(classes);
        f.manager.register(teams); f.manager.register(aim); InitialSettings.applyMissing(f.manager);
        flag(teams, "match_self_color", true);
        f.manager.applyNow(teams, true); f.manager.applyNow(aim, true);
        for (String key : new String[]{"require_sprint", "require_mouse_pressed", "require_mouse_moved", "require_weapon", "disable_on_block_break", "require_line_of_sight", "ignore_invis"}) flag(aim, key, false);
        num(aim, "predict", 0); f.self.rotationYaw = f.self.rotationPitch = 0;
        RotationService.reset(f.c);
        refresh(teams, f);
        check((RelationshipService.flags(f.enemy) & 1) != 0 && (RelationshipService.flags(opponent) & 1) == 0, "styled tab-list teammates were not marked consistently");
        aim.onEvent(f.c, new SemanticEvent(18));
        check(aim.getClass().getMethod("currentTarget").invoke(aim) == opponent, "Aim Assist acquired the styled teammate instead of the enemy");
        RelationshipService.set(opponent, 1, true);
        float yaw = f.self.rotationYaw, pitch = f.self.rotationPitch;
        SemanticEvent render = new SemanticEvent(1); render.elapsedSeconds = .05; render.timerRenderPartialTicks = 1;
        aim.onEvent(f.c, render);
        check(aim.getClass().getMethod("currentTarget").invoke(aim) == null && f.self.rotationYaw == yaw && f.self.rotationPitch == pitch, "cached Aim Assist target kept rotating after becoming a teammate");

        // Per-player scoreboard teams may share the game-team colour; rank prefixes may match across enemies.
        ScorePlayerTeam ours = board.createTeam("ours"), theirs = board.createTeam("friend"), hostile = board.createTeam("enemy");
        ours.setNamePrefix("\u00a76[MVP] \u00a7b"); theirs.setNamePrefix("\u00a76[MVP] \u00a7b"); hostile.setNamePrefix("\u00a76[MVP] \u00a7c");
        ours.setNameSuffix("\u00a7r"); theirs.setNameSuffix("\u00a7r"); hostile.setNameSuffix("\u00a7r");
        board.addPlayerToTeam(f.self.getName(), "ours"); board.addPlayerToTeam(f.enemy.getName(), "friend"); board.addPlayerToTeam(opponent.getName(), "enemy");
        enemy.setDisplayName(styled(opponent.getName(), EnumChatFormatting.AQUA));
        refresh(teams, f);
        check((RelationshipService.flags(f.enemy) & 1) != 0 && (RelationshipService.flags(opponent) & 1) == 0, "scoreboard username colour lost to shared rank/tab colour");
        aim.onEvent(f.c, new SemanticEvent(18));
        check(aim.getClass().getMethod("currentTarget").invoke(aim) == opponent, "scoreboard teammate was eligible for Aim Assist");
        flag(teams, "match_self_color", false); refresh(teams, f);
        check((RelationshipService.flags(f.enemy) & 1) == 0, "scoreboard auto-matching ignored Match self colour setting");
        teams.findSetting("team_colors").parseAndSet("Light aqua"); refresh(teams, f);
        check((RelationshipService.flags(f.enemy) & 1) != 0 && (RelationshipService.flags(opponent) & 1) == 0, "explicit team-colour selection stopped working");
        teams.findSetting("team_colors").parseAndSet(""); flag(teams, "match_self_color", true);
        board.removePlayerFromTeam(f.self.getName(), ours); board.removePlayerFromTeam(f.enemy.getName(), theirs); board.removePlayerFromTeam(opponent.getName(), hostile);
        own.setDisplayName(null); friend.setDisplayName(null); enemy.setDisplayName(null); refresh(teams, f);
        check((RelationshipService.flags(f.enemy) & 1) == 0 && (RelationshipService.flags(opponent) & 1) == 0, "uncoloured players were all treated as teammates");
        own.setDisplayName(styled(f.self.getName(), EnumChatFormatting.WHITE)); friend.setDisplayName(styled(f.enemy.getName(), EnumChatFormatting.WHITE)); refresh(teams, f);
        check((RelationshipService.flags(f.enemy) & 1) != 0, "an explicit white game team was discarded");
        RelationshipService.set(f.enemy, 4, true); f.manager.applyNow(teams, false);
        check(RelationshipService.flags(f.enemy) == 4, "Teams disable changed friendship flags");
        f.close();
        System.out.println("PASS: Teams styled/split names, tab/scoreboard consistency, rank/reset/unknown/explicit-white colours, configured filters, live Aim Assist teammate rejection and cached-target revalidation");
    }
}
