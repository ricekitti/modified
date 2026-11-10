package com.snowy.config;
import java.util.*;
/** Host-owned settings storage; the supplied module creates its original properties. */
public final class Configuration {
    private final Map<String,Map<String,Object>> categories=new HashMap<>();
    public Map<String,Object> getCategory(String name){return categories.computeIfAbsent(name,k->new HashMap<>());}
}
