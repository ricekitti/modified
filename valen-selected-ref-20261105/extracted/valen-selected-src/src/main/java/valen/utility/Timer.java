/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

public class Timer {
    public float updates;
    public long last;
    public float cached = Float.NaN;

    public Timer(float f) {
        this.updates = f;
    }

    public float getValueFloat(float f, float f2, int n) {
        if (!Float.isNaN(this.cached) && this.cached == f2) {
            return this.cached;
        }
        float f3 = (float)(System.currentTimeMillis() - this.last) / this.updates;
        switch (n) {
            case 1: {
                f3 = f3 < 0.5f ? 4.0f * f3 * f3 * f3 : (f3 - 1.0f) * (2.0f * f3 - 2.0f) * (2.0f * f3 - 2.0f) + 1.0f;
                break;
            }
            case 2: {
                f3 = (float)(1.0 - Math.pow(1.0f - f3, 5.0));
                break;
            }
            case 3: {
                f3 = this.bounce(f3);
                break;
            }
            case 4: {
                f3 = this.quadInOut(f3);
            }
        }
        float f4 = f + f3 * (f2 - f);
        if (f2 > f && f4 > f2 || f2 < f && f4 < f2) {
            f4 = f2;
        }
        if (f4 == f2) {
            this.cached = f4;
        }
        return f4;
    }

    public int getValueInt(int n, int n2, int n3) {
        return Math.round(this.getValueFloat(n, n2, n3));
    }

    public void start() {
        this.cached = Float.NaN;
        this.last = System.currentTimeMillis();
    }

    private float bounce(float f) {
        float f2;
        double d = 7.5625;
        double d2 = 2.75;
        if ((double)f < 1.0 / d2) {
            f2 = (float)(d * (double)f * (double)f);
        } else if ((double)f < 2.0 / d2) {
            f = (float)((double)f - 1.5 / d2);
            f2 = (float)(d * (double)f * (double)f + 0.75);
        } else if ((double)f < 2.5 / d2) {
            f = (float)((double)f - 2.25 / d2);
            f2 = (float)(d * (double)f * (double)f + 0.9375);
        } else {
            f = (float)((double)f - 2.625 / d2);
            f2 = (float)(d * (double)f * (double)f + 0.984375);
        }
        return f2;
    }

    float quadInOut(float f) {
        if (f < 0.5f) {
            return 2.0f * f * f;
        }
        return -1.0f + (4.0f - 2.0f * f) * f;
    }
}

