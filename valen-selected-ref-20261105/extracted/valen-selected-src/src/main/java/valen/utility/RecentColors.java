/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 */
package valen.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class RecentColors {
    public static final int CAPACITY = 12;
    private final List<Integer> colors = new ArrayList<Integer>();

    public List<Integer> list() {
        return Collections.unmodifiableList(new ArrayList<Integer>(this.colors));
    }

    public boolean push(int n) {
        Integer n2 = n | 0xFF000000;
        if (!this.colors.isEmpty() && this.colors.get(0).equals(n2)) {
            return false;
        }
        this.colors.remove(n2);
        this.colors.add(0, n2);
        while (this.colors.size() > 12) {
            this.colors.remove(this.colors.size() - 1);
        }
        return true;
    }

    public void load(JsonArray jsonArray) {
        this.colors.clear();
        if (jsonArray == null) {
            return;
        }
        for (JsonElement jsonElement : jsonArray) {
            Integer n;
            if (this.colors.size() >= 12) break;
            if (!jsonElement.isJsonPrimitive() || (n = RecentColors.parse(jsonElement.getAsString())) == null || this.colors.contains(n)) continue;
            this.colors.add(n);
        }
    }

    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (Integer n : this.colors) {
            jsonArray.add((JsonElement)new JsonPrimitive(RecentColors.hex(n)));
        }
        return jsonArray;
    }

    public static String hex(int n) {
        return String.format(Locale.ROOT, "#%06X", n & 0xFFFFFF);
    }

    public static Integer parse(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim();
        if (string2.startsWith("#")) {
            string2 = string2.substring(1);
        }
        if (string2.length() == 3) {
            StringBuilder stringBuilder = new StringBuilder(6);
            for (int i = 0; i < 3; ++i) {
                stringBuilder.append(string2.charAt(i)).append(string2.charAt(i));
            }
            string2 = stringBuilder.toString();
        }
        if (string2.length() != 6) {
            return null;
        }
        for (int i = 0; i < string2.length(); ++i) {
            if (Character.digit(string2.charAt(i), 16) >= 0) continue;
            return null;
        }
        return Integer.parseInt(string2, 16) | 0xFF000000;
    }
}

