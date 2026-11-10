/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package valen.lag.api;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import valen.lag.api.EnumLagDirection;
import valen.lag.timeout.AbstractTimeout;
import valen.lag.timeout.ModuleBackedTimeout;

public final class LagRequest {
    @NotNull
    private final Set<EnumLagDirection> directions;
    @NotNull
    private final AbstractTimeout timeout;
    private final long scheduledStartTimeMs;

    public LagRequest(@NotNull Set<EnumLagDirection> set, @NotNull AbstractTimeout abstractTimeout) {
        this.directions = set;
        this.timeout = abstractTimeout;
        long l = this.getDurationMs();
        this.scheduledStartTimeMs = l >= 90L ? System.currentTimeMillis() + 100L : System.currentTimeMillis();
    }

    @NotNull
    public Set<EnumLagDirection> getDirections() {
        return this.directions;
    }

    @NotNull
    public AbstractTimeout getTimeout() {
        return this.timeout;
    }

    public long getDurationMs() {
        if (this.timeout instanceof ModuleBackedTimeout) {
            return (long)((ModuleBackedTimeout)this.timeout).getModule().getLagDurationMs();
        }
        return this.timeout.getRemainingMs();
    }

    public long getScheduledStartTimeMs() {
        return this.scheduledStartTimeMs;
    }

    public boolean isPending() {
        return System.currentTimeMillis() < this.scheduledStartTimeMs;
    }
}

