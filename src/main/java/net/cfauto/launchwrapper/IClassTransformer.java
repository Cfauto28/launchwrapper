package net.cfauto.launchwrapper;

import net.cfauto.launchwrapper.mapping.MappingUtil;
import org.jspecify.annotations.Nullable;

/** A transformer that can modify/generate/destroy class file contents at class loading time. */
public interface IClassTransformer {
    /** Given a class name, and existing class bytes, returns the new class bytes.
     * Either or both of the input and output bytes can be null to add/remove a class.
     * @apiNote The name of the class is obfuscated! Use {@link MappingUtil#getObfuscatedClassName(String, int)} to use the mapped name
     * @param basicClass Existing class bytes
     * @param name The obfuscated class name
     * @return Modified class bytes
     */
    byte @Nullable [] transform(String name, byte[] basicClass);
}
