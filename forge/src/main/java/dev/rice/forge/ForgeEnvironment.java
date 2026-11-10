package dev.rice.forge;

import dev.rice.inject.Mappings;
import dev.rice.inject.Platform;
import java.io.InputStream;
import net.minecraft.launchwrapper.Launch;

public final class ForgeEnvironment {
    private static Boolean development;
    private static Boolean initialized;
    private ForgeEnvironment() {}

    public static synchronized void setDevelopment(boolean value) {
        if (initialized != null && initialized.booleanValue() != value)
            throw new IllegalStateException("Forge namespace changed after Rice hooks loaded");
        development = value;
    }

    public static synchronized void initialize() {
        if (initialized != null) return;
        boolean dev = development != null ? development.booleanValue()
            : Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"));
        try {
            Platform.initialize(dev ? Platform.Namespace.NAMED : Platform.Namespace.SRG, null, false);
            if (dev) {
                Mappings.load(null);
            } else {
                InputStream input = ForgeEnvironment.class.getResourceAsStream("/dev/rice/forge/named-to-srg.txt");
                if (input == null) throw new IllegalStateException("Missing Forge mappings");
                Mappings.load(input);
            }
            // Reading class metadata avoids recursively loading Minecraft during transformation.
            Mappings.setHierarchyProvider(Hierarchy::parents);
            initialized = dev;
            Platform.log("Rice Modified Forge hooks: " + (dev ? "named" : "SRG"));
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot initialize Rice Forge mappings", failure);
        }
    }
}
