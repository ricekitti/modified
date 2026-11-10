/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package valen.lag.timeout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import valen.lag.timeout.AbstractTimeout;
import valen.module.Module;

public final class ModuleBackedTimeout
extends AbstractTimeout {
    @NotNull
    private final Module module;
    @Nullable
    private final AbstractTimeout secondaryTimeout;
    private boolean hasModuleDisabled = false;
    private final long startTimeMs = System.currentTimeMillis();

    public ModuleBackedTimeout(@NotNull Module module, @Nullable AbstractTimeout abstractTimeout) {
        this.module = module;
        this.secondaryTimeout = abstractTimeout;
        if (!module.isEnabled()) {
            this.hasModuleDisabled = true;
        }
    }

    public ModuleBackedTimeout(@NotNull Module module) {
        this(module, null);
    }

    @Override
    protected boolean shouldHaveTimedOut() {
        if (!this.module.isEnabled()) {
            this.hasModuleDisabled = true;
        }
        if (this.hasModuleDisabled) {
            return true;
        }
        return this.secondaryTimeout != null && this.secondaryTimeout.isTimedOut();
    }

    @Override
    protected long getTimeoutDurationMs() {
        return this.secondaryTimeout != null ? this.secondaryTimeout.getTimeoutDurationMs() : 0L;
    }

    @Override
    protected long getStartTimeMs() {
        return this.startTimeMs;
    }

    @NotNull
    public Module getModule() {
        return this.module;
    }
}

