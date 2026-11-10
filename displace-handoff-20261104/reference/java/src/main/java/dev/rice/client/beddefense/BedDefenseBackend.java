package dev.rice.client.beddefense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Sam's OpenB4 Bed Defender script ("v1.0 by winnie") imported as a native Rice module body. The class
 * body retains the uploaded layouts and placement order, with session cleanup and missing-player guards (same server-synced
 * raycast placement). Module-facing entry points are public, and the OpenB4 script API
 * (client/world/inventory/keybinds/render/util/modules, Vec3/Block/ItemStack/Entity/CPacket/Json) is inherited
 * from {@link BedDefenseApi}, which rebinds it to the real Minecraft engine. No OpenJade.
 */
public final class BedDefenseBackend extends BedDefenseApi {
// ======= CONSTANT: embedded defenses JSON (edit if you want to add/remove patterns) =======
static final String DEFENSES_JSON =
"{\"EndWoolCorners\":[{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":3},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":-1},{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":2}],\"EndWoolCorners2\":[{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":3}],\"EndGlassNoCorners\":[{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"stained_glass\",\"x\":0,\"y\":0,\"z\":3},{\"block\":\"stained_glass\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"stained_glass\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"stained_glass\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"stained_glass\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"stained_glass\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"stained_glass\",\"x\":-1,\"y\":0,\"z\":2},{\"block\":\"stained_glass\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"stained_glass\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"stained_glass\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"stained_glass\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"stained_glass\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"stained_glass\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"stained_glass\",\"x\":0,\"y\":1,\"z\":2}],\"WoodWoolCorners\":[{\"block\":\"planks\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"planks\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"planks\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"planks\",\"x\":-1,\"y\":0,\"z\":2},{\"block\":\"planks\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"planks\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"planks\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"planks\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"planks\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"planks\",\"x\":0,\"y\":0,\"z\":3},{\"block\":\"planks\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"planks\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"planks\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"planks\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"planks\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"planks\",\"x\":0,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":1}],\"EndGlassWool\":[{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":2,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"stained_glass\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"stained_glass\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"stained_glass\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"stained_glass\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"stained_glass\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"stained_glass\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"stained_glass\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"stained_glass\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"stained_glass\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"stained_glass\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"stained_glass\",\"x\":-1,\"y\":0,\"z\":2},{\"block\":\"stained_glass\",\"x\":0,\"y\":1,\"z\":2},{\"block\":\"stained_glass\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"stained_glass\",\"x\":0,\"y\":0,\"z\":3},{\"block\":\"wool\",\"x\":-3,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":-3,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":-2,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":-2,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":3},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":3},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":4},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":3},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":2,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":2,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":3,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":3,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":2,\"z\":0},{\"block\":\"wool\",\"x\":1,\"y\":2,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":2},{\"block\":\"wool\",\"x\":-1,\"y\":2,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":2,\"z\":1},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":-2},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":-3},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":-2},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":-2},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":3,\"z\":0},{\"block\":\"wool\",\"x\":0,\"y\":3,\"z\":1}],\"Wool1Layer\":[{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":-1}],\"EndWoodLongCorners\":[{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"planks\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"planks\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"planks\",\"x\":-1,\"y\":1,\"z\":-1},{\"block\":\"planks\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"planks\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"planks\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"planks\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"planks\",\"x\":1,\"y\":1,\"z\":-1},{\"block\":\"planks\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"planks\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"planks\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"planks\",\"x\":2,\"y\":0,\"z\":1},{\"block\":\"planks\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"planks\",\"x\":0,\"y\":1,\"z\":2},{\"block\":\"planks\",\"x\":0,\"y\":0,\"z\":3},{\"block\":\"planks\",\"x\":-1,\"y\":0,\"z\":2}],\"EndWoolLong\":[{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":2},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":-1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-2},{\"block\":\"end_stone\",\"x\":0,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":-1},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":0},{\"block\":\"end_stone\",\"x\":1,\"y\":0,\"z\":1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":-1},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":0},{\"block\":\"end_stone\",\"x\":0,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":-2,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":-1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":-1},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":0},{\"block\":\"wool\",\"x\":0,\"y\":2,\"z\":1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":-1},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":0},{\"block\":\"wool\",\"x\":1,\"y\":1,\"z\":1},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":-1},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":0},{\"block\":\"wool\",\"x\":2,\"y\":0,\"z\":1},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":2},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":3},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":2},{\"block\":\"wool\",\"x\":1,\"y\":0,\"z\":-2},{\"block\":\"wool\",\"x\":0,\"y\":1,\"z\":-2},{\"block\":\"wool\",\"x\":0,\"y\":0,\"z\":-3},{\"block\":\"wool\",\"x\":-1,\"y\":0,\"z\":-2}]}";

// ======= STATE =======
int i;
boolean started;
boolean sneaked;
boolean onlyTopBeds;
boolean bedwarsOnly;
boolean pendingPlace;
String lockedDirection = "";
String face;
Vec3 origin = null;
Vec3 hitPos;
Vec3 placePos;
Vec3 renderTarget;
float intervalYaw;
float intervalPitch;
volatile float serverYaw;
volatile float serverPitch;
int delayAfterSwap;
int delayAfterAim;
int swapTicks;
int aimTicks;
int sneakHoldTicks;
int sneakHoldTicksRemaining;
Map<String, Integer> inventoryCache = new HashMap<>();
List<Object[]> defense;
List<Object[]>[] defenses;
String[] defenseNames;

// ======= DEBUG =======
void dbg(String s) {
    if (modules.getButton(getScriptName(), "Debug Logs")) {
        client.print("&7[beddef] " + s);
    }
}

// ======= LOAD =======
public void onLoad() {
    modules.registerDescription("v1.0 by \u00A7bwinnie");

    Json allDefs;
    try { allDefs = new Json(DEFENSES_JSON); }
    catch (Exception ignored) { allDefs = new Json("{}"); }

    Map<String, Json> rootMap = allDefs.object().map();
    String[] namesArr = rootMap.keySet().toArray(new String[0]);

    if (namesArr.length == 0) {
        defenseNames = new String[]{ util.color("&cNo Defenses Found") };
        defenses = new List[0];
        modules.registerDescription("No embedded bed defenses found.");
    } else {
        defenseNames = namesArr;

        @SuppressWarnings("unchecked")
        List<Object[]>[] loadedDefs = new List[defenseNames.length];

        for (int i = 0; i < defenseNames.length; i++) {
            List<Object[]> list = new ArrayList<>();
            List<Json> arr;
            try { arr = allDefs.array(defenseNames[i]); }
            catch (Exception e) { arr = new ArrayList<>(); }

            for (Json entry : arr) {
                try {
                    Json obj = entry.object();
                    String block = obj.get("block", "");
                    int x = parseInt(obj.get("x", "0"), 0);
                    int y = parseInt(obj.get("y", "0"), 0);
                    int z = parseInt(obj.get("z", "0"), 0);
                    if (!block.isEmpty()) list.add(new Object[]{ block, new Vec3(x, y, z) });
                } catch (Exception ignored) {}
            }
            loadedDefs[i] = list;
        }
        defenses = loadedDefs;

        modules.registerButton("Only Top Beds", true);
        modules.registerButton("Bedwars Only", true);
        modules.registerSlider("Delay After Swap", " ticks", 0, 0, 10, 1);
        modules.registerSlider("Delay After Aiming", " ticks", 0, 0, 10, 1);
        modules.registerSlider("Sneak Hold Ticks", " ticks", 5, 0, 20, 1);
        modules.registerSlider("FOV", "", 180, 0, 180, 1);

        // Option slider suffix MUST NOT be empty -> use " "
        if (defenseNames != null && defenseNames.length > 0) {
            modules.registerSlider("Defense", " ", 2, defenseNames);
        }
        // Debug toggle
        modules.registerButton("Debug Logs", false);
    }
}

// ======= ENABLE/DISABLE =======
public void onEnable() {
    onDisable();
    if (client.getPlayer() == null) return;
    int idx = (int) modules.getSlider(getScriptName(), "Defense");
    if (defenses == null || defenses.length == 0) {
        defense = new ArrayList<>();
        dbg("enabled, but no defenses loaded.");
    } else {
        if (idx < 0) idx = 0;
        if (idx >= defenses.length) idx = defenses.length - 1;
        defense = defenses[idx];
        dbg("enabled. defense='" + defenseNames[idx] + "' steps=" + defense.size());

        if (!hasAnyRequiredBlockForDefense(defense)) {
            dbg("no required blocks for selected defense in hotbar (disabling)");
            modules.disable(getScriptName());
            return;
        }
    }

    i = 0;
    renderTarget = null;
    started = sneaked = false;
    lockedDirection = "";
    aimTicks = (int) modules.getSlider(getScriptName(), "Delay After Aiming");
    serverYaw = client.getPlayer().getYaw();
    serverPitch = client.getPlayer().getPitch();
    inventoryCache.clear();
}

public void onDisable() {
    if (sneaked) keybinds.restore("sneak");
    sneaked = started = pendingPlace = false;
    i = swapTicks = aimTicks = sneakHoldTicksRemaining = 0;
    origin = hitPos = placePos = renderTarget = null;
    face = null;
    lockedDirection = "";
    defense = null;
    inventoryCache.clear();
    dbg("disabled.");
}

// ======= CORE LOOP =======
public float[] getRotations(float lastYaw, float lastPitch) {
    if (client.getPlayer() == null || defense == null) return null;
    onlyTopBeds    = modules.getButton(getScriptName(), "Only Top Beds");
    bedwarsOnly    = modules.getButton(getScriptName(), "Bedwars Only");
    delayAfterSwap = (int) modules.getSlider(getScriptName(), "Delay After Swap");
    delayAfterAim  = (int) modules.getSlider(getScriptName(), "Delay After Aiming");
    sneakHoldTicks = (int) modules.getSlider(getScriptName(), "Sneak Hold Ticks");

    float fov = (float) modules.getSlider(getScriptName(), "FOV");
    float maxYaw   = fov;
    float maxPitch = Math.min(fov, 90f);

    if (!started) {
        Vec3 bh = null;

        if (bedwarsOnly) {
            bh = modules.getWhitelistedBedPos();
            if (bh == null) {
                dbg("no whitelisted bed. disabling.");
                modules.disable(getScriptName());
                return null;
            }
            if (!world.getBlockAt(bh).name.equalsIgnoreCase("bed")) {
                dbg("whitelisted bed pos is stale/not a bed. disabling.");
                modules.disable(getScriptName());
                return null;
            }
        } else {
            bh = findBed(16); // more forgiving
            if (bh == null) {
                dbg("no bed in range. disabling.");
                modules.disable(getScriptName());
                return null;
            }
            if (!world.getBlockAt(bh).name.equalsIgnoreCase("bed")) {
                dbg("findBed returned invalid target. disabling.");
                modules.disable(getScriptName());
                return null;
            }
        }

        dbg("bed found at " + (int)bh.x + "," + (int)bh.y + "," + (int)bh.z);

        origin = bh;
        switch (world.getBlockAt(bh).variant) {
            case 10: case 0: lockedDirection = "north"; break;
            case 8:  case 2: lockedDirection = "south"; break;
            case 9:  case 3: lockedDirection = "west";  break;
            case 11: case 1: lockedDirection = "east";  break;
            default: lockedDirection = ""; break;
        }
        dbg("lockedDirection=" + lockedDirection);
        started = true;
    }

    while (i < defense.size()) {
        Vec3 off = rotateOffset((Vec3) defense.get(i)[1], lockedDirection);
        Vec3 tgt = origin.offset(off.x, off.y, off.z);
        if (world.getBlockAt((int)tgt.x,(int)tgt.y,(int)tgt.z).name.equals("air")) break;
        dbg("step " + i + " already filled at " + (int)tgt.x + "," + (int)tgt.y + "," + (int)tgt.z);
        i++;
    }
    if (i >= defense.size()) { dbg("done (no more steps)."); modules.disable(getScriptName()); return null; }

    Vec3 off = rotateOffset((Vec3) defense.get(i)[1], lockedDirection);
    Vec3 target = origin.offset(off.x, off.y, off.z);
    renderTarget = target;

    float baseYaw   = serverYaw;
    float basePitch = serverPitch;

    float[] res0 = attemptPlace(baseYaw, basePitch, target);
    if (res0 != null) {
        if (res0[1] == -999f) return new float[] { baseYaw, basePitch };
        return res0;
    }

    Entity me = client.getPlayer();

    String[] opp = { "DOWN", "UP", "SOUTH", "NORTH", "WEST", "EAST" };
    int[] dx = { 0, 0, 0, 0, 1, -1 };
    int[] dy = { 1, -1, 0, 0, 0, 0 };
    int[] dz = { 0, 0, -1, 1, 0, 0 };

    Vec3 eye = me.getPosition().offset(0, me.getEyeHeight(), 0);
    float curYawW = normYaw(serverYaw);
    float curPit  = serverPitch;
    float cliYawW = normYaw(me.getYaw());
    float cliPit  = me.getPitch();

    double INSET = 0.05, STEP = 0.2, JIT = 0.2, insetTop = 1 - INSET - 1e-3, insetBot = INSET + 1e-3;
    int GRID = (int) Math.round(1 / STEP);

    ArrayList<Object[]> cands = new ArrayList<>((GRID + 1) * (GRID + 1) * 6);

    for (int fi = 0; fi < 6; fi++) {
        String faceCheck = opp[fi];
        Vec3 support = new Vec3(target.x + dx[fi], target.y + dy[fi], target.z + dz[fi]);

        String supportName = world.getBlockAt(support).name;
        if (supportName.equals("air")) continue;
        if (onlyTopBeds && supportName.equals("bed") && !"UP".equals(faceCheck)) continue;

        for (int rr = 0; rr <= GRID; rr++) {
            boolean ltr = (rr & 1) == 0;
            double v = rr * STEP + util.randomDouble(-STEP * JIT, STEP * JIT);
            if (v < 0) v = 0; else if (v > 1) v = 1;

            for (int cc = 0; cc <= GRID; cc++) {
                double cu = cc * STEP + util.randomDouble(-STEP * JIT, STEP * JIT);
                if (cu < 0) cu = 0; else if (cu > 1) cu = 1;
                double u = ltr ? cu : 1 - cu;

                double px, py, pz;
                if (fi < 2) {
                    px = support.x + u; pz = support.z + v;
                    py = support.y + (fi == 1 ? insetTop : insetBot);
                } else if (fi < 4) {
                    px = support.x + u; py = support.y + v;
                    pz = support.z + (fi == 2 ? insetTop : insetBot);
                } else {
                    pz = support.z + u; py = support.y + v;
                    px = support.x + (fi == 5 ? insetTop : insetBot);
                }

                float[] rotW = getRotationsWrapped(eye, px, py, pz);
                float yawW = rotW[0], pit = rotW[1];

                if (Math.abs(wrapYawDelta(cliYawW, yawW)) > maxYaw) continue;
                if (Math.abs(pit - cliPit) > maxPitch) continue;
                if (Math.abs(pit) > 90f) continue;

                double cost = Math.abs((double) wrapYawDelta(curYawW, yawW)) + Math.abs((double) (pit - curPit)) + ("UP".equals(faceCheck) ? -0.25 : 0);

                cands.add(new Object[]{ cost, Float.valueOf(yawW), Float.valueOf(pit) });
            }
        }
    }

    if (cands.isEmpty()) { dbg("no aim candidates (FOV/angles?)"); return null; }
    cands.sort((a, b) -> Double.compare((Double) a[0], (Double) b[0]));

    for (int ci = 0; ci < cands.size(); ci++) {
        float yawW = (Float) cands.get(ci)[1];
        float pit  = (Float) cands.get(ci)[2];
        float yawUnwrapped = unwrapYaw(yawW, serverYaw);

        float[] res = attemptPlace(yawUnwrapped, pit, target);
        if (res != null) return (res[1] == -999f) ? new float[]{ yawUnwrapped, pit } : res;
    }

    return null;
}

// ======= Placement attempt =======
float[] attemptPlace(float yaw, float pitch, Vec3 target) {
    if (i >= defense.size()) return null;

    Object[] ray = client.raycastBlock(4.5, yaw, pitch);
    if (ray == null) return null;

    Vec3 hit = (Vec3) ray[0];
    String _face = (String) ray[2];
    Vec3 _place = offsetByFace(hit, _face);
    if (!_place.equals(target)) return null;

    String targetBlock = world.getBlockAt((int) hit.x, (int) hit.y, (int) hit.z).name;
    if (onlyTopBeds && !_face.equals("UP") && targetBlock.equals("bed")) return null;

    Object[] cur = defense.get(i);
    String block = (String) cur[0];

    int curSlot = inventory.getSlot();
    int wantSlot = getMatchingInventorySlot(block);
    if (wantSlot == -1) { dbg("missing block: " + block + " (disabling)"); modules.disable(getScriptName()); return null; }

    if (curSlot != wantSlot) {
        dbg("switching to slot " + wantSlot + " for " + block);
        inventory.setSlot(wantSlot);
        swapTicks = delayAfterSwap;
    }

    if (canPlaceAt(hit, _face)) {
        if (swapTicks-- > 0) { dbg("swap delay... (" + (swapTicks+1) + " left)"); return new float[]{ -999f, -999f }; }

        if (!keybinds.isPressed("sneak") && targetBlock.equals("bed")) {
            keybinds.setPressed("sneak", true);
            sneaked = true;
            sneakHoldTicksRemaining = sneakHoldTicks;
            dbg("sneaking over bed for placement");
            return new float[]{ -999f, -999f };
        }

        if (aimTicks-- > 0 || Math.abs(yaw - serverYaw) > 25 || Math.abs(pitch - serverPitch) > 25) {
            dbg("aiming... yaw��=" + Math.abs(yaw - serverYaw) + " pitch��=" + Math.abs(pitch - serverPitch) + " (aimTicks " + (aimTicks+1) + ")");
            return new float[]{ yaw, pitch };
        }

        aimTicks = delayAfterAim;

        hitPos = hit;
        face = _face;
        placePos = ((Vec3) ray[1]).offset(hit.x, hit.y, hit.z);
        pendingPlace = true;

        dbg("queue place " + block + " at " + (int)target.x + "," + (int)target.y + "," + (int)target.z + " via face " + face);
        return new float[]{ yaw, pitch };
    }
    return null;
}

// ======= Helpers =======
boolean canPlaceAt(Vec3 hit, String face) {
    Vec3 placeHere = offsetByFace(hit, face);
    Block dest = world.getBlockAt((int) placeHere.x, (int) placeHere.y, (int) placeHere.z);
    return "air".equalsIgnoreCase(dest.name);
}

float normYaw(float yaw) {
    yaw = ((yaw % 360f) + 360f) % 360f;
    return (yaw > 180f) ? (yaw - 360f) : yaw;
}
float wrapYawDelta(float base, float target) {
    float d = target - base;
    while (d <= -180f) d += 360f;
    while (d > 180f) d -= 360f;
    return d;
}
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

public void onPreUpdate() {
    if (sneaked) {
        if (sneakHoldTicksRemaining > 0) {
            sneakHoldTicksRemaining--;
        } else {
            keybinds.setPressed("sneak", false);
            sneaked = false;
            dbg("released sneak.");
        }
    }

    if (!pendingPlace) return;
    pendingPlace = false;
    if (client.placeBlock(hitPos, face, placePos)) {
        client.swing();
        dbg("placed step " + i);
        i++;
    } else {
        dbg("place failed (blocked or stale ray).");
    }
}

public void onRenderWorld(float partialTicks) {
    if (renderTarget != null) {
        render.block(renderTarget, 0x00FF00, true, true);
    }
}

boolean hasAnyRequiredBlockForDefense(List<Object[]> def) {
    if (def == null || def.isEmpty()) return false;

    HashSet<String> required = new HashSet<>();
    for (Object[] step : def) {
        if (step == null || step.length == 0) continue;
        String block = (String) step[0];
        if (block != null) required.add(block.toLowerCase());
    }

    for (int s = 0; s < 9; s++) {
        ItemStack item = inventory.getStackInSlot(s);
        if (item == null || item.name == null) continue;
        if (required.contains(item.name.toLowerCase())) return true;
    }
    return false;
}

int getMatchingInventorySlot(String blockName) {
    String key = blockName.toLowerCase();
    Integer slot = inventoryCache.get(key);
    if (slot != null) {
        ItemStack cachedItem = inventory.getStackInSlot(slot);
        if (cachedItem != null && cachedItem.name.equalsIgnoreCase(blockName)) {
            return slot;
        }
    }
    dbg("looking for block '" + key + "' in hotbar");
    for (int s = 0; s < 9; s++) {
        ItemStack item = inventory.getStackInSlot(s);
        if (item != null) {
            String nm = item.name;
            dbg("slot " + s + " -> " + nm);
            if (nm != null && nm.equalsIgnoreCase(blockName)) {
                inventoryCache.put(key, s);
                return s;
            }
        }
    }
    return -1;
}

Vec3 rotateOffset(Vec3 offset, String direction) {
    int x = (int) offset.x, y = (int) offset.y, z = (int) offset.z;
    switch (direction) {
        case "north": return new Vec3(x, y, z);
        case "south": return new Vec3(-x, y, -z);
        case "east":  return new Vec3(-z, y, x);
        case "west":  return new Vec3(z, y, -x);
        default:      return offset;
    }
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

Vec3 findBed(int range) {
    Vec3 playerPos = client.getPlayer().getBlockPosition();
    int centerX = (int) playerPos.x;
    int centerY = (int) playerPos.y;
    int centerZ = (int) playerPos.z;
    int startX = centerX - range, endX = centerX + range;
    int startY = centerY - range, endY = centerY + range;
    int startZ = centerZ - range, endZ = centerZ + range;

    double bestDist = Double.MAX_VALUE;
    Vec3 best = null;

    for (int x = startX; x <= endX; x++) {
        for (int y = startY; y <= endY; y++) {
            for (int z = startZ; z <= endZ; z++) {
                Vec3 pos = new Vec3(x, y, z);
                Block block = world.getBlockAt(pos);
                if (!block.name.equalsIgnoreCase("bed")) continue;
                Vec3 head = null, foot = null;
                switch (block.variant) {
                    case 0:  foot = pos; head = pos.offset(0, 0, 1);  break; // South (foot)
                    case 1:  foot = pos; head = pos.offset(-1, 0, 0); break; // West (foot)
                    case 2:  foot = pos; head = pos.offset(0, 0, -1); break; // North (foot)
                    case 3:  foot = pos; head = pos.offset(1, 0, 0);  break; // East (foot)
                    case 8:  head = pos; foot = pos.offset(0, 0, -1); break; // South (head)
                    case 9:  head = pos; foot = pos.offset(1, 0, 0);  break; // West (head)
                    case 10: head = pos; foot = pos.offset(0, 0, 1);  break; // North (head)
                    case 11: head = pos; foot = pos.offset(-1, 0, 0); break; // East (head)
                }
                if (head != null && foot != null) {
                    double headDist = head.distanceToSq(playerPos);
                    double footDist = foot.distanceToSq(playerPos);
                    // choose the NEAREST half (not farther)
                    double d = Math.min(headDist, footDist);
                    Vec3 candidate = (headDist <= footDist) ? head : foot;
                    if (d < bestDist) {
                        bestDist = d;
                        best = candidate;
                    }
                }
            }
        }
    }
    return best;
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
    if (button == 1 || button == 0) return false;
    return true;
}

int parseInt(String s, int def) {
    try { return Integer.parseInt(s); } catch (Exception e) { return def; }
}

}
