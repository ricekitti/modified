/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar.forge.fml.common.eventhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Set;
import valen.lunar.forge.fml.common.eventhandler.Event;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;

public final class EventBus {
    private volatile Listener[] listeners = new Listener[0];
    private final Set<Object> registered = Collections.newSetFromMap(new IdentityHashMap());

    public synchronized void register(Object object) {
        if (this.registered.contains(object)) {
            return;
        }
        ArrayList<Listener> arrayList = new ArrayList<Listener>(Arrays.asList(this.listeners));
        Class<?> clazz = object instanceof Class ? (Class<?>)object : object.getClass();
        for (Method method : clazz.getMethods()) {
            SubscribeEvent subscribeEvent = method.getAnnotation(SubscribeEvent.class);
            if (subscribeEvent == null) continue;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new IllegalArgumentException("Invalid event subscriber: " + method.getName());
            }
            if (object instanceof Class && !Modifier.isStatic(method.getModifiers())) continue;
            method.setAccessible(true);
            arrayList.add(new Listener(object, method, subscribeEvent));
        }
        arrayList.sort(Comparator.comparingInt(listener -> ((Listener)listener).subscription.priority().ordinal()));
        this.registered.add(object);
        this.listeners = arrayList.toArray(new Listener[0]);
    }

    public synchronized void unregister(Object object) {
        if (!this.registered.remove(object)) {
            return;
        }
        ArrayList<Listener> arrayList = new ArrayList<Listener>();
        for (Listener listener : this.listeners) {
            if (listener.target == object) continue;
            arrayList.add(listener);
        }
        this.listeners = arrayList.toArray(new Listener[0]);
    }

    public boolean post(Event event) {
        for (Listener listener : this.listeners) {
            if (!listener.type.isInstance(event) || event.isCanceled() && !listener.subscription.receiveCanceled()) continue;
            try {
                listener.method.invoke(listener.target instanceof Class ? null : listener.target, event);
            }
            catch (InvocationTargetException invocationTargetException) {
                Throwable throwable = invocationTargetException.getCause();
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException)throwable;
                }
                if (throwable instanceof Error) {
                    throw (Error)throwable;
                }
                throw new IllegalStateException("Valen event subscriber failed", throwable);
            }
            catch (ReflectiveOperationException reflectiveOperationException) {
                throw new IllegalStateException("Valen event dispatch failed", reflectiveOperationException);
            }
        }
        return event.isCancelable() && event.isCanceled();
    }

    private static final class Listener {
        private final Object target;
        private final Method method;
        private final Class<?> type;
        private final SubscribeEvent subscription;

        private Listener(Object object, Method method, SubscribeEvent subscribeEvent) {
            this.target = object;
            this.method = method;
            this.type = method.getParameterTypes()[0];
            this.subscription = subscribeEvent;
        }
    }
}

