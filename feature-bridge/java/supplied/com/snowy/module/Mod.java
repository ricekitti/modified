package com.snowy.module;
import com.snowy.config.*;
import dev.rice.setting.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
/** Framework bridge only; no placement, movement or rotation algorithm. */
public abstract class Mod {
    protected final String cat;
    private boolean enabled;
    private final List<Property> properties=new ArrayList<>();
    protected Mod(Configuration configuration,String name,String category,String description){cat=name;}
    private String key(String label){return label.toLowerCase(Locale.ROOT).replace(' ','_');}
    protected Property mode(String group,String label,String initial,String[] options,String help){
        int index=Arrays.asList(options).indexOf(initial);
        Property p=new Property(new ChoiceSetting(key(label),label,index,options));properties.add(p);return p;
    }
    protected Property bool(String group,String label,boolean value,String help){
        Property p=new Property(new BooleanSetting(key(label),label,value));properties.add(p);return p;
    }
    protected BooleanSupplier whenOn(Property property){return property::getBoolean;}
    protected void shownWhen(Property property,BooleanSupplier condition){property.visible=condition;}
    public final List<Property> properties(){return Collections.unmodifiableList(properties);}
    public final boolean on(){return enabled;}
    public final void enabled(boolean value){if(value==enabled)return;enabled=value;if(value)onEnable();else onDisable();}
    public final void input(MovementInput input){onInput(input);}
    public final void preMove(EntityPlayerSP player){onPreMove(player);}
    public final void postMove(EntityPlayerSP player){onPostMove(player);}
    protected void onEnable(){}
    protected void onDisable(){}
    protected void onInput(MovementInput input){}
    protected void onPreMove(EntityPlayerSP player){}
    protected void onPostMove(EntityPlayerSP player){}
    public String suffix(){return "";}
}
