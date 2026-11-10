/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL11
 *  valen.utility.font.ValenFontRenderer$GlyphColorProvider
 */
package valen.utility.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import valen.utility.ScaledResolutionCache;
import valen.utility.font.ValenFontRenderer;

public final class GlyphFontRenderer
implements ValenFontRenderer {
    private static final int FIRST_GLYPH = 0;
    private static final int LAST_GLYPH = 255;
    private static final int CHANNEL_MASK = 255;
    private static final int GLYPH_MARGIN = 4;
    private static final float MIN_RENDER_SCALE = 2.0f;
    private static final float QUALITY_MULTIPLIER = 2.0f;
    private static final String ALPHABET = "ABCDEFGHOKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String COLOR_CODES = "0123456789abcdefklmnor";
    private final Font renderFont;
    private final boolean antiAlias;
    private final FontRenderContext fontRenderContext;
    private final GlyphData[] defaultGlyphs = new GlyphData[256];
    private final Map<Character, GlyphData> extendedGlyphs = new ConcurrentHashMap<Character, GlyphData>();
    private final float drawScale;
    private final float rawScale;
    private final float rawTextTop;
    private final float rawTextBottom;
    private final float fontHeight;
    private final float lineHeight;
    private final float capCenter;
    private boolean destroyed;

    public GlyphFontRenderer(Font font, boolean bl) {
        float f = GlyphFontRenderer.resolveRenderScale();
        this.drawScale = 1.0f / f;
        this.rawScale = f;
        this.renderFont = font.deriveFont(font.getStyle(), Math.max(1.0f, font.getSize2D() * f));
        this.antiAlias = bl;
        this.fontRenderContext = new FontRenderContext(new AffineTransform(), bl, true);
        for (int i = 0; i <= 255; ++i) {
            this.defaultGlyphs[i] = this.createGlyph((char)i);
        }
        this.rawTextTop = this.computeRawTextTop();
        this.rawTextBottom = this.computeRawTextBottom();
        this.fontHeight = Math.max(1.0f, (this.rawTextBottom - this.rawTextTop) * this.drawScale);
        this.lineHeight = this.computeLineHeight();
        this.capCenter = this.computeCapCenter();
    }

    @Override
    public int drawString(String string, float f, float f2, int n, boolean bl) {
        if (this.destroyed || string == null || string.isEmpty()) {
            return 0;
        }
        int n2 = 0;
        if (bl) {
            n2 = this.drawInternal(string, f + 0.5f, f2 + 0.5f, n, true);
        }
        return Math.max(n2, this.drawInternal(string, f, f2, n, false));
    }

    @Override
    public int drawGlyphString(String string, float f, float f2, ValenFontRenderer.GlyphColorProvider glyphColorProvider, boolean bl) {
        if (this.destroyed || string == null || string.isEmpty()) {
            return 0;
        }
        int n = 0;
        if (bl) {
            n = this.drawGlyphInternal(string, f + 0.5f, f2 + 0.5f, glyphColorProvider, true);
        }
        return Math.max(n, this.drawGlyphInternal(string, f, f2, glyphColorProvider, false));
    }

    @Override
    public int getStringWidth(String string) {
        if (string == null || string.isEmpty()) {
            return 0;
        }
        float f = 0.0f;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (GlyphFontRenderer.isMalformedSectionPrefix(string, i)) continue;
            if (c == '\u00a7' && i + 1 < string.length()) {
                ++i;
                continue;
            }
            if (c == '\n') continue;
            f += this.getGlyph(c).advance;
        }
        return Math.round(f);
    }

    @Override
    public int getFontHeight() {
        return Math.round(this.fontHeight);
    }

    @Override
    public int getLineHeight() {
        return Math.round(this.lineHeight);
    }

    @Override
    public int getTextTopOffset() {
        return 0;
    }

    @Override
    public int getTextBottomOffset() {
        return Math.round(this.fontHeight);
    }

    @Override
    public float getCapCenter() {
        return this.capCenter;
    }

    @Override
    public void destroy() {
        if (this.destroyed) {
            return;
        }
        this.destroyed = true;
        GlyphFontRenderer.deleteGlyphTextures(this.defaultGlyphs);
        for (GlyphData glyphData : this.extendedGlyphs.values()) {
            GlyphFontRenderer.deleteGlyphTexture(glyphData);
        }
        this.extendedGlyphs.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int drawInternal(String string, float f, float f2, int n, boolean bl) {
        float f3;
        int n2 = n >>> 24 & 0xFF;
        if (n2 == 0) {
            n2 = 255;
        }
        int n3 = bl ? GlyphFontRenderer.applyShadowColor(n) : GlyphFontRenderer.withAlpha(n, n2);
        float f4 = f3 = f * this.rawScale;
        float f5 = f2 * this.rawScale - this.rawTextTop;
        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi((int)8960, (int)8704, (int)8448);
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.scale((float)this.drawScale, (float)this.drawScale, (float)1.0f);
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (GlyphFontRenderer.isMalformedSectionPrefix(string, i)) continue;
                if (c == '\u00a7' && i + 1 < string.length()) {
                    char c2;
                    int n4;
                    if ((n4 = COLOR_CODES.indexOf(c2 = Character.toLowerCase(string.charAt(++i)))) < 0) continue;
                    if (n4 < 16) {
                        n3 = GlyphFontRenderer.getMinecraftColor(n4, n2, bl);
                        continue;
                    }
                    if (c2 != 'r') continue;
                    n3 = bl ? GlyphFontRenderer.applyShadowColor(n) : GlyphFontRenderer.withAlpha(n, n2);
                    continue;
                }
                if (c == '\n') {
                    f4 = f3;
                    f5 += this.lineHeight * this.rawScale;
                    continue;
                }
                GlyphData glyphData = this.getGlyph(c);
                if (glyphData.textureId != 0 && glyphData.textureWidth > 0.0f && glyphData.textureHeight > 0.0f) {
                    this.renderGlyph(glyphData, f4 - 4.0f, f5, n3);
                }
                f4 += glyphData.rawAdvance;
            }
        }
        finally {
            GlStateManager.bindTexture((int)0);
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode((int)5888);
        }
        return Math.round((f4 - f3) * this.drawScale);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int drawGlyphInternal(String string, float f, float f2, ValenFontRenderer.GlyphColorProvider glyphColorProvider, boolean bl) {
        float f3;
        float f4 = f3 = f * this.rawScale;
        float f5 = f2 * this.rawScale - this.rawTextTop;
        Integer n = null;
        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi((int)8960, (int)8704, (int)8448);
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.scale((float)this.drawScale, (float)this.drawScale, (float)1.0f);
            for (int i = 0; i < string.length(); ++i) {
                int n2;
                char c = string.charAt(i);
                if (GlyphFontRenderer.isMalformedSectionPrefix(string, i)) continue;
                if (c == '\u00a7' && i + 1 < string.length()) {
                    char c2;
                    if ((n2 = COLOR_CODES.indexOf(c2 = Character.toLowerCase(string.charAt(++i)))) >= 0 && n2 < 16) {
                        n = GlyphFontRenderer.getMinecraftColor(n2, 255, false);
                        continue;
                    }
                    if (c2 != 'r') continue;
                    n = null;
                    continue;
                }
                if (c == '\n') {
                    f4 = f3;
                    f5 += this.lineHeight * this.rawScale;
                    continue;
                }
                GlyphData glyphData = this.getGlyph(c);
                n2 = glyphColorProvider.colorForGlyph(c, (f4 - f3) * this.drawScale, glyphData.advance, n);
                int n3 = n2 >>> 24 & 0xFF;
                if (n3 == 0) {
                    n3 = 255;
                }
                n2 = GlyphFontRenderer.withAlpha(n2, n3);
                if (bl) {
                    n2 = GlyphFontRenderer.applyShadowColor(n2);
                }
                if (glyphData.textureId != 0 && glyphData.textureWidth > 0.0f && glyphData.textureHeight > 0.0f) {
                    this.renderGlyph(glyphData, f4 - 4.0f, f5, n2);
                }
                f4 += glyphData.rawAdvance;
            }
        }
        finally {
            GlStateManager.bindTexture((int)0);
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode((int)5888);
        }
        return Math.round((f4 - f3) * this.drawScale);
    }

    private void renderGlyph(GlyphData glyphData, float f, float f2, int n) {
        GlStateManager.bindTexture((int)glyphData.textureId);
        float f3 = (float)(n >>> 24 & 0xFF) / 255.0f;
        float f4 = (float)(n >>> 16 & 0xFF) / 255.0f;
        float f5 = (float)(n >>> 8 & 0xFF) / 255.0f;
        float f6 = (float)(n & 0xFF) / 255.0f;
        GlStateManager.color((float)f4, (float)f5, (float)f6, (float)f3);
        GL11.glBegin((int)7);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex2f((float)f, (float)f2);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex2f((float)f, (float)(f2 + glyphData.textureHeight));
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex2f((float)(f + glyphData.textureWidth), (float)(f2 + glyphData.textureHeight));
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex2f((float)(f + glyphData.textureWidth), (float)f2);
        GL11.glEnd();
    }

    private GlyphData getGlyph(char c) {
        GlyphData glyphData;
        if (c >= '\u0000' && c <= '\u00ff' && (glyphData = this.defaultGlyphs[c]) != null) {
            return glyphData;
        }
        return this.extendedGlyphs.computeIfAbsent(Character.valueOf(c), this::createGlyph);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private GlyphData createGlyph(char c) {
        if (this.destroyed) {
            return new GlyphData(0, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0);
        }
        if (Character.isISOControl(c) && c != '\n') {
            return new GlyphData(0, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0);
        }
        String string = String.valueOf(c);
        BufferedImage bufferedImage = new BufferedImage(1, 1, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        try {
            graphics2D.setFont(this.renderFont);
            this.applyRenderHints(graphics2D);
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            Rectangle2D rectangle2D = fontMetrics.getStringBounds(string, graphics2D);
            float f = Math.max(1.0f, (float)rectangle2D.getWidth());
            int n = Math.max(1, (int)Math.ceil(f) + 8);
            int n2 = Math.max(1, fontMetrics.getHeight());
            BufferedImage bufferedImage2 = new BufferedImage(n, n2, 2);
            Graphics2D graphics2D2 = bufferedImage2.createGraphics();
            try {
                graphics2D2.setFont(this.renderFont);
                graphics2D2.setBackground(new Color(255, 255, 255, 0));
                graphics2D2.clearRect(0, 0, n, n2);
                graphics2D2.setColor(Color.WHITE);
                this.applyRenderHints(graphics2D2);
                graphics2D2.drawString(string, 4, fontMetrics.getAscent());
            }
            finally {
                graphics2D2.dispose();
            }
            int n3 = this.uploadTexture(bufferedImage2);
            int[] nArray = GlyphFontRenderer.findVisibleRowBounds(bufferedImage2);
            GlyphData glyphData = new GlyphData(n3, n, n2, f, f * this.drawScale, nArray[0], nArray[1]);
            return glyphData;
        }
        finally {
            graphics2D.dispose();
        }
    }

    private int uploadTexture(BufferedImage bufferedImage) {
        int n;
        int n2 = bufferedImage.getWidth();
        int n3 = bufferedImage.getHeight();
        int[] nArray = bufferedImage.getRGB(0, 0, n2, n3, new int[n2 * n3], 0, n2);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)(n2 * n3 * 4));
        for (n = 0; n < n3; ++n) {
            for (int i = 0; i < n2; ++i) {
                int n4 = nArray[i + n * n2];
                byteBuffer.put((byte)(n4 >> 16 & 0xFF));
                byteBuffer.put((byte)(n4 >> 8 & 0xFF));
                byteBuffer.put((byte)(n4 & 0xFF));
                byteBuffer.put((byte)(n4 >> 24 & 0xFF));
            }
        }
        byteBuffer.flip();
        n = GL11.glGenTextures();
        GlStateManager.bindTexture((int)n);
        GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        GL11.glTexImage2D((int)3553, (int)0, (int)6408, (int)n2, (int)n3, (int)0, (int)6408, (int)5121, (ByteBuffer)byteBuffer);
        return n;
    }

    private float computeRawTextTop() {
        float f = Float.MAX_VALUE;
        for (int i = 0; i < ALPHABET.length(); ++i) {
            GlyphData glyphData = this.getGlyph(ALPHABET.charAt(i));
            if (!glyphData.hasVisiblePixels()) continue;
            f = Math.min(f, (float)glyphData.visibleTop);
        }
        return f == Float.MAX_VALUE ? 0.0f : f;
    }

    private float computeRawTextBottom() {
        float f = 0.0f;
        for (int i = 0; i < ALPHABET.length(); ++i) {
            GlyphData glyphData = this.getGlyph(ALPHABET.charAt(i));
            if (!glyphData.hasVisiblePixels()) continue;
            f = Math.max(f, (float)glyphData.visibleBottom);
        }
        if (f <= 0.0f) {
            return Math.max(1.0f, this.renderFont.getSize2D());
        }
        return f;
    }

    private float computeCapCenter() {
        GlyphData glyphData = this.getGlyph('H');
        if (!glyphData.hasVisiblePixels()) {
            return this.fontHeight / 2.0f;
        }
        return ((float)(glyphData.visibleTop + glyphData.visibleBottom) / 2.0f - this.rawTextTop) * this.drawScale;
    }

    private float computeLineHeight() {
        return Math.max(this.fontHeight, (float)this.renderFont.getStringBounds(ALPHABET, this.fontRenderContext).getHeight() * this.drawScale);
    }

    private static int[] findVisibleRowBounds(BufferedImage bufferedImage) {
        int n = bufferedImage.getWidth();
        int n2 = bufferedImage.getHeight();
        int n3 = -1;
        int n4 = -1;
        block0: for (int i = 0; i < n2; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((bufferedImage.getRGB(j, i) >>> 24 & 0xFF) == 0) continue;
                if (n3 == -1) {
                    n3 = i;
                }
                n4 = i + 1;
                continue block0;
            }
        }
        if (n3 == -1) {
            return new int[]{0, 0};
        }
        return new int[]{n3, n4};
    }

    private static float resolveRenderScale() {
        int n = 1;
        try {
            n = Math.max(1, ScaledResolutionCache.get().getScaleFactor());
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Math.max(2.0f, (float)n * 2.0f);
    }

    private void applyRenderHints(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, this.antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, this.antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static void deleteGlyphTextures(GlyphData[] glyphDataArray) {
        for (int i = 0; i < glyphDataArray.length; ++i) {
            GlyphFontRenderer.deleteGlyphTexture(glyphDataArray[i]);
        }
    }

    private static void deleteGlyphTexture(GlyphData glyphData) {
        if (glyphData != null && glyphData.textureId != 0) {
            GL11.glDeleteTextures((int)glyphData.textureId);
        }
    }

    private static int getMinecraftColor(int n, int n2, boolean bl) {
        int n3 = (n >> 3 & 1) * 85;
        int n4 = (n >> 2 & 1) * 170 + n3;
        int n5 = (n >> 1 & 1) * 170 + n3;
        int n6 = (n & 1) * 170 + n3;
        if (n == 6) {
            n4 += 85;
        }
        if (bl) {
            n4 /= 4;
            n5 /= 4;
            n6 /= 4;
        }
        return n2 << 24 | n4 << 16 | n5 << 8 | n6;
    }

    private static int applyShadowColor(int n) {
        int n2 = n >>> 24 & 0xFF;
        if (n2 == 0) {
            n2 = 255;
        }
        int n3 = (n >>> 16 & 0xFF) / 4;
        int n4 = (n >>> 8 & 0xFF) / 4;
        int n5 = (n & 0xFF) / 4;
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    private static int withAlpha(int n, int n2) {
        return n2 << 24 | n & 0xFFFFFF;
    }

    private static boolean isMalformedSectionPrefix(String string, int n) {
        if (!GlyphFontRenderer.isFormattingArtifact(string.charAt(n))) {
            return false;
        }
        for (int i = n + 1; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\u00a7') {
                return true;
            }
            if (GlyphFontRenderer.isFormattingArtifact(c)) continue;
            return false;
        }
        return false;
    }

    private static boolean isFormattingArtifact(char c) {
        return c == '\u00c2' || c == '\u00c3' || c == '\u0082' || c == '\u201a';
    }

    private static final class GlyphData {
        private final int textureId;
        private final float textureWidth;
        private final float textureHeight;
        private final float rawAdvance;
        private final float advance;
        private final int visibleTop;
        private final int visibleBottom;

        private GlyphData(int n, float f, float f2, float f3, float f4, int n2, int n3) {
            this.textureId = n;
            this.textureWidth = f;
            this.textureHeight = f2;
            this.rawAdvance = f3;
            this.advance = f4;
            this.visibleTop = n2;
            this.visibleBottom = n3;
        }

        private boolean hasVisiblePixels() {
            return this.visibleBottom > this.visibleTop;
        }
    }
}

