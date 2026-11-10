package dev.rice.anticheat;
import java.util.*;
public final class Configuration {
    public static final class Category extends LinkedHashMap<String,Property>{}
    private final Map<String,Category> categories=new LinkedHashMap<>();
    public Category getCategory(String name){return categories.computeIfAbsent(name,k->new Category());}
    private Property create(String category,String key,Object value){Category c=getCategory(category);if(!c.containsKey(key))c.put(key,new Property(value));return c.get(key);}
    public Property get(String category,String key,boolean value,String description){return create(category,key,value);}
    public Property get(String category,String key,int value,String description,int min,int max){return create(category,key,value);}
    public Property get(String category,String key,double value,String description,double min,double max){return create(category,key,value);}
}
