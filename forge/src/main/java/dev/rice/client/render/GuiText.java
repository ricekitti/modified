package dev.rice.client.render;

import java.util.regex.Pattern;

/** Presentation only; module IDs, configuration values and source provenance stay intact. */
public final class GuiText {
    private static final Pattern OWN_NAME = Pattern.compile("(?i)(?<![a-z0-9_])(?:Rice Slinky|OpenSlinky|Slinky)(?![a-z0-9_])");
    private static final Pattern OTHER_NAMES = Pattern.compile("(?i)(?<![a-z0-9_])(?:Raven bS|Raven b\\+|Raven b4|RavenBS|Raven|OpenB4|Open B4|OpenJade|LiquidBounce|Myau|Mayu|Meow|Vape|Rise)(?![a-z0-9_])");
    private GuiText() {}
    public static String clean(String text) {
        if (text == null || text.isEmpty()) return text;
        text = OWN_NAME.matcher(text).replaceAll("Rice");
        return OTHER_NAMES.matcher(text).replaceAll("Standard");
    }
}
