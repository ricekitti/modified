package dev.rice.anticheat;
public final class Property {
    private Object value;
    Property(Object value){this.value=value;}
    public boolean getBoolean(){return Boolean.TRUE.equals(value);}
    public boolean getBoolean(boolean fallback){return value instanceof Boolean?(Boolean)value:fallback;}
    public int getInt(int fallback){return value instanceof Number?((Number)value).intValue():fallback;}
    public double getDouble(double fallback){return value instanceof Number?((Number)value).doubleValue():fallback;}
    public void set(boolean value){this.value=value;}
    public void set(int value){this.value=value;}
    public void set(double value){this.value=value;}
}
