package rice.features;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/** Isolated dependencies; never installs another client's hooks or menu. */
public final class RiceFeatureLoader extends ClassLoader {
    private final Map<String,byte[]> entries = new HashMap<>();
    public RiceFeatureLoader(byte[] jar, ClassLoader parent) throws Exception {
        super(parent);
        try (JarInputStream input = new JarInputStream(new ByteArrayInputStream(jar))) {
            for (JarEntry entry; (entry=input.getNextJarEntry())!=null;) {
                if(entry.isDirectory())continue;
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                byte[] buffer=new byte[8192];
                for(int count;(count=input.read(buffer))!=-1;)bytes.write(buffer,0,count);
                entries.put(entry.getName(),bytes.toByteArray());
            }
        }
    }
    @Override protected synchronized Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
        Class<?> type=findLoadedClass(name);
        if(type==null && (name.startsWith("dev.rice.")||name.startsWith("myau."))) {
            byte[] bytes=entries.get(name.replace('.','/')+".class");
            if(bytes!=null)type=defineClass(name,bytes,0,bytes.length);
        }
        if(type==null)type=super.loadClass(name,false);
        if(resolve)resolveClass(type);
        return type;
    }
    @Override public InputStream getResourceAsStream(String name) {
        byte[] bytes=entries.get(name);
        return bytes==null?super.getResourceAsStream(name):new ByteArrayInputStream(bytes);
    }
}
