package net.cfauto.launchwrapper;

import net.cfauto.launchwrapper.mapping.MappingUtil;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Main class to be used.
 * Used for tweakers, small mods that are loaded before the game/program.
 * Can optionally contain a {@linkplain IClassTransformer} to edit the games code using asm.
 */
public interface ITweaker {
    /** Lets the tweaker adjust the class loader */
    void injectIntoClassLoader(LaunchClassLoader classLoader);

    /**
     * Main tweaker entrypoint, ran earlier then {@linkplain ITweaker#injectIntoClassLoader(LaunchClassLoader)}
     * @param args The launch arguments given to the wrapper
     * @param mainTweakerClass The class name of the current main tweaker
     * @param tweakers The tweakers that are about to / need to be loaded, you can get the main tweaker using {@code tweakers.get("MainTweaker_" + mainTweakClassString)}
     * @param mappingUtil Instance of {@linkplain MappingUtil} specified by the command line argument --mapping, Is null is --mapping isn't set.
     */
    void main(String mainTweakerClass, String[] args, Map<String, ITweaker> tweakers, @Nullable MappingUtil mappingUtil);
}
