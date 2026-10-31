package net.cfauto.launchwrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LaunchClassLoader extends URLClassLoader {
    private final List<String> transformerExceptions = new CopyOnWriteArrayList<>();
    private final List<String> classLoaderExceptions = new CopyOnWriteArrayList<>();
    private final List<IClassTransformer> transformers = new CopyOnWriteArrayList<>();

    private final ClassLoader parent;

    private final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    private final ThreadLocal<byte[]> loadBuffer = new ThreadLocal<>();
    public static final int BUFFER_SIZE = 1 << 12;

    private List<URL> sources;

    public LaunchClassLoader(URL[] urls) {
        super(urls);
        this.sources = new ArrayList<>(Arrays.asList(urls));
        parent = getClass().getClassLoader();
        classLoaderExceptions.addAll(Arrays.asList(
                "java.",
                "javax.imageio.",
                "javax.sound.",
                "javax.swing.",
                "sun.",
                "org.apache.logging.",
                "org.objectweb.asm."));
        transformerExceptions.addAll(Arrays.asList(
                "javax.",
                "argo.",
                "com.google.common.",
                "org.bouncycastle."));
    }


    /**
     * Registers a transformer for transforming
     * @param transformerClassName The canonical class name "a.b.c.ABC" of the class implementing IClassTransformer
     */
    public void registerTransformer(String transformerClassName) {
        try {
            Class<?> transformerClass = Class.forName(transformerClassName, true, this);
            IClassTransformer transformer = (IClassTransformer) transformerClass.getConstructor().newInstance();
            transformers.add(transformer);
        } catch (ReflectiveOperationException e) {
            Launch.LOGGER.atFatal().log("Failed to register transformer {} !", transformerClassName, e);
        }
    }

    /** Adds a new entry to the end of the class loader exclusions list */
    public void addClassLoaderExclusion(String toExclude) {
        classLoaderExceptions.add(toExclude);
    }

    /** Adds a new entry to the end of the transformer exclusions list */
    public void addTransformerExclusion(String toExclude) {
        transformerExceptions.add(toExclude);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (String exception : classLoaderExceptions) {
            if (name.startsWith(exception)) {
                return parent.loadClass(name);
            }
        }
        Class<?> cacheClass = cachedClasses.get(name);
        if (cacheClass != null) {
            return cacheClass;
        }
        boolean runTransformers = true;
        for (String exception : transformerExceptions) {
            if (name.startsWith(exception)) {
                runTransformers = false;
                break;
            }
        }
        byte[] transformedClass;
        if (runTransformers) {
            transformedClass = runTransformers(name, getClassBytes(name));
        } else {
            transformedClass = getClassBytes(name);
        }
        Class<?> result = defineClass(name, transformedClass, 0, transformedClass.length);
        cachedClasses.put(name, result);
        return result;
    }

    /**
     * Gets a byte array for a specific class
     * @param name The canonical name of the class to get the bytes out of
     * @return The byte array of the class
     * @implNote This is pre transformation
     */
    public byte[] getClassBytes(String name) {
        try {
            String classPath = name.replace('.', '/') + ".class";
            URL resourceURL = findResource(classPath);
            InputStream stream = resourceURL.openConnection().getInputStream();
            byte[] result = readAllBytes(stream, getOrCreateBuffer());
            stream.close();
            return result;
        } catch (IOException e) {
            Launch.LOGGER.atFatal().log("Failed to read bytes of class {}!", name, e);
        }
        return null;
    }

    public byte[] runTransformers(String name, byte[] clazz) {
        for (IClassTransformer transformer : transformers) {
            clazz = transformer.transform(name, clazz);
        }
        return clazz;
    }

    private static byte[] readAllBytes(InputStream stream, byte[] readBuffer) throws IOException {
        // return stream.readAllBytes();
        if (readBuffer == null) {
            readBuffer = new byte[8192];
        }
        final int availableBytes = stream.available();
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream(availableBytes < 32 ? 8192 : availableBytes);
        int nRead;
        while ((nRead = stream.read(readBuffer, 0, readBuffer.length)) != -1) {
            outBuffer.write(readBuffer, 0, nRead);
        }
        return outBuffer.toByteArray();
    }

    private byte[] getOrCreateBuffer() {
        byte[] buf = loadBuffer.get();
        if (buf == null) {
            buf = new byte[BUFFER_SIZE];
            loadBuffer.set(buf);
        }
        return buf;
    }

    public List<URL> getSources() {
        return sources;
    }

    public List<IClassTransformer> getTransformers() {
        return transformers;
    }
}

