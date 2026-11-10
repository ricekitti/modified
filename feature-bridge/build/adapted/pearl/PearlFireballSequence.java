package dev.rice.module.impl.pearl;

import java.util.function.IntPredicate;

public final class PearlFireballSequence {
    private static final int EXPERIMENTAL_DELAY_TICKS = 1;
    private State state = State.IDLE;
    private int pearlSlot;
    private int fireballSlot;
    private int throwTick;
    private float yaw;
    private long deadline;

    public void begin(int n, int n2, float f, long l) {
        this.pearlSlot = n;
        this.fireballSlot = n2;
        this.yaw = f;
        this.state = n >= 0 && n2 >= 0 && n != n2 ? State.PEARL : State.FAILED;
        this.deadline = l + 1500L;
    }

    public boolean send(int n, long l, float f, float f2, boolean bl, IntPredicate intPredicate) {
        if (!bl || l >= this.deadline) {
            this.state = State.FAILED;
            return false;
        }
        float f3 = (f - this.yaw) % 360.0f;
        if (f3 > 180.0f) {
            f3 -= 360.0f;
        }
        if (f3 < -180.0f) {
            f3 += 360.0f;
        }
        if ((double)Math.abs(f3) > 0.01 || (double)Math.abs(f2 + 90.0f) > 0.01) {
            return false;
        }
        if (this.state == State.PEARL) {
            if (!intPredicate.test(this.pearlSlot)) {
                this.state = State.FAILED;
                return false;
            }
            this.throwTick = n;
            this.state = State.FIREBALL;
        } else if (this.state == State.FIREBALL && n - this.throwTick >= 1) {
            if (!intPredicate.test(this.fireballSlot)) {
                this.state = State.FAILED;
                return false;
            }
            this.state = State.FLIGHT;
            return true;
        }
        return false;
    }

    public State state() {
        return this.state;
    }

    public void reset() {
        this.state = State.IDLE;
    }

    public static enum State {
        IDLE,
        PEARL,
        FIREBALL,
        FLIGHT,
        FAILED;

    }
}

