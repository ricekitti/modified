package com.snowy.gui;
/** Current native client colors supplied to the preserved renderer helper. */
public final class Theme {
    private static int top=0xfff4b0ce,bottom=0xffaa82c8;
    public static int[] stops(){return new int[]{top,bottom};}
    public static void accent(int rgb){top=0xff000000|rgb;bottom=top;}
}
