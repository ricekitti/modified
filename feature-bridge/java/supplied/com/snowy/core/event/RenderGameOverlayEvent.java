package com.snowy.core.event;
import net.minecraft.client.gui.ScaledResolution;
public class RenderGameOverlayEvent {
    public static final class Post {
        public final ScaledResolution resolution;
        public Post(ScaledResolution resolution){this.resolution=resolution;}
    }
}
