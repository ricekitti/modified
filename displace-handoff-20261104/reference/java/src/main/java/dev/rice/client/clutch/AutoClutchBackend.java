package dev.rice.client.clutch;

import java.util.ArrayList;
import java.util.List;

/** Sam's OpenB4 AutoClutch script ("-original by afk, edited by noot-"), ported verbatim onto Rice's engine via
 * {@link ClutchApi}; the script-runtime interpreter is gone, the logic is unchanged. */
public class AutoClutchBackend extends ClutchApi {
String scriptName = "AutoClutch";

boolean falling = false, john = false, rcing = false;
boolean turnoff = false, fbed = false, delayB = false, toolazy = false;

int jumpticks = 0;
int hurtTime = 0, delayT = 0, maxdis = 0;
int ToggleDelayI = 0, GroundI = 0;
boolean ToggleDelayB = false, Groundb = false;

String[] offmode = {"None", "Movement", "Forward", "Ground", "Ground (delay)", "Ground + Forward"};
String[] maxblcokss = {"None", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20"};
String[] delaythingy = {"None", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "200"};
String[] onmode = {"Void", "Void hit", "Void hit (ignore fb)"};
String[] toggleOnOptions = onmode;
String[] toggleOffOptions = offmode;
String[] themeOptions = {
    "Default", "Rainbow", "Aurora", "Cherry", "Cotton Candy",
    "Flare", "Flower", "Forest", "Frost", "Gold",
    "Grayscale", "Inferno", "Royal", "Sandstorm", "Sky", "Vine"
};
String[] settingsThemeMap = {
    "Rainbow", "Aurora", "Cherry", "Cotton Candy", "Flare", "Flower", "Forest", "Frost",
    "Gold", "Grayscale", "Inferno", "Royal", "Sandstorm", "Sky", "Vine"
};
String[] maxFallOptions = {
    "Void only", "1 block", "2 blocks", "3 blocks", "4 blocks", "5 blocks",
    "6 blocks", "7 blocks", "8 blocks", "9 blocks", "10 blocks", "11 blocks",
    "12 blocks", "13 blocks", "14 blocks", "15 blocks", "16 blocks", "17 blocks",
    "18 blocks", "19 blocks", "20 blocks", "21 blocks", "22 blocks", "23 blocks",
    "24 blocks", "25 blocks", "26 blocks", "27 blocks", "28 blocks", "29 blocks",
    "30 blocks", "31 blocks", "32 blocks", "33 blocks", "34 blocks", "35 blocks",
    "36 blocks", "37 blocks", "38 blocks", "39 blocks", "40 blocks", "41 blocks",
    "42 blocks", "43 blocks", "44 blocks", "45 blocks", "46 blocks", "47 blocks",
    "48 blocks", "49 blocks", "50 blocks"
};

double HW = 0.3;
double[][] CORNERS = {{ -HW, -HW }, { HW, -HW }, { -HW, HW }, { HW, HW }};
double REACH = 4.5;
int BLOCK_DELAY = 0;
int FIREBALL_DISABLE_TICKS = 10;

float serverYaw, serverPitch;
Vec3 placeAt, hitAt;
String hitSide = "";
boolean placeQueued;
boolean placing;
boolean slotWasSwapped;
boolean autoClickerWasOn;
int prevSlot = -1;
int plannedSlot = -1;
int clutchOriginalSlot = -1;
int clutchEquippedBlockSlot = -1;
int clutchDoneGroundTicks = 0;
float aimYaw, aimPitch;
Vec3 targetHitPos;
String targetSide = "";
boolean hasAim;
boolean resetting;
int airTicksWithoutPlace = 0;
int groundDelayTicks = 0;
int lastPlacedX = -999, lastPlacedY = -999, lastPlacedZ = -999;
int clutchBlocksPlaced = 0;

ArrayList<Vec3> currentPath = new ArrayList<>();
Vec3 pathHead = null;
String pathSide = "";
boolean hasActivePath = false;
int pathFailTicks = 0;

int aimLingerTicks = 0;
boolean bridgeFailed = false;
int lowestBridgeY = -999;
int clutchTargetY = -999;
boolean wasSneakingAntiSlip = false;
int antiSlipTicks = 0;
Vec3 renderBlockPos = null;
ArrayList<Vec3> fadeBlockPositions = new ArrayList<>();
ArrayList<Long> fadeBlockFadeStarts = new ArrayList<>();
ArrayList<Long> fadeBlockDurations = new ArrayList<>();
ArrayList<Long> fadeBlockPlacedTimes = new ArrayList<>();

long clutchCooldownUntil = 0L;
int lastPlaceTick = -999;
int lastPlayerId = -1;
int lastSeenTicks = -1;

int prevHurtTime = -1;
float lastFallDistance = 0.0f;
boolean lastOnGround = true;
long lastFallDamageTime = 0L;
long lastClutchFinishTime = 0L;
int groundTicksAfterHit = 0;
boolean hitTriggered;
long lastHitTime = 0L;
long lastExplosionTime = 0L;
long lastNearFireballTime = 0L;
long lastFireballJumpTime = 0L;
long fireballDisableUntil = 0L;
long fireballHitTime = 0L;
boolean fireballTriggered;
double prevMotionX = 0.0, prevMotionZ = 0.0;
int clutchAttempts = 0;
boolean stopModeBlocked = false;
int lastGroundBlockY = -999;

double getSliderValue(String module, String setting) {
    try {
        Object v = modules.getSlider(module, setting);
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v != null) {
            return Double.parseDouble(String.valueOf(v));
        }
    } catch (Exception ignored) {}
    return 0.0;
}

String getModName() {
    try {
        String n = getScriptName();
        if (n != null && !n.isEmpty()) {
            scriptName = n;
            return n;
        }
    } catch (Exception ignored) {}
    return scriptName != null ? scriptName : "AutoClutch";
}

int getToggleOnMode() {
    try {
        Object v = modules.getSlider(getModName(), "Toggle on");
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v != null) {
            String s = String.valueOf(v).trim();
            for (int i = 0; i < onmode.length; i++) {
                if (onmode[i].equalsIgnoreCase(s)) return i;
            }
            return (int) Math.round(Double.parseDouble(s));
        }
    } catch (Exception ignored) {}
    return 1;
}

int getToggleOffMode() {
    try {
        Object v = modules.getSlider(getModName(), "Toggle off");
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v != null) {
            String s = String.valueOf(v).trim();
            for (int i = 0; i < offmode.length; i++) {
                if (offmode[i].equalsIgnoreCase(s)) return i;
            }
            return (int) Math.round(Double.parseDouble(s));
        }
    } catch (Exception ignored) {}
    return 2;
}

int getMaxBlocks() {
    try {
        Object val = modules.getSlider(getModName(), "Max blocks");
        if (val instanceof Number) {
            int idx = ((Number) val).intValue();
            if (idx >= 0 && idx < maxblcokss.length) {
                String s = maxblcokss[idx];
                if ("None".equals(s)) return 0;
                return Integer.parseInt(s);
            }
            return idx;
        }
    } catch (Exception ignored) {}
    return 6;
}

int getDelay() {
    try {
        Object val = modules.getSlider(getModName(), "Clutch Cooldown");
        if (val instanceof Number) {
            int idx = ((Number) val).intValue();
            if (idx >= 0 && idx < delaythingy.length) {
                String s = delaythingy[idx];
                if ("None".equalsIgnoreCase(s)) return 0;
                return Integer.parseInt(s);
            }
            return idx;
        } else if (val instanceof String) {
            String s = (String) val;
            if ("None".equalsIgnoreCase(s)) return 0;
            return Integer.parseInt(s);
        }
    } catch (Exception ignored) {}
    return 0;
}

void disableClutch() {
    if (falling || clutchBlocksPlaced > 0) {
        lastClutchFinishTime = client.time();
    }
    falling = false;
    disablePlacing();
    resetPath();
    clutchBlocksPlaced = 0;
    maxdis = 0;
    lowestBridgeY = -999;
    clutchTargetY = -999;
    toolazy = false;
    turnoff = false;
    bridgeFailed = false;
    hitTriggered = false;
    lastHitTime = 0L;
    airTicksWithoutPlace = 0;
    if (aimLingerTicks <= 0) {
        clearAim();
    }
    if (wasSneakingAntiSlip) {
        keybinds.setPressed("sneak", false);
        wasSneakingAntiSlip = false;
    }
    antiSlipTicks = 0;
    prevMotionX = 0.0;
    prevMotionZ = 0.0;
}

int fallDistance() {
    Entity me = client.getPlayer();
    if (me == null) return -1;
    Vec3 pos = me.getPosition();
    if (pos == null) return -1;
    int y = (int) Math.floor(pos.y) - 1;
    int px = (int) Math.floor(pos.x);
    int pz = (int) Math.floor(pos.z);

    for (int i = y; i >= 0; i--) {
        Block block = world.getBlockAt(px, i, pz);
        if (block == null || block.name.equals("air") || block.name.contains("sign") || canPlaceThrough(block.name)) {
            continue;
        }
        return y - i;
    }
    return -1;
}

   public void onPostPlayerInput() {
    rcing = keybinds.isPressed("use");
    if (keybinds.isPressed("jump")) {
        jumpticks = 0;
    }
}

double getSliderValue(String setting) {
    return getSliderValue(getModName(), setting);
}

int getSliderInt(String module, String setting) {
    return (int) Math.round(getSliderValue(module, setting));
}

int getSliderInt(String setting) {
    return (int) Math.round(getSliderValue(getModName(), setting));
}

boolean getButtonValue(String setting) {
    try {
        Object v = modules.getButton(getModName(), setting);
        if (v instanceof Boolean) return ((Boolean) v).booleanValue();
        if (v != null) return Boolean.parseBoolean(String.valueOf(v));
    } catch (Exception ignored) {}
    return false;
}

void dbg(String s) {
    if (getButtonValue("Debug Logs")) {
        client.print(util.color("&7[&dR&7] &d" + getModName() + "&7: &f" + s));
    }
}

int clampInt(int val, int min, int max) {
    return val < min ? min : (val > max ? max : val);
}

int lerpColor(int first, int second, double progress) {
    int red = clampInt((int) (((first >> 16) & 0xFF) + ((((second >> 16) & 0xFF) - ((first >> 16) & 0xFF)) * progress)), 0, 255);
    int green = clampInt((int) (((first >> 8) & 0xFF) + ((((second >> 8) & 0xFF) - ((first >> 8) & 0xFF)) * progress)), 0, 255);
    int blue = clampInt((int) ((first & 0xFF) + (((second & 0xFF) - (first & 0xFF)) * progress)), 0, 255);
    return 0xFF000000 | (red << 16) | (green << 8) | blue;
}

int withAlpha(int color, int alpha) {
    return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
}

int getThemeColor(String name) {
    if (name == null || name.isEmpty()) return 0xFFFFFFFF;
    String lower = name.toLowerCase().trim();
    double millis = client.time();

    if (lower.equals("rainbow")) {
        double phase = millis / 420.0;
        return 0xFF000000
            | (clampInt((int) (128 + 127 * Math.sin(phase)), 0, 255) << 16)
            | (clampInt((int) (128 + 127 * Math.sin(phase + 2.094)), 0, 255) << 8)
            | clampInt((int) (128 + 127 * Math.sin(phase + 4.189)), 0, 255);
    }

    double pulse = (Math.sin(millis / 1200.0) + 1.0) / 2.0;
    if (lower.equals("aurora"))                                     return lerpColor(0xFF7301C2, 0xFF17F0B1, pulse);
    if (lower.equals("cherry"))                                     return lerpColor(0xFFDD3D69, 0xFFE0B3B7, pulse);
    if (lower.equals("cotton candy") || lower.equals("cottoncandy")) return lerpColor(0xFF92DAE8, 0xFFED68B8, pulse);
    if (lower.equals("flare"))                                      return lerpColor(0xFFF26B16, 0xFFE4A61D, pulse);
    if (lower.equals("flower"))                                     return lerpColor(0xFFC89AD8, 0xFFAC59B9, pulse);
    if (lower.equals("forest"))                                     return lerpColor(0xFF1F7617, 0xFF60A623, pulse);
    if (lower.equals("frost"))                                      return lerpColor(0xFFDFE3E3, 0xFFBCC5CA, pulse);
    if (lower.equals("gold"))                                       return lerpColor(0xFFE5DF30, 0xFFDADAB6, pulse);
    if (lower.equals("grayscale") || lower.equals("gray scale"))    return lerpColor(0xFF616368, 0xFFE7E8EA, pulse);
    if (lower.equals("inferno"))                                    return lerpColor(0xFF350000, 0xFFC03912, pulse);
    if (lower.equals("royal"))                                      return lerpColor(0xFF85BFE8, 0xFF1D3D87, pulse);
    if (lower.equals("sandstorm") || lower.equals("sand storm"))    return lerpColor(0xFF9D9369, 0xFFF5E3B4, pulse);
    if (lower.equals("sky"))                                        return lerpColor(0xFF81EAF8, 0xFF15BCD3, pulse);
    if (lower.equals("vine"))                                       return lerpColor(0xFF27E439, 0xFF9AF8A1, pulse);
    return 0xFFFFFFFF;
}

