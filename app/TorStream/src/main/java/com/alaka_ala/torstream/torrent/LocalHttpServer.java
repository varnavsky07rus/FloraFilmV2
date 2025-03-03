package com.alaka_ala.torstream.torrent;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class LocalHttpServer extends NanoHTTPD {
    private final String videoPath;

    private void log(String msg) {
        Log.d("LocalHttpServer", msg);
    }

    public void startServer() throws IOException {
        if (isAlive()) {
            log("Сервер уже запущен.");
            //return;
        }
        start();
        log("Сервер запущен. Порт: " + getListeningPort() + " Путь к видео: " + videoPath);

    }

    public LocalHttpServer(int port, String videoPath) {
        super(port);
        this.videoPath = videoPath;
        log("Сервер подготовлен.");
    }

    @Override
    public Response serve(IHTTPSession session) {
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
        }

        try {
            String rangeHeader = session.getHeaders().get("range");
            long fileLength = videoFile.length();
            FileInputStream fileInputStream = new FileInputStream(videoFile);

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring(6);
                long start = Long.parseLong(rangeValue.split("-")[0]);
                long end = fileLength - 1;
                if (rangeValue.contains("-")) {
                    String[] parts = rangeValue.split("-");
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        end = Long.parseLong(parts[1]);
                    }
                }

                long contentLength = end - start + 1;
                fileInputStream.skip(start);
                byte[] buffer = new byte[(int) contentLength];
                fileInputStream.read(buffer, 0, (int) contentLength);
                fileInputStream.close();

                Response response = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, "video/mp4", new ByteArrayInputStream(buffer), contentLength);
                response.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.addHeader("Accept-Ranges", "bytes");
                return response;
            } else {
                Response response = newFixedLengthResponse(Response.Status.OK, "video/mp4", fileInputStream, fileLength);
                response.addHeader("Accept-Ranges", "bytes");
                return response;
            }
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
    }
}
