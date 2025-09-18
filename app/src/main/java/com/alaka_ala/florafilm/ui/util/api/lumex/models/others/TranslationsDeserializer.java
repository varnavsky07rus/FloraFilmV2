package com.alaka_ala.florafilm.ui.util.api.lumex.models.others;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslationsDeserializer implements JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<String> translations = new ArrayList<>();

        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                translations.add(element.isJsonNull() ? "" : element.getAsString());
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            // Собираем значения из объекта по ключам в порядке их натурального сортировки
            obj.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        JsonElement val = entry.getValue();
                        translations.add(val.isJsonNull() ? "" : val.getAsString());
                    });
        } else if (json.isJsonPrimitive()) {
            // Если это одиночная строка — просто добавляем в список
            translations.add(json.getAsString());
        } else if (json.isJsonNull()) {
            // пустой список
        } else {
            throw new JsonParseException("Unexpected JSON type for translations: " + json);
        }

        return translations;
    }
}