String resolveTheme() {
    int index = 0;
    try {
        index = (int) modules.getSlider(getScriptName(), "Theme");
    } catch (Exception e1) {
        try {
            index = (int) modules.getSlider(getModName(), "Theme");
        } catch (Exception e2) {
            try {
                index = (int) modules.getSlider("AutoClutch", "Theme");
            } catch (Exception e3) {}
        }
    }

    if (index == 0) {
        try {
            int settingsIndex = (int) modules.getSlider("Settings", "Default theme");
            if (settingsIndex >= 0 && settingsIndex < settingsThemeMap.length) {
                return settingsThemeMap[settingsIndex];
            }
        } catch (Exception ignored) {}
        return "Rainbow";
    }

    if (index >= 0 && index < themeOptions.length) {
        return themeOptions[index];
    }
    return "Rainbow";
}

void applyGlColor(int color) {
    float a = ((color >>> 24) & 0xFF) / 255.0f;
    float r = ((color >>> 16) & 0xFF) / 255.0f;
    float g = ((color >>> 8)  & 0xFF) / 255.0f;
    float b = (color & 0xFF)         / 255.0f;
    gl.color(r, g, b, a);
}

void renderBlockBox(Vec3 bp, int color, float alphaFactor) {
    Vec3 cam = render.getPosition();
    if (cam == null || bp == null) return;

    int outlineAlpha = clampInt((int) (240.0f * alphaFactor), 0, 255);
    if (outlineAlpha <= 2) return;

    double bx = Math.floor(bp.x);
    double by = Math.floor(bp.y);
    double bz = Math.floor(bp.z);

    double boxExpand = 0.002;
    double x1 = bx - boxExpand;
    double y1 = by - boxExpand;
    double z1 = bz - boxExpand;
    double x2 = bx + 1.0 + boxExpand;
    double y2 = by + 1.0 + boxExpand;
    double z2 = bz + 1.0 + boxExpand;

    gl.push();
    gl.blend(true);
    gl.texture2d(false);
    gl.depth(true);
    gl.depthMask(false);
    gl.lineSmooth(true);
    gl.lineWidth(2.0f);
    gl.translate(-cam.x, -cam.y, -cam.z);

    // ONLY wireframe outlines - NO filling / shaded faces
    applyGlColor(withAlpha(color, outlineAlpha));
    gl.begin(1); // GL_LINES
    // Bottom square
    gl.vertex3(x1, y1, z1); gl.vertex3(x2, y1, z1);
    gl.vertex3(x2, y1, z1); gl.vertex3(x2, y1, z2);
    gl.vertex3(x2, y1, z2); gl.vertex3(x1, y1, z2);
    gl.vertex3(x1, y1, z2); gl.vertex3(x1, y1, z1);

    // Top square
    gl.vertex3(x1, y2, z1); gl.vertex3(x2, y2, z1);
    gl.vertex3(x2, y2, z1); gl.vertex3(x2, y2, z2);
    gl.vertex3(x2, y2, z2); gl.vertex3(x1, y2, z2);
    gl.vertex3(x1, y2, z2); gl.vertex3(x1, y2, z1);

    // Vertical pillars
    gl.vertex3(x1, y1, z1); gl.vertex3(x1, y2, z1);
    gl.vertex3(x2, y1, z1); gl.vertex3(x2, y2, z1);
    gl.vertex3(x2, y1, z2); gl.vertex3(x2, y2, z2);
    gl.vertex3(x1, y1, z2); gl.vertex3(x1, y2, z2);
    gl.end();

    // Cleanly restore GL state without polluting Minecraft's render pipeline
    gl.lineWidth(1.0f);
    gl.lineSmooth(false);
    gl.depthMask(true);
    gl.depth(true);
    gl.texture2d(true);
    gl.blend(true);
    gl.color(1.0f, 1.0f, 1.0f, 1.0f);
    gl.resetColor();
    gl.pop();
}

void addUniqueBlock(ArrayList<Vec3> list, Vec3 pos) {
    if (list == null || pos == null) return;
    for (int i = 0; i < list.size(); i++) {
        Vec3 b = list.get(i);
        if (b != null && matchesBlock(b, pos)) return;
    }
    list.add(pos);
}

void recordPlacedBlockForRender(Vec3 placedBlock) {
    if (placedBlock == null) return;
    long now = client.time();
    int fadeMs = getSliderInt("Fade Time");
    if (fadeMs <= 0) fadeMs = 800;

    int existingIdx = -1;
    for (int i = 0; i < fadeBlockPositions.size(); i++) {
        Vec3 bp = fadeBlockPositions.get(i);
        if (bp != null && matchesBlock(bp, placedBlock)) {
            existingIdx = i;
            break;
        }
    }

    if (existingIdx >= 0) {
        fadeBlockFadeStarts.set(existingIdx, now);
        fadeBlockPlacedTimes.set(existingIdx, now);
        fadeBlockDurations.set(existingIdx, (long) fadeMs);
    } else {
        fadeBlockPositions.add(placedBlock);
        fadeBlockFadeStarts.add(now);
        fadeBlockPlacedTimes.add(now);
        fadeBlockDurations.add((long) fadeMs);
    }
}

   public void onRenderWorld(float partialTicks) {
    if (!getButtonValue("Render Block")) return;
    if (fadeBlockPositions.isEmpty()) return;

    long now = client.time();
    int baseCol = getThemeColor(resolveTheme());

    // Only render actual placed blocks that are fading out - NEVER render prospective/unplaced blocks!
    for (int i = fadeBlockPositions.size() - 1; i >= 0; i--) {
        Vec3 bp = fadeBlockPositions.get(i);
        long fStart = fadeBlockFadeStarts.get(i);
        long duration = fadeBlockDurations.get(i);
        if (duration <= 0) duration = 800L;

        long elapsed = now - fStart;
        if (elapsed >= duration || elapsed < 0) {
            fadeBlockPositions.remove(i);
            fadeBlockFadeStarts.remove(i);
            fadeBlockDurations.remove(i);
            fadeBlockPlacedTimes.remove(i);
            continue;
        }

        float progress = (float) elapsed / (float) duration;
        float alphaFactor = 1.0f - progress;
        if (alphaFactor > 0.01f && bp != null) {
            renderBlockBox(bp, baseCol, alphaFactor);
        }
    }
}

   public void onLoad() {
    try {
        String n = getScriptName();
        if (n != null && !n.isEmpty()) scriptName = n;
    } catch (Exception ignored) {}
    modules.registerDescription("Toggling options:");
    modules.registerSlider("Toggle on", " ", 0, onmode);
    modules.registerSlider("Toggle off", " ", 2, offmode);
    modules.registerDescription("Config:");
    modules.registerSlider("Max Fall", " ", 4, maxFallOptions);
    modules.registerSlider("Max blocks", " ", 3, maxblcokss);
    modules.registerSlider("Clutch Cooldown", " ", 0, delaythingy);
    modules.registerSlider("Air Limit", " ticks", 0, 0, 100, 1);
    modules.registerButton("Allow Placing", true);
    modules.registerButton("Stop mode", true);
    modules.registerButton("Anti tower", true);
    modules.registerButton("Stasis", true);
    modules.registerDescription("Prediction:");
    modules.registerButton("Recovery Check", true);
    modules.registerButton("Predicted Safe", true);
    modules.registerDescription("Rotations & Visuals:");
    modules.registerSlider("Speed", "", 35, 0, 100, 1);
    modules.registerButton("Legit Mode", true);
    modules.registerButton("Silent", false);
    modules.registerSlider("Aim Linger", " ticks", 4, 0, 20, 1);
    modules.registerButton("Render Block", true);
    modules.registerSlider("Fade Time", " ms", 800, 100, 3000, 50);
    modules.registerSlider("Theme", "", 0, themeOptions);
    modules.registerButton("Debug Logs", false);
}

   public void onEnable() {
    try {
        String n = getScriptName();
        if (n != null && !n.isEmpty()) scriptName = n;
    } catch (Exception ignored) {}
    Entity me = client.getPlayer();
    if (me != null) {
        lastPlayerId = me.entityId;
        lastSeenTicks = me.getTicksExisted();
        serverYaw = me.getYaw();
        serverPitch = me.getPitch();
        prevHurtTime = me.getHurtTime();
    } else {
        lastPlayerId = -1;
        lastSeenTicks = -1;
        prevHurtTime = -1;
    }
    falling = false;
    john = false;
    rcing = false;
    turnoff = false;
    fbed = false;
    delayB = false;
    delayT = 0;
    toolazy = false;
    jumpticks = 0;
    hurtTime = 0;
    maxdis = 0;
    ToggleDelayI = 0;
    ToggleDelayB = false;
    GroundI = 0;
    Groundb = false;
    hasAim = false;
    resetting = false;
    resetPath();
    clutchBlocksPlaced = 0;
    bridgeFailed = false;
    lowestBridgeY = -999;
    clutchTargetY = -999;
    renderBlockPos = null;
    long dcNow = client.time();
    for (int i = 0; i < fadeBlockPositions.size(); i++) {
        if (fadeBlockFadeStarts.get(i) == 0L) {
            fadeBlockFadeStarts.set(i, dcNow);
        }
    }
    hitTriggered = false;
    lastHitTime = 0L;
    lastExplosionTime = 0L;
    lastNearFireballTime = 0L;
    fireballDisableUntil = 0L;
    fireballHitTime = 0L;
    fireballTriggered = false;
    clutchCooldownUntil = 0L;
    lastClutchFinishTime = 0L;
    lastPlaceTick = -999;
    aimLingerTicks = 0;
    airTicksWithoutPlace = 0;
    groundDelayTicks = 0;
    prevMotionX = 0.0;
    prevMotionZ = 0.0;
    clutchAttempts = 0;
    stopModeBlocked = false;
    lastGroundBlockY = -999;
    clutchOriginalSlot = -1;
    clutchEquippedBlockSlot = -1;
    clutchDoneGroundTicks = 0;
    dbg("&aEnabled&7.");
}

   public void onDisable() {
    if (wasSneakingAntiSlip) {
        keybinds.setPressed("sneak", false);
        wasSneakingAntiSlip = false;
    }
    antiSlipTicks = 0;
    falling = false;
    turnoff = false;
    toolazy = false;
    ToggleDelayB = false;
    ToggleDelayI = 0;
    Groundb = false;
    GroundI = 0;
    fbed = false;
    delayB = false;
    delayT = 0;
    aimLingerTicks = 0;
    clearAim();
    disablePlacing();
    airTicksWithoutPlace = 0;
    groundDelayTicks = 0;
    bridgeFailed = false;
    lowestBridgeY = -999;
    clutchTargetY = -999;
    lastGroundBlockY = -999;
    renderBlockPos = null;
    fadeBlockPositions.clear();
    fadeBlockFadeStarts.clear();
    fadeBlockDurations.clear();
    fadeBlockPlacedTimes.clear();
    placeQueued = false;
    hitTriggered = false;
    fireballTriggered = false;
    fireballDisableUntil = 0L;
    clutchCooldownUntil = 0L;
    lastClutchFinishTime = 0L;
    lastPlaceTick = -999;
    lastPlayerId = -1;
    lastSeenTicks = -1;
    prevMotionX = 0.0;
    prevMotionZ = 0.0;
    clutchAttempts = 0;
    stopModeBlocked = false;
    lastGroundBlockY = -999;
    if (clutchOriginalSlot != -1) {
        int cur = inventory.getSlot();
        if (clutchEquippedBlockSlot == -1 || cur == clutchEquippedBlockSlot) {
            if (clutchOriginalSlot >= 0 && clutchOriginalSlot <= 8 && clutchOriginalSlot != cur) {
                inventory.setSlot(clutchOriginalSlot);
            }
        }
    }
    clutchOriginalSlot = -1;
    clutchEquippedBlockSlot = -1;
    clutchDoneGroundTicks = 0;
    dbg("&cDisabled&7.");
}

