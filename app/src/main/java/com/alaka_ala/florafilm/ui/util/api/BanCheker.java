package com.alaka_ala.florafilm.ui.util.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class BanCheker {
    public static final String FILE_NAME = "ban_list";
    public static final String FILE_URL_PATH = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/" + FILE_NAME;

    public void loadList() {
        new Thread(() -> {
            try {
                URL url = new URL(FILE_URL_PATH);
                InputStream inputStream = url.openStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] split = line.split("\n");
                    if (split.length == 2) {
                        String id = split[0];
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}
