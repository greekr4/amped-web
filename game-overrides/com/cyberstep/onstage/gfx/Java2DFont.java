/*
 * Decompiled with CFR 0.152.
 */
package com.cyberstep.onstage.gfx;

import com.cyberstep.onstage.gfx.Font;
import com.cyberstep.onstage.gfx.GLEngine;
import com.cyberstep.ring.util.Dict;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Locale;

public class Java2DFont
extends Font {
    static final String cashstr = "0123456789abcdefghijklmnopqrstuvwxyz\u3042\u3044\u3046\u3048\u304a\u304b\u304d\u304f\u3051\u3053\u3055\u3057\u3059\u305b\u305d\u305f\u3061\u3064\u3066\u3068\u306a\u306b\u306c\u306d\u306e\u306f\u3072\u3075\u3078\u307b\u307e\u307f\u3080\u3081\u3082\u3084\u3086\u3088\u3089\u308a\u308b\u308c\u308d\u308f\u3092\u3093\u30a2\u30a4\u30a6\u30a8\u30aa\u30ab\u30ad\u30af\u30b1\u30b3\u30b5\u30b7\u30b9\u30bb\u30bd\u30bf\u30c1\u30c4\u30c6\u30c8\u30ca\u30cb\u30cc\u30cd\u30ce\u30cf\u30d2\u30d5\u30d8\u30db\u30de\u30df\u30e0\u30e1\u30e2\u30e4\u30e6\u30e8\u30e9\u30ea\u30eb\u30ec\u30ed\u30ef\u30f2\u30f3";
    static Java2DFont defaultFont = new Java2DFont();
    static final int DEFAULT_FONT_SIZE = 16;
    private final int[] cache = new int[65535];
    private final Font.Character[] texCache = new Font.Character[1000];
    private int cacheCount;
    private BufferedImage bufferedImage;
    private Raster raster;
    private int[] buffer;
    private Graphics2D g;
    private java.awt.Font font;
    boolean antialias;
    int size = 16;
    boolean bold = false;
    Object exclusion = new Object();

    public Java2DFont() {
        ColorSpace colorSpace = ColorSpace.getInstance(1004);
        DataBufferInt dataBufferInt = new DataBufferInt(this.size * this.size * 2);
        SinglePixelPackedSampleModel singlePixelPackedSampleModel = new SinglePixelPackedSampleModel(3, this.size, this.size * 2, new int[]{0xFF0000, 65280, 255, -16777216});
        WritableRaster writableRaster = Raster.createWritableRaster(singlePixelPackedSampleModel, dataBufferInt, new Point(0, 0));
        ColorModel colorModel = ColorModel.getRGBdefault();
        this.bufferedImage = new BufferedImage(colorModel, writableRaster, false, null);
        this.raster = this.bufferedImage.getRaster();
        this.buffer = new int[this.size * (this.size * 2)];
        ColorModel colorModel2 = this.bufferedImage.getColorModel();
        SampleModel sampleModel = this.bufferedImage.getSampleModel();
        DataBuffer dataBuffer = this.raster.getDataBuffer();
        this.g = (Graphics2D)this.bufferedImage.getGraphics();
        this.g.setBackground(new Color(255, 255, 255, 0));
        this.g.setColor(Color.white);
        String[] stringArray = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.JAPANESE);
        String string = Dict.get("FontName");
        if (Arrays.asList(stringArray).contains(string)) {
            this.font = new java.awt.Font(string, 0, this.size - 1);
            if (string.startsWith("Gulim") || string.startsWith("Batang") || string.startsWith("GungsuhChe") || Locale.KOREAN.getLanguage().equals(Locale.getDefault().getLanguage())) {
                this.bold = true;
            }
        } else if (Locale.JAPANESE.getLanguage().equals(Locale.getDefault().getLanguage()) || Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage())) {
            int n = 0;
            while (n < stringArray.length) {
                if (stringArray[n].equals("HG\uff7a\uff9e\uff7c\uff6f\uff78E")) {
                    System.err.println("[Java2DFont#<init>] use HG\uff7a\uff9e\uff7c\uff6f\uff78E");
                    this.font = new java.awt.Font("HG\uff7a\uff9e\uff7c\uff6f\uff78E", 0, this.size - 1);
                    break;
                }
                if (stringArray[n].equals("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af")) {
                    System.err.println("[Java2DFont#<init>] use \uff2d\uff33 \u30b4\u30b7\u30c3\u30af");
                    this.font = new java.awt.Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", 1, this.size - 1);
                    break;
                }
                if (stringArray[n].equals("Osaka")) {
                    System.err.println("[Java2DFont#<init>] use Osaka");
                    this.font = new java.awt.Font("Osaka", 1, this.size - 1);
                    break;
                }
                ++n;
            }
        } else if (Locale.KOREAN.getLanguage().equals(Locale.getDefault().getLanguage())) {
            if (Arrays.asList(stringArray).contains("GulimChe")) {
                this.font = new java.awt.Font("GulimChe", 0, this.size - 1);
            } else if (Arrays.asList(stringArray).contains("GungsuhChe")) {
                this.font = new java.awt.Font("GungsuhChe", 0, this.size - 1);
            } else if (Arrays.asList(stringArray).contains("BatangChe")) {
                this.font = new java.awt.Font("BatangChe", 0, this.size - 1);
            } else if (Arrays.asList(stringArray).contains("Batang")) {
                this.font = new java.awt.Font("Batang", 0, this.size - 1);
            }
            this.bold = true;
            System.out.println("[Java2DFont#<init>] use " + this.font);
        }
        if (this.font == null) {
            // 웹 포트: 브라우저 JVM 기본 폰트에 CJK 가 없어 번들 폰트(Noto Sans JP, OFL)를 직접 로드한다
            try {
                java.io.File ttf = new java.io.File("/app/game/fonts/NotoSansJP-Regular.ttf");
                if (ttf.exists()) {
                    java.awt.Font base = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, ttf);
                    this.font = base.deriveFont(java.awt.Font.PLAIN, (float)(this.size - 1));
                    System.err.println("[Java2DFont#<init>] use bundled " + this.font.getFontName());
                }
            } catch (Exception e) {
                System.err.println("[Java2DFont#<init>] bundled font failed: " + e);
            }
        }
        if (this.font == null) {
            this.font = new java.awt.Font("SansSerif", 0, this.size - 1);
            System.err.println("[Java2DFont#<init>] use SansSerif: " + this.font.getFontName());
        }
        this.g.setFont(this.font);
        RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        this.g.addRenderingHints(renderingHints);
        this.cacheCount = 65535;
        this.clearCache();
        long l = System.currentTimeMillis();
        this.registerString(cashstr);
        long l2 = System.currentTimeMillis() - l;
        System.err.println("dt:" + l2 + " " + (float)l2 / 128.0f + "/char");
        this.g.dispose();
    }

    public void registerString(String string) {
        int n = 0;
        while (n < string.length()) {
            this.getCharacter(string.charAt(n));
            ++n;
        }
    }

    public static Java2DFont getDefaultFont() {
        return defaultFont;
    }

    private final void clearCache() {
        int n = 0;
        while (n < this.cacheCount) {
            this.cache[n] = 100000;
            ++n;
        }
        int n2 = 0;
        while (n2 < 1000) {
            this.texCache[n2] = null;
            ++n2;
        }
        this.cacheCount = 0;
    }

    public Font.Character getCharacter(char c) {
        try {
            if (this.cache[c] < 1000) {
                return this.texCache[this.cache[c]];
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            return this.texCache[this.cache[32]];
        }
        Font.Character character = new Font.Character();
        character.chr = c;
        character.pixtype = 2;
        character.width = character.height = this.size;
        // 웹 포트: CheerpJ 의 Java2D 텍스트 파이프라인은 CJK 글리프를 못 그린다 → 브라우저 캔버스로 래스터화
        character.pix = new byte[character.width * character.height];
        float adv = amped.web.WebRuntime.renderGlyph(c, this.size, character.pix);
        if (adv >= 0f) {
            character.orgx = 0.0f;
            character.orgy = 1.0f;
            character.fonth = 1.0f;
            character.fontw = adv;
            storeCharacter(c, character);
            return character;
        }
        Object object = this.exclusion;
        synchronized (object) {
            String string = new String(new char[]{c});
            FontRenderContext fontRenderContext = this.g.getFontRenderContext();
            Rectangle rectangle = this.font.getStringBounds(string, fontRenderContext).getBounds();
            LineMetrics lineMetrics = this.font.getLineMetrics(string, fontRenderContext);
            GlyphVector glyphVector = this.font.createGlyphVector(fontRenderContext, string);
            int n = (int)lineMetrics.getDescent();
            int n2 = (int)glyphVector.getGlyphMetrics(0).getLSB();
            boolean bl = rectangle.getWidth() <= (double)(this.size / 2);
            character.pix = new byte[character.width * character.height];
            character.orgx = 0.0f;
            character.orgy = 1.0f;
            character.fonth = 1.0f;
            character.fontw = glyphVector.getGlyphMetrics(0).getAdvance() / (float)this.size;
            this.g.setComposite(AlphaComposite.SrcIn);
            this.g.setBackground(new Color(255, 255, 255, 0));
            this.g.clearRect(0, 0, this.size, this.size * 2);
            this.g.setComposite(AlphaComposite.SrcOver);
            this.g.setColor(new Color(255, 255, 255, 255));
            this.g.drawString(string, 0, this.size);
            if (this.bold) {
                this.g.drawString(string, 1, this.size);
            }
            this.raster.getDataElements(0, n, this.size, this.size * 2 - n, this.buffer);
            if (!this.bold) {
                int n3 = 0;
                while (n3 < character.height) {
                    int n4 = 0;
                    while (n4 < this.size) {
                        character.pix[n3 * character.width + n4] = (byte)(this.buffer[n4 + n3 * this.size] >>> 24); // web: alpha channel
                        ++n4;
                    }
                    ++n3;
                }
            } else {
                int n5 = 0;
                while (n5 < character.height) {
                    int n6 = 0;
                    while (n6 < this.size) {
                        character.pix[n6 + n5 * character.width] = (byte)Math.min(255, ((this.buffer[n6 + n5 * this.size] >>> 24) & 0xFF) * 2); // web: alpha channel
                        ++n6;
                    }
                    ++n5;
                }
            }
        }
        storeCharacter(c, character);
        return character;
    }

    private void storeCharacter(char c, Font.Character character) {
        if (this.cacheCount >= this.texCache.length) {
            int n = (int)(Math.random() * (double)this.texCache.length);
            this.cache[this.texCache[n].chr] = 100000;
            GLEngine.removeTexture(this.texCache[n]);
            this.cache[c] = n;
            this.texCache[this.cache[c]] = character;
        } else {
            // CFR 디컴파일 오류 보정: 원본은 cache[c] = cacheCount++ 이다
            this.cache[c] = this.cacheCount++;
            this.texCache[this.cache[c]] = character;
        }
    }

    public float strwidth(String string) {
        return this.strwidth(string, 0, string.length());
    }

    public float strwidth(String string, int n, int n2) {
        float f = 0.0f;
        char[] cArray = string.toCharArray();
        int n3 = n;
        while (n3 < n2) {
            Font.Character character = this.getCharacter(cArray[n3]);
            f += character.fontw;
            ++n3;
        }
        return f;
    }

    public float chrwidth(char c) {
        Font.Character character = this.getCharacter(c);
        return character.fontw;
    }
}

