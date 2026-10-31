package org.spongepowered.asm.launch;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.cfauto.launchwrapper.ITweaker;
import net.cfauto.launchwrapper.Launch;
import net.cfauto.launchwrapper.LaunchClassLoader;
import net.cfauto.launchwrapper.mapping.MappingUtil;
import net.cfauto.launchwrapper.mixin.MixinServiceLaunchWrapper;
import net.cfauto.launchwrapper.mixin.TweakerMixinBootstrap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.launch.platform.CommandLineOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Hacky repackaging hack to get package private methods in {@linkplain MixinBootstrap}
 */
public class MixinTweakerCFLaunchWrapper implements ITweaker {
    public MixinTweakerCFLaunchWrapper() {
        MixinBootstrap.init();
        MixinExtrasBootstrap.init();
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.inject();
    }

    @Override
    public void main(String mainTweakerClass, String[] args, Map<String, ITweaker> tweakers, @Nullable MappingUtil mappingUtil) {
        MixinBootstrap.doInit(CommandLineOptions.ofArgs(Arrays.asList(args)));
        try {
            TweakerMixinBootstrap.bootstrap(args);
        } catch (IOException e) {
            Launch.LOGGER.atFatal().log("Failed to bootstrap mixins!", e);
        }
    }
}