boolean hasSolidBelow(Vec3 pos) {
    if (pos == null) return false;
    int curY = (int) Math.floor(pos.y);
    double[] ox = {0, -0.29, 0.29, -0.29, 0.29};
    double[] oz = {0, -0.29, -0.29, 0.29, 0.29};
    for (int k = 0; k < ox.length; k++) {
        int px = (int) Math.floor(pos.x + ox[k]);
        int pz = (int) Math.floor(pos.z + oz[k]);
        for (int dy = 1; dy <= 3; dy++) {
            Block b = world.getBlockAt(px, curY - dy, pz);
            if (b != null && !canPlaceThrough(b.name)) {
                return true;
            }
        }
    }
    return false;
}

   public float[] getRotations(float lastYaw, float lastPitch) {
    Entity me = client.getPlayer();
    if (me == null || !client.getScreen().isEmpty()) {
        disablePlacing();
        clearAim();
        return null;
    }

    int currentTick = me.getTicksExisted();
    if (me.entityId != lastPlayerId || currentTick < lastSeenTicks) {
        lastPlayerId = me.entityId;
        lastSeenTicks = currentTick;
        lastPlaceTick = -999;
        clutchCooldownUntil = 0L;
        fireballDisableUntil = 0L;
        lastHitTime = 0L;
        hitTriggered = false;
        prevHurtTime = me.getHurtTime();
        clearAim();
        disablePlacing();
        resetPath();
        clutchBlocksPlaced = 0;
        aimLingerTicks = 0;
        clutchOriginalSlot = -1;
        clutchEquippedBlockSlot = -1;
        clutchDoneGroundTicks = 0;
    } else {
        lastSeenTicks = currentTick;
    }

    if (currentTick < lastPlaceTick) {
        lastPlaceTick = -999;
    }

    serverYaw = lastYaw;
    serverPitch = lastPitch;

    if (resetting) {
        float targetYaw = unwrapYaw(me.getYaw(), lastYaw);
        float targetPitch = me.getPitch();
        float[] sm = getRotationsSmoothed(lastYaw, lastPitch, targetYaw, targetPitch, true);
        if (placing) equipPlannedSlot();
        if (Math.abs(sm[0] - targetYaw) < 1.0f && Math.abs(sm[1] - targetPitch) < 1.0f) {
            resetting = false;
            disablePlacing();
            if (Math.abs(me.getYaw() - sm[0]) > 0.01f) {
                me.setYaw(sm[0]);
            }
            return null;
        }
        return sm;
    }

    boolean active = falling;

    if (!active) {
        if (hasAim) {
            // Player mouse movement break: if player manually turns their mouse, immediately release linger
            float mouseDeltaYaw = Math.abs(wrapYawDelta(me.getYaw(), lastYaw));
            float mouseDeltaPitch = Math.abs(me.getPitch() - lastPitch);
            if (mouseDeltaYaw > 1.2f || mouseDeltaPitch > 1.2f) {
                clearAim();
                return null;
            }

            if (aimLingerTicks > 0) {
                aimLingerTicks--;
                disablePlacing();
                return getRotationsSmoothed(lastYaw, lastPitch, aimYaw, aimPitch, false);
            }
            clearAim();
            float targetYaw = unwrapYaw(me.getYaw(), lastYaw);
            return getRotationsSmoothed(lastYaw, lastPitch, targetYaw, me.getPitch(), true);
        }
        return null;
    }

    if (clutchBlocksPlaced == 0) {
        int distCheck = fallDistance();
        double maxFallValCheck = getSliderValue("Max Fall");
        boolean isOverVoidOrFar = (distCheck == -1) || (maxFallValCheck > 0 && distCheck > (int) maxFallValCheck);
        if (!isOverVoidOrFar) {
            if (hasAim) {
                clearAim();
                float targetYaw = unwrapYaw(me.getYaw(), lastYaw);
                return getRotationsSmoothed(lastYaw, lastPitch, targetYaw, me.getPitch(), true);
            }
            return null;
        }
    }

    Vec3 pos = me.getPosition();

    // Do not kill clutch while falling in air; keep trying to clutch under player

    boolean needsClutch = !me.onGround() && !hasSolidBelow(pos);

    if (!needsClutch) {
        if (hasAim) {
            if (aimLingerTicks > 0) {
                aimLingerTicks--;
                disablePlacing();
                return getRotationsSmoothed(lastYaw, lastPitch, aimYaw, aimPitch, false);
            }
            clearAim();
            float targetYaw = unwrapYaw(me.getYaw(), lastYaw);
            return getRotationsSmoothed(lastYaw, lastPitch, targetYaw, me.getPitch(), true);
        }
        return null;
    }

    ItemStack currentHeld = (plannedSlot >= 0 && plannedSlot <= 8) ? inventory.getStackInSlot(plannedSlot) : null;
    if (currentHeld == null || currentHeld.stackSize <= 0 || !currentHeld.isBlock || isUnplaceable(currentHeld.name.toLowerCase())) {
        int blockSlot = pickBlockSlot();
        if (blockSlot == -1) {
            if (hasAim) {
                if (getButtonValue("Legit Mode") && aimLingerTicks > 0) {
                    aimLingerTicks--;
                    if (placing) equipPlannedSlot();
                    return getRotationsSmoothed(lastYaw, lastPitch, aimYaw, aimPitch, false);
                }
                clearAim();
                float targetYaw = unwrapYaw(me.getYaw(), lastYaw);
                return getRotationsSmoothed(lastYaw, lastPitch, targetYaw, me.getPitch(), true);
            }
            return null;
        }
        plannedSlot = blockSlot;
    }

    Object[] tgt = clutchAim();
    if (tgt != null) {
        Object[] ray = (Object[]) tgt[0];
        targetHitPos = (Vec3) ray[0];
        targetSide = (String) ray[2];
        aimYaw = (float) tgt[1];
        aimPitch = (float) tgt[2];
        hasAim = true;
        resetting = false;
    } else {
        if (hasAim) {
            clearAim();
            disablePlacing();
            float targetYaw = unwrapYaw(me.getYaw(), lastYaw);
            return getRotationsSmoothed(lastYaw, lastPitch, targetYaw, me.getPitch(), true);
        }
        return null;
    }

    if (hasAim && !placing) enablePlacing();

    if (placing) {
        keybinds.setPressed("attack", false);
        keybinds.setPressed("use", false);
        equipPlannedSlot();
    }

    float[] sm = getRotationsSmoothed(lastYaw, lastPitch, aimYaw, aimPitch, false);

    if (placing && targetHitPos != null) {
        currentTick = me.getTicksExisted();
        int delay = BLOCK_DELAY;

        if (currentTick - lastPlaceTick >= delay) {
            Object[] chk = client.raycastBlock(REACH, sm[0], sm[1]);
            if (chk != null) {
                Vec3 hit = (Vec3) chk[0];
                String side = (String) chk[2];
                if (matchesBlock(hit, targetHitPos) && side.equals(targetSide)) {
                    int maxAllowed = getMaxBlocks();
                    if (maxAllowed == 0 || maxdis < maxAllowed) {
                        double tol = getButtonValue("Legit Mode") ? 45.0 : 100.0;
                        if (Math.abs(wrapYawDelta(sm[0], aimYaw)) <= tol && Math.abs(sm[1] - aimPitch) <= tol) {
                            Vec3 placedTarget = offsetByFace(hit, side);
                            int maxAllowedY = (int) Math.floor(me.getPosition().y - 0.01);
                            if (placedTarget.y > maxAllowedY || (clutchTargetY != -999 && placedTarget.y > clutchTargetY)) {
                                targetHitPos = null;
                                targetSide = "";
                                return null;
                            }
                            Vec3 hitVec = ((Vec3) chk[1]).offset(hit.x, hit.y, hit.z);
                            if (client.placeBlock(hit, side, hitVec)) {
                                clutchBlocksPlaced++;
                                maxdis++;
                                lastPlacedX = (int) Math.floor(hit.x);
                                lastPlacedY = (int) Math.floor(hit.y);
                                lastPlacedZ = (int) Math.floor(hit.z);
                                lastPlaceTick = currentTick;
                                airTicksWithoutPlace = 0;
                                client.swing();
                                dbg("&aPlaced block on &b" + side + " &fof (&e" + (int)hit.x + "&f, &e" + (int)hit.y + "&f, &e" + (int)hit.z + "&f)");
                                onBlockSuccessfullyPlaced(hit, side);

                                if (maxAllowed > 0 && maxdis >= maxAllowed) {
                                    if (isStopMode() && handleStopModeRetry(me, currentTick)) {
                                        // Attempt 2 started or final stop handled by Stop mode
                                    } else {
                                        falling = false;
                                        turnoff = true;
                                        toolazy = true;
                                        disablePlacing();
                                        if (aimLingerTicks <= 0) {
                                            clearAim();
                                        }
                                    }
                                }
                            } else {
                                hitAt = hit;
                                hitSide = side;
                                placeAt = hitVec;
                                placeQueued = true;
                            }
                        }
                    }
                }
            }
        }
    }

    return sm;
}

boolean isAntiTower() {
    return getButtonValue("Anti tower") || getButtonValue("Anti build up");
}

boolean isStopMode() {
    return getButtonValue("Stop mode");
}

boolean isStasisOption() {
    return getButtonValue("Stasis");
}

boolean isStasisActive() {
    try {
        if (modules.isEnabled("Stasis") || modules.isEnabled("stasis")) return true;
    } catch (Exception ignored) {}
    return false;
}

boolean isStasisModeActive() {
    return isStasisOption() && isStasisActive();
}

boolean isPlayerStationary(Entity me) {
    if (me == null) return false;
    if (isStasisActive()) return true;
    Vec3 p = me.getPosition();
    Vec3 lp = me.getLastPosition();
    if (p == null || lp == null) return false;
    double hSpeed = Math.hypot(p.x - lp.x, p.z - lp.z);
    boolean hasMoveKey = keybinds.isPressed("forward") || keybinds.isPressed("back") || keybinds.isPressed("left") || keybinds.isPressed("right");
    return hSpeed < 0.03 && !hasMoveKey;
}

boolean hasSolidBlockDirectlyUnder(Entity me) {
    if (me == null) return false;
    Vec3 p = me.getPosition();
    if (p == null) return false;
    int checkY = (int) Math.floor(p.y - 0.05);

    for (int y = checkY; y >= checkY - 1; y--) {
        for (double[] c : CORNERS) {
            int bx = (int) Math.floor(p.x + c[0]);
            int bz = (int) Math.floor(p.z + c[1]);
            Block b = world.getBlockAt(bx, y, bz);
            if (b != null && !b.name.equals("air") && !b.name.contains("sign") && !canPlaceThrough(b.name)) {
                return true;
            }
        }
        int px = (int) Math.floor(p.x);
        int pz = (int) Math.floor(p.z);
        Block b = world.getBlockAt(px, y, pz);
        if (b != null && !b.name.equals("air") && !b.name.contains("sign") && !canPlaceThrough(b.name)) {
            return true;
        }
    }
    return false;
}

boolean handleStopModeRetry(Entity player, int currentTick) {
    if (!isStopMode()) return false;
    if (player == null || player.onGround()) return false;

    int dist = fallDistance();
    double maxFallVal = getSliderValue("Max Fall");
    boolean isOverVoidOrFar = (dist == -1) || (maxFallVal > 0 && dist > (int) maxFallVal);
    boolean stillFalling = isOverVoidOrFar && !hasSolidBelow(player.getPosition());

    if (!stillFalling) return false;

    if (clutchAttempts == 1) {
        clutchAttempts = 2;
        falling = true;
        clutchBlocksPlaced = 0;
        maxdis = 0;
        toolazy = false;
        turnoff = false;
        ToggleDelayB = false;
        ToggleDelayI = 0;
        airTicksWithoutPlace = 0;
        aimLingerTicks = 0;
        resetPath();
        targetHitPos = null;
        targetSide = "";
        clutchTargetY = (int) Math.floor(player.getPosition().y) - 1;
        lastPlaceTick = -999;
        dbg("&e[Stop mode] Still falling after clutch! Trying one more time (Attempt 2).");
        return true;
    } else if (clutchAttempts >= 2) {
        falling = false;
        turnoff = true;
        toolazy = true;
        stopModeBlocked = true;
        disableClutch();
        dbg("&c[Stop mode] Second attempt finished. Will not try again.");
        return true;
    }
    return false;
}

double computeReachAtY(double startY, double landingY) {
    Vec3 motion = client.getMotion();
    if (motion == null) return 0.0;
    double velY = motion.y;
    double velX = motion.x;
    double velZ = motion.z;
    double simY = startY;
    double totalX = 0.0;
    double totalZ = 0.0;

    double accelX = 0.0;
    double accelZ = 0.0;
    if (prevMotionX != 0.0 || prevMotionZ != 0.0) {
        accelX = velX / 0.91 - prevMotionX;
        accelZ = velZ / 0.91 - prevMotionZ;
    }

    boolean reachedAbove = (startY >= landingY);

    for (int tick = 0; tick < 80; tick++) {
        velX += accelX;
        velZ += accelZ;
        simY += velY;
        totalX += velX;
        totalZ += velZ;
        velY = (velY - 0.08) * 0.98;
        velX *= 0.91;
        velZ *= 0.91;

        if (simY >= landingY) {
            reachedAbove = true;
        }

        if (reachedAbove && simY <= landingY) {
            return Math.sqrt(totalX * totalX + totalZ * totalZ);
        }
    }
    return 0.0;
}

