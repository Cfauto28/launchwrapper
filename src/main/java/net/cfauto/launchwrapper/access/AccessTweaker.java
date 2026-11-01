package net.cfauto.launchwrapper.access;

import joptsimple.OptionParser;
import net.cfauto.launchwrapper.ITweaker;
import net.cfauto.launchwrapper.Launch;
import net.cfauto.launchwrapper.LaunchClassLoader;
import net.cfauto.launchwrapper.util.Util;

import java.io.*;
import java.net.JarURLConnection;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;

public final class AccessTweaker implements ITweaker {
    private static final Map<String, File> ats = new HashMap<>();
    private static final Map<File, List<AccessTransformer>> parsedAts = new HashMap<>();

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.registerTransformer("net.cfauto.launchwrapper.access.AccessTransformer");
    }

    @Override
    public void main(String mainTweakerClass, String[] args, Map<String, ITweaker> tweakers) {
        try {
            File tweakDir = (File) new OptionParser().parse(args).valueOf("tweakDir");
            List<Path> tweakJars = new ArrayList<>();
            Util.recurseDirectory(tweakDir.toPath(), tweakJars);
            for (Path jar : tweakJars) {
                Manifest manifest = ((JarURLConnection) jar.toUri().toURL().openConnection()).getManifest();
                if (manifest.getMainAttributes().containsKey("AccessTransformer")) {
                    String accessTransformerLoc = manifest.getMainAttributes().getValue("AccessTransformer");
                    File accessTransformer = new File(accessTransformerLoc);
                    ats.put(manifest.getMainAttributes().getValue("TweakClass"), accessTransformer);
                    parsedAts.put(accessTransformer, parseAt(accessTransformer));
                }
            }
        } catch (IOException e) {
            Launch.LOGGER.atFatal().log("Failed to load ATs!", e);
        }
    }

    private List<AccessTransformer> parseAt(File at) {
        List<AccessTransformer> parsedAt = new ArrayList<>();
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(at.toURI().toURL().openConnection().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StringBuilder builder = new StringBuilder();
                List<String> parts = new ArrayList<>();
                builder.setLength(0);
                for (char c : line.toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        if (builder.length() != 0) {
                            parts.add(builder.toString());
                        }
                        builder.setLength(0);
                    } else if (c == '#') {
                        builder.setLength(0);
                        break;
                    } else {
                        builder.appendCodePoint(c);
                    }
                }
                if (builder.length() != 0) {
                    parts.add(builder.toString());
                }
                if (parts.isEmpty()) {
                    // The line is empty or all comments
                    continue;
                }
                if (parts.size() < 2) {
                    // The line has something in the "modifier" slot but nothing else
                    Launch.LOGGER.atFatal().log("Invalid line {}; should be '<modifier> <class name> [<member>]'", reader.getLineNumber());
                }

                AccessTransformer.FinalModification finalMod;
                AccessTransformer.AccessModification accessMod;
                String modifierString = parts.get(0);
                if (modifierString.endsWith("-f")) {
                    finalMod = AccessTransformer.FinalModification.REMOVE_FINAL;
                    modifierString = modifierString.substring(0, modifierString.length() - 2);
                } else if (modifierString.endsWith("+f")) {
                    finalMod = AccessTransformer.FinalModification.ADD_FINAL;
                    modifierString = modifierString.substring(0, modifierString.length()- 2);
                } else {
                    finalMod = AccessTransformer.FinalModification.NO_CHANGE;
                }
                accessMod = Objects.requireNonNull(parseAccessMod(modifierString));



                String className = parts.get(1);
                // Validate class name as dot-separated java identifiers
                if (className.chars().reduce(0, (last, current) -> {
                    if (last == 0 && !Character.isJavaIdentifierStart(current) ||
                            last != 0 && current != '.' && !Character.isJavaIdentifierPart(current)
                    ) {
                        throw new RuntimeException("Invalid class name '" + className + "' at line " + reader.getLineNumber());
                    }
                    return current == '.' ? 0 : 1;
                }) != 1) {
                    throw new RuntimeException("Invalid class name '" + className + "' at line " + reader.getLineNumber());
                }

                Target target;

                if (parts.size() < 3) {
                    // Class ATs of inner classes have a corresponding inner class AT for the nesting class
                    target = locateInnerClassAts(className);
                    if (target == null) {
                        target = new Target.ClassTarget(className);
                    }
                } else {
                    String member = parts.get(2);
                    if (member.equals("*")) {
                        target = new Target.WildcardFieldTarget(className);
                    } else if (member.equals("*()")) {
                        target = new Target.WildcardMethodTarget(className);
                    } else if (member.contains("(")) {
                        String name = member.substring(0, member.indexOf('('));
                        // For some reason old forge ATs let you use descriptors with dots instead of slashes
                        // We replicate this behavior here
                        String desc = member.substring(member.indexOf('('))
                                .replace('.', '/');
                        validateMethodDescriptor(desc, reader.getLineNumber());
                        if (!name.equals("<init>")) {
                            validateIdentifier(name, "method", reader.getLineNumber());
                        }
                        target = new Target.MethodTarget(className, name, desc);
                    } else {
                        validateIdentifier(member, "field", reader.getLineNumber());
                        target = new Target.FieldTarget(className, member);
                    }
                }

                parsedAt.add(new AccessTransformer(target, accessMod, finalMod));
            }
        } catch (IOException e) {
            Launch.LOGGER.atFatal().log("Failed to parse at {}", at, e);
        }
        return parsedAt;
    }

    private AccessTransformer.AccessModification parseAccessMod(String modifierString) {
        switch (modifierString) {
            case "public": {
                return AccessTransformer.AccessModification.PUBLIC;
            }
            case "private": {
                return AccessTransformer.AccessModification.PRIVATE;
            }
            case "protected": {
                return AccessTransformer.AccessModification.PROTECTED;
            }
            case "package-private": {
                return AccessTransformer.AccessModification.PACKAGE_PRIVATE;
            }
        }
        return null;
    }

    private static Target locateInnerClassAts(String className) {
        int split = className.lastIndexOf('$');
        if (split == -1) {
            return null;
        }
        String parent = className.substring(0, split);
        return new Target.InnerClassTarget(parent, className);
    }

    private static void validateMethodDescriptor(String desc, int index) {
        int open = desc.indexOf('(');
        int close = desc.lastIndexOf(')');
        String error = "Invalid method descriptor '"+ desc +"' at line " + index;
        if (open != 0 || close == -1) {
            throw new RuntimeException(error);
        }
        String argsTypes = desc.substring(open + 1, close);
        String returnType = desc.substring(close + 1);
        if (validateDescriptor(returnType, true, error) != 1) {
            throw new RuntimeException(error);
        }
        validateDescriptor(argsTypes, false, error);
    }

    private static int validateDescriptor(String types, boolean allowVoid, String error) {
        int count = 0;
        int idx = 0;
        boolean inArray = false;
        while (idx < types.length()) {
            char c = types.charAt(idx);
            if (c == 'V' && (!allowVoid || inArray)) {
                throw new RuntimeException(error);
            } else if ("ZCBSIFJDV".indexOf(c) != -1) {
                inArray = false;
                count++;
            } else if (c == '[') {
                inArray = true;
            } else if (c == 'L') {
                int end = types.indexOf(';', idx);
                if (end == -1) {
                    throw new RuntimeException(error);
                }
                String full = types.substring(idx+1, end);
                if (full.chars().anyMatch(i -> ".[".indexOf(i) != -1)) {
                    throw new RuntimeException(error);
                }
                count++;
                inArray = false;
                idx = end;
            } else {
                throw new RuntimeException(error);
            }
            idx++;
        }
        if (inArray) {
            throw new RuntimeException(error);
        }
        return count;
    }

    private static void validateIdentifier(String name, String sort, int index) {
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            throw new RuntimeException("Invalid " + sort + "name '" + name + "' at line " + index);
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                throw new RuntimeException("Invalid " + sort + "name '" + name + "' at line " + index);
            }
        }
    }

    public static Map<String, File> getAts() {
        return ats;
    }

    public static Map<File, List<AccessTransformer>> getParsedAts() {
        return parsedAts;
    }
}
