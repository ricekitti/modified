/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar.forge.fml.common.eventhandler;

import valen.lunar.forge.fml.common.eventhandler.Cancelable;

public class Event {
    private boolean canceled;
    private Result result = Result.DEFAULT;

    public boolean isCancelable() {
        for (Class<?> clazz = this.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (!clazz.isAnnotationPresent(Cancelable.class)) continue;
            return true;
        }
        return false;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean bl) {
        if (!this.isCancelable()) {
            throw new IllegalArgumentException("Event is not cancelable");
        }
        this.canceled = bl;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static enum Result {
        DENY,
        DEFAULT,
        ALLOW;

    }
}