boolean canRecoverToPlatform(Entity me) {
    if (me == null) return false;
    if (me.onGround()) return true;

    Vec3 pos = me.getPosition();
    if (pos == null) return false;
    Vec3 motion = client.getMotion();
    if (motion == null) return false;

    double horizSpeed = Math.hypot(motion.x, motion.z);
    if (horizSpeed <= 0.05 && !keybinds.isPressed("forward")) {
        return false;
    }

    double dirX = 0.0, dirZ = 0.0;
    if (horizSpeed > 0.04) {
        dirX = motion.x / horizSpeed;
        dirZ = motion.z / horizSpeed;
    } else {
        float yawRad = (float) Math.toRadians(me.getYaw());
        dirX = -Math.sin(yawRad);
        dirZ = Math.cos(yawRad);
    }

    int footY = (int) Math.floor(pos.y);
    double maxFallVal = getSliderValue("Max Fall");

    // Scan ahead in direction of motion / look for solid platforms up to 5 blocks
    for (double dist = 0.6; dist <= 5.0; dist += 0.5) {
        int cx = (int) Math.floor(pos.x + dirX * dist);
        int cz = (int) Math.floor(pos.z + dirZ * dist);

        double toCx = (cx + 0.5) - pos.x;
        double toCz = (cz + 0.5) - pos.z;
        if (toCx * dirX + toCz * dirZ <= 0) continue;

        for (int dy = 1; dy >= -4; dy--) {
            int cy = footY + dy;
            if (cy < 0 || cy > 255) continue;

            Block b = world.getBlockAt(cx, cy, cz);
            if (b != null && !b.name.equals("air") && !b.name.contains("sign") && !canPlaceThrough(b.name)) {
                // Check if top is clear to stand on
                Block above1 = world.getBlockAt(cx, cy + 1, cz);
                Block above2 = world.getBlockAt(cx, cy + 2, cz);
                boolean clear1 = (above1 == null || above1.name.equals("air") || canPlaceThrough(above1.name));
                boolean clear2 = (above2 == null || above2.name.equals("air") || canPlaceThrough(above2.name));

                if (clear1 && clear2) {
                    double landingTopY = cy + 1.0;
                    double drop = pos.y - landingTopY;
                    if (maxFallVal > 0 && drop > maxFallVal) {
                        continue;
                    }

                    double actualHorizDist = Math.hypot((pos.x + dirX * dist) - pos.x, (pos.z + dirZ * dist) - pos.z);
                    double reach = computeReachAtY(pos.y, landingTopY);
                    if (reach > 0.05 && actualHorizDist <= reach + 0.45) {
                        if (motion.x * dirX + motion.z * dirZ > 0.02) {
                            return true;
                        }
                    }
                }
            }
        }
    }

    return false;
}

