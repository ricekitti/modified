package com.snowy.config;
import dev.rice.setting.*;
import java.util.function.BooleanSupplier;
public final class Property {
    public final Setting<?> host;
    public BooleanSupplier visible=()->true;
    public Property(Setting<?> host){this.host=host;}
    public boolean getBoolean(){return ((BooleanSetting)host).isEnabled();}
    public String getString(){return ((ChoiceSetting)host).selectedLabel();}
}
