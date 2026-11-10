/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.page;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import valen.clickgui.animation.ToggleAnimation;

public final class HoverSet {
    private static final long DEFAULT_DURATION_MS = 130L;
    private final Map<String, ToggleAnimation> animations = new HashMap<String, ToggleAnimation>();
    private final long durationMs;

    public HoverSet() {
        this(130L);
    }

    public HoverSet(long l) {
        this.durationMs = l;
    }

    public float ease(String string, boolean bl) {
        ToggleAnimation toggleAnimation = this.animations.get(string);
        if (toggleAnimation == null) {
            toggleAnimation = new ToggleAnimation(this.durationMs);
            this.animations.put(string, toggleAnimation);
        }
        toggleAnimation.setTarget(bl);
        return toggleAnimation.getValue();
    }

    public void retain(Set<String> set) {
        this.animations.keySet().retainAll(set);
    }

    public void clear() {
        this.animations.clear();
    }
}

