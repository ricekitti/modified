package dev.rice.mayu;


public class RenderOverlayEvent
extends Event {
    private final float partialTicks;

    public RenderOverlayEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}

