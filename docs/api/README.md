# TorrServer API для FloraFilm

Этот документ представляет собой руководство по использованию сгенерированного Java API для взаимодействия с TorrServer. API имеет двухуровневую структуру: высокоуровневый, простой в использовании API и низкоуровневый API, который напрямую соответствует эндпоинтам сервера.

---

## 🚀 Быстрый старт: Высокоуровневый API (`SimpleStreamingApi`)

Для большинства задач следует использовать `SimpleStreamingApi`. Он предоставляет простой и интуитивно понятный интерфейс для таких распространенных операций, как запуск, остановка и мониторинг торрент-стримов.

**Расположение:** `com.alaka_ala.florafilm.ui.util.coreMatrix.api.SimpleStreamingApi`

### Пример: Запуск видеострима

Вот типичный пример того, как начать стриминг видео по magnet-ссылке.

```java
// 1. Укажите базовый URL вашего сервера
String serverUrl = "http://127.0.0.1:8090";

// 2. Создайте экземпляр высокоуровневого API
SimpleStreamingApi streamingApi = new SimpleStreamingApi(serverUrl);

// 3. Определите magnet-ссылку и необязательные метаданные
String magnetLink = "magnet:?xt=urn:btih:ВАШ_ТОРРЕНТ_ХЭШ";
String movieTitle = "Мой потрясающий фильм";
String posterUrl = "https://example.com/poster.jpg";

try {
    // 4. Добавьте торрент на сервер для начала загрузки/стриминга
    TorrentStatus status = streamingApi.startStreaming(magnetLink, movieTitle, posterUrl);
    String torrentHash = status.getHash();
    
    System.out.println("Торрент успешно добавлен. Хэш: " + torrentHash);

    // 5. Асинхронно дождитесь, пока торрент будет готов к воспроизведению
    // Этот метод ожидает загрузки метаданных и начального фрагмента данных.
    streamingApi.waitForReady(torrentHash, 60).thenAccept(readyStatus -> {
        
        // 6. Получите прямую ссылку на стриминг для первого файла (индекс 0)
        // Вы можете изучить readyStatus.getFileStats() для поиска нужного индекса файла
        String playbackUrl = streamingApi.getFileStreamUrl(readyStatus.getHash(), 0);
        
        System.out.println("Стрим готов! URL для воспроизведения: " + playbackUrl);

        // 7. Передайте этот URL вашему видеоплееру (например, ExoPlayer)
        // exoPlayer.setMediaItem(MediaItem.fromUri(playbackUrl));
        // exoPlayer.prepare();
        // exoPlayer.play();

    }).exceptionally(ex -> {
        // Этот блок выполнится, если торрент не будет готов в течение 60 секунд
        System.err.println("Тайм-аут или ошибка ожидания стрима: " + ex.getMessage());
        return null;
    });

} catch (SimpleStreamingApi.StreamingException e) {
    // Этот блок выполнится, если не удалось добавить торрент
    System.err.println("Не удалось запустить стриминг: " + e.getMessage());
}
```

### Другие ключевые методы

-   `stopStreaming(String torrentHash)`: Останавливает и удаляет торрент.
-   `getTorrentStatus(String torrentHash)`: Получает актуальный статус торрента.
-   `search(String query)`: Выполняет поиск торрентов.
-   `getPlaylistUrl(String torrentHash)`: Возвращает URL на M3U плейлист.

---

## 🛠️ Продвинутое использование: Низкоуровневый API (`TorrServeApi`)

`TorrServeApi` — это низкоуровневый клиент, который обеспечивает прямое соответствие (1-в-1) с каждым эндпоинтом, определенным в Swagger-документации. Этот API следует использовать только тогда, когда вам нужен тонкий контроль, который не предоставляет `SimpleStreamingApi`.

**Расположение:** `com.alaka_ala.florafilm.ui.util.coreMatrix.api.TorrServeApi`

### Пример: Получение настроек сервера

```java
TorrServeApi rawApi = new TorrServeApi("http://127.0.0.1:8090");

try {
    BTSettings settings = rawApi.getSettings();
    System.out.println("Текущий размер кэша: " + settings.getCacheSize() + " MB");
} catch (TorrServeApi.ApiException e) {
    System.err.println("Не удалось получить настройки: " + e.getMessage());
}
```

---

## 📦 Модели данных

Все структуры данных (такие как `TorrentStatus`, `TorrentDetails`, `BTSettings` и т.д.) определены как простые Java-объекты (POJO) в следующем пакете:

`com.alaka_ala.florafilm.ui.util.coreMatrix.api.model`

Эти модели используются как высокоуровневым, так и низкоуровневым API для запросов и ответов. Они аннотированы для легкой сериализации и десериализации с помощью Gson.
