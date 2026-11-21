package net.cfauto.launchwrapper.util;

import net.cfauto.launchwrapper.Launch;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("unused")
public final class Utils {
    private static IUnsafeUtils unsafe = new UnsafeUtils();

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

    public static IUnsafeUtils getUnsafe(String tweakerName) {
        Launch.LOGGER.atWarn().log("Tweaker {} got unsafe!", tweakerName);
        return unsafe;
    }

    public static void setUnsafe(IUnsafeUtils unsafe) {
        Utils.unsafe = unsafe;
    }
}
