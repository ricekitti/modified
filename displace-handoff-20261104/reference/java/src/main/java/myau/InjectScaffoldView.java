package myau;
/** Inert legacy renderer contract. Scaffold is removed and this view never owns state. */
public final class InjectScaffoldView extends Module {
 public final BooleanSetting itemSpoof=new BooleanSetting("item-spoof",false);
 public InjectScaffoldView(){super("Removed Scaffold compatibility",false,0);}
 public int getPreviousHotbarSlot(){return -1;}
 @Override public boolean isEnabled(){return false;}
}
