package com.alaka_ala.florafilm.ui.util.torrents;
import fi.iki.elonen.NanoHTTPD;

import org.libtorrent4j.TorrentHandle;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class TorrentProxyServer extends NanoHTTPD {
    private final TorrentHandle torrentHandle;
    private final String filePath;  // Путь к файлу в торренте

    public TorrentProxyServer(int port, TorrentHandle handle, String filePath) throws IOException {
        super(port);
        this.torrentHandle = handle;
        this.filePath = filePath;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Сервер запущен на http://localhost:" + port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            // Эмулируем потоковое видео для ExoPlayer
            Map<String, String> headers = session.getHeaders();
            String rangeHeader = headers.get("range");  // ExoPlayer шлёт Range-заголовки

            long fileSize = torrentHandle.torrentFile().files().fileSize(0);
            long start = 0, end = fileSize - 1;

            if (rangeHeader != null) {
                // Парсим Range-заголовок (например, "bytes=0-1023")
                String[] range = rangeHeader.replace("bytes=", "").split("-");
                start = Long.parseLong(range[0]);
                if (range.length > 1) end = Long.parseLong(range[1]);
            }

            // Получаем данные из торрента
            byte[] data = new byte[(int)(end - start + 1)];
            torrentHandle.readPiece(0);  // Упрощённо!

            Response response = newFixedLengthResponse(
                    Response.Status.PARTIAL_CONTENT,
                    "video/mp4",
                    Arrays.toString(data)
            );
            response.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            response.addHeader("Accept-Ranges", "bytes");
            return response;
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
    }
}