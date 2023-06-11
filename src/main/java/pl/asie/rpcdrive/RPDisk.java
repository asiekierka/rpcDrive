package pl.asie.rpcdrive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface RPDisk {
    boolean hasData();

    boolean isWritable();

    void eraseContents();

    String getSerialNumber();

    String getLabel();

    boolean setLabel(String label);

    InputStream openInputStream() throws IOException;

    File getPath(boolean create) throws IOException;
}
