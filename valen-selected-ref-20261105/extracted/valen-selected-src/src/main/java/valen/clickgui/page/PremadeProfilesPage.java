/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.page.GuiPage;
import valen.clickgui.page.PageUI;
import valen.utility.RenderUtils;
import valen.utility.config.PremadeProfiles;

public final class PremadeProfilesPage
implements GuiPage {
    private final List<PremadeProfiles.Entry> entries = PremadeProfiles.load();
    private final Map<String, String> added = new HashMap<String, String>();
    private float left;
    private float top;
    private float right;
    private float bottom;
    private float scroll;
    private long lastCheck;
    private String feedback;

    @Override
    public void render(float f, float f2, float f3, float f4, int n, int n2) {
        this.left = f;
        this.top = f2;
        this.right = f3;
        this.bottom = f4;
        if (System.currentTimeMillis() - this.lastCheck > 500L) {
            this.added.clear();
            for (PremadeProfiles.Entry entry : this.entries) {
                String string = Valen.configManager.findPremadeCopy(entry.id);
                if (string == null) continue;
                this.added.put(entry.id, string);
            }
            this.lastCheck = System.currentTimeMillis();
        }
        if (this.entries.isEmpty()) {
            PageUI.smallCentred("No premade profiles available.", f, f3, f2, f4, ClickGuiStyle.TEXT_MUTED);
            return;
        }
        RenderUtils.scissorPushGui(f, f2, f3 - f, f4 - f2 - 20.0f);
        for (int i = 0; i < this.entries.size(); ++i) {
            PremadeProfiles.Entry entry;
            entry = this.entries.get(i);
            float f5 = f2 + (float)i * 60.0f - this.scroll;
            PageUI.surface(f + 7.0f, f5, f3 - 7.0f, f5 + 54.0f, ClickGuiStyle.SURFACE);
            PageUI.label(PageUI.truncateLabel(entry.name, f3 - f - 110.0f), f + 19.0f, f5 + 10.0f, ClickGuiStyle.TEXT_PRIMARY);
            String string = entry.description;
            while (string.length() > 3 && PageUI.smallWidth(string) > f3 - f - 110.0f) {
                string = string.substring(0, string.length() - 4) + "...";
            }
            PageUI.small(string, f + 19.0f, f5 + 33.0f, ClickGuiStyle.TEXT_SECONDARY);
            boolean bl = this.added.containsKey(entry.id);
            PageUI.button(bl ? "Added" : "Add", f3 - 72.0f, f5 + 16.0f, f3 - 19.0f, f5 + 38.0f, bl ? ClickGuiStyle.SURFACE_ACTIVE : ClickGuiStyle.ACCENT_SOFT, ClickGuiStyle.TEXT_PRIMARY, PageUI.inside(n, n2, f3 - 72.0f, f5 + 16.0f, f3 - 19.0f, f5 + 38.0f) ? 1.0f : 0.0f, true);
        }
        RenderUtils.scissorPop();
        if (this.feedback != null) {
            PageUI.small(this.feedback, f + 7.0f, f4 - 14.0f, ClickGuiStyle.TEXT_SECONDARY);
        }
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (n3 != 0 || (float)n2 < this.top || (float)n2 >= this.bottom - 20.0f || (float)n < this.right - 72.0f || (float)n > this.right - 19.0f) {
            return false;
        }
        int n4 = (int)(((float)n2 - this.top + this.scroll) / 60.0f);
        if (n4 >= 0 && n4 < this.entries.size()) {
            float f = this.top + (float)n4 * 60.0f - this.scroll;
            if ((float)n2 < f + 16.0f || (float)n2 > f + 38.0f) {
                return false;
            }
            PremadeProfiles.Entry entry = this.entries.get(n4);
            if (!this.added.containsKey(entry.id)) {
                String string = Valen.configManager.addPremade(entry);
                String string2 = this.feedback = string == null ? "Could not add profile" : "Added " + string + " to Profiles";
                if (string != null) {
                    this.added.put(entry.id, string);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(int n, int n2, int n3) {
        if (!PageUI.inside(n2, n3, this.left, this.top, this.right, this.bottom)) {
            return false;
        }
        this.scroll = Math.max(0.0f, Math.min(Math.max(0.0f, (float)this.entries.size() * 60.0f - (this.bottom - this.top - 20.0f)), this.scroll - (float)n / 120.0f * 34.0f));
        return true;
    }

    @Override
    public boolean keyTyped(char c, int n) {
        return false;
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
    }

    @Override
    public void onClosed() {
        this.scroll = 0.0f;
        this.feedback = null;
        this.lastCheck = 0L;
    }
}

