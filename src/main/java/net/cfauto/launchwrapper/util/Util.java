package net.cfauto.launchwrapper.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Util {
    public static IUnsafeUtils unsafe = new UnsafeUtils();

    public static void recurseDirectory(Path path, List<Path> files) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    recurseDirectory(entry, files);
                } else {
                    files.add(entry);
                }
            }
        }
    }
}
