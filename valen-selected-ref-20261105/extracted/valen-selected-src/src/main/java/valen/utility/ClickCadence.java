/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.util.Random;

public final class ClickCadence {
    private boolean recovery;
    private double drift;
    private double fatigue = 1.0;
    private int fatigueClicks;

    public long nextDelay(int n, double d, boolean bl, Random random) {
        double d2;
        double d3 = 1000.0 / Math.max(1.0, d);
        if (n == 0) {
            return ClickCadence.normalDelay(d, bl, random);
        }
        if (bl && --this.fatigueClicks <= 0) {
            this.fatigue = 1.0 + random.nextDouble() * 0.12;
            this.fatigueClicks = 30 + random.nextInt(50);
        }
        if (n == 2) {
            d2 = this.recovery ? 1.55 : 0.45;
            this.recovery = !this.recovery;
            d2 += (random.nextDouble() - 0.5) * 0.08;
        } else {
            this.drift = Math.max(-0.08, Math.min(0.08, this.drift * 0.96 + (random.nextDouble() - 0.5) * 0.025));
            d2 = 1.0 + this.drift + (random.nextDouble() - 0.5) * 0.16;
        }
        return Math.max(1L, Math.round(d3 * d2 * (bl ? this.fatigue : 1.0)));
    }

    public static long normalDelay(double d, boolean bl, Random random) {
        double d2;
        double d3 = d2 = 1000.0 / Math.max(1.0, d);
        if (bl) {
            d3 += (double)random.nextInt((int)d2 + 1) - d2 / 2.0;
            if (random.nextInt(100) < 15) {
                double d4 = d3 = random.nextBoolean() ? (double)(25 + random.nextInt(16)) : d2 + 50.0 + (double)random.nextInt(41);
            }
            if (random.nextInt(100) < 8) {
                d3 *= (double)(50 + random.nextInt(151)) / 100.0;
            }
            if (random.nextInt(100) < 10) {
                d3 += (double)(10 + random.nextInt(26));
            }
        } else {
            d3 += (double)(random.nextInt(21) - 10);
        }
        return Math.round(Math.max(33.0, Math.min(180.0, d3)));
    }

    public void reset() {
        this.recovery = false;
        this.drift = 0.0;
        this.fatigue = 1.0;
        this.fatigueClicks = 0;
    }
}

