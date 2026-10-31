package net.cfauto.launchwrapper.mixin;

import joptsimple.OptionParser;
import net.cfauto.launchwrapper.Util;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

public final class TweakerMixinBootstrap {
    public static void bootstrap(String[] args) throws IOException {
        File tweakDir = (File) new OptionParser().parse(args).valueOf("tweakDir");
        List<Path> tweakJars = new ArrayList<>();
        Util.recurseDirectory(tweakDir.toPath(), tweakJars);
        for (Path jar : tweakJars) {
            Manifest manifest = ((JarURLConnection) jar.toUri().toURL().openConnection()).getManifest();
            if (manifest.getMainAttributes().containsKey("MixinConfig")) {
                String mixinConfigLocation = manifest.getMainAttributes().get("MixinConfig").toString();
                Mixins.addConfiguration(mixinConfigLocation);
            }
        }
    }
}
