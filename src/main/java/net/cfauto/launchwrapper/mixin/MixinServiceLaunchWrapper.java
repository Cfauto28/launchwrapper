package net.cfauto.launchwrapper.mixin;

import net.cfauto.launchwrapper.IClassTransformer;
import net.cfauto.launchwrapper.Launch;
import net.cfauto.launchwrapper.LaunchClassLoader;
import net.cfauto.launchwrapper.util.DelegatedTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.transformers.MixinClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class MixinServiceLaunchWrapper extends MixinServiceAbstract implements IClassProvider, IClassBytecodeProvider, ITransformerProvider {
    private List<IClassTransformer> transformers;

    private List<URL> sources;

    private List<ILegacyClassTransformer> delegatedTransformers;

    private static final Logger logger = LogManager.getLogger();

    public MixinServiceLaunchWrapper() {
        try {
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            this.transformers = (List<IClassTransformer>) transformersField.get(Launch.classLoader);
            Field sourcesField = LaunchClassLoader.class.getDeclaredField("sources");
            sourcesField.setAccessible(true);
            this.sources = (List<URL>) sourcesField.get(Launch.classLoader);
        } catch (NoSuchFieldException | IllegalAccessException e) {
           throw new RuntimeException(e);
        }
    }




    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, this.getClassBytes(name, true), ClassReader.EXPAND_FRAMES);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, this.getClassBytes(name, runTransformers), ClassReader.EXPAND_FRAMES);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, this.getClassBytes(name, runTransformers), readerFlags);
    }

    private byte[] getClassBytes(String name, boolean runTransformers) {
        if (!runTransformers) {
            return Launch.classLoader.getClassBytes(name);
        } else {
            return Launch.classLoader.runTransformers(name, Launch.classLoader.getClassBytes(name));
        }
    }

    private ClassNode getClassNode(String className, byte[] classBytes, int flags) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new MixinClassReader(classBytes, className);
        classReader.accept(classNode, flags);
        return classNode;
    }


    @Override
    public URL[] getClassPath() {
        return sources.toArray(new URL[0]);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.classLoader);
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.class.getClassLoader());
    }

    @Override
    public String getName() {
        return "LaunchWrapper";
    }

    @Override
    public boolean isValid() {
        try {
            // Detect launchwrapper
            Launch.classLoader.hashCode();
        } catch (Throwable ex) {
            return false;
        }
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    @Override
    public IClassTracker getClassTracker() {
        return new IClassTracker() {
            private final List<String> classLoaderExceptions;
            private final Map<String, Class<?>> cachedClasses;
            private final List<String> transformerExceptions;
            {
                try {
                    Field classLoaderExclusion = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
                    classLoaderExclusion.setAccessible(true);
                    classLoaderExceptions = (List<String>) classLoaderExclusion.get(Launch.classLoader);
                    Field cacheClassesField = LaunchClassLoader.class.getDeclaredField("cachedClasses");
                    cacheClassesField.setAccessible(true);
                    cachedClasses = (Map<String, Class<?>>) cacheClassesField.get(Launch.classLoader);
                    Field transformerExceptionsField = LaunchClassLoader.class.getDeclaredField("transformerExceptions");
                    transformerExceptionsField.setAccessible(true);
                    transformerExceptions = (List<String>) transformerExceptionsField.get(Launch.classLoader);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void registerInvalidClass(String className) {
                if (!classLoaderExceptions.contains(className)) {
                    Launch.classLoader.addClassLoaderExclusion(className);
                }
            }

            @Override
            public boolean isClassLoaded(String className) {
                return cachedClasses.containsKey(className);
            }

            boolean isClassClassLoaderExcluded(String name) {
                for (final String exception : this.classLoaderExceptions) {
                    if (name.startsWith(exception)) {
                        return true;
                    }
                }

                return false;
            }

            boolean isClassTransformerExcluded(String name) {
                for (final String exception : this.transformerExceptions) {
                    if (name.startsWith(exception)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public String getClassRestrictions(String className) {
                String restrictions = "";
                if (this.isClassClassLoaderExcluded(className)) {
                    restrictions = "PACKAGE_CLASSLOADER_EXCLUSION";
                }
                if (this.isClassTransformerExcluded(className)) {
                    restrictions = (restrictions.length() > 0 ? restrictions + "," : "") + "PACKAGE_TRANSFORMER_EXCLUSION";
                }
                return restrictions;
            }
        };
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public IFeatureValidator getFeatureValidator() {
        return IFeatureValidator.ALLOW_ALL;
    }

    @Override
    public IAdviceProvider getAdviceProvider() {
        return IAdviceProvider.GENERIC;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.emptyList();
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        URI uri = null;
        try {
            uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            if (uri != null) {
                return new ContainerHandleURI(uri);
            }
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return new ContainerHandleVirtual(this.getName());
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return Launch.classLoader.getResourceAsStream(name);
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        return transformers.stream().map(transform -> new ILegacyClassTransformer() {
            @Override
            public byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {
                return transform.transform(name, basicClass);
            }

            @Override
            public String getName() {
                return transform.getClass().getName();
            }

            @Override
            public boolean isDelegationExcluded() {
                return transform.getClass().getAnnotation(DelegatedTransformer.class) != null;
            }
        }).collect(Collectors.toSet());
    }

    @Override
    public List<ITransformer> getDelegatedTransformers() {
        return Collections.unmodifiableList(this.getDelegatedLegacyTransformers());
    }

    private List<ILegacyClassTransformer> getDelegatedLegacyTransformers() {
        if (this.delegatedTransformers == null) {
            this.buildTransformerDelegationList();
        }

        return this.delegatedTransformers;
    }

    /**
     * Builds the transformer list to apply to loaded mixin bytecode. Since
     * generating this list requires inspecting each transformer by name (to
     * cope with the new wrapper functionality added by FML) we generate the
     * list just once per environment and cache the result.
     */
    private void buildTransformerDelegationList() {
        MixinServiceLaunchWrapper.logger.debug("Rebuilding transformer delegation list:");
        this.delegatedTransformers = new ArrayList<>();
        for (ITransformer transformer : this.getTransformers()) {
            if (!(transformer instanceof ILegacyClassTransformer)) {
                continue;
            }

            ILegacyClassTransformer legacyTransformer = (ILegacyClassTransformer)transformer;
            String transformerName = legacyTransformer.getName();
            if (!legacyTransformer.isDelegationExcluded()) {
                MixinServiceLaunchWrapper.logger.debug("  Adding:    {}", transformerName);
                this.delegatedTransformers.add(legacyTransformer);
            } else {
                MixinServiceLaunchWrapper.logger.debug("  Excluding: {}", transformerName);
            }
        }

        MixinServiceLaunchWrapper.logger.debug("Transformer delegation list created with {} entries", this.delegatedTransformers.size());
    }


    @Override
    public void addTransformerExclusion(String name) {
        Launch.classLoader.addTransformerExclusion(name);
    }

    @Override
    public void prepare() {
        Launch.classLoader.addClassLoaderExclusion(MixinServiceAbstract.LAUNCH_PACKAGE);
    }
}