boolean isPredictedSafe(Entity me) {
    if (me == null) return false;
    if (me.onGround()) return true;

    Vec3 pos = me.getPosition();
    if (pos == null) return false;
    if (pos.y < 0 || pos.y > 256) return false;

    Vec3 motion = client.getMotion();
    if (motion == null) return false;

    double velX = motion.x;
    double velY = motion.y;
    double velZ = motion.z;

    // If motionless (e.g. Stasis or suspended in air), only safe if on ground or supported by block directly below
    if (Math.abs(velX) < 0.0001 && Math.abs(velZ) < 0.0001 && Math.abs(velY) < 0.0001) {
        return me.onGround() || hasSolidBlockDirectlyUnder(me);
    }

    double accelX = 0.0;
    double accelZ = 0.0;
    if (prevMotionX != 0.0 || prevMotionZ != 0.0) {
        accelX = velX / 0.91 - prevMotionX;
        accelZ = velZ / 0.91 - prevMotionZ;
    }

    // Movement key acceleration
    float moveForward = 0f;
    float moveStrafe = 0f;
    if (keybinds.isPressed("forward")) moveForward += 1f;
    if (keybinds.isPressed("back")) moveForward -= 1f;
    if (keybinds.isPressed("left")) moveStrafe += 1f;
    if (keybinds.isPressed("right")) moveStrafe -= 1f;
    if (moveForward != 0f || moveStrafe != 0f) {
        float inputYaw = me.getYaw();
        if (moveForward < 0f) inputYaw += 180f;
        float fwd = 1f;
        if (moveForward < 0f) fwd = -0.5f;
        else if (moveForward > 0f) fwd = 0.5f;
        if (moveStrafe > 0f) inputYaw -= 90f * fwd;
        if (moveStrafe < 0f) inputYaw += 90f * fwd;
        float moveRad = (float) Math.toRadians(inputYaw);
        accelX += -Math.sin(moveRad) * 0.026;
        accelZ += Math.cos(moveRad) * 0.026;
    }

    double simX = pos.x;
    double simY = pos.y;
    double simZ = pos.z;
    double halfW = 0.3;
    double maxFallVal = getSliderValue("Max Fall");

    for (int tick = 0; tick < 60; tick++) {
        velX += accelX;
        velZ += accelZ;

        double prevSimX = simX;
        double prevSimY = simY;
        double prevSimZ = simZ;

        simX += velX;
        simY += velY;
        simZ += velZ;

        velY = (velY - 0.08) * 0.98;
        velX *= 0.91;
        velZ *= 0.91;

        if (simY < 0) return false;

        // Horizontal wall collision check
        int simFootY = (int) Math.floor(simY);
        int simHeadY = (int) Math.floor(simY + 1.8);
        for (int by = simFootY; by <= simHeadY; by++) {
            if (by < 0 || by > 255) continue;
            int xEdge = (int) Math.floor(velX >= 0 ? simX + halfW : simX - halfW);
            int prevXEdge = (int) Math.floor(velX >= 0 ? prevSimX + halfW : prevSimX - halfW);
            if (xEdge != prevXEdge) {
                for (int bz = (int) Math.floor(simZ - halfW); bz <= (int) Math.floor(simZ + halfW); bz++) {
                    Block b = world.getBlockAt(xEdge, by, bz);
                    if (b != null && !b.name.equals("air") && !b.name.contains("sign") && !canPlaceThrough(b.name)) {
                        velX = 0;
                        simX = prevSimX;
                        break;
                    }
                }
            }
            int zEdge = (int) Math.floor(velZ >= 0 ? simZ + halfW : simZ - halfW);
            int prevZEdge = (int) Math.floor(velZ >= 0 ? prevSimZ + halfW : prevSimZ - halfW);
            if (zEdge != prevZEdge) {
                for (int bx = (int) Math.floor(simX - halfW); bx <= (int) Math.floor(simX + halfW); bx++) {
                    Block b = world.getBlockAt(bx, by, zEdge);
                    if (b != null && !b.name.equals("air") && !b.name.contains("sign") && !canPlaceThrough(b.name)) {
                        velZ = 0;
                        simZ = prevSimZ;
                        break;
                    }
                }
            }
        }

        // Landing collision check on block tops
        int minY = (int) Math.floor(simY);
        int maxY = (int) Math.floor(prevSimY);
        for (int checkY = maxY; checkY >= minY; checkY--) {
            if (checkY < 0 || checkY > 255) continue;
            double blockTop = checkY + 1.0;
            if (prevSimY >= blockTop - 0.05) {
                double[] xCorners = {simX - halfW, simX + halfW};
                double[] zCorners = {simZ - halfW, simZ + halfW};
                for (int cx = 0; cx < 2; cx++) {
                    for (int cz = 0; cz < 2; cz++) {
                        int bx = (int) Math.floor(xCorners[cx]);
                        int bz = (int) Math.floor(zCorners[cz]);
                        Block block = world.getBlockAt(bx, checkY, bz);
                        if (block != null && !block.name.equals("air") && !block.name.contains("sign") && !canPlaceThrough(block.name)) {
                            // Check that space above is clear
                            Block above1 = world.getBlockAt(bx, checkY + 1, bz);
                            Block above2 = world.getBlockAt(bx, checkY + 2, bz);
                            boolean clear1 = (above1 == null || above1.name.equals("air") || canPlaceThrough(above1.name));
                            boolean clear2 = (above2 == null || above2.name.equals("air") || canPlaceThrough(above2.name));
                            if (clear1 && clear2) {
                                double drop = pos.y - blockTop;
                                if (maxFallVal > 0 && drop > maxFallVal) {
                                    return false; // Fall is too far
                                }
                                return true; // Landed safely!
                            }
                        }
                    }
                }
            }
        }
    }

    return false;
}

   public void onPreUpdate() {
    Entity player = client.getPlayer();
    if (player == null) return;

    int currentTick = player.getTicksExisted();
    if (player.entityId != lastPlayerId || currentTick < lastSeenTicks) {
        lastPlayerId = player.entityId;
        lastSeenTicks = currentTick;
        falling = false;
        turnoff = false;
        toolazy = false;
        ToggleDelayB = false;
        ToggleDelayI = 0;
        Groundb = false;
        GroundI = 0;
        fbed = false;
        delayB = false;
        delayT = 0;
        maxdis = 0;
        clutchBlocksPlaced = 0;
        bridgeFailed = false;
        lowestBridgeY = -999;
        clutchTargetY = -999;
        lastGroundBlockY = -999;
        clearAim();
        disablePlacing();
        resetPath();
        hitTriggered = false;
        lastHitTime = 0L;
        prevHurtTime = player.getHurtTime();
        lastFallDistance = player.getFallDistance();
        lastOnGround = player.onGround();
        lastFallDamageTime = 0L;
        groundTicksAfterHit = 0;
        clutchAttempts = 0;
        stopModeBlocked = false;
        lastGroundBlockY = -999;
        clutchOriginalSlot = -1;
        clutchEquippedBlockSlot = -1;
        clutchDoneGroundTicks = 0;
    } else {
        lastSeenTicks = currentTick;
    }

    if (currentTick < lastPlaceTick) {
        lastPlaceTick = -999;
    }

    int jamalbutoutsidelol = getMaxBlocks();
    int jamal2butoutsidelol = getDelay();
    int turnoffdascaffold = getToggleOffMode();
    int turnondascaffold = getToggleOnMode();

    int dist = fallDistance();
    int blinkDist = 50;
    long now = client.time();

    float currentFallDist = player.getFallDistance();
    int currentHurtTime = player.getHurtTime();
    boolean currentOnGround = player.onGround();

    // Accurately distinguish combat damage from fall damage
    boolean isNewDamage = (currentHurtTime > prevHurtTime && currentHurtTime >= 8);
    if (isNewDamage) {
        // Fall damage in Minecraft 1.8 ONLY happens when landing on ground after falling >= 3 blocks
        boolean isLandingDamage = (currentOnGround || lastOnGround) && (lastFallDistance >= 3.0f || currentFallDist >= 3.0f);
        if (isLandingDamage) {
            lastFallDamageTime = now;
            dbg("&7Landing fall damage detected (fell " + lastFallDistance + " blocks). Ignoring.");
        } else if (!fbed) {
            hitTriggered = true;
            lastHitTime = now;
            groundTicksAfterHit = 0;
            bridgeFailed = false;
            toolazy = false;
            turnoff = false;
            ToggleDelayB = false;
            ToggleDelayI = 0;
            maxdis = 0;
            dbg("&eCombat hit detected! Hit mode armed.");
        }
    }
    prevHurtTime = currentHurtTime;
    lastFallDistance = currentFallDist;
    lastOnGround = currentOnGround;

    // Disarm combat hit only after player stays stably on ground for > 12 ticks
    if (currentOnGround) {
        groundTicksAfterHit++;
        if (groundTicksAfterHit > 12) {
            hitTriggered = false;
        }
    } else {
        groundTicksAfterHit = 0;
    }

    // Determine if hit is active based on chosen Toggle on mode
    boolean hitActive = false;
    if (turnondascaffold == 0) {
        hitActive = true;
    } else {
        if (turnondascaffold == 2 && (fbed || now - lastFireballJumpTime < 5000L)) {
            hitActive = false;
        } else {
            hitActive = hitTriggered && (now - lastHitTime < 3500L);
        }
    }

    hurtTime = hitActive ? 1 : 0;

    ItemStack held = player.getHeldItem();
    float pitch = player.getPitch();

    // Max blocks check
    if (jamalbutoutsidelol != 0 && maxdis >= jamalbutoutsidelol) {
        if (isStopMode() && handleStopModeRetry(player, currentTick)) {
            // Attempt 2 started or final stop handled by Stop mode
        } else {
            falling = false;
            turnoff = true;
            toolazy = true;
        }
    }

    // Cooldown decrement (from 56jn26m.java lines 106-114)
    if (ToggleDelayB && ToggleDelayI > 0) {
        ToggleDelayI--;
        if (ToggleDelayI <= 1) {
            ToggleDelayB = false;
        }
    }

    // Ground reset (let Toggle off handle disarm according to chosen mode)
    if (player.onGround()) {
        int gy = (int) Math.floor(player.getPosition().y - 0.1);
        Block gb = world.getBlockAt((int) Math.floor(player.getPosition().x), gy, (int) Math.floor(player.getPosition().z));
        if (gb != null && !canPlaceThrough(gb.name)) {
            lastGroundBlockY = gy;
        }
        if (falling || clutchBlocksPlaced > 0) {
            lastClutchFinishTime = now;
            hitTriggered = false;
            lastHitTime = 0L;
        }
        toolazy = false;
        turnoff = false;
        ToggleDelayB = false;
        ToggleDelayI = 0;
        maxdis = 0;
        clutchBlocksPlaced = 0;
        clutchTargetY = -999;
        lowestBridgeY = -999;
        bridgeFailed = false;
        clutchAttempts = 0;
        stopModeBlocked = false;
        if (falling) {
            falling = false;
            disableClutch();
        }
    }

    // Fireball jump detection for "Void hit (ignore fb)" (mode 2)
    now = client.time();
    if (turnondascaffold == 2) {
        if (isFireballItem(held) && keybinds.isMouseDown(1)) {
            fbed = true;
            lastFireballJumpTime = now;
        }

        // Entity check: nearby fireball entity in the world
        try {
            List<Entity> ents = world.getEntities();
            if (ents != null) {
                Vec3 p = player.getPosition();
                for (int i = 0; i < ents.size(); i++) {
                    Entity ent = ents.get(i);
                    if (ent == null || ent.entityId == player.entityId) continue;
                    String ename = ent.getName();
                    if (ename != null && ename.toLowerCase().contains("fireball")) {
                        Vec3 ep = ent.getPosition();
                        double d2 = (ep.x - p.x)*(ep.x - p.x) + (ep.y - p.y)*(ep.y - p.y) + (ep.z - p.z)*(ep.z - p.z);
                        if (d2 < 64.0) {
                            lastNearFireballTime = now;
                            fbed = true;
                            lastFireballJumpTime = now;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Suppress damage and hit triggers while in a fireball jump
        if (fbed && (now - lastExplosionTime < 2000L || now - lastFireballJumpTime < 5000L)) {
            hurtTime = 0;
            hitTriggered = false;
        }

        // Reset fireball ignore state once safely on ground or after jump timeout
        if (player.onGround() || (now - lastFireballJumpTime > 5000L)) {
            fbed = false;
        }
    }

    // Air Limit check (with safety threshold so bridging is never killed after 2 ticks)
    int airLimit = getSliderInt("Air Limit");
    if (falling && airLimit > 0) {
        airTicksWithoutPlace++;
        int effectiveLimit = Math.max(airLimit, 25);
        if (airTicksWithoutPlace >= effectiveLimit) {
            if (isStopMode() && handleStopModeRetry(player, currentTick)) {
                // Attempt 2 started or final stop handled by Stop mode
            } else {
                falling = false;
                turnoff = true;
                dbg("&cAir limit reached (" + effectiveLimit + " ticks)! Forced off.");
            }
        }
    }

    // Turn off handling (from 56jn26m.java lines 149-280)
    if (turnoff) {
        if (turnoffdascaffold == 0) { // None
            disableClutch();
            if (keybinds.isMouseDown(1) && getButtonValue("Allow Placing")) {
                keybinds.setPressed("use", true);
            }
            turnoff = false;
            if (jamal2butoutsidelol > 1) {
                ToggleDelayI = jamal2butoutsidelol;
                ToggleDelayB = true;
            }
        } else if (turnoffdascaffold == 1) { // Movement
            if (player.onGround() && client.isMoving()) {
                disableClutch();
                if (keybinds.isMouseDown(1) && getButtonValue("Allow Placing")) {
                    keybinds.setPressed("use", true);
                }
                turnoff = false;
                if (jamal2butoutsidelol > 1) {
                    ToggleDelayI = jamal2butoutsidelol;
                    ToggleDelayB = true;
                }
            }
        } else if (turnoffdascaffold == 2) { // Forward
            if (player.onGround() && keybinds.isPressed("forward")) {
                disableClutch();
                if (keybinds.isMouseDown(1) && getButtonValue("Allow Placing")) {
                    keybinds.setPressed("use", true);
                }
                turnoff = false;
                if (jamal2butoutsidelol > 1) {
                    ToggleDelayI = jamal2butoutsidelol;
                    ToggleDelayB = true;
                }
            }
        } else if (turnoffdascaffold == 3) { // Ground
            if (player.onGround()) {
                disableClutch();
                if (keybinds.isMouseDown(1) && getButtonValue("Allow Placing")) {
                    keybinds.setPressed("use", true);
                }
                turnoff = false;
                if (jamal2butoutsidelol > 1) {
                    ToggleDelayI = jamal2butoutsidelol;
                    ToggleDelayB = true;
                }
            }
        } else if (turnoffdascaffold == 4) { // Ground (delay)
            if (player.onGround()) {
                if (!Groundb) {
                    GroundI = 20;
                    Groundb = true;
                }
            } else {
                if (!keybinds.isPressed("jump")) {
                    Groundb = false;
                }
            }

            if (Groundb && GroundI > 0) {
                GroundI--;
            }

            if (GroundI == 0) {
                Groundb = false;
                disableClutch();
                if (keybinds.isMouseDown(1) && getButtonValue("Allow Placing")) {
                    keybinds.setPressed("use", true);
                }
                turnoff = false;
                if (jamal2butoutsidelol > 1) {
                    ToggleDelayI = jamal2butoutsidelol;
                    ToggleDelayB = true;
                }
            }
        } else if (turnoffdascaffold == 5) { // Ground + Forward
            if (player.onGround() && keybinds.isPressed("forward")) {
                disableClutch();
                if (keybinds.isMouseDown(1) && getButtonValue("Allow Placing")) {
                    keybinds.setPressed("use", true);
                }
                turnoff = false;
                if (jamal2butoutsidelol > 1) {
                    ToggleDelayI = jamal2butoutsidelol;
                    ToggleDelayB = true;
                }
            }
        }
    }

    // Toggle on == 0 means "Void" (no hit needed):
    if (turnondascaffold == 0) {
        hurtTime = 1;
    }

    double maxFallVal = getSliderValue("Max Fall");
    boolean isOverVoidOrFar = (dist == -1) || (maxFallVal > 0 && dist > (int) maxFallVal);

    if (wasSneakingAntiSlip) {
        keybinds.setPressed("sneak", false);
        wasSneakingAntiSlip = false;
    }

    // Prediction (Recovery Check & Predicted Safe): if jump trajectory naturally lands safely on a solid platform, skip clutching!
    boolean shouldSkip = false;
    if (getButtonValue("Predicted Safe") && isPredictedSafe(player)) {
        shouldSkip = true;
    }
    if (!shouldSkip && getButtonValue("Recovery Check") && canRecoverToPlatform(player)) {
        shouldSkip = true;
    }

    // Stasis Option: rescan ground status and retry clutching if suspended in air
    boolean isStasisOn = isStasisModeActive();
    boolean supportedOnGround = player.onGround() || hasSolidBlockDirectlyUnder(player);

    if (isStasisOn) {
        if (supportedOnGround) {
            // Safely disarm once supported on solid ground or placed block directly below
            if (falling) {
                falling = false;
                turnoff = false;
                clutchAttempts = 0;
                disablePlacing();
                resetPath();
                if (aimLingerTicks <= 0) {
                    clearAim();
                }
            }
        } else if (!shouldSkip) {
            // Player is in Stasis and NOT on ground and NOT recovering -> retry clutch!
            // Respect "Void hit" mode: do not force hitActive if a combat hit is required but player was not hit!
            boolean hitMode = (turnondascaffold != 0);
            boolean ongoingClutch = (clutchOriginalSlot != -1) || (clutchBlocksPlaced > 0) || falling;
            if (!hitMode || hitActive || ongoingClutch) {
                if (ongoingClutch) {
                    hitActive = true;
                }
                toolazy = false;
                turnoff = false;
                ToggleDelayB = false;
                ToggleDelayI = 0;
                bridgeFailed = false;
                clutchCooldownUntil = 0L;
                if (!falling) {
                    maxdis = 0;
                    airTicksWithoutPlace = 0;
                    clutchAttempts = 0;
                    stopModeBlocked = false;
                }
            }
        }
    } else {
        // Fallback completion if Stasis button is off
        if (falling && (isStasisActive() || isPlayerStationary(player)) && hasSolidBlockDirectlyUnder(player)) {
            falling = false;
            turnoff = false;
            clutchAttempts = 0;
            disableClutch();
            clearAim();
            resetPath();
        }
    }

    if (shouldSkip && isOverVoidOrFar && !player.onGround() && !hasSolidBelow(player.getPosition())) {
        dbg("&bSafe jump predicted! Clutch suppressed.");
    }

    // Stop mode safety catch: if attempt 1 stopped while player is still falling over void, trigger attempt 2
    if (isStopMode() && clutchAttempts == 1 && !falling && isOverVoidOrFar && !player.onGround() && !hasSolidBelow(player.getPosition())) {
        handleStopModeRetry(player, currentTick);
    }

    // Trigger check (from 56jn26m.java line 288)
    if (hitActive && !toolazy && !ToggleDelayB && !falling && !turnoff && jumpticks-- <= 0 && !client.isFlying() && (isOverVoidOrFar || isStasisOn) && !player.onGround() && !hasSolidBelow(player.getPosition()) && !fbed && !shouldSkip) {
        if (!isStopMode() || !stopModeBlocked) {
            falling = true;
            if (clutchOriginalSlot == -1) {
                clutchOriginalSlot = inventory.getSlot();
            }
            if (isStopMode()) {
                clutchAttempts = 1;
            }
            int feetY = (int) Math.floor(player.getPosition().y);
            if (isAntiTower() && lastGroundBlockY != -999 && feetY >= lastGroundBlockY && player.getPosition().y >= lastGroundBlockY + 0.85) {
                clutchTargetY = lastGroundBlockY;
            } else {
                clutchTargetY = feetY - 1;
            }
            hurtTime = 0;
            hitTriggered = false;
            lastHitTime = 0L;
            turnoff = false;
            airTicksWithoutPlace = 0;
            dbg("&aClutch triggered! (Toggle on: " + onmode[turnondascaffold] + ")");
        }
    } else if (falling && clutchBlocksPlaced == 0) {
        int feetY = (int) Math.floor(player.getPosition().y);
        if (isAntiTower() && lastGroundBlockY != -999 && feetY >= lastGroundBlockY && player.getPosition().y >= lastGroundBlockY + 0.85) {
            clutchTargetY = lastGroundBlockY;
        } else {
            clutchTargetY = feetY - 1;
        }
    }
    else if (falling && player.getFallDistance() > blinkDist && dist == -1 && !john) {
        john = true;
        turnoff = true;
    }
    else if (falling && player.onGround()) {
        int gy = (int) Math.floor(player.getPosition().y - 0.1);
        Block gb = world.getBlockAt((int) Math.floor(player.getPosition().x), gy, (int) Math.floor(player.getPosition().z));
        if (gb != null && !canPlaceThrough(gb.name)) {
            lastGroundBlockY = gy;
        }
        falling = false;
        john = false;
        turnoff = false;
        clutchAttempts = 0;
        stopModeBlocked = false;
        disableClutch();
        hitTriggered = false;
        lastHitTime = 0L;
        if (wasSneakingAntiSlip) {
            keybinds.setPressed("sneak", false);
            wasSneakingAntiSlip = false;
        }
    }
    // Only disable clutch when player actually lands on solid ground (handled above)

    if (placeQueued && hitAt != null && hitSide != null && placeAt != null) {
        currentTick = player != null ? player.getTicksExisted() : 0;
        if (currentTick - lastPlaceTick >= BLOCK_DELAY) {
            placeQueued = false;
            if (player != null) {
                int maxAllowedY = (int) Math.floor(player.getPosition().y - 0.01);
                int placeY = (int) Math.floor(placeAt.y);
                if (placeY > maxAllowedY || (clutchTargetY != -999 && placeY > clutchTargetY)) {
                    targetHitPos = null;
                    targetSide = "";
                    return;
                }
            }
            if (client.placeBlock(hitAt, hitSide, placeAt)) {
                clutchBlocksPlaced++;
                maxdis++;
                lastPlacedX = (int) Math.floor(hitAt.x);
                lastPlacedY = (int) Math.floor(hitAt.y);
                lastPlacedZ = (int) Math.floor(hitAt.z);
                lastPlaceTick = currentTick;
                airTicksWithoutPlace = 0;
                client.swing();
                dbg("&aPlaced block (queued) on &b" + hitSide);
                onBlockSuccessfullyPlaced(hitAt, hitSide);
            }
        }
    }

    Vec3 curMotion = client.getMotion();
    if (curMotion != null) {
        prevMotionX = curMotion.x;
        prevMotionZ = curMotion.z;
    }

    checkClutchDoneAndRestoreSlot(player);
}

   public boolean onChat(String message, int type) {
    if (message != null && message.contains("Applied velocity")) {
        hitTriggered = true;
        lastHitTime = client.time();
        groundTicksAfterHit = 0;
        bridgeFailed = false;
        toolazy = false;
        turnoff = false;
        ToggleDelayB = false;
        ToggleDelayI = 0;
        maxdis = 0;
        dbg("&eACTest velocity detected! Hit mode armed.");
    }
    return true;
}

   public boolean onPacketReceived(SPacket packet) {
    if (packet != null) {
        int turnondascaffold = getToggleOnMode();
        long now = client.time();
        boolean nearFb = (now - lastNearFireballTime < 1500L) || (now - lastExplosionTime < 1000L) || fbed;

        if (packet.name != null && (packet.name.contains("Explosion") || packet.name.startsWith("S27"))) {
            lastExplosionTime = now;
            if (turnondascaffold == 2) {
                fbed = true;
                lastFireballJumpTime = now;
                dbg("&eFireball explosion ignored (Void hit ignore fb).");
            } else {
                hitTriggered = true;
                lastHitTime = now;
                groundTicksAfterHit = 0;
                bridgeFailed = false;
                toolazy = false;
                turnoff = false;
                ToggleDelayB = false;
                ToggleDelayI = 0;
                maxdis = 0;
            }
        }

        if (packet instanceof S12) {
            S12 s12 = (S12) packet;
            Entity me = client.getPlayer();
            if (me != null && s12.entityId == me.entityId) {
                if (turnondascaffold == 2 && (nearFb || fbed)) {
                    dbg("&eFireball velocity ignored.");
                } else {
                    hitTriggered = true;
                    lastHitTime = now;
                    groundTicksAfterHit = 0;
                    bridgeFailed = false;
                    toolazy = false;
                    turnoff = false;
                    ToggleDelayB = false;
                    ToggleDelayI = 0;
                    maxdis = 0;
                    dbg("&eReceived velocity packet for local player! Hit mode armed.");
                }
            }
        }
    }
    return true;
}

   public boolean onPacketSent(CPacket packet) {
    if (packet instanceof C03) {
        C03 c03 = (C03) packet;
        if (c03.name.startsWith("C05") || c03.name.startsWith("C06")) {
            serverYaw = c03.yaw;
            serverPitch = c03.pitch;
        }
    }
    return true;
}

   public boolean onMouse(int button, boolean state) {
    if (placing && button > -1) return false;
    return true;
}

boolean isOverVoidAt(double x, double z, int startY) {
    if (startY < 0) return true;
    int px = (int) Math.floor(x);
    int pz = (int) Math.floor(z);
    for (int y = startY; y >= 0; y--) {
        Block blk = world.getBlockAt(px, y, pz);
        if (blk != null && !canPlaceThrough(blk.name)) {
            return false;
        }
    }
    return true;
}


boolean isOverVoid() {
    Entity me = client.getPlayer();
    if (me == null) return false;
    Vec3 p = me.getPosition();
    int startY = (int) Math.floor(p.y);
    if (startY < 0) return true;

    int px = (int) Math.floor(p.x);
    int pz = (int) Math.floor(p.z);

    for (int y = startY; y >= 0; y--) {
        Block blk = world.getBlockAt(px, y, pz);
        if (blk != null && !canPlaceThrough(blk.name)) {
            return false;
        }
    }
    return true;
}

void enablePlacing() {
    if (placing) return;
    placing = true;
    slotWasSwapped = false;
    prevSlot = inventory.getSlot();
    if (clutchOriginalSlot == -1) {
        clutchOriginalSlot = prevSlot;
    }
    // AutoClutch no longer turns the AutoClicker module off during placement (Sam: stop toggling it).
    autoClickerWasOn = false;
}

void disablePlacing() {
    if (!placing) return;

    placing = false;
    slotWasSwapped = false;
    prevSlot = -1;
    plannedSlot = -1;

    if (client.getScreen().isEmpty()) {
        keybinds.setPressed("attack", keybinds.isMouseDown(0));
        keybinds.setPressed("use", keybinds.isMouseDown(1));
    }
    // AutoClicker is left untouched here too (AutoClutch never disables/re-enables it).
    autoClickerWasOn = false;
}

void checkClutchDoneAndRestoreSlot(Entity player) {
    if (player == null) return;

    Vec3 p = player.getPosition();
    if (p == null || p.y < 0) {
        clutchOriginalSlot = -1;
        clutchEquippedBlockSlot = -1;
        clutchDoneGroundTicks = 0;
        return;
    }

    boolean supported = player.onGround() || hasSolidBlockDirectlyUnder(player);

    if (supported && !falling) {
        clutchDoneGroundTicks++;
    } else {
        clutchDoneGroundTicks = 0;
    }

    if (clutchOriginalSlot != -1) {
        boolean lingerDone = (aimLingerTicks <= 0);
        boolean notPlacing = (!placing && !placeQueued && targetHitPos == null);
        int requiredGroundTicks = getButtonValue("Legit Mode") ? 2 : 1;

        if (supported && !falling && lingerDone && notPlacing && clutchDoneGroundTicks >= requiredGroundTicks) {
            int cur = inventory.getSlot();
            if (clutchEquippedBlockSlot == -1 || cur == clutchEquippedBlockSlot) {
                if (clutchOriginalSlot >= 0 && clutchOriginalSlot <= 8 && clutchOriginalSlot != cur) {
                    inventory.setSlot(clutchOriginalSlot);
                    dbg("&aFully done clutching! Swapped back to slot " + (clutchOriginalSlot + 1));
                }
            }
            clutchOriginalSlot = -1;
            clutchEquippedBlockSlot = -1;
            clutchDoneGroundTicks = 0;
        }
    }
}

void resetPath() {
    hasActivePath = false;
    if (currentPath != null) currentPath.clear();
    pathSide = "";
    pathHead = null;
    pathFailTicks = 0;
}

String getFacingDirection() {
    Entity me = client.getPlayer();
    if (me != null) {
        double mx = me.getPosition().x - me.getLastPosition().x;
        double mz = me.getPosition().z - me.getLastPosition().z;
        if (Math.abs(mx) > 0.05 || Math.abs(mz) > 0.05) {
            if (Math.abs(mx) > Math.abs(mz)) {
                return mx > 0 ? "EAST" : "WEST";
            } else {
                return mz > 0 ? "SOUTH" : "NORTH";
            }
        }
        float yaw = normYaw(me.getYaw());
        if (yaw >= -45f && yaw < 45f) return "SOUTH";
        if (yaw >= 45f && yaw < 135f) return "WEST";
        if (yaw >= -135f && yaw < -45f) return "EAST";
        return "NORTH";
    }
    return "SOUTH";
}

void onBlockSuccessfullyPlaced(Vec3 hit, String side) {
    Vec3 placedBlock = offsetByFace(hit, side);
    recordPlacedBlockForRender(placedBlock);
    renderBlockPos = null;

    int placedLinger = getSliderInt("Aim Linger");
    aimLingerTicks = placedLinger > 0 ? placedLinger : 0;

    Entity placedMe = client.getPlayer();
    if (placedMe != null && (isStasisActive() || isPlayerStationary(placedMe)) && hasSolidBlockDirectlyUnder(placedMe)) {
        falling = false;
        turnoff = false;
        clutchAttempts = 0;
        disablePlacing();
        resetPath();
        targetHitPos = null;
        targetSide = "";
        if (aimLingerTicks <= 0) {
            clearAim();
        }
        return;
    }
    int pby = (int) Math.floor(placedBlock.y);
    if (lowestBridgeY == -999 || pby < lowestBridgeY) {
        lowestBridgeY = pby;
    }
    if (clutchTargetY == -999 || clutchBlocksPlaced == 1) {
        clutchTargetY = pby;
        lastGroundBlockY = pby;
    } else if (!isAntiTower() && pby < clutchTargetY) {
        clutchTargetY = pby;
        lastGroundBlockY = pby;
    }
    Entity me = client.getPlayer();
    if (me != null && me.onGround() && hasSolidBelow(me.getPosition())) {
        resetPath();
        targetHitPos = null;
        targetSide = "";
        return;
    }
    pathHead = placedBlock;
    if (!hasActivePath || pathSide == null || pathSide.isEmpty()) {
        if (!"UP".equals(side) && !"DOWN".equals(side)) {
            pathSide = side;
        } else {
            pathSide = getFacingDirection();
        }
    }
    hasActivePath = true;
    pathFailTicks = 0;

    boolean alreadyInPath = false;
    for (int i = 0; i < currentPath.size(); i++) {
        if (matchesBlock(currentPath.get(i), placedBlock)) {
            alreadyInPath = true;
            break;
        }
    }
    if (!alreadyInPath) {
        currentPath.add(placedBlock);
        if (currentPath.size() > 20) {
            currentPath.remove(0);
        }
    }

    targetHitPos = placedBlock;
    targetSide = pathSide;
}

void clearAim() {
    if (!falling) {
        clutchTargetY = -999;
    }
    renderBlockPos = null;
    targetHitPos = null;
    targetSide = "";
    lastPlacedX = lastPlacedY = lastPlacedZ = -999;
    resetPath();
    aimLingerTicks = 0;
    if (hasAim) resetting = true;
    hasAim = false;
}

// Midpoint samples from blockplacements.java for visible point search on a block face
final double[] FACE_SAMPLE_OFFSETS = {
    0.03125D, 0.09375D, 0.15625D, 0.21875D,
    0.28125D, 0.34375D, 0.40625D, 0.46875D,
    0.53125D, 0.59375D, 0.65625D, 0.71875D,
    0.78125D, 0.84375D, 0.90625D, 0.96875D
};

double targetDistanceSquared(Vec3 desired, Vec3 candidate) {
    double dx = (candidate.x + 0.5) - (desired.x + 0.5);
    double dy = (candidate.y + 0.5) - (desired.y + 0.5);
    double dz = (candidate.z + 0.5) - (desired.z + 0.5);
    return dx * dx + dy * dy + dz * dz;
}

Vec3 getDesiredPlacementBlock(Entity me, int targetY) {
    if (me == null) return new Vec3(0, targetY, 0);
    Vec3 p = me.getPosition();
    int bx = (int) Math.floor(p.x);
    int bz = (int) Math.floor(p.z);

    Block curBlock = world.getBlockAt(bx, targetY, bz);
    if (curBlock == null || canPlaceThrough(curBlock.name)) {
        return new Vec3(bx, targetY, bz);
    }

    // In Stasis or stationary, NEVER bridge forward around the player!
    if (isStasisActive() || isPlayerStationary(me)) {
        return new Vec3(bx, targetY, bz);
    }

    double mx = p.x - me.getLastPosition().x;
    double mz = p.z - me.getLastPosition().z;
    Vec3 curMot = client.getMotion();
    if (curMot != null && Math.hypot(curMot.x, curMot.z) > Math.hypot(mx, mz)) {
        mx = curMot.x;
        mz = curMot.z;
    }

    if (Math.abs(mx) < 0.02 && Math.abs(mz) < 0.02) {
        float moveForward = 0f;
        float moveStrafe = 0f;
        if (keybinds.isPressed("forward")) moveForward += 1f;
        if (keybinds.isPressed("back")) moveForward -= 1f;
        if (keybinds.isPressed("left")) moveStrafe += 1f;
        if (keybinds.isPressed("right")) moveStrafe -= 1f;
        if (moveForward != 0f || moveStrafe != 0f) {
            float moveYaw = me.getYaw();
            if (moveForward < 0f) moveYaw += 180f;
            float fwd = 1f;
            if (moveForward < 0f) fwd = -0.5f;
            else if (moveForward > 0f) fwd = 0.5f;
            if (moveStrafe > 0f) moveYaw -= 90f * fwd;
            if (moveStrafe < 0f) moveYaw += 90f * fwd;
            double rad = Math.toRadians(moveYaw);
            mx = -Math.sin(rad) * 0.3;
            mz = Math.cos(rad) * 0.3;
        } else {
            return new Vec3(bx, targetY, bz);
        }
    }

    double h = Math.hypot(mx, mz);
    if (h > 0.0001) {
        double nx = mx / h;
        double nz = mz / h;
        for (double step = 0.8; step <= 3.5; step += 0.5) {
            int fx = (int) Math.floor(p.x + nx * step);
            int fz = (int) Math.floor(p.z + nz * step);
            Block fb = world.getBlockAt(fx, targetY, fz);
            if (fb == null || canPlaceThrough(fb.name)) {
                return new Vec3(fx, targetY, fz);
            }
        }
    }

    return new Vec3(bx, targetY, bz);
}

boolean isWithinClutchCorridor(Vec3 placedBlock, Vec3 playerPos) {
    if (placedBlock == null || playerPos == null) return false;
    double dx = (placedBlock.x + 0.5) - playerPos.x;
    double dz = (placedBlock.z + 0.5) - playerPos.z;
    double distSq = dx * dx + dz * dz;
    return distSq <= (REACH + 0.8) * (REACH + 0.8);
}

String findConnectingFace(Vec3 supportBlock, Vec3 desiredPlacementBlock) {
    String bestFace = null;
    double bestDistanceSquared = 0.0;
    String[] candidateFaces = {"DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST"};

    Entity me = client.getPlayer();
    double mx = 0, mz = 0;
    int feetY = (int) Math.floor(supportBlock.y);
    if (me != null) {
        mx = me.getPosition().x - me.getLastPosition().x;
        mz = me.getPosition().z - me.getLastPosition().z;
        feetY = (int) Math.floor(me.getPosition().y);
    }

    for (int i = 0; i < candidateFaces.length; i++) {
        String candidateFace = candidateFaces[i];
        if ("DOWN".equals(candidateFace) && supportBlock.y <= desiredPlacementBlock.y) continue;

        Vec3 neighborBlock = offsetByFace(supportBlock, candidateFace);

        int nby = (int) Math.floor(neighborBlock.y);
        int sby = (int) Math.floor(supportBlock.y);

        if (me != null && !isWithinClutchCorridor(neighborBlock, me.getPosition())) {
            continue;
        }

        // NEVER place blocks above player's Y level
        if (me != null) {
            int maxAllowedBlockY = (int) Math.floor(me.getPosition().y - 0.01);
            if (nby > maxAllowedBlockY) continue;
            if ("UP".equals(candidateFace) && sby >= maxAllowedBlockY) continue;
        }

        // Anti tower / clutch target: never place above clutchTargetY
        if (clutchTargetY != -999) {
            if (nby > clutchTargetY) continue;
            if ("UP".equals(candidateFace) && sby >= clutchTargetY) continue;
            if (isAntiTower() && clutchBlocksPlaced > 0 && nby != clutchTargetY) continue;
        } else if (isAntiTower()) {
            int lockY = (lastGroundBlockY != -999 && feetY >= lastGroundBlockY && me != null && me.getPosition().y >= lastGroundBlockY + 0.85) ? lastGroundBlockY : (feetY - 1);
            if (nby > lockY) continue;
            if ("UP".equals(candidateFace) && sby >= lockY) continue;
        }

        Block nb = world.getBlockAt((int) Math.floor(neighborBlock.x), (int) Math.floor(neighborBlock.y), (int) Math.floor(neighborBlock.z));
        if (nb != null && !canPlaceThrough(nb.name)) {
            continue;
        }

        double distanceSquared = targetDistanceSquared(desiredPlacementBlock, neighborBlock);

        if (Math.abs(mx) > Math.abs(mz)) {
            if ((mx > 0 && "EAST".equals(candidateFace)) || (mx < 0 && "WEST".equals(candidateFace))) {
                distanceSquared -= 0.01;
            }
        } else if (Math.abs(mz) > 0.001) {
            if ((mz > 0 && "SOUTH".equals(candidateFace)) || (mz < 0 && "NORTH".equals(candidateFace))) {
                distanceSquared -= 0.01;
            }
        }

        if (bestFace == null || distanceSquared < bestDistanceSquared) {
            bestDistanceSquared = distanceSquared;
            bestFace = candidateFace;
        }
    }

    return bestFace;
}

float clampAngleChange(float angleDifference, float maxChange) {
    return Math.max(-maxChange, Math.min(maxChange, angleDifference));
}

float snapToMouseSensitivity(float targetAngle, float previousAngle) {
    float delta = targetAngle - previousAngle;
    float gcd = (float) SENS_GCD;
    return previousAngle + Math.round(delta / gcd) * gcd;
}

float[] rotationsFromDelta(double deltaX, double deltaY, double deltaZ) {
    double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX))) - 90.0F;
    float pitch = (float) (-Math.toDegrees(Math.atan2(deltaY, distanceXZ)));
    return new float[]{ normYaw(yaw), clampPitch(pitch) };
}

Object[] findBestPlacementHit(ItemStack held, Vec3 block, String face, Vec3 eye, double reach, Vec3 playerPos, float previousYaw, float previousPitch) {
    if (block == null || face == null || face.isEmpty()) return null;

    Vec3 placedPos = offsetByFace(block, face);

    int pby = (int) Math.floor(placedPos.y);
    int by = (int) Math.floor(block.y);
    int feetY = (int) Math.floor(playerPos.y);

    // NEVER place blocks above player's Y level
    int maxAllowedBlockY = (int) Math.floor(playerPos.y - 0.01);
    if (pby > maxAllowedBlockY) return null;
    if ("UP".equals(face) && by >= maxAllowedBlockY) return null;

    // Strict clutch plane check: never place above clutchTargetY
    if (clutchTargetY != -999) {
        if (pby > clutchTargetY) return null;
        if ("UP".equals(face) && by >= clutchTargetY) return null;
        if (isAntiTower() && clutchBlocksPlaced > 0 && pby != clutchTargetY) return null;
    } else if (isAntiTower()) {
        int lockY = (lastGroundBlockY != -999 && feetY >= lastGroundBlockY && playerPos.y >= lastGroundBlockY + 0.85) ? lastGroundBlockY : (feetY - 1);
        if (pby > lockY) return null;
        if ("UP".equals(face) && by >= lockY) return null;
    }

    if (!isWithinClutchCorridor(placedPos, playerPos)) return null;

    Block existing = world.getBlockAt((int) Math.floor(placedPos.x), (int) Math.floor(placedPos.y), (int) Math.floor(placedPos.z));
    if (existing != null && !canPlaceThrough(existing.name)) return null;
    if (doesIntersectPlayer(placedPos, playerPos)) return null;

    double[] hitOffsetsX = FACE_SAMPLE_OFFSETS;
    double[] hitOffsetsY = FACE_SAMPLE_OFFSETS;
    double[] hitOffsetsZ = FACE_SAMPLE_OFFSETS;

    if ("NORTH".equals(face)) {
        hitOffsetsZ = new double[]{0.0};
    } else if ("EAST".equals(face)) {
        hitOffsetsX = new double[]{1.0};
    } else if ("SOUTH".equals(face)) {
        hitOffsetsZ = new double[]{1.0};
    } else if ("WEST".equals(face)) {
        hitOffsetsX = new double[]{0.0};
    } else if ("DOWN".equals(face)) {
        hitOffsetsY = new double[]{0.0};
    } else if ("UP".equals(face)) {
        hitOffsetsY = new double[]{1.0};
    }

    float bestYaw = -180.0F;
    float bestPitch = 0.0F;
    float bestRotationCost = 999999.0F;
    Object[] bestRay = null;

    for (int ix = 0; ix < hitOffsetsX.length; ix++) {
        double hitOffsetX = hitOffsetsX[ix];
        for (int iy = 0; iy < hitOffsetsY.length; iy++) {
            double hitOffsetY = hitOffsetsY[iy];
            for (int iz = 0; iz < hitOffsetsZ.length; iz++) {
                double hitOffsetZ = hitOffsetsZ[iz];

                double deltaX = block.x + hitOffsetX - eye.x;
                double deltaY = block.y + hitOffsetY - eye.y;
                double deltaZ = block.z + hitOffsetZ - eye.z;

                float[] rots = rotationsFromDelta(deltaX, deltaY, deltaZ);
                rots[0] = snapToMouseSensitivity(rots[0], previousYaw);
                rots[1] = snapToMouseSensitivity(rots[1], previousPitch);
                rots[1] = clampPitch(rots[1]);

                float yawDiff = Math.abs(wrapYawDelta(previousYaw, rots[0]));
                float pitchDiff = Math.abs(rots[1] - previousPitch);
                float rotationCost = yawDiff + pitchDiff;

                if (bestRay != null && rotationCost >= bestRotationCost) {
                    continue;
                }

                Object[] ray = client.raycastBlock(reach, rots[0], rots[1]);
                if (ray == null) continue;

                Vec3 hit = (Vec3) ray[0];
                String sideHit = (String) ray[2];
                if (!matchesBlock(hit, block) || !face.equals(sideHit)) {
                    continue;
                }

                bestYaw = rots[0];
                bestPitch = rots[1];
                bestRotationCost = rotationCost;
                bestRay = ray;
            }
        }
    }

    if (bestRay != null) {
        return new Object[]{ bestRay, bestYaw, bestPitch };
    }
    return null;
}

Object[] clutchAim() {
    Entity me = client.getPlayer();
    if (me == null) return null;
    Vec3 p = me.getPosition();
    Vec3 eye = p.offset(0, me.getEyeHeight(), 0);
    double reach = REACH;
    ItemStack held = inventory.getStackInSlot(plannedSlot);

    // If in Stasis or stationary and already supported by a block directly beneath, do not place anything!
    if ((isStasisActive() || isPlayerStationary(me)) && hasSolidBlockDirectlyUnder(me)) {
        return null;
    }

    int feetY = (int) Math.floor(p.y);
    int targetY = (clutchTargetY != -999 ? clutchTargetY : feetY - 1);
    if (!isAntiTower()) {
        if (feetY - 1 < targetY) {
            targetY = feetY - 1;
            clutchTargetY = targetY;
        }
    } else {
        if (clutchBlocksPlaced == 0) {
            if (lastGroundBlockY != -999 && feetY >= lastGroundBlockY && p.y >= lastGroundBlockY + 0.85) {
                targetY = lastGroundBlockY;
                clutchTargetY = targetY;
            } else {
                targetY = feetY - 1;
                clutchTargetY = targetY;
            }
        } else if (clutchTargetY != -999) {
            targetY = clutchTargetY;
        }
    }
    Vec3 desiredBlock = getDesiredPlacementBlock(me, targetY);

    if (hasActivePath && !currentPath.isEmpty()) {
        if (me.onGround() && hasSolidBelow(p)) {
            resetPath();
        } else {
            if (pathHead != null) {
                Block blk = world.getBlockAt((int) Math.floor(pathHead.x), (int) Math.floor(pathHead.y), (int) Math.floor(pathHead.z));
                if (blk != null && !canPlaceThrough(blk.name)) {
                    double d2 = dist2PointAABB(p, pathHead);
                    if (d2 <= (reach + 1.2) * (reach + 1.2)) {
                        boolean underPlayer = isBlockUnderPlayer(pathHead, p);
                        String faceToDesired = findConnectingFace(pathHead, desiredBlock);
                        Object[] res = getBestRotationsToBlock(held, pathHead, eye, reach, underPlayer, p, targetY, faceToDesired);
                        if (res != null) {
                            pathFailTicks = 0;
                            return res;
                        }
                    }
                }
            }

            for (int i = currentPath.size() - 1; i >= 0; i--) {
                Vec3 pb = currentPath.get(i);
                if (pathHead != null && matchesBlock(pb, pathHead)) continue;
                Block blk = world.getBlockAt((int) Math.floor(pb.x), (int) Math.floor(pb.y), (int) Math.floor(pb.z));
                if (blk == null || canPlaceThrough(blk.name)) continue;

                double d2 = dist2PointAABB(p, pb);
                if (d2 > (reach + 1.2) * (reach + 1.2)) continue;

                boolean underPlayer = isBlockUnderPlayer(pb, p);
                String faceToDesired = findConnectingFace(pb, desiredBlock);
                Object[] res = getBestRotationsToBlock(held, pb, eye, reach, underPlayer, p, targetY, faceToDesired);
                if (res != null) {
                    pathFailTicks = 0;
                    return res;
                }
            }

            // Path failed this tick; reset path and fall through to scan candidate blocks in world
            resetPath();
        }
    }

    if (targetHitPos != null && targetSide != null && !targetSide.isEmpty()) {
        Block tb = world.getBlockAt((int) Math.floor(targetHitPos.x), (int) Math.floor(targetHitPos.y), (int) Math.floor(targetHitPos.z));
        if (tb != null && !canPlaceThrough(tb.name)) {
            double d2 = dist2PointAABB(p, targetHitPos);
            if (d2 <= (reach + 1.2) * (reach + 1.2)) {
                boolean underPlayer = isBlockUnderPlayer(targetHitPos, p);
                Object[] res = getBestRotationsToBlock(held, targetHitPos, eye, reach, underPlayer, p, targetY, targetSide);
                if (res != null) {
                    return res;
                }
            }
        }
    }

    int feetX = (int) Math.floor(p.x), feetZ = (int) Math.floor(p.z);
    int searchR = (int) Math.ceil(reach + 1.5);
    int minX = feetX - searchR, maxX = feetX + searchR;
    int minZ = feetZ - searchR, maxZ = feetZ + searchR;
    int maxY = Math.min(feetY + 2, 255);
    int minY = Math.max(0, feetY - searchR);

    ArrayList<Object[]> cands = new ArrayList<>();
    for (int y = maxY; y >= minY; y--) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Block blk = world.getBlockAt(x, y, z);
                if (blk == null || canPlaceThrough(blk.name)) continue;

                Vec3 b = new Vec3(x, y, z);
                double currentDist = dist2PointAABB(p, b);
                if (currentDist > (reach + 1.2) * (reach + 1.2)) continue;

                if (isAntiTower() && clutchBlocksPlaced > 0 && clutchTargetY != -999) {
                    if (b.y < clutchTargetY - 2 || b.y > clutchTargetY + 2) continue;
                }

                // Candidate distance metric from blockplacements.java
                double score = targetDistanceSquared(desiredBlock, b);

                if (isAntiTower() && clutchTargetY != -999) {
                    if (b.y == clutchTargetY) {
                        score -= 20.0;
                    }
                }

                cands.add(new Object[]{ score, b });
            }
        }
    }

    cands.sort((a, b2) -> Double.compare(((Number) a[0]).doubleValue(), ((Number) b2[0]).doubleValue()));

    for (int i = 0; i < cands.size(); i++) {
        Vec3 b = (Vec3) cands.get(i)[1];
        boolean underPlayer = isBlockUnderPlayer(b, p);
        Object[] res = getBestRotationsToBlock(held, b, eye, reach, underPlayer, p, targetY, null);
        if (res != null) return res;
    }

    return null;
}

