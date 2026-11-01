package net.cfauto.launchwrapper;

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
     */
    void main(String mainTweakerClass, String[] args, Map<String, ITweaker> tweakers);
}
