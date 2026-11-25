package net.cfauto.launchwrapper;

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
     */
    void main();
}