boolean isBlockUnderPlayer(Vec3 b, Vec3 pos) {
    if (b.y > (int) Math.floor(pos.y + 0.2)) return false;
    for (double[] c : CORNERS) {
        int cx = (int) Math.floor(pos.x + c[0]);
        int cz = (int) Math.floor(pos.z + c[1]);
        if ((int) b.x == cx && (int) b.z == cz) return true;
    }
    return false;
}

Object[] getBestRotationsToBlock(ItemStack held, Vec3 b, Vec3 eye, double reach, boolean underPlayer, Vec3 playerPos, int targetY, String preferredSide) {
    int feetY = (int) Math.floor(playerPos.y);
    Entity me = client.getPlayer();
    Vec3 desiredBlock = me != null ? getDesiredPlacementBlock(me, targetY) : new Vec3((int) Math.floor(playerPos.x), targetY, (int) Math.floor(playerPos.z));

    // 1. Try preferredSide first if provided
    if (preferredSide != null && !preferredSide.isEmpty()) {
        Object[] res = findBestPlacementHit(held, b, preferredSide, eye, reach, playerPos, serverYaw, serverPitch);
        if (res != null) return res;
    }

    // 2. Try connectingFace towards desiredBlock
    String connectingFace = findConnectingFace(b, desiredBlock);
    if (connectingFace != null && !connectingFace.equals(preferredSide)) {
        Object[] res = findBestPlacementHit(held, b, connectingFace, eye, reach, playerPos, serverYaw, serverPitch);
        if (res != null) return res;
    }

    // 3. Fallback: test all valid placement faces of block b, prioritized by proximity to desired placement
    String[] allFaces = {"UP", "NORTH", "SOUTH", "EAST", "WEST", "DOWN"};
    ArrayList<Object[]> validFaces = new ArrayList<>();
    for (int fi = 0; fi < allFaces.length; fi++) {
        String face = allFaces[fi];
        if (face.equals(preferredSide) || face.equals(connectingFace)) continue;
        Vec3 placedPos = offsetByFace(b, face);

        Block existing = world.getBlockAt((int) Math.floor(placedPos.x), (int) Math.floor(placedPos.y), (int) Math.floor(placedPos.z));
        if (existing != null && !canPlaceThrough(existing.name)) continue;

        int pby = (int) Math.floor(placedPos.y);
        int by = (int) Math.floor(b.y);

        // NEVER place blocks above player's Y level
        int maxAllowedBlockY = (int) Math.floor(playerPos.y - 0.01);
        if (pby > maxAllowedBlockY) continue;
        if ("UP".equals(face) && by >= maxAllowedBlockY) continue;

        // Strict clutch plane check: never place above clutchTargetY
        if (clutchTargetY != -999) {
            if (pby > clutchTargetY) continue;
            if ("UP".equals(face) && by >= clutchTargetY) continue;
            if (isAntiTower() && clutchBlocksPlaced > 0 && pby != clutchTargetY) continue;
        } else if (isAntiTower()) {
            int lockY = (lastGroundBlockY != -999 && feetY >= lastGroundBlockY && playerPos.y >= lastGroundBlockY + 0.85) ? lastGroundBlockY : (feetY - 1);
            if (pby > lockY) continue;
            if ("UP".equals(face) && by >= lockY) continue;
        }

        if (doesIntersectPlayer(placedPos, playerPos)) continue;

        double score = targetDistanceSquared(desiredBlock, placedPos);
        if (isBlockUnderPlayer(placedPos, playerPos)) {
            score -= 100.0;
        } else if (isStasisActive()) {
            // In Stasis, allow bridging towards under the player if not yet reachable
            score += 15.0;
        }
        if ((int) Math.floor(placedPos.y) == targetY) {
            score -= 20.0;
        }
        validFaces.add(new Object[]{ score, face });
    }

    validFaces.sort((f1, f2) -> Double.compare(((Number) f1[0]).doubleValue(), ((Number) f2[0]).doubleValue()));

    for (int fi = 0; fi < validFaces.size(); fi++) {
        String face = (String) validFaces.get(fi)[1];
        Object[] res = findBestPlacementHit(held, b, face, eye, reach, playerPos, serverYaw, serverPitch);
        if (res != null) return res;
    }

    return null;
}

