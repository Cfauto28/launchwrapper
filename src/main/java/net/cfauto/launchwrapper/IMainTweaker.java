package net.cfauto.launchwrapper;

/**
 * Tweaker that give information about the program.
 * @apiNote This is required for each program.
 */
public interface IMainTweaker extends ITweaker {
    String getProgramName();

    String getJavaVersion();

    String getLaunchTarget();

    String getSide();
}
