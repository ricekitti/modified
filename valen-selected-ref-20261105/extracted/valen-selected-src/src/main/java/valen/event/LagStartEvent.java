/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lag.api.LagRequest;
import valen.lunar.forge.fml.common.eventhandler.Event;

public final class LagStartEvent
extends Event {
    private final LagRequest request;
    private final boolean willStartSoon;

    public LagStartEvent(LagRequest lagRequest, boolean bl) {
        this.request = lagRequest;
        this.willStartSoon = bl;
    }

    public LagRequest getRequest() {
        return this.request;
    }

    public boolean willStartSoon() {
        return this.willStartSoon;
    }
}

