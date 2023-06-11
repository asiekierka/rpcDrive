package pl.asie.rpcdrive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class Utils {
    private Utils() {

    }

    public static void deleteDirectoryRecursively(File file) {
        deleteDirectoryRecursively(file, file);
    }

    private static void deleteDirectoryRecursively(File parent, File file) {
        try {
            // Security check
            if (parent != file && !file.getCanonicalPath().startsWith(parent.getCanonicalPath())) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File sub : files) {
                deleteDirectoryRecursively(parent, sub);
            }
        }
        file.delete();
    }

    public static int getInputStreamActualSizeAndClose(InputStream stream) {
        int size = 0;
        if (stream != null) {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = stream.read(buffer);
                    if (read < 0) break;
                    else size += read;
                }
            } catch (IOException e) {
                // pass
            }
            try {
                stream.close();
            } catch (IOException e) {
                // pass
            }
        }
        return size;
    }

    public static int inUnitsRoundedUp(int value, int round) {
        return (value + round - 1) / round;
    }
}
