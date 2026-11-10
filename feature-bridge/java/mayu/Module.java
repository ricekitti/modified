package dev.rice.mayu;

/** Lifecycle adapter only; behavior lives in the original Scaffold class. */
public abstract class Module {
    private boolean enabled;
    protected Module(String name,boolean enabled,int key){this.enabled=enabled;}
    public final boolean isEnabled(){return enabled;}
    public final void setEnabled(boolean value){if(value==enabled)return;enabled=value;if(value)onEnable();else onDisable();}
    public void onEnable(){}
    public void onDisable(){}
    public void onSettingChanged(String name){}
}
