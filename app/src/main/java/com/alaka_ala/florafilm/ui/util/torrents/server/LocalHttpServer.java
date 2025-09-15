package com.alaka_ala.florafilm.ui.util.torrents.server;

import com.alaka_ala.florafilm.ui.util.torrents.main.Torrent;

import org.libtorrent4j.TorrentHandle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class LocalHttpServer extends NanoHTTPD {
    private static final Map<String, String> videoMimeTypes = new HashMap<>();
    private final Torrent torrent;
    private final File videoFile;
    private String mimeType = "video/mp4";


    public LocalHttpServer(int port, Torrent torrent) {
        super(port);
        this.torrent = torrent;
        this.videoFile = torrent.getVideoFile();
        videoMimeTypes.put("avi", "video/x-msvideo");
        videoMimeTypes.put("mp4", "video/mp4");
        videoMimeTypes.put("mov", "video/quicktime");
        videoMimeTypes.put("wmv", "video/x-ms-wmv");
        videoMimeTypes.put("flv", "video/x-flv");
        videoMimeTypes.put("mkv", "video/x-matroska");
        videoMimeTypes.put("webm", "video/webm");
        videoMimeTypes.put("mpeg", "video/mpeg");
        videoMimeTypes.put("mpg", "video/mpeg");
        videoMimeTypes.put("3gp", "video/3gpp");
        videoMimeTypes.put("ts", "video/mp2t");
        videoMimeTypes.put("m4v", "video/x-m4v");
        this.mimeType = getMimeType(torrent.getVideoFile().getName());
    }

    public static String getMimeType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "video/mp4"; // неизвестный тип
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return videoMimeTypes.getOrDefault(ext, "video/mp4");
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String rangeHeader = session.getHeaders().get("range");
            long fileLength = videoFile.length();

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // This is a range request, handle it with seeking.
                return serveRange(session, rangeHeader, fileLength);
            } else {
                // This is a full file request.
                return serveFile(session, fileLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal Server Error: " + e.getMessage());
        }
    }

    private Response serveRange(IHTTPSession session, String rangeHeader, long fileLength) throws IOException {
        long start = 0;
        long end = fileLength - 1;

        String[] ranges = rangeHeader.substring(6).split("-");
        try {
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
        } catch (NumberFormatException e) {
            // Malformed header, default to whole file, but this path is unlikely for range requests.
            start = 0;
            end = fileLength - 1;
        }

        if (end > fileLength - 1) {
            end = fileLength - 1;
        }

        if (start < 0 || start > end) {
            return newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "Invalid Range");
        }

        if (start == 0) {
            torrent.setGentleInterestedBytes(start);
        } else {
            // Prioritize the requested range for download
            torrent.setInterestedBytes(start);
        }


        // Wait until the end of the requested range is available on disk.
        // This is the blocking part that ensures data is ready for fast serving.
        while (!torrent.hasBytes(end)) {
            try {
                Thread.sleep(250); // Polling interval
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Interrupted while waiting for data");
            }
        }

        // Now that data is on disk, use RandomAccessFile for fast seeking.
        final RandomAccessFile fileReader = new RandomAccessFile(videoFile, "r");
        fileReader.seek(start);

        long contentLength = end - start + 1;

        // NanoHTTPD needs an InputStream. We create one that reads from our RandomAccessFile.
        InputStream fis = new InputStream() {
            @Override
            public int read() throws IOException {
                return fileReader.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return fileReader.read(b, off, len);
            }

            @Override
            public void close() throws IOException {
                fileReader.close();
            }
        };

        Response res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mimeType, fis, contentLength);
        res.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Content-Length", String.valueOf(contentLength));

        return res;
    }

    private Response serveFile(IHTTPSession session, long fileLength) throws FileNotFoundException {
        // For full file requests, we can just use the torrent's stream directly.
        InputStream stream = torrent.getVideoStream();
        Response res = newFixedLengthResponse(Response.Status.OK, mimeType, stream, fileLength);
        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Content-Length", String.valueOf(fileLength));
        return res;
    }
}
