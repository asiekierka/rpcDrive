package pl.asie.rpcdrive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class RPDiskResource implements RPDisk {
    private final int index;
    private final String path;
    private Boolean hasData;

    public RPDiskResource(int index, String path) {
        this.index = index;
        this.path = path;
    }

    @Override
    public boolean hasData() {
        if (hasData == null) {
            hasData = RPDiskResource.class.getClassLoader().getResource(path) != null;
        }
        return hasData;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public void eraseContents() {

    }

    @Override
    public String getSerialNumber() {
        return String.format("%016d", this.index);
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public boolean setLabel(String label) {
        return false;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return RPDiskResource.class.getClassLoader().getResourceAsStream(path);
    }

    @Override
    public File getPath(boolean create) throws IOException {
        return null;
    }
}
