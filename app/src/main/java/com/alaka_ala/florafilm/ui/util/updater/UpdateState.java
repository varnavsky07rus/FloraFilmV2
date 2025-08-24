package com.alaka_ala.florafilm.ui.util.updater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * Класс, представляющий состояние процесса обновления.
 * Является неизменяемым (immutable) для потокобезопасности.
 */
public final class UpdateState {

    public enum Status {
        IDLE,                   // Бездействие
        CHECKING_VERSION,       // Проверка версии
        UPDATE_AVAILABLE,       // Обновление доступно
        NO_UPDATE,              // Обновлений нет
        DOWNLOADING,            // Идет загрузка
        DOWNLOADED,             // Загрузка завершена
        ERROR                   // Произошла ошибка
    }

    @NonNull
    public final Status status;
    public final int progress;
    @Nullable
    public final File apkFile;
    @Nullable
    public final String errorMessage;

    // Приватные конструкторы для каждого состояния
    private UpdateState(@NonNull Status status, int progress, @Nullable File apkFile, @Nullable String errorMessage) {
        this.status = status;
        this.progress = progress;
        this.apkFile = apkFile;
        this.errorMessage = errorMessage;
    }

    // Статические фабричные методы для создания состояний
    public static UpdateState idle() {
        return new UpdateState(Status.IDLE, 0, null, null);
    }

    public static UpdateState checking() {
        return new UpdateState(Status.CHECKING_VERSION, 0, null, null);
    }

    public static UpdateState updateAvailable() {
        return new UpdateState(Status.UPDATE_AVAILABLE, 0, null, null);
    }

    public static UpdateState noUpdate() {
        return new UpdateState(Status.NO_UPDATE, 0, null, null);
    }

    public static UpdateState downloading(int progress) {
        return new UpdateState(Status.DOWNLOADING, progress, null, null);
    }

    public static UpdateState downloaded(@NonNull File apkFile) {
        return new UpdateState(Status.DOWNLOADED, 100, apkFile, null);
    }

    public static UpdateState error(@NonNull String message) {
        return new UpdateState(Status.ERROR, 0, null, message);
    }
}