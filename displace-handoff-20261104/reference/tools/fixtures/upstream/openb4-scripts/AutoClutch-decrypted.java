boolean falling = false, john = false;
boolean wass, turnoff, fbed, delayB, toolazy;

int delayT, maxdis = 0;
int ToggleDelayI, GroundI;
boolean ToggleDelayB, Groundb;
boolean clutchOwned = false;
boolean prevHotbarKey = false;

int airTicks = 0;
int ticksSinceLastPlace = 0;

double hitboxHalfWidth = 0.3;
double hitboxHeight = 1.8;

String[] offmode = {"None", "Movement", "Forward", "Ground", "Ground (delay)", "Ground + Forward"};
String[] maxblcokss = {"None", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20"};
String[] delaythingy = {"None", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "200"};
String[] onmode = {"Void", "Void hit", "Void hit (ignore fb)"};

void onLoad() {
    String[] maxfalls = new String[51];
    maxfalls[0] = "Void only";
    for (int i = 1; i <= 50; i++) {
        maxfalls[i] = i + (i == 1 ? " block" : " blocks");
    }

    modules.registerDescription("-original by afk, edited by noot-");
    modules.registerDescription("Toggling options:");
    modules.registerSlider("Toggle on", " ", 2, onmode);
    modules.registerSlider("Toggle off", " ", 2, offmode);
    modules.registerDescription("Config:");
    modules.registerSlider("Max Fall", "", 0, maxfalls);
    modules.registerSlider("Max blocks", " ", 3, maxblcokss);
    modules.registerSlider("Clutch Cooldown", " ", 4, delaythingy);
    modules.registerSlider("Air Limit", " ticks", 10, 0, 20, 1);
    modules.registerButton("Falling Only", false);
    modules.registerButton("Allow Placing", true);
    modules.registerButton("Pearl Cancel", true);
    modules.registerButton("Ladder/Water Cancel", true);
    modules.registerDescription("Prediction:");
    modules.registerButton("Recovery Check", true);
    modules.registerButton("Predicted Safe", true);
    modules.registerDescription("Debug:");
    modules.registerButton("Trajectory", false);
}

void onEnable() {
    if (me0ef0d2d6d8947("Scaffold")) {
        wass = true;
    }
}

boolean m847abb7ab100c1(CPacket packet) {
    if (packet instanceof C08 && falling) {
        maxdis++;
        ticksSinceLastPlace = 0;
    }

    if (packet instanceof C08 && (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Toggle on") == 2) {
        C08 p = (C08) packet;
        if (p.itemStack != null && p.itemStack.toString().contains("fire_charge")) {
            fbed = true;
            delayB = true;
            delayT = 20;
        }
    }

    return true;
}

int getMaxBlocks() {
    int index = (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Max blocks");
    String value = maxblcokss[index];
    if (value.equals("None")) return 0;
    return Integer.parseInt(value);
}

int getDelay() {
    int index = (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Clutch Cooldown");
    String value = delaythingy[index];
    if (value.equals("None")) return 0;
    return Integer.parseInt(value);
}

void mc9827b8a72cd48() {
    int maxBlocks = getMaxBlocks();
    int clutchCooldown = getDelay();
    int airTickLimit = (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Air Limit");
    int maxFall = (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Max Fall");

    Entity player = ma1da5bf03ec1d5();
    int toggleOffMode = (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Toggle off");
    int toggleOnMode = (int) m351d12ebfcdc1f(m51e8a7d3c9bf42(), "Toggle on");

    int dist = fallDistance();
    int blinkDist = 50;

    int hurtTime = player.getHurtTime();
    ItemStack held = player.getHeldItem();

    if (me0ef0d2d6d8947("Scaffold")) {
        ticksSinceLastPlace++;
        if (!player.onGround()) {
            airTicks++;
        } else {
            airTicks = 0;
        }
        if (airTickLimit > 0 && clutchOwned && airTicks > airTickLimit && ticksSinceLastPlace > airTickLimit) {
            m273b7e3f39ca1e("Scaffold");
            falling = false;
            turnoff = false;
            airTicks = 0;
            ticksSinceLastPlace = 0;
        }
    } else {
        airTicks = 0;
        ticksSinceLastPlace = 0;
        clutchOwned = false;
    }

    if (maxBlocks != 0 && maxdis >= maxBlocks && hurtTime > 0) {
        falling = false;
        turnoff = true;
        toolazy = true;
    }

    if (ToggleDelayB && ToggleDelayI > 0) {
        ToggleDelayI--;
        if (ToggleDelayI == 1) {
            ToggleDelayB = false;
        }
    }

    if (player.onGround()) {
        toolazy = false;
        maxdis = 0;
    }

    if (held != null && held.toString().contains("fire_charge") && mb6cb1cd3241227(1) && toggleOnMode == 2) {
        fbed = true;
        delayB = true;
        delayT = 20;
    }

    if (delayB && delayT > 0) {
        delayT--;
        if (delayT < 2) {
            fbed = false;
            delayB = false;
        }
    }

    int switchSlot = -1;
    for (int n = 1; n <= 9; n++) {
        int key = maa00019f32541b("hotbar." + n);
        if (key <= 0) key = n + 1;
        if (m3cb75614ee4976(key)) {
            switchSlot = n - 1;
            break;
        }
    }
    boolean hotbarKeyDown = switchSlot != -1;

    ItemStack switchStack = hotbarKeyDown ? mcddb23d9d93af2(switchSlot) : null;
    boolean switchToPearl = switchStack != null && switchStack.name.contains("ender_pearl")
            && med53d00cb85603(m51e8a7d3c9bf42(), "Pearl Cancel");
    boolean switchToLadder = switchStack != null
            && (switchStack.name.contains("ladder") || switchStack.name.contains("water_bucket"))
            && med53d00cb85603(m51e8a7d3c9bf42(), "Ladder/Water Cancel");

    if ((switchToPearl || switchToLadder) && !prevHotbarKey
            && clutchOwned && me0ef0d2d6d8947("Scaffold")) {
        m273b7e3f39ca1e("Scaffold");
        mef74bbf389a75c(switchSlot);
        falling = false;
        john = false;
        turnoff = false;
        clutchOwned = false;
        int cd = clutchCooldown;
        if (cd < 20) cd = 20;
        ToggleDelayI = cd;
        ToggleDelayB = true;
    }
    prevHotbarKey = hotbarKeyDown;

    if (turnoff) {
        if (toggleOffMode == 0 && !me0ef0d2d6d8947("Scaffold")) {
            turnoff = false;
            if (mb6cb1cd3241227(1) && med53d00cb85603(m51e8a7d3c9bf42(), "Allow Placing")) {
                mec665253088a75("use", true);
                if (clutchCooldown > 1) {
                    ToggleDelayI = clutchCooldown;
                    ToggleDelayB = true;
                }
            }
        }

        if (toggleOffMode == 1 && m51699116dc0d32()) {
            releaseScaffold(clutchCooldown);
        }

        if (toggleOffMode == 2 && m21eba45aea8060("forward")) {
            releaseScaffold(clutchCooldown);
        }

        if (toggleOffMode == 3 && player.onGround()) {
            releaseScaffold(clutchCooldown);
        }

        if (toggleOffMode == 4) {
            if (!me0ef0d2d6d8947("Scaffold")) {
                turnoff = false;
                Groundb = false;
            }

            if (player.onGround()) {
                if (!Groundb) {
                    GroundI = 20;
                    Groundb = true;
                }
            } else if (!m21eba45aea8060("jump")) {
                Groundb = false;
            }

            if (Groundb && GroundI > 0) {
                GroundI--;
            }

            if (GroundI == 0) {
                Groundb = false;
                releaseScaffold(clutchCooldown);
            }
        }

        if (toggleOffMode == 5 && player.onGround() && m21eba45aea8060("forward")) {
            releaseScaffold(clutchCooldown);
        }
    }

    if (med53d00cb85603(m51e8a7d3c9bf42(), "Predicted Safe") && me0ef0d2d6d8947("Scaffold") && clutchOwned && simulateTrajectory(false, 2.0)) {
        releaseScaffold(clutchCooldown);
        falling = false;
        john = false;
    }

    if (toggleOnMode == 0) {
        hurtTime = 1;
    }

    if (hurtTime > 0 && !toolazy && !ToggleDelayB && !me0ef0d2d6d8947("Scaffold") && !me3259e23ea00b4()
            && (dist == -1 || (maxFall > 0 && dist > maxFall)) && !falling && !player.onGround() && !fbed
            && isDescending() && !canReturnToPlatform()) {
        falling = true;
        m8246c06a696131("Scaffold");
        clutchOwned = true;
        wass = false;
    } else if (falling && player.getFallDistance() > blinkDist && dist == -1 && !john) {
        john = true;
        turnoff = true;
    } else if (falling && (player.onGround() || (dist != -1 && (maxFall <= 0 || dist <= maxFall)))) {
        falling = false;
        john = false;
        turnoff = true;
    }
}

void releaseScaffold(int clutchCooldown) {
    m273b7e3f39ca1e("Scaffold");
    if (mb6cb1cd3241227(1) && med53d00cb85603(m51e8a7d3c9bf42(), "Allow Placing")) {
        mec665253088a75("use", true);
    }
    turnoff = false;
    if (clutchCooldown > 1) {
        ToggleDelayI = clutchCooldown;
        ToggleDelayB = true;
    }
}

int fallDistance() {
    int fallDist = -1;

    Vec3 pos = ma1da5bf03ec1d5().getPosition();
    int x = (int) Math.floor(pos.x);
    int z = (int) Math.floor(pos.z);
    int y = (int) Math.floor(pos.y) - 1;

    for (int i = y; i > -1; i--) {
        Block block = me055b71d99fb47(x, i, z);
        if (block.name.equals("air") || block.name.contains("sign")) {
            continue;
        }
        fallDist = y - i;
        break;
    }

    return fallDist;
}

boolean isDescending() {
    if (!med53d00cb85603(m51e8a7d3c9bf42(), "Falling Only")) return true;
    return m561fb4b556a781().y < 0.0;
}

boolean canReturnToPlatform() {
    if (!med53d00cb85603(m51e8a7d3c9bf42(), "Recovery Check")) return false;
    return simulateTrajectory(false, 2.0);
}

void mefde3ad093036b(float partialTicks) {
    if (!med53d00cb85603(m51e8a7d3c9bf42(), "Trajectory")) return;

    ma03258600c2d43();
    mecc0542efd6108();
    mf58ad24838894d(false);
    m847f3ea4155e20(false);
    m6eb36c7aa4e10c(false);
    m6de92a3398625f(true);
    m1bbc8a0ce32204(false);
    mc9e9651588b2cd(false);
    mfc369f2f7806b9(false);
    mbc99457df9804f(true);
    me8fce7cd6becd0();

    simulateTrajectory(true, 1.0E9);

    mbc99457df9804f(false);
    mfc369f2f7806b9(true);
    mc9e9651588b2cd(true);
    m1bbc8a0ce32204(true);
    m6de92a3398625f(false);
    m6eb36c7aa4e10c(true);
    m847f3ea4155e20(true);
    mf58ad24838894d(true);
    me8fce7cd6becd0();
    m8df4a6c012f52c();
}

void colorRGBA(int rgb, float a) {
    m5eba1266aa2c40(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, a);
}

void boxOutline(Vec3 cam, double x0, double y0, double z0, double x1, double y1, double z1) {
    double ax0 = x0 - cam.x, ay0 = y0 - cam.y, az0 = z0 - cam.z;
    double ax1 = x1 - cam.x, ay1 = y1 - cam.y, az1 = z1 - cam.z;
    mb7aa1d5d8e0d32(1);
    me0a1c755ccff88(ax0, ay0, az0); me0a1c755ccff88(ax1, ay0, az0);
    me0a1c755ccff88(ax1, ay0, az0); me0a1c755ccff88(ax1, ay0, az1);
    me0a1c755ccff88(ax1, ay0, az1); me0a1c755ccff88(ax0, ay0, az1);
    me0a1c755ccff88(ax0, ay0, az1); me0a1c755ccff88(ax0, ay0, az0);
    me0a1c755ccff88(ax0, ay1, az0); me0a1c755ccff88(ax1, ay1, az0);
    me0a1c755ccff88(ax1, ay1, az0); me0a1c755ccff88(ax1, ay1, az1);
    me0a1c755ccff88(ax1, ay1, az1); me0a1c755ccff88(ax0, ay1, az1);
    me0a1c755ccff88(ax0, ay1, az1); me0a1c755ccff88(ax0, ay1, az0);
    me0a1c755ccff88(ax0, ay0, az0); me0a1c755ccff88(ax0, ay1, az0);
    me0a1c755ccff88(ax1, ay0, az0); me0a1c755ccff88(ax1, ay1, az0);
    me0a1c755ccff88(ax1, ay0, az1); me0a1c755ccff88(ax1, ay1, az1);
    me0a1c755ccff88(ax0, ay0, az1); me0a1c755ccff88(ax0, ay1, az1);
    me383a8a14f149b();
}

void boxFilled(Vec3 cam, double x0, double y0, double z0, double x1, double y1, double z1) {
    double ax0 = x0 - cam.x, ay0 = y0 - cam.y, az0 = z0 - cam.z;
    double ax1 = x1 - cam.x, ay1 = y1 - cam.y, az1 = z1 - cam.z;
    mb7aa1d5d8e0d32(7);
    me0a1c755ccff88(ax0, ay0, az0); me0a1c755ccff88(ax1, ay0, az0); me0a1c755ccff88(ax1, ay0, az1); me0a1c755ccff88(ax0, ay0, az1);
    me0a1c755ccff88(ax0, ay1, az0); me0a1c755ccff88(ax0, ay1, az1); me0a1c755ccff88(ax1, ay1, az1); me0a1c755ccff88(ax1, ay1, az0);
    me0a1c755ccff88(ax0, ay0, az0); me0a1c755ccff88(ax0, ay1, az0); me0a1c755ccff88(ax1, ay1, az0); me0a1c755ccff88(ax1, ay0, az0);
    me0a1c755ccff88(ax0, ay0, az1); me0a1c755ccff88(ax1, ay0, az1); me0a1c755ccff88(ax1, ay1, az1); me0a1c755ccff88(ax0, ay1, az1);
    me0a1c755ccff88(ax0, ay0, az0); me0a1c755ccff88(ax0, ay0, az1); me0a1c755ccff88(ax0, ay1, az1); me0a1c755ccff88(ax0, ay1, az0);
    me0a1c755ccff88(ax1, ay0, az0); me0a1c755ccff88(ax1, ay1, az0); me0a1c755ccff88(ax1, ay1, az1); me0a1c755ccff88(ax1, ay0, az1);
    me383a8a14f149b();
}

void highlightCollisionBlock(Vec3 cam, double x, double y, double z) {
    double best = -1.0E9;
    int bx = 0, by = 0, bz = 0;
    boolean found = false;
    for (int cx = 0; cx < 2; cx++) {
        for (int cz = 0; cz < 2; cz++) {
            double sx = cx == 0 ? x - hitboxHalfWidth : x + hitboxHalfWidth;
            double sz = cz == 0 ? z - hitboxHalfWidth : z + hitboxHalfWidth;
            int ix = (int) Math.floor(sx);
            int iz = (int) Math.floor(sz);
            int top = (int) Math.floor(y);
            for (int iy = top; iy >= top - 5; iy--) {
                Block block = me055b71d99fb47(ix, iy, iz);
                if (block.name.equals("air") || block.name.contains("sign")) continue;
                double surf = iy + block.height;
                if (surf > best) { best = surf; bx = ix; by = iy; bz = iz; found = true; }
                break;
            }
        }
    }
    if (!found) return;
    colorRGBA(0xFF5555, 0.22f);
    boxFilled(cam, bx, by, bz, bx + 1, best, bz + 1);
    mdc3ab0a92aa4a4(1.5);
    colorRGBA(0xFF5555, 0.9f);
    boxOutline(cam, bx, by, bz, bx + 1, best, bz + 1);
}

void drawHitbox(Vec3 cam, double x, double y, double z) {
    mdc3ab0a92aa4a4(2.0);
    colorRGBA(0xFFFF55, 0.95f);
    boxOutline(cam, x - hitboxHalfWidth, y, z - hitboxHalfWidth,
            x + hitboxHalfWidth, y + hitboxHeight, z + hitboxHalfWidth);
}

void drawSmoothPath(Vec3 cam, double[] xs, double[] ys, double[] zs, int count) {
    if (count < 2) return;
    mdc3ab0a92aa4a4(2.0);
    colorRGBA(0x55FF66, 0.9f);
    mb7aa1d5d8e0d32(3);
    me0a1c755ccff88(xs[0] - cam.x, ys[0] - cam.y, zs[0] - cam.z);
    int sub = 6;
    for (int i = 0; i < count - 1; i++) {
        int i0 = i - 1; if (i0 < 0) i0 = 0;
        int i2 = i + 1;
        int i3 = i + 2; if (i3 > count - 1) i3 = count - 1;

        double p0x = xs[i0], p0y = ys[i0], p0z = zs[i0];
        double p1x = xs[i],  p1y = ys[i],  p1z = zs[i];
        double p2x = xs[i2], p2y = ys[i2], p2z = zs[i2];
        double p3x = xs[i3], p3y = ys[i3], p3z = zs[i3];

        for (int s = 1; s <= sub; s++) {
            double u = (double) s / sub;
            double u2 = u * u;
            double u3 = u2 * u;
            double cx = 0.5 * (2 * p1x + (-p0x + p2x) * u + (2 * p0x - 5 * p1x + 4 * p2x - p3x) * u2 + (-p0x + 3 * p1x - 3 * p2x + p3x) * u3);
            double cy = 0.5 * (2 * p1y + (-p0y + p2y) * u + (2 * p0y - 5 * p1y + 4 * p2y - p3y) * u2 + (-p0y + 3 * p1y - 3 * p2y + p3y) * u3);
            double cz = 0.5 * (2 * p1z + (-p0z + p2z) * u + (2 * p0z - 5 * p1z + 4 * p2z - p3z) * u2 + (-p0z + 3 * p1z - 3 * p2z + p3z) * u3);
            me0a1c755ccff88(cx - cam.x, cy - cam.y, cz - cam.z);
        }
    }
    me383a8a14f149b();
}

boolean simulateTrajectory(boolean draw, double maxDrop) {
    Entity player = ma1da5bf03ec1d5();
    Vec3 pos = player.getPosition();
    Vec3 motion = m561fb4b556a781();

    double px = pos.x, py = pos.y, pz = pos.z;
    double mx = motion.x, my = motion.y, mz = motion.z;
    double startY = py;

    double yawRad = player.getYaw() * Math.PI / 180.0;
    double sinYaw = mc1569e33fa2277(yawRad);
    double cosYaw = m854902085fd2c2(yawRad);

    double strafe = m7e79b326fc6e30();
    double forward = mc15bbb9c75c1ca();
    boolean sprint = player.isSprinting();
    boolean jumpHeld = m21eba45aea8060("jump");
    boolean grounded = player.onGround();

    double sprintMult = sprint ? 1.3 : 1.0;
    double groundFactor = 0.1 * sprintMult * 0.16277136 / (0.6 * 0.6 * 0.6);
    double airFactor = 0.02 * sprintMult;

    double[] xs = null, ys = null, zs = null;
    int count = 0;
    if (draw) {
        xs = new double[81];
        ys = new double[81];
        zs = new double[81];
        xs[0] = px; ys[0] = py; zs[0] = pz;
        count = 1;
    }

    boolean result = false;
    boolean landedFlag = false;

    for (int t = 0; t < 80; t++) {
        if (grounded && jumpHeld) {
            my = 0.42;
            if (sprint) {
                mx -= sinYaw * 0.2;
                mz += cosYaw * 0.2;
            }
            grounded = false;
        }

        double factor = grounded ? groundFactor : airFactor;
        double inSq = strafe * strafe + forward * forward;
        if (inSq >= 1.0E-4) {
            double n = m3fd9a8cdd0b860(inSq);
            if (n < 1.0) n = 1.0;
            n = factor / n;
            double s = strafe * n;
            double f = forward * n;
            mx += s * cosYaw - f * sinYaw;
            mz += f * cosYaw + s * sinYaw;
        }

        double nx = px + mx;
        if (hitboxBlocked(nx, py, pz)) {
            mx = 0.0;
        } else {
            px = nx;
        }
        double nz = pz + mz;
        if (hitboxBlocked(px, py, nz)) {
            mz = 0.0;
        } else {
            pz = nz;
        }

        boolean landed = false;
        double surface = surfaceBelowColumn(px, py, pz);
        if (grounded) {
            if (surface > -1.0E8 && m453a86ec2d6a16(surface - py) <= 1.0) {
                py = surface;
                my = 0.0;
                mx *= 0.546;
                mz *= 0.546;
            } else {
                grounded = false;
                my = (my - 0.08) * 0.98;
                mx *= 0.91;
                mz *= 0.91;
            }
        } else {
            double ny = py + my;
            if (my <= 0.0 && surface > -1.0E8 && py >= surface && ny <= surface) {
                py = surface;
                landed = true;
            } else {
                py = ny;
                my = (my - 0.08) * 0.98;
                mx *= 0.91;
                mz *= 0.91;
            }
        }

        if (draw) {
            xs[count] = px; ys[count] = py; zs[count] = pz;
            count++;
        }

        if (landed) {
            result = true;
            landedFlag = true;
            break;
        }
        if (py < startY - maxDrop) break;
        if (py < -64.0) break;
    }

    if (draw) {
        Vec3 cam = m184663ac3accc6();
        drawSmoothPath(cam, xs, ys, zs, count);
        if (landedFlag && count > 0) {
            highlightCollisionBlock(cam, xs[count - 1], ys[count - 1], zs[count - 1]);
            drawHitbox(cam, xs[count - 1], ys[count - 1], zs[count - 1]);
        }
    }
    return result;
}

boolean hitboxBlocked(double x, double y, double z) {
    int minX = (int) Math.floor(x - hitboxHalfWidth);
    int maxX = (int) Math.floor(x + hitboxHalfWidth);
    int minZ = (int) Math.floor(z - hitboxHalfWidth);
    int maxZ = (int) Math.floor(z + hitboxHalfWidth);
    int minY = (int) Math.floor(y + 0.001);
    int maxY = (int) Math.floor(y + hitboxHeight - 0.001);
    for (int bx = minX; bx <= maxX; bx++) {
        for (int bz = minZ; bz <= maxZ; bz++) {
            for (int by = minY; by <= maxY; by++) {
                Block block = me055b71d99fb47(bx, by, bz);
                if (block.name.equals("air") || block.name.contains("sign")) continue;
                if (by + block.height > y + 0.001) return true;
            }
        }
    }
    return false;
}

double surfaceBelowColumn(double x, double y, double z) {
    double best = -1.0E9;
    for (int cx = 0; cx < 2; cx++) {
        for (int cz = 0; cz < 2; cz++) {
            double sx = cx == 0 ? x - hitboxHalfWidth : x + hitboxHalfWidth;
            double sz = cz == 0 ? z - hitboxHalfWidth : z + hitboxHalfWidth;
            double s = columnSurface(sx, y, sz);
            if (s > best) best = s;
        }
    }
    return best;
}

double columnSurface(double x, double y, double z) {
    int bx = (int) Math.floor(x);
    int bz = (int) Math.floor(z);
    int top = (int) Math.floor(y);
    for (int by = top; by >= top - 5; by--) {
        Block block = me055b71d99fb47(bx, by, bz);
        if (block.name.equals("air") || block.name.contains("sign")) continue;
        double surf = by + block.height;
        if (surf > y + 0.001) continue;
        return surf;
    }
    return -1.0E9;
}

void onDisable() {
    if (!wass) {
        m273b7e3f39ca1e("Scaffold");
    }
}
27a7b452e1e23dab7f290adb124c20fe