
package com.alaka_ala.florafilm.ui.util.coreTorrent.models;

public class TorrentFile {
    private final int index;
    private final String path;
    private final long size;

    public TorrentFile(int index, String path, long size) {
        this.index = index;
        this.path = path;
        this.size = size;
    }

    public int getIndex() {
        return index;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }
}
