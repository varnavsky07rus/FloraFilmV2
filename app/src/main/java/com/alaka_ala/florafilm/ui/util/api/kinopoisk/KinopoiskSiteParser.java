package com.alaka_ala.florafilm.ui.util.api.kinopoisk;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KinopoiskSiteParser {

    public interface CallbackStaffPhoto {
        void onSuccess(List<String> listImg);

        void onFailure(IOException e);
    }

    /**
     * Делает ссылку абсолютной, если она относительная. пример: //site.com -> https://site.com
     */
    private static String makeAbsoluteUrl(String url) {
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        return url;
    }

    /**
     * Метод служит для извлечения изображений из каждого элемента srcset.
     * Используется в методе {@link KinopoiskSiteParser#getStaffPhoto(WebView, int, CallbackStaffPhoto)}
     */
    private static List<String> extractBestQualityFromEachSrcsetSimple(String html) {
        Set<String> imageUrls = new LinkedHashSet<>();
        Document doc = Jsoup.parse(html);

        Elements imgTags = doc.select("img[src*='get-kinopoisk-image']");

        for (Element img : imgTags) {
            String srcset = img.attr("srcset");
            if (!srcset.isEmpty()) {
                // Просто берем последний URL из srcset (обычно самый качественный)
                String[] parts = srcset.split("\\s*,\\s*");
                String lastUrl = parts[parts.length - 1].trim().split("\\s+")[0];
                imageUrls.add(makeAbsoluteUrl(lastUrl));
            } else {
                String src = img.attr("abs:src");
                imageUrls.add(src);
            }
        }

        return new ArrayList<>(imageUrls);
    }


    private int page = 0;
    private boolean pageFinished = false;

    @SuppressLint("SetJavaScriptEnabled")
    /** Получение фото актера из кинопоиска */
    public void getStaffPhoto(WebView webView, int staffId, CallbackStaffPhoto callback) {
        ++page;
        String url = "https://www.kinopoisk.ru/name/" + staffId + "/photos/page/" + page;

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDatabaseEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 YaBrowser/25.8.0.0 Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pageFinished = false; // <<< 2. Сбрасываем флаг при начале загрузки новой страницы
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // <<< 3. Проверяем флаг. Если код уже выполнялся, выходим.
                if (pageFinished) {
                    return;
                }
                pageFinished = true; // <<< 4. Устанавливаем флаг, чтобы предотвратить повторный запуск

                // Добавляем небольшую задержку, чтобы все скрипты на странице успели отработать
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    view.evaluateJavascript(
                            "(function() { return document.documentElement.outerHTML; })();",
                            html -> {
                                if (html != null && !html.equals("null")) {
                                    // --- ВАЖНОЕ ИСПРАВЛЕНИЕ ВАШЕГО КОДА ---
                                    // Вы используете Gson, это самый правильный способ.
                                    // Не нужно делать ручные замены и использовать StringEscapeUtils.
                                    // Просто передайте JSON-строку в Gson.
                                    String unescapedHtml = new com.google.gson.Gson().fromJson(html, String.class);

                                    List<String> listImg = extractBestQualityFromEachSrcsetSimple(unescapedHtml);
                                    if (listImg.isEmpty()) {
                                        callback.onFailure(new IOException("Фото не найдены. Возможно, страница пуста или сработала CAPTCHA."));
                                    } else {
                                        callback.onSuccess(listImg);
                                    }
                                } else {
                                    callback.onFailure(new IOException("Не удалось получить HTML из WebView."));
                                }
                            });
                }, 1500); // Задержка 1.5 секунды
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    callback.onFailure(new IOException("Ошибка WebView: " + error.getDescription()));
                }
            }
        });

        webView.loadUrl(url);
    }


}
