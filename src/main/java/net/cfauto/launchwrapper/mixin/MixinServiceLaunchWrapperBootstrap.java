package net.cfauto.launchwrapper.mixin;

import net.cfauto.launchwrapper.Launch;
import org.spongepowered.asm.service.IMixinServiceBootstrap;
import org.spongepowered.asm.service.ServiceInitialisationException;

public final class MixinServiceLaunchWrapperBootstrap implements IMixinServiceBootstrap {
    private static final String SERVICE_PACKAGE = "org.spongepowered.asm.service.";
    private static final String LAUNCH_PACKAGE = "org.spongepowered.asm.launch.";
    private static final String LOGGING_PACKAGE = "org.spongepowered.asm.logging.";

    private static final String MIXIN_UTIL_PACKAGE = "org.spongepowered.asm.util.";
    private static final String LEGACY_ASM_PACKAGE = "org.spongepowered.asm.lib.";
    private static final String ASM_PACKAGE = "org.objectweb.asm.";
    private static final String MIXIN_PACKAGE = "org.spongepowered.asm.mixin.";

    @Override
    public String getName() {
        return "LaunchWrapper";
    }

    @Override
    public String getServiceClassName() {
        return "net.cfauto.launchwrapper.mixin.MixinServiceLaunchWrapper";
    }

    @Override
    public void bootstrap() {
        try {
            Launch.classLoader.hashCode();
        } catch (Throwable th) {
            throw new ServiceInitialisationException(this.getName() + " is not available");
        }

        // Essential ones
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.SERVICE_PACKAGE);
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.LAUNCH_PACKAGE);
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.LOGGING_PACKAGE);

        // Important ones
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.ASM_PACKAGE);
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.LEGACY_ASM_PACKAGE);
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.MIXIN_PACKAGE);
        Launch.classLoader.addClassLoaderExclusion(MixinServiceLaunchWrapperBootstrap.MIXIN_UTIL_PACKAGE);
    }
}
