package net.cfauto.launchwrapper.mapping;

import net.cfauto.launchwrapper.IClassTransformer;
import net.cfauto.launchwrapper.Launch;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.File;
import java.io.IOException;

/**
 * Utility for dealing with obfuscated stuff. <br>
 * By default, this is only created when --mapping is set
 * but tweakers can create their own instance with any arbitrary mapping file.
 * This is meant to allow {@linkplain IClassTransformer} to use mapped names.
 */
public final class MappingUtil {
    private final MemoryMappingTree tree;

    public MappingUtil(File mappingFile) throws IOException {
        tree = new MemoryMappingTree();
        MappingReader.read(mappingFile.toPath(), tree);
    }

    public MappingUtil(MemoryMappingTree tree) {
        this.tree = tree;
    }

    public String getObfuscatedClassName(String name, int namespaceIndex) {
        MappingTree.ClassMapping classMapping = tree.getClass(name, namespaceIndex);
        if (classMapping != null) {
            return classMapping.getSrcName();
        } else {
            Launch.LOGGER.atFatal().log("Invalid class name give by tweaker!", new UnsupportedOperationException());
            return name;
        }
    }

    public String getObfuscatedMethodName(String className, String name, String desc, int namespaceIndex) {
        MappingTree.ClassMapping classMapping = tree.getClass(className, namespaceIndex);
        MappingTree.MethodMapping methodMapping = null;
        if (classMapping != null) {
            methodMapping = classMapping.getMethod(name, desc, namespaceIndex);
        } else {
            Launch.LOGGER.atFatal().log("Invalid class name give by tweaker!", new UnsupportedOperationException());
        }
        if (methodMapping != null) {
            return methodMapping.getSrcName();
        } else {
            Launch.LOGGER.atFatal().log("Invalid method name give by tweaker!", new UnsupportedOperationException());
            return name;
        }
    }

    public String getObfuscatedFieldName(String className, String name, String desc, int namespaceIndex) {
        MappingTree.ClassMapping classMapping = tree.getClass(className, namespaceIndex);
        MappingTree.FieldMapping fieldMapping = null;
        if (classMapping != null) {
            fieldMapping = classMapping.getField(name, desc, namespaceIndex);
        } else {
            Launch.LOGGER.atFatal().log("Invalid class name give by tweaker!", new UnsupportedOperationException());
        }
        if (fieldMapping != null) {
            return fieldMapping.getSrcName();
        } else {
            Launch.LOGGER.atFatal().log("Invalid field name give by tweaker!", new UnsupportedOperationException());
            return name;
        }
    }
}
