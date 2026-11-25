package net.cfauto.launchwrapper;

import java.util.Map;

public final class LaunchAPI {
    private final String mainTweakerClass;
    private final String[] args;
    private final Map<String, ITweaker> tweakers;
    private static LaunchAPI instance;

    LaunchAPI(String mainTweakerClass, String[] args, Map<String, ITweaker> tweakers) {
        this.mainTweakerClass = mainTweakerClass;
        this.args = args;
        this.tweakers = tweakers;
        instance = this;
    }

    public String getMainTweakerClass() {
        return mainTweakerClass;
    }

    public String[] getProgramArgs() {
        return args;
    }

    public Map<String, ITweaker> getTweakers() {
        return tweakers;
    }

    public static LaunchAPI getInstance() {
        return instance;
    }
}
