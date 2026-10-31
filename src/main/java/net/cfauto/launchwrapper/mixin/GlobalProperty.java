package net.cfauto.launchwrapper.mixin;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

public class GlobalProperty implements IGlobalPropertyService {
    private final Map<String, Object> veryGlobalAndNotAtAllPrivateProperty = new HashMap<>();

    static class Key implements IPropertyKey {

        private final String key;

        Key(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return this.key;
        }
    }

    @Override
    public IPropertyKey resolveKey(String name) {
        return new Key(name);
    }

    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T) veryGlobalAndNotAtAllPrivateProperty.get(key.toString());
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        veryGlobalAndNotAtAllPrivateProperty.put(key.toString(), value);
    }

    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T) veryGlobalAndNotAtAllPrivateProperty.getOrDefault(key.toString(), defaultValue);
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        return veryGlobalAndNotAtAllPrivateProperty.getOrDefault(key.toString(), defaultValue).toString();
    }
}