boolean doesIntersectPlayer(Vec3 b, Vec3 p) {
    double bx = Math.floor(b.x);
    double by = Math.floor(b.y);
    double bz = Math.floor(b.z);

    // If the top of the block is near or below the player's feet, it supports the player
    if (by + 0.7 <= p.y) return false;
    if (by <= Math.floor(p.y)) {
        if (p.y >= by + 0.45) return false;
    }

    boolean overlapX = (p.x + 0.29 > bx) && (p.x - 0.29 < bx + 1.0);
    boolean overlapY = (p.y + 1.8 > by) && (p.y < by + 1.0);
    boolean overlapZ = (p.z + 0.29 > bz) && (p.z - 0.29 < bz + 1.0);

    return overlapX && overlapY && overlapZ;
}

Vec3 offsetByFace(Vec3 pos, String face) {
    switch (face) {
        case "UP":    return pos.offset(0, 1, 0);
        case "DOWN":  return pos.offset(0, -1, 0);
        case "NORTH": return pos.offset(0, 0, -1);
        case "SOUTH": return pos.offset(0, 0, 1);
        case "EAST":  return pos.offset(1, 0, 0);
        case "WEST":  return pos.offset(-1, 0, 0);
        default:      return pos;
    }
}

boolean matchesBlock(Vec3 a, Vec3 b) {
    if (a == null || b == null) return false;
    return (int) Math.floor(a.x) == (int) Math.floor(b.x)
        && (int) Math.floor(a.y) == (int) Math.floor(b.y)
        && (int) Math.floor(a.z) == (int) Math.floor(b.z);
}

