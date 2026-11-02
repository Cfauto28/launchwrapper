package net.cfauto.launchwrapper.access;

import net.cfauto.launchwrapper.IClassTransformer;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class AccessClassTransformer implements IClassTransformer {
    private final ThreadLocal<String> className = new ThreadLocal<>();
    private final ThreadLocal<byte[]> basicClass = new ThreadLocal<>();
    private static final ThreadLocal<AccessTransformer.AccessModification[]> lookup = ThreadLocal.withInitial(() -> new AccessTransformer.AccessModification[4]);

    @Override
    public byte @Nullable [] transform(String name, byte[] basicClass) {
        this.className.set(name);
        this.basicClass.set(basicClass);
        Collection<List<AccessTransformer>> ats = AccessTweaker.getParsedAts().values();
        ats.forEach(list -> list.forEach(this::accessTransformer));
        return this.basicClass.get();
    }

    private void accessTransformer(AccessTransformer at) {
        if (Objects.equals(at.getTarget().className(), className.get())){
            basicClass.set(runTransformation(basicClass.get(), at));
        }
    }

    private byte[] runTransformation(byte[] basicClass, AccessTransformer at) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        if (at.getTarget() instanceof Target.ClassTarget) {
            classNode.access = mergeWith(classNode.access, at.getAccessMod(), at.getFinalMod());
            classNode.innerClasses.stream().filter(c -> c.name.equals(classNode.name)).forEach(inner ->
                    inner.access = mergeWith(inner.access, at.getAccessMod(), at.getFinalMod())
            );
        } else if (at.getTarget() instanceof Target.FieldTarget) {
            classNode.fields.stream().filter(f -> f.name.equals(((Target.FieldTarget) at.getTarget()).fieldName())).forEach(fieldNode ->
                    fieldNode.access = mergeWith(fieldNode.access, at.getAccessMod(), at.getFinalMod())
            );
        } else if (at.getTarget() instanceof Target.MethodTarget) {
            AtomicReference<MethodNode> privateChangedMethod = new AtomicReference<>();
            classNode.methods.stream().filter(m -> m.name.equals(((Target.MethodTarget) at.getTarget()).methodName())).filter(m -> m.desc.equals(((Target.MethodTarget) at.getTarget()).methodDescriptor())).forEach( methodNode -> {
                    boolean wasPrivate = (methodNode.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
                    methodNode.access = mergeWith(methodNode.access, at.getAccessMod(), at.getFinalMod());
                    if (wasPrivate && !"<init>".equals(methodNode.name) && (methodNode.access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
                        privateChangedMethod.set(methodNode);
                    }
            });
            if (privateChangedMethod.get() != null) {
                replaceSpecialWithVirtual(classNode, privateChangedMethod.get());
            }
        } else if (at.getTarget() instanceof Target.InnerClassTarget) {
            classNode.innerClasses.stream().filter(inner -> inner.name.equals(((Target.InnerClassTarget) at.getTarget()).innerName())).forEach(inner ->
                    inner.access = mergeWith(inner.access, at.getAccessMod(), at.getFinalMod())
            );
        } else if (at.getTarget() instanceof Target.WildcardFieldTarget) {
            for (FieldNode filed : classNode.fields) {
                filed.access = mergeWith(filed.access, at.getAccessMod(), at.getFinalMod());
            }
        } else if (at.getTarget() instanceof Target.WildcardMethodTarget) {
            for (MethodNode method : classNode.methods) {
                MethodNode privateChangedMethod = null;
                boolean wasPrivate = (method.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
                method.access = mergeWith(method.access, at.getAccessMod(), at.getFinalMod());
                if (wasPrivate && !"<init>".equals(method.name) && (method.access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
                    privateChangedMethod = method;
                }
                if (privateChangedMethod != null) {
                    replaceSpecialWithVirtual(classNode, privateChangedMethod);
                }
            }
        }
        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static int mergeWith(int access, AccessTransformer.AccessModification modifier) {
        AccessTransformer.AccessModification floor = lookup.get()[firstBit(access & 7)];
        return (access & ~7) | AccessTransformer.AccessModification.toAsm(AccessTransformer.AccessModification.values()[Math.min(floor.ordinal(), modifier.ordinal())]);
    }

    private static int mergeWith(int access, AccessTransformer.FinalModification finalState) {
        switch (finalState) {
            case NO_CHANGE:
                return access;
            case ADD_FINAL:
                return access | Opcodes.ACC_FINAL;
            case REMOVE_FINAL:
                return access & ~Opcodes.ACC_FINAL;
            default:
                throw new IllegalArgumentException("Unknown final state: " + finalState);
        }
    }

    private static int mergeWith(int access, AccessTransformer.AccessModification modifier, AccessTransformer.FinalModification finalState) {
        return mergeWith(mergeWith(access, modifier), finalState);
    }

    private static int firstBit(int flags) {
        return flags == 0 ? 0 : firstBit(flags >>> 1) + 1;
    }

    static {
        Arrays.stream(AccessTransformer.AccessModification.values()).forEach(m->lookup.get()[firstBit(AccessTransformer.AccessModification.toAsm(m))] = m);
    }

    private void replaceSpecialWithVirtual(ClassNode clazz, MethodNode methodNode) {
        for (MethodNode method : clazz.methods) {
            for (AbstractInsnNode insnNode : method.instructions) {
                if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode mInsn = (MethodInsnNode) insnNode;
                    if (methodNode.name.equals(mInsn.name) && methodNode.desc.equals(mInsn.desc)) {
                        mInsn.setOpcode(Opcodes.INVOKEVIRTUAL);
                    }
                }
            }
        }
    }
}
