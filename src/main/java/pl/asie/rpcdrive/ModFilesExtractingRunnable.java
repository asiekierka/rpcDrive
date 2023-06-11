package pl.asie.rpcdrive;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ModFilesExtractingRunnable implements Runnable {
    private final String prefix;
    private final List<String> files;
    private final File filePath;

    public ModFilesExtractingRunnable(String prefix, List<String> files, File filePath) {
        this.prefix = prefix;
        this.files = files;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        if (filePath.exists()) {
            Utils.deleteDirectoryRecursively(filePath);
        }
        if (!filePath.mkdirs()) {
            return;
        }
        for (String inputFilename : files) {
            InputStream is = ModFilesExtractingRunnable.class.getClassLoader().getResourceAsStream(prefix + inputFilename);
            if (is != null) {
                File of = new File(filePath, inputFilename);
                File opf = of.getParentFile();
                if (opf == null || (!opf.exists() && !opf.mkdirs())) {
                    return;
                }
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(of);
                    ByteStreams.copy(is, os);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    try {
                        is.close();
                    } catch (Exception e) {
                        // pass
                    }
                    try {
                        if (os != null) {
                            os.close();
                        }
                    } catch (Exception e) {
                        // pass
                    }
                }
            }
        }
    }
}
