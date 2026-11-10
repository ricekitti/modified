package com.snowy.util;
import com.snowy.module.Mod;
import java.util.*;
/** Resource arbitration for the supplied modules hosted in this payload. */
public final class Claims {
    public enum Resource {LOOK,ATTACK,MOVE,PLACE,USE,SLOT}
    public interface Holder {boolean holds(Resource resource);}
    private static final class Entry{
        final Mod owner;final int priority;final Holder holder;
        Entry(Mod owner,int priority,Holder holder){this.owner=owner;this.priority=priority;this.holder=holder;}
    }
    private static final List<Entry> entries=new ArrayList<>();
    public static void register(Mod owner,int priority,Holder holder){entries.add(new Entry(owner,priority,holder));}
    public static boolean blocked(Object owner,Resource resource){
        int priority=Integer.MIN_VALUE;for(Entry e:entries)if(e.owner==owner)priority=e.priority;
        return blocked(owner,priority,resource);
    }
    public static boolean blocked(Object owner,int priority,Resource resource){
        for(Entry e:entries)if(e.owner!=owner&&e.owner.on()&&e.priority>priority&&e.holder.holds(resource))return true;
        return false;
    }
    public static void unregister(Mod owner){entries.removeIf(e->e.owner==owner);}
}
