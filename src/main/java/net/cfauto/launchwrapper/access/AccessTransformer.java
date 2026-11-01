package net.cfauto.launchwrapper.access;

import org.objectweb.asm.Opcodes;

import java.util.Objects;

public final class AccessTransformer {
    private final Target target;
    private final AccessModification accessMod;
    private final FinalModification finalMod;

    public AccessTransformer(Target target, AccessModification accessMod, FinalModification finalMod) {
        this.target = target;
        this.accessMod = accessMod;
        this.finalMod = finalMod;
    }

    public Target getTarget() {
        return target;
    }

    public AccessModification getAccessMod() {
        return accessMod;
    }

    public FinalModification getFinalMod() {
        return finalMod;
    }

    public enum AccessModification {
        PUBLIC,
        PRIVATE,
        PROTECTED,
        PACKAGE_PRIVATE;

        public static int toAsm(AccessModification modification) {
            switch (modification) {
                case PUBLIC: {
                    return Opcodes.ACC_PUBLIC;
                }
                case PRIVATE: {
                    return Opcodes.ACC_PRIVATE;
                }
                case PROTECTED: {
                    return Opcodes.ACC_PROTECTED;
                }
                case PACKAGE_PRIVATE: {
                    return 0;
                }
            }
            return 69;
        }
    }

    public enum FinalModification {
        REMOVE_FINAL,
        NO_CHANGE,
        ADD_FINAL
    }

}
