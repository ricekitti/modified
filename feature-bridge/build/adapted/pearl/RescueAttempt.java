/*
 * Decompiled with CFR 0.152.
 */
package dev.rice.module.impl.pearl;

public final class RescueAttempt {
    private State state = State.IDLE;
    private int throwTick;
    private long deadline;

    public void begin(long l, long l2) {
        this.state = State.AIMING;
        this.deadline = l + l2;
    }

    public void thrown(int n) {
        if (this.state == State.AIMING) {
            this.throwTick = n;
            this.state = State.THROWN;
        }
    }

    public boolean advance(int n, long l, boolean bl, boolean bl2) {
        if (!this.active()) {
            return false;
        }
        if (bl2 || l >= this.deadline) {
            this.state = State.FAILED;
        } else if (bl) {
            this.state = State.COMPLETE;
        } else if (this.state == State.THROWN && n > this.throwTick) {
            this.state = State.DELAYED_SLOWDOWN;
        } else if (this.state == State.DELAYED_SLOWDOWN) {
            this.state = State.WAITING_FOR_SUPPORT;
        }
        return this.slowing();
    }

    public boolean active() {
        return this.state == State.AIMING || this.state == State.THROWN || this.slowing();
    }

    public boolean slowing() {
        return this.state == State.DELAYED_SLOWDOWN || this.state == State.WAITING_FOR_SUPPORT;
    }

    public State state() {
        return this.state;
    }

    public void fail() {
        this.state = State.FAILED;
    }

    public void reset() {
        this.state = State.IDLE;
        this.deadline = 0L;
    }

    public static enum State {
        IDLE,
        AIMING,
        THROWN,
        DELAYED_SLOWDOWN,
        WAITING_FOR_SUPPORT,
        COMPLETE,
        FAILED;

    }
}

