package net.cfauto.launchwrapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.cfauto.launchwrapper.mapping.MappingUtil;
import net.cfauto.launchwrapper.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;

public class Launch {
    public static LaunchClassLoader classLoader;
    public static Logger LOGGER = LogManager.getLogger("LaunchWrapper");
    public Map<String, ITweaker> tweakers = new HashMap<>();
    public Map<String, ITweaker> tweakersToDo = new HashMap<>();

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    private Launch() {}

    private void launch(String[] args) {
        OptionParser parser = new OptionParser(false);
        parser.allowsUnrecognizedOptions();

        OptionSpec<String> mainTweakClassStringArg = parser.accepts("mainTweakClass").withRequiredArg().ofType(String.class).required();
        OptionSpec<File> tweakDirArg = parser.accepts("tweakDir").withRequiredArg().ofType(File.class).required();
        OptionSpec<File> mapFileArg = parser.accepts("mapping").withRequiredArg().ofType(File.class);

        OptionSet option = parser.parse(args);
        String mainTweakClassString = option.valueOf(mainTweakClassStringArg);
        File tweakDir = option.valueOf(tweakDirArg);

        MappingUtil mappingUtil = null;

        if (option.has(mapFileArg)) {
            try {
                mappingUtil = new MappingUtil(option.valueOf(mapFileArg));
            } catch (IOException e) {
                LOGGER.atFatal().log("Failed to create mapping util for specified mapping!", e);
            }
        }

        //Gets all the jars in the tweak dir
        List<Path> filesInTweakDir = new ArrayList<>();
        Set<URL> tweakJars = new HashSet<>();
        try {
            Util.recurseDirectory(tweakDir.toPath(), filesInTweakDir);
            for (Path tweakJar : filesInTweakDir) {
                if (tweakJar.toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    tweakJars.add(tweakJar.toUri().toURL());
            }
        } catch (IOException e) {
            LOGGER.atFatal().log("Failed to recurse tweak dir!", e);
        }

        //Sets the classloader
        URLClassLoader urlClassLoader = (URLClassLoader) getClass().getClassLoader();
        tweakJars.addAll(Arrays.asList(urlClassLoader.getURLs()));
        classLoader = new LaunchClassLoader(tweakJars.toArray(new URL[0]));
        Thread.currentThread().setContextClassLoader(classLoader);

        //Actually gets the tweakers
        try {
            if (Boolean.parseBoolean(System.getProperties().getProperty("launchwrapper.mixin", "true"))) {
                //Mixin Tweaker
                Class<?> mixinTweakClass = Class.forName("org.spongepowered.asm.launch.MixinTweakerCFLaunchWrapper", true, classLoader);
                ITweaker mixinTweaker = (ITweaker) mixinTweakClass.getConstructor().newInstance();
                tweakers.put("Mixin", mixinTweaker);
            }
            //Main Tweaker
            Class<?> mainTweakClass = Class.forName(mainTweakClassString, true, classLoader);
            IMainTweaker mainTweaker = (IMainTweaker) mainTweakClass.getConstructor().newInstance();
            tweakers.put("MainTweaker_" + mainTweakClassString, mainTweaker);
            //Other Tweakers
            for (URL tweakJar : tweakJars) {
                JarURLConnection jarConnection = (JarURLConnection) tweakJar.openConnection();
                Manifest manifest = jarConnection.getManifest();
                String tweakClassString = manifest.getMainAttributes().getValue("TweakClass");
                Class<?> tweakClass = Class.forName(tweakClassString, true, classLoader);
                ITweaker tweaker = (ITweaker) tweakClass.getConstructor().newInstance();
                tweakers.put(tweakClassString, tweaker);
            }
        } catch (ReflectiveOperationException | IOException e) {
            LOGGER.atFatal().log("Failed to load tweakers class!", e);
        }



        //Runs the tweakers
        tweakersToDo = tweakers;
        if (Boolean.parseBoolean(System.getProperties().getProperty("launchwrapper.mixin", "true"))) {
            //Mixin
            ITweaker mixinTweaker = tweakers.get("Mixin");
            mixinTweaker.main(mainTweakClassString, args, tweakers, mappingUtil);
            mixinTweaker.injectIntoClassLoader(classLoader);
            tweakersToDo.remove("Mixin");
        }
        //Main tweaker
        IMainTweaker mainTweaker = (IMainTweaker) tweakers.get("MainTweaker_" + mainTweakClassString);
        mainTweaker.main(mainTweakClassString, args, tweakers, mappingUtil);
        mainTweaker.injectIntoClassLoader(classLoader);
        tweakersToDo.remove("MainTweaker_" + mainTweakClassString);
        //Other tweakers
        for (ITweaker tweaker : tweakersToDo.values()) {
            tweaker.main(mainTweakClassString, args, tweakers, mappingUtil);
            tweaker.injectIntoClassLoader(classLoader);
            tweakersToDo.remove(tweaker.getClass().getCanonicalName());
        }

        //Launch
        try {
            String launchTargetString = mainTweaker.getLaunchTarget();
            Class<?> launchTargetClass = Class.forName(launchTargetString);
            Method mainTarget = launchTargetClass.getDeclaredMethod("main", String[].class);
            mainTarget.invoke(null, (Object) args);
        } catch (ReflectiveOperationException e) {
            LOGGER.atFatal().log("Failed to call main class!", e);
        }
    }
}