int getBlockScore(String name) {
    if (name == null) return -1;
    if (name.contains("wool") || name.contains("stone")) return 5;
    if (name.contains("clay")) return 4;
    if (name.contains("glass")) return 3;
    if (name.contains("planks") || name.contains("log")) return 2;
    if (name.contains("end_stone")) return 1;
    if (name.contains("obsidian")) return 0;
    return -1;
}

boolean isUnplaceable(String name) {
    if (name == null) return true;
    if (name.contains("torch") || name.contains("sapling") || name.contains("flower") ||
        name.contains("ladder") || name.contains("button") || name.contains("lever") ||
        name.contains("bed") || name.contains("door") || name.contains("rail") ||
        name.contains("carpet") || name.contains("web") || name.contains("sign") ||
        name.contains("slab") || name.contains("stairs") || name.contains("chest") ||
        name.contains("furnace") || name.contains("crafting_table") || name.contains("anvil")) {
        return true;
    }
    return false;
}

int pickBlockSlot() {
    int curSlot = inventory.getSlot();
    int bestSlot = -1;
    int bestScore = -1;

    for (int slot = 0; slot <= 8; slot++) {
        ItemStack s = inventory.getStackInSlot(slot);
        if (s == null || s.stackSize <= 0 || !s.isBlock) continue;

        String name = s.name.toLowerCase();
        if (isUnplaceable(name)) continue;

        int score = getBlockScore(name);
        if (score == -1) score = 1;

        if (slot == curSlot && score >= bestScore) {
            bestScore = score;
            bestSlot = slot;
        } else if (score > bestScore) {
            bestScore = score;
            bestSlot = slot;
        }
    }
    return bestSlot;
}

void equipPlannedSlot() {
    int cur = inventory.getSlot();
    if (plannedSlot != -1 && plannedSlot != cur) {
        if (clutchOriginalSlot == -1) {
            clutchOriginalSlot = cur;
        }
        clutchEquippedBlockSlot = plannedSlot;
        inventory.setSlot(plannedSlot);
        slotWasSwapped = true;
    }
}

final double SENS_GCD = 0.03404715;

float quantizeDelta(float d) {
    if (Math.abs(d) < 0.0001f) return 0f;
    double steps = Math.round((double) d / SENS_GCD);
    return (float) (steps * SENS_GCD);
}

float[] getRotationsSmoothed(float curYaw, float curPitch, float targetYaw, float targetPitch, boolean snapback) {
    float deltaYaw = wrapYawDelta(curYaw, targetYaw);
    float deltaPitch = targetPitch - curPitch;

    if (Math.abs(deltaYaw) < (float) SENS_GCD && Math.abs(deltaPitch) < (float) SENS_GCD) {
        return new float[]{ curYaw + deltaYaw, clampPitch(targetPitch) };
    }

    float speedVal = (float) getSliderValue("Speed");
    if (speedVal < 0f) speedVal = 0f;
    if (speedVal > 100f) speedVal = 100f;

    // 100 is completely snappy (instant snap)
    if (speedVal >= 100f) {
        return new float[]{ curYaw + deltaYaw, clampPitch(targetPitch) };
    }

    // 0 is slow asf (2.0 deg/tick = 40 deg/sec), scaling smoothly up to near-instant before 100
    float progress = speedVal / 100.0f;
    float maxYawChange = 2.0f + (float) Math.pow(progress, 2.0) * 180.0f;
    if (snapback) {
        maxYawChange *= 0.8f;
    }

    float maxPitchChange = maxYawChange * 0.85f;

    float moveYaw = deltaYaw;
    if (Math.abs(deltaYaw) > maxYawChange) {
        moveYaw = clampAngleChange(deltaYaw, maxYawChange);
    }

    float movePitch = deltaPitch;
    if (Math.abs(deltaPitch) > maxPitchChange) {
        movePitch = clampAngleChange(deltaPitch, maxPitchChange);
    }

    curYaw = snapToMouseSensitivity(curYaw + moveYaw, curYaw);
    curPitch = snapToMouseSensitivity(curPitch + movePitch, curPitch);

    return new float[]{ curYaw, clampPitch(curPitch) };
}

float clampPitch(float p) {
    return p > 90f ? 90f : (p < -90f ? -90f : p);
}

boolean isFireballItem(ItemStack item) {
    if (item == null) return false;
    if (item.name != null) {
        String n = item.name.toLowerCase();
        if (n.contains("fire_charge") || n.contains("fireball")) return true;
    }
    if (item.displayName != null && item.displayName.toLowerCase().contains("fireball")) return true;
    String s = item.toString().toLowerCase();
    return s.contains("fire_charge") || s.contains("fireball");
}

boolean canPlaceThrough(String name) {
    if (name == null) return true;
    String lower = name.toLowerCase();
    return lower.equals("air") || lower.equals("water") || lower.equals("flowing_water")
        || lower.equals("lava") || lower.equals("flowing_lava") || lower.equals("fire");
}

double clamp(double v, double lo, double hi) { return v < lo ? lo : (v > hi ? hi : v); }

double dist2PointAABB(Vec3 p, Vec3 b) {
    double minX = b.x, maxX = b.x + 1;
    double minY = b.y, maxY = b.y + 1;
    double minZ = b.z, maxZ = b.z + 1;
    double cx = clamp(p.x, minX, maxX);
    double cy = clamp(p.y, minY, maxY);
    double cz = clamp(p.z, minZ, maxZ);
    double dx = p.x - cx, dy = p.y - cy, dz = p.z - cz;
    return dx*dx + dy*dy + dz*dz;
}

float normYaw(float yaw) { yaw = ((yaw % 360f) + 360f) % 360f; return (yaw > 180f) ? (yaw - 360f) : yaw; }
float wrapYawDelta(float base, float target) { float d = target - base; while (d <= -180f) d += 360f; while (d > 180f) d -= 360f; return d; }
float unwrapYaw(float yaw, float prevYaw) {
    return prevYaw + ((((yaw - prevYaw + 180f) % 360f) + 360f) % 360f - 180f);
}
float[] getRotationsWrapped(Vec3 eye, double tx, double ty, double tz) {
    double dx = tx - eye.x, dy = ty - eye.y, dz = tz - eye.z;
    double hd = Math.sqrt(dx*dx + dz*dz);
    float yawWrapped = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
    yawWrapped = normYaw(yawWrapped);
    float pitch = (float) Math.toDegrees(-Math.atan2(dy, hd));
    return new float[]{ yawWrapped, pitch };
}

boolean isAtEdgeOfBlock(Entity me) {
    if (me == null || !me.onGround()) return false;
    Vec3 p = me.getPosition();
    if (p == null) return false;
    int feetY = (int) Math.floor(p.y - 0.1);

    for (double[] c : CORNERS) {
        int cx = (int) Math.floor(p.x + c[0]);
        int cz = (int) Math.floor(p.z + c[1]);
        Block blk = world.getBlockAt(cx, feetY, cz);
        if (blk == null || canPlaceThrough(blk.name)) {
            return true;
        }
    }

    Vec3 lastP = me.getLastPosition();
    if (lastP != null) {
        double mx = p.x - lastP.x;
        double mz = p.z - lastP.z;
        if (Math.abs(mx) > 0.01 || Math.abs(mz) > 0.01) {
            for (double[] c : CORNERS) {
                int cx = (int) Math.floor(p.x + mx * 1.5 + c[0]);
                int cz = (int) Math.floor(p.z + mz * 1.5 + c[1]);
                Block blk = world.getBlockAt(cx, feetY, cz);
                if (blk == null || canPlaceThrough(blk.name)) {
                    return true;
                }
            }
        }
    }
    return false;
}
}
